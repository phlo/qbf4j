package at.jku.fmv.qbf.pnf;

import at.jku.fmv.qbf.QBF;

public interface PrenexingStrategy {

	/**
	 * Apply the prenexing strategy to the given formula.
	 *
	 * @param formula QBF in NNF
	 * @return QBF in PNF
	 */
	public QBF apply(QBF formula);
}
