package com.cvortex.cc.atd.impl;

import java.util.concurrent.ScheduledThreadPoolExecutor;

import org.cvortex.env.ExecutionEnvironment;
import org.cvortex.env.ExecutionEnvironmentReal;
import org.cvortex.env.TimeInterval;
import org.cvortex.util.SilentSleep;
import org.cvortex.util.testing.WatchDog;
import org.junit.Assert;
import org.junit.Test;

import com.cvortex.cc.atd.OfferParams;
import com.cvortex.cc.atd.OfferResultListener;
import com.cvortex.cc.atd.Offerer;
import com.cvortex.cc.atd.Processor;
import com.cvortex.cc.atd.Task;
import com.cvortex.cc.atd.TaskControl;

public class AutomaticTaskDistributerImplTest extends Assert implements Offerer<Processor, Task>, QueueProvider {

    private final ScheduledThreadPoolExecutor executor = new ScheduledThreadPoolExecutor(1);
    
    private final ExecutionEnvironment execEnvironment = new ExecutionEnvironmentReal(executor);
    
    private final AutomaticTaskDistributorImpl atd = new AutomaticTaskDistributorImpl(execEnvironment, this, this);
    
    private boolean offerResult;
    
    private long offerDelay = 0L;
    
    private ResultListener listener = new ResultListener();
    
    private class ResultListener implements OfferResultListener {
        
        private Processor assignedTo;
        private boolean cancelled;
        private boolean timeout;

        @Override
        public void onAssignedTo(Processor processor) {
            assignedTo = processor;
        }

        @Override
        public void onCancelled() {
            cancelled = true;
        }

        @Override
        public void onTimeout() {
            timeout = true;
        }
        
    }

    @Override
    public QHolder<ProcessorHolder, TaskHolder> getNewQueue() {
        return new QHolder<ProcessorHolder, TaskHolder>(new WaitingTimeProcessorQueueStrategy(), new TaskPriorityQueueStrategy());
    }

    @Override
    public boolean offer(Processor p, Task t) {
        if (offerDelay > 0L) {
            SilentSleep.sleep(offerDelay);
        }
        return offerResult;
    }
    
    @Test
    public void timeout0millis() {
        atd.offer(new Task() {}, getOfferParams(0L));
        waitTimeout(500L);
    }
    
    @Test
    public void timeout100millis() {
        atd.offer(new Task() {}, getOfferParams(100L));
        waitTimeout(500L);
    }
    
    @Test
    public void timeout600millis() {
        atd.offer(new Task() {}, getOfferParams(600L));
        try {
            waitTimeout(300L);
        } catch (AssertionError ae) {
            return;
        }
        fail("Should not wait execution timeout");
    }
    
    @Test
    public void successOffer() {
        Processor p = new Processor() {};
        atd.register(p);
        offerResult = true;
        atd.offer(new Task() {}, getOfferParams(0L));
        waitOffered(p);
    }
    
    @Test
    public void unsuccessOffer() {
        Processor p = new Processor() {};
        atd.register(p);
        atd.offer(new Task() {}, getOfferParams(0L));
        waitTimeout(500L);
    }
    
    @Test
    public void successOfferTP() {
        atd.offer(new Task() {}, getOfferParams(500L));
        offerResult = true;
        Processor p = new Processor() {};
        atd.register(p);
        waitOffered(p);
    }
    
    @Test
    public void unsuccessOfferTP() {
        atd.offer(new Task() {}, getOfferParams(50L));
        Processor p = new Processor() {};
        atd.register(p);
        waitTimeout(500L);
    }
    
    @Test
    public void cancel() {
        TaskControl tc = atd.offer(new Task() {}, getOfferParams(50000L));
        tc.cancel();
        waitCancelled();
    }
    
    @Test
    public void unsuccessOfferTPCancelled() {
        TaskControl tc = atd.offer(new Task() {}, getOfferParams(50000L));
        offerDelay = 200L;
        Processor p = new Processor() {};
        atd.register(p);
        tc.cancel();
        waitCancelled();
    }
    
    @Test
    public void unsuccessOfferTPTimeout() {
        atd.offer(new Task() {}, getOfferParams(100L));
        offerDelay = 200L;
        Processor p = new Processor() {};
        atd.register(p);
        waitTimeout(500L);
    }
    
    @Test
    public void processorGoOutOfQueue() {
        Processor p = new Processor() {};
        atd.register(p);
        offerResult = true;
        atd.offer(new Task() {}, getOfferParams(0L));
        waitOffered(p);
        
        listener.assignedTo = null;
        atd.offer(new Task() {}, getOfferParams(0L));
        waitTimeout(100L);
    }
    
    @Test
    public void taskOrder() {
        Task t1 = new Task() {};
        TaskControl tc = atd.offer(t1, getOfferParams(5000L, 10));
        Task t2 = new Task() {};
        atd.offer(t2, getOfferParams(5000L, 5));
        offerResult = true;
        Processor p = new Processor() {};
        atd.register(p);
        
        waitOffered(p);
        listener.assignedTo = null;
        tc.cancel();
        waitCancelled();
    }
    
    @Test
    public void processorsOrder() {
        Processor p1 = new Processor() {};
        atd.register(p1);
        SilentSleep.sleep(1L);
        Processor p2 = new Processor() {};
        atd.register(p2);
        
        atd.unRegister(p1);
        SilentSleep.sleep(1L);
        atd.register(p1);
        
        offerResult = true;
        atd.offer(new Task() {}, getOfferParams(500L));
        waitOffered(p2);
    }
    
    @Test
    public void processorStillInQueue() {
        Processor p = new Processor() {};
        atd.register(p);
        atd.offer(new Task() {}, getOfferParams(0L));
        waitTimeout(500L);
        
        listener.timeout = false;
        offerResult = true;
        atd.offer(new Task() {}, getOfferParams(0L));
        waitOffered(p);
    }
    
    private void waitOffered(Processor p) {
        WatchDog wd = new WatchDog(10000L);
        try {
            while (listener.assignedTo != p) {
                assertNull(SilentSleep.sleep(10L));
            }
        } finally {
            wd.done();
        }
        assertFalse(listener.timeout);
        assertFalse(listener.cancelled);
    }
    
    private void waitTimeout(long maxWaitTime) {
        WatchDog wd = new WatchDog(maxWaitTime);
        try {
            while (!listener.timeout) {
                assertNull(SilentSleep.sleep(10L));
            }
        } finally {
            wd.done();
        }
        assertNull(listener.assignedTo);
        assertFalse(listener.cancelled);
    }
    
    private void waitCancelled() {
        WatchDog wd = new WatchDog(10000L);
        try {
            while (!listener.cancelled) {
                assertNull(SilentSleep.sleep(10L));
            }
        } finally {
            wd.done();
        }
        assertNull(listener.assignedTo);
        assertFalse(listener.timeout);
    }
    
    private OfferParams getOfferParams(long timeout) {
        return new OfferParams(new TimeInterval(timeout), listener, 0);
    }
    
    private OfferParams getOfferParams(long timeout, int priority) {
        return new OfferParams(new TimeInterval(timeout), listener, priority);
    }
}
