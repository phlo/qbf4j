package at.jku.fmv.qcir;

import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

public abstract class QBF {

	private QBF() {}

	public abstract <T> T apply(Function<Literal, T> lit,
								Function<Not, T> not,
								Function<And, T> and,
								Function<Or, T> or,
								Function<ForAll, T> forall,
								Function<Exists, T> exists);

//	public static final class Variable {
//		public final String name;
//		public Variable(String name) { this.name = name; }
//
//		public String toString() {
//			return name;
//		}
//	}

	public static final class Literal extends QBF {
		public final String variable;

		public <T> T apply(	Function<Literal, T> lit,
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

		public <T> T apply(	Function<Literal, T> lit,
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

		public <T> T apply(	Function<Literal, T> lit,
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

		public <T> T apply(	Function<Literal, T> lit,
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
		public final List<String> variables;

		public <T> T apply(	Function<Literal, T> lit,
							Function<Not, T> not,
							Function<And, T> and,
							Function<Or, T> or,
							Function<ForAll, T> forall,
							Function<Exists, T> exists ) { return forall.apply(this); }

		public ForAll(QBF subformula, List<String> variables) {
			if (subformula == null)
				throw new IllegalArgumentException("missing subformula");
			if (variables == null || variables.isEmpty())
				throw new IllegalArgumentException("missing variable");

			this.subformula = subformula;
			this.variables = variables;
		}

		public ForAll(QBF subformula, String... variables) { this(subformula, Arrays.asList(variables)); }
	}

	public static final class Exists extends QBF {
		public final QBF subformula;
		public final List<String> variables;

		public <T> T apply(	Function<Literal, T> lit,
							Function<Not, T> not,
							Function<And, T> and,
							Function<Or, T> or,
							Function<ForAll, T> forall,
							Function<Exists, T> exists ) { return exists.apply(this); }

		public Exists(QBF subformula, List<String> variables) {
			if (subformula == null)
				throw new IllegalArgumentException("missing subformula");
			if (variables == null || variables.isEmpty())
				throw new IllegalArgumentException("missing variable");

			this.subformula = subformula;
			this.variables = variables;
		}

		public Exists(QBF subformula, String... variables) { this(subformula, Arrays.asList(variables)); }
	}

	// TODO: static method for parsing a QCIR file

	public boolean equals(Object o) {
		if (this == o) return true;
		if (!(o instanceof QBF)) return false;

		final QBF other = (QBF) o;

		Function<Literal, Boolean> noLiteral = x -> false;
		Function<Not, Boolean> noNot = x -> false;
		Function<And, Boolean> noAnd = x -> false;
		Function<Or, Boolean> noOr = x -> false;
		Function<ForAll, Boolean> noForall = x -> false;
		Function<Exists, Boolean> noExists = x -> false;

		return this.apply(
			(Literal lit1) ->
				other.apply(
					(Literal lit2) -> lit1.variable.equals(lit2.variable),
					noNot, noAnd, noOr, noForall, noExists),
			(Not not1) ->
				other.apply(
					noLiteral,
					(Not not2) -> not1.subformula.equals(not2.subformula),
					noAnd, noOr, noForall, noExists),
			(And and1) ->
				other.apply(
					noLiteral, noNot,
					(And and2) -> and1.subformulas.equals(and2.subformulas),
					noOr, noForall, noExists),
			(Or or1) ->
				other.apply(
					noLiteral, noNot, noAnd,
					(Or or2) -> or1.subformulas.equals(or2.subformulas),
					noForall, noExists),
			(ForAll forall1) ->
				other.apply(
					noLiteral, noNot, noAnd, noOr,
					(ForAll forall2) ->
						forall1.variables.equals(forall2.variables) &&
						forall1.subformula.equals(forall2.subformula),
					noExists),
			(Exists exists1) ->
				other.apply(
					noLiteral, noNot, noAnd, noOr, noForall,
					(Exists exists2) ->
						exists1.variables.equals(exists2.variables) &&
						exists1.subformula.equals(exists2.subformula))
		);
	}

	public QBF toNNF() {
		return this.apply(
			(Literal lit) -> lit,
			(Not not) -> not.subformula.apply(
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
			(And and) -> new And(and.subformulas.stream().map(f -> f.toNNF()).collect(Collectors.toList())),
			(Or or) -> new Or(or.subformulas.stream().map(f -> f.toNNF()).collect(Collectors.toList())),
			(ForAll forall) -> new ForAll(forall.subformula.toNNF(), forall.variables),
			(Exists exists) -> new Exists(exists.subformula.toNNF(), exists.variables)
		);
	}

	public QBF toPNF() {
		return this.apply(
			(Literal lit) -> lit,
			(Not not) -> not.subformula.apply(
				(Literal lit) -> not,
				(Not notpossible) -> notpossible,
				(And and) -> and.toPNF(),
				(Or or) -> or.toPNF(),
				(ForAll forall) -> new Exists(forall.subformula.negate(), forall.variables),
				(Exists exists) -> new ForAll(exists.subformula.negate(), exists.variables)
			),
			(And and) -> new And(and.subformulas.stream().map(f -> f.toNNF()).collect(Collectors.toList())),
			(Or or) -> new Or(or.subformulas.stream().map(f -> f.toNNF()).collect(Collectors.toList())),
			(ForAll forall) -> new Not(forall),
			(Exists exists) -> new Not(exists)
		);
	}

	public QBF negate() {
		return this.apply(
			(Literal lit) -> new Not(lit),
			(Not not) -> not.subformula,
			(And and) -> new Not(and),
			(Or or) -> new Not(or),
			(ForAll forall) -> new Not(forall),
			(Exists exists) -> new Not(exists)
		);
	}

	public String toString() {
		return this.apply(
			(Literal lit) -> lit.variable.toString(),
			(Not not) -> "-" + not.subformula.toString(),
			(And and) ->
				"(" +
				and.subformulas
					.stream()
					.map(f -> f.toString())
					.collect(Collectors.joining(" ∧ ")) +
				")",
			(Or or) ->
				"(" +
				or.subformulas
					.stream()
					.map(f -> f.toString())
					.collect(Collectors.joining(" ∨ ")) +
				")",
			(ForAll forall) ->
				"∀ " +
				forall.variables
					.stream()
					.map(f -> f.toString())
					.collect(Collectors.joining(", ")) +
				": " + forall.subformula.toString(),
			(Exists exists) ->
				"∃ " +
				exists.variables
					.stream()
					.map(f -> f.toString())
					.collect(Collectors.joining(", ")) +
				": " + exists.subformula.toString()
		);
	}
}