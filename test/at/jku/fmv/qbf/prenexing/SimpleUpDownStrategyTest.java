package at.jku.fmv.qbf.prenexing;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import at.jku.fmv.qbf.QBF;
import at.jku.fmv.qbf.QBF.*;

@DisplayName("SimpleUpDownStrategy")
class SimpleUpDownStrategyTest {

	static final QBF lncsExample =
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


	@Test
	@DisplayName("∀↑∃↑")
	void forallUpExistsUp() {
		// ∀↑∃↑
		PrenexingStrategy aueu = new ForAllUpExistsUp();
		QBF pnf = lncsExample.toPNF(aueu);
		assertEquals("∃p,q'': ∀q,q',r'': ∃r,r': ∀s: ∃t: (ϕ0 ∧ ϕ1 ∧ -ϕ2)", pnf.toString());
	}

	@Test
	@DisplayName("∀↓∃↑")
	void forallDownExistsUp() {
		// ∀↓∃↑
		PrenexingStrategy adeu = new ForAllDownExistsUp();
		QBF pnf = lncsExample.toPNF(adeu);
		assertEquals("∃p,q'': ∀q: ∃r: ∀q',r'',s: ∃r',t: (ϕ0 ∧ ϕ1 ∧ -ϕ2)", pnf.toString());
	}

	@Test
	@DisplayName("∃↑∀↓")
	void existsUpForallDown() {
		// ∃↑∀↓
		PrenexingStrategy euad = new ExistsUpForAllDown();
		QBF pnf = lncsExample.toPNF(euad);
		assertEquals("∃p,q'': ∀q,q': ∃r,r': ∀r'',s: ∃t: (ϕ0 ∧ ϕ1 ∧ -ϕ2)", pnf.toString());
	}

	@Test
	@DisplayName("∀↑∃↓")
	void forallUpExistsDown() {
		// ∀↑∃↓
		PrenexingStrategy aued = new ForAllUpExistsDown();
		QBF pnf = lncsExample.toPNF(aued);
		assertEquals("∃p,q'': ∀q,q',r'': ∃r: ∀s: ∃r',t: (ϕ0 ∧ ϕ1 ∧ -ϕ2)", pnf.toString());
	}

	@Test
	@DisplayName("∃↓∀↑")
	void existsDownForallUp() {
		// ∃↓∀↑
		PrenexingStrategy edau = new ExistsDownForAllUp();
		QBF pnf = lncsExample.toPNF(edau);
		assertEquals("∃p: ∀q,q': ∃q'',r: ∀r'',s: ∃r',t: (ϕ0 ∧ ϕ1 ∧ -ϕ2)", pnf.toString());
	}

	@Test
	@DisplayName("∀↓∃↓")
	void forallDownExistsDown() {
		// ∀↓∃↓
		PrenexingStrategy aded = new ForAllDownExistsDown();
		QBF pnf = lncsExample.toPNF(aded);
		assertEquals("∃p: ∀q: ∃q'',r: ∀q',r'',s: ∃r',t: (ϕ0 ∧ ϕ1 ∧ -ϕ2)", pnf.toString());
	}
}
