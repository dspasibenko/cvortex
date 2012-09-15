package com.cvortex.cc.atd.impl;

import org.cvortex.env.TimeSourceProvider;

import com.cvortex.cc.atd.Processor;

class ProcessorHolder implements QEntry<TaskHolder> {

    private final Processor processor;
    
    private boolean removed;
    
    private final long createTimeMillis = TimeSourceProvider.currentTimeMillis();
    
    ProcessorHolder(Processor processor) {
        this.processor = processor;
    }
    
    Processor getProcessor() {
        return processor;
    }
    
    @Override
    public boolean isAcceptableFor(TaskHolder tHolder) {
        return tHolder.isAcceptableFor(this);
    }

    @Override
    public void offer(TaskHolder tHolder) {
        tHolder.offer(this);
    }
 
    @Override
    public boolean canBePlacedToQueue() {
        return !removed;
    }

    long getCreateTimeMillis() {
        return createTimeMillis;
    }
    
    void remove() {
        removed = true;
    }
    
}