package org.sat4j.minisat;

import org.sat4j.specs.IVec;
import org.sat4j.specs.IVecInt;

public interface IProofTraverser {
	void root(IVecInt c);
	void chain(IVecInt clauseIds, IVecInt vars);
	void deleted(int clauseId);
	void done();
	
	IVec<IVecInt> getClauses();
}
