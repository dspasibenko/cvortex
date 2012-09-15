package com.cvortex.cc.atd.impl;

interface QueueProvider<P, T> {

    QHolder<ProcessorHolder, TaskHolder> getNewQueue();
    
}
