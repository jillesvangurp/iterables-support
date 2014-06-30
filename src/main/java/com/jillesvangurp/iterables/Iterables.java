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
     * @param <T> type
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
     * @param array array with elements
     * @param <T> type
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
     * @param it an iterable
     * @param filter a filter
     * @param <T> type
     * @return A filtering iterable that applies the the provided filter.
     */
    public static <T> Iterable<T> filter(Iterable<T> it, Filter<T> filter) {
        return new FilteringIterable<T>(it, filter);
    }

    /**
     * @param it an iterable
     * @param from start position
     * @param to end position
     * @param <T> type
     * @return elements between from and to in the wrapped iterator.
     */
    public static <T> Iterable<T> filterRange(Iterable<T> it, final long from, final long to) {
        return filter(it, new Filter<T>() {
            long count=0;

            @Override
            public boolean passes(T o) {
                long current = count++;
                return current >=from && current <=to;
            }
        });
    }

    /**
     * @param it an iterable
     * @param to start position
     * @param <T> type
     * @return the elements in the wrapped iterator until element number to
     */
    public static <T> Iterable<T> head(Iterable<T> it, final long to) {
        return filter(it, new Filter<T>() {
            long count=0;

            @Override
            public boolean passes(T o) {
                if (count++ <=to) {
                    return true;
                } else {
                    throw new PermanentlyFailToPassException();
                }
            }
        });
    }

    /**
     * @param it an iterable
     * @param from end position
     * @param <T> type
     * @return iterable that iterates the elementents from the 'from'th element in the wrapped iterator.
     */
    public static <T> Iterable<T> from(Iterable<T> it, final long from) {
        return filter(it, new Filter<T>() {
            long count=0;

            @Override
            public boolean passes(T o) {
                long current = count++;
                return current >=from;
            }
        });
    }

    /**
     * Implement a map operation that applies a processor to each element in the wrapped iterator and iterates over the resulting output.
     * @param it an iterable of I
     * @param processor a processors that converts I to O
     * @param <I> input
     * @param <O> output
     * @return iteratable over the output of the processor on the input iterator
     */
    public static <I,O> Iterable<O> map(Iterable<I> it, Processor<I,O> processor) {
        return new ProcessingIterable<I, O>(it.iterator(), processor);
    }

    /**
     * @param it an iterable
     * @param reducer reducer class that produces a T out of the iterable of T
     * @param <T> type that is reduced
     * @return a value T
     */
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
     * @param <I> Input of first processor
     * @param <S> Output of first and input of second processor
     * @param <O> Output of second processor
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
     * @param input input iterable
     * @param processor a processor that transforms I into O
     * @param blockSize number of items that is processed in one go by each consumer thread
     * @param threadPoolSize number of threads (including the producer threads). CPU count +1 is typically what you want for CPU constrained tasks.
     * @param queueCapacity number of items that get queued. Tune this to ensure the consumer threads don't run out of work.
     * @param <I> input type
     * @param <O> output type
     * @return a concurrent processing iterable that will process the input iterable concurrently and offer the output as another iterable.
     */
    public static <I,O> ConcurrentProcessingIterable<I, O> processConcurrently(Iterable<I> input, Processor<I,O> processor, int blockSize, int threadPoolSize, int queueCapacity) {
        return new ConcurrentProcessingIterable<I,O>(input, processor, blockSize, threadPoolSize, queueCapacity);
    }

    /**
     * @param input input iterable
     * @param mapper processor that transforms I into O
     * @param reducer reducer that reduces iterables of O into a single O value
     * @param blockSize number of items that is processed in one go by each consumer thread
     * @param threadPoolSize number of threads (including the producer threads). CPU count +1 is typically what you want for CPU constrained tasks.
     * @param queueCapacity number of items that get queued. Tune this to ensure the consumer threads don't run out of work.
     * @param <I> input type
     * @param <O> output type
     * @return a value of the Output type
     */
    public static <I,O> O mapReduce(Iterable<I> input, final Processor<I,O> mapper, final Reducer<O> reducer, int blockSize, int threadPoolSize, int queueCapacity) {
        Processor<List<I>, O> pageProcessor = new Processor<List<I>, O> () {
            @Override
            public O process(List<I> input) {
                return reduce(map(input, mapper), reducer);
            }

        };

        O result = null;
        try(ConcurrentProcessingIterable<List<I>, O> processor = processConcurrently(page(input, blockSize), pageProcessor, blockSize, threadPoolSize, queueCapacity)) {
            result = reduce(processor, reducer);
        } catch (IOException e) {
            throw new IllegalStateException("error during map reduce", e);
        }
        return result;
    }

    /**
     * Given a number of iterables, construct a iterable that iterates all of the iterables.
     * @param iterables iterable of iterables of T that need to be combined into one
     * @param <T> type
     * @return an iterable that can provide a single iterator for all the elements of the iterables.
     */
    public static <T> Iterable<T> compose(final Iterable<Iterable<T>> iterables) {
        return toIterable(new Iterator<T>() {
            Iterator<Iterable<T>> it=iterables.iterator();
            Iterator<T> current = null;
            T next = null;

            @Override
            public boolean hasNext() {
                if(next != null) {
                    return true;
                } else {
                    if((current == null || !current.hasNext()) && it.hasNext()) {
                        while(it.hasNext() && (current == null || !current.hasNext())) {
                            Iterable<T> nextIt = it.next();
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
            public T next() {
                if(hasNext()) {
                    T result = next;
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
     * @param it iterable of I
     * @param clazz class to cast to
     * @param <I> input type
     * @param <O> output type
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

    /**
     * @param iterable an iterable
     * @param <T> type
     * @return the number of items in the iterable
     */
    public static <T> long count(Iterable<T> iterable) {
        long count = 0;
        for(@SuppressWarnings("unused") T e:iterable) {
            count++;
        }
        return count;
    }

    /**
     * Creates an iterator and iterates over it without doing anything thus 'consuming' the iterable. Useful when
     * using processing iterables where the side effects are more interesting than the return value of process.
     * @param it the iterable to consume
     */
    public static void consume(Iterable<?> it) {
        Iterator<?> iterator = it.iterator();
        consume(iterator);
    }

    /**
     *Iterates over an iterator without doing anything with the elements thus 'consuming' the iterator. Useful when
     * using processing iterables where the side effects are more interesting than the return value of process.
     * @param iterator the iterator to consume
     */
    public static void consume(Iterator<?> iterator) {
        while(iterator.hasNext()) {
            iterator.next();
        }
    }

    /**
     * @param it an iterable
     * @param pageSize number of items in each page
     * @param <T> type
     * @return an iterable of lists with the specified number of items in each
     */
    public static <T> Iterable<List<T>> page(Iterable<T> it, int pageSize) {
        return new PagingIterable<>(it, pageSize);
    }
}
