package at.jku.fmv.qbf.io.util;

import static org.junit.jupiter.api.Assertions.*;

import java.util.stream.Collectors;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import at.jku.fmv.qbf.io.util.StringTokenizer;

@DisplayName("StringTokenizer")
class StringTokenizerTest {

	@Test
	@DisplayName("stream")
	void test_stream() {
		assertEquals(
		"a b c",
		new StringTokenizer(' ')
			.stream("a b c")
			.collect(Collectors.joining(" ")));

		assertEquals(
		"a b c",
		new StringTokenizer(' ')
			.stream("a  b  c")
			.collect(Collectors.joining(" ")));

		assertEquals(
		"a b c",
		new StringTokenizer(' ')
			.stream("a b c ")
			.collect(Collectors.joining(" ")));

		assertEquals(
		"a b c",
		new StringTokenizer(' ')
			.stream(" a b c")
			.collect(Collectors.joining(" ")));

		assertEquals(
		"a b c",
		new StringTokenizer(' ')
			.stream(" a b c ")
			.collect(Collectors.joining(" ")));

		assertEquals(
		"a b c\n",
		new StringTokenizer(' ')
			.stream("a b c\n")
			.collect(Collectors.joining(" ")));

		assertEquals(
		"a,b,c",
		new StringTokenizer(',')
			.stream("a, b, c")
			.map(String::trim)
			.collect(Collectors.joining(",")));
	}
}
