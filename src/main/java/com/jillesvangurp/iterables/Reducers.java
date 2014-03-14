package com.jillesvangurp.iterables;

import java.math.BigDecimal;

public class Reducers {

    private static final class AddingReducer<T extends Number> implements Reducer<T> {
        @Override
        public T reduce(T input) {
            return input;
        }

        @SuppressWarnings("unchecked")
        @Override
        public T reduce(T cumulative, T input) {
            Class<? extends Number> clazz = cumulative.getClass();
            if(clazz.isAssignableFrom(Integer.class)) {
                Integer result = (Integer)input + (Integer) cumulative;
                return (T)result;
            } else if(clazz.isAssignableFrom(Long.class)) {
                Long result = (Long)input + (Long) cumulative;
                return (T)result;
            } else if(clazz.isAssignableFrom(Double.class)){
                Double result = (Double)input + (Double) cumulative;
                return (T)result;
            } if(clazz.isAssignableFrom(Float.class)) {
                Float result = (Float)input + (Float) cumulative;
                return (T)result;
            } else if(clazz.isAssignableFrom(BigDecimal.class)) {
                BigDecimal result = ((BigDecimal)input).add((BigDecimal) cumulative);
                return (T)result;
            } else {
                throw new IllegalArgumentException("unsupported type " + clazz.getName());
            }
        }
    }

    public static <T extends Number> Reducer<T> sum(Class<T> clazz) {
        return new AddingReducer<>();
    }

}
