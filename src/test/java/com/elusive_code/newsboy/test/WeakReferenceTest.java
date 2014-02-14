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

import java.util.Collection;
import java.util.concurrent.ExecutionException;

@RunWith(JUnit4.class)
public class WeakReferenceTest {

    private int listeners = 100000;

    @Test
    public void test() throws Throwable {
        AsyncEventService eventService = new AsyncEventService();
        System.out.println("Weak reference test being performed with listeners="+listeners);
        Runtime runtime = Runtime.getRuntime();

        for (int i=0; i<listeners; i++) {
            eventService.subscribe(new EventListener());
        }
        System.out.println("Memory used after subscription: "+(runtime.totalMemory()-runtime.freeMemory()));
        System.gc();
        System.out.println("Memory used before notification: "+(runtime.totalMemory()-runtime.freeMemory()));
        Collection<NotificationFuture> results = eventService.publish(new Object());

        for (NotificationFuture f: results) {
            try {
                f.get();
            } catch (ExecutionException ex) {
                throw ex.getCause();
            }
        }
    }

    public int getListeners() {
        return listeners;
    }

    public void setListeners(int listeners) {
        this.listeners = listeners;
    }

    public static class EventListener{

        @Subscribe
        public void onEvent(){
            Assert.fail("Event listener that should be collected be GC received event");
        }
    }
}
