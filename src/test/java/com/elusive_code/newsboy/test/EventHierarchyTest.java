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

package com.elusive_code.newsboy.test;

import com.elusive_code.newsboy.AsyncEventService;
import com.elusive_code.newsboy.NotificationFuture;
import com.elusive_code.newsboy.Subscribe;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@RunWith(JUnit4.class)
public class EventHierarchyTest {

    public void test(Object event, Set<Method> triggeredMethods) throws Exception{
        AsyncEventService eventService = new AsyncEventService();
        eventService.subscribe(this);

        List<NotificationFuture> notifications = eventService.publish(event);

        for (NotificationFuture f: notifications) {
            boolean removed = triggeredMethods.remove(f.getMethod());
            Assert.assertTrue("Event "+event+" unexpectedly triggered method "+f.getMethod(),
                              removed);
        }

        Assert.assertTrue("Event "+event+" didn't trigger methods " + triggeredMethods,
                          triggeredMethods.isEmpty());
    }


    @Test
    public void testObjectEvent() throws Exception {
        Set<Method> triggeredMethods = new HashSet<>();
        triggeredMethods.add(EventHierarchyTest.class.getMethod("OnObjectEvent"));
        triggeredMethods.add(EventHierarchyTest.class.getMethod("OnObjectEvent",Object.class));

        Object event = new Object();

        test(event,triggeredMethods);
    }

    @Test
    public void testParentEvent1() throws Exception {
        Set<Method> triggeredMethods = new HashSet<>();
        triggeredMethods.add(EventHierarchyTest.class.getMethod("OnObjectEvent"));
        triggeredMethods.add(EventHierarchyTest.class.getMethod("OnObjectEvent",Object.class));

        triggeredMethods.add(EventHierarchyTest.class.getMethod("OnParentEvent1"));
        triggeredMethods.add(EventHierarchyTest.class.getMethod("OnParentEvent1",ParentEvent1.class));

        triggeredMethods.add(EventHierarchyTest.class.getMethod("OnEventInterface1"));
        triggeredMethods.add(EventHierarchyTest.class.getMethod("OnEventInterface1",EventInterface1.class));

        Object event = new ParentEvent1();

        test(event,triggeredMethods);
    }

    @Test
    public void testParentEvent2() throws Exception {
        Set<Method> triggeredMethods = new HashSet<>();
        triggeredMethods.add(EventHierarchyTest.class.getMethod("OnObjectEvent"));
        triggeredMethods.add(EventHierarchyTest.class.getMethod("OnObjectEvent",Object.class));

        triggeredMethods.add(EventHierarchyTest.class.getMethod("OnParentEvent2"));
        triggeredMethods.add(EventHierarchyTest.class.getMethod("OnParentEvent2",ParentEvent2.class));

        triggeredMethods.add(EventHierarchyTest.class.getMethod("OnEventInterface1"));
        triggeredMethods.add(EventHierarchyTest.class.getMethod("OnEventInterface1",EventInterface1.class));

        triggeredMethods.add(EventHierarchyTest.class.getMethod("OnEventInterface2"));
        triggeredMethods.add(EventHierarchyTest.class.getMethod("OnEventInterface2",EventInterface2.class));

        Object event = new ParentEvent2();

        test(event,triggeredMethods);
    }

    @Test
    public void testChildEvent1() throws Exception {
        Set<Method> triggeredMethods = new HashSet<>();
        triggeredMethods.add(EventHierarchyTest.class.getMethod("OnObjectEvent"));
        triggeredMethods.add(EventHierarchyTest.class.getMethod("OnObjectEvent",Object.class));

        triggeredMethods.add(EventHierarchyTest.class.getMethod("OnChildEvent1"));
        triggeredMethods.add(EventHierarchyTest.class.getMethod("OnChildEvent1",ChildEvent1.class));

        triggeredMethods.add(EventHierarchyTest.class.getMethod("OnParentEvent1"));
        triggeredMethods.add(EventHierarchyTest.class.getMethod("OnParentEvent1",ParentEvent1.class));

        triggeredMethods.add(EventHierarchyTest.class.getMethod("OnEventInterface1"));
        triggeredMethods.add(EventHierarchyTest.class.getMethod("OnEventInterface1",EventInterface1.class));

        triggeredMethods.add(EventHierarchyTest.class.getMethod("OnChildEventInterface"));
        triggeredMethods.add(EventHierarchyTest.class.getMethod("OnChildEventInterface",ChildEventInterface.class));

        Object event = new ChildEvent1();

        test(event,triggeredMethods);
    }

    @Test
    public void testChildEvent1_2() throws Exception {
        Set<Method> triggeredMethods = new HashSet<>();
        triggeredMethods.add(EventHierarchyTest.class.getMethod("OnObjectEvent"));
        triggeredMethods.add(EventHierarchyTest.class.getMethod("OnObjectEvent",Object.class));

        triggeredMethods.add(EventHierarchyTest.class.getMethod("OnChildEvent2"));
        triggeredMethods.add(EventHierarchyTest.class.getMethod("OnChildEvent2",ChildEvent2.class));

        triggeredMethods.add(EventHierarchyTest.class.getMethod("OnParentEvent2"));
        triggeredMethods.add(EventHierarchyTest.class.getMethod("OnParentEvent2",ParentEvent2.class));

        triggeredMethods.add(EventHierarchyTest.class.getMethod("OnEventInterface1"));
        triggeredMethods.add(EventHierarchyTest.class.getMethod("OnEventInterface1",EventInterface1.class));

        triggeredMethods.add(EventHierarchyTest.class.getMethod("OnEventInterface2"));
        triggeredMethods.add(EventHierarchyTest.class.getMethod("OnEventInterface2",EventInterface2.class));

        Object event = new ChildEvent2();

        test(event,triggeredMethods);
    }

    @Test
    public void testChildEvent2() throws Exception {
        Set<Method> triggeredMethods = new HashSet<>();
        triggeredMethods.add(EventHierarchyTest.class.getMethod("OnObjectEvent"));
        triggeredMethods.add(EventHierarchyTest.class.getMethod("OnObjectEvent",Object.class));

        triggeredMethods.add(EventHierarchyTest.class.getMethod("OnChildEvent1_2"));
        triggeredMethods.add(EventHierarchyTest.class.getMethod("OnChildEvent1_2",ChildEvent1_2.class));

        triggeredMethods.add(EventHierarchyTest.class.getMethod("OnParentEvent1"));
        triggeredMethods.add(EventHierarchyTest.class.getMethod("OnParentEvent1",ParentEvent1.class));

        triggeredMethods.add(EventHierarchyTest.class.getMethod("OnEventInterface1"));
        triggeredMethods.add(EventHierarchyTest.class.getMethod("OnEventInterface1",EventInterface1.class));

        Object event = new ChildEvent1_2();

        test(event,triggeredMethods);
    }



    @Subscribe
    public void OnObjectEvent() {}

    @Subscribe
    public void OnObjectEvent(Object event) {}

    @Subscribe(eventType = EventInterface1.class)
    public void OnEventInterface1() {}

    @Subscribe
    public void OnEventInterface1(EventInterface1 event) {}

    @Subscribe(eventType = EventInterface2.class)
    public void OnEventInterface2() {}

    @Subscribe
    public void OnEventInterface2(EventInterface2 event) {}

    @Subscribe(eventType = ParentEvent1.class)
    public void OnParentEvent1() {}

    @Subscribe
    public void OnParentEvent1(ParentEvent1 event) {}

    @Subscribe(eventType = ParentEvent2.class)
    public void OnParentEvent2() {}

    @Subscribe
    public void OnParentEvent2(ParentEvent2 event) {}

    @Subscribe(eventType = ChildEventInterface.class)
    public void OnChildEventInterface() {}

    @Subscribe
    public void OnChildEventInterface(ChildEventInterface event) {}

    @Subscribe(eventType = ChildEvent1.class)
    public void OnChildEvent1() {}

    @Subscribe
    public void OnChildEvent1(ChildEvent1 event) {}

    @Subscribe(eventType = ChildEvent1_2.class)
    public void OnChildEvent1_2() {}

    @Subscribe
    public void OnChildEvent1_2(ChildEvent1_2 event) {}

    @Subscribe(eventType = ChildEvent2.class)
    public void OnChildEvent2() {}

    @Subscribe
    public void OnChildEvent2(ChildEvent2 event) {}


    public static interface EventInterface1 {}

    public static interface EventInterface2 extends EventInterface1 {}

    public static class ParentEvent1 implements EventInterface1 {}

    public static class ParentEvent2 implements EventInterface2 {}

    public static interface ChildEventInterface {}

    public static class ChildEvent1 extends ParentEvent1 implements ChildEventInterface {}

    public static class ChildEvent1_2 extends ParentEvent1 {}

    public static class ChildEvent2 extends ParentEvent2 {}

}
