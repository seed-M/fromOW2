package org.sat4j.minisat.constraints;

import org.sat4j.minisat.constraints.card.AtLeast;
import org.sat4j.minisat.constraints.cnf.Clauses;
import org.sat4j.minisat.constraints.cnf.LearntHTClause;
import org.sat4j.minisat.constraints.cnf.Lits;
import org.sat4j.minisat.constraints.cnf.OriginalHTClause;
import org.sat4j.minisat.core.Constr;
import org.sat4j.minisat.core.ILits;
import org.sat4j.specs.ContradictionException;
import org.sat4j.specs.IVecInt;

public class HTDataStructureFactory extends
		AbstractDataStructureFactory<ILits> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
    
	@Override
	protected ILits createLits() {
		return new Lits();
	}

	@Override
	public Constr createClause(IVecInt literals) throws ContradictionException {
		IVecInt v = Clauses.sanityCheck(literals, getVocabulary(), solver);
		if (v == null)
			return null;
		return OriginalHTClause.brandNewClause(solver, getVocabulary(), v);
	}

	@Override
	public Constr createUnregisteredClause(IVecInt literals) {
		return new LearntHTClause(literals, getVocabulary());
	}

	@Override
	public Constr createCardinalityConstraint(IVecInt literals, int degree)
			throws ContradictionException {
		return AtLeast.atLeastNew(solver, getVocabulary(), literals, degree);
	}

}
