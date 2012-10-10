package com.jillesvangurp.iterables;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.List;

import org.testng.annotations.Test;

public class CSVLineIterableTest {
    String input = "f1\tf2\tf3\nf1\t\tf3\nf1\tf2\t f3 \n\t\t\n";

    @Test
    public void shouldIterate() throws Exception {
        ByteArrayInputStream bis = new ByteArrayInputStream(input.getBytes());
        try(Reader r = new InputStreamReader(bis)) {
            int count=0;
            int numFields=0;
            for(List<String> fields: new CSVLineIterable(new LineIterable(r),'\t')) {
                count++;
                numFields+=fields.size();
            }
            assertThat(count,equalTo(4));
            assertThat(numFields,equalTo(count*3));
        } catch(IllegalStateException | IOException e) {
            throw e;
        }
    }
}
