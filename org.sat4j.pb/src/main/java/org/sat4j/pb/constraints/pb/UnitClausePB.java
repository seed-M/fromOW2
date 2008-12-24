package org.sat4j.pb.constraints.pb;

import java.math.BigInteger;

import org.sat4j.minisat.constraints.cnf.UnitClause;
import org.sat4j.minisat.core.ILits;
import org.sat4j.specs.IVecInt;

public class UnitClausePB extends UnitClause implements PBConstr {

	public UnitClausePB(int value) {
		super(value);
	}

	public IVecInt computeAnImpliedClause() {
		return null;
	}

	public BigInteger getCoef(int theLiteral1) {
		return BigInteger.ONE;
	}

	public BigInteger[] getCoefs() {
		return new BigInteger[] {BigInteger.ONE};
	}

	public BigInteger getDegree() {
		return BigInteger.ONE;
	}

	public int[] getLits() {
		return new int[] {literal};
	}

	public ILits getVocabulary() {
		return null;
	}

}
