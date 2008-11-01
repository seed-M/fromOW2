package org.sat4j.minisat.constraints.cnf;

import org.sat4j.minisat.core.Constr;
import org.sat4j.minisat.core.UnitPropagationListener;
import org.sat4j.specs.IVecInt;

public class UnitClause implements Constr {

	private final int literal;
	
	public UnitClause(int value) {
		literal = value;
	}
	
	@Override
	public void assertConstraint(UnitPropagationListener s) {
		s.enqueue(literal, this);
	}

	@Override
	public void calcReason(int p, IVecInt outReason) {
		throw new UnsupportedOperationException();

	}

	@Override
	public double getActivity() {
		throw new UnsupportedOperationException();
	}

	@Override
	public void incActivity(double claInc) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean locked() {
		throw new UnsupportedOperationException();
	}

	@Override
	public void register() {
		throw new UnsupportedOperationException();
	}

	@Override
	public void remove() {
		throw new UnsupportedOperationException();
	}

	@Override
	public void rescaleBy(double d) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void setLearnt() {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean simplify() {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean propagate(UnitPropagationListener s, int p) {
		throw new UnsupportedOperationException();
    }

	@Override
	public int get(int i) {
		if (i>0) throw new IllegalArgumentException();
		return literal;
	}

	@Override
	public boolean learnt() {
		throw new UnsupportedOperationException();
	}

	@Override
	public int size() {
		return 1;
	}
}
