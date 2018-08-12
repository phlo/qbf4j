package at.jku.fmv.qbf.pnf;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import at.jku.fmv.qbf.QBF;
import at.jku.fmv.qbf.QBF.*;

/**
 * Abstract class for strategies based on simple variable reordering.
 *
 * @author phlo
 */
public abstract class SimpleUpDownStrategy extends ShiftingStrategy {

	/**
	 * Selects a universally quantified variable for the current prefix
	 * element.
	 *
	 * @param numQPath remaining q-path elements
	 * @param numCritical remaining critical path (prefix) elements
	 * @return {@code true} if the variable should be selected
	 */
	abstract boolean selectForAll(long numQPath, long numCritical);

	/**
	 * Selects a existentially quantified variable for the current prefix
	 * element.
	 *
	 * @param numQPath remaining q-path elements
	 * @param numCritical remaining critical path (prefix) elements
	 * @return {@code true} if the variable should be selected
	 */
	abstract boolean selectExists(long numQPath, long numCritical);

	// prefer existentially quantified prefixes
	QBF selectCriticalPath(List<QBF> criticalPaths) {
		return criticalPaths.stream()
			.filter(QBF::isExists)
			.findAny()
			.orElse(criticalPaths.get(0));
	}

	List<Set<String>> getVariableOrdering(
		QBF criticalPath,
		List<QBF> qpaths,
		QBF skeleton
	) {
		Map<QBF, Set<String>> varsPerQuant = qpaths.stream()
			.flatMap(qp -> qp.streamPrefix())
			.collect(Collectors.toMap(
				(QBF qp) -> qp,
				(QBF qp) -> qp.apply(
					(ForAll f) -> f.variables.stream(),
					(Exists e) -> e.variables.stream())
					.collect(Collectors.toSet())));

		long[] numCritical = new long[1];
		numCritical[0] = criticalPath.streamPrefix().count();

		return criticalPath.streamPrefix()
			.map(cp -> {
				Set<String> critical = cp.apply(
					f -> f.variables,
					e -> e.variables);
				Set<String> selected = qpaths.stream()
					.filter(qp ->
						cp.isForAll() ?
							qp.isForAll() :
							qp.isExists())
					.flatMap(qp -> {
						long numQPath = qp.streamPrefix().count();
						Set<String> vars = varsPerQuant.get(qp);
						Set<String> merged = qp.apply(
							f -> vars.stream()
								.filter(v ->
									critical.contains(v) ||
									selectForAll(numQPath, numCritical[0])),
							e -> vars.stream()
								.filter(v ->
									critical.contains(v) ||
									selectExists(numQPath, numCritical[0])))
							.collect(Collectors.toSet());

						vars.removeAll(merged);

						if (vars.isEmpty())
							qpaths.set(
								qpaths.indexOf(qp),
								qp.apply(
									f -> f.subformula,
									e -> e.subformula));

						return merged.stream();
					})
					.collect(Collectors.toSet());

				qpaths.removeAll(
					qpaths.stream()
						.filter(QBF.isQuantifier.negate())
						.collect(Collectors.toSet()));

				numCritical[0]--;
				return selected;
			})
			.collect(Collectors.toList());
	}
}
