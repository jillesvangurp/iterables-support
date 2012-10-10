package com.jillesvangurp.iterables;

import java.io.BufferedReader;
import java.io.Closeable;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.Charset;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.zip.GZIPInputStream;

public class LineIterable implements Iterable<String>, Closeable{
    private final BufferedReader bufferedReader;

    public LineIterable(Reader r) {
        bufferedReader = new BufferedReader(r);
    }
    
    public static LineIterable openGzipFile(String fileName) throws IOException {
        return new LineIterable(new InputStreamReader(new GZIPInputStream(new FileInputStream(fileName)), Charset.forName("UTF-8")));
    }

    @Override
    public Iterator<String> iterator() {
        return new Iterator<String>() {
            boolean hasMoreLines=true;
            String nextLine=null;

            @Override
            public boolean hasNext() {
                if(hasMoreLines && nextLine==null) {
                    nextLine = readNextLine();
                }
                return hasMoreLines && nextLine!=null;
            }

            @Override
            public String next() {
                if(!hasNext()) {
                    throw new NoSuchElementException();
                } else {
                    String result=nextLine;
                    nextLine=null;
                    return result;
                }
            }

            private String readNextLine() {
                String line = null;
                if(hasMoreLines) {
                    try {
                        line = bufferedReader.readLine();
                    } catch (IOException e) {
                        hasMoreLines=false;
                        throw new IllegalStateException("could not read line", e);
                    }
                    if(line==null) {
                        hasMoreLines=false;
                    }
                }
                return line;
            }

            @Override
            public void remove() {
                throw new UnsupportedOperationException("cannot remove lines");
            }
        };
    }

    @Override
    public void close() throws IOException {
        bufferedReader.close();
    }
}
