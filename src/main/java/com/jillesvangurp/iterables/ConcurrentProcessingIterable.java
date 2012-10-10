package com.jillesvangurp.iterables;

import java.io.Closeable;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

public class ConcurrentProcessingIterable<Input, Output> implements Iterable<Output>, Closeable {

    private static final int BLOCK_SIZE = 1000;
    private static final int THREAD_POOL_SIZE = 4; // 4 cpu cores + 1 producer thread that doesn't do much
    private static final int QUEUE_CAPACITY = 10;

    private final Processor<Input, Output> processor;
    private final Iterable<Input> input;
    private final ExecutorService executorService;
    private final LinkedBlockingQueue<List<Input>> scheduledWork = new LinkedBlockingQueue<>(QUEUE_CAPACITY);
    private final LinkedBlockingQueue<List<Output>> completedWork = new LinkedBlockingQueue<>(QUEUE_CAPACITY);
    private final AtomicBoolean done = new AtomicBoolean(false);

    public ConcurrentProcessingIterable(Iterable<Input> input, Processor<Input, Output> processor) {
        this.input = input;
        this.processor = processor;
        executorService = Executors.newFixedThreadPool(THREAD_POOL_SIZE, new ThreadFactory() {
            @Override
            public Thread newThread(Runnable r) {
                return new Thread(r, "processor");
            }
        });
    }

    @Override
    public Iterator<Output> iterator() {
        // start the producer thread
        executorService.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    ArrayList<Input> block = new ArrayList<>(BLOCK_SIZE);
                    for (Input i : input) {
                        block.add(i);
                        if (block.size() == 1000) {
                            scheduledWork.put(block);
                            block = new ArrayList<>(1000);
                        }
                    }
                    ;
                    if (block.size() > 0) {
                        scheduledWork.put(block);
                    }
                    done.set(true);
                } catch (InterruptedException e) {
                    throw new IllegalStateException(e);
                }
            }
        });

        // start consumer threads
        for (int i = 0; i < THREAD_POOL_SIZE - 1; i++) {
            executorService.execute(new Runnable() {
                @Override
                public void run() {
                    List<Input> block;
                    try {
                        while ((block = scheduledWork.poll(100, TimeUnit.MILLISECONDS)) != null || !done.get()) {
                            if(block != null) {
                                ArrayList<Output> outputBlock = new ArrayList<>(BLOCK_SIZE);
                                for (Input input : block) {
                                    outputBlock.add(processor.process(input));
                                }
                                completedWork.put(outputBlock);
                            }
                        }
                    } catch (InterruptedException e) {
                        throw new IllegalStateException(e);
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
                    } else if ((currentBlock = completedWork.poll(100,TimeUnit.MILLISECONDS)) != null || !done.get()) {
                        if(currentBlock != null) {
                            blockIndex = 0;
                            return hasNext();
                        } else  if (done.get()){
                            return false;
                        } else {
                            // if completedWork has no results yet, poll returns immediately with null and you get a stack overflow
                            // so wait
                            Thread.sleep(100);
                            return hasNext();
                        }
                    } else {
                        return false;
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
        executorService.shutdownNow();
        try {
            executorService.awaitTermination(1, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            throw new IllegalStateException("executor failed to shut down cleanly within 1 second");
        }
    }
}
