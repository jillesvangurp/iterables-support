package com.jillesvangurp.iterables;

import java.util.Iterator;

/**
 * Collection of static methods to make working with the various iterables a bit nicer.
 */
public class Iterables {
    
    /**
     * Wraps an iterator with an iterable so that you may use it in a for loop.
     * @param it any iterator
     * @return an iterable that may be used with a for loop
     */
    public static <T> Iterable<T> toIterable(final Iterator<T> it) {
        return new Iterable<T>() {
            @Override
            public Iterator<T> iterator() {
                return it;
            }
        };
    }

    /**
     * @param it
     * @param filter
     * @return A filtering iterable that applies the the provided filter. 
     */
    public static <S> Iterable<S> filter(Iterable<S> it, Filter<S> filter) {
        return new FilteringIterable<S>(it, filter);
    }

    /**
     * @param it
     * @param from
     * @param to
     * @return elements between from and to in the wrapped iterator.
     */
    public static <S> Iterable<S> filterRange(Iterable<S> it, final long from, final long to) {
        return filter(it, new Filter<S>() {
            long count=0;
    
            @Override
            public boolean passes(S o) {
                long current = count++;
                return current >=from && current <=to;
            }
        });
    }

    /**
     * @param it
     * @param to
     * @return the elements in the wrapped iterator until element number to
     */
    public static <S> Iterable<S> head(Iterable<S> it, final long to) {
        return filter(it, new Filter<S>() {
            long count=0;
    
            @Override
            public boolean passes(S o) {
                if (count++ <=to) {
                    return true;
                } else {
                    throw new PermanentlyFailToPassException();
                }
            }
        });
    }

    /**
     * @param it
     * @param from
     * @return iterable that iterates the elementents from the 'from'th element in the wrapped iterator.
     */
    public static <S> Iterable<S> from(Iterable<S> it, final long from) {
        return filter(it, new Filter<S>() {
            long count=0;
    
            @Override
            public boolean passes(S o) {
                long current = count++;
                return current >=from;
            }
        });
    }

    /**
     * Implement a map operation that applies a processor to each element in the wrapped iterator and iterates over the resulting output.
     * @param it
     * @param processor
     * @return iteratable over the output of the processor on the input iterator
     */
    public static <I,O> Iterable<O> map(Iterable<I> it, Processor<I,O> processor) {
        return new ProcessingIterable<I, O>(it.iterator(), processor);
    }
    
    /**
     * Compose two or more processors into one.
     * @param first transform into intermediate type
     * @param last transform intermediate type into output type
     * @param extraSteps optional, varargs with extra transformation steps on the output type
     * @return a processor that composes the argumentst
     */
    @SafeVarargs
    public static <I,S,O> Processor<I,O> compose(final Processor<I,S> first, final Processor<S,O> last, final Processor<O,O>...extraSteps) {
        return new Processor<I, O>() {

            @Override
            public O process(I input) {
                O result = last.process(first.process(input));
                for (Processor<O, O> processor : extraSteps) {
                    result = processor.process(result);
                }
                return result;
            }
        };
    }
    
    /**
     * @param input
     * @param processor
     * @param blockSize
     * @param threadPoolSize
     * @param queueCapacity
     * @return a concurrent processing iterable that will process the input iterable concurrently and offer the output as another iterable.
     */
    public static <Input,Output> ConcurrentProcessingIterable<Input, Output> processConcurrently(Iterable<Input> input, Processor<Input,Output> processor, int blockSize, int threadPoolSize, int queueCapacity) {
        return new ConcurrentProcessingIterable<Input,Output>(input, processor, blockSize, threadPoolSize, queueCapacity);
    }
}
