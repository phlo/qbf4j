package at.jku.fmv.qbf.pcnf;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import at.jku.fmv.qbf.QBF;
import at.jku.fmv.qbf.QBF.*;
import at.jku.fmv.qbf.QBFTest;
import at.jku.fmv.qbf.prenexing.*;

@DisplayName("PG86")
class PG86Test {

	@Test
	@DisplayName("encode")
	void encode() {

		PG86 pg86 = new PG86();

		Literal x1 = new Literal("x1");
		Literal x2 = new Literal("x2");
		Literal x3 = new Literal("x3");
		Literal x4 = new Literal("x4");

		QBF and = new And(x1, x2);
		assertEquals(and, pg86.apply(and));

		QBF or = new Or(x1, x2);
		assertEquals(
			"(_pg0 ∧ (-_pg0 ∨ x1 ∨ x2))",
			pg86.apply(or).toString());

		QBF orAnd = new Or(and, new And(x3, x4));
		assertEquals(
			"(_pg0 "
			+ "∧ (-_pg0 ∨ _pg1 ∨ _pg2) "
			+ "∧ (-_pg1 ∨ x1) "
			+ "∧ (-_pg1 ∨ x2) "
			+ "∧ (-_pg2 ∨ x3) "
			+ "∧ (-_pg2 ∨ x4))",
			pg86.apply(orAnd).toString());

		QBF andOr = new And(or, new Or(x3, x4));
		assertEquals(andOr, pg86.apply(andOr));

		QBF nonPrenex = new Or(x1, new Exists(new Or(x2, x3), x2.variable));
		assertThrows(
			IllegalArgumentException.class,
			() -> pg86.apply(nonPrenex));

		PrenexingStrategy aueu = new ForAllUpExistsUp();

		assertEquals(
			"∀z: ∃x1,x2: ("
			+ "_pg0 "
			+ "∧ (-_pg0 ∨ z ∨ _pg1) "
			+ "∧ (-_pg1 ∨ x1) "
			+ "∧ (-_pg1 ∨ x2) "
			+ "∧ (-_pg1 ∨ z))",
			QBFTest.g14.toPCNF(aueu, pg86).toString());

		assertEquals(
			"∃p,q'': ∀q,q',r'': ∃r,r': ∀s: ∃t: (ϕ0 ∧ ϕ1 ∧ -ϕ2)",
			QBFTest.lncs.toPCNF(aueu, pg86).toString());
	}
}
