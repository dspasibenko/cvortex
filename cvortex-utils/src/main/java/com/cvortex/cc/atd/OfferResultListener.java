package com.cvortex.cc.atd;

public interface OfferResultListener {

    void onAssignedTo(Processor processor);
    
    void onCancelled();
    
    void onTimeout();
}
