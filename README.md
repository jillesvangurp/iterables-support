# Introduction

Iterables support builds on language features in Java 7 to provide some features that those familiar with more powerful scripting languages such as javascript or ruby would be familiar with. This reduces the amount of boiler plate that needs to be written for doing things like iterating over lines in a file, doing map reduce style processing, and using Java's concurrency features to do this fast.

When processing large amounts of data in files, you often can't afford to load all of it in memory. Iterating avoids doing that and having some simple ways of applying map reduce functions to the iterator allows you to do processing.

Programming this in Java from scratch results in large amounts of boiler plate code. Iterables-support vastly reduces this boiler plate code. It's still Java of course so some amount of it is unavoidable but I've used this library successfully to do fairly complicated things with huge amounts of open streetmap, wikipedia and other data.

# Get it from Maven Central

```
<dependency>
    <groupId>com.jillesvangurp</groupId>
    <artifactId>iterables-support</artifactId>
    <version>1.7</version>
</dependency>
```

Note. check for the latest version. I do not always update the readme.

# Design

Iterables support makes heavy use of several java language features:
* anything implementing *Iterable<T>* can be used with a *foreach* type for loop.
* anything implementing *Closeable* cleans up after itself. Together with Java's try with resources this means no more finally blocks are needed for this and much less boiler plate code is needed. 
* *inner classes* are the closest thing Java has to closures and you can do some useful stuff with them. 

# Overview

## Iterating over content in files or streams.

### LineIterable

Allows you to iterate over the lines in a file without having to juggle a lot of streams, readers, etc. Supports try with resources so you don't have to worry about dangling file handles. Supports both plain text and gzipped files. 

```
try(LineIterable it = LineIterable.openGzipFile(fileName)) {
  for(String line: it) {
    ..
  }
} catch(IOException e) {
  ...
}
```

### CSVLineIterable

Similar to LineIterable but parses the line into a list of fields using a configurable delimiter. So you can iterate over lists of fields.

### MergingCSVIterable

If you have two csv files sorted on a particular column with an id, this class can perform a join. Useful if you want to e.g. process Geonames data and merge translations with poi data.

### BlobIterable

Sometimes what you want to iterate over in a file can span multiple lines. For example xml files commonly have xml fragments that span many lines. E.g. openstreet map nodes have coordinates and properties and each node can span several lines. With this iterable, you can foreach over such content easily. I've used it with open streetmap, wikipedia and several other datasources.

## Functional programming

Java is somewhat limited when it comes to functional progamming. However, using iterables and inner classes, you can actually make it do some useful things. 


### Iterables.map

`public static <I,O> Iterable<O> map(Iterable<I> it, Processor<I,O> processor)`

Allows you to iterate over the processed input.

### Iterables.reduce

`public static <T> T reduce(Iterable<T> it, Reducer<T> reducer)`

Allows you to reduce the iterable. 

### Iterables.compose

`<I,S,O> Processor<I,O> compose(final Processor<I,S> first, final Processor<S,O> last, final Processor<O,O>...extraSteps)`

Compose two or more processors into one processor that you can use with the map function.

### Concurrency: ConcurrentProcessingIterable

The map and reduce functions above are single threaded. Sometimes, it is nice to use multiple threads and process things a bit faster if you have a nice multi core CPU. ConcurrentProcessingIterable allows you to do just that.

`ConcurrentProcessingIterable` implements the producer consumer pattern and uses a queue to provide data from the producer thread to the consumer threads that each apply the processor to their input. The result is of course iterable, so you can concurrently process input simply by iterating over the output. Uses try with resources so you don't have to worry about creating and destroying threads and executors, etc. 

Extremely easy to use with these two functions:

- `Iterables.processConcurrently(..)`
- `Iterables.mapReduce(..)`
 
```
        // some large input
        List<Integer> it = new ArrayList<>();
        for(int i=0; i< 1000000;i++) {
            input.add(i);
        }
        
        // a trivial processor
        Processor<Integer, Integer> doubler new Processor<Integer, Integer>() {

            @Override
            public Integer process(Integer input) {
                return 2*input;
            }
        }
        
        // now lets concurrently process this with 50 threads each processing chunks of 100 in each thread using a buffer of 10000 elements
        try(ConcurrentProcessingIterable<Integer, Integer> cpi = Iterables.processConcurrently(it, doubler, 50, 100, 10000)){    
            int count=0;
            for(Integer i:cpi) {
                System.out.println("" + i);
            }
            assertThat(count, is(totalInput));
        } catch (IOException e) {
            e.printStackTrace();
        }
        
        // or lets use map reduce to sum up the numbers concurrently
        int total = Iterables.mapReduce(it, processor, Reducers.sum(), 50, 300, 10000)
        
```

# Building from source

It's a maven project. So, checking it out and doing a mvn clean install should do the trick.

Alternatively, you can exercise your rights under the license and simply copy and adapt as needed. The [license](https://github.com/jillesvangurp/iterables-support/blob/master/LICENSE) allows you to do this and I have no problems with this although I do appreciate attribution.

Should anyone like this licensed differently, please contact me.

If anyone wants to fix stuff just send me a pull request.

# License

Like all my other projects, this project is licensed under the so-called MIT license. 

For more details see the LICENSE file

# Future

Obviously, Java 8 is around the corner with lambda support and many other goodies that will make some things in this library redundant. I will most likely switch to using Java 8 in the next year or so and will probably update this library at that point.

# Changelog
* 1.8
    * Add support for reduce function to complement the already implemented map function
    * Concurrent map reduce.
    * fix toIterable so that it is able to iterate more than once over the same array
* 1.7
    * Now in maven central
* 1.6
    * Make consume method static so you can actually use it ...
* 1.5
    * Add convenience methods for quickly consuming an iterable or iterator.
* 1.4
    * Update dependencies
* 1.3 
    * Add convenience method to create peeking iterator from an iterable and to create an iterable from any array
* 1.2
    * Add castingIterable method to Iterables. Very useful to get rid of stupid casts in loops.
* 1.1
    * Add compose method to Iterables for composing multiple Iterables
    * Add counts method to Iterables for counting the number of elements (useful for testing)
* 1.0
    * First release
