package org.cvortex.events.details;

import java.util.concurrent.Executor;

import org.cvortex.events.EventChannel;

public class EventChannelFactory {

    private final Executor executor;
    
    private final int serialChannelCapacity;
    
    private final SubscriberTypeParser typeParser = new SubscriberTypeParser();
    
    public EventChannelFactory(Executor executor, int serialChannelCapacity) {
        this.executor = executor;
        this.serialChannelCapacity = serialChannelCapacity;
    }
    
    public EventChannel createSerialEventChannel(String name) {
        return createSerialEventChannel(name, serialChannelCapacity);
    }
    
    public EventChannel createSerialEventChannel(String name, int capacity) {
        return new SerialEventChannel(name, capacity, executor, new SubscribersRegistry(typeParser));
    }
    
}
