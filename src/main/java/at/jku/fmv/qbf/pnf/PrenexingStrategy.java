package at.jku.fmv.qbf.pnf;

import at.jku.fmv.qbf.QBF;

/**
 * A prenexing strategy.
 *
 * @author phlo
 */
public interface PrenexingStrategy {

	/**
	 * Applies the prenexing strategy to the given formula.
	 *
	 * @param formula {@link QBF} in NNF
	 * @return {@link QBF} in PNF
	 */
	public QBF apply(QBF formula);
}
