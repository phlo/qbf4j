package at.jku.fmv.qbf.pcnf;

import java.util.HashMap;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import at.jku.fmv.qbf.QBF;
import at.jku.fmv.qbf.QBF.*;

/**
 * An optimized variant of Tseitin transformation [PG86].
 * <p>
 * Removes constraints of unnegated And/Or nodes.
 * <p>
 * [PG86] D. A. Plaisted and S. Greenbaum. A Structure-Preserving Clause Form
 * Translation. <i>Journal of Symbolic Computation</i>, 2(3): 293-304, 1986.
 *
 * @author phlo
 */
public class PG86 implements CNFEncoder {

	private final String prefix = "_pg";

	private HashMap<QBF, QBF> auxiliary = new HashMap<>();

	private int counter = 0;

	private QBF getAuxiliary(QBF gate) {
		return gate.isLiteral()
			? gate
			: auxiliary.containsKey(gate)
				? auxiliary.get(gate)
				: auxiliary.compute(
					gate,
					(k, v) ->
						new Variable(
							prefix + Integer.toString(counter++)));
	}

	private Stream<QBF> encodeSubformulas(MultiaryOperator gate) {
		return gate.subformulas.stream().flatMap(this::streamClauses);
	}

	private Stream<QBF> encodeAnd(And and) {
		Not aux = new Not(getAuxiliary(and));
		return and.subformulas.stream()
			.map(f -> new Or(aux, f));
	}

	private Stream<QBF> encodeOr(Or or) {
		Not aux = new Not(getAuxiliary(or));
		return Stream.of(
			new Or(
				Stream.concat(
					Stream.of(aux),
					or.subformulas.stream().map(this::getAuxiliary))
					.collect(Collectors.toList())));
	}

	private Stream<QBF> streamClauses(QBF skeleton) {
		Function<Quantifier, Stream<QBF>> illegalQuantifier = q -> {
			throw new IllegalArgumentException(
				"not a propositional formula");
		};

		return skeleton.apply(
			(True t) -> Stream.empty(),
			(False f) -> Stream.empty(),
			(Variable var) -> Stream.empty(),
			(Not not) -> Stream.empty(),
			(And and) -> Stream.concat(
				encodeAnd(and),
				encodeSubformulas(and)),
			(Or or) -> Stream.concat(
				encodeOr(or),
				encodeSubformulas(or)),
			(ForAll forall) -> illegalQuantifier.apply(forall),
			(Exists exists) -> illegalQuantifier.apply(exists)
		);
	}

	public List<QBF> getClauses(QBF skeleton) {
		try {
			List<QBF> clauses = streamClauses(skeleton)
				.collect(Collectors.toList());
			clauses.add(0, getAuxiliary(skeleton));
			return clauses;
		} catch (IllegalArgumentException e) {
			throw e;
		} finally {
			auxiliary.clear();
			counter = 0;
		}
	}
}
