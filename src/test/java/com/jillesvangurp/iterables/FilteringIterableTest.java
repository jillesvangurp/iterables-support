package com.jillesvangurp.iterables;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import java.util.Arrays;

import org.testng.annotations.Test;

@Test
public class FilteringIterableTest {

    public void shouldFilter() {
        int count=0;
        for(@SuppressWarnings("unused") Integer i: FilteringIterable.filter(Arrays.asList(1,2,3,4,5,6,7,8), new Filter<Integer>() {
            @Override
            public boolean passes(Integer o) {
                return o % 2 == 0;
            }})) {
            count++;
        }

        assertThat(count, is(4));
    }
}
