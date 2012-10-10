package com.jillesvangurp.iterables;


public interface Filter <T> {
    boolean passes(T o);
}
