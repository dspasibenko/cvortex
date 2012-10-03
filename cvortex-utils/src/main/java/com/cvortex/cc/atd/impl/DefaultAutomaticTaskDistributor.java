package com.cvortex.cc.atd.impl;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.cvortex.env.ExecutionEnvironment;
import org.cvortex.env.TimeInterval;
import org.cvortex.log.Logger;
import org.cvortex.log.LoggerFactory;

import com.cvortex.cc.atd.AutomaticTaskDistributor;
import com.cvortex.cc.atd.OfferParams;
import com.cvortex.cc.atd.Offerer;
import com.cvortex.cc.atd.Processor;
import com.cvortex.cc.atd.Task;
import com.cvortex.cc.atd.TaskControl;

/**
 * An implementation of {@link AutomaticTaskDistributor} interface
 * 
 * @author Dmitry Spasibenko 
 *
 */
class DefaultAutomaticTaskDistributor implements AutomaticTaskDistributor {

    private Logger logger = LoggerFactory.getLogger(DefaultAutomaticTaskDistributor.class);
    
    private Lock lock = new ReentrantLock();
    
    private Map<Processor, ProcessorHolder> processors = new HashMap<Processor, ProcessorHolder>();
    
    private final ExecutionEnvironment execEnvironment;
    
    private final Offerer offerer;
    
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
                logger.info("Cancelling task: ", tHolder);
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
                logger.info("Timeout for task: ", tHolder);
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
            logger.info("Offering task: ", tHolder, " to ", pHolder);
            boolean positive = offerer.offer(pHolder.getProcessor(), tHolder.getTask());
            lock.lock();
            try {
                onOfferDone(positive);
            } finally {
                lock.unlock();
            }
        }
        
        private void onOfferDone(boolean positive) {
            logger.info("Offer result is ", positive);
            tHolder.onOfferDone(pHolder, positive);
            if (!positive) {
                if (tHolder.canBePlacedToQueue()) {
                    putTaskHolderToQueues(tHolder);
                }
                if (pHolder.canBePlacedToQueue()) {
                    putProcessorHolderToQueues(pHolder);
                }
            } else {
                processors.remove(pHolder.getProcessor());
            }
        }
    }
    
    DefaultAutomaticTaskDistributor(ExecutionEnvironment execEnvironment, Offerer offerer, QueueProvider queueProvider) {
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
                processors.put(processor, pHolder);
                logger.info("Register new processor", pHolder);
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
                logger.info("Unregister processor", pHolder);
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
            TaskHolder tHolder = new TaskHolder(task, offerParams, this);
            logger.info("New task is offerred: ", tHolder);
            putTaskHolderToQueues(tHolder);
            return new TaskControlImpl(tHolder); 
        } finally {
            lock.unlock();
        }
    }
    
    void putProcessorHolderToQueues(ProcessorHolder pHolder) {
        logger.debug("Trying to put ", pHolder, " to queues");
        mainQueue.putP(pHolder);
    }

    void removeProcessorHolderFromQueues(ProcessorHolder pHolder) {
        logger.debug("Removing ", pHolder, " from queues");
        mainQueue.removeP(pHolder);
    }
    
    void putTaskHolderToQueues(TaskHolder tHolder) {
        logger.debug("Trying to put ", tHolder, " to queues");
        mainQueue.putT(tHolder);
    }

    void removeTaskHolderFromQueues(TaskHolder tHolder) {
        logger.debug("Removing ", tHolder, " from queues");
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
