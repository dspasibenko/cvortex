package org.cvortex.statistic;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;

import org.cvortex.env.TimeSource;
import org.cvortex.env.TimeSourceProvider;

public abstract class TimeBasedSlidingWindow<T> {
   
    private long size;

    private long bucketSize;

    protected TimeSource timeSource = TimeSourceProvider.getTimeSource();

    private LinkedList<Bucket<T>> list = new LinkedList<Bucket<T>>();

    protected static class Bucket<T> implements IntervalledValue<T> {

        private T value;

        private long time;

        Bucket(long time) {
            this.time = time;
        }
        
        @Override
        public long getTime() {
            return time;
        }
        
        @Override
        public T getValue() {
            return value;
        }
        
        void setValue(T value) {
            this.value = value;
        }
    }

    protected TimeBasedSlidingWindow(long timeWindowMs, long bucketSize) {
        this.size = timeWindowMs;
        this.bucketSize = bucketSize;
    }

    public void setSize(long newSize) {
        if (newSize < bucketSize) {
            throw new IllegalArgumentException("Window size cannot be less than bucket size");
        }
        this.size = newSize;
        wipe(timeSource.currentTimeMillis());
    }
    
    public Collection<IntervalledValue<T>> getValues() {
        return Collections.<IntervalledValue<T>>unmodifiableCollection(list);
    }

    protected abstract void onRemove(Bucket<T> bucket);
    
    protected abstract void onAdd(Bucket<T> bucket, T value);
    
    protected void wipe(long currentTime) {
        currentTime -= size;
        while (list.size() > 0 && list.getFirst().time < currentTime) {
            onRemove(list.getFirst());
            list.removeFirst();
        }
    }

    public void add(T value) {
        long now = timeSource.currentTimeMillis();
        long time = getNormilizedTime(now);
        Bucket<T> bucket = null;
        if (list.size() > 0 && list.getLast().time == time) {
            bucket = list.getLast();
        } else {
            bucket = new Bucket<T>(time);
            list.addLast(bucket);
        }
        onAdd(bucket, value);
        wipe(now);
    }
    
    protected long getNormilizedTime(long currentTime) {
        long time = currentTime / bucketSize;
        return time * bucketSize;
    }
    
    long getSize() {
        return size;
    }

    long getBucketSize() {
        return bucketSize;
    }
}
