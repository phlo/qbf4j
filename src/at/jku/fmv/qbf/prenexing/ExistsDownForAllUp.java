package at.jku.fmv.qbf.prenexing;

// ∃↓∀↑ - prioritize existential quantifiers
public class ExistsDownForAllUp extends SimpleUpDownStrategy {

	boolean selectForAll(long numQPath, long numCritical) { return true; }

	boolean selectExists(long numQPath, long numCritical) {
		return numCritical - numQPath <= 1;
	}
}
