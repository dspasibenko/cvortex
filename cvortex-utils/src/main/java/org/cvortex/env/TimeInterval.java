package org.cvortex.env;

import java.util.concurrent.TimeUnit;

import org.cvortex.util.TimeFormatter;

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
    
    @Override
    public String toString() {
        return new StringBuilder().append("{interval=").append(TimeFormatter.toHumanView(timeIntervalMillis()))
                .append(", unit=").append(unit).append("}").toString();
    }
    
}
