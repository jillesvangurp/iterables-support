package com.jillesvangurp.iterables;

import java.util.Iterator;
import java.util.NoSuchElementException;

public class FilteringIterable<S> implements Iterable<S> {
    private final Iterable<S> iterable;
    private final Filter<S> filter;

    public FilteringIterable(Iterable<S> iterable, Filter<S> filter) {
        this.iterable = iterable;
        this.filter = filter;
    }

    @Override
    public Iterator<S> iterator() {
        final Iterator<S> iterator = iterable.iterator();
        return new Iterator<S>() {
            S next = null;

            @Override
            public boolean hasNext() {
                while (iterator.hasNext() && next == null) {
                    S candidate = iterator.next();
                    if (filter.passes(candidate)) {
                        next = candidate;
                    }
                }
                return next != null;
            }

            @Override
            public S next() {
                if (hasNext()) {
                    S result = next;
                    next=null;
                    return result;
                } else {
                    throw new NoSuchElementException();
                }
            }

            @Override
            public void remove() {
                throw new UnsupportedOperationException("remove is not supported");
            }
        };
    }
}
