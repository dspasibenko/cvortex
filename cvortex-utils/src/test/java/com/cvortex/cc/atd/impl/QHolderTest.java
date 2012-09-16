package com.cvortex.cc.atd.impl;

import java.util.Comparator;
import java.util.Set;

import org.cvortex.util.testing.MockUtils;
import org.junit.Assert;
import org.junit.Test;

public class QHolderTest extends Assert {

    private final QHolder<A, B> qHolder = new QHolder<A, B>(new CComparator<A>(), new CComparator<B>());
    
    private final A a = new A();
    
    private final B b = new B();
    
    private static class C<T> implements QEntry<T> {
        
        private static int idx = 0;
        
        private int id = idx++;

        private boolean acceptable = true;
        private boolean offered = false;
        
        public void setAcceptable(boolean acceptable) {
            this.acceptable = acceptable;
        }

        public boolean isOffered() {
            return offered;
        }

        @Override
        public boolean canBePlacedToQueue() {
            return true;
        }

        @Override
        public boolean isAcceptableFor(T anotherEntry) {
            return acceptable;
        }

        @Override
        public void offer(T entry) {
            offered = true;
        }
    }
    
    private static class A extends C<B> { };
    private static class B extends C<A> { };
    
    private static class CComparator<T extends C<?>> implements Comparator<T> {

        @Override
        public int compare(T o1, T o2) {
            int p1 = o1.id;
            int p2 = o2.id;
            return (p1 < p2 ? -1 : (p1 == p2 ? 0 : 1));
        }
        
    }
    
    @Test
    public void offerAB() {
        qHolder.putP(a);
        qHolder.putT(b);
        assertTrue(a.isOffered());
        assertFalse(b.isOffered());
        checkPSetSize(1);
        checkTSetSize(0);
    }
    
    @Test
    public void offerABB() {
        qHolder.putP(a);
        A a2 = new A();
        qHolder.putP(a2);
        a.setAcceptable(false);
        qHolder.putT(b);
        assertFalse(a.isOffered());
        assertTrue(a2.isOffered());
        assertFalse(b.isOffered());
        checkPSetSize(2);
        checkTSetSize(0);
    }
    
    @Test
    public void offerABBNegative() {
        qHolder.putP(a);
        A a2 = new A();
        qHolder.putP(a2);
        a.setAcceptable(false);
        a2.setAcceptable(false);
        qHolder.putT(b);
        assertFalse(a.isOffered());
        assertFalse(a2.isOffered());
        assertFalse(b.isOffered());
        checkPSetSize(2);
        checkTSetSize(1);
    }
    
    @Test
    public void offerBA() {
        qHolder.putT(b);
        qHolder.putP(a);
        assertTrue(b.isOffered());
        assertFalse(a.isOffered());
    }
    
    @Test
    public void offerABFailed() {
        qHolder.putP(a);
        a.setAcceptable(false);
        qHolder.putT(b);
        assertFalse(a.isOffered());
        assertFalse(b.isOffered());
    }
    
    @Test
    public void offerBAFailed() {
        qHolder.putT(b);
        b.setAcceptable(false);
        qHolder.putP(a);
        assertFalse(b.isOffered());
        assertFalse(a.isOffered());
    }
    
    @Test
    public void removeA() {
        qHolder.putP(a);
        qHolder.removeP(a);
        checkPSetSize(0);
    }
    
    @Test
    public void removeB() {
        qHolder.putT(b);
        qHolder.removeT(b);
        checkTSetSize(0);
    }

    @SuppressWarnings("unchecked")
    private void checkPSetSize(int size) {
        assertEquals(size, ((Set<A>) MockUtils.getFieldValue(qHolder, "pSet")).size());
    }
    
    @SuppressWarnings("unchecked")
    private void checkTSetSize(int size) {
        assertEquals(size, ((Set<B>) MockUtils.getFieldValue(qHolder, "tSet")).size());
    }
}
