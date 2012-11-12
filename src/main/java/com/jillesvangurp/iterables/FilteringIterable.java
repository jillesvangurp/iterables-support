package com.jillesvangurp.iterables;

import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * Filter the elements in an Iterable using a {@link Filter}.
 * 
 * @param <T>
 */
public class FilteringIterable<T> implements Iterable<T> {
    private final Iterable<T> iterable;
    private final Filter<T> filter;

    public FilteringIterable(Iterable<T> iterable, Filter<T> filter) {
        this.iterable = iterable;
        this.filter = filter;
    }

    @Override
    public Iterator<T> iterator() {
        final Iterator<T> iterator = iterable.iterator();
        return new Iterator<T>() {
            T next = null;

            @Override
            public boolean hasNext() {
                while (iterator.hasNext() && next == null) {
                    T candidate = iterator.next();
                    try {
                        if (filter.passes(candidate)) {
                            next = candidate;
                        }
                    } catch (PermanentlyFailToPassException e) {
                        // special poison pill to force the iterator to abort
                        return false;
                    }
                }
                return next != null;
            }

            @Override
            public T next() {
                if (hasNext()) {
                    T result = next;
                    next = null;
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
