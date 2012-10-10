package com.jillesvangurp.iterables;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

import java.io.ByteArrayInputStream;
import java.io.InputStreamReader;
import java.util.Iterator;
import java.util.List;

import org.testng.annotations.Test;

@Test
public class MergingCsvIterableTest {
    String primary = "1\tfoo\n2\tbar";
    String secondary = "x\t0\tfoo\ny\t2\tbar\nz\t2\tfoobar\na\t3\txxxx";

    public void shouldMergeRecords() {
        LineIterable primaryLineIterable = new LineIterable(new InputStreamReader(new ByteArrayInputStream(primary.getBytes())));
        CSVLineIterable primaryCsvIterable = new CSVLineIterable(primaryLineIterable, '\t');
        LineIterable secondaryLineIterable = new LineIterable(new InputStreamReader(new ByteArrayInputStream(secondary.getBytes())));
        CSVLineIterable secondaryCsvIterable = new CSVLineIterable(secondaryLineIterable, '\t');

        MergingCsvIterable mergingCsvIterable = new MergingCsvIterable(primaryCsvIterable, secondaryCsvIterable, 0, 1);
        Iterator<List<List<String>>> iterator = mergingCsvIterable.iterator();
        assertThat(iterator.next().size(), equalTo(1));
        assertThat(iterator.next().size(), equalTo(3));
    }
}
