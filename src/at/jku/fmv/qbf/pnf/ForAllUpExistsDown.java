package at.jku.fmv.qbf.pnf;

// ∀↑∃↓ - prioritize universal quantifiers
public class ForAllUpExistsDown extends SimpleUpDownStrategy {

	boolean selectForAll(long numQPath, long numCritical) { return true; }

	boolean selectExists(long numQPath, long numCritical) {
		return numQPath > 1 || numCritical <= 2;
	}
}
