package org.sat4j.minisat;

import org.sat4j.minisat.proof.ProofAssertionError;
import org.sat4j.specs.IVecInt;

public interface IProof {
	public static final int CLAUSE_ID_NULL = -1;
	
	int addRoot (IVecInt clause);
	void beginChain (int start);
	void resolve (int next, int var);
	int endChain();
	void deleted(int gone);
	int last();
	void traverse (IProofTraverser pt);
	void traverse (IProofTraverser pt, int goal);
	void check () throws ProofAssertionError;
	void check (int goal) throws ProofAssertionError;
}
