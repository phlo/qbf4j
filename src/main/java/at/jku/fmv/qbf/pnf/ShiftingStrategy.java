package at.jku.fmv.qbf.pnf;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import at.jku.fmv.qbf.QBF;
import at.jku.fmv.qbf.QBF.*;

/**
 * Abstract class for strategies based on quantifier shifting.
 *
 * @author phlo
 */
public abstract class ShiftingStrategy implements PrenexingStrategy {

	/**
	 * Selects the critical q-path to use.
	 *
	 * @param criticalPaths a list of critical q-paths
	 * @return critical q-path used to assemble the prefix
	 */
	abstract QBF selectCriticalPath(List<QBF> criticalPaths);

	/**
	 * Gets the prefix variable ordering.
	 *
	 * @param criticalPath a critical q-path
	 * @param qpaths a list of q-paths
	 * @param skeleton a propositional skeleton
	 * @return a list of prefix variable sets
	 */
	abstract List<Set<String>> getVariableOrdering(
		QBF criticalPath,
		List<QBF> qpaths,
		QBF skeleton);

	private static List<Set<String>> getVariableOrdering(QBF qpath) {
		return qpath.streamPrefix()
			.map(f -> f.apply(
				forall -> forall.variables,
				exists -> exists.variables))
			.collect(Collectors.toList());
	}

	private static List<Set<String>> getVariableOrdering(List<QBF> qpaths) {

		assert qpaths.stream()
			.mapToLong(qp -> qp.streamPrefix().count())
			.distinct()
			.count() == 1;

		return qpaths.stream()
			.map(qp -> getVariableOrdering(qp))
			.collect(Collector.of(
				() -> qpaths.get(0).streamPrefix()
					.map(f -> new HashSet<String>())
					.collect(Collectors.toList()),
				(full, partial) -> IntStream.range(0, full.size())
					.forEach(i -> full.get(i).addAll(partial.get(i))),
				(l1, l2) -> {
					IntStream.range(0, l1.size())
						.forEach(i -> l1.get(i).addAll(l2.get(i)));
					return l1;
				}));
	}

	private static QBF assemble(
		QBF qpath,
		List<Set<String>> variableOrdering,
		QBF skeleton
	) {
		assert qpath.streamPrefix().count() == variableOrdering.size();

		return qpath.apply(
			t -> skeleton, f -> skeleton, var -> skeleton,
			not -> skeleton, and -> skeleton, or -> skeleton,
			forall -> {
				Set<String> variables = variableOrdering.remove(0);
				return new ForAll(
					assemble(forall.subformula, variableOrdering, skeleton),
					variables);
			},
			exists -> {
				Set<String> variables = variableOrdering.remove(0);
				return new Exists(
					assemble(exists.subformula, variableOrdering, skeleton),
					variables);
			}
		);
	}

	private QBF apply(List<QBF> qpaths, QBF skeleton) {

		// single quantified subformula
		if (qpaths.size() == 1)
			return assemble(
				qpaths.get(0),
				getVariableOrdering(qpaths.get(0)),
				skeleton);

		List<QBF> criticalPaths = QBF.getCriticalPaths(qpaths);

		// all qpaths equal (single critical path and all of equal length)
		if (criticalPaths.size() == 1
			&& qpaths.stream()
				.mapToLong(qp -> qp.streamPrefix().count())
				.distinct()
				.count() == 1)
			return assemble(
				qpaths.get(0),
				getVariableOrdering(qpaths),
				skeleton);

		// qpaths differ
		QBF criticalPath = selectCriticalPath(criticalPaths);
		return assemble(
			criticalPath,
			getVariableOrdering(criticalPath, qpaths, skeleton),
			skeleton);
	}

	public QBF apply(QBF formula) {
		List<QBF> qpaths = formula.getQPaths();

		// propositional formula
		if (qpaths.isEmpty())
			return formula;

		QBF skeleton = formula.getSkeleton();

		return apply(qpaths, skeleton);
	}
}
