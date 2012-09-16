package com.cvortex.cc.atd.impl;

interface QueueProvider {

    QHolder<ProcessorHolder, TaskHolder> getNewQueue();
    
}
