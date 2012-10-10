package com.jillesvangurp.iterables;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;

import java.util.Arrays;
import java.util.NoSuchElementException;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

@Test
public class PeekableIteratorTest {

    private PeekableIterator<String> peekableIterator;

    @BeforeMethod
    public void before() {
        peekableIterator = new PeekableIterator<>(Arrays.asList("foo", "bar").iterator());
    }

    public void shouldPeek() {
        assertThat(peekableIterator.peek(), equalTo("foo"));
        assertThat(peekableIterator.peek(), equalTo("foo"));
        assertThat(peekableIterator.next(), equalTo("foo"));
        assertThat(peekableIterator.peek(), equalTo("bar"));
        assertThat(peekableIterator.peek(), equalTo("bar"));
        assertThat(peekableIterator.next(), equalTo("bar"));
    }

    public void shouldSupportWhile() {
        int count = 0;
        while (peekableIterator.hasNext()) {
            String string = peekableIterator.next();
            assertThat(string, notNullValue());
            count++;
        }
        assertThat(count, equalTo(2));
    }

    @Test(expectedExceptions=NoSuchElementException.class)
    public void shouldThrowNoSuchElementException() {
        assertThat(peekableIterator.next(), equalTo("foo"));
        assertThat(peekableIterator.next(), equalTo("bar"));
        peekableIterator.peek();
    }
}
