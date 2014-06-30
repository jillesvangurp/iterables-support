package com.jillesvangurp.iterables;

/**
 * Intended for use with {@link ConcurrentProcessingIterable}.
 *
 * @param <Input> input
 * @param <Output> output
 */
public interface Processor<Input,Output> {
    /**
     * Transform input into output.
     * @param input the input
     * @return a value of type Output
     */
    Output process(Input input);
}
