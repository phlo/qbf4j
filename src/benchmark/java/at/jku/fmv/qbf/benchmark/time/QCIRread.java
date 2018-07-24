package at.jku.fmv.qbf.benchmark.time;

import java.nio.file.Paths;

import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.options.Options;

import at.jku.fmv.qbf.benchmark.TestSet;

public class QCIRread {
	public static void main(String[] args) throws Exception {

		String benchmark = "readQCIR";

		TestSet testset = new TestSet(
			Paths.get(TestSet.properties.getProperty("qcir_non-prenex")));

		Options opt = Benchmarks.getOptions(benchmark, testset).build();
		new Runner(opt).run();
	}
}
