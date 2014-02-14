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

import java.util.LinkedList;
import java.util.concurrent.ExecutionException;

@RunWith(JUnit4.class)
public class OrderingTest {

    private int eventAmount = 20000;
    private int parallelism = 10;
    private boolean showProgress = true;

    private int lastOrder = 0;

    @Test
    public void testOrdering() throws InterruptedException, ExecutionException {
        System.out.println("Event ordering test being performed with parallelism=" + parallelism +
                           " eventAmount=" + eventAmount+" showProgress="+showProgress);

        this.lastOrder = 0;
        AsyncEventService eventService = new AsyncEventService(parallelism);
        eventService.subscribe(this);

        LinkedList<NotificationFuture> results = new LinkedList<>();

        for (int i=0; i<eventAmount; i++){
            OrderedEvent event = new OrderedEvent(i);
            results.addAll(eventService.publishOrdered(event));
        }

        NotificationFuture future = results.pollFirst();
        while (future != null) {
            while (!future.isDone()){
                synchronized (this) {
                    this.wait();
                }
            }
            future.get();
            future = results.pollFirst();
        }
        System.out.println();
    }

    @Subscribe
    public OrderedEvent onEvent(OrderedEvent event) {
        Assert.assertTrue(
                "Event ordering test failed on event " + event.order + " (expected: " + lastOrder + ")",
                event.order == lastOrder);
        if (showProgress) {
            TestHelper.progress((float) lastOrder / eventAmount);
        }

        lastOrder++;
        synchronized (this){
            this.notifyAll();
        }
        return event;
    }

    public static class OrderedEvent {
        public int order;

        public OrderedEvent(int order) {
            this.order = order;
        }
    }

    public int getEventAmount() {
        return eventAmount;
    }

    public void setEventAmount(int eventAmount) {
        this.eventAmount = eventAmount;
    }

    public int getParallelism() {
        return parallelism;
    }

    public void setParallelism(int parallelism) {
        this.parallelism = parallelism;
    }

    public boolean isShowProgress() {
        return showProgress;
    }

    public void setShowProgress(boolean showProgress) {
        this.showProgress = showProgress;
    }


}
