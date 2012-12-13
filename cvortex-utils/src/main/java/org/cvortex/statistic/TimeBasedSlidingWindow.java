package org.cvortex.statistic;

import java.util.Collection;
import java.util.Collections;

import org.cvortex.env.TimeSourceProvider;
import org.jrivets.collection.RingBuffer;
import org.jrivets.env.TimeSource;

public abstract class TimeBasedSlidingWindow<T> {
   
    private long size;

    private long bucketSize;

    protected TimeSource timeSource = TimeSourceProvider.getTimeSource();

    private final RingBuffer<Bucket<T>> buckets;

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
        int capacity = (int) ((size + bucketSize/2) / bucketSize);
        this.buckets = new RingBuffer<TimeBasedSlidingWindow.Bucket<T>>(capacity);
    }

    public Collection<IntervalledValue<T>> getValues() {
        return Collections.<IntervalledValue<T>>unmodifiableCollection(buckets);
    }

    protected abstract void onRemove(Bucket<T> bucket);
    
    protected abstract void onAdd(Bucket<T> bucket, T value);
    
    protected void wipe(long currentTime) {
        currentTime -= size;
        while (buckets.size() > 0 && buckets.first().time < currentTime) {
            onRemove(buckets.first());
            buckets.removeFirst();
        }
    }

    public void add(T value) {
        long now = timeSource.currentTimeMillis();
        long time = getNormilizedTime(now);
        Bucket<T> bucket = null;
        if (buckets.size() > 0 && buckets.last().time == time) {
            bucket = buckets.last();
        } else {
            bucket = new Bucket<T>(time);
            buckets.add(bucket);
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
