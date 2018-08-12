package at.jku.fmv.qbf.pnf;

/**
 * ∀↓∃↓
 * <p>
 * Places universal quantifiers and existential quantifiers as low as possible.
 *
 * @author phlo
 */
public class ForAllDownExistsDown extends SimpleUpDownStrategy {

	boolean selectForAll(long numQPath, long numCritical) {
		return numCritical - numQPath <= 1;
	}

	boolean selectExists(long numQPath, long numCritical) {
		return numCritical - numQPath <= 1;
	}
}
