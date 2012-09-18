package com.cvortex.cc.atd;

import org.cvortex.env.TimeInterval;
import org.cvortex.events.EventChannel;

public final class OfferParams {

    private final TimeInterval timeout;
    
    private final EventChannel offerResultChannel;
    
    private final int priority;

    public OfferParams(TimeInterval timeout, EventChannel offerResultChannel, int priority) {
        super();
        this.timeout = timeout;
        this.offerResultChannel = offerResultChannel;
        this.priority = priority;
    }

    public TimeInterval getTimeout() {
        return timeout;
    }

    public EventChannel getOfferResultChannel() {
        return offerResultChannel;
    }

    public int getPriority() {
        return priority;
    }
 
    @Override
    public String toString() {
        return new StringBuilder().append("{timeout=").append(timeout).append(", priority=")
                .append(priority).append("}").toString();
    }
}
