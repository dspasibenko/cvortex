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
        return new SerialEventChannel(name, serialChannelCapacity, executor, new SubscribersRegistry(typeParser));
    }
    
}
