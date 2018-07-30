import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.options.Options;

import at.jku.fmv.qbf.benchmark.Executable;

public class qcir2pnf {
	public static void main(String[] args) throws Exception {
		Options opt = Executable.getOptions().build();
		new Runner(opt).run();
	}
}
