package com.cvortex.cc.atd.impl;

import java.util.Comparator;

class TaskPriorityQueueStrategy implements Comparator<TaskHolder> {

    @Override
    public int compare(TaskHolder tHolder1, TaskHolder tHolder2) {
        int p1 = tHolder1.getOfferParams().getPriority();
        int p2 = tHolder2.getOfferParams().getPriority();
        
        return (p1 < p2 ? -1 : (p1 == p2 ? 0 : 1));
    }
}
