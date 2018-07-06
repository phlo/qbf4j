package at.jku.fmv.qbf;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import at.jku.fmv.qbf.QBF;
import at.jku.fmv.qbf.QBF.*;

@DisplayName("QBF")
public class QBFTest {

	// variables
	static final String x1 = "x1";
	static final String x2 = "x2";
	static final String x3 = "x3";
	static final String x4 = "x4";

	// LNCS paper example: ∃p (∀q ∃r ∀s ∃t ϕ0 ∧ ∀q' ∃r' ϕ1 ∧ ¬∀q'' ∃r'' ϕ2)
	public static final QBF lncs =
		new Exists(
			new And(
				new ForAll(
					new Exists(
						new ForAll(
							new Exists(
								new Literal("ϕ0"),
								"t"),
							"s"),
						"r"),
					"q"),
				new ForAll(
					new Exists(
							new Literal("ϕ1"),
						"r'"),
					"q'"),
				new Not(
					new ForAll(
						new Exists(
							new Literal("ϕ2"),
							"r''"),
						"q''"))),
			"p");
	public static final QBF lncsNNF = lncs.toNNF();

	// G14 file format specification example: ∀z: (z ∨ ∃x1,x2: (x1 ∧ x2 ∧ z))
	public static final QBF g14 =
		new ForAll(
			new Or(
				new Literal("z"),
				new Exists(
					new And(
						new Literal(x1),
						new Literal(x2),
						new Literal("z")),
					x1, x2)),
			"z");

	private static String joinCommaDelimited(Stream<String> stream) {
		return stream.collect(Collectors.joining(","));
	}

	@Test
	@DisplayName("illegal construction")
	void test_illegalConstruction() {
		Literal lit = new Literal(x1);

		// Literal
		assertThrows(IllegalArgumentException.class, () -> new Literal(null));
		assertThrows(IllegalArgumentException.class, () -> new Literal(""));

		// Not
		assertThrows(IllegalArgumentException.class, () -> new Not(null));

		// And
		List<QBF> subformulasNull = null;
		List<QBF> subformulasSingle = Arrays.asList(lit);
		assertThrows(IllegalArgumentException.class, () -> new And(subformulasNull));
		assertThrows(IllegalArgumentException.class, () -> new And(subformulasSingle));
		assertThrows(IllegalArgumentException.class, () -> new And());

		// Or
		assertThrows(IllegalArgumentException.class, () -> new Or(subformulasNull));
		assertThrows(IllegalArgumentException.class, () -> new And(subformulasSingle));
		assertThrows(IllegalArgumentException.class, () -> new Or());

		// ForAll
		Set<String> variablesNull = null;
		assertThrows(IllegalArgumentException.class, () -> new ForAll(null, variablesNull));
		assertThrows(IllegalArgumentException.class, () -> new ForAll(lit, variablesNull));
		assertThrows(IllegalArgumentException.class, () -> new ForAll(null, x1, x2));

		// ForAll
		assertThrows(IllegalArgumentException.class, () -> new Exists(null, variablesNull));
		assertThrows(IllegalArgumentException.class, () -> new Exists(lit, variablesNull));
		assertThrows(IllegalArgumentException.class, () -> new Exists(null, x1, x2));
	}

	@Test
	@DisplayName("equals")
	void test_equals() {
		QBF lit = new Literal(x1);
		QBF and = new And(new Literal(x1), new Literal(x2), new Literal(x3));
		QBF or = new Or(new Literal(x1), new Literal(x2), new Literal(x3));
		QBF not = new Not(or);
		QBF forall = new ForAll(not, x2);
		QBF exists = new Exists(forall, x1);

		// Literal
		assertEquals(lit, new Literal(x1));
		assertNotEquals(lit, new Literal(x2));
		assertNotEquals(lit, and);

		// Not
		assertEquals(not, new Not(new Or(new Literal(x1), new Literal(x2), new Literal(x3))));
		assertNotEquals(not, new Not(lit));
		assertNotEquals(not, and);

		// And
		assertEquals(and, new And(new Literal(x1), new Literal(x2), new Literal(x3)));
		assertNotEquals(and, new And(new Literal(x1), new Literal(x2)));
		assertNotEquals(and, or);

		// Or
		assertEquals(or, new Or(new Literal(x1), new Literal(x2), new Literal(x3)));
		assertNotEquals(or, new Or(new Literal(x1), new Literal(x2)));
		assertNotEquals(or, and);

		// ForAll
		assertEquals(forall, new ForAll(new Not(new Or(new Literal(x1), new Literal(x2), new Literal(x3))), x2));
		assertNotEquals(forall, new ForAll(new Not(and), x1));
		assertNotEquals(forall, new ForAll(not, x1));
		assertNotEquals(forall, and);

		// Exists
		assertEquals(
			exists,
			new Exists(
				new ForAll(
					new Not(new Or(new Literal(x1), new Literal(x2), new Literal(x3))),
					x2),
				x1));
		assertNotEquals(exists, new Exists(new Not(and), x1));
		assertNotEquals(exists, new Exists(not, x2));
		assertNotEquals(exists, and);
	}

	@Test
	@DisplayName("stream")
	void test_stream() {
		QBF lit1 = new Literal(x1);
		QBF lit2 = new Literal(x2);
		QBF lit3 = new Literal(x3);
		QBF and = new And(new Literal(x1), new Literal(x2), new Literal(x3), QBF.True);
		QBF or = new Or(new Literal(x1), new Literal(x2), new Literal(x3), QBF.False);
		QBF not = new Not(or);
		QBF forall = new ForAll(not, x2);
		QBF exists = new Exists(forall, x1);

		// pre-order
		assertEquals(
			lit1.stream(Traverse.PreOrder).collect(Collectors.toList()),
			Arrays.asList(lit1));
		assertEquals(
			and.stream(Traverse.PreOrder).collect(Collectors.toList()),
			Arrays.asList(and, lit1, lit2, lit3, QBF.True));
		assertEquals(
			or.stream(Traverse.PreOrder).collect(Collectors.toList()),
			Arrays.asList(or, lit1, lit2, lit3, QBF.False));
		assertEquals(
			not.stream(Traverse.PreOrder).collect(Collectors.toList()),
			Arrays.asList(not, or, lit1, lit2, lit3, QBF.False));
		assertEquals(
			forall.stream(Traverse.PreOrder).collect(Collectors.toList()),
			Arrays.asList(forall, not, or, lit1, lit2, lit3, QBF.False));
		assertEquals(
			exists.stream(Traverse.PreOrder).collect(Collectors.toList()),
			Arrays.asList(exists, forall, not, or, lit1, lit2, lit3, QBF.False));

		// post-order
		assertEquals(
			lit1.stream(Traverse.PostOrder).collect(Collectors.toList()),
			Arrays.asList(lit1));
		assertEquals(
			and.stream(Traverse.PostOrder).collect(Collectors.toList()),
			Arrays.asList(lit1, lit2, lit3, QBF.True, and));
		assertEquals(
			or.stream(Traverse.PostOrder).collect(Collectors.toList()),
			Arrays.asList(lit1, lit2, lit3, QBF.False, or));
		assertEquals(
			not.stream(Traverse.PostOrder).collect(Collectors.toList()),
			Arrays.asList(lit1, lit2, lit3, QBF.False, or, not));
		assertEquals(
			forall.stream(Traverse.PostOrder).collect(Collectors.toList()),
			Arrays.asList(lit1, lit2, lit3, QBF.False, or, not, forall));
		assertEquals(
			exists.stream(Traverse.PostOrder).collect(Collectors.toList()),
			Arrays.asList(lit1, lit2, lit3, QBF.False, or, not, forall, exists));
	}

	@Test
	@DisplayName("streamVariables")
	void test_streamVariables() {
		QBF lit1 = new Literal(x1);
		QBF lit2 = new Literal(x2);
		QBF lit3 = new Literal(x3);
		QBF lit4 = new Literal(x4);
		QBF and = new And(lit1, lit2, lit3);
		QBF or = new Or(and, lit4);
		QBF not = new Not(or);
		QBF forall = new ForAll(not, x2);
		QBF exists = new Exists(forall, x1);

		assertEquals(
			"x1",
			joinCommaDelimited(lit1.streamVariables()));

		assertEquals(
			"x1,x2,x3",
			joinCommaDelimited(and.streamVariables()));

		assertEquals(
			"x1,x2,x3,x4",
			joinCommaDelimited(or.streamVariables()));

		assertEquals(
			"x1,x2,x3,x4",
			joinCommaDelimited(not.streamVariables()));

		assertEquals(
			"x2,x1,x2,x3,x4",
			joinCommaDelimited(forall.streamVariables()));

		assertEquals(
			"x1,x2,x1,x2,x3,x4",
			joinCommaDelimited(exists.streamVariables()));

		assertEquals(
			"p,q,r,s,t,ϕ0,q',r',ϕ1,q'',r'',ϕ2",
			joinCommaDelimited(lncs.streamVariables()));
	}

	@Test
	@DisplayName("streamFreeVariables")
	void test_streamFreeVariables() {
		QBF lit1 = new Literal(x1);
		QBF lit2 = new Literal(x2);
		QBF lit3 = new Literal(x3);
		QBF lit4 = new Literal(x4);
		QBF and = new And(lit1, lit2, lit3);
		QBF or = new Or(and, lit4);
		QBF not = new Not(or);
		QBF forall = new ForAll(not, x2);
		QBF exists = new Exists(forall, x1);

		assertEquals(
			"x1",
			joinCommaDelimited(lit1.streamFreeVariables()));

		assertEquals(
			"x1,x2,x3",
			joinCommaDelimited(and.streamFreeVariables()));

		assertEquals(
			"x1,x2,x3,x4",
			joinCommaDelimited(or.streamFreeVariables()));

		assertEquals(
			"x1,x2,x3,x4",
			joinCommaDelimited(not.streamFreeVariables()));

		assertEquals(
			"x1,x3,x4",
			joinCommaDelimited(forall.streamFreeVariables()));

		assertEquals(
			"x3,x4",
			joinCommaDelimited(exists.streamFreeVariables()));

		assertEquals(
			"ϕ0,ϕ1,ϕ2",
			joinCommaDelimited(lncs.streamFreeVariables()));
	}

	@Test
	@DisplayName("streamBoundVariables")
	void test_streamBoundVariables() {
		QBF lit1 = new Literal(x1);
		QBF lit2 = new Literal(x2);
		QBF lit3 = new Literal(x3);
		QBF lit4 = new Literal(x4);
		QBF and = new And(lit1, lit2, lit3);
		QBF or = new Or(and, lit4);
		QBF not = new Not(or);
		QBF forall = new ForAll(not, x2);
		QBF exists = new Exists(forall, x1);
		QBF unclean = new And(forall, exists, new ForAll(and, x2));

		assertEquals(
			"",
			joinCommaDelimited(lit1.streamBoundVariables()));

		assertEquals(
			"",
			joinCommaDelimited(and.streamBoundVariables()));

		assertEquals(
			"",
			joinCommaDelimited(or.streamBoundVariables()));

		assertEquals(
			"",
			joinCommaDelimited(not.streamBoundVariables()));

		assertEquals(
			"x2",
			joinCommaDelimited(forall.streamBoundVariables()));

		assertEquals(
			"x1,x2",
			joinCommaDelimited(exists.streamBoundVariables()));

		assertEquals(
			"x2,x1,x2,x2",
			joinCommaDelimited(unclean.streamBoundVariables()));

		assertEquals(
			"p,q,r,s,t,q',r',q'',r''",
			joinCommaDelimited(lncs.streamBoundVariables()));
	}

	@Test
	@DisplayName("streamPrefix")
	void test_streamPrefix() {
		QBF sub1 = ((And) ((Exists) lncsNNF).subformula).subformulas.get(0);
		QBF sub2 = ((And) ((Exists) lncsNNF).subformula).subformulas.get(1);
		QBF sub3 = ((And) ((Exists) lncsNNF).subformula).subformulas.get(2);

		assertEquals(lncs.prefixToString(), "∃p");
		assertEquals(sub1.prefixToString(), "∀q ∃r ∀s ∃t");
		assertEquals(sub2.prefixToString(), "∀q' ∃r'");
		assertEquals(sub3.prefixToString(), "∃q'' ∀r''");
	}

	@Test
	@DisplayName("streamQPaths")
	void test_streamQPaths() {
		Function<QBF, List<String>> qpathsToString = f ->
			f.streamQPaths()
				.map(QBF::prefixToString)
				.collect(Collectors.toList());

		// LNCS paper example
		List<String> qpaths = qpathsToString.apply(lncsNNF);

		assertEquals(3, qpaths.size());
		assertEquals(qpaths.get(0), "∃p ∀q ∃r ∀s ∃t");
		assertEquals(qpaths.get(1), "∃p ∀q' ∃r'");
		assertEquals(qpaths.get(2), "∃p,q'' ∀r''");

		And and = (And) ((Exists) lncsNNF).subformula;
		qpaths = qpathsToString.apply(and);

		assertEquals(3, qpaths.size());
		assertEquals(qpaths.get(0), "∀q ∃r ∀s ∃t");
		assertEquals(qpaths.get(1), "∀q' ∃r'");
		assertEquals(qpaths.get(2), "∃q'' ∀r''");

		qpaths = and.subformulas.stream().flatMap(f -> qpathsToString.apply(f).stream()).collect(Collectors.toList());

		assertEquals(3, qpaths.size());
		assertEquals(qpaths.get(0), "∀q ∃r ∀s ∃t");
		assertEquals(qpaths.get(1), "∀q' ∃r'");
		assertEquals(qpaths.get(2), "∃q'' ∀r''");

		// two different qpaths
		QBF twoPaths =
			new And(
				new ForAll(new Exists(new Literal("x3"), "x2"), "x1"),
				new Exists(new ForAll(new Literal("x6"), "x5"), "x4"));

		qpaths = qpathsToString.apply(twoPaths);

		assertEquals(2, qpaths.size());
		assertEquals(qpaths.get(0), "∀x1 ∃x2");
		assertEquals(qpaths.get(1), "∃x4 ∀x5");
	}

	@Test
	@DisplayName("getCriticalPaths")
	void test_getCriticalPaths() {
		Function<List<QBF>, List<String>> criticalPathsToString = l ->
			l.stream().map(QBF::prefixToString).collect(Collectors.toList());

		// LNCS paper example
		List<QBF> qpaths = lncsNNF.getQPaths();

		List<String> criticalPaths = criticalPathsToString.apply(QBF.getCriticalPaths(qpaths));

		assertEquals(1, criticalPaths.size());
		assertEquals("∃p ∀q ∃r ∀s ∃t", criticalPaths.get(0));

		// two different qpaths
		QBF twoPaths =
			new And(
				new ForAll(new Exists(new Literal("x3"), "x2"), "x1"),
				new Exists(new ForAll(new Literal("x6"), "x5"), "x4"));

		qpaths = twoPaths.getQPaths();

		criticalPaths = criticalPathsToString.apply(QBF.getCriticalPaths(qpaths));

		assertEquals(2, criticalPaths.size());
		assertEquals("∀x1 ∃x4 ∀x5", criticalPaths.get(0));
		assertEquals("∃x4 ∀x1 ∃x2", criticalPaths.get(1));

		// three equal paths
		QBF samePaths =
			new And(
				new ForAll(new Exists(new Literal("x3"), "x2"), "x1"),
				new ForAll(new Exists(new Literal("x6"), "x5"), "x4"),
				new ForAll(new Exists(new Literal("x9"), "x8"), "x7"));

		qpaths = samePaths.getQPaths();

		criticalPaths = criticalPathsToString.apply(QBF.getCriticalPaths(qpaths));

		assertEquals(1, criticalPaths.size());
		assertEquals("∀x1 ∃x2", criticalPaths.get(0));
	}

	@Test
	@DisplayName("getSkeleton")
	void test_getSkeleton() {
		assertEquals("(ϕ0 ∧ ϕ1 ∧ -ϕ2)", lncs.getSkeleton().toString());
		assertEquals("(ϕ0 ∧ ϕ1 ∧ -ϕ2)", lncsNNF.getSkeleton().toString());

		QBF qbf = new ForAll(new Exists(new Literal(x3), x2), x1);
		assertEquals("x3", qbf.getSkeleton().toString());

		qbf = new Not(qbf);
		assertEquals("-x3", qbf.getSkeleton().toString());

		qbf = new And(qbf.negate(), qbf);
		assertEquals("(x3 ∧ -x3)", qbf.getSkeleton().toString());

		qbf = new Or(qbf.negate(), qbf);
		assertEquals("(-(x3 ∧ -x3) ∨ (x3 ∧ -x3))", qbf.getSkeleton().toString());

		qbf = new Not(qbf);
		assertEquals("-(-(x3 ∧ -x3) ∨ (x3 ∧ -x3))", qbf.getSkeleton().toString());
	}

	@Test
	@DisplayName("unifyPrefix")
	void test_unifyPrefix() {
		QBF lit1 = new Literal(x1);
		QBF lit2 = new Literal(x2);
		QBF not = new Not(lit1);
		QBF and = new And(not, lit2);
		QBF or = new Or(lit1, and);
		QBF forall1 = new ForAll(or, x2);
		QBF forall2 = new ForAll(forall1, x1);
		QBF exists1 = new Exists(or, x2);
		QBF exists2 = new Exists(exists1, x1);

		assertEquals(lit1, lit1.unifyPrefix());
		assertEquals(not, not.unifyPrefix());
		assertEquals(and, and.unifyPrefix());
		assertEquals(or, or.unifyPrefix());
		assertEquals(forall1, forall1.unifyPrefix());
		assertEquals(new ForAll(or, x1, x2), forall2.unifyPrefix());
		assertEquals(exists1, exists1.unifyPrefix());
		assertEquals(new Exists(or, x1, x2), exists2.unifyPrefix());

		QBF[][] triples = {
			{ new ForAll(new ForAll(new ForAll(or, x3), x2), x1), new ForAll(or, x1, x2, x3) },
			{ new ForAll(new ForAll(new Exists(or, x3), x2), x1), new ForAll(new Exists(or, x3), x1, x2) },
			{ new ForAll(new Exists(new ForAll(or, x3), x2), x1), new ForAll(new Exists(new ForAll(or, x3), x2), x1) },
			{ new ForAll(new Exists(new Exists(or, x3), x2), x1), new ForAll(new Exists(or, x2, x3), x1) },
			{ new Exists(new ForAll(new ForAll(or, x3), x2), x1), new Exists(new ForAll(or, x2, x3), x1) },
			{ new Exists(new ForAll(new Exists(or, x3), x2), x1), new Exists(new ForAll(new Exists(or, x3), x2), x1) },
			{ new Exists(new Exists(new ForAll(or, x3), x2), x1), new Exists(new ForAll(or, x3), x1, x2) },
			{ new Exists(new Exists(new Exists(or, x3), x2), x1), new Exists(or, x1, x2, x3) }
		};

		for(QBF[] t : triples)
			assertEquals(t[1], t[0].unifyPrefix());
	}

	@Test
	@DisplayName("negate")
	void test_negate() {
		QBF lit = new Literal(x1);
		QBF not = new Not(lit);
		QBF and = new And(new Literal(x1), new Literal(x2));
		QBF or = new Or(new Not(new Literal(x1)), lit);
		QBF forall = new ForAll(and, x1, x2);
		QBF exists = new Exists(or, x3);

		assertEquals(QBF.True.negate(), QBF.False);
		assertEquals(QBF.False.negate(), QBF.True);
		assertEquals(lit.negate(), not);
		assertEquals(not.negate(), lit);
		assertEquals(and.negate(), new Not(and));
		assertEquals(or.negate(), new Not(or));
		assertEquals(forall.negate(), new Not(forall));
		assertEquals(exists.negate(), new Not(exists));
	}

	@Test
	@DisplayName("rename")
	void test_rename() {
		QBF lit1 = new Literal(x1);
		QBF lit2 = new Literal(x2);
		QBF lit3 = new Literal(x3);
		QBF lit4 = new Literal(x4);
		QBF and = new And(lit1, lit2, lit3);
		QBF or = new Or(and, lit4);
		QBF not = new Not(or);
		QBF forall = new ForAll(not, x2);
		QBF exists = new Exists(forall, x1);

		QBF result;
		Map<String, String> variables = new HashMap<>();

		variables.put("x1", "1");
		variables.put("x2", "2");
		variables.put("x3", "3");
		variables.put("x4", "4");
		result = exists.rename(variables);

		assertEquals(
			"1,2,1,2,3,4",
			joinCommaDelimited(result.streamVariables()));

		variables.put("ϕ0", "1");
		variables.put("ϕ1", "2");
		variables.put("ϕ2", "3");
		result = lncs.rename(variables);

		assertEquals(
			"p,q,r,s,t,1,q',r',2,q'',r'',3",
			joinCommaDelimited(result.streamVariables()));
	}

	@Test
	@DisplayName("cleanse")
	void test_cleanse() {
		QBF lit1 = new Literal(x1);
		QBF lit2 = new Literal(x2);
		QBF lit3 = new Literal(x3);
		QBF lit4 = new Literal(x4);
		QBF and = new And(lit1, lit2, lit3);
		QBF or = new Or(and, lit4);
		QBF not = new Not(or);
		QBF forall = new ForAll(not, x2);
		QBF exists = new Exists(forall, x1);
		QBF constant = new And(exists, QBF.True);
		QBF duplicates1 = new And(forall, exists, new ForAll(and, x2));
		QBF duplicates2 = new Or(exists, exists, exists);
		QBF notOccuring =
			new ForAll(
				new And(
					new ForAll(
						new And(
							new Or(lit1, lit3, lit4),
							new Or(lit1, lit3, lit4)),
						x1, x2),
					new ForAll(
						new And(
							new Or(lit1, lit3, lit4),
							new Or(lit1, lit3, lit4)),
						x1, x2),
					new ForAll(
						new And(
							new Or(lit1, lit3, lit4),
							new Or(lit1, lit3, lit4)),
						x1, x2)),
				x1);
		QBF topFree =
			new Or(
				new Literal(x1),
				new Literal(x2),
				new And(
					new Literal(x3),
					new Literal(x4),
					notOccuring));
		QBF shadowed = new Exists(topFree, x1);

		assertEquals(
			"∃1: ∀2: -((1 ∧ 2 ∧ 3) ∨ 4)",
			exists.cleanse().toString());

		assertEquals(
			"(∃1: ∀2: -((1 ∧ 2 ∧ 3) ∨ 4) ∧ TRUE)",
			constant.cleanse().toString());

		assertEquals(
			 "(∀2: -((1 ∧ 2 ∧ 3) ∨ 4) ∧ "
			+ "∃5: ∀6: -((5 ∧ 6 ∧ 3) ∨ 4) ∧ "
			+ "∀7: (1 ∧ 7 ∧ 3))",
			duplicates1.cleanse().toString());

		assertEquals(
			 "(∃1: ∀2: -((1 ∧ 2 ∧ 3) ∨ 4) ∨ "
			+ "∃5: ∀6: -((5 ∧ 6 ∧ 3) ∨ 4) ∨ "
			+ "∃7: ∀8: -((7 ∧ 8 ∧ 3) ∨ 4))",
			duplicates2.cleanse().toString());

		assertEquals(
			 "(∀1: ((1 ∨ 2 ∨ 3) ∧ (1 ∨ 2 ∨ 3)) ∧ "
			+ "∀4: ((4 ∨ 2 ∨ 3) ∧ (4 ∨ 2 ∨ 3)) ∧ "
			+ "∀5: ((5 ∨ 2 ∨ 3) ∧ (5 ∨ 2 ∨ 3)))",
			notOccuring.cleanse().toString());

		assertEquals(
			"(1 ∨ 2 ∨ ("
			+ "3 ∧ 4 ∧ ("
				+ "∀5: ((5 ∨ 3 ∨ 4) ∧ (5 ∨ 3 ∨ 4)) ∧ "
				+ "∀6: ((6 ∨ 3 ∨ 4) ∧ (6 ∨ 3 ∨ 4)) ∧ "
				+ "∀7: ((7 ∨ 3 ∨ 4) ∧ (7 ∨ 3 ∨ 4)))))",
			topFree.cleanse().toString());

		assertEquals(
			"∃1: (1 ∨ 2 ∨ ("
			+ "3 ∧ 4 ∧ ("
				+ "∀5: ((5 ∨ 3 ∨ 4) ∧ (5 ∨ 3 ∨ 4)) ∧ "
				+ "∀6: ((6 ∨ 3 ∨ 4) ∧ (6 ∨ 3 ∨ 4)) ∧ "
				+ "∀7: ((7 ∨ 3 ∨ 4) ∧ (7 ∨ 3 ∨ 4)))))",
			shadowed.cleanse().toString());

		assertEquals(
			"(1 ∧ 2 ∧ -3)",
			lncs.cleanse().toString());

		assertEquals(
			"(1 ∧ 2 ∧ -3)",
			lncsNNF.cleanse().toString());
	}

	@Test
	@DisplayName("toNNF")
	void test_toNNF() {
		QBF lit = new Not(new Literal(x1));
		QBF and = new Not(new And(new Literal(x1), new Literal(x2), QBF.True));
		QBF or = new Not(new Or(lit, new Not(new Literal(x2)), QBF.False));
		QBF forall = new Not(new ForAll(and, x1, x2));
		QBF exists = new Not(new Exists(forall, x3));

//		System.out.println(lit.toString() + " == " + lit.toNNF().toString());
//		System.out.println(and.toString() + " == " + and.toNNF().toString());
//		System.out.println(or.toString() + " == " + or.toNNF().toString());
//		System.out.println(forall.toString() + " == " + forall.toNNF().toString());
//		System.out.println(exists.toString() + " == " + exists.toNNF().toString());

		assertEquals(lit.toNNF(), new Not(new Literal(x1)));
		assertEquals(and.toNNF(), new Or(new Not(new Literal(x1)), new Not(new Literal(x2)), QBF.False));
		assertEquals(or.toNNF(), new And(new Literal(x1), new Literal(x2), QBF.True));
		assertEquals(forall.toNNF(), new Exists(new And(new Literal(x1), new Literal(x2), QBF.True), x1, x2));
		assertEquals(
			exists.toNNF(),
			new ForAll(
				new ForAll(
					new Or(new Not(new Literal(x1)), new Not(new Literal(x2)), QBF.False),
					x1, x2),
				x3));
	}

	@Test
	@DisplayName("toString")
	void test_toString() {
		QBF and = new And(new Literal(x1), new Literal(x2), new Literal (x3));
		QBF or = new Or(new Not(new Literal(x1)), and);
		QBF forall = new ForAll(or, x1, x2);
		QBF exists = new Exists(forall, x3);
		QBF tautology = new Or(QBF.True, QBF.False);

		assertEquals("(x1 ∧ x2 ∧ x3)", and.toString());
		assertEquals("(-x1 ∨ (x1 ∧ x2 ∧ x3))", or.toString());
		assertEquals("∀x1,x2: (-x1 ∨ (x1 ∧ x2 ∧ x3))", forall.toString());
		assertEquals("∃x3: ∀x1,x2: (-x1 ∨ (x1 ∧ x2 ∧ x3))", exists.toString());
		assertEquals("(TRUE ∨ FALSE)", tautology.toString());
	}
}