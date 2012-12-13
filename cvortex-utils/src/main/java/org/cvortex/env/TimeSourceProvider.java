package org.cvortex.env;

import org.jrivets.env.TimeSource;

public final class TimeSourceProvider {

    private static TimeSource localHostTimeSource = new TimeSource() {

        @Override
        public long currentTimeMillis() {
            return System.currentTimeMillis();
        }
    };
    
    public static TimeSource getTimeSource() {
        return localHostTimeSource;
    }
    
}
