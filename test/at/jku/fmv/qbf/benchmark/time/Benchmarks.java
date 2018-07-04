package at.jku.fmv.qbf.benchmark.time;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;
import org.openjdk.jmh.results.format.ResultFormatType;
import org.openjdk.jmh.runner.options.ChainedOptionsBuilder;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import at.jku.fmv.qbf.QBF;
import at.jku.fmv.qbf.QBF.Traverse;
import at.jku.fmv.qbf.benchmark.TestSet;
import at.jku.fmv.qbf.io.QCIR;

@BenchmarkMode(Mode.SingleShotTime)
@Warmup(iterations = 3)
@Measurement(iterations = 5)
@Fork(value = 1)
public class Benchmarks {

	public static String getSystemInformation() throws IOException {
		return
			new BufferedReader(
				new InputStreamReader(
					Runtime.getRuntime()
						.exec("uname -s -r -p")
						.getInputStream()))
				.lines()
				.collect(Collectors.joining());
	}

	protected static String getTimeStamp() {
		return new SimpleDateFormat("yyyyMMddHHmm").format(new Date());
	}

	@State(Scope.Thread)
	public static class Variables {
		@Param("")
		public String system;
		@Param("")
		public String instance;
		@Param("")
		public String directory;
		public Path file;
		public QBF formula;

		@Setup(Level.Trial)
		public void setup() throws IOException {
			file = Paths.get(directory + "/" + instance);
			instance = file.getFileName().toString();
			formula = QCIR.read(file);
		}
	}

	@Benchmark
	@Warmup(iterations = 0)
	@OutputTimeUnit(TimeUnit.SECONDS)
	public void readQCIR(Variables v, Blackhole hole) throws IOException {
		hole.consume(QCIR.read(v.file));
	}

	@Benchmark
	@Warmup(iterations = 0)
	@OutputTimeUnit(TimeUnit.SECONDS)
	public void writeQCIR(Variables v) throws IOException {
		QCIR.write(v.formula, "/tmp/" + v.file.getFileName(), true);
	}

	@Benchmark
	@OutputTimeUnit(TimeUnit.MILLISECONDS)
	public void streamFormula(Variables v, Blackhole hole) {
		v.formula.stream(Traverse.PostOrder)
			.forEach(o -> hole.consume(o));
	}

	@Benchmark
	@OutputTimeUnit(TimeUnit.MILLISECONDS)
	public void streamFormulaParallel(Variables v, Blackhole hole) {
		v.formula.stream(Traverse.PostOrder)
			.parallel()
			.forEach(o -> hole.consume(o));
	}

	@Benchmark
	@OutputTimeUnit(TimeUnit.MILLISECONDS)
	public void streamVariables(Variables v, Blackhole hole) {
		v.formula.streamVariables()
			.forEach(o -> hole.consume(o));
	}

	@Benchmark
	@OutputTimeUnit(TimeUnit.MILLISECONDS)
	public void streamVariablesParallel(Variables v, Blackhole hole) {
		v.formula.streamVariables()
			.parallel()
			.forEach(o -> hole.consume(o));
	}

	@Benchmark
	@BenchmarkMode(Mode.SingleShotTime)
	@OutputTimeUnit(TimeUnit.MILLISECONDS)
	public void streamBoundVariables(Variables v, Blackhole hole) {
		v.formula.streamBoundVariables()
			.forEach(o -> hole.consume(o));
	}

	@Benchmark
	@BenchmarkMode(Mode.SingleShotTime)
	@OutputTimeUnit(TimeUnit.MILLISECONDS)
	public void streamBoundVariablesParallel(Variables v, Blackhole hole) {
		v.formula.streamBoundVariables()
			.parallel()
			.forEach(o -> hole.consume(o));
	}

	@Benchmark
	@OutputTimeUnit(TimeUnit.MILLISECONDS)
	public void streamFreeVariables(Variables v, Blackhole hole) {
		v.formula.streamBoundVariables()
			.forEach(o -> hole.consume(o));
	}

	@Benchmark
	@OutputTimeUnit(TimeUnit.MILLISECONDS)
	public void streamFreeVariablesParallel(Variables v, Blackhole hole) {
		v.formula.streamBoundVariables()
			.parallel()
			.forEach(o -> hole.consume(o));
	}

	@Benchmark
	@OutputTimeUnit(TimeUnit.MILLISECONDS)
	public void streamQPaths(Variables v, Blackhole hole) {
		v.formula.streamQPaths()
			.forEach(o -> hole.consume(o));
	}

	@Benchmark
	@OutputTimeUnit(TimeUnit.MILLISECONDS)
	public void streamQPathsParallel(Variables v, Blackhole hole) {
		v.formula.streamQPaths()
			.parallel()
			.forEach(o -> hole.consume(o));
	}

	@Benchmark
	@OutputTimeUnit(TimeUnit.MILLISECONDS)
	public void streamQPathsParallelUnordered(Variables v, Blackhole hole) {
		v.formula.streamQPaths()
			.unordered()
			.parallel()
			.forEach(o -> hole.consume(o));
	}

	@Benchmark
	@OutputTimeUnit(TimeUnit.MILLISECONDS)
	public void cleanse(Variables v, Blackhole hole) {
		hole.consume(v.formula.cleanse());
	}

	protected static ChainedOptionsBuilder getOptions(
		String benchmark,
		TestSet testset
	) throws IOException {
		String cls = ".*" + Benchmarks.class.getSimpleName();
		return new OptionsBuilder()
                .include(cls + "." + benchmark + "*")
                .param("system", getSystemInformation())
				.param("directory", testset.directory.toString())
				.param("instance", testset.fileNames.stream().toArray(String[]::new))
                .resultFormat(ResultFormatType.JSON)
                .result(
                	"test/benchmarks/"
                	+ benchmark
                	+ "_"
                	+ Benchmarks.getTimeStamp()
                	+ ".json");
	}
}
