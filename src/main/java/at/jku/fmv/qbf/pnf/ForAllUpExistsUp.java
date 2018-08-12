package at.jku.fmv.qbf.pnf;

/**
 * ∀↑∃↑
 * <p>
 * Places universal quantifiers and existential quantifiers as high as possible.
 *
 * @author phlo
 */
public class ForAllUpExistsUp extends SimpleUpDownStrategy {

	boolean selectForAll(long numQPath, long numCritical) { return true; }

	boolean selectExists(long numQPath, long numCritical) { return true; }
}
