package org.cvortex.collection;

import java.io.Serializable;
import java.util.AbstractCollection;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Iterator;
import java.util.NoSuchElementException;

public class SortedArray<T> extends AbstractCollection<T> implements Serializable {

    private static final long serialVersionUID = -7924982364593874916L;

    private final Comparator<T> comparator;

    private transient T[] elements;

    private int size = 0;

    public SortedArray(int capacity) {
        this(null, capacity);
    }

    public SortedArray(Comparator<T> comparator, int capacity) {
        this.comparator = comparator;
        setCapacity(capacity);
    }

    public SortedArray() {
        this(10);
    }

    @SuppressWarnings("unchecked")
    private void setCapacity(int newCapacity) {
        if (newCapacity < size) {
            throw new InternalError("wrong capacity requests " + newCapacity + " which is less than size=" + size);
        }
        T[] oldData = elements;
        elements = (T[]) new Object[newCapacity];
        if (oldData != null) {
            System.arraycopy(oldData, 0, elements, 0, size);
        }
    }

    public int size() {
        return size;
    }

    public void clear() {
        size = 0;
        elements = null;
        setCapacity(10);
    }

    public void trimToSize() {
        if (elements.length > size) {
            elements = Arrays.copyOf(elements, size);
        }
    }

    public T get(int index) {
        checkIndexInRange(index);
        return elements[index];
    }

    public boolean add(T element) {
        ensureCapacityBeforeInsertion();
        int idx = getIndexForInsertion(element);
        insertElementAtSpecificPosition(element, idx);
        return true;
    }

    private void insertElementAtSpecificPosition(T element, int idx) {
        System.arraycopy(elements, idx, elements, idx + 1, size - idx);
        elements[idx] = element;
        size++;
    }

    public boolean addByIndexIfPossible(T element, int index) {
        checkIndexInRange(index);

        int cmpResult = compare(element, elements[index]);
        if (cmpResult == 0 || (cmpResult < 0 && (index == 0 || compare(element, elements[index - 1]) >= 0))) {
            ensureCapacityBeforeInsertion();
            insertElementAtSpecificPosition(element, index);
            return true;
        }

        return false;
    }

    private void ensureCapacityBeforeInsertion() {
        if (size == elements.length) {
            setCapacity(size * 3 / 2 + 1);
        }
    }

    private int getIndexForInsertion(T element) {
        if (size == 0) {
            return 0;
        }
        if (compare(elements[size - 1], element) <= 0) {
            return size;
        }
        int idx = getIndexOf(element);
        if (idx < 0) {
            return -idx - 1;
        }
        return idx;
    }

    @SuppressWarnings("unchecked")
    private int compare(T element1, T element2) {
        return comparator != null ? comparator.compare(element1, element2) : ((Comparable<T>) element1)
                .compareTo(element2);
    }

    public int getIndexOf(T element) {
        return comparator == null ? Arrays.binarySearch(elements, 0, size, element) : Arrays.binarySearch(elements, 0,
                size, element, comparator);
    }

    public T removeElement(T element) {
        int idx = getIndexOf(element);
        return idx >= 0 ? removeByIndex(idx) : null;
    }

    public T removeByIndex(int index) {
        checkIndexInRange(index);
        if (index < --size) {
            System.arraycopy(elements, index + 1, elements, index, size - index);
        }
        T result = elements[size];
        elements[size] = null;
        return result;
    }

    private void checkIndexInRange(int index) {
        if (index < 0 || index >= size) {
            throw new IndexOutOfBoundsException("index: " + index + ", size: " + size);
        }
    }

    private void writeObject(java.io.ObjectOutputStream s) throws java.io.IOException {
        s.defaultWriteObject();
        s.writeInt(elements.length);

        for (int i = 0; i < size; i++) {
            s.writeObject(elements[i]);
        }
    }

    @SuppressWarnings("unchecked")
    private void readObject(java.io.ObjectInputStream s) throws java.io.IOException, ClassNotFoundException {
        s.defaultReadObject();
        int arrayLength = s.readInt();
        Object[] a = new Object[arrayLength];

        for (int i = 0; i < size; i++) {
            a[i] = s.readObject();
        }
        elements = (T[]) a;
    }

    public Iterator<T> iterator() {
        return new Iterator<T>() {

            private int idx = 0;

            private int removeIdx = -1;

            public boolean hasNext() {
                return idx < size;
            }

            public T next() {
                if (!hasNext()) {
                    throw new NoSuchElementException();
                }
                removeIdx = idx++;
                return elements[removeIdx];
            }

            public void remove() {
                if (removeIdx == -1) {
                    throw new IllegalStateException();
                }
                removeByIndex(removeIdx);
                removeIdx = -1;
            }
        };
    }

    @SuppressWarnings("unchecked")
    @Override
    public boolean contains(Object o) {
        return getIndexForInsertion((T) o) >= 0;
    }

    @Override
    public Object[] toArray() {
        return Arrays.copyOf(elements, size);
    }

    @Override
    @SuppressWarnings("unchecked")
    public boolean remove(Object o) {
        return removeElement((T) o) != null;
    }

    @Override
    public int hashCode() {
        int hashCode = size;
        for (T element : elements) {
            hashCode = 31 * hashCode + element.hashCode();
        }
        return hashCode;
    }

    @Override
    public String toString() {
        return new StringBuilder().append("SortedArray: {size=").append(size).append(", capacity=")
                .append(elements.length).append(", elements=").append(Arrays.toString(elements)).append("}").toString();
    }
}
