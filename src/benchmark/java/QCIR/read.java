package QCIR;

import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.options.Options;

import at.jku.fmv.qbf.benchmark.QCIR;

public class read {
	public static void main(String[] args) throws Exception {
		Options opt = QCIR.getOptions().build();
		new Runner(opt).run();
	}
}
