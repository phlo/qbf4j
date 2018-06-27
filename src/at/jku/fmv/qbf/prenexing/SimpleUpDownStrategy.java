package at.jku.fmv.qbf.prenexing;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import at.jku.fmv.qbf.QBF;
import at.jku.fmv.qbf.QBF.*;

public abstract class SimpleUpDownStrategy extends ShiftingStrategy {

	abstract boolean selectForAll(long numQPath, long numCritical);

	abstract boolean selectExists(long numQPath, long numCritical);

	// prefer existentially quantified prefixes
	QBF selectCriticalPath(List<QBF> criticalPaths) {
		return criticalPaths.stream()
			.filter(QBF::isExists)
			.findAny()
			.orElse(criticalPaths.get(0));
	}

	List<Set<String>> getVariableOrdering(QBF criticalPath, List<QBF> qpaths, QBF skeleton) {
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

//	List<Set<String>> getVariableOrdering(QBF criticalPath, List<QBF> qpaths, QBF skeleton) {
//
//		long criticalLength = criticalPath.streamPrefix().count();
//
//		// no qpath longer than the critical path
//		assert qpaths.stream().mapToLong(qp -> qp.streamPrefix().count()).max().getAsLong() <= criticalLength;
//
//		Predicate<QBF> pathFilter =
//			criticalPath.apply(
//				forall ->
//					QBF.isForAll
//						.and(qp ->
//							selectForAll(
//								qp.streamPrefix().filter(QBF.isForAll).count(),
//								forall.streamPrefix().filter(QBF.isForAll).count())
//							|| ((ForAll) qp).variables.containsAll(forall.variables)),
//				exists ->
//					QBF.isExists
//						.and(qp ->
//							selectExists(
//								qp.streamPrefix()
//									.filter(QBF.isExists)
//									.count(),
//								exists.streamPrefix().filter(QBF.isExists).count())
//							|| ((Exists) qp).variables.containsAll(exists.variables)));
//
//		Set<QBF> mergePaths = qpaths.stream().filter(pathFilter).collect(Collectors.toSet());
//
//		List<Set<String>> ordering = new ArrayList<Set<String>>();
//		ordering.add(
//			mergePaths.stream()
//				.flatMap(qp -> qp.apply(
//					forall -> forall.variables.stream(),
//					exists -> exists.variables.stream()))
//				.collect(Collectors.toSet()));
//
//		if (criticalLength > 1)
//			ordering.addAll(
//				getVariableOrdering(
//					criticalPath.apply(
//						forall -> forall.subformula,
//						exists -> exists.subformula),
//					qpaths.stream()
//						.flatMap(qp -> qp.streamPrefix().skip(mergePaths.contains(qp) ? 1 : 0).limit(1))
//						.collect(Collectors.toList()),
//					skeleton));
//
//		return ordering;
//	}

//	List<Set<String>> getVariableOrdering(QBF criticalPath, List<QBF> qpaths, QBF skeleton) {
//
//		// list of maps per qpath, containing the paths universally (key = true) and existentially quantified (key = false)
//		List<Map<Boolean, List<Set<String>>>> varsPerPath =
//			qpaths.stream()
//				.map(qp ->
//					qp.streamPrefix().collect(
//						Collectors.partitioningBy(
//							QBF::isForAll,
//							Collectors.mapping(
//								(QBF q) -> q.apply(
//									f -> f.variables.stream().collect(Collectors.toSet()),
//									e -> e.variables.stream().collect(Collectors.toSet())),
//								Collectors.toList()))))
//				.collect(Collectors.toList());
//
//		List<Set<String>> result = criticalPath.streamPrefix()
//			.map(cp -> {
//				boolean isForAll = cp.isForAll();
//				long numCritical = cp.streamPrefix().filter(QBF::isForAll).count();
//				return varsPerPath.stream()
//					.flatMap(qp -> {
//						if (qp.get(isForAll).isEmpty()) return Stream.empty();
//
//						Set<String> vars = qp.get(isForAll).get(0);
//						Set<String> merged = vars.stream()
//							.filter(var -> {
//								boolean isOnCriticalPath =  cp.apply(f -> f.variables.contains(var), e -> e.variables.contains(var));
//								boolean isSelected = isForAll ?
//									selectForAll(vars.size(), numCritical) :
//									selectExists(vars.size(), numCritical);
//								return isOnCriticalPath || isSelected;
//							})
//							.collect(Collectors.toSet());
//
//						vars.removeAll(merged);
//
//						if (vars.isEmpty())
//							qp.get(isForAll).remove(0);
//
//						return merged.stream();
//				})
//				.collect(Collectors.toSet());
//			})
//			.collect(Collectors.toList());
//
//		return result;
//	}
}
