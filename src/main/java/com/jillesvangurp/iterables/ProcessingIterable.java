package com.jillesvangurp.iterables;

import java.util.Iterator;

/**
 * Non concurrent variant of the {@link ConcurrentProcessingIterable}.
 *
 * @param <Input> input type
 * @param <Output> output type
 */
public class ProcessingIterable <Input,Output> implements Iterable<Output> {

    private final Iterator<Input> it;
    private final Processor<Input, Output> processor;

    public ProcessingIterable(Iterator<Input> it, Processor<Input, Output> processor) {
        this.it = it;
        this.processor = processor;
    }

    @Override
    public Iterator<Output> iterator() {
        return new Iterator<Output>() {

            @Override
            public boolean hasNext() {
                return it.hasNext();
            }

            @Override
            public Output next() {
                return processor.process(it.next());
            }

            @Override
            public void remove() {
                it.remove();
            }};
    }
}
