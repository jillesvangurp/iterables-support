package com.jillesvangurp.iterables;

import static com.jillesvangurp.iterables.Iterables.count;
import static com.jillesvangurp.iterables.Iterables.page;
import static com.jillesvangurp.iterables.Iterables.toIterable;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import org.testng.annotations.Test;

@Test
public class PagingIterableTest {
    public void shouldPage() {
        Iterable<Integer> it = toIterable((new Integer[] {1,2,3}));
        assertThat(count(page(it, 1)), is(3l));
        assertThat(count(page(it, 2)), is(2l));
        assertThat(count(page(it, 3)), is(1l));
        assertThat(count(page(it, 4)), is(1l));
    }

    @Test(expectedExceptions=IllegalArgumentException.class)
    public void shouldNotAllow0PageSize() {
        Iterable<Integer> it = toIterable((new Integer[] {1,2,3}));
        page(it, 0);
    }
}
