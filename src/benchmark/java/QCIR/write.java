package QCIR;

import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.options.Options;

import at.jku.fmv.qbf.benchmark.QCIR;

public class write {
	public static void main(String[] args) throws Exception {
		// TODO: eval17/c2_BMC_p1_k2048.qcir seems to be too much
		Options opt = QCIR.getOptions()
			.include(QCIR.class.getName() + ".write*")
			.param("parse", "true")
			.build();
		new Runner(opt).run();
	}
}
