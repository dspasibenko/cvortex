package org.cvortex.statistic;

import org.cvortex.collection.RingBuffer;
import org.cvortex.collection.Tuple;

public abstract class TimeBasedAbstractWindow<T> {

    protected final RingBuffer<Tuple<Long, T>> buffer;

    protected final long timeIntervalMs;

    protected abstract void onFirstRemove(T element);

    protected void sweep(long timeMs) {
        if (buffer.size() == 0) {
            return;
        }
        Tuple<Long, T> firstElement = buffer.first();
        while (firstElement != null && firstElement.getFirst() < timeMs - timeIntervalMs) {
            onFirstRemove(buffer.removeFirst().getSecond());
            if (buffer.size() == 0) {
                return;
            }
            firstElement = buffer.first();
        }
    }

    public TimeBasedAbstractWindow(int capacity, long timeIntervalMs) {
        this.buffer = new RingBuffer<Tuple<Long, T>>(capacity);
        this.timeIntervalMs = timeIntervalMs;
    }

    public void add(long timeMs, T value) {
        Tuple<Long, T> p = new Tuple<Long, T>(timeMs, value);

        while(!buffer.add(p)) {
            Tuple<Long, T> removed = buffer.removeFirst();
            if (removed != null) {
                onFirstRemove(removed.getSecond());
            }
        }
        sweep(timeMs);
    }

    public int size() {
        return buffer.size();
    }

    @Override
    public String toString() {
        return new StringBuilder().append(" buffer={").append(buffer).append("}, timeIntervalMs=")
                .append(timeIntervalMs).toString();
    }
}
