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
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * <p>Stores event handling information, namely</p>
 *  <ul>
 *     <li>method that performs handling
 *     <li>target object on which this method is invoked
 *     <li>event type
 *  </ul>
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
    private int           sourceParameter;
    private int           eventParameter;

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

        this.target = new WeakReference(target);
        this.method = method;

        Subscribe annotation = method.getAnnotation(Subscribe.class);
        this.sourceParameter = annotation.eventSourceParameter();
        if (this.sourceParameter < 0) this.sourceParameter = -1;

        //if method has no arguments it will be subscribed to all events
        Class[] args = method.getParameterTypes();

        if (this.sourceParameter >= args.length) {
            throw new IllegalArgumentException(
                    "eventSourceParameter is '"+this.sourceParameter+"' but method has "+args.length+" arguments");
        }

        if (this.sourceParameter<0){
            //attempting to determine source parameter when not specified in annotation
            for (int i=0; i<args.length; i++) {
                Class c = args[i];
                if (EventSource.class.isAssignableFrom(c)){
                    if (this.sourceParameter<0) {
                        this.sourceParameter = i;
                    } else {
                        throw new IllegalArgumentException(
                                "Ambiguous EventSource parameters: #"+this.sourceParameter+" and #"+i);
                    }
                }
            }
            if (args.length == 1 && this.sourceParameter == 0 && annotation.eventSourceParameter() <= -2) {
                throw new IllegalArgumentException("Ambiguous EventSource, not sure if event type or source");
            }
        }



        if (args.length >= 3 || args.length == 2 && this.sourceParameter < 0){
            throw new IllegalArgumentException("Method has too many arguments, don't know for which to subscribe");
        }

        if (args.length == 2) {
            this.eventParameter = args.length - 1 - this.sourceParameter;
        } else if (args.length == 1) {
            this.eventParameter = -1 - this.sourceParameter;
        } else {
            this.eventParameter = -1;
        }

        if (this.eventParameter>=0){
            this.eventType = args[this.eventParameter];
        } else {
            this.eventType = annotation.eventType();
        }

        if(LOG.isLoggable(Level.FINE)){
            LOG.fine("Subscribed {"+target+"}" +
                     " method {"+method+"}" +
                     " for events {"+this.eventType+"}" +
                     " eventParameter="+this.eventParameter +
                     " sourceParameter="+this.sourceParameter);
        }
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
     * @throws java.lang.Throwable exception produced by listener
     */
    public Object handleEvent(Object event) throws Throwable {
        return handleEvent(event, null);
    }


    /**
     * <p>Invokes event handling method.</p>
     * <p>If method accepts 1 parameter it will pass it event object,
     * otherwise it will invoke it without parameters</p>
     * @param event event being notified of
     * @param source event source that produced event
     * @return results that were return by event handling method
     * @throws java.lang.Throwable exception produced by listener
     */
    public Object handleEvent(Object event, EventSource source) throws Throwable {
        Object target = this.target.get();
        if (target == null) throw new WeakReferenceCollectedException();
        try {

            Object[] args = new Object[method.getParameterTypes().length];
            for (int i = 0; i < args.length; i++) {
                if (i == this.sourceParameter) {
                    args[i] = source;
                } else if (i == eventParameter) {
                    args[i] = event;
                } else {
                    args[i] = null;
                }
            }

            return method.invoke(target,args);
        }catch (InvocationTargetException ex) {
            throw ex.getCause();
        }
    }


    @Override
    public String toString() {
        return "WeakEventHandler{ " + target.get() + " # " + method + " }";
    }
}
