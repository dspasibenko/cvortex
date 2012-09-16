package com.cvortex.cc.atd;

/**
 * Interface for Automatic Task Distribution functionality
 * 
 * @author Dmitry Spasibenko 
 *
 */
public interface AutomaticTaskDistributor {

    /**
     * Registers a processor, which is ready to handle tasks
     * @param processor
     */
    void register(Processor processor);
    
    /**
     * Let the ATD know about the processor is not able to handle
     * tasks
     * @param processor
     */
    void unRegister(Processor processor);
    
    /**
     * Offer ATD to distribute the task between available processors (if
     * they are present), or handle it according provided offered params 
     * settings.
     * @param task
     * @param offerParams
     * @return
     */
    TaskControl offer(Task task, OfferParams offerParams);
    
}
