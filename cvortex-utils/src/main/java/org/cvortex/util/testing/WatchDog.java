package org.cvortex.util.testing;

/**
 * WatchDog for timing threads
 * 
 * <p> This simple class calls <code>interrupt()</code> for monitoredThread
 * in case of the watch dog is not cancelled in the specified timeout.
 * 
 * @author Dmitry Spasibenko 
 *
 */
public final class WatchDog implements Runnable {
    
    private final Thread monitoredThread;
    
    private final long timeoutMs;
    
    private volatile boolean done = false;
    
    private final Object syncObject = new Object();

    public WatchDog(long timeoutMs, Thread monitoredThread) {
        this.monitoredThread = monitoredThread;
        this.timeoutMs = timeoutMs;
        new Thread(this).start();
    }
    
    public WatchDog(long timeoutMs) {
        this(timeoutMs, Thread.currentThread());
    }

    @Override
    public void run() {
        synchronized (syncObject) {
            long killTime = System.currentTimeMillis() + timeoutMs;
            while (!done && monitoredThread.isAlive() && System.currentTimeMillis() < killTime) {
                try {
                    syncObject.wait(killTime - System.currentTimeMillis());
                } catch (InterruptedException e) {
                    return;
                }
            }
            if (!monitoredThread.isAlive() || done) {
                return;
            }
            monitoredThread.interrupt();
        }
    }
    
    public void done() {
        synchronized (syncObject) {
            done = true;
            syncObject.notify();
        }
    }
    
}
