package at.jku.fmv.qcir.test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import at.jku.fmv.qcir.QBF;
import at.jku.fmv.qcir.QBF.*;

@DisplayName("QBF")
class QBFTest {

	// variables
	static final String x1 = "x1";
	static final String x2 = "x2";
	static final String x3 = "x3";
	static final String x4 = "x4";

//	// And
//	static final QBF fAnd12 = new And(new Literal(x1), new Literal(x2));
//	static final QBF fAnd23 = new And(new Literal(x2), new Literal(x3));
//	static final QBF fAnd123 = new And(new Literal(x1), new Literal(x2), new Literal(x3));
//	// Or
//	static final QBF fOr12 = new Or(new Literal(x1), new Literal(x2));
//	static final QBF fOr23 = new Or(new Literal(x2), new Literal(x3));
//	static final QBF fOr123 = new Or(new Literal(x1), new Literal(x2), new Literal(x3));
//	// ForAll
//	static final QBF fForAll12And12 = new ForAll(fAnd12, x1, x2);
//	static final QBF fForAll23And23 = new ForAll(fAnd23, x2, x3);
//
//	// Exists
//	static final QBF fExists12And12 = new Exists(fAnd12, x1, x2);
//	static final QBF fExists23And23 = new Exists(fAnd23, x2, x3);
//
//	// combinations
//	static final QBF fExists1ForAll23OrAnd12And23 = new Exists(new ForAll(new Or(fAnd12, fAnd23), x2, x3), x1);

//	@BeforeAll
//	static void initAll() {
//	}

	@Test
	@DisplayName("illegal construction")
	void test_constructor() {
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
		List<String> variablesNull = null;
		assertThrows(IllegalArgumentException.class, () -> new ForAll(null, variablesNull));
		assertThrows(IllegalArgumentException.class, () -> new ForAll(lit, variablesNull));
		assertThrows(IllegalArgumentException.class, () -> new ForAll(null, Arrays.asList(x1, x2)));

		// ForAll
		assertThrows(IllegalArgumentException.class, () -> new Exists(null, variablesNull));
		assertThrows(IllegalArgumentException.class, () -> new Exists(lit, variablesNull));
		assertThrows(IllegalArgumentException.class, () -> new Exists(null, Arrays.asList(x1, x2)));
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
		assertTrue(lit.equals(new Literal(x1)));
		assertFalse(lit.equals(new Literal(x2)));
		assertFalse(lit.equals(and));

		// Not
		assertTrue(not.equals(new Not(new Or(new Literal(x1), new Literal(x2), new Literal(x3)))));
		assertFalse(not.equals(new Not(lit)));
		assertFalse(not.equals(and));

		// And
		assertTrue(and.equals(new And(new Literal(x1), new Literal(x2), new Literal(x3))));
		assertFalse(and.equals(new And(new Literal(x1), new Literal(x2))));
		assertFalse(and.equals(or));

		// Or
		assertTrue(or.equals(new Or(new Literal(x1), new Literal(x2), new Literal(x3))));
		assertFalse(or.equals(new Or(new Literal(x1), new Literal(x2))));
		assertFalse(or.equals(and));

		// ForAll
		assertTrue(forall.equals(new ForAll(new Not(new Or(new Literal(x1), new Literal(x2), new Literal(x3))), x2)));
		assertFalse(forall.equals(new ForAll(new Not(and), x1)));
		assertFalse(forall.equals(new ForAll(not, x1)));
		assertFalse(forall.equals(and));

		// Exists
		assertTrue(
			exists.equals(
				new Exists(
					new ForAll(
						new Not(new Or(new Literal(x1), new Literal(x2), new Literal(x3))),
						x2),
					x1)));
		assertFalse(exists.equals(new Exists(new Not(and), x1)));
		assertFalse(exists.equals(new Exists(not, x2)));
		assertFalse(exists.equals(and));
	}

	@Test
	@DisplayName("toString")
	void test_toString() {
		QBF and = new And(new Literal(x1), new Literal(x2), new Literal (x3));
		QBF or = new Or(new Not(new Literal(x1)), and);
		QBF forall = new ForAll(or, x1, x2);
		QBF exists = new Exists(forall, x3);

//		System.out.println(t1.toString());
//		System.out.println(t2.toString());
//		System.out.println(t3.toString());
//		System.out.println(t4.toString());

		assertEquals("(x1 ∧ x2 ∧ x3)", and.toString());
		assertEquals("(-x1 ∨ (x1 ∧ x2 ∧ x3))", or.toString());
		assertEquals("∀ x1, x2: (-x1 ∨ (x1 ∧ x2 ∧ x3))", forall.toString());
		assertEquals("∃ x3: ∀ x1, x2: (-x1 ∨ (x1 ∧ x2 ∧ x3))", exists.toString());
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
		QBF and = new Not(new And(new Literal(x1), new Literal(x2)));
		QBF or = new Not(new Or(lit, new Not(new Literal(x2))));
		QBF forall = new Not(new ForAll(and, x1, x2));
		QBF exists = new Not(new Exists(forall, x3));

//		System.out.println(lit.toString() + " == " + lit.toNNF().toString());
//		System.out.println(and.toString() + " == " + and.toNNF().toString());
//		System.out.println(or.toString() + " == " + or.toNNF().toString());
//		System.out.println(forall.toString() + " == " + forall.toNNF().toString());
//		System.out.println(exists.toString() + " == " + exists.toNNF().toString());

		assertEquals(lit.toNNF(), new Not(new Literal(x1)));
		assertEquals(and.toNNF(), new Or(new Not(new Literal(x1)), new Not(new Literal(x2))));
		assertEquals(or.toNNF(), new And(new Literal(x1), new Literal(x2)));
		assertEquals(forall.toNNF(), new Exists(new And(new Literal(x1), new Literal(x2)), x1, x2));
		assertEquals(
			exists.toNNF(),
				new ForAll(
					new ForAll(
						new Or(new Not(new Literal(x1)), new Not(new Literal(x2))),
						x1, x2),
					x3));
	}
}