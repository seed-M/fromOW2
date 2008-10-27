package org.sat4j.minisat.constraints.cnf;

import org.sat4j.minisat.core.ILits;
import org.sat4j.minisat.core.UnitPropagationListener;
import org.sat4j.specs.ContradictionException;
import org.sat4j.specs.IVecInt;

public abstract class Clauses {
	/**
	 * Perform some sanity check before constructing a clause a) if a literal is
	 * assigned true, return null (the clause is satisfied) b) if a literal is
	 * assigned false, remove it c) if a clause contains a literal and its
	 * opposite (tautology) return null d) remove duplicate literals e) if the
	 * clause is empty, return null f) if the clause if unit, transmit it to the
	 * object responsible for unit propagation
	 * 
	 * @param ps
	 * 		the list of literals
	 * @param voc
	 * 		the vocabulary used
	 * @param s
	 * 		the object responsible for unit propagation
	 * @return null if the clause should be ignored, the (possibly modified)
	 * 	list of literals otherwise
	 * @throws ContradictionException
	 * 		if discovered by unit propagation
	 */
	public static IVecInt sanityCheck(IVecInt ps, ILits voc,
			UnitPropagationListener s) throws ContradictionException {
		// si un litt???ral de ps est vrai, retourner vrai
		// enlever les litt???raux falsifi???s de ps
		for (int i = 0; i < ps.size();) {
			// on verifie si le litteral est affecte
			if (voc.isUnassigned(ps.get(i))) {
				// on passe au literal suivant
				i++;
			} else {
				// Si le litteral est satisfait, la clause est
				// satisfaite
				if (voc.isSatisfied(ps.get(i))) {
					// on retourne la clause
					return null;
				}
				// on enleve le ieme litteral
				ps.delete(i);

			}
		}

		// on trie le vecteur ps
		ps.sortUnique();

		// ???limine les clauses tautologiques
		// deux litt???raux de signe oppos???s apparaissent dans la m???me
		// clause
		for (int i = 0; i < ps.size() - 1; i++) {
			if (ps.get(i) == (ps.get(i + 1) ^ 1)) {
				// la clause est tautologique
				return null;
			}
		}

		if (propagationCheck(ps, s))
			return null;

		return ps;
	}
	
	/**
	 * Check if this clause is null or unit
	 * 
	 * @param p
	 * 		the list of literals (supposed to be clean as after a call to
	 * 		sanityCheck())
	 * @param s
	 * 		the object responsible for unit propagation
	 * @return true iff the clause should be ignored (because it's unit)
	 * @throws ContradictionException
	 * 		when detected by unit propagation
	 */
	static boolean propagationCheck(IVecInt ps, UnitPropagationListener s)
			throws ContradictionException {
		if (ps.size() == 0) {
			throw new ContradictionException("Creating Empty clause ?"); //$NON-NLS-1$
		} else if (ps.size() == 1) {
			if (!s.enqueue(ps.get(0))) {
				throw new ContradictionException("Contradictory Unit Clauses"); //$NON-NLS-1$
			}
			return true;
		}

		return false;
	}


}
