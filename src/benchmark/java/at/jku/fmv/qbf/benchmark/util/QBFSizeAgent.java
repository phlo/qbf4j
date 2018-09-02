package at.jku.fmv.qbf.benchmark.util;

import java.lang.instrument.Instrumentation;
import java.util.IdentityHashMap;

import at.jku.fmv.qbf.QBF;
import at.jku.fmv.qbf.QBF.*;

public class QBFSizeAgent {

	private static Instrumentation instrumentation;

	public static void premain(String agentArgs, Instrumentation inst) {
		instrumentation = inst;
	}

	public static void agentmain(String agentArgs, Instrumentation inst) {
		premain(agentArgs, inst);
	}

	private static IdentityHashMap<QBF, Object> visited = new IdentityHashMap<>();

	public static int numInstances() {
		return visited.size();
	}

	public static void reset() {
		visited.clear();
	}

	public static long sizeOf(QBF formula) {
		if (visited.containsKey(formula))
			return 0l;
		else {
			visited.put(formula, null);
			return formula.apply(
					(True t) -> instrumentation.getObjectSize(t),
					(False f) -> instrumentation.getObjectSize(f),
					(Variable var) -> instrumentation.getObjectSize(var),
					(Not not) -> instrumentation.getObjectSize(not) + sizeOf(not.subformula),
					(And and) ->
						instrumentation.getObjectSize(and) +
						and.subformulas.stream().filter(x -> !visited.containsKey(x)).mapToLong(QBFSizeAgent::sizeOf).sum(),
//						and.subformulas.stream().mapToLong(QBFSizeAgent::sizeOf).sum(),
					(Or or) ->
						instrumentation.getObjectSize(or) +
						or.subformulas.stream().filter(x -> !visited.containsKey(x)).mapToLong(QBFSizeAgent::sizeOf).sum(),
//						or.subformulas.stream().mapToLong(QBFSizeAgent::sizeOf).sum(),
					(ForAll forall) ->
						instrumentation.getObjectSize(forall) +
						forall.variables.stream().mapToLong(instrumentation::getObjectSize).sum() +
						sizeOf(forall.subformula),
					(Exists exists) ->
						instrumentation.getObjectSize(exists) +
						exists.variables.stream().mapToLong(instrumentation::getObjectSize).sum() +
						sizeOf(exists.subformula));
		}
	}
}
