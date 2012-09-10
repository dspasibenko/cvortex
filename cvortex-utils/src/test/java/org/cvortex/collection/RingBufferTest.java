package org.cvortex.collection;

import static org.junit.Assert.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.NoSuchElementException;

import org.junit.Test;

public class RingBufferTest {

    @Test(expected = IllegalArgumentException.class)
    public void zeroCapacity() {
        @SuppressWarnings("unused")
        RingBuffer<Integer> ringBuffer = new RingBuffer<Integer>(0);
    }

    @Test(expected = IllegalArgumentException.class)
    public void negativeCapacity() {
        @SuppressWarnings("unused")
        RingBuffer<Integer> ringBuffer = new RingBuffer<Integer>(-10);
    }

    @Test
    public void addTest() {
        RingBuffer<Integer> ringBuffer = new RingBuffer<Integer>(1);
        assertTrue(ringBuffer.add(1) == null);
        assertTrue(ringBuffer.add(2) == 1);
        assertTrue(ringBuffer.add(3) == 2);
        assertTrue(ringBuffer.size() == 1);
    }

    @Test
    public void addTest2() {
        RingBuffer<Integer> ringBuffer = new RingBuffer<Integer>(3);
        assertTrue(ringBuffer.add(1) == null);
        assertTrue(ringBuffer.add(2) == null);
        assertTrue(ringBuffer.size() == 2);
        assertTrue(ringBuffer.add(3) == null);
        assertTrue(ringBuffer.add(4) == 1);
        assertTrue(ringBuffer.add(5) == 2);
        assertTrue(ringBuffer.add(6) == 3);
        assertTrue(ringBuffer.size() == 3);
    }

    @Test(expected = NoSuchElementException.class)
    public void firstTest() {
        new RingBuffer<Integer>(3).first();
    }

    @Test
    public void firstTest2() {
        RingBuffer<Integer> ringBuffer = new RingBuffer<Integer>(2);
        ringBuffer.add(1);
        ringBuffer.add(2);
        ringBuffer.add(3);
        assertTrue(ringBuffer.first() == 2);
        assertTrue(ringBuffer.size() == 2);
    }

    @Test(expected = NoSuchElementException.class)
    public void lastTest() {
        new RingBuffer<Integer>(3).last();
    }

    @Test
    public void lastTest2() {
        RingBuffer<Integer> ringBuffer = new RingBuffer<Integer>(2);
        ringBuffer.add(1);
        ringBuffer.add(2);
        ringBuffer.add(3);
        assertTrue(ringBuffer.last() == 3);
    }

    @Test(expected = NoSuchElementException.class)
    public void removeFirst() {
        new RingBuffer<Integer>(3).removeFirst();
    }

    @Test
    public void removeFirst2() {
        RingBuffer<Integer> ringBuffer = new RingBuffer<Integer>(2);
        ringBuffer.add(1);
        ringBuffer.add(2);
        ringBuffer.add(3);
        ringBuffer.removeFirst();
        assertTrue(ringBuffer.last() == 3);
        assertTrue(ringBuffer.first() == 3);
        assertTrue(ringBuffer.size() == 1);
    }

    @SuppressWarnings("unchecked")
    @Test
    public void serialization() throws IOException, ClassNotFoundException {
        RingBuffer<Integer> ringBuffer = new RingBuffer<Integer>(3);
        ringBuffer.add(1);
        ringBuffer.add(2);
        ringBuffer.add(3);
        ringBuffer.removeFirst();

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream os = new ObjectOutputStream(baos);
        os.writeObject(ringBuffer);

        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
        ObjectInputStream ois = new ObjectInputStream(bais);

        RingBuffer<Integer> readBuffer = (RingBuffer<Integer>) ois.readObject();
        assertTrue(readBuffer.size() == 2);
        assertTrue(readBuffer.first() == 2);
        assertTrue(readBuffer.last() == 3);
    }
}
