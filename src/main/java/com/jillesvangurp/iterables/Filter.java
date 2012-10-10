package com.jillesvangurp.iterables;


/**
 * Intended to be used together with the {@link FilteringIterable}.
 *
 * @param <T>
 */
public interface Filter <T> {
    boolean passes(T o);
}
