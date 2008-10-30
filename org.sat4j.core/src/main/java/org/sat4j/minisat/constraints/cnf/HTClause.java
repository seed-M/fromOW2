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

import static org.sat4j.core.LiteralsUtils.neg;

import java.io.Serializable;

import org.sat4j.minisat.core.Constr;
import org.sat4j.minisat.core.ILits;
import org.sat4j.minisat.core.UnitPropagationListener;
import org.sat4j.specs.IVecInt;

/**
 * Lazy data structure for clause using the Head Tail data structure from SATO,
 * The original scheme is improved by avoiding moving pointers to literals but 
 * moving the literals themselves.
 * 
 * @author leberre
 */
public abstract class HTClause implements Constr, Serializable {

	private static final long serialVersionUID = 1L;

	private double activity;

	protected final int[] lits;

	protected final ILits voc;

	protected static final int HEAD = 0;

	protected final int tail;

	/**
	 * Creates a new basic clause
	 * 
	 * @param voc
	 *            the vocabulary of the formula
	 * @param ps
	 *            A VecInt that WILL BE EMPTY after calling that method.
	 */
	public HTClause(IVecInt ps, ILits voc) {
		lits = new int[ps.size()];
		ps.moveTo(lits);
		assert ps.size() == 0;
		this.voc = voc;
		activity = 0;
		tail = lits.length - 1;
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
	 *            the literals to store in the clause
	 * @return the created clause or null if the clause should be ignored
	 *         (tautology for example)
	 */
	public static HTClause brandNewClause(UnitPropagationListener s, ILits voc,
			IVecInt literals) {
		HTClause c = new DefaultHTClause(literals, voc);
		c.register();
		return c;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see Constr#calcReason(Solver, Lit, Vec)
	 */
	public void calcReason(int p, IVecInt outReason) {
		final int[] mylits = lits;
		for (int i = 0; i < mylits.length; i++) {
			if (voc.isFalsified(mylits[i])) {
				outReason.push(neg(mylits[i]));
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see Constr#remove(Solver)
	 */
	public void remove() {
		voc.attaches(neg(lits[HEAD])).remove(this);
		voc.attaches(neg(lits[tail])).remove(this);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see Constr#simplify(Solver)
	 */
	public boolean simplify() {
		for (int i = 0; i < lits.length; i++) {
			if (voc.isSatisfied(lits[i])) {
				return true;
			}
		}
		return false;
	}

	public boolean propagate(UnitPropagationListener s, int p) {
		final int[] mylits = lits;
		if (mylits[HEAD] == neg(p)) {
			int temphead = HEAD + 1;
			// moving head on the right
			while (temphead < tail && voc.isFalsified(mylits[temphead])) {
				temphead++;
			}
			assert temphead <= tail;
			if (temphead == tail) {
				voc.attach(p, this);
				return s.enqueue(mylits[tail], this);
			}
			mylits[HEAD] = mylits[temphead];
			mylits[temphead] = neg(p);
			voc.attach(neg(mylits[HEAD]), this);
			return true;
		}
		assert mylits[tail] == neg(p);
		int temptail = tail - 1;
		// moving tail on the left
		while ( HEAD < temptail && voc.isFalsified(mylits[temptail])) {
			temptail--;
		}
		assert HEAD <= temptail;
		if (HEAD == temptail) {
			voc.attach(p, this);
			return s.enqueue(mylits[HEAD], this);
		}
		mylits[tail] = mylits[temptail];
		mylits[temptail] = neg(p);
		voc.attach(neg(mylits[tail]), this);
		return true;
	}

	/*
	 * For learnt clauses only @author leberre
	 */
	public boolean locked() {
		return voc.getReason(lits[HEAD]) == this || voc.getReason(lits[tail]) == this;
	}

	/**
	 * @return the activity of the clause
	 */
	public double getActivity() {
		return activity;
	}

	@Override
	public String toString() {
		StringBuffer stb = new StringBuffer();
		for (int i = 0; i < lits.length; i++) {
			stb.append(Lits.toString(lits[i]));
			stb.append("["); //$NON-NLS-1$
			stb.append(voc.valueToString(lits[i]));
			stb.append("]"); //$NON-NLS-1$
			stb.append(" "); //$NON-NLS-1$
		}
		return stb.toString();
	}

	/**
	 * Retourne le ieme literal de la clause. Attention, cet ordre change durant
	 * la recherche.
	 * 
	 * @param i
	 *            the index of the literal
	 * @return the literal
	 */
	public int get(int i) {
		return lits[i];
	}

	/**
	 * @param claInc
	 */
	public void incActivity(double claInc) {
		activity += claInc;
	}

	/**
	 * @param d
	 */
	public void rescaleBy(double d) {
		activity *= d;
	}

	public int size() {
		return lits.length;
	}

	public void assertConstraint(UnitPropagationListener s) {
		final int[] mylits = lits;
		boolean ret;	
		if (voc.isUnassigned(mylits[HEAD])) {
		 ret = s.enqueue(mylits[HEAD], this);
		} else {
			assert voc.isUnassigned(mylits[tail]);
			ret = s.enqueue(mylits[tail], this);
		}
		assert ret;
	}

	public ILits getVocabulary() {
		return voc;
	}

	public int[] getLits() {
		int[] tmp = new int[size()];
		System.arraycopy(lits, 0, tmp, 0, size());
		return tmp;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null)
			return false;
		try {
			HTClause wcl = (HTClause) obj;
			if (lits.length != wcl.lits.length)
				return false;
			boolean ok;
			for (int lit : lits) {
				ok = false;
				for (int lit2 : wcl.lits)
					if (lit == lit2) {
						ok = true;
						break;
					}
				if (!ok)
					return false;
			}
			return true;
		} catch (ClassCastException e) {
			return false;
		}
	}

	@Override
	public int hashCode() {
		long sum = 0;
		for (int p : lits) {
			sum += p;
		}
		return (int) sum / lits.length;
	}
}
