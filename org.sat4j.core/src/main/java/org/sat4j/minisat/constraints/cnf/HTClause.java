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

	protected final int[] middleLits;

	protected final ILits voc;

	protected int head;

	protected int tail;

	private static final int[] NO_MIDDLE_LITS = new int[0];

	/**
	 * Creates a new basic clause
	 * 
	 * @param voc
	 *            the vocabulary of the formula
	 * @param ps
	 *            A VecInt that WILL BE EMPTY after calling that method.
	 */
	public HTClause(IVecInt ps, ILits voc) {
		assert ps.size() > 1;
		head = ps.get(0);
		ps.delete(0);
		tail = ps.last();
		ps.pop();
		if (ps.size() > 0) {
			middleLits = new int[ps.size()];
		} else {
			middleLits = NO_MIDDLE_LITS;
		}
		ps.moveTo(middleLits);
		assert ps.size() == 0;
		this.voc = voc;
		activity = 0;
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
		if (voc.isFalsified(head))  {
			outReason.push(head);
		}
		if (voc.isFalsified(tail))  {
			outReason.push(tail);
		}
		final int[] mylits = middleLits;
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
		voc.attaches(neg(head)).remove(this);
		voc.attaches(neg(tail)).remove(this);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see Constr#simplify(Solver)
	 */
	public boolean simplify() {
		if (voc.isSatisfied(head)||voc.isSatisfied(tail))  {
			return true;
		}
		for (int i = 0; i < middleLits.length; i++) {
			if (voc.isSatisfied(middleLits[i])) {
				return true;
			}
		}
		return false;
	}

	public boolean propagate(UnitPropagationListener s, int p) {
		
		if (head == neg(p)) {
			if (voc.isSatisfied(tail)) {
				voc.attach(p, this);
				return true;
			}
			final int[] mylits = middleLits;
			int temphead = 0;
			// moving head on the right
			while (temphead < mylits.length && voc.isFalsified(mylits[temphead])) {
				temphead++;
			}
			assert temphead <= mylits.length;
			if (temphead == mylits.length) {
				voc.attach(p, this);
				return s.enqueue(tail, this);
			}
			head = mylits[temphead];
			mylits[temphead] = neg(p);
			voc.attach(neg(head), this);
			return true;
		}
		assert tail == neg(p);
		if (voc.isSatisfied(head)) {
			voc.attach(p, this);
			return true;
		}
		final int[] mylits = middleLits;
		int temptail = mylits.length - 1;
		// moving tail on the left
		while (temptail>=0 && voc.isFalsified(mylits[temptail])) {
			temptail--;
		}
		assert -1 <= temptail;
		if (-1 == temptail) {
			voc.attach(p, this);
			return s.enqueue(head, this);
		}
		tail = mylits[temptail];
		mylits[temptail] = neg(p);
		voc.attach(neg(tail), this);
		return true;
	}

	/*
	 * For learnt clauses only @author leberre
	 */
	public boolean locked() {
		return voc.getReason(head) == this
				|| voc.getReason(tail) == this;
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
		stb.append(Lits.toString(head));
		stb.append("["); //$NON-NLS-1$
		stb.append(voc.valueToString(head));
		stb.append("]"); //$NON-NLS-1$
		stb.append(" "); //$NON-NLS-1$
		for (int i = 0; i < middleLits.length; i++) {
			stb.append(Lits.toString(middleLits[i]));
			stb.append("["); //$NON-NLS-1$
			stb.append(voc.valueToString(middleLits[i]));
			stb.append("]"); //$NON-NLS-1$
			stb.append(" "); //$NON-NLS-1$
		}
		stb.append(Lits.toString(tail));
		stb.append("["); //$NON-NLS-1$
		stb.append(voc.valueToString(tail));
		stb.append("]"); //$NON-NLS-1$
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
		if (i==0) return head;
		if (i==middleLits.length+1) return tail;
		return middleLits[i-1];
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
		return middleLits.length+2;
	}

	public void assertConstraint(UnitPropagationListener s) {
		boolean ret;
		if (voc.isUnassigned(head)) {
			ret = s.enqueue(head, this);
		} else {
			assert voc.isUnassigned(tail);
			ret = s.enqueue(tail, this);
		}
		assert ret;
	}

	public ILits getVocabulary() {
		return voc;
	}

	public int[] getLits() {
		int[] tmp = new int[size()];
		System.arraycopy(middleLits, 0, tmp, 1, middleLits.length);
		tmp[0]=head;
		tmp[tmp.length-1] = tail;
		return tmp;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null)
			return false;
		try {
			HTClause wcl = (HTClause) obj;
			if (wcl.head!= head || wcl.tail!=tail) {
				return false;
			}
			if (middleLits.length != wcl.middleLits.length)
				return false;
			boolean ok;
			for (int lit : middleLits) {
				ok = false;
				for (int lit2 : wcl.middleLits)
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
		long sum = head+tail;;
		for (int p : middleLits) {
			sum += p;
		}
		return (int) sum / middleLits.length;
	}
}
