package org.sat4j.minisat.proof;

import java.io.Serializable;
import java.util.Iterator;

import org.sat4j.core.VecInt;
import org.sat4j.minisat.IProof;
import org.sat4j.minisat.IProofTraverser;
import org.sat4j.specs.IVecInt;

public class Proof implements IProof, Serializable{
	private static final long serialVersionUID = 1L;
	
	private final IProofKeeper memory;
	private int idCounter;
	private final IProofTraverser trav;

	private final IVecInt clause;
	private final IVecInt chainId;
	private final IVecInt chainVar;
	
	public Proof(){
		//System.out.println ("New proof");

		idCounter = 0;
		trav      = new NullProofTraverser();
		memory    = new MemoryProofKeeper();
		chainId   = new VecInt();
		chainVar  = new VecInt();
		clause    = new VecInt();
	}
	
	
	public Proof(IProofTraverser pt){
		//System.out.println ("New proof with traverser");

		idCounter = 0;
		trav      = pt;
		memory    = new MemoryProofKeeper();
		chainId   = new VecInt();
		chainVar  = new VecInt();
		clause    = new VecInt();
	}
	
	
	public int addRoot (IVecInt lits){
		//System.out.println ("add root " + (idCounter + 1));
		
		clause.clear();
		lits.copyTo(clause);
		clause.sortUnique();
		
		trav.root(clause);
		memory.putInt(clause.get(0)<< 1);

		for (int i = 1; i < clause.size(); i++){
			memory.putInt(clause.get(i) - clause.get(i-1));
		}

		memory.putInt(0); // (0 is safe terminator since we removed duplicates)

		return idCounter++;
	}
	
	
	public void beginChain (int start){
		//System.out.println("Begin chain");

		assert(start != CLAUSE_ID_NULL);
		chainId.clear();
		chainVar.clear();
		chainId.push(start);
	}


	public void resolve (int next, int var){
		//System.out.println ("Resolve");

		assert(next != CLAUSE_ID_NULL);
		chainId.push(next);
		chainVar.push(var);
	}
	
	
	public int endChain() throws ProofAssertionError{
		//System.out.println ("End chain");

		assert(chainId.size() == chainVar.size() + 1);

		if (chainId.size() == 1){
			return chainId.get(0);
		}
		else{
			//System.out.println("Conflit : " + chainId + " et " + chainVar);
			trav.chain(chainId, chainVar);
			memory.putInt(((idCounter - chainId.get(0)) << 1) | 1);

			for (int i = 0; i < chainVar.size(); i++){
				memory.putInt(chainVar.get(i));
				memory.putInt(idCounter - chainId.get(i+1));
			}

			memory.putInt(0);

			return idCounter++;
		}
	}


	public void deleted(int gone){
		//System.out.println ("Deleted");
		
		trav.deleted(gone);
		memory.putInt(((idCounter - gone) << 1) | 1);
		memory.putInt(0);
	}
	
	
	public int last(){
		return idCounter - 1;
	}
	
	
	public void traverse (IProofTraverser pt) throws ProofAssertionError{
		traverse (pt, CLAUSE_ID_NULL);
	}
	
	
	public void traverse (IProofTraverser pt, int goal) throws ProofAssertionError{
		Iterator<Integer> it = memory.iterator();
		
		goal = (goal == CLAUSE_ID_NULL) ? last() : goal;
		
		for (int id = 0; id <= goal; id++){
			int tmp = it.next();
			
			if ((tmp & 1) == 0){
				traverseClause(pt, tmp, it);
			}
			else{
				id = traverseChaining(id, pt, tmp, it);
			}
		}
		pt.done();
	}
	
	
	private int traverseChaining(int id, IProofTraverser pt, int tmp, Iterator<Integer> it) {
		chainId .clear();
		chainVar.clear();
		chainId.push(id - (tmp >> 1));

		for(;;){
			tmp = it.next();
	
			if (tmp == 0) break;
			
			chainVar.push(tmp);
			chainId.push(id - it.next());
		}

		if (chainVar.size() == 0){
			id--;   // (no new clause introduced)
			pt.deleted(chainId.get(0));
		}
		else{
			pt.chain(chainId, chainVar);
		}
		
		return id;
	}


	private void traverseClause(IProofTraverser pt, int tmp, Iterator<Integer> it) {
		int idx = tmp >> 1;
		
		clause.clear();
		clause.push(idx);

		for(;;){
			tmp = it.next();
			
			if (tmp == 0) break;
			
			idx += tmp;
			clause.push(idx);
		}

		pt.root(clause);
	}


	public void check () throws ProofAssertionError{
		check(CLAUSE_ID_NULL);
	}
	
	
	public void check (int goal) throws ProofAssertionError{
		IProofTraverser trav = new SimpleProofTraverser();

		System.out.println ("c Check proof !");

		traverse(trav, goal);

		IVecInt clause = trav.getClauses().last();
		System.out.print("c Final clause:");
		System.out.println(clause.size() == 0 ? " <empty>" : clause);
	}
}
