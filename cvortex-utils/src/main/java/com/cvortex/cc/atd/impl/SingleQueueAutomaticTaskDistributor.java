package com.cvortex.cc.atd.impl;

import org.cvortex.env.ExecutionEnvironment;

import com.cvortex.cc.atd.Offerer;

public final class SingleQueueAutomaticTaskDistributor extends DefaultAutomaticTaskDistributor {

    private static class QProvider implements QueueProvider {
        @Override
        public QHolder<ProcessorHolder, TaskHolder> getNewQueue() {
            return new QHolder<ProcessorHolder, TaskHolder>(new WaitingTimeProcessorQueueStrategy(), new TaskPriorityQueueStrategy());
        }
    }
     
    public SingleQueueAutomaticTaskDistributor(ExecutionEnvironment execEnvironment, Offerer offerer) {
        super(execEnvironment, offerer, new QProvider());
    }

}
