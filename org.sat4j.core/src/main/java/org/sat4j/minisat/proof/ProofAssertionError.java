package org.sat4j.minisat.proof;

public class ProofAssertionError extends AssertionError {
	private static final long serialVersionUID = 1L;
	
	public ProofAssertionError(String mesg){
		super("PROOF ERROR : " + mesg);
	}
}
