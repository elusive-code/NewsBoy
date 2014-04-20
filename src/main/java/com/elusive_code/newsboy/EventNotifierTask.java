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

import org.apache.commons.lang3.ArrayUtils;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.concurrent.RecursiveTask;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * <p>Simple task that performs notification</p>
 * <p>If listener was claimed by garbage collector before event handling than
 * {@link com.elusive_code.newsboy.WeakReferenceCollectedException} is thrown</p>
 *
 * @author Vladislav Dolgikh
 */
public class EventNotifierTask extends RecursiveTask implements NotificationFuture {

    private static final Logger LOG = Logger.getLogger(EventNotifierTask.class.getName());

    private WeakEventHandler eventHandler;
    private Object           event;
    private EventSource      source;
    private EventStackTrace  eventStackTrace;

    public EventNotifierTask(WeakEventHandler handler, Object event) {
        this(handler, event, null);
    }

    public EventNotifierTask(WeakEventHandler handler, Object event, EventSource source) {
        this(handler, event, source, null);
    }

    public EventNotifierTask(WeakEventHandler handler, Object event, EventSource source, EventStackTrace stackTrace) {
        super();
        this.eventHandler = handler;
        this.event = event;
        this.source = source;
        this.eventStackTrace = stackTrace;
    }

    @Override
    public Object getListener() {
        return eventHandler.getTarget();
    }

    @Override
    public Method getMethod() {
        return eventHandler.getMethod();
    }

    @Override
    public Object getEvent() {
        return event;
    }

    public EventStackTrace getEventStackTrace() {
        return eventStackTrace;
    }

    @Override
    protected Object compute() {
        try {
            return eventHandler.handleEvent(event, source);
        } catch (WeakReferenceCollectedException ex) {
            LOG.log(Level.WARNING, ex.getMessage());
            completeExceptionally(ex);
            return null;
        } catch (Throwable ex) {
            LOG.log(Level.WARNING, "Failed to invoke " + eventHandler + " with " + event + "\n", ex);
            updateStackTrace(ex);
            completeExceptionally(ex);
            return null;
        }
    }

    private void updateStackTrace(Throwable ex) {
        if (eventStackTrace == null) return;
        try {
            StackTraceElement[] stack1 = ex.getStackTrace();
            StackTraceElement[] stack2 = eventStackTrace.getStackTrace();
            StackTraceElement[] result = ArrayUtils.addAll(stack1, stack2);
            ex.setStackTrace(result);
        } catch (Throwable t) {
            LOG.log(Level.FINE, "Failed to update stack trace for " + ex, t);
        }
    }
}
