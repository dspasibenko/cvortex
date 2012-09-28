package org.cvortex.events;

/**
 * The interface allows to deliver events from publishers to subscribers
 * 
 * @author Dmitry Spasibenko 
 *
 */
public interface EventChannel {

    /**
     * Publishes an event into the channel. Depending on implementation 
     * the method can block invoker in case of the channel reaches the maximum 
     * event capacity or return negative result. 
     * 
     * @param e
     * @returns true if the event is successfully published, or false otherwise
     */
    boolean publish(Object e);
    
    void addSubscriber(Object subscriber);

    void removeSubscriber(Object subscriber);
    
}
