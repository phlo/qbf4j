package at.jku.fmv.qbf.benchmark;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;

@BenchmarkMode(Mode.SingleShotTime)
@Warmup(iterations = 0)
@Measurement(iterations = 1)
@Fork(value = 5)
@OutputTimeUnit(TimeUnit.SECONDS)
public class QDIMACS extends Benchmarks {

	@Benchmark
	public void read(Variables v, Blackhole hole) throws IOException {
		hole.consume(at.jku.fmv.qbf.io.QDIMACS.read(v.file));
	}

	@Benchmark
	public void write(Variables v) throws IOException {
		at.jku.fmv.qbf.io.QDIMACS
			.write(v.formula, createTempFile(v.instance, ".qdimacs"));
	}
}
