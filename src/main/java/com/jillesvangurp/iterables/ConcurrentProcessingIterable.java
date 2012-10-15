package com.jillesvangurp.iterables;

import java.io.Closeable;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Iterable that processes the input concurrently using a {@link Processor} to produce its output.
 * 
 * Please note that this class implements {@link Closeable} and that you are supposed to use a try with resources
 * call. The reason for this is to guarantee the executor used for delegating the work is shut down correctly.
 * 
 * @param <Input> type of the input processed by this iterable
 * @param <Output> type of the output processed by this iterable
 */
public class ConcurrentProcessingIterable<Input, Output> implements Iterable<Output>, Closeable {

    private final int blockSize;
    private final int threadPoolSize; 

    private final Processor<Input, Output> processor;
    private final Iterable<Input> input;
    private final ExecutorService executorService;
    private final LinkedBlockingQueue<List<Input>> scheduledWork;
    private final LinkedBlockingQueue<List<Output>> completedWork;
    private final AtomicBoolean doneProducing = new AtomicBoolean(false);

    /**
     * Create a new iterable.
     * 
     * @param input
     *            iterable with the input
     * @param processor
     *            {@link Processor} that processes each element in the input
     * @param blockSize
     *            size of the list of elements that is processed by the worker threads. Having a large list means there
     *            is less contention on the queues by the threads.
     * @param threadPoolSize
     *            number of threads used. What is sensible very much depends on the work load of the processor.
     *            Generally you don't want to have more threads than CPU cores + one for the producer thread used
     *            internally to queue stuff for the worker threads. 
     * @param queueCapacity
     */
    public ConcurrentProcessingIterable(Iterable<Input> input, Processor<Input, Output> processor, int blockSize, int threadPoolSize, int queueCapacity) {
        this.input = input;
        this.processor = processor;
        this.blockSize = blockSize;
        this.threadPoolSize = threadPoolSize;
        executorService = Executors.newFixedThreadPool(threadPoolSize, new ThreadFactory() {
            @Override
            public Thread newThread(Runnable r) {
                return new Thread(r, "processor");
            }
        });
        scheduledWork = new LinkedBlockingQueue<>(queueCapacity);
        completedWork = new LinkedBlockingQueue<>(queueCapacity);
    }

    @Override
    public Iterator<Output> iterator() {
        // start the producer thread
        executorService.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    ArrayList<Input> block = new ArrayList<>(blockSize);
                    for (Input i : input) {
                        block.add(i);
                        if (block.size() == blockSize) {
                            scheduledWork.put(block);
                            block = new ArrayList<>(blockSize);
                        }
                    }
                    ;
                    if (block.size() > 0) {
                        scheduledWork.put(block);
                    }
                    doneProducing.set(true);
                } catch (InterruptedException e) {
                    throw new IllegalStateException(e);
                } 
            }
        });
        final CountDownLatch latch = new CountDownLatch(threadPoolSize-1);

        // start consumer threads
        for (int i = 0; i < threadPoolSize - 1; i++) {
            executorService.execute(new Runnable() {
                @Override
                public void run() {
                    List<Input> block;
                    try {
                        while ((block = scheduledWork.poll(100, TimeUnit.MILLISECONDS)) != null || !doneProducing.get()) {
                            if(block != null) {
                                ArrayList<Output> outputBlock = new ArrayList<>(blockSize);
                                for (Input input : block) {
                                    outputBlock.add(processor.process(input));
                                }
                                if(outputBlock.size() >0) {
                                    completedWork.put(outputBlock);
                                }
                            }
                        }
                    } catch (InterruptedException e) {
                        throw new IllegalStateException(e);
                    } finally {
                        latch.countDown();
                    }
                }
            });
        }

        return new Iterator<Output>() {
            Output next = null;
            List<Output> currentBlock = null;
            int blockIndex = 0;

            @Override
            public boolean hasNext() {
                try {
                    if (next != null) {
                        return true;
                    } else if (currentBlock != null && blockIndex < currentBlock.size()) {
                        next = currentBlock.get(blockIndex++);
                        return true;
                    } else if ((currentBlock = completedWork.poll(100,TimeUnit.MILLISECONDS)) != null) {
                        blockIndex = 0;
                        return hasNext();                       
                    } else if(doneProducing.get() && scheduledWork.size() == 0 && completedWork.size() ==0 && latch.getCount()==0) {
                        return false;
                    } else {
                        return hasNext();
                    }
                } catch (InterruptedException e) {
                    throw new IllegalStateException(e);
                }
            }
            
            @Override
            public Output next() {
                if (hasNext()) {
                    Output result = next;
                    next = null;
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

    @Override
    public void close() throws IOException {
        executorService.shutdown();
        try {
            executorService.awaitTermination(1, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            throw new IllegalStateException("executor failed to shut down cleanly within 1 second");
        }
    }
}
