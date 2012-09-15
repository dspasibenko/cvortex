package com.cvortex.cc.atd.impl;

interface QEntry<T> {
    
    boolean canBePlacedToQueue();
    
    boolean isAcceptableFor(T anotherEntry);
    
    void offer(T entry);
    
}
