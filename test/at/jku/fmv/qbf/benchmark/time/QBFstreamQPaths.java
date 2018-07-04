package at.jku.fmv.qbf.benchmark.time;

import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.options.Options;

import at.jku.fmv.qbf.benchmark.TestSet;

public class QBFstreamQPaths {
	public static void main(String[] args) throws Exception {

		String benchmark = "streamQPaths";

		TestSet testset =
			new TestSet(
				TestSet.qcirNonPrenex);
//				TestSet.qcirNonPrenex,
//				path -> path
//					.toString()
//					.matches(".*0\\d\\.qcir"));

		Options opt = Benchmarks.getOptions(benchmark, testset).build();
		new Runner(opt).run();
	}
}
