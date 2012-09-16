package com.cvortex.cc.atd.impl;

import java.util.Comparator;
import java.util.TreeSet;

class QHolder<P extends QEntry<T>, T extends QEntry<P>> {

    private final TreeSet<P> pSet;
    
    private final TreeSet<T> tSet;
    
    QHolder(Comparator<P> pComparator, Comparator<T> tComparator) {
        pSet = new TreeSet<P>(pComparator);
        tSet = new TreeSet<T>(tComparator);
    }
 
    void putT(T t) {
        if (put(t, pSet)) {
            tSet.add(t);
        }
    }
    
    void putP(P p) {
        if (put(p, tSet)) {
            pSet.add(p);
        }
    }
    
    boolean removeT(T t) {
        return tSet.remove(t);
    }
    
    boolean removeP(P p) {
        return pSet.remove(p);
    }
    
    private <P1 extends QEntry<T1>, T1 extends QEntry<P1>> boolean put(P1 e, TreeSet<T1> queue) {
        T1 offeredTo = canOffer(e, queue);
        if (offeredTo != null) {
            offeredTo.offer(e);
            return false;
        }
        return true;
    }
    
    private <P1 extends QEntry<T1>, T1 extends QEntry<P1>> T1 canOffer(P1 e, TreeSet<T1> queue) {
        for (T1 qEntry: queue) {
            if (qEntry.isAcceptableFor(e)) {
                return qEntry;
            }
        }
        return null;
    }
   
    @Override
    public String toString() {
        return new StringBuilder().append("{pSet=").append(pSet).append(", tSet=")
                .append(tSet).append("}").toString();
    }
}
