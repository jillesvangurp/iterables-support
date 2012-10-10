package com.jillesvangurp.iterables;

import java.util.Iterator;

/**
 * Iterator that implements a peek method.
 *
 * @param <T>
 */
public class PeekableIterator<T> implements Iterator<T> {

    private final Iterator<T> iterator;
    T buffered=null;

    public PeekableIterator(Iterator<T> iterator) {
        this.iterator = iterator;
    }

    @Override
    public boolean hasNext() {
        return iterator.hasNext() || buffered != null;
    }

    @Override
    public T next() {
        T result = buffered;
        if(result != null) {
            buffered=null;
            return result;
        } else {
            return iterator.next();
        }
    }

    @Override
    public void remove() {
        iterator.remove();
    }

    /**
     * Allows you to peek at the next element without moving the iterator forward. You can call peek as often as you want.
     * @return the next element without moving the iterator forward.
     */
    public T peek() {
        if(buffered != null) {
            return buffered;
        } else {
            buffered=next();
            return buffered;
        }
    }
}
