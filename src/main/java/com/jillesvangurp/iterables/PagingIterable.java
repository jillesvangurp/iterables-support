package com.jillesvangurp.iterables;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

import org.apache.commons.lang.Validate;

class PagingIterable<T> implements Iterable<List<T>> {
    private final Iterable<T> input;
    private final int pageSize;

    public PagingIterable(Iterable<T> input, int pageSize) {
        Validate.isTrue(pageSize > 0);
        this.input = input;
        this.pageSize = pageSize;
    }

    @Override
    public Iterator<List<T>> iterator() {
        final Iterator<T> it = input.iterator();

        return new Iterator<List<T>>() {
            List<T> next=null;

            @Override
            public boolean hasNext() {
                if(next != null) {
                    return true;
                } else if(it.hasNext()) {
                    next = new ArrayList<>(pageSize);
                    int i = 0;
                    while(i < pageSize && it.hasNext()) {
                        next.add(it.next());
                        i++;
                    }
                    if(next.size() > 0) {
                        return true;
                    } else {
                        return false;
                    }

                } else {
                    return false;
                }
            }

            @Override
            public List<T> next() {
                if(hasNext()) {
                    List<T> result = next;
                    next=null;
                    return result;
                } else {
                    throw new NoSuchElementException();
                }
            }

            @Override
            public void remove() {
                throw new UnsupportedOperationException();
            }
        };
    }

}