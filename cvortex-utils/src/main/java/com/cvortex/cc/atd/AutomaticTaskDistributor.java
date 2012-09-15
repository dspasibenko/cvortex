package com.cvortex.cc.atd;

public interface AutomaticTaskDistributor {

    void register(Processor processor);
    
    void unRegister(Processor processor);
    
    TaskControl offer(Task task, OfferParams offerParams);
    
}
