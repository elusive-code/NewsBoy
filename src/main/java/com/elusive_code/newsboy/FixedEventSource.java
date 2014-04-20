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

import java.util.Collection;

/**
 * Event source that produces only certain event types.
 * @see com.elusive_code.newsboy.EventSource
 */
public interface FixedEventSource extends EventSource {

    /**
     * Collection of event classes produced by this event source.
     *
     * @return collection of event classes
     */
    Collection<Class> getProducedEvents();

}
