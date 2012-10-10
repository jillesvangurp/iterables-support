package com.jillesvangurp.iterables;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;

import org.testng.annotations.Test;

public class LineIterableTest {

    String input = "line1\nline2\nline3\n\nline5\nline6";

    @Test
    public void shouldIterateLines() throws Exception {
        ByteArrayInputStream bis = new ByteArrayInputStream(input.getBytes());
        try(Reader r = new InputStreamReader(bis)) {
            int count=0;
            for(String line: new LineIterable(r)) {
                assertThat(line, notNullValue());
                count++;
            }
            assertThat(count,equalTo(6));
        } catch(IllegalStateException | IOException e) {
            throw e;
        }
    }
}
