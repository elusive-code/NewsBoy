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

package com.elusive_code.newsboy.examples;

import com.elusive_code.newsboy.AsyncEventService;
import com.elusive_code.newsboy.EventService;
import com.elusive_code.newsboy.NotificationFuture;
import com.elusive_code.newsboy.Subscribe;

import java.util.Collection;

/**
 * Created by vlad on 14.02.14.
 */
public class SimpleExample {

    public static class Event {
        private String eventInfo;

        public Event(String eventInfo) {
            this.eventInfo = eventInfo;
        }

        public String getEventInfo() {
            return eventInfo;
        }
    }

    public static class Listener {

        @Subscribe
        public void printEvent(Event event){
            System.out.println("Something happens: " + event.getEventInfo());
        }
    }

    public static void main(String[] args) {

        // initial setup
        // create event service and listener
        EventService eventService = new AsyncEventService();
        Listener listener = new Listener();
        eventService.subscribe(listener);

        //event publishing
        //results will contain java.util.concurrent.Future that represent notifications
        Collection<NotificationFuture> results = eventService.publish(new Event("Some event"));

        //this will make us wait until all listeners processed event
        for (NotificationFuture future: results) {
            try{
                future.get();
            }catch (Exception ex){
                ex.printStackTrace();
            }
        }

    }

}
