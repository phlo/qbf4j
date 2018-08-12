package at.jku.fmv.qbf.pnf;

/**
 * ∃↓∀↑ (prioritizing existential quantifiers)
 * <p>
 * Places existential quantifiers as low and universal quantifiers as high as
 * possible.
 *
 * @author phlo
 */
public class ExistsDownForAllUp extends SimpleUpDownStrategy {

	boolean selectForAll(long numQPath, long numCritical) { return true; }

	boolean selectExists(long numQPath, long numCritical) {
		return numCritical - numQPath <= 1;
	}
}
