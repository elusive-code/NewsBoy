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
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.logging.Logger;

/**
 * <p>Stores event handling information, namely <br>
 *  <ul>
 *     <li>method that performs handling
 *     <li>target object on which this method is invoked
 *     <li>event type
 *  </ul>
 * </p>
 *
 * @see com.elusive_code.newsboy.Subscribe
 * @see AsyncEventService
 * @author Vladislav Dolgikh
 */
public class WeakEventHandler {
    public static final Logger LOG = Logger.getLogger(WeakEventHandler.class.getName());

    private WeakReference target;
    private Method        method;
    private Class         eventType;

    /**
     * <p>Constructor that accepts target object and method that will perform event handling</p>
     * <p>
     *     If method has 1 argument this argument is used as event type.<br>
     *     If method has no arguments than parameter of
     *     {@link com.elusive_code.newsboy.Subscribe} annotation is used.
     *     (It has default value of {@link java.lang.Object},
     *     which means supplied method will be invoked on every event)
     * </p>
     * <p> Also event type's hierarchy is collected</p>
     * @param target object on which event handling method will be invoked
     * @param method method that performs event handling
     */
    public WeakEventHandler(Object target, Method method) {
        if (target == null) throw new IllegalArgumentException("Target is null");
        if (method == null) throw new IllegalArgumentException("Method is null");

        Subscribe annotation = method.getAnnotation(Subscribe.class);

        //if method has no arguments it will be subscribed to all events
        Class[] args = method.getParameterTypes();
        if (args == null || args.length != 1) {
            this.eventType = annotation.eventType();
            LOG.fine("Method {" + method + "} of type " + target.getClass() +
                     " marked with @Subscribe but has no arguments, it will be subscribed to " + this.eventType);
        } else {
            this.eventType = args[0];
        }

        this.target = new WeakReference(target);
        this.method = method;
    }

    /**
     * Returns object on which event handling method will be invoked
     * @return event listener
     */
    public Object getTarget() {
        return target.get();
    }

    /**
     * Returns method that will be invoked for event handling
     * @return event handling method
     */
    public Method getMethod() {
        return method;
    }

    /**
     * If event handling method has one argument it is Class of that argument
     * otherwise it is eventType of {@link Subscribe#eventType()}
     * @return event type
     */
    public Class getEventType() {
        return eventType;
    }

    /**
     * <p>Invokes event handling method.</p>
     * <p>If method accepts 1 parameter it will pass it event object,
     * otherwise it will invoke it without parameters</p>
     * @param event event being notified of
     * @return results that were return by event handling method
     */
    public Object handleEvent(Object event) throws Throwable {
        Object target = this.target.get();
        if (target == null) throw new WeakReferenceCollectedException();
        try {
            if (method.getParameterTypes().length == 1) {
                return method.invoke(target, event);
            } else {
                return method.invoke(target);
            }
        }catch (InvocationTargetException ex) {
            throw ex.getCause();
        }
    }

    @Override
    public String toString() {
        return "WeakEventHandler{ " + target.get() + " # " + method + " }";
    }
}
