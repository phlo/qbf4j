package at.jku.fmv.qbf.pnf;

/**
 * ∃↑∀↓ (prioritizing existential quantifiers)
 * <p>
 * Places existential quantifiers as high and universal quantifiers as low as
 * possible.
 *
 * @author phlo
 */
public class ExistsUpForAllDown extends SimpleUpDownStrategy {

	boolean selectForAll(long numQPath, long numCritical) {
		return numQPath > 1 || numCritical <= 2;
	}

	boolean selectExists(long numQPath, long numCritical) { return true; }
}
