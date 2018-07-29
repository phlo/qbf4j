package at.jku.fmv.qbf.benchmark;

import java.util.HashMap;
import java.util.concurrent.TimeUnit;

import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;

import at.jku.fmv.qbf.QBF.Traverse;
import at.jku.fmv.qbf.pcnf.PG86;
import at.jku.fmv.qbf.pnf.ForAllUpExistsUp;

@BenchmarkMode(Mode.SingleShotTime)
@Warmup(iterations = 3)
@Measurement(iterations = 5)
@Fork(value = 1)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
public class QBF extends Benchmarks {

	@Benchmark
	public void stream(Variables v, Blackhole hole) {
		v.formula.stream(Traverse.PostOrder)
			.forEach(o -> hole.consume(o));
	}

	@Benchmark
	public void streamParallel(Variables v, Blackhole hole) {
		v.formula.stream(Traverse.PostOrder)
			.parallel()
			.forEach(o -> hole.consume(o));
	}

	@Benchmark
	public void streamVariables(Variables v, Blackhole hole) {
		v.formula.streamVariables()
			.forEach(o -> hole.consume(o));
	}

	@Benchmark
	public void streamVariablesParallel(Variables v, Blackhole hole) {
		v.formula.streamVariables()
			.unordered()
			.parallel()
			.forEach(o -> hole.consume(o));
	}

	@Benchmark
	public void streamBoundVariables(Variables v, Blackhole hole) {
		v.formula.streamBoundVariables()
			.forEach(o -> hole.consume(o));
	}

	@Benchmark
	public void streamBoundVariablesParallel(Variables v, Blackhole hole) {
		v.formula.streamBoundVariables()
			.unordered()
			.parallel()
			.forEach(o -> hole.consume(o));
	}

	@Benchmark
	public void streamFreeVariables(Variables v, Blackhole hole) {
		v.formula.streamBoundVariables()
			.forEach(o -> hole.consume(o));
	}

	@Benchmark
	public void streamFreeVariablesParallel(Variables v, Blackhole hole) {
		v.formula.streamFreeVariables()
			.parallel()
			.forEach(o -> hole.consume(o));
	}

	@Benchmark
	public void streamQPaths(Variables v, Blackhole hole) {
		v.formula.streamQPaths()
			.forEach(o -> hole.consume(o));
	}

	@Benchmark
	public void streamQPathsParallel(Variables v, Blackhole hole) {
		v.formula.streamQPaths()
			.parallel()
			.forEach(o -> hole.consume(o));
	}

	@Benchmark
	public void streamQPathsParallelUnordered(Variables v, Blackhole hole) {
		v.formula.streamQPaths()
			.unordered()
			.parallel()
			.forEach(o -> hole.consume(o));
	}

	@Benchmark
	public void getCriticalPaths(Variables v, Blackhole hole) {
		hole.consume(
			at.jku.fmv.qbf.QBF
				.getCriticalPaths(
					v.formula.getQPaths()));
	}

	@Benchmark
	public void getSkeleton(Variables v, Blackhole hole) {
		hole.consume(v.formula.getSkeleton());
	}

	@Benchmark
	public void unifyPrefix(Variables v, Blackhole hole) {
		hole.consume(v.formula.unifyPrefix());
	}

	@Benchmark
	public void rename(Variables v, Blackhole hole) {
		hole.consume(v.formula.rename(new HashMap<>()));
	}

	@Benchmark
	public void cleanse(Variables v, Blackhole hole) {
		hole.consume(v.formula.cleanse());
	}

	@Benchmark
	public void toNNF(Variables v, Blackhole hole) {
		hole.consume(v.formula.toNNF());
	}

	@Benchmark
	public void toPNF(Variables v, Blackhole hole) {
		hole.consume(v.formula.toPNF(new ForAllUpExistsUp()));
	}

	@Benchmark
	public void toPCNF(Variables v, Blackhole hole) {
		hole.consume(v.formula.toPCNF(new ForAllUpExistsUp(), new PG86()));
	}

	@Benchmark
	public void toString(Variables v, Blackhole hole) {
		hole.consume(v.formula.toString());
	}
}
