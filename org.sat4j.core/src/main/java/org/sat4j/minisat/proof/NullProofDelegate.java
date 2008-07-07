package org.sat4j.minisat.proof;

import org.sat4j.minisat.IProof;
import org.sat4j.minisat.IProofDelegate;
import org.sat4j.minisat.core.Solver;
import org.sat4j.specs.IConstr;
import org.sat4j.specs.IVecInt;

public final class NullProofDelegate implements IProofDelegate {
	public NullProofDelegate(){}
	public void deleted(IConstr gone) {}
	public void beginChain (IConstr start){}
	
	public int newClause(IVecInt vlits) {
		return IProof.CLAUSE_ID_NULL;
	}
	
	public void setId (IConstr clause, int id){}
	public void resolveFromId(int var) {}
	public void resolve(IConstr next, int var) {}
	public void newVar(int howmany) {}
	
	public int afterAnalyze(IVecInt outLearnt, IVecInt analyzetoclear){
		return IProof.CLAUSE_ID_NULL;
	}
	
	public void onPropagate(IConstr c, int[] lits) {}
	public void setSolver(Solver s){}
	public void check() {}
	public void check(int goalId) {}
}
