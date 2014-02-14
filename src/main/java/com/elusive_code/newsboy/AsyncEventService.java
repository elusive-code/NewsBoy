/*
 * Copyright 2014. Vladislav Dolgikh
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.elusive_code.newsboy;

import java.lang.ref.WeakReference;
import java.util.*;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveAction;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Logger;

/**
 * <p>
 *     Implementation of {@link com.elusive_code.newsboy.EventService} that uses
 *     <ul>
 *         <li>weak references to store subscribers
 *         <li>asynchronous, and uses Fork-Join framework to schedule notifications
 *     </ul>
 *
 * </p>
 *
 * @see com.elusive_code.newsboy.EventService
 * @see com.elusive_code.newsboy.Subscribe
 * @author Vladislav Dolgikh
 */
public class AsyncEventService implements EventService {

    private static final Logger LOG = Logger.getLogger(AsyncEventService.class.getName());

    /**
     * <p>Subscribed listeners. Used for faster unsubscribing</p>
     * <p>Key - listener object, Value - set of event handlers</p>
     */
    private WeakHashMap<Object, Collection<WeakEventHandler>> listeners = new WeakHashMap<>();

    /**
     * <p>Listeners by event class, used for faster publishing</p>
     * <p>Key - class of event to handle, Value - set of event handlers from all listeners</p>
     */
    private Map<Class, Set<WeakEventHandler>> listenersByClass = new HashMap<>();

    /**
     * Lock for synchronizing listeners' collections
     */
    private Lock listenersLock = new ReentrantLock();

    private PublishAction lastOrderedEvent = null;

    private ForkJoinPool notificatorPool;

    public AsyncEventService() {
        this(Runtime.getRuntime().availableProcessors());
    }

    public AsyncEventService(int parallelism) {
        notificatorPool = new ForkJoinPool(parallelism);
    }

    @Override
    public void subscribe(Object object) {
        if (object == null) return;

        Class clazz = object.getClass();
        LinkedList<WeakEventHandler> eventHandlers = EventServiceHelper.createObjectEventHandlers(object);

        listenersLock.lock();
        try {
            listeners.put(object, eventHandlers);
            for (WeakEventHandler handler : eventHandlers) {
                addListenerByClass(handler.getEventType(), handler);
//                for (Class c : handler.getEventTypeHierarchy()) {
//                    addListenerByClass(c, handler);
//                }
            }
        } finally {
            listenersLock.unlock();
        }
    }

    @Override
    public void unsubscribe(Object object) {
        if (object == null) return;

        listenersLock.lock();
        try {
            Collection<WeakEventHandler> handlers = listeners.remove(object);
            if (handlers == null || handlers.size() <= 0) return;

            for (WeakEventHandler handler : handlers) {
                removeListenerByClass(handler.getEventType(), handler);
//                for (Class c : handler.getEventTypeHierarchy()) {
//                    removeListenerByClass(c,handler);
//                }
            }
        } finally {
            listenersLock.unlock();
        }
    }

    /**
     * <p>Publish event to this EventService.</p>
     * <p>No delivery order guaranteed.</p>
     * <p>When using returned futures keep in mind they may fail with
     * {@link com.elusive_code.newsboy.WeakReferenceCollectedException}
     * in that case nothing should be done.
     * Listener was claimed by GC before event handling (but after event scheduling)</p>
     * @param event event to notify of
     * @return list of {@link com.elusive_code.newsboy.NotificationFuture} that represent scheduled notifications
     */
    @Override
    public List<NotificationFuture> publish ( Object event ) {
        if ( event == null ) return Collections.EMPTY_LIST;
        PublishAction task = new PublishAction ( event );
        notificatorPool.execute ( task );
        return new ArrayList<NotificationFuture>(task.getNotifiers());
    }

    /**
     * <p>Publish event to this EventService.</p>
     * <p>Guaranteed to deliver in the same order that was published
     * relative to other <b>ordered</b> events</p>
     * <p>When using returned futures keep in mind they may fail with
     * {@link com.elusive_code.newsboy.WeakReferenceCollectedException}
     * in that case nothing should be done.
     * Listener was claimed by GC before event handling (but after event scheduling)</p>
     * @param event event to notify of
     * @return list of {@link com.elusive_code.newsboy.NotificationFuture} that represent scheduled notifications
     */
    @Override
    @Subscribe
    public List<NotificationFuture> publishOrdered ( Object event ) {
        if ( event == null ) return Collections.EMPTY_LIST;
        lastOrderedEvent = new PublishAction ( event, lastOrderedEvent, true );
        notificatorPool.execute ( lastOrderedEvent );
        return new ArrayList<NotificationFuture>(lastOrderedEvent.getNotifiers());
    }

    private void addListenerByClass (Class clazz, WeakEventHandler handler) {
        Set handlers = listenersByClass.get(clazz);
        if (handlers == null) {
            handlers = new HashSet();
            listenersByClass.put(clazz,handlers);
        }
        handlers.add(handler);
    }

    private void removeListenerByClass(Class clazz, WeakEventHandler handler) {
        Set<WeakEventHandler> handlers = listenersByClass.get(clazz);
        if (handlers == null) return;
        handlers.remove(handler);
    }

    /**
     * Task that initiates event notifications and handles ordering
     */
    protected class PublishAction extends RecursiveAction {

        private Object                  event;
        private List<EventNotifierTask> notifiers;
        private PublishAction           previousEvent;
        private boolean                 ordered;

        public PublishAction(Object event) {
            this(event, null, false);
        }

        public PublishAction(Object event, PublishAction previousEvent, boolean ordered) {
            this.event = event;
            this.ordered = ordered;
            this.previousEvent = previousEvent;
            this.notifiers = Collections.unmodifiableList(collectNotifiers());
        }

        public List<EventNotifierTask> getNotifiers() {
            return notifiers;
        }

        /**
         * Collects all notifiers for current event
         * @return
         */
        private LinkedList<EventNotifierTask> collectNotifiers() {
            LinkedList<EventNotifierTask> notifiers = new LinkedList<>();

            listenersLock.lock();
            try {
                Set<Class> classes = EventServiceHelper.collectClassHierarchy(event.getClass());
                for (Class clazz : classes) {
                    Set<WeakEventHandler> handlers = listenersByClass.get(clazz);
                    if (handlers != null) {
                        Iterator<WeakEventHandler> i = handlers.iterator();
                        while (i.hasNext()) {
                            WeakEventHandler eventHandler = i.next();
                            Object listener = eventHandler.getTarget();
                            if (listener == null) {
                                //listener collected by GC
                                i.remove();
                            } else {
                                EventNotifierTask task = new EventNotifierTask(eventHandler, event);
                                notifiers.add(task);
                            }
                        }
                    }
                }
                return notifiers;
            } finally {
                listenersLock.unlock();
            }
        }

        protected void compute() {
            try {
                //if event ordered and it's not first one wait for it's turn
                if (previousEvent != null) {
                    previousEvent.quietlyJoin();
                }

                //scheduling notification
                for (EventNotifierTask task : getNotifiers()) {
                    task.fork();
                }

                //if event ordered we should wait for notifications to complete
                //so that next event won't fire until we notify of this one
                if (ordered) {
                    for (EventNotifierTask task : getNotifiers()) {
                        task.quietlyJoin();
                    }
                }
            } finally {
                //for processed events we need to set previous to null to prevent memory leak
                //(chaining events with hard references like current event->prev->prev->.....->first event)
                previousEvent = null;
            }
        }
    }
}
