package at.jku.fmv.qbf.benchmark.time;

import java.io.IOException;
import java.nio.file.Paths;
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
		QCIR.writeParallel(
			v.formula,
			Benchmarks.createTempFile("writeQCIR", ".qcir"),
			true);
	}

	public static void main(String[] args) throws Exception {
//		String dev = ".*" + QCIRwrite.class.getSimpleName();

		String benchmark = "writeQCIR";

		TestSet testset = new TestSet(
			Paths.get(TestSet.properties.getProperty("qbf_eval17")),
			path -> path.toString().endsWith(".qcir"));

		Options opt = Benchmarks.getOptions(benchmark, testset)
//			.include(dev + "." + benchmark + "*")
			.param("parse", "true")
			.build();
		new Runner(opt).run();
	}
}
