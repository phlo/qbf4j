package at.jku.fmv.qbf;

import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import at.jku.fmv.qbf.QBF;
import at.jku.fmv.qbf.QBF.*;

@DisplayName("QBF")
class QBFTest {

	// variables
	static final String x1 = "x1";
	static final String x2 = "x2";
	static final String x3 = "x3";
	static final String x4 = "x4";

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
		assertEquals("∀ x1, x2: (-x1 ∨ (x1 ∧ x2 ∧ x3))", forall.toString());
		assertEquals("∃ x3: ∀ x1, x2: (-x1 ∨ (x1 ∧ x2 ∧ x3))", exists.toString());
		assertEquals("(TRUE ∨ FALSE)", tautology.toString());
	}
}