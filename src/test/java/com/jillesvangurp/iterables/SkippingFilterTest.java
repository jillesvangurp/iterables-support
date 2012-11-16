package com.jillesvangurp.iterables;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import java.util.Arrays;

import org.testng.annotations.Test;

@Test
public class SkippingFilterTest {
    @SuppressWarnings("deprecation")
	public void shouldSkipSpecifiedNumberOfItems() {
        Iterable<Integer> list = Arrays.asList(1,2,3,4,5,6);
        int c=0;
        for(@SuppressWarnings("unused") int i: SkippingFilter.filter(list, 3)) {
            c++;
        }
        assertThat(c, is(3));
    }
}
