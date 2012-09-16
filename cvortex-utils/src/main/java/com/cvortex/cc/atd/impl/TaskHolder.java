package com.cvortex.cc.atd.impl;

import java.util.HashSet;
import java.util.Set;

import org.cvortex.log.Logger;
import org.cvortex.log.LoggerFactory;


import com.cvortex.cc.atd.OfferParams;
import com.cvortex.cc.atd.Task;

class TaskHolder implements QEntry<ProcessorHolder> {
    
    private final Logger logger = LoggerFactory.getLogger(TaskHolder.class);
    
    private final Task task;
    
    private final AutomaticTaskDistributorImpl atd;
    
    private final OfferParams offerParams;
    
    private Set<ProcessorHolder> blackList;
    
    private State state = State.QUEUE;
    
    enum State {
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
        return State.QUEUE.equals(state) && (blackList == null || !blackList.contains(pHolder));
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
            addToBlackList(pHolder);
        } else if (State.OFFERED_CANCEL.equals(state)) {
            notifyAboutCancellation();
            state = State.REMOVE;
        } else if (State.OFFERED_TIMEOUT.equals(state)) {
            notifyAboutTimeout();
            state = State.REMOVE;
        }
        logger.info("onOfferDone() for ", this, " pHolder: ", pHolder);
    }

    void cancel() {
        if (State.QUEUE.equals(state)) {
            notifyAboutCancellation();
            state = State.REMOVE;
        } else if (State.OFFERED.equals(state)) {
            state = State.OFFERED_CANCEL;
        }
        logger.info("On cancelled: ", this);
    }
    
    void onTimeout() {
        if (State.QUEUE.equals(state)) {
            notifyAboutTimeout();
            state = State.REMOVE;
        } else if (State.OFFERED.equals(state)) {
            state = State.OFFERED_TIMEOUT;
        }
        logger.info("On timeout: ", this);
    }
    
    private void addToBlackList(ProcessorHolder pHolder) {
        if (blackList == null) {
            blackList = new HashSet<ProcessorHolder>();
        }
        blackList.add(pHolder);
        logger.info("Add ", pHolder, " to black list for ", this);
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
                    logger.debug("Notify listener about cancellation");
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
                    logger.debug("Notify listener about timeout");
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
                    logger.debug("Notify listener about assignment.");
                    offerParams.getResultListener().onAssignedTo(pHolder.getProcessor());
                }
            });
        }
    }
    
    @Override
    public String toString() {
        return new StringBuilder().append("{task=").append(task).append(", offerParams=")
                .append(offerParams).append(", blackListSize=").append(blackList == null ? 0 : blackList.size())
                .append("}").toString();
    }
}