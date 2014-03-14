package com.jillesvangurp.iterables;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

/**
 * Collection of static methods for working with iterators and iterables that allow you to filter, process, etc.
 * elements in an Iterable. The methods in this class make it easy to use many of the more primitive functionality
 * in this library. You can implement concurrent map reduce logic with just a few lines of code for example.
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
     * @param array
     * @return Iterable that allows you to iterate over the array
     */
    public static <T> Iterable<T> toIterable(final T[] array) {
        return new Iterable<T>() {

            @Override
            public Iterator<T> iterator() {
                return new Iterator<T>() {
                    int index=0;

                    @Override
                    public boolean hasNext() {
                        return index<array.length;
                    }

                    @Override
                    public T next() {
                        if(hasNext()) {
                            return array[index++];
                        } else {
                            throw new NoSuchElementException();
                        }
                    }

                    @Override
                    public void remove() {
                        throw new UnsupportedOperationException("arrays don't support removing elements");
                    }
                };
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

    public static <T> T reduce(Iterable<T> it, Reducer<T> reducer) {
        Iterator<T> iterator = it.iterator();
        if(!iterator.hasNext()) {
            throw new NoSuchElementException();
        } else {
            T current = iterator.next();
            T output = reducer.reduce(current);
            while(iterator.hasNext()) {
                current = iterator.next();
                output = reducer.reduce(output, current);
            }
            return output;
        }
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
     * Process iterable concurrently using the processor. IMPORTANT, you must close the iterable (it implements Closeable) after use otherwise, the process
     * may never exit.
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

    public static <Input,Output> Output mapReduce(Iterable<Input> input, final Processor<Input,Output> mapper, final Reducer<Output> reducer, int blockSize, int threadPoolSize, int queueCapacity) {
        Processor<List<Input>, Output> pageProcessor = new Processor<List<Input>, Output> () {
            @Override
            public Output process(List<Input> input) {
                return reduce(map(input, mapper), reducer);
            }

        };

        Output result = null;
        try(ConcurrentProcessingIterable<List<Input>, Output> processor = processConcurrently(page(input, blockSize), pageProcessor, blockSize, threadPoolSize, queueCapacity)) {
            result = reduce(processor, reducer);
        } catch (IOException e) {
            throw new IllegalStateException("error during map reduce", e);
        }
        return result;
    }

    /**
     * Given a number of iterables, construct a iterable that iterates all of the iterables.
     * @param iterables
     * @return an iterable that can provide a single iterator for all the elements of the iterables.
     */
    public static <V> Iterable<V> compose(final Iterable<Iterable<V>> iterables) {
        return toIterable(new Iterator<V>() {
            Iterator<Iterable<V>> it=iterables.iterator();
            Iterator<V> current = null;
            V next = null;

            @Override
            public boolean hasNext() {
                if(next != null) {
                    return true;
                } else {
                    if((current == null || !current.hasNext()) && it.hasNext()) {
                        while(it.hasNext() && (current == null || !current.hasNext())) {
                            Iterable<V> nextIt = it.next();
                            if(nextIt != null) {
                                current = nextIt.iterator();
                            }
                        }
                    }
                    if(current !=null && current.hasNext()) {
                        next = current.next();
                        return true;
                    }
                }
                return false;
            }

            @Override
            public V next() {
                if(hasNext()) {
                    V result = next;
                    next = null;
                    return result;
                } else {
                    throw new NoSuchElementException();
                }
            }

            @Override
            public void remove() {
                throw new UnsupportedOperationException("Remove is not supported");
            }
        });
    }

    /**
     * Allows you to iterate over objects and cast them to the appropriate type on the fly.
     * @param it
     * @param clazz
     * @return an iterable that casts elements to the specified class.
     * @throws ClassCastException if the elements are not castable
     */
    public static <I,O> Iterable<O> castingIterable(Iterable<I> it, Class<O> clazz) {
        return map(it, new Processor<I,O>() {
            @SuppressWarnings("unchecked")
            @Override
            public O process(I input) {
                return (O)input;
            }});
    }

    public static <V> long count(Iterable<V> iterable) {
        long count = 0;
        for(@SuppressWarnings("unused") V e:iterable) {
            count++;
        }
        return count;
    }

    /**
     * Creates an iterator and iterates over it without doing anything thus 'consuming' the iterable. Useful when
     * using processing iterables where the side effects are more interesting than the return value of process.
     */
    public static void consume(Iterable<?> it) {
        Iterator<?> iterator = it.iterator();
        consume(iterator);
    }

    /**
     *Iterates over an iterator without doing anything with the elements thus 'consuming' the iterator. Useful when
     * using processing iterables where the side effects are more interesting than the return value of process.
     */
    public static void consume(Iterator<?> iterator) {
        while(iterator.hasNext()) {
            iterator.next();
        }
    }

    public static <T> Iterable<List<T>> page(Iterable<T> it, int pageSize) {
        return new PagingIterable<>(it, pageSize);
    }
}
