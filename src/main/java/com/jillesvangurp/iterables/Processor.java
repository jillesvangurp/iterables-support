package com.jillesvangurp.iterables;

public interface Processor<Input,Output> {
    Output process(Input input);
}
