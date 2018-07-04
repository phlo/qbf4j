package at.jku.fmv.qbf.benchmark.time;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Warmup;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.options.Options;

import at.jku.fmv.qbf.benchmark.TestSet;
import at.jku.fmv.qbf.benchmark.time.Benchmarks.Variables;
import at.jku.fmv.qbf.io.QCIR;

@BenchmarkMode(Mode.SingleShotTime)
@Warmup(iterations = 0)
@Measurement(iterations = 5)
@Fork(value = 1)
public class QCIRwrite {

	@Benchmark
	@OutputTimeUnit(TimeUnit.SECONDS)
	public void writeQCIRParallel(Variables v) throws IOException {
		QCIR.writeParallel(v.formula, "/tmp/" + v.file.getFileName(), true);
	}

	public static void main(String[] args) throws Exception {
		String dev = ".*" + QCIRwrite.class.getSimpleName();

		String benchmark = "write";

		TestSet testset =
			new TestSet(
				TestSet.qcirNonPrenex);
//				TestSet.qcirNonPrenex,
//				path -> path
//					.toString()
//					.matches(".*2[789]\\.qcir"));

		Options opt = Benchmarks.getOptions(benchmark, testset)
			.include(dev + "." + benchmark + "*")
			.build();
		new Runner(opt).run();
	}
}
