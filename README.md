# Introduction

Iterables support is a small collection of utility classes that I developed while processing a lot of content in files, which inevitably involves a lot of iterating and a lot of boiler plate code related to that. Basically following the DRY principle, I started collecting some idioms and patterns and ended up with this convenient library. 

If your Java code involves iterating over stuff (and how could it not), you probably want to take a look at this. *Using IterablesSupport will result in vastly reduced amounts of boiler plate code in Java*. Almost to the point where the whole 'OMG Java is so verbose' argument that fans of certain other languages use largely goes away.

Note. this class partially overlaps with Google Guava's Iterables class. However, much more is provided here.

# Highlights

* Concurrently *map reduce* any iterable. Use as many threads as you like. Chunk your input iterable to keep your threads busy. Iterate over the aggregated output.
* Use composable Processor instances for concurrently mapping something to an iterable
* Compose iterables to loop over multiple iterable objects in one loop.
* Filter iterables, or look ahead using the PeekableIterable.
* Iterators for Gzip files, CSV files, Multiline xml files, and more

# Features and design

Iterables support makes heavy use of several java language features:
* anything implementing *Iterable<T>* can be used with a *foreach* type for loop.
* anything implementing *Closeable* cleans up after itself. Together with Java's try with resources this means no more finally blocks are needed for this and much less boiler plate code is needed. 
* *inner classes* are the closest thing Java has to closures and you can do some useful stuff with them. 

This framework represents inputs as iterables that one can iterate over using a foreach loop. Several iterables are provided that can be combined to do useful things.

* Processing files.
    * *LineIterable*. The most basic iterable is the LineIterable, which takes a stream and iterates over the lines as strings. It basically makes dealing with BufferedReaders a lot less tedious.
    * *CSVLineIterable*. If you have a simple csv file, you can use the line iterable to construct a CSVLineIterable and iterate over the list of fields.
    * *MergingCSVIterable*. Say you have two csv files that have a 1 to many relation and you want the join of those two files. Provided the files are sorted correctly, you can merge using an id. MergingCSVIterable does this. I've used it for processing millions of GeoNames and their translations (in a separate file).  
    * *BlobIterable*. If you have a big xml file with tags inside that you want to process that span multiple lines, use the BlobIterable and process each blob separately. Simply configure it with the begin and end tag and process each blob one by one.
* General purpose iterables
    * *ConcurrentProcessingIterable*. Often processing is expensive and you want to process things concurrently. This can be tricky to get right. So, use the ConcurrentProcessingIterable and configure it with the input iterable and a processor. It will do the rest for you and you simply iterate over the processed output.
    * *FilteringIterable*. Sometimes you want to filter what you iterate over: FilteringIterable does that. Simply implement the Filter interface and pass an instance to the FilteringIterable and you've got filtering.
    * *PeekableIterable*. Sometimes, you want to see what's next before it's coming. PeekableIterable allows a convenient peek() method that behaves just like next but without moving the iterator forward. 
* *Iterables* utility class. The Iterables class contains static methods to provide some useful processing and filtering primitives*. You may want to use static imports and add this class to your eclipse Favorites.

# Get it from Maven Central

```
<dependency>
    <groupId>com.jillesvangurp</groupId>
    <artifactId>iterables-support</artifactId>
    <version>1.7</version>
</dependency>
```

Note. check for the latest version. I do not always update the readme.

# Building from source

It's a maven project. So, checking it out and doing a mvn clean install should do the trick.

Alternatively, you can exercise your rights under the license and simply copy and adapt as needed. The "license":https://github.com/jillesvangurp/xmltools/blob/master/LICENSE allows you to do this and I have no problems with this although I do appreciate attribution.

Should anyone like this licensed differently, please contact me.

If anyone wants to fix stuff just send me a pull request.

# License

Like all my other projects, this project is licensed under the so-called MIT license. 

For more details see the LICENSE file

# Changelog

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