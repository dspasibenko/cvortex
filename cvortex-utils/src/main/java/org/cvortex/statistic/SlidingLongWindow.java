package org.cvortex.statistic;

public final class SlidingLongWindow extends TimeBasedSlidingWindow<Long> {

    private long sum;
    
    public SlidingLongWindow(long timeWindowMs, long bucketSize) {
        super(timeWindowMs, bucketSize);
    }

    protected void onRemove(Bucket<Long> bucket) {
        sum -= bucket.getValue();
    }
    
    protected void onAdd(Bucket<Long> bucket, Long value) {
        if (bucket.getValue() == null) {
            bucket.setValue(0L);
        }
        bucket.setValue(bucket.getValue() + value);
        sum += value;
    }
    
    public long getSum() {
        wipe(timeSource.currentTimeMillis());
        return sum;
    }
    
    @Override
    public String toString() {
        return "{size=" + getSize() + ", bucketSize=" + getBucketSize() + ", totalSum=" + sum + "}";
    }
}
