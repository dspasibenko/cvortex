package org.cvortex.events.details;

import java.util.concurrent.Executor;

import org.jrivets.event.EventChannel;
import org.jrivets.event.SerialEventChannel;

public class EventChannelFactory {

    private final Executor executor;
    
    private final int serialChannelCapacity;
    
    public EventChannelFactory(Executor executor, int serialChannelCapacity) {
        this.executor = executor;
        this.serialChannelCapacity = serialChannelCapacity;
    }
    
    public EventChannel createSerialEventChannel(String name) {
        return createSerialEventChannel(name, serialChannelCapacity);
    }
    
    public EventChannel createSerialEventChannel(String name, int capacity) {
        return new SerialEventChannel(name, capacity, executor);
    }
    
}
