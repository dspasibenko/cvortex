package org.cvortex.env;

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
