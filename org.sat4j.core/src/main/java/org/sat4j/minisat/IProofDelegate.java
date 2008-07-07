package org.sat4j.minisat;

import org.sat4j.minisat.core.Solver;
import org.sat4j.minisat.proof.ProofAssertionError;
import org.sat4j.specs.IConstr;
import org.sat4j.specs.IVecInt;

public interface IProofDelegate {
	//Constr newClause(IVecInt vlits, Constr clause);
	/**
	 * Informs the proof delegate a new clause has been taken into account
	 * @param vlits literals of the clause
	 * @param voc the current vocabulary
	 * @return id of the new clause, or CLAUSE_ID_NULL
	 * @throws ProofAssertionError 
	 */
	int newClause(IVecInt vlits);
	void setSolver (Solver s);
	void setId (IConstr clause, int id);
	void deleted (IConstr gone);
	void beginChain (IConstr start);
	void resolveFromId (int var);
	void resolve(IConstr next, int var);
	void newVar(int howmany);
	void onPropagate(IConstr c, int[] lits);
	int afterAnalyze(IVecInt outLearnt, IVecInt analyzetoclear);
	void check() throws ProofAssertionError;
	void check(int goalId) throws ProofAssertionError;
}
