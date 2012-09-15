package com.cvortex.cc.atd.impl;

import java.util.HashSet;
import java.util.Set;

import com.cvortex.cc.atd.OfferParams;
import com.cvortex.cc.atd.Task;

class TaskHolder implements QEntry<ProcessorHolder> {
    
    private final Task task;
    
    private final AutomaticTaskDistributorImpl atd;
    
    private final OfferParams offerParams;
    
    private final Set<ProcessorHolder> blackList = new HashSet<ProcessorHolder>();
    
    private State state = State.QUEUE;
    
    private enum State {
        QUEUE, 
        OFFERED, 
        OFFERED_CANCEL, 
        OFFERED_TIMEOUT, 
        REMOVE;
    }
    
    TaskHolder(Task task, OfferParams offerParams, AutomaticTaskDistributorImpl atd) {
        this.task = task;
        this.atd = atd;
        this.offerParams = offerParams;
    }
    
    Task getTask() {
        return task;
    }
    
    OfferParams getOfferParams() {
        return offerParams;
    }

    @Override
    public boolean isAcceptableFor(ProcessorHolder pHolder) {
        return State.QUEUE.equals(state) && !blackList.contains(pHolder);
    }

    @Override
    public void offer(ProcessorHolder entry) {
        assertState(State.QUEUE);
        state = State.OFFERED;
        // task first
        atd.offer(this, entry);
    }
    
    @Override
    public boolean canBePlacedToQueue() {
        return State.QUEUE.equals(state);
    }
    
    void onOfferDone(ProcessorHolder pHolder, boolean positive) {
        if (positive) {
            notifyAboutGoodOffer(pHolder);
            state = State.REMOVE;
            return;
        }
        if (State.OFFERED.equals(state)) {
            state = State.QUEUE;
            blackList.add(pHolder);
        } else if (State.OFFERED_CANCEL.equals(state)) {
            notifyAboutCancellation();
            state = State.REMOVE;
        } else if (State.OFFERED_TIMEOUT.equals(state)) {
            notifyAboutTimeout();
            state = State.REMOVE;
        }
    }

    void cancel() {
        if (State.QUEUE.equals(state)) {
            notifyAboutCancellation();
            state = State.REMOVE;
        } else if (State.OFFERED.equals(state)) {
            state = State.OFFERED_CANCEL;
        }
    }
    
    void onTimeout() {
        if (State.QUEUE.equals(state)) {
            notifyAboutTimeout();
            state = State.REMOVE;
        } else if (State.OFFERED.equals(state)) {
            state = State.OFFERED_TIMEOUT;
        }
    }
    
    private void assertState(State expectedState) {
        if (!expectedState.equals(state)) {
            throw new IllegalStateException(""); //TODO:
        }
    }
    
    private void notifyAboutCancellation() {
        if (offerParams.getResultListener() != null) {
            atd.getExecEnvironment().execute(new Runnable() {
                @Override
                public void run() {
                    offerParams.getResultListener().onCancelled();
                }
            });
        }
    }
    
    private void notifyAboutTimeout() {
        if (offerParams.getResultListener() != null) {
            atd.getExecEnvironment().execute(new Runnable() {
                @Override
                public void run() {
                    offerParams.getResultListener().onTimeout();
                }
            });
        }
    }

    private void notifyAboutGoodOffer(final ProcessorHolder pHolder) {
        if (offerParams.getResultListener() != null) {
            atd.getExecEnvironment().execute(new Runnable() {
                @Override
                public void run() {
                    offerParams.getResultListener().onAssignedTo(pHolder.getProcessor());
                }
            });
        }
    }
}