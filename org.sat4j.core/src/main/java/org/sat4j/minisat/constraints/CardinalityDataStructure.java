/*******************************************************************************
* SAT4J: a SATisfiability library for Java Copyright (C) 2004-2008 Daniel Le Berre
*
* All rights reserved. This program and the accompanying materials
* are made available under the terms of the Eclipse Public License v1.0
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v10.html
*
* Alternatively, the contents of this file may be used under the terms of
* either the GNU Lesser General Public License Version 2.1 or later (the
* "LGPL"), in which case the provisions of the LGPL are applicable instead
* of those above. If you wish to allow use of your version of this file only
* under the terms of the LGPL, and not to allow others to use your version of
* this file under the terms of the EPL, indicate your decision by deleting
* the provisions above and replace them with the notice and other provisions
* required by the LGPL. If you do not delete the provisions above, a recipient
* may use your version of this file under the terms of the EPL or the LGPL.
* 
* Based on the original MiniSat specification from:
* 
* An extensible SAT solver. Niklas Een and Niklas Sorensson. Proceedings of the
* Sixth International Conference on Theory and Applications of Satisfiability
* Testing, LNCS 2919, pp 502-518, 2003.
*
* See www.minisat.se for the original solver in C++.
* 
*******************************************************************************/
package org.sat4j.minisat.constraints;

import org.sat4j.minisat.constraints.card.AtLeast;
import org.sat4j.minisat.constraints.cnf.LearntHTClause;
import org.sat4j.minisat.constraints.cnf.UnitClause;
import org.sat4j.minisat.core.Constr;
import org.sat4j.specs.ContradictionException;
import org.sat4j.specs.IVecInt;

/**
 * @author leberre To change the template for this generated type comment go to
 *         Window - Preferences - Java - Code Generation - Code and Comments
 */
public class CardinalityDataStructure extends AbstractCardinalityDataStructure {

    private static final long serialVersionUID = 1L;

    public Constr createUnregisteredClause(IVecInt literals) {
    	if (literals.size()==1) {
    		return new UnitClause(literals.last());
    	}
        return new LearntHTClause(literals, getVocabulary());
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.sat4j.minisat.DataStructureFactory#createClause(org.sat4j.datatype.VecInt)
     */
    public Constr createClause(IVecInt literals) throws ContradictionException {
        return AtLeast.atLeastNew(solver, getVocabulary(), literals, 1);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.sat4j.minisat.DataStructureFactory#createCardinalityConstraint(org.sat4j.datatype.VecInt,
     *      int)
     */
    @Override
    public Constr createCardinalityConstraint(IVecInt literals, int degree)
            throws ContradictionException {
        return AtLeast.atLeastNew(solver, getVocabulary(), literals, degree);
    }

}
