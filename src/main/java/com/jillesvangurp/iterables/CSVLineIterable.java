package com.jillesvangurp.iterables;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

import com.google.common.base.Splitter;

/**
 * Simple iterable that breaks lines into fields based on a separator.
 * Note. this class is fairly simplistic compared to solutions like open csv and doesn't support things like escaping
 * currently. Feel free to contribute patches to fix this.
 */
public class CSVLineIterable implements Iterable<List<String>> {
    private final Iterable<String> lineIterator;
    private final char delimiter;

    public CSVLineIterable(Iterable<String> lineIterator, char delimiter) {
        this.lineIterator = lineIterator;
        this.delimiter = delimiter;
    }

    @Override
    public Iterator<List<String>> iterator() {
        final Iterator<String> iterator = lineIterator.iterator();
        return new Iterator<List<String>>() {

            @Override
            public boolean hasNext() {
                return iterator.hasNext();
            }

            @Override
            public List<String> next() {
                String line = iterator.next();
                if (line != null) {
                    List<String> result = new ArrayList<>();
                    for (String field : Splitter.on(delimiter).trimResults().split(line)) {
                        result.add(field);
                    }
                    return result;
                } else {
                    throw new NoSuchElementException();
                }
            }

            @Override
            public void remove() {
                iterator.remove();
            }
        };
    }
}
