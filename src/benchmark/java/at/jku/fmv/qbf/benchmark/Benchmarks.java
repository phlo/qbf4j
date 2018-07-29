package at.jku.fmv.qbf.benchmark;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Properties;
import java.util.stream.Collectors;

import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.results.format.ResultFormatType;
import org.openjdk.jmh.runner.options.ChainedOptionsBuilder;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import at.jku.fmv.qbf.QBF;
import at.jku.fmv.qbf.benchmark.util.TestSet;

public abstract class Benchmarks {

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
					? at.jku.fmv.qbf.io.QCIR.read(file)
					: at.jku.fmv.qbf.io.QDIMACS.read(file)
				: null;
		}
	}

	public static Properties properties = new Properties();
	static {
		String propertiesFile = "benchmark.properties";
		try {
			properties.load(
				Benchmarks.class
					.getResourceAsStream("/" + propertiesFile));
		} catch (IOException e) {
			throw new RuntimeException(
				"unable to load '" + propertiesFile + "'",
				e);
		}
	}

	public static StackTraceElement getCallee() {
		return Thread.currentThread().getStackTrace()[3];
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

	public static String getTimeStamp() {
		return new SimpleDateFormat("yyyyMMddHHmm").format(new Date());
	}

	public static Path createTempFile(
		String prefix,
		String suffix
	) throws IOException {
		Path tmp = Files.createTempFile(prefix, suffix);
		tmp.toFile().deleteOnExit();
		return tmp;
	}

	public static TestSet getTestSet() {
		return new TestSet(
			Paths.get(properties.getProperty("testset_dir")),
			properties.getProperty("testset_filter", ".*"));
	}

	public static ChainedOptionsBuilder getOptions()
	throws IOException {
		String benchmark = getCallee().getClassName();
		TestSet testset = getTestSet();
		return new OptionsBuilder()
				.include(benchmark + "$")
				.shouldDoGC(true)
//				.shouldFailOnError(true)
                .param("system",
                	getSystemInformation())
				.param("directory",
					testset.directory.toString())
				.param("instance",
					testset.fileNames.stream().toArray(String[]::new))
                .resultFormat(ResultFormatType.JSON)
                .result(Paths.get(properties.getProperty("result_dir"))
					.resolve(
						benchmark
						+ "_"
						+ getTimeStamp()
						+ ".json")
					.toString());
	}
}
