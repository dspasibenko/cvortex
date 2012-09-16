package org.cvortex.env;

import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public final class ExecutionEnvironmentReal implements ExecutionEnvironment {
    
    private final ScheduledThreadPoolExecutor executor;
    
    public ExecutionEnvironmentReal(ScheduledThreadPoolExecutor executor) {
        this.executor = executor;
    }

    @Override
    public ScheduledFuture<?> schedule(Runnable command, long delay, TimeUnit unit) {
        return executor.schedule(command, delay, unit);
    }

    @Override
    public void execute(Runnable command) {
        executor.execute(command);
    }

    @Override
    public long currentTimeMillis() {
        return System.currentTimeMillis();
    }

    public ScheduledThreadPoolExecutor getExecutor() {
        return executor;
    }

}
