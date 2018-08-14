package at.jku.fmv.qbf.io;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import at.jku.fmv.qbf.QBF;
import at.jku.fmv.qbf.QBF.*;
import at.jku.fmv.qbf.io.util.*;

/**
 * A class for reading and writing QCIR-G14 files.
 * <p>
 * See
 * <a href="http://qbf.satisfiability.org/gallery/qcir-gallery14.pdf">
 * http://qbf.satisfiability.org/gallery/qcir-gallery14.pdf
 * </a>
 * for details on the file format.
 *
 * @author phlo
 */
public final class QCIR {

	// prevent instantiation
	private QCIR() {}

	/**
	 * Reads a given QCIR file.
	 *
	 * @param file a file {@link Path}
	 * @return the contained {@link QBF}
	 * @throws IOException if the given {@link Path} is not accessible
	 */
	public static QBF read(Path file) throws IOException {

		List<String> lines = Files.readAllLines(file);

		if (lines.isEmpty())
			throw new ParserException(file, "file is empty");

		StringTokenizer splitComa = new StringTokenizer(',');
		StringTokenizer splitEquals = new StringTokenizer('=');
		StringTokenizer splitLPar = new StringTokenizer('(');
		StringTokenizer splitRPar = new StringTokenizer(')');
		StringTokenizer splitSemiColon = new StringTokenizer(';');

		Map<String, QBF> subformulas = new HashMap<String, QBF>();

		BiFunction<String, QBF, QBF> parseGate = (str, sub) -> {

			Function<String, Stream<String>> streamOperands = lst ->
				splitComa.stream(lst)
					.map(String::trim);

			Function<Stream<String>, List<QBF>> getOperands = s -> {

				Function<String, QBF> getVariableOrFormula = id ->
					subformulas.containsKey(id)
						? subformulas.get(id)
						: new Variable(id);
				return
					s.map(id -> id.startsWith("-")
						? new Not(getVariableOrFormula.apply(id.substring(1)))
						: getVariableOrFormula.apply(id))
					.collect(Collectors.toList());
			};

			Function<String, List<QBF>> parseOperands =
				streamOperands.andThen(getOperands);

			List<String> args = splitLPar.stream(str)
				.flatMap(splitRPar::stream)
				.map(String::trim)
				.collect(Collectors.toList());

			if (args.size() < 2)
				throw new IllegalArgumentException("missing operands");

			switch (args.get(0)) {
				case "and":
					List<QBF> operands = parseOperands.apply(args.get(1));
					return operands.size() > 1
						? new And(operands)
						: operands.isEmpty() ? QBF.True : operands.get(0);
				case "or":
					operands = parseOperands.apply(args.get(1));
					return operands.size() > 1
						? new Or(operands)
						: operands.isEmpty() ? QBF.False : operands.get(0);
				case "forall":
					args = splitSemiColon.stream(args.get(1))
						.map(String::trim)
						.collect(Collectors.toList());
					return
						new ForAll(
							sub == null
								? parseOperands.apply(args.get(1)).get(0)
								: sub,
							streamOperands.apply(args.get(0))
								.collect(Collectors.toSet()));
				case "exists":
					args = splitSemiColon.stream(args.get(1))
						.map(String::trim)
						.collect(Collectors.toList());
					return
						new Exists(
							sub == null
								? parseOperands.apply(args.get(1)).get(0)
								: sub,
							streamOperands.apply(args.get(0))
								.collect(Collectors.toSet()));
				default:
					throw new IllegalArgumentException(
						"unknown gate type '" + args.get(0) + "'");
			}
		};

		Consumer<String> parseSubformula = line -> {

			List<String> args = splitEquals.stream(line)
				.map(String::trim)
				.collect(Collectors.toList());

			if (args.size() < 2 || args.stream().anyMatch(String::isEmpty))
				throw new ParserException(
					file,
					"illegal gate definition",
					lines.indexOf(line) + 1);

			try {
				subformulas.put(
					args.get(0),
					parseGate.apply(args.get(1), null));
			} catch (IllegalArgumentException e) {
				throw new ParserException(file, lines.indexOf(line) + 1, e);
			}
		};

		// find output
		int outputIdx = IntStream.range(1, lines.size())
			.parallel()
			.filter(i -> lines.get(i).startsWith("output"))
			.findAny()
			.orElseThrow(() ->
				new ParserException(file, "missing output"));

		// parse output variable
		String output = splitLPar.stream(lines.get(outputIdx))
			.skip(1)
			.flatMap(splitRPar::stream)
			.findFirst()
			.orElseThrow(() ->
				new ParserException(file, "illegal output", outputIdx + 1));

		// parse gates
		lines.stream()
			.skip(outputIdx + 1)
			.filter(s -> !s.isEmpty() && !s.startsWith("#"))
			.forEach(parseSubformula);

		// prepend prefix
		// BUUUUUUHHH! ... reducing different types not supported
		QBF formula = subformulas.get(output);
		for (int i = outputIdx - 1; i >= 0; i--)
			try {
				String line = lines.get(i);
				if (!line.isEmpty() && !line.startsWith("#"))
					formula = parseGate.apply(line, formula);
			} catch (IllegalArgumentException e) {
				throw new ParserException(file, i + 1, e);
			}

		return formula;
	}

	/**
	 * Writes the given {@link QBF} to a QCIR file.
	 *
	 * @param formula a {@link QBF} worth saving
	 * @param file a file {@link Path}
	 * @param isCleansed write in cleansed form
	 * @throws IOException if the given {@link Path} is not accessible
	 */
	public static void write(QBF formula, Path file, boolean isCleansed)
		throws IOException {

		class Writer {
			StringBuilder buffer = new StringBuilder();

			Set<String> boundVars = formula.streamBoundVariables()
				.unordered()
				.parallel()
				.collect(Collectors.toSet());

			Set<String> freeVars = formula.streamFreeVariables()
				.unordered()
				.parallel()
				.collect(Collectors.toSet());

			int counter = boundVars.size() + freeVars.size() + 1;

			Map<QBF, String> gates = new HashMap<>();

			QBF output;

			void setGateID(QBF gate) {
				gates.putIfAbsent(
					gate,
					Integer.toString(counter++));
			}

			String getGateID(QBF gate) {
				return gate.isNegation()
					? "-" + gates.get(((Not) gate).subformula)
					: gates.get(gate);
			}

			void setOutput(QBF formula) {
				output = formula;
				setGateID(output);
			}

			void appendPrefix() {
				buffer.append(formula.streamPrefix()
					.peek(q -> {
						Quantifier quantifier = (Quantifier) q;
						if (!quantifier.subformula.isQuantifier())
							setOutput(quantifier.subformula);
					})
					.map(q -> q.apply(
						f -> f.variables.stream()
							.collect(Collectors.joining(", ", "forall(", ")")),
						e -> e.variables.stream()
							.collect(Collectors.joining(", ", "exists(", ")"))
						))
					.collect(Collectors.joining("\n")));

				// propositional formula
				if (output == null)
					setOutput(formula);
				else
					buffer.append("\n");
			}

			void appendOutput() {
				buffer.append("output(");
				buffer.append(getGateID(output));
				buffer.append(")\n");
			}

			void appendGate(QBF gate) {
				Function<QBF, String> illegalArgument = f -> {
					throw new IllegalArgumentException("not a gate");
				};

				BiFunction<String, MultiaryOperator, String> buildGate =
					(sym, g) ->
						sym + "("
						+ g.subformulas.stream()
							.map(this::getGateID)
							.collect(Collectors.joining(", "))
						+ ")";

				BiFunction<String, Quantifier, String> buildQuantifier =
					(sym, q) ->
						sym + "("
						+ q.variables.stream()
							.collect(Collectors.joining(", "))
						+ "; "
						+ getGateID(q.subformula)
						+ ")";

				setGateID(gate);

				buffer.append(getGateID(gate));
				buffer.append(" = ");
				buffer.append(gate.apply(
					(True t) -> "and()",
					(False f) -> "or()",
					(Variable var) -> illegalArgument.apply(var),
					(Not not) -> illegalArgument.apply(not),
					(And and) -> buildGate.apply("and", and),
					(Or or) -> buildGate.apply("or", or),
					(ForAll forall) -> buildQuantifier.apply("forall", forall),
					(Exists exists) -> buildQuantifier.apply("exists", exists)
				));
				buffer.append("\n");
			}

			void appendGates(QBF formula) {
				if (formula != output && gates.containsKey(formula)) return;

				Consumer<MultiaryOperator> appendGate = g -> {
						g.subformulas.forEach(this::appendGates);
						appendGate(g);
				};

				Consumer<Quantifier> appendQuantifier = q -> {
					appendGates(q.subformula);
					appendGate(q);
				};

				formula.accept(
					t -> appendGate(t),
					f -> appendGate(f),
					var -> gates.putIfAbsent(var, var.name),
					not -> appendGates(not.subformula),
					and -> appendGate.accept(and),
					or -> appendGate.accept(or),
					forall -> appendQuantifier.accept(forall),
					exists -> appendQuantifier.accept(exists)
				);
			}

			void prependHeader() {
				buffer.insert(0,
					"#QCIR-G14"
					+ (isCleansed ? " " + Integer.toString(counter - 2) : "")
					+ "\n");
			}

			Writer() {
				appendPrefix();
				appendOutput();
				appendGates(output);
				prependHeader();
			}
		}

		try (BufferedWriter bw = Files.newBufferedWriter(file)) {
			bw.append(new Writer().buffer);
		}
	}

	public static void writeParallel(QBF formula, Path file, boolean isCleansed)
		throws IOException {

		class Writer {
			StringBuffer buffer = new StringBuffer();

			Set<String> boundVars = formula.streamBoundVariables()
				.unordered()
				.parallel()
				.collect(Collectors.toSet());

			Set<String> freeVars = formula.streamFreeVariables()
				.unordered()
				.parallel()
				.collect(Collectors.toSet());

			AtomicInteger counter =
				new AtomicInteger(boundVars.size() + freeVars.size() + 1);

			Map<QBF, String> gates = new ConcurrentHashMap<>();

			QBF output;

			void setGateID(QBF gate) {
				gates.putIfAbsent(
					gate,
					Integer.toString(counter.getAndIncrement()));
			}

			String getGateID(QBF gate) {
				return gate.isNegation()
					? "-" + gates.get(((Not) gate).subformula)
					: gates.get(gate);
			}

			void setOutput(QBF formula) {
				output = formula;
				setGateID(output);
			}

			void appendPrefix() {
				buffer.append(formula.streamPrefix()
					.peek(q -> {
						Quantifier quantifier = (Quantifier) q;
						if (!quantifier.subformula.isQuantifier())
							setOutput(quantifier.subformula);
					})
					.map(q -> q.apply(
						f -> f.variables.stream()
							.collect(Collectors.joining(", ", "forall(", ")")),
						e -> e.variables.stream()
							.collect(Collectors.joining(", ", "exists(", ")"))
						))
					.collect(Collectors.joining("\n")) + "\n");
			}

			void appendOutput() {
				buffer.append("output(");
				buffer.append(getGateID(output));
				buffer.append(")\n");
			}

			void appendGate(QBF gate) {
				Function<QBF, String> illegalArgument = f -> {
					throw new IllegalArgumentException("not a gate");
				};

				BiFunction<String, MultiaryOperator, String> buildGate =
					(sym, g) ->
						sym + "("
						+ g.subformulas.stream()
							.map(this::getGateID)
							.collect(Collectors.joining(", "))
						+ ")";

				BiFunction<String, Quantifier, String> buildQuantifier =
					(sym, q) ->
						sym + "("
						+ q.variables.stream()
							.collect(Collectors.joining(", "))
						+ "; "
						+ getGateID(q.subformula)
						+ ")";

				setGateID(gate);

				buffer.append(
					getGateID(gate)
					+ " = "
					+ gate.apply(
						t -> "and()",
						f -> "or()",
						var -> illegalArgument.apply(var),
						not -> illegalArgument.apply(not),
						and -> buildGate.apply("and", and),
						or -> buildGate.apply("or", or),
						forall -> buildQuantifier.apply("forall", forall),
						exists -> buildQuantifier.apply("exists", exists))
					+ "\n"
				);
			}

			void appendGates(QBF formula) {
				if (formula != output && gates.containsKey(formula)) return;

				Consumer<MultiaryOperator> appendGate = g -> {
						g.subformulas
							.stream()
							.unordered()
							.parallel()
							.forEach(this::appendGates);
						appendGate(g);
				};

				Consumer<Quantifier> appendQuantifier = q -> {
					appendGates(q.subformula);
					appendGate(q);
				};

				formula.accept(
					t -> appendGate(t),
					f -> appendGate(f),
					var -> gates.putIfAbsent(var, var.name),
					not -> appendGates(not.subformula),
					and -> appendGate.accept(and),
					or -> appendGate.accept(or),
					forall -> appendQuantifier.accept(forall),
					exists -> appendQuantifier.accept(exists)
				);
			}

			void prependHeader() {
				buffer.insert(0,
					"#QCIR-G14"
					+ (isCleansed ? " " + Integer.toString(counter.get() - 2) : "")
					+ "\n");
			}

			Writer() {
				appendPrefix();
				appendOutput();
				appendGates(output);
				prependHeader();
			}
		}

		try (BufferedWriter bw = Files.newBufferedWriter(file)) {
			bw.append(new Writer().buffer);
		}
	}
}
