package at.jku.fmv.qbf.pnf;

// ∀↑∃↑
public class ForAllUpExistsUp extends SimpleUpDownStrategy {

	boolean selectForAll(long numQPath, long numCritical) { return true; }

	boolean selectExists(long numQPath, long numCritical) { return true; }
}
