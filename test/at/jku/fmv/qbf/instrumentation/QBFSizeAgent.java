package at.jku.fmv.qbf.instrumentation;

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

//	private static long sizeOf(QBF formula) {
//		return formula.stream(Traverse.PostOrder).distinct().mapToLong(instrumentation::getObjectSize).sum();
//	}

	private static IdentityHashMap<QBF, Object> visited = new IdentityHashMap<>();

	public static int numInstances() {
		return visited.size();
	}

	public static void reset() {
		visited.clear();
	}

//	private static long sizeOfRecursive(QBF formula) {
//		if (visited.containsKey(formula))
//			return 0l;
//		else {
//			visited.put(formula, null);
//			return formula.apply(
//					(True t) -> instrumentation.getObjectSize(t),
//					(False f) -> instrumentation.getObjectSize(f),
//					(Literal lit) -> instrumentation.getObjectSize(lit),
//					(Not not) -> instrumentation.getObjectSize(not) + sizeOfRecursive(not.subformula),
//					(And and) ->
//						instrumentation.getObjectSize(and) +
//						and.subformulas.stream().mapToLong(QBFSizeAgent::sizeOfRecursive).sum(),
//					(Or or) ->
//						instrumentation.getObjectSize(or) +
//						or.subformulas.stream().mapToLong(QBFSizeAgent::sizeOfRecursive).sum(),
//					(ForAll forall) ->
//						instrumentation.getObjectSize(forall) +
//						forall.variables.stream().mapToLong(instrumentation::getObjectSize).sum() +
//						sizeOfRecursive(forall.subformula),
//					(Exists exists) ->
//						instrumentation.getObjectSize(exists) +
//						exists.variables.stream().mapToLong(instrumentation::getObjectSize).sum() +
//						sizeOfRecursive(exists.subformula));
//		}
//	}
//
//	public static long sizeOf(QBF formula) {
//		visited.clear();
//		return sizeOfRecursive(formula);
//	}

//	public static long sizeOf(QBF formula) {
//		long size = 0;
//
//		Stack<QBF> stack = new Stack<>();
//		IdentityHashMap<QBF, Object> visited = new IdentityHashMap<>();
//
//		stack.push(formula);
//		while (!stack.isEmpty()) {
//			QBF cur = stack.pop();
//
//			if (!visited.containsKey(cur)) {
//				visited.put(cur, null);
//
//				size += cur.apply(
//					(True t) -> instrumentation.getObjectSize(t),
//					(False f) -> instrumentation.getObjectSize(f),
//					(Literal lit) -> instrumentation.getObjectSize(lit),
//					(Not not) -> {
//						stack.push(not.subformula);
//						return instrumentation.getObjectSize(not);
//					},
//					(And and) -> {
//						and.subformulas.stream().forEach(stack::push);
//						return instrumentation.getObjectSize(and);
//					},
//					(Or or) -> {
//						or.subformulas.stream().forEach(stack::push);
//						return instrumentation.getObjectSize(or);
//					},
//					(ForAll forall) -> {
//						stack.push(forall.subformula);
//						return
//							instrumentation.getObjectSize(forall) +
//							forall.variables.stream().mapToLong(instrumentation::getObjectSize).sum();
//					},
//					(Exists exists) -> {
//						stack.push(exists.subformula);
//						return
//							instrumentation.getObjectSize(exists) +
//							exists.variables.stream().mapToLong(instrumentation::getObjectSize).sum();
//					});
//			}
//		}
//
//		return size;
//	}

	public static long sizeOf(QBF formula) {
		if (visited.containsKey(formula))
			return 0l;
		else {
			visited.put(formula, null);
			return formula.apply(
					(True t) -> instrumentation.getObjectSize(t),
					(False f) -> instrumentation.getObjectSize(f),
					(Literal lit) -> instrumentation.getObjectSize(lit),
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

//	private static Stream<QBF> successors(QBF formula) {
//		return formula.apply(
//			t -> Stream.empty(),
//			f -> Stream.empty(),
//			lit -> Stream.empty(),
//			not -> Stream.of(not.subformula),
//			and -> and.subformulas.stream(),
//			or -> or.subformulas.stream(),
//			forall -> Stream.of(forall.subformula),
//			exists -> Stream.of(exists.subformula));
//	}
//
//	public static long sizeOf(QBF formula) {
//		long size = 0;
//		Set<QBF> visited = new HashSet<>();
//		Stack<QBF> stack = new Stack<>();
//
//		stack.push(formula);
//
//		while (!stack.isEmpty()) {
//			QBF node = stack.pop();
//
//			size += instrumentation.getObjectSize(node);
//
//			successors(node).filter(f -> !visited.contains(f)).forEach(f -> {
//				visited.add(f);
//				stack.push(f);
//			});
//		}
//
//		return size;
//	}
}