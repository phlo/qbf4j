package at.jku.fmv.qbf.benchmark.time;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.concurrent.TimeUnit;

import org.openjdk.jmh.annotations.*;
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

		String benchmark = "writeQCIR";

		// TODO: eval17/c2_BMC_p1_k2048.qcir seems to be too much
		TestSet testset = new TestSet(
//			Paths.get(TestSet.properties.getProperty("qcir_non-prenex")),
			Paths.get(TestSet.properties.getProperty("qbf_eval17")),
			path -> path.toString().endsWith(".qcir")
				&& !path.getFileName().toString().startsWith("c2_BMC_p1_k204"));

		Options opt = Benchmarks.getOptions(benchmark, testset)
//			.include(
//				QCIRwrite.class.getName()
//				+ "."
//				+ benchmark
//				+ "*")
			.param("parse", "true")
			.build();
		new Runner(opt).run();
	}
}
