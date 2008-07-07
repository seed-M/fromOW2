package org.sat4j;

import org.sat4j.core.ASolverFactory;
import org.sat4j.minisat.IProofDelegate;
import org.sat4j.minisat.SolverFactory;
import org.sat4j.minisat.core.Solver;
import org.sat4j.minisat.proof.ProofDelegate;
import org.sat4j.specs.ISolver;

public class ProofLauncher<T extends ISolver> extends BasicLauncher {
	private static final long serialVersionUID = 1L;
	
	private final IProofDelegate proof;
	private Solver solver;
	
	
	public ProofLauncher(ASolverFactory<T> factory){
		super(factory);
		proof = new ProofDelegate();
	}
	
	
	protected ISolver configureSolver(String[] args) {
	       solver = (Solver) SolverFactory.newDefault();
	       solver.setTimeout(Integer.MAX_VALUE);
	       solver.setProof(proof);
	       proof.setSolver(solver);
	       log(solver.toString(COMMENT_PREFIX)); //$NON-NLS-1$
	       
	       return solver;
    }
	
	
	protected void check(){
		proof.check();
	}
	
	
	public ISolver getSolver(){
		return this.solver;
	}
	
	
	public static void main (String args[]){
		ProofLauncher<ISolver> pl = new ProofLauncher<ISolver>(SolverFactory.instance());
		pl.run(args);
		
		if (pl.getExitCode() == ExitCode.UNSATISFIABLE){
			pl.check();
		}
		
		System.exit(pl.getExitCode().value());
	}
}
