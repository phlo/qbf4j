package at.jku.fmv.qbf.io.util;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.Stream.Builder;

public class StringTokenizer {

	private final char delimiter;

	public StringTokenizer(char delimiter) {
		this.delimiter = delimiter;
	}

	public Stream<String> stream(String s) {
		Builder<String> tokens = Stream.builder();

		int pos = 0, end;

		while ((end = s.indexOf(delimiter, pos)) >= 0) {
			String token = s.substring(pos, end);

			if (!token.isEmpty())
				tokens.add(token);

			pos = end + 1;
		}

		if (pos < s.length())
			tokens.add(s.substring(pos));

		return tokens.build();
	}

	public List<String> tokenize(String s) {
		return stream(s).collect(Collectors.toList());
	}
}
