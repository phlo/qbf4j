package at.jku.fmv.qbf.benchmark;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import org.openjdk.jmh.annotations.*;

import main.qcir2pnf;

@BenchmarkMode(Mode.SingleShotTime)
@Warmup(iterations = 0)
@Measurement(iterations = 1)
@Fork(value = 5)
@OutputTimeUnit(TimeUnit.SECONDS)
public class Executable extends Benchmarks {

	@Benchmark
	public void qcir2pnf(Variables v) throws IOException {
		qcir2pnf.main(new String[] {
			v.file.toString(),
			createTempFile("qcir2pnf", ".qcir").toString()
		});
	}
}
