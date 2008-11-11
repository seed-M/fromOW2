package org.sat4j.pb.constraints.pb;

import java.math.BigInteger;

import org.sat4j.minisat.constraints.cnf.UnitClause;
import org.sat4j.minisat.core.ILits;
import org.sat4j.specs.IVecInt;

public class UnitClausePB extends UnitClause implements PBConstr {

	public UnitClausePB(int value) {
		super(value);
	}

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
		return new BigInteger[] {BigInteger.ONE};
	}

	@Override
	public BigInteger getDegree() {
		return BigInteger.ONE;
	}

	@Override
	public int[] getLits() {
		return new int[] {literal};
	}

	@Override
	public ILits getVocabulary() {
		return null;
	}

}
