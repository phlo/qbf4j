package at.jku.fmv.qbf.benchmark.time;

import java.nio.file.Paths;

import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.options.Options;

import at.jku.fmv.qbf.benchmark.TestSet;

public class QCIR2PNF {
	public static void main(String[] args) throws Exception {

		String benchmark = "qcir2pnf";

		// TODO: eval17/c2_BMC_p1_k2048.qcir seems to be too much
		TestSet testset = new TestSet(
			Paths.get(TestSet.properties.getProperty("qcir_non-prenex")));
//			Paths.get(TestSet.properties.getProperty("qbf_eval17")));

		Options opt = Benchmarks.getOptions(benchmark, testset).build();
		new Runner(opt).run();
	}
}
