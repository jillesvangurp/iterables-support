package com.jillesvangurp.iterables;

/**
 * Intended for use with {@link ConcurrentProcessingIterable}.
 *
 * @param <Input>
 * @param <Output>
 */
public interface Processor<Input,Output> {
    Output process(Input input);
}
