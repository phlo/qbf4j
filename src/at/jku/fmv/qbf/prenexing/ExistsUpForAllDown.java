package at.jku.fmv.qbf.prenexing;

// ∃↑∀↓ - prioritize existential quantifiers
public class ExistsUpForAllDown extends SimpleUpDownStrategy {

	boolean selectForAll(long numQPath, long numCritical) {
		return numQPath > 1 || numCritical <= 2;
	}

	boolean selectExists(long numQPath, long numCritical) { return true; }
}
