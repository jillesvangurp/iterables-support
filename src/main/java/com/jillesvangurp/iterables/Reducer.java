package com.jillesvangurp.iterables;

public interface Reducer<T> {
    public T reduce(T input);
    public T reduce(T cumulative, T input);
}