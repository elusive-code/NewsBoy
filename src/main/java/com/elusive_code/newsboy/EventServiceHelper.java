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

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;

/**
 * <p>Helper class that contain static methods</p>
 * @author Vladislav Dolgikh
 */
public class EventServiceHelper {

    private EventServiceHelper(){}

    /**
     * <p>Looks for objects' methods marked with {@link com.elusive_code.newsboy.Subscribe} annotation and
     * creates a list of {@link com.elusive_code.newsboy.WeakEventHandler} for this object</p>
     *
     * @param object for
     * @return list of created event handlers
     */
    public static LinkedList<WeakEventHandler> createObjectEventHandlers(Object object) {

        Class clazz = object.getClass();
        LinkedList<WeakEventHandler> handlers = new LinkedList<>();

        for (Method m : clazz.getMethods()) {
            for (Annotation a : m.getAnnotations()) {
                if (Subscribe.class.equals(a.annotationType())) {
                    handlers.add(new WeakEventHandler(object, m));
                }
            }
        }

        return handlers;
    }

    /**
     * <p>Recursively collects class hierarchy.</p>
     *
     * <p>Convenience overload for {@link #collectClassHierarchy(Class, java.util.Set)}
     * with second parameter set to null.</p>
     *
     * @param clazz class which hierarchy to collect
     * @return collection of classes that form hierarchy of supplied class
     */
    public static Set<Class> collectClassHierarchy(Class clazz) {
        return collectClassHierarchy(clazz,null);
    }

    /**
     * <p>Recursively collects all parent classes and interfaces of the supplied class.</p>
     *
     * <p>If second {@link java.util.Set} parameter is not null it puts collected classes into it and returns it,
     * otherwise creates new set.</p>
     *
     * @param clazz class which hierarchy to collect
     * @param result set to put collected classes into, if null new one created
     * @return {@link java.util.Set} of classes that form hierarchy of supplied class
     */
    public static Set<Class> collectClassHierarchy(Class clazz, Set<Class> result) {

        if (result == null) result = new HashSet<>();
        result.add(clazz);

        //interfaces
        for (Class c : clazz.getInterfaces()) {
            collectClassHierarchy(c, result);
        }

        //parent class
        Class c = clazz.getSuperclass();
        if (c != null) {
            collectClassHierarchy(c, result);
        }

        return result;
    }

}
