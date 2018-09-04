# [qbf4j](https://github.com/phlo/qbf4j)

[QBF](https://phlo.github.io/qbf4j/at/jku/fmv/qbf/QBF.html) is an immutable data structure, representing quantified boolean formula trees containing the following connectives:
* `¬` - negation
* `∧` - conjunction
* `∨` - disjunction
* `∀` - universal quantification
* `∃` - existential quantification

## Features

* Java 8 [Stream](https://docs.oracle.com/javase/8/docs/api/java/util/stream/Stream.html) support
* structural pattern matching using [QBF::accept](https://phlo.github.io/qbf4j/at/jku/fmv/qbf/QBF.html#accept-java.util.function.Consumer-java.util.function.Consumer-java.util.function.Consumer-java.util.function.Consumer-java.util.function.Consumer-java.util.function.Consumer-java.util.function.Consumer-java.util.function.Consumer-) and [QBF::apply](https://phlo.github.io/qbf4j/at/jku/fmv/qbf/QBF.html#apply-java.util.function.Function-java.util.function.Function-java.util.function.Function-java.util.function.Function-java.util.function.Function-java.util.function.Function-java.util.function.Function-java.util.function.Function-) (Church encoding)
* reading/writing of [QCIR-G14](http://qbf.satisfiability.org/gallery/qcir-gallery14.pdf) and [QDIAMCS](http://www.qbflib.org/qdimacs.html) files
* transformation into
  * [cleansed form](https://phlo.github.io/qbf4j/at/jku/fmv/qbf/QBF.html#cleanse--)
  * [negation normal form](https://phlo.github.io/qbf4j/at/jku/fmv/qbf/QBF.html#toNNF--)
  * [prenex normal form](https://phlo.github.io/qbf4j/at/jku/fmv/qbf/pnf/PrenexingStrategy.html#apply-at.jku.fmv.qbf.QBF-)
  * [conjunctive normal form](https://phlo.github.io/qbf4j/at/jku/fmv/qbf/pcnf/CNFEncoder.html#encode-at.jku.fmv.qbf.QBF-)

## Prerequisites

* Ant >= 1.9
* Java >= 1.8

## Installation

Build from source using `ant dist` and include `dist/qbf4j-VERSION.jar` in the classpath.

## Executables

### qcir2pnf

Converts a given [QCIR-G14](http://qbf.satisfiability.org/gallery/qcir-gallery14.pdf) formula into [prenex normal form](https://en.wikipedia.org/wiki/Prenex_normal_form).

```
> $ java -jar dist/qcir2pnf.jar

Usage: qcir2pnf [OPTION]... <input-file> <output-file>

  -s <class>, --strategy=<class>  prenexing strategy to apply, where <class>
                                  is the fully qualified name of a class
                                  implementing the PrenexingStrategy interface,
                                  e.g.:

                                  at.jku.fmv.qbf.pnf.ExistsDownForAllUp
                                  at.jku.fmv.qbf.pnf.ExistsUpForAllDown
                                  at.jku.fmv.qbf.pnf.ForAllDownExistsDown
                                  at.jku.fmv.qbf.pnf.ForAllDownExistsUp
                                  at.jku.fmv.qbf.pnf.ForAllUpExistsDown
                                  at.jku.fmv.qbf.pnf.ForAllUpExistsUp (default)

  -c [<class>], --cnf[=<class>]   transform to PCNF, where <class> is the fully
                                  qualified name of a class implementing the
                                  CNFEncoder interface, e.g.:

                                  at.jku.fmv.qbf.pcnf.PG86 (default)

  --cleanse                       cleanse formula

  --qdimacs                       output formula in QDIMACS format
```

## Benchmarks

[qbf4j](https://github.com/phlo/qbf4j) contains benchmarks for critical functions using [JMH](http://openjdk.java.net/projects/code-tools/jmh).

After building the benchmarks using `ant benchmark`, see `java -jar dist/qbf4j-VERSION-benchmark.jar -h` for available command line options.

Listing all available benchmarks is done using:
```
java -jar dist/qbf4j-VERSION-benchmark.jar -l
```

For running a preconfigured version of a specific benchmark, use:
```
java -cp dist/qbf4j-VERSION-benchmark.jar <class>.<method>
```

To show an overview of a benchmark's result in markdown format, a simple python script is included at:
```
src/benchmark/python/printBenchmark.py
```

### Tree Size

To measure the formula tree size of the given test set run:
```
java -cp dist/qbf4j-VERSION-benchmark.jar -javaagent:dist/qbf4j-VERSION-QBFSizeAgent.jar QBF.MemoryConsumption
```

### Test Sets

Any `*.qcir` or `*.qdimacs` file can be used for running benchmarks.

The following bash scripts may be used to download and extract a test set:
* [qcir non-prenex test set](https://phlo.github.io/qbf4j/files/dl-qcir-non-prenex.sh)  ([archive](https://phlo.github.io/qbf4j/files/qcir-non-prenex.tar.xz)) *default*
* [qbfeval'17 test set](https://phlo.github.io/qbf4j/files/dl-qbfeval17.sh) ([archive](http://www.qbflib.org/eval17.zip))

### Configuration

A properties file for customizing certain benchmark parameters can be found at:
```
src/benchmark/resources/benchmark.properties
```

After changing the properties file, either run `ant benchmark` to rebuild the `jar` file or include it in the classpath when running a benchmark.
