package org.sat4j.minisat.core;

import org.sat4j.specs.IVecInt;

public interface ModelAnalyzer<D extends DataStructureFactory> {

	/**
	 * Allow to configure the behavior of the solver when a model is found.
	 * 
	 * @param internalModel
	 *            a model in the solver' internal representation.
	 * @param dsf
	 *            a data structure factory
	 * @return a constraint to continue the search, or null to stop the search.
	 */
	Constr analyze(IVecInt internalModel, D dsf);
}
