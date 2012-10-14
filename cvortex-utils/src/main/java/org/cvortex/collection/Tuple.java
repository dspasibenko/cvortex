package org.cvortex.collection;

public final class Tuple<A, B> {
    
    private final A first;
    
    private final B second;

    public Tuple(A first, B second) {
        this.first = first;
        this.second = second;
    }

    public A getFirst() {
        return first;
    }

    public B getSecond() {
        return second;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Tuple<?, ?> tuple = (Tuple<?, ?>) o;
        if (first != null ? !first.equals(tuple.first) : tuple.first != null) {
            return false;
        }
        if (second != null ? !second.equals(tuple.second) : tuple.second != null) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int result = first != null ? first.hashCode() : 1;
        result = 31 * result + (second != null ? second.hashCode() : 0);
        return result;
    }
    
    @Override
    public String toString() {
        return new StringBuilder().append("Tuple {first=").append(first)
                    .append(", second=").append(second).append("}").toString();
    }
}
