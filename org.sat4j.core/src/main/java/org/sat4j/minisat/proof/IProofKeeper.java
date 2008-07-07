package org.sat4j.minisat.proof;

import java.util.Iterator;

/**
 * Keeps track of a proof
 * @author delorme
 *
 */
public interface IProofKeeper {
	void putInt(int i);
	Iterator<Integer> iterator();
}
