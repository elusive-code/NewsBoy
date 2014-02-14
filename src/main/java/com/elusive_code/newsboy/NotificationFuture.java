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

import java.lang.reflect.Method;
import java.util.concurrent.Future;

/**
 * <p>Future that represent notification results</p>
 * <p>Adds notification information to the {@link java.util.concurrent.Future}:
 * <ul>
 *     <li>event that triggered notification
 *     <li>listener being notified
 *     <li>method of the listener used to handle event
 * </ul>
 * </p>
 * @see java.util.concurrent.Future
 * @see com.elusive_code.newsboy.EventService
 * @author Vladislav Dolgikh
 */
public interface NotificationFuture extends Future {

    /**
     * Returns listener being notified
     */
    Object getListener();

    /**
     * Returns method of the listener handling notification
     */
    Method getMethod();

    /**
     * Returns triggered event
     */
    Object getEvent();

}
