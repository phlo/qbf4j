package at.jku.fmv.qbf.io;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

import at.jku.fmv.qbf.QBF;
import at.jku.fmv.qbf.QBF.*;

/**
 * See http://www.qbflib.org/qdimacs.html for details.
 *
 * @author phlo
 */
public class QDIMACS {

	// prevent instantiation
	private QDIMACS() {}

	public static QBF read(Path file) throws IOException {
		int prefixStart = 0;
		int prefixEnd = 0;

		List<String> lines = Files.lines(file).collect(Collectors.toList());

		// error reporter
		BiConsumer<String, String> error = (msg, line) -> {
			throw new IllegalArgumentException(
				file.toFile()
				+ (line == null
					? ""
					: ": " + (lines.indexOf(line) + 1))
				+ ": error: " + msg);
		};

		if (lines.isEmpty())
			error.accept(file + " is empty", null);

		// scan pre{amble,fix} - for loop because we want to break early
		for (String line : lines) {
			if (line.startsWith("c") || line.startsWith("p"))
				prefixStart++;
			else if (line.startsWith("a") || line.startsWith("e"))
				prefixEnd = (prefixEnd == 0 ? prefixStart : prefixEnd + 1);
			else break;
		}

		// parse literal
		Function<String, QBF> parseLiteral = s ->
			s.startsWith("-")
				? new Not(new Literal(s.substring(1)))
				: new Literal(s);

		// parse clause
		Function<String, QBF> parseClause = line -> {
			String[] args = line.split("\\s*0");

			if (args.length == 0)
				error.accept("missing variables", line);

			List<QBF> operands =
				Arrays.stream(args[0].split("\\s+"))
					.map(parseLiteral)
					.collect(Collectors.toList());

			return operands.size() > 1
				? new Or(operands)
				: operands.get(0);
		};

		// parse matrix
		List<QBF> clauses = lines.subList(prefixEnd + 1, lines.size()).stream()
			.map(parseClause)
			.collect(Collectors.toList());

		if (clauses.isEmpty())
			error.accept("missing clauses", null);

		QBF formula = clauses.size() > 1
			? new And(clauses)
			: clauses.get(0);

		// prepend prefix
		for (int i = prefixEnd; i >= prefixStart; i--) {
			String[] variables = lines.get(i).split("(^[ae]\\s*|\\s*0$)");

			if (variables.length == 0)
				error.accept("missing variables", lines.get(i));

			formula = lines.get(i).startsWith("a")
				? new ForAll(formula, variables[1].split("\\s+"))
				: new Exists(formula, variables[1].split("\\s+"));
		}

		return formula;
	}

	public static void write(QBF formula, Path file) throws IOException {
		try (BufferedWriter bw = Files.newBufferedWriter(file)) {
			Consumer<QBF> noCNF = f -> {
				throw new IllegalArgumentException("skeleton not in CNF");
			};
			Consumer<True> illegalTrue = x -> noCNF.accept(x);
			Consumer<False> illegalFalse = x -> noCNF.accept(x);
			Consumer<And> illegalAnd = x -> noCNF.accept(x);
			Consumer<Or> illegalOr = x -> noCNF.accept(x);
			Consumer<ForAll> illegalForAll = x -> noCNF.accept(x);
			Consumer<Exists> illegalExists = x -> noCNF.accept(x);

			StringBuilder buffer = new StringBuilder();

			Set<String> variables = new HashSet<>();

			int[] numClauses = {0};

			QBF[] matrix = {formula};

			Function<Quantifier, String> getVariables = q ->
				q.variables.stream()
					.peek(variables::add)
					.collect(Collectors.joining(" "));

			Function<QBF, String> getLiteral = lit -> {
				String var = lit.isLiteral()
					? lit.isNegation()
						? ((Literal) ((Not) lit).subformula).variable
						: ((Literal) lit).variable
					: null;

				if (var == null) noCNF.accept(lit);

				variables.add(var);
				return lit.isNegation() ? "-" + var : var;
			};

			Consumer<String> appendLine = s -> buffer.append(s + " 0\n");

			Consumer<String> appendClause = s -> {
				appendLine.accept(s);
				numClauses[0]++;
				// NOTE: andThen(x -> numClauses[0]++) not working!?
			};

			// append prefix
			formula.streamPrefix().forEach(q -> {
				if (!q.subformula.isQuantifier())
					matrix[0] = q.subformula;

				q.accept(
					f -> appendLine.accept("a " + getVariables.apply(f)),
					e -> appendLine.accept("e " + getVariables.apply(e)));
			});

			// append matrix
			matrix[0].accept(illegalTrue, illegalFalse,
				lit -> appendClause.accept(getLiteral.apply(lit)),
				not -> appendClause.accept(getLiteral.apply(not)),
				and -> and.subformulas.stream().forEach(f ->
					f.accept(illegalTrue, illegalFalse,
						lit -> appendClause.accept(getLiteral.apply(lit)),
						not -> appendClause.accept(getLiteral.apply(not)),
						illegalAnd,
						or -> appendClause.accept(
							or.subformulas.stream()
								.map(getLiteral)
								.collect(Collectors.joining(" "))),
						illegalForAll, illegalExists)),
				illegalOr, illegalForAll, illegalExists
			);

			// write to file
			bw.append("p cnf " + variables.size() + " " + numClauses[0] + "\n");
			bw.append(buffer);
		}
	}
}
