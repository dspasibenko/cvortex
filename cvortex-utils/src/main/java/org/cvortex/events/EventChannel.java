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
     * event capacity. This case {@link InterruptedException} can be thrown 
     * in case of the thread is interrupted while it is blocked.
     * 
     * @param e
     * @throws InterruptedException
     */
    void publish(Object e) throws InterruptedException;
    
}
