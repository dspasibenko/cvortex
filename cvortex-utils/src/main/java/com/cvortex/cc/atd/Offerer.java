package com.cvortex.cc.atd;

public interface Offerer<P extends Processor, T extends Task> {

    boolean offer(P p, T t);
    
}
