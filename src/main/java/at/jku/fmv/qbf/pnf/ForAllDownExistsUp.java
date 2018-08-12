package at.jku.fmv.qbf.pnf;

/**
 * ∀↓∃↑ (prioritizing universal quantifiers)
 * <p>
 * Places universal quantifiers as low and existential quantifiers as high as
 * possible.
 *
 * @author phlo
 */
public class ForAllDownExistsUp extends SimpleUpDownStrategy {

	boolean selectForAll(long numQPath, long numCritical) {
		return numCritical - numQPath <= 1;
	}

	boolean selectExists(long numQPath, long numCritical) { return true; }
}
