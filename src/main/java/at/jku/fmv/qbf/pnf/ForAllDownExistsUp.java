package at.jku.fmv.qbf.pnf;

// ∀↓∃↑ - prioritize universal quantifiers
public class ForAllDownExistsUp extends SimpleUpDownStrategy {

	boolean selectForAll(long numQPath, long numCritical) {
		return numCritical - numQPath <= 1;
	}

	boolean selectExists(long numQPath, long numCritical) { return true; }
}
