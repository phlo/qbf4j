package at.jku.fmv.qbf.pnf;

/**
 * ∀↑∃↓ (prioritizing universal quantifiers)
 * <p>
 * Places universal quantifiers as high and existential quantifiers as low as
 * possible.
 *
 * @author phlo
 */
public class ForAllUpExistsDown extends SimpleUpDownStrategy {

	boolean selectForAll(long numQPath, long numCritical) { return true; }

	boolean selectExists(long numQPath, long numCritical) {
		return numQPath > 1 || numCritical <= 2;
	}
}
