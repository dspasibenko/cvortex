package org.cvortex.events.details;

import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.cvortex.collection.RingBuffer;
import org.cvortex.events.EventChannel;

class SerialEventChannel implements EventChannel {

    private final Lock lock = new ReentrantLock();
    
    private Condition condition;
    
    private State state = State.WAITING;
    
    private final CopyOnWriteArraySet<Subscriber> subscribers = new CopyOnWriteArraySet<Subscriber>();
    
    private final RingBuffer<Object> events;
    
    private final Executor executor;
    
    private long waitingEventTimeoutMillis = 0L; 
    
    enum State {
        WAITING,
        PROCESSING, 
        PROCESSING_WAITING;
    }
    
    SerialEventChannel(int capacity, Executor executor) {
        this.events = new RingBuffer<Object>(capacity);
        this.executor = executor;
    }
    
    void setWaitingTimeout(long millis) {
        waitingEventTimeoutMillis = millis;
    }
    
    @Override
    public void publish(Object e) throws InterruptedException {
        lock.lock();
        try {
            onNewEvent(e);
            runExecutorIfRequired();
        } finally {
            lock.unlock();
        }
    }
    
    void addSubscriber(Subscriber subscriber) {
        subscribers.add(subscriber);
    }
    
    void removeSubscriber(Subscriber subscriber) {
        subscribers.remove(subscriber);
    }
    
    private void onNewEvent(Object e) throws InterruptedException {
        while (events.size() == events.capacity()) {
            getCondition().await();
        }
        events.add(e);
    }
    
    private Condition getCondition() {
        if (condition == null) {
            condition = lock.newCondition();
        }
        return condition;
    }
    
    private void runExecutorIfRequired() {
        if (State.WAITING.equals(state)) {
            executor.execute(new Runnable() {
                @Override
                public void run() {
                    notifyListeners();
                }
            });
            state = State.PROCESSING;
        } else if (State.PROCESSING_WAITING.equals(state)) {
            getCondition().signalAll();
        }
    }
    
    private void notifyListeners() {
        Object e = getEvent();
        while (e != null) {
            for (Subscriber subscriber: subscribers) {
                subscriber.notify(e);
            }
            e = getEvent();
        }
    }
    
    private Object getEvent() {
        lock.lock();
        try {
            if (!waitAnEventOrDoneInTimeout()) {
                return null;
            }
            boolean needNotifyPublishThread = events.size() == events.capacity();
            Object result = events.removeFirst();
            if (needNotifyPublishThread) {
                getCondition().signalAll();
            }
            return result;
        } finally {
            lock.unlock();
        }
    }
    
    private boolean waitAnEventOrDoneInTimeout() {
        if (!waitNewEvent()) {
            state = State.WAITING;
            return false;
        }
        return true;
    }
    
    private boolean waitNewEvent() {
        if (events.size() == 0 && waitingEventTimeoutMillis > 0L) {
            try {
                state = State.PROCESSING_WAITING;
                getCondition().await(waitingEventTimeoutMillis, TimeUnit.MILLISECONDS);
            } catch (InterruptedException ie) {
                return false;
            } finally {
                state = State.PROCESSING;
            }
        }
        return events.size() > 0;
    }
    
    @Override
    public String toString() {
        return new StringBuilder().append("{state=").append(state).append(", subscribers=").append(subscribers.size())
                .append(", events=").append(events.size()).append("}").toString();
    }
}
