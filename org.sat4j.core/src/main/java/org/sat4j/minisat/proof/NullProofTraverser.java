package org.sat4j.minisat.proof;

import org.sat4j.minisat.IProofTraverser;
import org.sat4j.specs.IVec;
import org.sat4j.specs.IVecInt;

public final class NullProofTraverser implements IProofTraverser {

	public void chain(IVecInt clauseIds, IVecInt vars) {
	}

	public void deleted(int clauseId) {
	}

	public void done() {
	}

	public IVec<IVecInt> getClauses() {
		return null;
	}

	public void root(IVecInt c) {
	}
}
