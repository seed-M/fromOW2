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
package org.sat4j.minisat.opt;

import org.sat4j.core.LiteralsUtils;
import org.sat4j.core.VecInt;
import org.sat4j.minisat.core.Constr;
import org.sat4j.minisat.core.DataStructureFactory;
import org.sat4j.minisat.core.Solver;
import org.sat4j.specs.ContradictionException;
import org.sat4j.specs.IConstr;
import org.sat4j.specs.IVecInt;
import org.sat4j.specs.IteratorInt;
import org.sat4j.specs.TimeoutException;

/**
 * Computes a solution that satisfies the maximum of clauses.
 * 
 * @author daniel
 * 
 */
public final class MaxSatDecorator extends AbstractSelectorVariablesDecorator {

	/**
     * 
     */
	private static final long serialVersionUID = 1L;

	public MaxSatDecorator(Solver<DataStructureFactory> solver) {
		super(solver);
		solver.setModelAnalyzer(this);
	}

	@Override
	public void setExpectedNumberOfClauses(int nb) {
		super.setExpectedNumberOfClauses(nb);
		lits.ensure(nb);
	}

	@Override
	public IConstr addClause(IVecInt literals) throws ContradictionException {
		int newvar = nborigvars + ++nbnewvar;
		lits.push(newvar);
		literals.push(newvar);
		return super.addClause(literals);
	}

	@Override
	public void reset() {
		nbnewvar = 0;
		lits.clear();
		super.reset();
		prevConstr = null;
	}

	public boolean hasNoObjectiveFunction() {
		return false;
	}

	public boolean nonOptimalMeansSatisfiable() {
		return false;
	}

	private final IVecInt lits = new VecInt();

	private int counter;

	private IConstr prevConstr;

	/**
	 * @since 2.1
	 */
	public void discardCurrentSolution() throws ContradictionException {

	}

	@Override
	public boolean isSatisfiable(IVecInt assumps) throws TimeoutException {

		boolean result = super.isSatisfiable(assumps);
		if (!result) {
			if (prevConstr != null) {
				super.removeConstr(prevConstr);
				prevConstr = null;
			}
		}
		return result;
	}

	/**
	 * @since 2.1
	 */
	public Number getObjectiveValue() {
		return counter;
	}

	@Override
	void calculateObjectiveValue(IVecInt internalModel) {
		counter = 0;
		int q;
		for (IteratorInt it = internalModel.iterator(); it.hasNext();) {
			q = LiteralsUtils.toDimacs(it.next());
			if (q > nborigvars) {
				counter++;
			}
		}
	}

	/**
	 * @since 2.1
	 */
	public void forceObjectiveValueTo(Number forcedValue)
			throws ContradictionException {
		super.addAtMost(lits, forcedValue.intValue());
	}

	public Constr analyze(IVecInt internalModel, DataStructureFactory dsf) {
		calculateObjectiveValue(internalModel);
		IVecInt clause = new VecInt(internalModel.size());
		for (IteratorInt it = internalModel.iterator(); it.hasNext();) {
			clause.push(LiteralsUtils.neg(it.next()));
		}
		if (prevConstr != null) {
			super.removeSubsumedConstr(prevConstr);
		}
		try {
			prevConstr = dsf.createCardinalityConstraint(clause,
					internalModel.size() + 1 - counter);

		} catch (ContradictionException e) {
			return null;
		}
		return dsf.createUnregisteredClause(clause);
	}

}
