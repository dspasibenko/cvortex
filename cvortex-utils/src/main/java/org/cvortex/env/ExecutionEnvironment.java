package org.cvortex.env;

import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public interface ExecutionEnvironment {

    ScheduledFuture<?> schedule(Runnable command, long delay, TimeUnit unit);
    
    void execute(Runnable command);
    
    long currentTimeMillis();
    
}
