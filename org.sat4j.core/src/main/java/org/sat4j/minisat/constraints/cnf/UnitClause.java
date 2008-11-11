/*******************************************************************************
* SAT4J: a SATisfiability library for Java Copyright (C) 2004-2008 Daniel Le Berre
*
* All rights reserved. This program and the accompanying materials
* are made available under the terms of the Eclipse Public License v1.0
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v10.html
*
* Alternatively, the contents of this file may be used under the terms of
* either the GNU Lesser General Public License Version 2.1 or later (the
* "LGPL"), in which case the provisions of the LGPL are applicable instead
* of those above. If you wish to allow use of your version of this file only
* under the terms of the LGPL, and not to allow others to use your version of
* this file under the terms of the EPL, indicate your decision by deleting
* the provisions above and replace them with the notice and other provisions
* required by the LGPL. If you do not delete the provisions above, a recipient
* may use your version of this file under the terms of the EPL or the LGPL.
* 
* Based on the original MiniSat specification from:
* 
* An extensible SAT solver. Niklas Een and Niklas Sorensson. Proceedings of the
* Sixth International Conference on Theory and Applications of Satisfiability
* Testing, LNCS 2919, pp 502-518, 2003.
*
* See www.minisat.se for the original solver in C++.
* 
*******************************************************************************/
package org.sat4j.minisat.constraints.cnf;

import org.sat4j.minisat.core.Constr;
import org.sat4j.minisat.core.UnitPropagationListener;
import org.sat4j.specs.IVecInt;

public class UnitClause implements Constr {

	protected final int literal;
	
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
