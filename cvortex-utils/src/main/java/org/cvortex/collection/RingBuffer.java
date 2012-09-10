package org.cvortex.collection;

import java.io.Serializable;
import java.util.Arrays;
import java.util.NoSuchElementException;

public class RingBuffer<T> implements Serializable {

    private static final long serialVersionUID = -8275240829599598029L;

    private transient T[] values;

    private transient int size = 0;

    private transient int tailIdx = 0;

    @SuppressWarnings("unchecked")
    public RingBuffer(int capacity) {
        if (capacity <= 0) {
            throw new IllegalArgumentException();
        }
        this.values = (T[]) new Object[capacity];
    }

    public T add(T value) {
        T result = null;
        if (size == values.length) {
            result = values[tailIdx];
        } else {
            size++;
        }
        values[getTailIdxThenAdvance()] = value;
        return result;
    }

    private int getTailIdxThenAdvance() {
        int result = tailIdx;
        tailIdx = tailIdx == values.length - 1 ? 0 : tailIdx + 1;
        return result;
    }

    public T first() {
        assertSizeIsNotZero();
        return values[normalizeIndex(tailIdx - size)];
    }

    public T last() {
        assertSizeIsNotZero();
        return values[normalizeIndex(tailIdx - 1)];
    }

    public T removeFirst() {
        assertSizeIsNotZero();
        int firstIndex = normalizeIndex(tailIdx - size);

        T result = values[firstIndex];
        values[firstIndex] = null;
        --size;

        return result;
    }

    public void clear() {
        if (size > 0) {
            Arrays.fill(values, 0, values.length - 1, null);
        }
        size = 0;
        tailIdx = 0;
    }

    public int size() {
        return size;
    }

    public int capacity() {
        return values.length;
    }

    private void assertSizeIsNotZero() {
        if (size == 0) {
            throw new NoSuchElementException("RingBuffer is empty");
        }
    }

    private int normalizeIndex(int index) {
        if (index < 0) {
            return index + values.length;
        }
        if (index >= values.length) {
            return index - values.length;
        }
        return index;
    }

    private void writeObject(java.io.ObjectOutputStream s) throws java.io.IOException {
        s.defaultWriteObject();
        s.writeInt(size);
        s.writeInt(values.length);

        for (int i = 0; i < size; i++) {
            int idx = normalizeIndex(tailIdx - size + i);
            s.writeObject(values[idx]);
        }
    }

    @SuppressWarnings("unchecked")
    private void readObject(java.io.ObjectInputStream s) throws java.io.IOException, ClassNotFoundException {
        s.defaultReadObject();
        size = s.readInt();
        int arrayLength = s.readInt();
        Object[] a = new Object[arrayLength];

        for (int i = 0; i < size; i++) {
            a[i] = s.readObject();
        }
        tailIdx = size;
        values = (T[]) a;
    }

    @Override
    public String toString() {
        return new StringBuilder().append("RingBuffer: {size=").append(size).append(", capacity=").append(capacity())
                .append(", tailIdx=").append(tailIdx).append(", values=").append(Arrays.toString(values)).append("}")
                .toString();
    }
}
