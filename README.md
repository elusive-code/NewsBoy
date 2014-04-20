# NewsBoy

NewsBoy is a library that provides publish-subscribe style communication between Java components.

NewsBoy mimics EventBus from Google Guava, but has several differences:

- it uses weak references
- it is asynchronous
- it is concurrent

## Features

1. Listeners are stored using WeakReferences to prevent memory leaks when they are not unsubscribed.
2. Uses fork-join framework for concurrent event delivery.
3. Publish methods returns collection of Futures that represent event notifications.
4. Supports ordered publishing: guaranteed to notify of the events in order they were published.
5. Provides EventSource and EventService interfaces for better integration with IOC containers and alternative implementations.

## Dependencies
1. Java SE 7
2. Apache Commons Lang 3
3. JUnit4 for tests

## Maven
```xml

    <dependency>
        <groupId>com.elusive-code.newsboy</groupId>
        <artifactId>NewsBoy</artifactId>
        <version>0.1</version>
    </dependency>

```

## Usage

1. Create instance of AsyncEventService
2. Create listener object: it should have public method(s) with zero or one argument marked with @Subscribe
3. Subscribe listener object using AsyncEventService.subscribe() method
4. Publish event using AsyncEventService.publish() or AsyncEventService.publishOrdered()

## Example

```Java

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

```

## License

Apache Software License 2.0.

Copyright (C) 2014, Vladislav Dolgikh.

See LICENSE for details.
