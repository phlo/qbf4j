package at.jku.fmv.qbf;

import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import at.jku.fmv.qbf.prenexing.PrenexingStrategy;

public abstract class QBF {

	public static QBF True = new True();
	public static QBF False = new False();

	private QBF() {}

//	public abstract void accept(Consumer<True> t,
//								Consumer<False> f,
//								Consumer<Literal> lit,
//								Consumer<Not> not,
//								Consumer<And> and,
//								Consumer<Or> or,
//								Consumer<ForAll> forall,
//								Consumer<Exists> exists);

	public abstract <T> T apply(Function<True, T> t,
								Function<False, T> f,
								Function<Literal, T> lit,
								Function<Not, T> not,
								Function<And, T> and,
								Function<Or, T> or,
								Function<ForAll, T> forall,
								Function<Exists, T> exists);

	public <T> T apply(Function<ForAll, T> forall, Function<Exists, T> exists) {
		return this.apply(
			t -> { throw new IllegalArgumentException("not a quantifier"); },
			f -> { throw new IllegalArgumentException("not a quantifier"); },
			lit -> { throw new IllegalArgumentException("not a quantifier"); },
			not -> { throw new IllegalArgumentException("not a quantifier"); },
			and -> { throw new IllegalArgumentException("not a quantifier"); },
			or -> { throw new IllegalArgumentException("not a quantifier"); },
			forall,
			exists);
	}

	public static final class True extends QBF {
		public <T> T apply(	Function<True, T> t,
							Function<False, T> f,
							Function<Literal, T> lit,
							Function<Not, T> not,
							Function<And, T> and,
							Function<Or, T> or,
							Function<ForAll, T> forall,
							Function<Exists, T> exists ) { return t.apply(this); }


		private True() {}
	}

	public static final class False extends QBF {
		public <T> T apply(	Function<True, T> t,
							Function<False, T> f,
							Function<Literal, T> lit,
							Function<Not, T> not,
							Function<And, T> and,
							Function<Or, T> or,
							Function<ForAll, T> forall,
							Function<Exists, T> exists ) { return f.apply(this); }

		private False() {}
	}

	public static final class Literal extends QBF {
		public final String variable;

		public <T> T apply(	Function<True, T> t,
							Function<False, T> f,
							Function<Literal, T> lit,
							Function<Not, T> not,
							Function<And, T> and,
							Function<Or, T> or,
							Function<ForAll, T> forall,
							Function<Exists, T> exists ) { return lit.apply(this); }

		public Literal(String variable) {
			if (variable == null || variable.isEmpty())
				throw new IllegalArgumentException("missing variable");

			this.variable = variable;
		}
	}

	public static final class Not extends QBF {
		public final QBF subformula;

		public <T> T apply(	Function<True, T> t,
							Function<False, T> f,
							Function<Literal, T> lit,
							Function<Not, T> not,
							Function<And, T> and,
							Function<Or, T> or,
							Function<ForAll, T> forall,
							Function<Exists, T> exists ) { return not.apply(this); }

		public Not(QBF subformula) {
			if (subformula == null)
				throw new IllegalArgumentException("missing subformula");

			this.subformula = subformula;
		}
	}

	public static final class And extends QBF {
		public final List<QBF> subformulas;

		public <T> T apply(	Function<True, T> t,
							Function<False, T> f,
							Function<Literal, T> lit,
							Function<Not, T> not,
							Function<And, T> and,
							Function<Or, T> or,
							Function<ForAll, T> forall,
							Function<Exists, T> exists ) { return and.apply(this); }

		public And(List<QBF> subformulas) {
			if (subformulas == null || subformulas.size() < 2)
				throw new IllegalArgumentException("missing subformulas");

			this.subformulas = subformulas;
		}

		public And(QBF... subformulas) { this(Arrays.asList(subformulas)); }
	}

	public static final class Or extends QBF {
		public final List<QBF> subformulas;

		public <T> T apply(	Function<True, T> t,
							Function<False, T> f,
							Function<Literal, T> lit,
							Function<Not, T> not,
							Function<And, T> and,
							Function<Or, T> or,
							Function<ForAll, T> forall,
							Function<Exists, T> exists ) { return or.apply(this); }

		public Or(List<QBF> subformulas) {
			if (subformulas == null || subformulas.size() < 2)
				throw new IllegalArgumentException("missing subformulas");

			this.subformulas = subformulas;
		}

		public Or(QBF... subformulas) { this(Arrays.asList(subformulas)); }
	}

	public static final class ForAll extends QBF {
		public final QBF subformula;
		public final Set<String> variables;

		public <T> T apply(	Function<True, T> t,
							Function<False, T> f,
							Function<Literal, T> lit,
							Function<Not, T> not,
							Function<And, T> and,
							Function<Or, T> or,
							Function<ForAll, T> forall,
							Function<Exists, T> exists ) { return forall.apply(this); }

		public ForAll(QBF subformula, Set<String> variables) {
			if (subformula == null)
				throw new IllegalArgumentException("missing subformula");
			if (variables == null || variables.isEmpty())
				throw new IllegalArgumentException("missing variable");

			this.subformula = subformula;
			this.variables = Collections.unmodifiableSet(variables);
		}

		public ForAll(QBF subformula, String... variables) {
			this(subformula, new HashSet<String>(Arrays.asList(variables)));
		}
	}

	public static final class Exists extends QBF {
		public final QBF subformula;
		public final Set<String> variables;

		public <T> T apply(	Function<True, T> t,
							Function<False, T> f,
							Function<Literal, T> lit,
							Function<Not, T> not,
							Function<And, T> and,
							Function<Or, T> or,
							Function<ForAll, T> forall,
							Function<Exists, T> exists ) { return exists.apply(this); }

		public Exists(QBF subformula, Set<String> variables) {
			if (subformula == null)
				throw new IllegalArgumentException("missing subformula");
			if (variables == null || variables.isEmpty())
				throw new IllegalArgumentException("missing variable");

			this.subformula = subformula;
			this.variables = Collections.unmodifiableSet(variables);
		}

		public Exists(QBF subformula, String... variables) {
			this(subformula, new HashSet<String>(Arrays.asList(variables)));
		}
	}

	// TODO: include (more) predicates? static or not static?
	public static final Predicate<QBF> isQuantifier = formula ->
		formula.apply(
			t -> false, f -> false, lit -> false, not -> false, and -> false, or -> false,
			forall -> true,
			exists -> true
		);
	public boolean isQuantifier() { return isQuantifier.test(this); }

	public static final Predicate<QBF> isForAll = formula ->
		formula.apply(
			t -> false, f -> false, lit -> false, not -> false, and -> false, or -> false,
			forall -> true,
			exists -> false
		);
	public boolean isForAll() { return isForAll.test(this); }

	public static final Predicate<QBF> isExists = formula ->
		formula.apply(
			t -> false, f -> false, lit -> false, not -> false, and -> false, or -> false,
			forall -> false,
			exists -> true
		);
	public boolean isExists() { return isExists.test(this); }

	public static final Predicate<QBF> isConstant = formula ->
		formula.apply(
			t -> true, f -> true,
			lit -> false, not -> false, and -> false, or -> false, forall -> false, exists -> false
		);
	public boolean isConstant() { return isConstant.test(this); }

	public static final Predicate<QBF> isLiteral = formula ->
		formula.apply(
			t -> false, f -> false,
			lit -> true,
			not -> not.subformula.isLiteral(),
			and -> false, or -> false, forall -> false, exists -> false
		);
	public boolean isLiteral() { return isLiteral.test(this); }

	private enum HashID {
		FALSE,
		TRUE,
		LITERAL,
		NOT,
		AND,
		OR,
		FORALL,
		EXISTS
	}

	public int hashCode() {
		return this.apply(
			t -> HashID.TRUE.ordinal(),
			f -> HashID.FALSE.ordinal(),
			lit -> (lit.variable.hashCode() << 3) + HashID.LITERAL.ordinal(),
			not -> (not.subformula.hashCode() << 3) + HashID.NOT.ordinal(),
			and -> (and.subformulas.hashCode() << 3) + HashID.AND.ordinal(),
			or -> (or.subformulas.hashCode() << 3) + HashID.OR.ordinal(),
			forall -> (Objects.hash(forall.subformula, forall.variables) << 3) + HashID.FORALL.ordinal(),
			exists -> (Objects.hash(exists.subformula, exists.variables) << 3) + HashID.EXISTS.ordinal());
	}

	public boolean equals(Object o) {
		if (this == o) return true;
		if (!(o instanceof QBF)) return false;

		final QBF other = (QBF) o;

		Function<True, Boolean> noTrue = x -> false;
		Function<False, Boolean> noFalse = x -> false;
		Function<Literal, Boolean> noLiteral = x -> false;
		Function<Not, Boolean> noNot = x -> false;
		Function<And, Boolean> noAnd = x -> false;
		Function<Or, Boolean> noOr = x -> false;
		Function<ForAll, Boolean> noForall = x -> false;
		Function<Exists, Boolean> noExists = x -> false;

		return this.apply(
			(True t) -> t == True,
			(False f) -> f == False,
			(Literal lit1) ->
				other.apply(
					noTrue, noFalse,
					(Literal lit2) -> lit1.variable.equals(lit2.variable),
					noNot, noAnd, noOr, noForall, noExists),
			(Not not1) ->
				other.apply(
					noTrue, noFalse, noLiteral,
					(Not not2) -> not1.subformula.equals(not2.subformula),
					noAnd, noOr, noForall, noExists),
			(And and1) ->
				other.apply(
					noTrue, noFalse, noLiteral, noNot,
					(And and2) -> and1.subformulas.equals(and2.subformulas),
					noOr, noForall, noExists),
			(Or or1) ->
				other.apply(
					noTrue, noFalse, noLiteral, noNot, noAnd,
					(Or or2) -> or1.subformulas.equals(or2.subformulas),
					noForall, noExists),
			(ForAll forall1) ->
				other.apply(
					noTrue, noFalse, noLiteral, noNot, noAnd, noOr,
					(ForAll forall2) ->
						forall1.variables.equals(forall2.variables) &&
						forall1.subformula.equals(forall2.subformula),
					noExists),
			(Exists exists1) ->
				other.apply(
					noTrue, noFalse, noLiteral, noNot, noAnd, noOr, noForall,
					(Exists exists2) ->
						exists1.variables.equals(exists2.variables) &&
						exists1.subformula.equals(exists2.subformula))
		);
	}

	public enum Traverse { PreOrder, PostOrder };
	public Stream<QBF> stream(Traverse traversal) {
		Stream<QBF> childStream =
			this.apply(
				(True t) -> Stream.empty(),
				(False f) -> Stream.empty(),
				(Literal lit) -> Stream.empty(),
				(Not not) -> not.subformula.stream(traversal),
				(And and) -> and.subformulas.stream().flatMap(f -> f.stream(traversal)),
				(Or or) -> or.subformulas.stream().flatMap(f -> f.stream(traversal)),
				(ForAll forall) -> forall.subformula.stream(traversal),
				(Exists exists) -> exists.subformula.stream(traversal));

		return
			traversal == Traverse.PreOrder ?
				Stream.concat(Stream.of(this), childStream) :
				Stream.concat(childStream, Stream.of(this));
	}

	public Stream<QBF> streamPrefix() {
		return this.apply(
			(True t) -> Stream.empty(),
			(False f) -> Stream.empty(),
			(Literal lit) -> Stream.empty(),
			(Not not) -> Stream.empty(),
			(And and) -> Stream.empty(),
			(Or or) -> Stream.empty(),
			(ForAll forall) -> Stream.concat(Stream.of(forall), forall.subformula.streamPrefix()),
			(Exists exists) -> Stream.concat(Stream.of(exists), exists.subformula.streamPrefix())
		);
	}

//	public List<QBF> getPrefix() { return streamPrefix().collect(Collectors.toList()); }

	// qpath = head node of qpath prefix (get full path with streamPrefix)
	public Stream<QBF> streamQPaths() {
		return this.apply(
			(True t) -> Stream.empty(),
			(False f) -> Stream.empty(),
			(Literal lit) -> Stream.empty(),
			(Not not) -> Stream.empty(),
			(And and) -> and.subformulas.stream().flatMap(f -> f.streamQPaths()),
			(Or or) -> or.subformulas.stream().flatMap(f -> f.streamQPaths()),
			(ForAll forall) -> {
				List<QBF> childPaths = forall.subformula.streamQPaths().collect(Collectors.toList());
				return
					childPaths.isEmpty() ?
						Stream.of(forall) :
						childPaths.stream().map(f -> new ForAll(f, forall.variables).unifyPrefix());
//						childPaths.stream().map(f -> new ForAll(f, forall.variables));
			},
			(Exists exists) -> {
				List<QBF> childPaths = exists.subformula.streamQPaths().collect(Collectors.toList());
				return
					childPaths.isEmpty() ?
						Stream.of(exists) :
						childPaths.stream().map(f -> new Exists(f, exists.variables).unifyPrefix());
//						childPaths.stream().map(f -> new Exists(f, exists.variables));
			}
		);
	}

	public List<QBF> getQPaths() { return streamQPaths().collect(Collectors.toList()); }

	public static List<QBF> getCriticalPaths(List<QBF> qpaths) {
		// NOTE: filtering collector - Java 9 or https://stackoverflow.com/a/48276796
		Map<Long, List<QBF>> qpathsByLen = qpaths.stream()
			.collect(Collectors.groupingBy(p -> p.streamPrefix().count()));

		// get longest paths and remove those with equal quantifier strings (leading is equal)
		boolean[] contained = {false, false}; // ∀[0] ∃[1]
		List<QBF> critical =
			qpathsByLen.get(
					qpathsByLen.keySet().stream()
					.max(Comparator.naturalOrder())
					.get())
				.stream()
				.filter(f -> f.apply(
					forall -> {
						boolean c = contained[0];
						if (!c) contained[0] = true;
						return !c;
					},
					exists -> {
						boolean c = contained[1];
						if (!c) contained[1] = true;
						return !c;
					}))
				.collect(Collectors.toList());

		//   leading quantifiers differ -> prepend the first quantifier from each qpath to the other
		if (critical.size() > 1) {
			BiFunction<QBF, QBF, QBF> prependLeadingQuantifier = (cp1, cp2) ->
				cp2.apply(
					forall -> new ForAll(cp1, forall.variables),
					exists -> new Exists(cp1, exists.variables));

			QBF cp1 = critical.get(0);
			QBF cp2 = critical.get(1);

			critical.set(0, prependLeadingQuantifier.apply(cp2, cp1));
			critical.set(1, prependLeadingQuantifier.apply(cp1, cp2));
		}

		return critical;
	}

	public QBF getSkeleton() {
		return this.apply(
			t -> t, f -> f, lit -> lit,
			(Not not) ->
				not.subformula.apply(
					t -> not, f -> not, lit -> not,
					not2 -> new Not(not2.getSkeleton()),
					and -> new Not(and.getSkeleton()),
					or -> new Not(or.getSkeleton()),
					forall -> new Not(forall.getSkeleton()),
					exists -> new Not(exists.getSkeleton())
				),
			(And and) ->
				new And(
					and.subformulas.stream()
						.map(f -> f.getSkeleton())
						.collect(Collectors.toList())),
			(Or or) ->
				new Or(
					or.subformulas.stream()
						.map(f -> f.getSkeleton())
						.collect(Collectors.toList())),
			(ForAll forall) -> forall.subformula.getSkeleton(),
			(Exists exists) -> exists.subformula.getSkeleton()
		);
	}

	/**
	 * Unify all sequences of equal quantifiers in the formula's prefix (e.g. ∀x: ∀y: ϕ -> ∀x,y: ϕ).
	 *
	 * @return QBF with purely alternating prefix
	 */
	public QBF unifyPrefix() {
		return this.apply(
			t -> t, f -> f, lit -> lit, not -> not, and -> and, or -> or,
			(ForAll forall1) -> forall1.subformula.apply(
				t -> forall1, f -> forall1, lit -> forall1, not -> forall1, and -> forall1, or -> forall1,
				(ForAll forall2) ->
					new ForAll(
						forall2.subformula,
						Stream.of(forall1.variables, forall2.variables)
							.flatMap(Set::stream)
							.collect(Collectors.toSet()))
						.unifyPrefix(),
				(Exists exists) -> new ForAll(exists.unifyPrefix(), forall1.variables)
			),
			(Exists exists1) -> exists1.subformula.apply(
				t -> exists1, f -> exists1, lit -> exists1, not -> exists1, and -> exists1, or -> exists1,
				(ForAll forall) -> new Exists(forall.unifyPrefix(), exists1.variables),
				(Exists exists2) ->
					new Exists(
						exists2.subformula,
						Stream.of(exists1.variables, exists2.variables)
							.flatMap(Set::stream)
							.collect(Collectors.toSet()))
						.unifyPrefix()
			)
		);
	}

	public QBF negate() {
		return this.apply(
			(True t) -> False,
			(False f) -> True,
			(Literal lit) -> new Not(lit),
			(Not not) -> not.subformula,
			(And and) -> new Not(and),
			(Or or) -> new Not(or),
			(ForAll forall) -> new Not(forall),
			(Exists exists) -> new Not(exists)
		);
	}

	public QBF toNNF() {
		return this.apply(
			(True t) -> t,
			(False f) -> f,
			(Literal lit) -> lit,
			(Not not) -> not.subformula.apply(
				(True t) -> False,
				(False f) -> True,
				(Literal lit) -> not,
				(Not impossible) -> not,
				(And and) ->
					new Or(
						and.subformulas
							.stream()
							.map(f -> f.negate().toNNF())
							.collect(Collectors.toList())
					),
				(Or or) ->
					new And(
						or.subformulas
							.stream()
							.map(f -> f.negate().toNNF())
							.collect(Collectors.toList())
					),
				(ForAll forall) -> new Exists(forall.subformula.negate().toNNF(), forall.variables),
				(Exists exists) -> new ForAll(exists.subformula.negate().toNNF(), exists.variables)
			),
			(And and) -> new And(and.subformulas.stream().map(QBF::toNNF).collect(Collectors.toList())),
			(Or or) -> new Or(or.subformulas.stream().map(QBF::toNNF).collect(Collectors.toList())),
			(ForAll forall) -> new ForAll(forall.subformula.toNNF(), forall.variables),
			(Exists exists) -> new Exists(exists.subformula.toNNF(), exists.variables)
		);
	}

	public QBF toPNF(PrenexingStrategy strategy) {
		return strategy.apply(this.toNNF());
	}

	public static String quantifierToString(QBF formula) {
		return formula.apply(
//			t -> "", f -> "", lit -> "", not -> "", and -> "", or -> "",
			forall -> "∀" + forall.variables.stream().sorted().collect(Collectors.joining(",")),
			exists -> "∃" + exists.variables.stream().sorted().collect(Collectors.joining(","))
		);
	}

	public String prefixToString() {
		return streamPrefix().map(QBF::quantifierToString).collect(Collectors.joining(" "));
	}

	public String toString() {
		return this.apply(
			(True t) -> "TRUE",
			(False f) -> "FALSE",
			(Literal lit) -> lit.variable.toString(),
			(Not not) -> "-" + not.subformula.toString(),
			(And and) ->
				"(" +
				and.subformulas
					.stream()
					.map(QBF::toString)
					.collect(Collectors.joining(" ∧ ")) +
				")",
			(Or or) ->
				"(" +
				or.subformulas
					.stream()
					.map(QBF::toString)
					.collect(Collectors.joining(" ∨ ")) +
				")",
			(ForAll forall) ->
				quantifierToString(forall) +
				": " + forall.subformula.toString(),
			(Exists exists) ->
				quantifierToString(exists) +
				": " + exists.subformula.toString()
		);
	}
}