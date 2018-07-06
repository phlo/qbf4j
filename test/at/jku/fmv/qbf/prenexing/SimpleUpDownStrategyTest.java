package at.jku.fmv.qbf.prenexing;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import at.jku.fmv.qbf.QBF;
import at.jku.fmv.qbf.QBFTest;

@DisplayName("SimpleUpDownStrategy")
public class SimpleUpDownStrategyTest {

	@Test
	@DisplayName("∀↑∃↑")
	void forallUpExistsUp() {
		// ∀↑∃↑
		PrenexingStrategy aueu = new ForAllUpExistsUp();
		QBF pnf = QBFTest.lncs.toPNF(aueu);
		assertEquals("∃p,q'': ∀q,q',r'': ∃r,r': ∀s: ∃t: (ϕ0 ∧ ϕ1 ∧ -ϕ2)", pnf.toString());
	}

	@Test
	@DisplayName("∀↓∃↑")
	void forallDownExistsUp() {
		// ∀↓∃↑
		PrenexingStrategy adeu = new ForAllDownExistsUp();
		QBF pnf = QBFTest.lncs.toPNF(adeu);
		assertEquals("∃p,q'': ∀q: ∃r: ∀q',r'',s: ∃r',t: (ϕ0 ∧ ϕ1 ∧ -ϕ2)", pnf.toString());
	}

	@Test
	@DisplayName("∃↑∀↓")
	void existsUpForallDown() {
		// ∃↑∀↓
		PrenexingStrategy euad = new ExistsUpForAllDown();
		QBF pnf = QBFTest.lncs.toPNF(euad);
		assertEquals("∃p,q'': ∀q,q': ∃r,r': ∀r'',s: ∃t: (ϕ0 ∧ ϕ1 ∧ -ϕ2)", pnf.toString());
	}

	@Test
	@DisplayName("∀↑∃↓")
	void forallUpExistsDown() {
		// ∀↑∃↓
		PrenexingStrategy aued = new ForAllUpExistsDown();
		QBF pnf = QBFTest.lncs.toPNF(aued);
		assertEquals("∃p,q'': ∀q,q',r'': ∃r: ∀s: ∃r',t: (ϕ0 ∧ ϕ1 ∧ -ϕ2)", pnf.toString());
	}

	@Test
	@DisplayName("∃↓∀↑")
	void existsDownForallUp() {
		// ∃↓∀↑
		PrenexingStrategy edau = new ExistsDownForAllUp();
		QBF pnf = QBFTest.lncs.toPNF(edau);
		assertEquals("∃p: ∀q,q': ∃q'',r: ∀r'',s: ∃r',t: (ϕ0 ∧ ϕ1 ∧ -ϕ2)", pnf.toString());
	}

	@Test
	@DisplayName("∀↓∃↓")
	void forallDownExistsDown() {
		// ∀↓∃↓
		PrenexingStrategy aded = new ForAllDownExistsDown();
		QBF pnf = QBFTest.lncs.toPNF(aded);
		assertEquals("∃p: ∀q: ∃q'',r: ∀q',r'',s: ∃r',t: (ϕ0 ∧ ϕ1 ∧ -ϕ2)", pnf.toString());
	}
}
