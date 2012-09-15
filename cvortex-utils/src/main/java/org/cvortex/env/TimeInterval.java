package org.cvortex.env;

import java.util.concurrent.TimeUnit;

public final class TimeInterval {

    private final long timeInterval;
    
    private final TimeUnit unit;
    
    public TimeInterval(long timeInterval, TimeUnit unit) {
        this.timeInterval = timeInterval;
        this.unit = unit;
    }
    
    public TimeInterval(long timeInterval) {
        this(timeInterval, TimeUnit.MILLISECONDS);
    }
    
    public long timeIntervalMillis() {
        return unit.toMillis(timeInterval);
    }
    
    public long timeInterval() {
        return timeInterval;
    }
    
    public TimeUnit timeUnit() {
        return unit;
    }
    
}
