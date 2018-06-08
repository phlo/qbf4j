package at.jku.fmv.qbf;

import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public abstract class QBF {

	public static QBF True = new True();
	public static QBF False = new False();

	private QBF() {}

	public abstract <T> T apply(Function<True, T> t,
								Function<False, T> f,
								Function<Literal, T> lit,
								Function<Not, T> not,
								Function<And, T> and,
								Function<Or, T> or,
								Function<ForAll, T> forall,
								Function<Exists, T> exists);

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
		public final List<String> variables;

		public <T> T apply(	Function<True, T> t,
							Function<False, T> f,
							Function<Literal, T> lit,
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

		public <T> T apply(	Function<True, T> t,
							Function<False, T> f,
							Function<Literal, T> lit,
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

	public QBF toPNF() {
		return null;
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
				"∀ " + forall.variables.stream().collect(Collectors.joining(", ")) +
				": " + forall.subformula.toString(),
			(Exists exists) ->
				"∃ " + exists.variables.stream().collect(Collectors.joining(", ")) +
				": " + exists.subformula.toString()
		);
	}
}