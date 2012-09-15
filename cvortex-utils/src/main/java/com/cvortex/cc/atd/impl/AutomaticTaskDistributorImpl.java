package com.cvortex.cc.atd.impl;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.cvortex.env.ExecutionEnvironment;
import org.cvortex.env.TimeInterval;

import com.cvortex.cc.atd.AutomaticTaskDistributor;
import com.cvortex.cc.atd.OfferParams;
import com.cvortex.cc.atd.Offerer;
import com.cvortex.cc.atd.Processor;
import com.cvortex.cc.atd.Task;
import com.cvortex.cc.atd.TaskControl;

final class AutomaticTaskDistributorImpl implements AutomaticTaskDistributor {

    private Lock lock = new ReentrantLock();
    
    private Map<Processor, ProcessorHolder> processors = new HashMap<Processor, ProcessorHolder>();
    
    private final ExecutionEnvironment execEnvironment;
    
    private final Offerer<Processor, Task> offerer;
    
    private final QHolder<ProcessorHolder, TaskHolder> mainQueue;
    
    private class TaskControlImpl implements Runnable, TaskControl {
        
        private final TaskHolder tHolder;
        
        private final ScheduledFuture<?> future;

        TaskControlImpl(TaskHolder tHolder) {
            this.tHolder = tHolder;
            this.future = scheduleItself();
        }
        
        @Override
        public void cancel() {
            lock.lock();
            try {
                future.cancel(false);
                tHolder.cancel();
            } finally {
                lock.unlock();
            }
        }

        @Override
        public void run() {
            lock.lock();
            try {
                tHolder.onTimeout();
            } finally {
                lock.unlock();
            }
        }
        
        private ScheduledFuture<?> scheduleItself() {
            TimeInterval to = tHolder.getOfferParams().getTimeout();
            return execEnvironment.schedule(this, to.timeInterval(), to.timeUnit());
        }
        
    }
    
    private class TransferTask implements Runnable {
        
        private final ProcessorHolder pHolder;
        
        private final TaskHolder tHolder;

        TransferTask(ProcessorHolder pHolder, TaskHolder tHolder) {
            super();
            this.pHolder = pHolder;
            this.tHolder = tHolder;
        }

        @Override
        public void run() {
            boolean positive = offerer.offer(pHolder.getProcessor(), tHolder.getTask());
            lock.lock();
            try {
                onOfferDone(positive);
            } finally {
                lock.unlock();
            }
        }
        
        private void onOfferDone(boolean positive) {
            tHolder.onOfferDone(pHolder, positive);
            if (!positive) {
                if (tHolder.canBePlacedToQueue()) {
                    putTaskHolderToQueues(tHolder);
                }
                if (pHolder.canBePlacedToQueue()) {
                    putProcessorHolderToQueues(pHolder);
                }
            }
        }
    }
    
    AutomaticTaskDistributorImpl(ExecutionEnvironment execEnvironment, Offerer<Processor, Task> offerer, QueueProvider<Processor, Task> queueProvider) {
        this.execEnvironment = execEnvironment;
        this.offerer = offerer;
        this.mainQueue = queueProvider.getNewQueue();
    }
    
    @Override
    public void register(Processor processor) {
        lock.lock();
        try {
            if (!processors.containsKey(processor)) {
                ProcessorHolder pHolder = new ProcessorHolder(processor);
                putProcessorHolderToQueues(pHolder);
            }
        } finally {
            lock.unlock();
        }
    }

    @Override
    public void unRegister(Processor processor) {
        lock.lock();
        try {
            ProcessorHolder pHolder = processors.get(processor);
            if (pHolder != null) {
                pHolder.remove();
                removeProcessorHolderFromQueues(pHolder);
            }
        } finally {
            lock.unlock();
        }
    }

    @Override
    public TaskControl offer(Task task, OfferParams offerParams) {
        lock.lock();
        try {
            TaskHolder tHolder = new TaskHolder(task, offerParams, this); //TODO: need to have offer params...
            putTaskHolderToQueues(tHolder);
            return new TaskControlImpl(tHolder); 
        } finally {
            lock.unlock();
        }
    }
    
    void putProcessorHolderToQueues(ProcessorHolder pHolder) {
        mainQueue.putP(pHolder);
    }

    void removeProcessorHolderFromQueues(ProcessorHolder pHolder) {
        mainQueue.removeP(pHolder);
    }
    
    void putTaskHolderToQueues(TaskHolder tHolder) {
        mainQueue.putT(tHolder);
    }

    void removeTaskHolderFromQueues(TaskHolder tHolder) {
        mainQueue.removeT(tHolder);
    }
    
    ExecutionEnvironment getExecEnvironment() {
        return execEnvironment;
    }
    
    void offer(TaskHolder tHolder, ProcessorHolder pHolder) {
        removeTaskHolderFromQueues(tHolder);
        removeProcessorHolderFromQueues(pHolder);
        execEnvironment.execute(new TransferTask(pHolder, tHolder));
    }
}    
