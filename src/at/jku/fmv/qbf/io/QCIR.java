package at.jku.fmv.qbf.io;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import at.jku.fmv.qbf.QBF;
import at.jku.fmv.qbf.QBF.*;

/**
 *
 * See http://qbf.satisfiability.org/gallery/qcir-gallery14.pdf for details.
 *
 * @author phlo
 */
public final class QCIR {

	// prevent instantiation
	private QCIR() {}

	public static QBF read(String fileName) throws IOException {

		// TODO: ignore comments?
		List<String> lines = Files.lines(Paths.get(fileName)).map(String::trim).collect(Collectors.toList());

		Map<String, QBF> subformulas = new HashMap<String, QBF>();

		BiFunction<String, QBF, QBF> parseGate = (str, sub) -> {

			Function<String, Stream<String>> streamOperands = lst -> Stream.of(lst.split(",")).map(String::trim);

			Function<Stream<String>, List<QBF>> getOperands = s -> {
				Function<String, QBF> getLiteralOrFormula = id ->
					subformulas.containsKey(id) ?
						subformulas.get(id) :
						new Literal(id);
				return
					s.map(id ->
						id.startsWith("-") ?
							new Not(getLiteralOrFormula.apply(id.substring(1))) :
							getLiteralOrFormula.apply(id))
					.collect(Collectors.toList());
			};

			Function<String, List<QBF>> parseOperands = streamOperands.andThen(getOperands);

			String[] l = str.split("[()]");
			String op = l[0].trim().toLowerCase();
			switch (op) {
				case "and":
					List<QBF> operands = parseOperands.apply(l[1]);
					return operands.size() > 1 ?
						new And(parseOperands.apply(l[1])) :
						operands.isEmpty() ? QBF.True : operands.get(0);
				case "or":
					operands = parseOperands.apply(l[1]);
					return operands.size() > 1 ?
						new Or(parseOperands.apply(l[1])) :
						operands.isEmpty() ? QBF.False : operands.get(0);
				case "forall":
					l = l[1].split(";");
					return
						new ForAll(
							sub == null ? parseOperands.apply(l[1]).get(0) : sub,
							streamOperands.apply(l[0]).collect(Collectors.toSet()));
				case "exists":
					l = l[1].split(";");
					return
						new Exists(
							sub == null ? parseOperands.apply(l[1]).get(0) : sub,
							streamOperands.apply(l[0]).collect(Collectors.toSet()));
				default:
					throw new IllegalArgumentException("unknown gate type '" + op + "'");
			}
		};

		Consumer<String> parseSubformula = line -> {
			String[] l = line.split("=");
			String id = l[0].trim();

			try {
				subformulas.put(id, parseGate.apply(l[1], null));
			}
			catch (IllegalArgumentException e) {
				throw new IllegalArgumentException(
					fileName + ":" + (lines.indexOf(line) + 1) + ": error: " + e.getMessage());
			}
		};

		// TODO: parse sequential (using a fast-forward index) or parallel (using CompletableFutures)
		int[] lastLine = {1};

		// parse prefix
		List<String> prefix =
			lines.stream()
				.filter(line -> line.startsWith("forall") || line.startsWith("exists"))
				.peek(line -> lastLine[0]++)
				.collect(Collectors.toList());

		// parse output
		String output =
			lines.stream()
				.skip(lastLine[0])
				.filter(line -> line.startsWith("output"))
				.peek(line -> lastLine[0]++)
				.map(line -> line.split("[()]")[1].trim())
				.findAny()
				.orElse("");

		// parse gates
		lines.stream()
			.skip(lastLine[0])
			.filter(line -> !line.startsWith("#")) // past output section, just skip comments
//			.filter(line -> line.matches("\\w+\\s+=.*")) // filter out gate definitions
			.forEach(parseSubformula);

		// BUUUUUUHHH! ... reducing different types not supported
		QBF formula = subformulas.get(output);
		Collections.reverse(prefix);
		for (String str : prefix)
			formula = parseGate.apply(str, formula);

		return formula;
	}

	// write to file
	public static void write(QBF formula, String fileName) {
	}
}