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

/**
 * <p>Exception thrown when attempting to notify event listener
 * that no longer exists (claimed by garbage collector). </p>
 * <p>It may happen when GC invoked after event notification scheduled,
 * but before actual handling performed.
 * This period of time could be long if there are a lot of ordered events scheduled.
 * </p>
 * @author Vladislav Dolgikh
 */
public class WeakReferenceCollectedException extends IllegalStateException {

    public WeakReferenceCollectedException() {
        super("WeakReference already claimed by GC, no event notification needed");
    }
}
