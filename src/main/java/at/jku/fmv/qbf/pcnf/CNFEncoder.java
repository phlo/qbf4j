package at.jku.fmv.qbf.pcnf;

import java.util.List;
import java.util.function.Function;

import at.jku.fmv.qbf.QBF;
import at.jku.fmv.qbf.QBF.*;

public interface CNFEncoder {

	public List<QBF> getClauses(QBF skeleton);

	public default QBF apply(QBF pnf) {
		Function<QBF, QBF> applySkeleton = s ->
			s.isCNF() ? s : new And(getClauses(s));

		Function<Quantifier, QBF> applyQuantifier = q ->
			q.subformula.isQuantifier()
				? apply(q.subformula)
				: applySkeleton.apply(q.subformula);

		return pnf.isQuantifier()
			? pnf.apply(
				f -> new ForAll(applyQuantifier.apply(f), f.variables),
				e -> new Exists(applyQuantifier.apply(e), e.variables))
			: applySkeleton.apply(pnf);
	}
}
