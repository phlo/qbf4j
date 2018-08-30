package at.jku.fmv.qbf;

import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import at.jku.fmv.qbf.pcnf.CNFEncoder;
import at.jku.fmv.qbf.pnf.PrenexingStrategy;

/**
 * A quantified boolean formula.
 * <p>
 * {@link QBF} is an immutable data structure, representing quantified boolean
 * formula trees containing the following connectives:
 * <ul>
 * <li>¬ (negation)
 * <li>∧ (conjunction)
 * <li>∨ (disjunction)
 * <li>∀ (universal quantification)
 * <li>∃ (existential quantification)
 * </ul>
 *
 * @author phlo
 */
public abstract class QBF {

	/** Singleton instance of the boolean constant {@link True true}. */
	public static final QBF True = new True();

	/** Singleton instance of the boolean constant {@link False false}. */
	public static final QBF False = new False();

	private QBF() {}

	/**
	 * Applies a given {@link Consumer} to the underlying concrete type.
	 *
	 * @param t
	 * {@link Consumer} accepting the {@link True boolean constant true}
	 * @param f
	 * {@link Consumer} accepting the {@link False boolean constant false}
	 * @param var
	 * {@link Consumer} accepting {@link Variable variables}
	 * @param not
	 * {@link Consumer} accepting {@link Not negations}
	 * @param and
	 * {@link Consumer} accepting {@link And conjunctions}
	 * @param or
	 * {@link Consumer} accepting {@link Or disjunctions}
	 * @param forall
	 * {@link Consumer} accepting {@link ForAll universal quantifiers}
	 * @param exists
	 * {@link Consumer} accepting {@link Exists existential quantifiers}
	 */
	public abstract void accept(
		Consumer<True> t,
		Consumer<False> f,
		Consumer<Variable> var,
		Consumer<Not> not,
		Consumer<And> and,
		Consumer<Or> or,
		Consumer<ForAll> forall,
		Consumer<Exists> exists
	);

	/**
	 * Applies a given {@link Consumer} to a quantifier.
	 *
	 * @param forall
	 * {@link Consumer} accepting {@link ForAll universal quantifiers}
	 * @param exists
	 * {@link Consumer} accepting {@link Exists existential quantifiers}
	 *
	 * @throws IllegalArgumentException if this is not a quantifier
	 */
	public void accept(Consumer<ForAll> forall, Consumer<Exists> exists) {
		String msg = "not a quantifier";
		this.accept(
			t -> { throw new IllegalArgumentException(msg); },
			f -> { throw new IllegalArgumentException(msg); },
			var -> { throw new IllegalArgumentException(msg); },
			not -> { throw new IllegalArgumentException(msg); },
			and -> { throw new IllegalArgumentException(msg); },
			or -> { throw new IllegalArgumentException(msg); },
			forall,
			exists);
	}

	/**
	 * Applies a given {@link Function} to the underlying concrete type.
	 *
	 * @param <T> return type
	 *
	 * @param t
	 * {@link Function} accepting the {@link True boolean constant true}
	 * @param f
	 * {@link Function} accepting the {@link False boolean constant false}
	 * @param var
	 * {@link Function} accepting {@link Variable variables}
	 * @param not
	 * {@link Function} accepting {@link Not negations}
	 * @param and
	 * {@link Function} accepting {@link And conjunctions}
	 * @param or
	 * {@link Function} accepting {@link Or disjunctions}
	 * @param forall
	 * {@link Function} accepting {@link ForAll universal quantifiers}
	 * @param exists
	 * {@link Function} accepting {@link Exists existential quantifiers}
	 *
	 * @return result of the {@link Function} application
	 */
	public abstract <T> T apply(
		Function<True, T> t,
		Function<False, T> f,
		Function<Variable, T> var,
		Function<Not, T> not,
		Function<And, T> and,
		Function<Or, T> or,
		Function<ForAll, T> forall,
		Function<Exists, T> exists
	);

	/**
	 * Applies a given {@link Function} to a quantifier.
	 *
	 * @param <T> return type
	 *
	 * @param forall
	 * {@link Function} accepting {@link ForAll universal quantifiers}
	 * @param exists
	 * {@link Function} accepting {@link Exists existential quantifiers}
	 *
	 * @return result of the {@link Function} application
	 *
	 * @throws IllegalArgumentException if this is not a quantifier
	 */
	public <T> T apply(Function<ForAll, T> forall, Function<Exists, T> exists) {
		String msg = "not a quantifier";
		return this.apply(
			t -> { throw new IllegalArgumentException(msg); },
			f -> { throw new IllegalArgumentException(msg); },
			var -> { throw new IllegalArgumentException(msg); },
			not -> { throw new IllegalArgumentException(msg); },
			and -> { throw new IllegalArgumentException(msg); },
			or -> { throw new IllegalArgumentException(msg); },
			forall,
			exists);
	}

	/**
	 * A terminal node in the QBF tree.
	 *
	 * @author phlo
	 */
	public static abstract class Terminal extends QBF {}

	/**
	 * The boolean constant {@code true}.
	 *
	 * @author phlo
	 */
	public static final class True extends Terminal {

		private final int hash;

		public void accept(
			Consumer<True> t,
			Consumer<False> f,
			Consumer<Variable> var,
			Consumer<Not> not,
			Consumer<And> and,
			Consumer<Or> or,
			Consumer<ForAll> forall,
			Consumer<Exists> exists
		) { t.accept(this); }

		public <T> T apply(
			Function<True, T> t,
			Function<False, T> f,
			Function<Variable, T> var,
			Function<Not, T> not,
			Function<And, T> and,
			Function<Or, T> or,
			Function<ForAll, T> forall,
			Function<Exists, T> exists
		) { return t.apply(this); }

		private True() { this.hash = this.hash(); }
	}

	/**
	 * The boolean constant {@code false}.
	 *
	 * @author phlo
	 */
	public static final class False extends Terminal {

		private final int hash;

		public void accept(
			Consumer<True> t,
			Consumer<False> f,
			Consumer<Variable> var,
			Consumer<Not> not,
			Consumer<And> and,
			Consumer<Or> or,
			Consumer<ForAll> forall,
			Consumer<Exists> exists
		) { f.accept(this); }

		public <T> T apply(
			Function<True, T> t,
			Function<False, T> f,
			Function<Variable, T> var,
			Function<Not, T> not,
			Function<And, T> and,
			Function<Or, T> or,
			Function<ForAll, T> forall,
			Function<Exists, T> exists
		) { return f.apply(this); }

		private False() { this.hash = this.hash(); }
	}

	/**
	 * A variable.
	 *
	 * @author phlo
	 */
	public static final class Variable extends Terminal {

		private final int hash;

		public final String name;

		public void accept(
			Consumer<True> t,
			Consumer<False> f,
			Consumer<Variable> var,
			Consumer<Not> not,
			Consumer<And> and,
			Consumer<Or> or,
			Consumer<ForAll> forall,
			Consumer<Exists> exists
		) { var.accept(this); }

		public <T> T apply(
			Function<True, T> t,
			Function<False, T> f,
			Function<Variable, T> var,
			Function<Not, T> not,
			Function<And, T> and,
			Function<Or, T> or,
			Function<ForAll, T> forall,
			Function<Exists, T> exists
		) { return var.apply(this); }

		public Variable(String name) {
			if (name == null || name.isEmpty())
				throw new IllegalArgumentException("missing variable");

			this.name = name;

			this.hash = this.hash();
		}
	}

	/**
	 * Base class for unary operators.
	 *
	 * @author phlo
	 */
	public static abstract class UnaryOperator extends QBF {

		public final QBF subformula;

		private UnaryOperator(QBF subformula) {
			if (subformula == null)
				throw new IllegalArgumentException("missing subformula");

			this.subformula = subformula;
		}
	}

	/**
	 * A negation.
	 *
	 * @author phlo
	 */
	public static final class Not extends UnaryOperator {

		private final int hash;

		public void accept(
			Consumer<True> t,
			Consumer<False> f,
			Consumer<Variable> var,
			Consumer<Not> not,
			Consumer<And> and,
			Consumer<Or> or,
			Consumer<ForAll> forall,
			Consumer<Exists> exists
		) { not.accept(this); }

		public <T> T apply(
			Function<True, T> t,
			Function<False, T> f,
			Function<Variable, T> var,
			Function<Not, T> not,
			Function<And, T> and,
			Function<Or, T> or,
			Function<ForAll, T> forall,
			Function<Exists, T> exists
		) { return not.apply(this); }

		public Not(QBF subformula) {
			super(subformula);
			this.hash = this.hash();
		}
	}

	/**
	 * Base class for multiary (n-ary) operators.
	 *
	 * @author phlo
	 */
	public static abstract class MultiaryOperator extends QBF {

		public final List<QBF> subformulas;

		private MultiaryOperator(List<QBF> subformulas) {
			if (subformulas == null || subformulas.size() < 2)
				throw new IllegalArgumentException("missing subformulas");

			this.subformulas = Collections.unmodifiableList(subformulas);
		}
	}

	/**
	 * A conjunction.
	 *
	 * @author phlo
	 */
	public static final class And extends MultiaryOperator {

		private final int hash;

		public void accept(
			Consumer<True> t,
			Consumer<False> f,
			Consumer<Variable> var,
			Consumer<Not> not,
			Consumer<And> and,
			Consumer<Or> or,
			Consumer<ForAll> forall,
			Consumer<Exists> exists
		) { and.accept(this); }

		public <T> T apply(
			Function<True, T> t,
			Function<False, T> f,
			Function<Variable, T> var,
			Function<Not, T> not,
			Function<And, T> and,
			Function<Or, T> or,
			Function<ForAll, T> forall,
			Function<Exists, T> exists
		) { return and.apply(this); }

		public And(List<QBF> subformulas) {
			super(subformulas);
			this.hash = this.hash();
		}

		public And(QBF... subformulas) { this(Arrays.asList(subformulas)); }
	}

	/**
	 * A disjunction.
	 *
	 * @author phlo
	 */
	public static final class Or extends MultiaryOperator {

		private final int hash;

		public void accept(
			Consumer<True> t,
			Consumer<False> f,
			Consumer<Variable> var,
			Consumer<Not> not,
			Consumer<And> and,
			Consumer<Or> or,
			Consumer<ForAll> forall,
			Consumer<Exists> exists
		) { or.accept(this); }

		public <T> T apply(
			Function<True, T> t,
			Function<False, T> f,
			Function<Variable, T> var,
			Function<Not, T> not,
			Function<And, T> and,
			Function<Or, T> or,
			Function<ForAll, T> forall,
			Function<Exists, T> exists
		) { return or.apply(this); }

		public Or(List<QBF> subformulas) {
			super(subformulas);
			this.hash = this.hash();
		}

		public Or(QBF... subformulas) { this(Arrays.asList(subformulas)); }
	}

	/**
	 * Base class for quantifier nodes.
	 *
	 * @author phlo
	 *
	 */
	public static abstract class Quantifier extends UnaryOperator {

		public final Set<String> variables;

		private Quantifier(QBF subformula, Set<String> variables) {
			super(subformula);

			if (variables == null || variables.isEmpty())
				throw new IllegalArgumentException("missing variable");

			this.variables = Collections.unmodifiableSet(variables);
		}
	}

	/**
	 * A universal quantifier.
	 *
	 * @author phlo
	 */
	public static final class ForAll extends Quantifier {

		private final int hash;

		public void accept(
			Consumer<True> t,
			Consumer<False> f,
			Consumer<Variable> var,
			Consumer<Not> not,
			Consumer<And> and,
			Consumer<Or> or,
			Consumer<ForAll> forall,
			Consumer<Exists> exists
		) { forall.accept(this); }

		public <T> T apply(
			Function<True, T> t,
			Function<False, T> f,
			Function<Variable, T> var,
			Function<Not, T> not,
			Function<And, T> and,
			Function<Or, T> or,
			Function<ForAll, T> forall,
			Function<Exists, T> exists
		) { return forall.apply(this); }

		public ForAll(QBF subformula, Set<String> variables) {
			super(
				subformula == null ? null : subformula.apply(
					t -> t, f -> f, var -> var,
					not -> not, and -> and, or -> or,
					forall -> forall == null ? null : forall.subformula,
					exists -> exists),
				subformula == null ? null : subformula.apply(
					t -> variables, f -> variables, var -> variables,
					not -> variables, and -> variables, or -> variables,
					forall -> {
						HashSet<String> vars = new HashSet<>(variables);
						vars.addAll(forall.variables);
						return Collections.unmodifiableSet(vars);
					},
					exists -> variables));
			this.hash = this.hash();
		}

		public ForAll(QBF subformula, String... variables) {
			this(subformula, new HashSet<String>(Arrays.asList(variables)));
		}
	}

	/**
	 * An existential quantifier.
	 *
	 * @author phlo
	 */
	public static final class Exists extends Quantifier {

		private final int hash;

		public void accept(
			Consumer<True> t,
			Consumer<False> f,
			Consumer<Variable> var,
			Consumer<Not> not,
			Consumer<And> and,
			Consumer<Or> or,
			Consumer<ForAll> forall,
			Consumer<Exists> exists
		) { exists.accept(this); }

		public <T> T apply(
			Function<True, T> t,
			Function<False, T> f,
			Function<Variable, T> var,
			Function<Not, T> not,
			Function<And, T> and,
			Function<Or, T> or,
			Function<ForAll, T> forall,
			Function<Exists, T> exists
		) { return exists.apply(this); }

		public Exists(QBF subformula, Set<String> variables) {
			super(
				subformula == null ? null : subformula.apply(
					t -> t, f -> f, var -> var,
					not -> not, and -> and, or -> or,
					forall -> forall,
					exists -> exists == null ? null : exists.subformula),
				subformula == null ? null : subformula.apply(
					t -> variables, f -> variables, var -> variables,
					not -> variables, and -> variables, or -> variables,
					forall -> variables,
					exists -> {
						HashSet<String> vars = new HashSet<>(variables);
						vars.addAll(exists.variables);
						return Collections.unmodifiableSet(vars);
					}));
			this.hash = this.hash();
		}

		public Exists(QBF subformula, String... variables) {
			this(subformula, new HashSet<String>(Arrays.asList(variables)));
		}
	}

	// TODO: include (more) predicates? static or not static?
	/**
	 * Tests if the given instance is a boolean constant.
	 */
	public static final Predicate<QBF> isConstant = formula ->
		formula.apply(
			t -> true, f -> true,
			var -> false, not -> false, and -> false,
			or -> false, forall -> false, exists -> false
		);
	/**
	 * Tests if this is a boolean constant.
	 *
	 * @return {@code true} if this is either {@link True} or {@link False}
	 */
	public boolean isConstant() { return isConstant.test(this); }

	/**
	 * Tests if the given instance is a quantifier.
	 */
	public static final Predicate<QBF> isQuantifier = formula ->
		formula.apply(
			t -> false, f -> false, var -> false,
			not -> false, and -> false, or -> false,
			forall -> true,
			exists -> true
		);
	/**
	 * Tests if this is a quantifier.
	 *
	 * @return {@code true} if this is either {@link ForAll} or {@link Exists}
	 */
	public boolean isQuantifier() { return isQuantifier.test(this); }

	/**
	 * Tests if the given instance is a {@link ForAll universal quantifier}.
	 */
	public static final Predicate<QBF> isForAll = formula ->
		formula.apply(
			t -> false, f -> false, var -> false,
			not -> false, and -> false, or -> false,
			forall -> true,
			exists -> false
		);
	/**
	 * Tests if this is a {@link ForAll universal quantifier}.
	 *
	 * @return {@code true} if this is a {@link ForAll universal quantifier}
	 */
	public boolean isForAll() { return isForAll.test(this); }

	/**
	 * Tests if the given instance is a {@link Exists existential quantifier}.
	 */
	public static final Predicate<QBF> isExists = formula ->
		formula.apply(
			t -> false, f -> false, var -> false,
			not -> false, and -> false, or -> false,
			forall -> false,
			exists -> true
		);
	/**
	 * Tests if this is a {@link Exists existential quantifier}.
	 *
	 * @return {@code true} if this is a {@link Exists existential quantifier}
	 */
	public boolean isExists() { return isExists.test(this); }

	/**
	 * Tests if the given instance is a {@link Variable variable}.
	 */
	public static final Predicate<QBF> isVariable = formula ->
		formula.apply(
			t -> false, f -> false,
			var -> true,
			not -> false,
			and -> false, or -> false, forall -> false, exists -> false
		);
	/**
	 * Tests if this is a {@link Variable variable}.
	 *
	 * @return {@code true} if this is a {@link Variable variable}
	 */
	public boolean isVariable() { return isVariable.test(this); }

	/**
	 * Tests if the given instance is a literal.
	 */
	public static final Predicate<QBF> isLiteral = formula ->
		formula.apply(
			t -> false, f -> false,
			var -> true,
			not -> not.subformula.isLiteral(),
			and -> false, or -> false, forall -> false, exists -> false
		);
	/**
	 * Tests if this is a literal.
	 *
	 * @return {@code true} if this is a literal.
	 */
	public boolean isLiteral() { return isLiteral.test(this); }

	/**
	 * Tests if the given instance is a {@link Not negation}.
	 */
	public static final Predicate<QBF> isNegation = formula ->
		formula.apply(
			t -> false, f -> false, var -> false,
			not -> true,
			and -> false, or -> false, forall -> false, exists -> false
		);
	/**
	 * Tests if this is a {@link Not negation}.
	 *
	 * @return {@code true} if this is a {@link Not negation}
	 */
	public boolean isNegation() { return isNegation.test(this); }

	/**
	 * Tests if the given instance is a {@link And conjunction}.
	 */
	public static final Predicate<QBF> isAnd = formula ->
		formula.apply(
			t -> false, f -> false, var -> false, not -> false,
			and -> true,
			or -> false, forall -> false, exists -> false
		);
	/**
	 * Tests if this is a {@link And conjunction}.
	 *
	 * @return {@code true} if this is a {@link And conjunction}
	 */
	public boolean isAnd() { return isAnd.test(this); }

	/**
	 * Tests if the given instance is a {@link Or disjunction}.
	 */
	public static final Predicate<QBF> isOr = formula ->
		formula.apply(
			t -> false, f -> false, var -> false, not -> false, and -> false,
			or -> true,
			forall -> false, exists -> false
		);
	/**
	 * Tests if this is a {@link Or disjunction}.
	 *
	 * @return {@code true} if this is a {@link Or disjunction}
	 */
	public boolean isOr() { return isOr.test(this); }

	/**
	 * Tests if the given instance is in conjunctive normal form.
	 */
	public static final Predicate<QBF> isCNF = formula ->
		formula.isAnd()
			&& ((And) formula).subformulas.stream().allMatch(f ->
				f.isLiteral()
				|| (f.isOr() && ((Or) f).subformulas.stream()
					.allMatch(QBF::isLiteral)));
	/**
	 * Tests if this formula is in conjunctive normal form.
	 *
	 * @return {@code true} if this is in conjunctive normal form
	 */
	public boolean isCNF() { return isCNF.test(this); }

	private static enum HashID {
		FALSE,
		TRUE,
		VARIABLE,
		NOT,
		AND,
		OR,
		FORALL,
		EXISTS
	}

	protected int hash() {
		return this.apply(
			t -> HashID.TRUE.ordinal(),
			f -> HashID.FALSE.ordinal(),
			var -> (var.name.hashCode() << 3) + HashID.VARIABLE.ordinal(),
			not -> (not.subformula.hashCode() << 3) + HashID.NOT.ordinal(),
			and -> (and.subformulas.hashCode() << 3) + HashID.AND.ordinal(),
			or -> (or.subformulas.hashCode() << 3) + HashID.OR.ordinal(),
			forall -> (Objects.hash(forall.subformula, forall.variables) << 3)
				+ HashID.FORALL.ordinal(),
			exists -> (Objects.hash(exists.subformula, exists.variables) << 3)
				+ HashID.EXISTS.ordinal());
	}

	public int hashCode() {
		return this.apply(
			t -> t.hash,
			f -> f.hash,
			var -> var.hash,
			not -> not.hash,
			and -> and.hash,
			or -> or.hash,
			forall -> forall.hash,
			exists -> exists.hash);
	}

	public boolean equals(Object o) {
		if (this == o) return true;
		if (!(o instanceof QBF)) return false;

		return hashCode() == o.hashCode();
	}

//	public boolean equals(Object o) {
//		if (this == o) return true;
//		if (!(o instanceof QBF)) return false;
//
//		final QBF other = (QBF) o;
//
//		Function<True, Boolean> noTrue = x -> false;
//		Function<False, Boolean> noFalse = x -> false;
//		Function<Literal, Boolean> noLiteral = x -> false;
//		Function<Not, Boolean> noNot = x -> false;
//		Function<And, Boolean> noAnd = x -> false;
//		Function<Or, Boolean> noOr = x -> false;
//		Function<ForAll, Boolean> noForall = x -> false;
//		Function<Exists, Boolean> noExists = x -> false;
//
//		return this.apply(
//			(True t) -> t == True,
//			(False f) -> f == False,
//			(Literal lit1) ->
//				other.apply(
//					noTrue, noFalse,
//					(Literal lit2) -> lit1.variable.equals(lit2.variable),
//					noNot, noAnd, noOr, noForall, noExists),
//			(Not not1) ->
//				other.apply(
//					noTrue, noFalse, noLiteral,
//					(Not not2) -> not1.subformula.equals(not2.subformula),
//					noAnd, noOr, noForall, noExists),
//			(And and1) ->
//				other.apply(
//					noTrue, noFalse, noLiteral, noNot,
//					(And and2) -> and1.subformulas.equals(and2.subformulas),
//					noOr, noForall, noExists),
//			(Or or1) ->
//				other.apply(
//					noTrue, noFalse, noLiteral, noNot, noAnd,
//					(Or or2) -> or1.subformulas.equals(or2.subformulas),
//					noForall, noExists),
//			(ForAll forall1) ->
//				other.apply(
//					noTrue, noFalse, noLiteral, noNot, noAnd, noOr,
//					(ForAll forall2) ->
//						forall1.variables.equals(forall2.variables) &&
//						forall1.subformula.equals(forall2.subformula),
//					noExists),
//			(Exists exists1) ->
//				other.apply(
//					noTrue, noFalse, noLiteral, noNot, noAnd, noOr, noForall,
//					(Exists exists2) ->
//						exists1.variables.equals(exists2.variables) &&
//						exists1.subformula.equals(exists2.subformula))
//		);
//	}

	/**
	 * Tree traversal order.
	 *
	 * @author phlo
	 */
	public enum Traverse { PreOrder, PostOrder };

	/**
	 * Streams the formula's nodes in a depth-first search manner.
	 *
	 * @param traversal pre- or post-order
	 * @return a sequential Stream over all nodes in this formula
	 */
	public Stream<QBF> stream(Traverse traversal) {
		Stream<QBF> childStream =
			this.apply(
				(True t) -> Stream.empty(),
				(False f) -> Stream.empty(),
				(Variable var) -> Stream.empty(),
				(Not not) -> not.subformula.stream(traversal),
				(And and) ->
					and.subformulas.stream()
						.flatMap(f -> f.stream(traversal)),
				(Or or) ->
					or.subformulas.stream()
						.flatMap(f -> f.stream(traversal)),
				(ForAll forall) -> forall.subformula.stream(traversal),
				(Exists exists) -> exists.subformula.stream(traversal));

		return
			traversal == Traverse.PreOrder
				? Stream.concat(Stream.of(this), childStream)
				: Stream.concat(childStream, Stream.of(this));
	}

	/**
	 * Streams all variables in this formula.
	 *
	 * @return a sequential Stream over all variables in this formula
	 */
	public Stream<String> streamVariables() {
		return this.apply(
			(True t) -> Stream.empty(),
			(False f) -> Stream.empty(),
			(Variable var) -> Stream.of(var.name),
			(Not not) ->
				not.subformula.streamVariables(),
			(And and) ->
				and.subformulas.stream().flatMap(QBF::streamVariables),
			(Or or) ->
				or.subformulas.stream().flatMap(QBF::streamVariables),
			(ForAll forall) ->
				Stream.concat(
					forall.variables.stream(),
					forall.subformula.streamVariables()),
			(Exists exists) ->
				Stream.concat(
					exists.variables.stream(),
					exists.subformula.streamVariables())
		);
	}

	/**
	 * Streams all free variables in this formula.
	 *
	 * @return a sequential Stream over all free variables in this formula
	 */
	public Stream<String> streamFreeVariables() {
		return this.apply(
			(True t) -> Stream.empty(),
			(False f) -> Stream.empty(),
			(Variable var) -> Stream.of(var.name),
			(Not not) ->
				not.subformula.streamFreeVariables(),
			(And and) ->
				and.subformulas.stream().flatMap(QBF::streamFreeVariables),
			(Or or) ->
				or.subformulas.stream().flatMap(QBF::streamFreeVariables),
			(ForAll forall) ->
				forall.subformula.streamFreeVariables()
					.filter(v -> !forall.variables.contains(v)),
			(Exists exists) ->
				exists.subformula.streamFreeVariables()
					.filter(v -> !exists.variables.contains(v))
		);
	}

	/**
	 * Streams all bound variables in this formula.
	 *
	 * @return a sequential Stream over all bound variables in this formula
	 */
	public Stream<String> streamBoundVariables() {
		return this.apply(
			(True t) -> Stream.empty(),
			(False f) -> Stream.empty(),
			(Variable var) -> Stream.empty(),
			(Not not) ->
				not.subformula.streamBoundVariables(),
			(And and) ->
				and.subformulas.stream()
					.parallel()
					.flatMap(QBF::streamBoundVariables),
			(Or or) ->
				or.subformulas.stream()
					.parallel()
					.flatMap(QBF::streamBoundVariables),
			(ForAll forall) ->
				Stream.concat(
					forall.variables.stream(),
					forall.subformula.streamBoundVariables()),
			(Exists exists) ->
				Stream.concat(
					exists.variables.stream(),
					exists.subformula.streamBoundVariables())
		);
	}

	/**
	 * Streams this formula's prefix.
	 *
	 * @return a sequential Stream over this formula's prefix
	 */
	public Stream<Quantifier> streamPrefix() {
		return this.apply(
			(True t) -> Stream.empty(),
			(False f) -> Stream.empty(),
			(Variable var) -> Stream.empty(),
			(Not not) -> Stream.empty(),
			(And and) -> Stream.empty(),
			(Or or) -> Stream.empty(),
			(ForAll forall) -> Stream.concat(
				Stream.of(forall),
				forall.subformula.streamPrefix()),
			(Exists exists) -> Stream.concat(
				Stream.of(exists),
				exists.subformula.streamPrefix())
		);
	}

//	public List<QBF> getPrefix() { return streamPrefix().collect(Collectors.toList()); }

	// qpath = head node of qpath prefix (get full path with streamPrefix)
	/**
	 * Streams this formula's q-paths.
	 * <p>
	 * The set of q-paths of Φ, {@code qpaths(Φ)}, is defined inductively as
	 * follows:
	 * <ol>
	 * <li>If {@code Φ} is a variable, {@link True} or {@link False}, then
	 * {@code qpaths(Φ) = {}}.
	 * <li>If {@code Φ = ¬Φ1}, then
	 * {@code qpaths(Φ) = {Q̄1 p1 ... Q̄k pk | Q1 p1 ... Qk pk ∈ qpaths(Φ1)}}.
	 * <li>If {@code Φ = Φ1 ◦ Φ 2 (◦ ∈ {∧, ∨})}, then
	 * {@code qpaths(Φ) = qpaths(Φ1) ∪ qpaths(Φ2)}.
	 * <li>If {@code Φ = Φ1 → Φ2}, then
	 * {@code qpaths(Φ) = qpaths(¬Φ1 ∨ Φ2)}.
	 * <li>If {@code Φ = Qp Φ1 (Q ∈ {∀, ∃})}, then
	 * {@code qpaths(Φ) = {Q ps | s ∈ qpaths(Φ1)}}.
	 * </ol>
	 * <p>
	 * <b>Note:</b> only the q-path's head node is returned, use
	 * {@link QBF#streamPrefix} to get the full path.
	 *
	 * @return a sequential Stream over this formula's q-paths
	 */
	public Stream<QBF> streamQPaths() {
		return this.apply(
			(True t) -> Stream.empty(),
			(False f) -> Stream.empty(),
			(Variable var) -> Stream.empty(),
			(Not not) -> Stream.empty(),
			(And and) ->
				and.subformulas.stream()
					.flatMap(f -> f.streamQPaths()),
			(Or or) ->
				or.subformulas.stream()
					.flatMap(f -> f.streamQPaths()),
			(ForAll forall) -> {
				List<QBF> childPaths =
					forall.subformula.streamQPaths()
						.collect(Collectors.toList());
				return childPaths.isEmpty()
					? Stream.of(forall)
					: childPaths.stream()
						.map(f -> new ForAll(f, forall.variables));
			},
			(Exists exists) -> {
				List<QBF> childPaths =
					exists.subformula.streamQPaths()
						.collect(Collectors.toList());
				return childPaths.isEmpty()
					? Stream.of(exists)
					: childPaths.stream()
						.map(f -> new Exists(f, exists.variables));
			}
		);
	}

	/**
	 * Gets a list of this formula's q-paths.
	 * <p>
	 * The set of q-paths of Φ, {@code qpaths(Φ)}, is defined inductively as
	 * follows:
	 * <ol>
	 * <li>If {@code Φ} is a variable, {@link True} or {@link False}, then
	 * {@code qpaths(Φ) = {}}.
	 * <li>If {@code Φ = ¬Φ1}, then
	 * {@code qpaths(Φ) = {Q̄1 p1 ... Q̄k pk | Q1 p1 ... Qk pk ∈ qpaths(Φ1)}}.
	 * <li>If {@code Φ = Φ1 ◦ Φ 2 (◦ ∈ {∧, ∨})}, then
	 * {@code qpaths(Φ) = qpaths(Φ1) ∪ qpaths(Φ2)}.
	 * <li>If {@code Φ = Φ1 → Φ2}, then
	 * {@code qpaths(Φ) = qpaths(¬Φ1 ∨ Φ2)}.
	 * <li>If {@code Φ = Qp Φ1 (Q ∈ {∀, ∃})}, then
	 * {@code qpaths(Φ) = {Q ps | s ∈ qpaths(Φ1)}}.
	 * </ol>
	 * <p>
	 * <b>Note:</b> only the q-path's head node is returned, use
	 * {@link QBF#streamPrefix} to get the full path.
	 *
	 * @return a list of this formula's q-paths
	 */
	public List<QBF> getQPaths() {
		return streamQPaths().collect(Collectors.toList());
	}

	/**
	 * Gets a list of critical q-paths.
	 * <p>
	 * A critical path is defined by having the largest number of quantifier
	 * alterations.
	 *
	 * @param qpaths a list of q-paths
	 * @return a list of critical paths
	 */
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

		// leading quantifiers differ
		// prepend the first quantifier from each qpath to the other
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

	/**
	 * Gets the propositional skeleton of this formula.
	 * <p>
	 * The propositional skeleton of a QBF Φ is given by deleting all
	 * quantifiers in Φ.
	 *
	 * @return the propositional skeleton
	 */
	public QBF getSkeleton() {
		return this.apply(
			t -> t, f -> f, var -> var,
			(Not not) ->
				not.subformula.apply(
					t -> not, f -> not, var -> not,
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
	 * Unifies all sequences of equal quantifiers in the formula's prefix.
	 *
	 * Example: {@code ∀x: ∀y: ϕ → ∀x,y: ϕ}
	 *
	 * @return this {@link QBF} with purely alternating prefix
	 */
	@Deprecated
	public QBF unifyPrefix() {
		return this.apply(
			t -> t, f -> f, var -> var, not -> not, and -> and, or -> or,
			(ForAll forall1) -> forall1.subformula.apply(
				t -> forall1, f -> forall1, var -> forall1, not -> forall1, and -> forall1, or -> forall1,
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
				t -> exists1, f -> exists1, var -> exists1, not -> exists1, and -> exists1, or -> exists1,
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

	/**
	 * Negates the current formula.
	 *
	 * @return this {@link QBF}'s negation
	 */
	public QBF negate() {
		return this.apply(
			(True t) -> False,
			(False f) -> True,
			(Variable var) -> new Not(var),
			(Not not) -> not.subformula,
			(And and) -> new Not(and),
			(Or or) -> new Not(or),
			(ForAll forall) -> new Not(forall),
			(Exists exists) -> new Not(exists)
		);
	}

	/**
	 * Renames this formula's variables.
	 *
	 * @param variables a map of old to new variable names
	 * @return this {@link QBF} with renamed variables
	 */
	public QBF rename(Map<String, String> variables) {
		return this.apply(
			(True t) -> t,
			(False f) -> f,
			(Variable var) ->
				variables.containsKey(var.name)
					? new Variable(variables.get(var.name))
					: var,
			(Not not) -> new Not(not.subformula.rename(variables)),
			(And and) ->
				new And(
					and.subformulas.stream()
						.map(f -> f.rename(variables))
						.collect(Collectors.toList())),
			(Or or) ->
				new Or(
					or.subformulas.stream()
						.map(f -> f.rename(variables))
						.collect(Collectors.toList())),
			(ForAll forall) ->
				new ForAll(
					forall.subformula.rename(variables),
					forall.variables.stream()
						.map(v -> variables.containsKey(v)
							? variables.get(v)
							: v)
						.collect(Collectors.toSet())),
			(Exists exists) ->
				new Exists(
					exists.subformula.rename(variables),
					exists.variables.stream()
						.map(v -> variables.containsKey(v)
							? variables.get(v)
							: v)
						.collect(Collectors.toSet()))
		);
	}

	/**
	 * Produces a cleansed formula.
	 * <p>
	 * <ol>
	 *   <li>a variable is only quantified once
	 *   <li>a variable is either quantified or free
	 *   <li>quantifiers have all bound variables in their scope
	 *   <li>variables are renamed to integers in order of their occurrence,
	 *     starting from 1:
	 *     <ul>
	 *       <li>bound variables: {@code [1, #bound]}
	 *       <li>free variables: {@code ]#bound, #variables]}
	 *     </ul>
	 * </ol>
	 *
	 * @return the cleansed {@link QBF}
	 */
	public QBF cleanse() {
		int[] counter = {1};
		Map<String, String> bound = new HashMap<>();
		Map<String, String> free = new HashMap<>();

		// NOTE: local variables can't hold recursive lambdas - use Class instead
		class Cleaner {

			Function<Quantifier, QBF> cleanseQuantifier = q -> {
				Map<String, String> shadowed = new HashMap<>();
				for (String v : q.variables) {
					if (bound.containsKey(v))
						shadowed.put(v, bound.get(v));
					bound.put(v, null);
				}

				QBF subformula = new Cleaner(q.subformula).formula;

				Set<String> variables =	q.variables.stream()
					.map(bound::remove)
					.filter(Objects::nonNull)
					.collect(Collectors.toSet());

				bound.putAll(shadowed);

				return variables.isEmpty()
					? subformula
					: q.apply(
						f -> new ForAll(subformula, variables),
						e -> new Exists(subformula, variables));
			};

			final QBF formula;

			Cleaner(QBF formula) {
				this.formula = formula.apply(
					(True t) -> t,
					(False f) -> f,
					(Variable var) -> {
						if (bound.containsKey(var.name))
							bound.compute(var.name, (k, v) ->
								v == null ? Integer.toString(counter[0]++) : v);

						return new Variable(
							bound.containsKey(var.name)
								? bound.get(var.name)
								: free.containsKey(var.name)
									? free.get(var.name)
									: free.computeIfAbsent(var.name,
										v -> Integer.toString(counter[0]++)));
					},
					(Not not) -> new Not(new Cleaner(not.subformula).formula),
					(And and) ->
						new And(and.subformulas.stream()
							.map(f -> new Cleaner(f).formula)
							.collect(Collectors.toList())),
					(Or or) ->
						new Or(or.subformulas.stream()
							.map(f -> new Cleaner(f).formula)
							.collect(Collectors.toList())),
					(ForAll forall) -> cleanseQuantifier.apply(forall),
					(Exists exists) -> cleanseQuantifier.apply(exists)
				);
			}
		}

		return new Cleaner(this).formula;
	}

	/**
	 * Transforms this formula into negated normal form.
	 *
	 * @return this {@link QBF} in negated normal form
	 */
	public QBF toNNF() {
		return this.apply(
			(True t) -> t,
			(False f) -> f,
			(Variable var) -> var,
			(Not not) -> not.subformula.apply(
				(True t) -> False,
				(False f) -> True,
				(Variable var) -> not,
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
				(ForAll forall) ->
					new Exists(
						forall.subformula.negate().toNNF(),
						forall.variables),
				(Exists exists) ->
					new ForAll(
						exists.subformula.negate().toNNF(),
						exists.variables)
			),
			(And and) ->
				new And(
					and.subformulas.stream()
						.map(QBF::toNNF)
						.collect(Collectors.toList())),
			(Or or) ->
				new Or(
					or.subformulas.stream()
						.map(QBF::toNNF)
						.collect(Collectors.toList())),
			(ForAll forall) ->
				new ForAll(forall.subformula.toNNF(), forall.variables),
			(Exists exists) ->
				new Exists(exists.subformula.toNNF(), exists.variables)
		);
	}

	/**
	 * Transforms this formula into prenex normal form.
	 *
	 * @param strategy prenexing strategy
	 * @return this {@link QBF} in prenex normal form
	 */
	public QBF toPNF(PrenexingStrategy strategy) {
		return strategy.apply(this.toNNF());
	}

	/**
	 * Transforms this formula into prenex conjunctive normal form.
	 *
	 * @param strategy prenexing strategy
	 * @param encoder CNF encoder
	 * @return this {@link QBF} in prenex conjunctive normal form
	 */
	public QBF toPCNF(PrenexingStrategy strategy, CNFEncoder encoder) {
		return encoder.encode(toPNF(strategy));
	}

	/**
	 * Gets a String representation of the given quantifier.
	 *
	 * @param q a quantifier
	 * @return the quantifier's String representation
	 */
	public static String quantifierToString(Quantifier q) {
		return q.apply(
			forall -> "∀" + forall.variables.stream()
				.sorted()
				.collect(Collectors.joining(",")),
			exists -> "∃" + exists.variables.stream()
				.sorted()
				.collect(Collectors.joining(","))
		);
	}

	/**
	 * Gets a String representation of this formula's prefix.
	 *
	 * @return this formulas prefix as String
	 */
	public String prefixToString() {
		return streamPrefix()
			.map(QBF::quantifierToString)
			.collect(Collectors.joining(" "));
	}

	public String toString() {
		return this.apply(
			(True t) -> "TRUE",
			(False f) -> "FALSE",
			(Variable var) -> var.name.toString(),
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