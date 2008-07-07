package org.sat4j.minisat.proof;

import org.sat4j.core.Vec;
import org.sat4j.core.VecInt;
import org.sat4j.minisat.IProofTraverser;
import org.sat4j.specs.IVec;
import org.sat4j.specs.IVecInt;

import static org.sat4j.core.LiteralsUtils.var;

public class SimpleProofTraverser implements IProofTraverser{
	private IVec<IVecInt> clauses;
	
	
	public SimpleProofTraverser(){
		clauses = new Vec<IVecInt>();
	}
	
	
	public void root(IVecInt clause){
		IVecInt c = new VecInt();
		clause.copyTo(c);
		clauses.push(c);
	}
	
	
	public void chain(IVecInt clauseIds, IVecInt vars) throws ProofAssertionError{
		IVecInt clause = new VecInt();

		clauses.get(clauseIds.get(0)).copyTo(clause);
		clauses.push(clause);

		for (int i = 0; i < vars.size(); i++){
			resolve(clause, clauses.get(clauseIds.get(i+1)), vars.get(i));
		}
	}
	
	
	public void deleted(int clauseId){
		clauses.get(clauseId).clear();
	}

	
	public void done(){
	}
	
	
	public IVec<IVecInt> getClauses(){
		return clauses;
	}
	
	
	private void resolve (IVecInt clause, IVecInt other, int var) throws ProofAssertionError{
		int p = 0; /* Dummy initialization to make Java happy */
		boolean ok1 = false;
		boolean ok2 = false;
		
		for (int i = 0 ; i < clause.size() ; i++){
			if (var(clause.get(i)) == var){
				ok1 = true;
				p = clause.get(i);
				// Remove last literal
				clause.set(i, clause.last());
				clause.pop();
				break;
			}
		}
		
		for (int i = 0 ; i < other.size() ; i++){
	    	if (var(other.get(i)) != var){
	    		clause.push(other.get(i));
	    	}else{
	    		if (p == other.get(i)){//p != ~other.get(i)
	    			throw new ProofAssertionError("Resolved on variable with SAME polarity in both clauses: " + var);
	    		}
	    		ok2 = true;
	    	}
		}

		if (!ok1 || !ok2){
			throw new ProofAssertionError("Resolved on missing variable: " + var);
		}

		clause.sortUnique();
	}
}
