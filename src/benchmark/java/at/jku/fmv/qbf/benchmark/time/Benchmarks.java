package at.jku.fmv.qbf.benchmark.time;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Properties;
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
import at.jku.fmv.qbf.io.QDIMACS;
import at.jku.fmv.qbf.pcnf.PG86;
import at.jku.fmv.qbf.pnf.ForAllUpExistsUp;
import main.QCIR2PNF;

@BenchmarkMode(Mode.SingleShotTime)
@Warmup(iterations = 3)
@Measurement(iterations = 5)
@Fork(value = 1)
public class Benchmarks {

	public static Properties properties = new Properties();

	static {
		String propertiesFile = "benchmark.properties";
		try {
			properties.load(new FileInputStream(propertiesFile));
		} catch (IOException e) {
			throw new RuntimeException(
				"unable to load '" + propertiesFile + "'",
				e);
		}
	}

	public static String getSystemInformation() throws IOException {
		return
			new BufferedReader(
				new InputStreamReader(
					Runtime.getRuntime()
						.exec(properties.getProperty("sys_info_cmd"))
						.getInputStream()))
				.lines()
				.collect(Collectors.joining());
	}

	public static Path createTempFile(
		String prefix,
		String suffix
	) throws IOException {
		Path tmp = Files.createTempFile(prefix, suffix);
		tmp.toFile().deleteOnExit();
		return tmp;
	}

	protected static String getTimeStamp() {
		return new SimpleDateFormat("yyyyMMddHHmm").format(new Date());
	}

	@State(Scope.Benchmark)
	public static class Variables {
		@Param("")
		public String system;
		@Param("")
		public String directory;
		@Param("")
		public String instance;
		@Param("false")
		public boolean parse;

		public Path file;
		public QBF formula;

		@Setup(Level.Trial)
		public void setup() throws IOException {
			file = Paths.get(directory + "/" + instance);

			instance = file.getFileName().toString();

			formula = parse
				? file.toString().endsWith(".qcir")
					? QCIR.read(file)
					: QDIMACS.read(file)
				: null;
		}
	}

	@Benchmark
	@Warmup(iterations = 0)
	@Measurement(iterations = 1)
	@Fork(value = 5)
	@OutputTimeUnit(TimeUnit.SECONDS)
	public void readQCIR(Variables v, Blackhole hole) throws IOException {
		hole.consume(QCIR.read(v.file));
	}

	@Benchmark
	@Warmup(iterations = 0)
	@Measurement(iterations = 1)
	@Fork(value = 5)
	@OutputTimeUnit(TimeUnit.SECONDS)
	public void writeQCIR(Variables v) throws IOException {
		QCIR.write(v.formula, createTempFile("writeQCIR", ".qcir"), true);
	}

	@Benchmark
	@Warmup(iterations = 0)
	@Measurement(iterations = 1)
	@Fork(value = 5)
	@OutputTimeUnit(TimeUnit.SECONDS)
	public void readQDIMACS(Variables v, Blackhole hole) throws IOException {
		hole.consume(QDIMACS.read(v.file));
	}

	@Benchmark
	@Warmup(iterations = 0)
	@Measurement(iterations = 1)
	@Fork(value = 5)
	@OutputTimeUnit(TimeUnit.SECONDS)
	public void writeQDIMACS(Variables v) throws IOException {
		QDIMACS.write(v.formula, createTempFile("writeQDIMACS", ".qdimacs"));
	}

	@Benchmark
	@OutputTimeUnit(TimeUnit.MILLISECONDS)
	public void streamFormula(Variables v, Blackhole hole) {
		v.formula.stream(Traverse.PostOrder)
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
	public void streamBoundVariables(Variables v, Blackhole hole) {
		v.formula.streamBoundVariables()
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
	public void streamQPaths(Variables v, Blackhole hole) {
		v.formula.streamQPaths()
			.forEach(o -> hole.consume(o));
	}

	@Benchmark
	@OutputTimeUnit(TimeUnit.MILLISECONDS)
	public void getCriticalPaths(Variables v, Blackhole hole) {
		hole.consume(QBF.getCriticalPaths(v.formula.getQPaths()));
	}

	@Benchmark
	@OutputTimeUnit(TimeUnit.MILLISECONDS)
	public void getSkeleton(Variables v, Blackhole hole) {
		hole.consume(v.formula.getSkeleton());
	}

	@Benchmark
	@OutputTimeUnit(TimeUnit.MILLISECONDS)
	public void unifyPrefix(Variables v, Blackhole hole) {
		hole.consume(v.formula.unifyPrefix());
	}

	@Benchmark
	@OutputTimeUnit(TimeUnit.MILLISECONDS)
	public void rename(Variables v, Blackhole hole) {
		hole.consume(v.formula.rename(new HashMap<>()));
	}

	@Benchmark
	@OutputTimeUnit(TimeUnit.MILLISECONDS)
	public void cleanse(Variables v, Blackhole hole) {
		hole.consume(v.formula.cleanse());
	}

	@Benchmark
	@OutputTimeUnit(TimeUnit.MILLISECONDS)
	public void toNNF(Variables v, Blackhole hole) {
		hole.consume(v.formula.toNNF());
	}

	@Benchmark
	@OutputTimeUnit(TimeUnit.MILLISECONDS)
	public void toPNF(Variables v, Blackhole hole) {
		hole.consume(v.formula.toPNF(new ForAllUpExistsUp()));
	}

	@Benchmark
	@OutputTimeUnit(TimeUnit.MILLISECONDS)
	public void toPCNF(Variables v, Blackhole hole) {
		hole.consume(v.formula.toPCNF(new ForAllUpExistsUp(), new PG86()));
	}

	@Benchmark
	@OutputTimeUnit(TimeUnit.MILLISECONDS)
	public void toString(Variables v, Blackhole hole) {
		hole.consume(v.formula.toString());
	}

	@Benchmark
	@Warmup(iterations = 0)
	@Measurement(iterations = 1)
	@Fork(value = 5)
	@OutputTimeUnit(TimeUnit.SECONDS)
	public void qcir2pnf(Variables v) throws IOException {
		QCIR2PNF.main(new String[] {
			v.file.toString(),
			createTempFile("qcir2pnf", ".qcir").toString()
		});
	}

	protected static ChainedOptionsBuilder getOptions(
		String benchmark,
		TestSet testset
	) throws IOException {
		String cls = ".*" + Benchmarks.class.getSimpleName();
		return new OptionsBuilder()
                .include(cls + "." + benchmark)
				.shouldDoGC(true)
//				.shouldFailOnError(true)
                .param("system", getSystemInformation())
				.param("directory", testset.directory.toString())
				.param("instance", testset.fileNames.stream().toArray(String[]::new))
                .resultFormat(ResultFormatType.JSON)
                .result(Paths.get(properties.getProperty("result_dir"))
					.resolve(
						benchmark
						+ "_"
						+ Benchmarks.getTimeStamp()
						+ ".json")
					.toString());
	}
}
