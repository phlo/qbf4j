package at.jku.fmv.qbf.pnf;

// ∀↓∃↓
public class ForAllDownExistsDown extends SimpleUpDownStrategy {

	boolean selectForAll(long numQPath, long numCritical) {
		return numCritical - numQPath <= 1;
	}

	boolean selectExists(long numQPath, long numCritical) {
		return numCritical - numQPath <= 1;
	}
}
