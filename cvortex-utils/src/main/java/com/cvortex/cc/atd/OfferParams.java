package com.cvortex.cc.atd;

import org.cvortex.env.TimeInterval;

public final class OfferParams {

    private final TimeInterval timeout;
    
    private final OfferResultListener resultListener;
    
    private final int priority;

    public OfferParams(TimeInterval timeout, OfferResultListener resultListener, int priority) {
        super();
        this.timeout = timeout;
        this.resultListener = resultListener;
        this.priority = priority;
    }

    public TimeInterval getTimeout() {
        return timeout;
    }

    public OfferResultListener getResultListener() {
        return resultListener;
    }

    public int getPriority() {
        return priority;
    }
 
}
