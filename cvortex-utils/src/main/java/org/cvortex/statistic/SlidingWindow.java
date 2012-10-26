package org.cvortex.statistic;

import java.util.LinkedList;

import org.cvortex.env.TimeSource;
import org.cvortex.env.TimeSourceProvider;

public final class SlidingWindow {

    private long size;

    private long bucketSize;

    private long sum;

    private TimeSource timeSource = TimeSourceProvider.getTimeSource();

    private LinkedList<Bucket> list = new LinkedList<Bucket>();

    private static class Bucket {

        private long value;

        private long time;

        Bucket(long time, long value) {
            this.time = time;
            this.value = value;
        }
    }

    public SlidingWindow(long timeWindowMs, long bucketSize) {
        this.size = timeWindowMs;
        this.bucketSize = bucketSize;
    }

    public void setSize(long newSize) {
        if (newSize < bucketSize) {
            throw new IllegalArgumentException("Window size cannot be less than bucket size");
        }
        this.size = newSize;
        wipe(timeSource.currentTimeMillis() - size);
    }

    private void wipe(long minAllowed) {
        while (list.size() > 0 && list.getLast().time < minAllowed) {
            sum -= list.getLast().value;
            list.removeLast();
        }
    }

    public void add(long value) {
        long now = timeSource.currentTimeMillis();
        long time = now / bucketSize;
        time = time * bucketSize;
        if (list.size() > 0 && list.getFirst().time == time) {
            list.getFirst().value += value;
        } else {
            list.addFirst(new Bucket(time, value));
        }
        sum += value;
        wipe(now - size);
    }

    public long getSum() {
        wipe(timeSource.currentTimeMillis() - size);
        return sum;
    }

    @Override
    public String toString() {
        return "{size=" + size + ", bucketSize=" + bucketSize + ", totalSum=" + sum + "}";
    }
}
