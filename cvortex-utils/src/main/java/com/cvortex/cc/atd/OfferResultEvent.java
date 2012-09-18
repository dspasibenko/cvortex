package com.cvortex.cc.atd;

public final class OfferResultEvent {

    private final Task task;
    
    private final Processor processor;
    
    private final OfferResult result;
    
    public OfferResultEvent(Task task, Processor processor, OfferResult result) {
        super();
        this.task = task;
        this.processor = processor;
        this.result = result;
    }

    public Task getTask() {
        return task;
    }

    public Processor getProcessor() {
        return processor;
    }

    public OfferResult getResult() {
        return result;
    }

    @Override
    public String toString() {
        return new StringBuilder("{task=").append(task).append(", processor=").append(processor)
                .append(", result=").append(result).append("}").toString();
    }
    
}
