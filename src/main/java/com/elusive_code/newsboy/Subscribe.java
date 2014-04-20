/*
Copyright 2014. Vladislav Dolgikh

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
 */

package com.elusive_code.newsboy;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * <p>Marks event handling method.</p>
 * <p>
 *     If method has one argument than type of that argument is type of event for subscription,
 *     otherwise {@link #eventType()} used.
 * </p>
 * @author Vladislav Dolgikh
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Subscribe {

    /**
     * <p>
     * When method has no arguments, it still could be
     * subscribed to events, and this parameter specifies which ones.
     * </p>
     * @return class of the events to subscribe
     */
    Class eventType() default Object.class;

    /**
     * <p>
     *     This parameter could be used to mark method argument
     *     in which event source should be passed in ambiguous cases.
     * </p>
     * <p>
     *     Use -1, if you don't want to pass event source.
     *     (for example you want to subscribe to 'EventSource'-type events)
     * </p>
     * @return index of argument for EventSource to be passed
     */
    int eventSourceParameter() default -2;

}
