package at.jku.fmv.qbf.prenexing;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import at.jku.fmv.qbf.QBF;
import at.jku.fmv.qbf.QBF.*;

@DisplayName("ShiftingStrategy")
public class ShiftingStrategyTest {

	@Test
	@DisplayName("apply")
	void test_apply() {
		class DummyStrategy extends ShiftingStrategy {
			QBF selectCriticalPath(List<QBF> criticalPaths) {
				return criticalPaths.get(0);
			}

			List<Set<String>> getVariableOrdering(
				QBF criticalPath,
				List<QBF> qpaths,
				QBF skeleton
			) {
				throw new UnsupportedOperationException();
			}
		}

		ShiftingStrategy dummy = new DummyStrategy();

		// no transformation required
		QBF nop = new And(new Literal("x1"), new Literal("x2"));
		assertEquals(nop, nop.toPNF(dummy));

		nop = new ForAll(new Exists(nop, "x2"), "x1");
		assertEquals(nop, nop.toPNF(dummy));

		// simple quantifier shifting (single quantified subformula)
		QBF singleQuantifiedSubformula =
			new And(
				new Literal("x1"),
				new ForAll(new Exists(new Literal("x4"), "x3"), "x2"),
				new Literal("x5"));

		assertEquals(
			"(x1 ∧ ∀x2: ∃x3: x4 ∧ x5)",
			singleQuantifiedSubformula.toString());
		QBF result = singleQuantifiedSubformula.toPNF(dummy);
		assertEquals("∀x2: ∃x3: (x1 ∧ x4 ∧ x5)", result.toString());

		// simple quantifier merging (single critical path and equal qpaths)
		QBF andEqualQPaths =
			new And(
				new ForAll(
					new Exists(
						new ForAll(
							new Literal("ϕ0"),
							"x3"),
						"x2"),
					"x1"),
				new ForAll(
					new Exists(
						new ForAll(
							new Literal("ϕ1"),
							"x6"),
						"x5"),
					"x4"),
				new ForAll(
					new Exists(
						new ForAll(
							new Literal("ϕ2"),
							"x9"),
						"x8"),
					"x7"));

		assertEquals(
			"(∀x1: ∃x2: ∀x3: ϕ0 ∧ ∀x4: ∃x5: ∀x6: ϕ1 ∧ ∀x7: ∃x8: ∀x9: ϕ2)",
			andEqualQPaths.toString());
		result = andEqualQPaths.toPNF(dummy);
		assertEquals(
			"∀x1,x4,x7: ∃x2,x5,x8: ∀x3,x6,x9: (ϕ0 ∧ ϕ1 ∧ ϕ2)",
			result.toString());

		QBF forallAndEqualQPaths = new ForAll(andEqualQPaths, "x10");

		assertEquals(
			"∀x10: (∀x1: ∃x2: ∀x3: ϕ0 ∧ ∀x4: ∃x5: ∀x6: ϕ1 ∧ ∀x7: ∃x8: ∀x9: ϕ2)",
			forallAndEqualQPaths.toString());
		result = forallAndEqualQPaths.toPNF(dummy);
		assertEquals(
			"∀x1,x10,x4,x7: ∃x2,x5,x8: ∀x3,x6,x9: (ϕ0 ∧ ϕ1 ∧ ϕ2)",
			result.toString());

		QBF existsAndEqualQPaths = new Exists(andEqualQPaths, "x10");

		assertEquals(
			"∃x10: (∀x1: ∃x2: ∀x3: ϕ0 ∧ ∀x4: ∃x5: ∀x6: ϕ1 ∧ ∀x7: ∃x8: ∀x9: ϕ2)",
			existsAndEqualQPaths.toString());
		result = existsAndEqualQPaths.toPNF(dummy);
		assertEquals(
			"∃x10: ∀x1,x4,x7: ∃x2,x5,x8: ∀x3,x6,x9: (ϕ0 ∧ ϕ1 ∧ ϕ2)",
			result.toString());

		QBF existsExistsAndEqualQPaths =
			new Exists(existsAndEqualQPaths, "x11");

		assertEquals(
			"∃x11: ∃x10: (∀x1: ∃x2: ∀x3: ϕ0 ∧ ∀x4: ∃x5: ∀x6: ϕ1 ∧ ∀x7: ∃x8: ∀x9: ϕ2)",
			existsExistsAndEqualQPaths.toString());
		result = existsExistsAndEqualQPaths.toPNF(dummy);
		assertEquals(
			"∃x10,x11: ∀x1,x4,x7: ∃x2,x5,x8: ∀x3,x6,x9: (ϕ0 ∧ ϕ1 ∧ ϕ2)",
			result.toString());

		// different qpaths: apply prenexing strategy
		QBF twoCriticalPaths =
			new And(
				new ForAll(new Exists(new Literal("x3"), "x2"), "x1"),
				new Exists(new ForAll(new Literal("x6"), "x5"), "x4"));

		assertThrows(
			UnsupportedOperationException.class,
			() -> twoCriticalPaths.toPNF(dummy));
	}
}
