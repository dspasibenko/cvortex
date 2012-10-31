package org.cvortex.statistic;

public final class SlidingIntWindow extends TimeBasedSlidingWindow<Integer> {

    private long sum;
    
    public SlidingIntWindow(long timeWindowMs, long bucketSize) {
        super(timeWindowMs, bucketSize);
    }

    protected void onRemove(Bucket<Integer> bucket) {
        sum -= bucket.getValue();
    }
    
    protected void onAdd(Bucket<Integer> bucket, Integer value) {
        if (bucket.getValue() == null) {
            bucket.setValue(0);
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
