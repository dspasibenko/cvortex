package org.cvortex.collection;

import static org.junit.Assert.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Random;
import java.util.TreeSet;

import org.junit.Test;

public class SortedArrayTest {

    private Comparator<Integer> descComparator = new DescComparator();

    private static class DescComparator implements Comparator<Integer>, Serializable {

        private static final long serialVersionUID = 1L;

        public int compare(Integer o1, Integer o2) {
            return o2.compareTo(o1);
        }

    };

    @Test
    public void add() {
        SortedArray<Integer> sa = new SortedArray<Integer>();
        sa.add(2);
        sa.add(3);
        sa.add(1);
        assertTrue(sa.getIndexOf(3) == 2);
        assertTrue(sa.getIndexOf(2) == 1);
        assertTrue(sa.getIndexOf(1) == 0);
    }

    @Test
    public void add2() {
        SortedArray<Integer> sa = new SortedArray<Integer>(descComparator, 2);
        sa.add(2);
        sa.add(3);
        sa.add(1);
        assertTrue(sa.get(0) == 3);
        assertTrue(sa.get(1) == 2);
        assertTrue(sa.get(2) == 1);
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void get() {
        new SortedArray<Integer>().get(0);
    }

    @Test
    public void get2() {
        SortedArray<Integer> sa = new SortedArray<Integer>();
        sa.add(1);
        assertTrue(sa.get(0) == 1);
    }

    @Test
    public void remove() {
        SortedArray<Integer> sa = new SortedArray<Integer>();
        sa.add(1);
        assertTrue(sa.removeElement(0) == null);
        assertTrue(sa.removeElement(1) == 1);
        assertTrue(sa.size() == 0);
    }

    @Test
    public void removeByIndex() {
        SortedArray<Integer> sa = new SortedArray<Integer>();
        sa.add(1);
        assertTrue(sa.removeByIndex(0) == 1);
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void removeByIndex2() {
        SortedArray<Integer> sa = new SortedArray<Integer>();
        sa.add(1);
        assertTrue(sa.removeByIndex(1) == 1);
    }

    @Test
    public void getIndexOf() {
        SortedArray<Integer> sa = new SortedArray<Integer>();
        sa.add(1);
        assertTrue(sa.getIndexOf(0) < 0);
        assertTrue(sa.getIndexOf(1) == 0);
    }

    @SuppressWarnings("unchecked")
    @Test
    public void serialization() throws IOException, ClassNotFoundException {
        SortedArray<Integer> sa = new SortedArray<Integer>(descComparator, 1);
        sa.add(1);
        sa.add(2);
        sa.add(3);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream os = new ObjectOutputStream(baos);
        os.writeObject(sa);

        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
        ObjectInputStream ois = new ObjectInputStream(bais);

        SortedArray<Integer> readArray = (SortedArray<Integer>) ois.readObject();
        assertTrue(readArray.size() == 3);
        assertTrue(readArray.get(0) == 3);
        assertTrue(readArray.get(1) == 2);
        assertTrue(readArray.get(2) == 1);
        readArray.add(5);
        assertTrue(readArray.get(0) == 5);
    }

    @Test
    public void iteratorTest() {
        SortedArray<Integer> sa = new SortedArray<Integer>();
        for (int idx = 0; idx < 1000; idx++) {
            sa.add(idx);
        }

        int value = 0;
        for (Integer i : sa) {
            assertTrue(i == value++);
        }

        assertTrue(value == 1000);
    }

    @Test
    public void toStringTest() {
        SortedArray<Integer> sa = new SortedArray<Integer>();
        sa.add(1);
        sa.add(2);
        sa.add(-3);
        assertTrue(sa.toString().contains("SortedArray: {size=3, capacity=10, elements=[-3, 1, 2"));
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void addByIndexIfPossibleTest0() {
        new SortedArray<Integer>().addByIndexIfPossible(1, -1);
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void addByIndexIfPossibleTest1() {
        new SortedArray<Integer>().addByIndexIfPossible(1, 1);
    }

    @Test
    public void addByIndexIfPossibleTest3() {
        SortedArray<Integer> sa = new SortedArray<Integer>();
        sa.add(10);
        sa.add(20);
        assertFalse(sa.addByIndexIfPossible(9, 1));
        assertFalse(sa.addByIndexIfPossible(21, 0));
        assertFalse(sa.addByIndexIfPossible(11, 0));
        assertTrue(sa.addByIndexIfPossible(8, 0)); // now it is 8, 10, 20
        assertTrue(sa.addByIndexIfPossible(8, 1)); // now it is 8, 8, 10, 20
        assertTrue(sa.addByIndexIfPossible(9, 2)); // now it is 8, 8, 9, 10, 20
    }

    @Test
    public void performanceTest() {
        Random rand = new Random();
        HashSet<Integer> values = new HashSet<Integer>();
        while (values.size() < 1000) {
            values.add(rand.nextInt());
        }
        Integer valuesArr[] = new Integer[1000];
        valuesArr = values.toArray(valuesArr);

        TreeSet<Integer> ts = new TreeSet<Integer>();
        int changesCountInTreeSet = 0;
        long endTime = System.currentTimeMillis() + 1000;

        int idx = 0;
        while (System.currentTimeMillis() < endTime) {
            ts.add(valuesArr[idx]);
            idx = (idx + 1) % 1000;
            while (ts.size() > 500) {
                ts.remove(valuesArr[idx]);
                idx = (idx + 1) % 1000;
                changesCountInTreeSet++;
            }
            changesCountInTreeSet++;
        }

        System.out.println("TreeSet iterations " + changesCountInTreeSet);

        SortedArray<Integer> sa = new SortedArray<Integer>();
        int changesCountInArray = 0;
        endTime = System.currentTimeMillis() + 1000;
        idx = 0;
        while (System.currentTimeMillis() < endTime) {
            sa.add(valuesArr[idx]);
            idx = (idx + 1) % 1000;
            while (sa.size() > 500) {
                sa.removeElement(valuesArr[idx]);
                idx = (idx + 1) % 1000;
                changesCountInArray++;
            }
            changesCountInArray++;
        }

        System.out.println("Array iterations " + changesCountInArray);
    }

}
