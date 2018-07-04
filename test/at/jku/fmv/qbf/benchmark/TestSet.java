package at.jku.fmv.qbf.benchmark;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class TestSet {

	public final static Path qcirNonPrenex = Paths.get("/tmp/qcir-non-prenex/");

	public final Path directory;
	public final List<Path> files;
	public final List<String> fileNames;

	public TestSet(Path directory, Predicate<Path> selector) {
		try {
			this.directory = directory;
			this.files = Files.list(directory)
				.filter(selector)
				.sorted()
				.collect(Collectors.toList());
			this.fileNames = files.stream()
				.map(Path::getFileName)
				.map(Path::toString)
				.collect(Collectors.toList());
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public TestSet(Path directory) {
		this(directory, path -> true);
	}
}
