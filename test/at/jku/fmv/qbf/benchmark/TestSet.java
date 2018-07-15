package at.jku.fmv.qbf.benchmark;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;
import java.util.Properties;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class TestSet {

	public static Properties properties = new Properties();

	static {
		String propertiesFile = "testset.properties";
		try {
			properties.load(new FileInputStream(propertiesFile));
		} catch (IOException e) {
			throw new RuntimeException(
				"unable to load '" + propertiesFile + "'",
				e);
		}
	}

	public static final Comparator<Path> bySizeAscending =
		new Comparator<Path>() {
			public int compare(Path p1, Path p2) {
				try {
					return Math.toIntExact(Files.size(p1) - Files.size(p2));
				} catch (IOException e) {
					throw new RuntimeException(e);
				}
		}
	};

	public final Path directory;
	public final List<Path> files;
	public final List<String> fileNames;

	public TestSet(Path directory, Predicate<Path> selector) {
		try {
			this.directory = directory;
			this.files = Files.list(directory)
				.filter(selector)
				.sorted(bySizeAscending)
				.collect(Collectors.toList());
			this.fileNames = files.stream()
				.map(Path::getFileName)
				.map(Path::toString)
				.collect(Collectors.toList());
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public TestSet(Path directory) { this(directory, path -> true); }
}
