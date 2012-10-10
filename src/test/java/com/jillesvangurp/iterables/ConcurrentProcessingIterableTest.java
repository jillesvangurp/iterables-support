package com.jillesvangurp.iterables;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class ConcurrentProcessingIterableTest {
    @DataProvider
    private Object[][] sizes() {
        return new Integer[][] {{0}, {10}, {999}, {1001}, {10001},{66666},{10000001}};
    }
    
    @Test(dataProvider="sizes")
    public void shouldProcessAllInputAndThenCompleteNormally(int totalInput) {
        List<Integer> input = new ArrayList<>(totalInput);
        for(int i=0; i< totalInput;i++) {
            input.add(i);
        }
        
        try(ConcurrentProcessingIterable<Integer, Integer> cpi = new ConcurrentProcessingIterable<>(input, new Processor<Integer, Integer>() {

            @Override
            public Integer process(Integer input) {
                return 42;
            }
        })){    
            int count=0;
            for(@SuppressWarnings("unused") Integer o:cpi) {
                count++;
            }
            assertThat(count, is(totalInput));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
