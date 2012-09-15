package com.cvortex.cc.atd.impl;

import java.util.Comparator;

final class WaitingTimeProcessorQueueStrategy implements Comparator<ProcessorHolder> {

    @Override
    public int compare(ProcessorHolder pHolder1, ProcessorHolder pHolder2) {
        long p1 = pHolder1.getCreateTimeMillis();
        long p2 = pHolder2.getCreateTimeMillis();
        
        return (p1 < p2 ? -1 : (p1 == p2 ? 0 : 1));
    }

}
