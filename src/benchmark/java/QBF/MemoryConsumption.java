package QBF;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.List;
import java.util.OptionalInt;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import at.jku.fmv.qbf.benchmark.Benchmarks;
import at.jku.fmv.qbf.benchmark.util.QBFSizeAgent;
import at.jku.fmv.qbf.benchmark.util.TestSet;
import at.jku.fmv.qbf.io.QCIR;

public class MemoryConsumption {

	private static class Result {
		final int nodes;
		final double size;

		Result(long size, int nodes) {
			this.size = (double) size / 1000000;
			this.nodes = nodes;
		}
	}

	private static TestSet testset = Benchmarks.getTestSet();

	private static void printResult(List<Result> results) {
		String header[] = { "instance", "tree size [mb]", "nodes [#]" };

		List<String> sizes = results.stream()
			.map(s -> String.format("%1$.3f", s.size))
			.collect(Collectors.toList());

		List<String> nodes = results.stream()
			.mapToInt(r -> r.nodes)
			.mapToObj(Integer::toString)
			.collect(Collectors.toList());

		Function<Stream<String>, OptionalInt> colSize = s -> s
			.mapToInt(String::length)
			.max();

		int colSizeInst = colSize.apply(
			Stream.concat(
				Stream.of(header[0]),
				testset.fileNames.stream()))
			.getAsInt();
		int colSizeSize = colSize.apply(
			Stream.concat(
				Stream.of(header[1]),
				sizes.stream()))
			.getAsInt();
		int colSizeNodes = colSize.apply(
			Stream.concat(
				Stream.of(header[2]),
				nodes.stream()))
			.getAsInt();

		BiFunction<String, Integer, String> repeatChar = (s, n) ->
			new String(new char[n]).replace("\0", s);
		Function<String, String> formatInstance = s ->
			String.format(" %1$-" + colSizeInst + "s ", s);
		Function<String, String> formatSize = s ->
			String.format(" %1$" + colSizeSize + "s ", s);
		Function<String, String> formatNodes = s ->
			String.format(" %1$" + colSizeNodes + "s ", s);

		System.out.println(
			"|"
			+ formatInstance.apply(header[0])
			+ "|"
			+ formatSize.apply(header[1])
			+ "|"
			+ formatNodes.apply(header[2])
			+ "|"
		);
		System.out.println(
			"| "
			+ repeatChar.apply("=", colSizeInst)
			+ " | "
			+ repeatChar.apply("=", colSizeSize)
			+ " | "
			+ repeatChar.apply("=", colSizeNodes)
			+ " |"
		);

		IntStream.range(0, testset.fileNames.size())
			.forEach(i -> System.out.println(
				"|"
				+ formatInstance.apply(testset.fileNames.get(i))
				+ "|"
				+ formatSize.apply(sizes.get(i))
				+ "|"
				+ formatNodes.apply(nodes.get(i))
				+ "|"
			));
	}

	public static void main(String[] args) {
		printResult(testset.files.stream()
			.map(i -> {
				try {
					return QCIR.read(i);
				} catch (IOException e) {
					return null;
				}
			})
			.map(f -> {
				Result r = new Result(
					QBFSizeAgent.sizeOf(f),
					QBFSizeAgent.numInstances());
				QBFSizeAgent.reset();
				return r;
			})
			.collect(Collectors.toList()));
	}
}
