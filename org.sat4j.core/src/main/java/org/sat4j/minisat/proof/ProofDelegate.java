package org.sat4j.minisat.proof;

import static org.sat4j.core.LiteralsUtils.var;
import static org.sat4j.minisat.IProof.CLAUSE_ID_NULL;

import org.sat4j.core.VecInt;
import org.sat4j.minisat.IProofDelegate;
import org.sat4j.minisat.constraints.cnf.WLClause;
import org.sat4j.minisat.core.ILits;
import org.sat4j.minisat.core.Solver;
import org.sat4j.specs.IConstr;
import org.sat4j.specs.IVecInt;

public class ProofDelegate implements IProofDelegate {
	private final Proof proof;
	private final IVecInt unitId;
	private Solver s;
	private ILits voc;
	private int conflId;  // id of the last conflict clause ; this is the id we will return if there was no resolving
	
	public ProofDelegate(){
		proof  = new Proof();
		unitId = new VecInt();
		conflId = CLAUSE_ID_NULL;
		WLClause.setProof(this);
	}
	
	
	public void setSolver (Solver s){
		this.s   = s;
		this.voc = s.getVocabulary();
	}
	
	
	public IVecInt unitId(){
		return unitId;
	}
	
	
	public int newClause(IVecInt vlits) throws ProofAssertionError{
		IVecInt vclone = new VecInt();
		
		vlits.copyTo(vclone);
		vclone.sortUnique();
		
		if (trueClause(vclone)){
			return CLAUSE_ID_NULL;
		}
		
		proof.beginChain(proof.addRoot(vclone));
		findFalsified(vclone);
		int id = proof.endChain();
		
		//System.out.println(id + " : " + vlits);
        
		if (vclone.size() == 1){
			unitId.set(var(vclone.get(0)) - 1, id);			
		}
		
		return id;
	}
	
	
	private int newLearntClause(IVecInt vlits){
		assert vlits.size() > 0;		
		int id = proof.last();
		
		if (vlits.size() == 1){
			unitId.set(var(vlits.get(0)) - 1, id);
		}
		
		//System.out.println ("L " + id + " : " + vlits);
		
		return id;
	}
	
	
	/* Is the clause true ? (tautology or satisfied literal) */
	private boolean trueClause (IVecInt vlits){
        for (int i = 0; i < vlits.size() - 1; i++) {
        	if (vlits.get(i) == (vlits.get(i + 1) ^ 1)) {
                // la clause est tautologique
                return true;
            }
        }
        
        for (int i = 0 ; i < vlits.size() ; i++){
        	if (voc.isSatisfied(vlits.get(i))){
        		return true;
        	}
        }
        
        return false;
	}
	
	
	private void findFalsified (IVecInt vlits){
        int i = 0;
        
        while (i < vlits.size()){
        	int cur = vlits.get(i);
        	
        	if (voc.isFalsified(cur)){
        		vlits.delete(i);
        		proof.resolve(unitId.get(var(cur) - 1), var(cur));
        	}
        	else{
        		i++;
        	}
        }
	}
	
	
	public void setId (IConstr clause, int id){
		//'clause' can be null if we just made a unit clause, for example :
		// just ignore this case as it is not relevant
		if (clause != null){
			WLClause c = (WLClause) clause;
			c.setId(id);
		}
	}
	
	
	public void deleted(IConstr gone){
		proof.deleted(((WLClause) gone).getId());
	}
	
	
	public void beginChain (IConstr start){
		assert start != null;
		
		int id = ((WLClause) start).getId();
		assert id != CLAUSE_ID_NULL;
		
		conflId = id;
		proof.beginChain(id);
	}


	public void resolveFromId(int var) {
		assert unitId.get(var - 1) != CLAUSE_ID_NULL;
		
		conflId = CLAUSE_ID_NULL;
		proof.resolve(unitId.get(var - 1), var);
	}


	public void resolve(IConstr next, int var) {
		assert next != null;
		int id = ((WLClause) next).getId();
		assert next.learnt() || id != CLAUSE_ID_NULL;
		
		conflId = CLAUSE_ID_NULL;
		
		if (id != CLAUSE_ID_NULL){
			proof.resolve(id, var);
		}
	}
	
	
	public void newVar(int howmany){
		for (int i = 0 ; i < howmany ; i++){
			unitId.push(CLAUSE_ID_NULL);
		}
	}
	
	
	public int afterAnalyze(IVecInt outLearnt, IVecInt analyzetoclear) throws ProofAssertionError {
		/* analyzetoclear.sort();
		
		for (int i = 0; i < analyzetoclear.size(); i++){
			System.err.println((i+1) + " / " + analyzetoclear.size());
			int v = var(analyzetoclear.get(i));
			System.err.println(v + " / " + analyzetoclear + " et " + outLearnt);
			assert voc.getLevel(v) > 0;
			IConstr c = voc.getReason(v);
			resolve(c, v);
			
			for (int j = 1; j < c.size(); j++){
				if (voc.getLevel(var(c.get(j))) == 0){
					proof.resolve(unitId.get(var(c.get(j)) - 1), var(c.get(j)));
				}
			}
		}*/
		
		proof.endChain();
		
		if (conflId == CLAUSE_ID_NULL){
			return newLearntClause(outLearnt);
		}
		else{
			int result = conflId;
			conflId    = CLAUSE_ID_NULL;
			
			return result;
		}
	}


	public void onPropagate(IConstr c, int[] lits) throws ProofAssertionError{
		if (s.decisionLevel() == 0){
			int first = lits[0];
			boolean firstFalsified = voc.isFalsified(first);
			int id;
		
			beginChain(c);
		
			for (int k = 1; k < lits.length; k++){
				resolveFromId(var(lits[k]));
			}

			id = this.proof.endChain();
			assert unitId.get(var(first) - 1) == CLAUSE_ID_NULL || firstFalsified;
    		
			if (!firstFalsified){
				unitId.set(var(first) - 1, id);
			}
			else{
				proof.beginChain(unitId.get(var(first) - 1));
				proof.resolve(id, var(first));
				proof.endChain();
			}
    	}
	}
	
	
	public void check() throws ProofAssertionError{
		proof.check();
	}
	
	
	public void check(int goalId) throws ProofAssertionError{
		proof.check(goalId);
	}
}
