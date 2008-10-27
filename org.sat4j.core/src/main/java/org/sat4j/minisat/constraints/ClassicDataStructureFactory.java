package org.sat4j.minisat.constraints;

import org.sat4j.minisat.constraints.card.AtLeast;
import org.sat4j.minisat.constraints.cnf.CBClause;
import org.sat4j.minisat.constraints.cnf.Clauses;
import org.sat4j.minisat.constraints.cnf.Lits;
import org.sat4j.minisat.core.Constr;
import org.sat4j.minisat.core.ILits;
import org.sat4j.minisat.core.Propagatable;
import org.sat4j.specs.ContradictionException;
import org.sat4j.specs.IVec;
import org.sat4j.specs.IVecInt;

public class ClassicDataStructureFactory extends
		AbstractDataStructureFactory<ILits> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

    /*
     * (non-Javadoc)
     * 
     * @see org.sat4j.minisat.core.DataStructureFactory#conflictDetectedInWatchesFor(int,
     *      int)
     */
    @Override
    public void conflictDetectedInWatchesFor(int p, int i) {
        // to nothing
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.sat4j.minisat.core.DataStructureFactory#getWatchesFor(int)
     */
    @Override
    public IVec<Propagatable> getWatchesFor(int p) {
        return getVocabulary().watches(p);
    }
    
	@Override
	protected ILits createLits() {
		return new Lits();
	}

	@Override
	public Constr createClause(IVecInt literals) throws ContradictionException {
		IVecInt v = Clauses.sanityCheck(literals, getVocabulary(), solver);
		if (v == null)
			return null;
		return CBClause.brandNewClause(solver, getVocabulary(), v);
	}

	@Override
	public Constr createUnregisteredClause(IVecInt literals) {
		return new CBClause(literals, getVocabulary());
	}

	@Override
	public Constr createCardinalityConstraint(IVecInt literals, int degree)
			throws ContradictionException {
		return AtLeast.atLeastNew(solver, getVocabulary(), literals, degree);
	}

}
