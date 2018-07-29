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
public class QCIR extends Benchmarks {

	@Benchmark
	public void read(Variables v, Blackhole hole) throws IOException {
		hole.consume(at.jku.fmv.qbf.io.QCIR.read(v.file));
	}

	@Benchmark
	public void write(Variables v) throws IOException {
		at.jku.fmv.qbf.io.QCIR
			.write(
				v.formula,
				createTempFile(v.instance, ".qcir"),
				true);
	}

	@Benchmark
	public void writeParallel(Variables v) throws IOException {
		at.jku.fmv.qbf.io.QCIR
			.writeParallel(
				v.formula,
				createTempFile(v.instance, ".qcir"),
				true);
	}
}
