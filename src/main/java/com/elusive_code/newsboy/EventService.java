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

import java.util.List;

/**
 * <p>Publish-subscribe style communication</p>
 * <p>
 *     To receive events object must have public method with zero or one argument
 *     marked with {@link com.elusive_code.newsboy.Subscribe} annotation.<br>
 *     This object should be passed to {@link com.elusive_code.newsboy.EventService#subscribe(Object)}.<br>
 * </p>
 *     <ul>
 *     <li>If event handling method has one argument, listener will be notified of the events
 *     that fit the type of that argument.<br>
 *     <li>If event handling method has no arguments, listener will notified of the events that fit {@link Subscribe#eventType()}
 *     </ul>
 * @see com.elusive_code.newsboy.Subscribe
 * @see AsyncEventService
 * @author Vladislav Dolgikh
 */
public interface EventService {

    /**
     * <p>Subscribe for events from this EventService.</p>
     *
     * <p>Subscriber should have method or methods marked with
     * {@link com.elusive_code.newsboy.Subscribe}
     * annotation.<br>
     * If annotated method has no arguments, it still will be subscribed and notified
     * of the events of type specified in annotation parameter.
     * </p>
     *
     * @param listener listener to receive events
     * @see com.elusive_code.newsboy.Subscribe
     */
    void subscribe(Object listener);

    /**
     * <p>Unsubscribe listener from events from this EventService</p>
     * @param listener listener to
     */
    void unsubscribe(Object listener);

    /**
     * <p>Publish event to this EventService.</p>
     * <p>No delivery order guaranteed.</p>
     * @param event event to notify of
     * @return list of {@link com.elusive_code.newsboy.NotificationFuture} that represent scheduled notifications
     */
    List<NotificationFuture> publish(Object event);

    /**
     *
     * <p>Publish event to this EventService.</p>
     * <p>Guaranteed to deliver event in the same order it was published
     * relative to other <b>ordered</b> events</p>
     * @param event event to notify of
     * @return list of {@link com.elusive_code.newsboy.NotificationFuture} that represent scheduled notifications
     */
    List<NotificationFuture> publishOrdered(Object event);
    
}
