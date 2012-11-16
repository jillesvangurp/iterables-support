package com.jillesvangurp.iterables;

/**
 * Poison pill to make {@link FilteringIterable}'s hasNext return false rather than just
 * merely skipping the entry. Throw from any {@link Filter} to trigger the iterable to stop iterating.
 */
public class PermanentlyFailToPassException extends RuntimeException {
    private static final long serialVersionUID = 1635283150201993963L;

}
