package at.jku.fmv.qbf.io;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
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

	public static QBF read(Path file) throws IOException {

		// TODO: ignore comments?
//		List<String> lines = Files.lines(file).map(String::trim).collect(Collectors.toList());
		List<String> lines = Files.readAllLines(file);

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
					file.toFile() + ":" + (lines.indexOf(line) + 1) + ": error: " + e.getMessage());
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
					(Literal lit) -> illegalArgument.apply(lit),
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
					lit -> gates.putIfAbsent(lit, lit.variable),
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
						lit -> illegalArgument.apply(lit),
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
					lit -> gates.putIfAbsent(lit, lit.variable),
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