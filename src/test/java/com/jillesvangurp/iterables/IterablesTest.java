package com.jillesvangurp.iterables;

import static com.jillesvangurp.iterables.Iterables.count;
import static com.jillesvangurp.iterables.Iterables.mapReduce;
import static com.jillesvangurp.iterables.Iterables.reduce;
import static com.jillesvangurp.iterables.Iterables.toIterable;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import org.testng.annotations.Test;

@Test
public class IterablesTest {
    public void shouldCount() {
        assertThat(Iterables.count(Arrays.asList(1,2,3)), is(3l));
    }

    public void shouldComposeIterable() {
        Iterable<Integer> l1 = Arrays.asList(1,2,3,4);
        Iterable<Integer> l2 = Arrays.asList(5,6,7);
        Iterable<Iterable<Integer>> iterables = Arrays.asList(l1,l2);
        assertThat(Iterables.count(Iterables.compose(iterables)), is(7l));
    }

    public void shouldComposeIterableAndAllowEmptyIterables() {
        Iterable<Integer> l1 = new ArrayList<Integer>();
        Iterable<Integer> l2 = Arrays.asList(5,6,7);
        Iterable<Iterable<Integer>> iterables = Arrays.asList(l1,l2);
        assertThat(Iterables.count(Iterables.compose(iterables)), is(3l));
    }

    public void shouldComposeIterableAndAllowNullIterables() {
        Iterable<Integer> l1 = new ArrayList<Integer>();
        Iterable<Integer> l2 = Arrays.asList(5,6,7);
        Iterable<Iterable<Integer>> iterables = Arrays.asList(l1,l2,null);
        assertThat(Iterables.count(Iterables.compose(iterables)), is(3l));
    }

    public void shouldCastElements() {
        List<Collection<?>> list = new ArrayList<>();
        list.add(new ArrayList<>());
        list.add(new LinkedList<>());
        int i=0;
        for(List<?> l : Iterables.castingIterable(list, List.class)) {
            i++;
            assertThat("should be a list", l instanceof List);
        }
        assertThat(i, is(2));
    }

    public void shouldIterateOverArrayIterable() {
        assertThat(count(toIterable(new Integer[] {1,2,3})), is(3l));
    }

    public void shouldIterateMultipletimesOverSameIterable() {
        Iterable<Integer> it = toIterable(new Integer[] {1,2,3});
        assertThat(count(it), is(3l));
        assertThat(count(it), is(3l));
    }

    public void shouldReduceInts() {
        int total = reduce(toIterable(new Integer[] {1,2,3}), Reducers.sum(Integer.class));
        assertThat(total, is(6));
    }

    public void shouldReduceLongs() {
        Long total = reduce(toIterable(new Long[] {1l,2l,3l}), Reducers.sum(Long.class));
        assertThat(total, is(6l));
    }

    public void shouldMapReduceConcurrently() {
        ArrayList<Integer> l = new ArrayList<Integer>();
        for(int i = 0; i< 666; i++) {
            l.add(666);
        }

        Processor<Integer,Integer> identityProcessor = new Processor<Integer,Integer>() {

            @Override
            public Integer process(Integer input) {
                return input;
            }};
        Integer reduced = mapReduce(l, identityProcessor, Reducers.sum(Integer.class), 5, 10, 100);
        assertThat(reduced, is(666*l.size()));
    }
}
