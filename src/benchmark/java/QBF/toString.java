package QBF;

import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.options.Options;

import at.jku.fmv.qbf.benchmark.QBF;

public class toString {
	public static void main(String[] args) throws Exception {
		Options opt = QBF.getOptions()
			.param("parse", "true")
			.build();
		new Runner(opt).run();
	}
}
