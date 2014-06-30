package com.jillesvangurp.iterables;


/**
 * Intended to be used together with the {@link FilteringIterable}.
 *
 * @param <T> the type that needs to be filtered
 */
public interface Filter <T> {
    boolean passes(T o);
}
