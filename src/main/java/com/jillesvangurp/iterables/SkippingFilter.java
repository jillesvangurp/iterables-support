package com.jillesvangurp.iterables;

/**
 * Simple filter that allows you to skip the first n items using a filtering iterable.
 *
 * @param <T>
 */
@Deprecated // use the static methods in Iterables
public final class SkippingFilter<T> implements Filter<T> {
    long count=0;
    private final long threshold;
    public SkippingFilter(long threshold) {
        this.threshold = threshold;
    }

    @Override
    public boolean passes(T o) {
        return count++>=threshold;
    }
    
    public static <S> Iterable<S> filter(Iterable<S> it, long threshold) {
        return Iterables.filter(it, new SkippingFilter<S>(threshold));
    }
}