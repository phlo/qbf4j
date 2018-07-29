package QDIMACS;

import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.options.Options;

import at.jku.fmv.qbf.benchmark.QDIMACS;

public class read {
	public static void main(String[] args) throws Exception {
		Options opt = QDIMACS.getOptions().build();
		new Runner(opt).run();
	}
}
