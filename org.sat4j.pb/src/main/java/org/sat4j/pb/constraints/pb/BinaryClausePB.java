package org.sat4j.pb.constraints.pb;

import java.math.BigInteger;

import org.sat4j.minisat.constraints.cnf.BinaryClause;
import org.sat4j.minisat.core.ILits;
import org.sat4j.minisat.core.UnitPropagationListener;
import org.sat4j.specs.IVecInt;

public class BinaryClausePB extends BinaryClause implements PBConstr {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public BinaryClausePB(IVecInt ps, ILits voc) {
		super(ps, voc);
	}

	private boolean learnt = false;
	
	@Override
	public IVecInt computeAnImpliedClause() {
		return null;
	}

	@Override
	public BigInteger getCoef(int literal) {
		return BigInteger.ONE;
	}

	@Override
	public BigInteger[] getCoefs() {
		return new BigInteger [] {BigInteger.ONE,BigInteger.ONE};
	}

	@Override
	public BigInteger getDegree() {
		return BigInteger.ONE;
	}

	@Override
	public void setLearnt() {
		learnt = true;
	}

	@Override
	public boolean learnt() {
		return learnt;
	}
	
    /**
     * Creates a brand new clause, presumably from external data. Performs all
     * sanity checks.
     * 
     * @param s
     *            the object responsible for unit propagation
     * @param voc
     *            the vocabulary
     * @param literals
     *            the literals to store in the clause (size should be two)
     * @return the created clause or null if the clause should be ignored
     *         (tautology for example)
     */
    public static BinaryClausePB brandNewClause(UnitPropagationListener s,
            ILits voc, IVecInt literals) {
        BinaryClausePB c = new BinaryClausePB(literals, voc);
        c.register();
        return c;
    }

}
