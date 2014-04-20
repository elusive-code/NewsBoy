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

import com.elusive_code.newsboy.*;
import org.junit.Assert;
import org.junit.Test;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by vlad on 20.04.14.
 */
public class EventSourceTest {

    public static AsyncEventService service = new AsyncEventService();

    @Test
    public void testSource() throws Exception{
        service.subscribe(this);
        List<NotificationFuture> futures = service.publish(new EventSource() {
            @Override
            public void subscribe(Object listener) {
            }

            @Override
            public void unsubscribe(Object listener) {
            }
        });

        Assert.assertEquals(3,futures.size());
        for (NotificationFuture f: futures) {
            f.get();
        }
    }

    @Test
    public void testCorruptedListners(){
        try {
            service.subscribe(new CorruptedListener1());
            Assert.fail("Successfully subscribed corrupted listener, IllegalArgumentException expected");
        }catch (IllegalArgumentException ex){

        }

        try {
            service.subscribe(new CorruptedListener2());
            Assert.fail("Successfully subscribed corrupted listener, IllegalArgumentException expected");
        }catch (IllegalArgumentException ex){

        }
    }

    @Test
    public void testError(){
        Logger.getLogger(EventNotifierTask.class.getName()).setLevel(Level.SEVERE);
        AsyncEventService service = new AsyncEventService();
        service.subscribe(new ErrorListener());
        service.setSaveEventStackTrace(true);

        List<NotificationFuture> notifications = service.publish(new Object());
        for (NotificationFuture f: notifications) {
            try{
                f.get();
                Assert.fail("Throwable expected");
            }catch (Throwable t) {

            }
        }
    }

    @Subscribe
    public void onEvent(Object event, EventSource source){
        Assert.assertEquals(service,source);
    }

    @Subscribe(eventSourceParameter = 0)
    public void onEvent(EventSource source){
        Assert.assertEquals(service,source);
    }

    @Subscribe(eventSourceParameter = 1)
    public void onEvent(EventSource event, EventSource source){
        Assert.assertEquals(service,source);
        Assert.assertNotEquals(service,event);
    }


    public static class CorruptedListener1 {

        @Subscribe
        public void onEvent(EventSource source) {
            Assert.assertEquals(this, source);
        }
    }

    public static class CorruptedListener2 {

        @Subscribe
        public void onEvent(EventSource event, EventSource source) {
            Assert.assertEquals(this, source);
        }
    }

    public static class ErrorListener {

        @Subscribe
        public void onEvent(Object event) throws Throwable {
            throw new Throwable("some error");
        }
    }

}
