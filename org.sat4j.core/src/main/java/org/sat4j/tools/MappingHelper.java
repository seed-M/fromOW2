package org.sat4j.tools;

import java.util.HashMap;
import java.util.Map;

import org.sat4j.core.Vec;
import org.sat4j.core.VecInt;
import org.sat4j.specs.ContradictionException;
import org.sat4j.specs.IConstr;
import org.sat4j.specs.ISolver;
import org.sat4j.specs.IVec;
import org.sat4j.specs.IVecInt;
import org.sat4j.specs.TimeoutException;
/**
 * Helper class intended to make life easier to people to feed a 
 * sat solver programmatically.
 * 
 * @author daniel
 *
 * @param <T> The class of the objects to map into boolean variables.
 */
public class MappingHelper<T> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private final Map<T, Integer> mapToDimacs = new HashMap<T, Integer>();
	private final IVec<T> mapToDomain;
	final IVec<IConstr> constrs = new Vec<IConstr>();
	final ISolver solver;
	
	public MappingHelper(ISolver solver, int maxvarid) {
		this.solver = solver;
		mapToDomain = new Vec<T>();
		mapToDomain.push(null);
	}

	int getIntValue(T thing) {
		Integer intValue = mapToDimacs.get(thing);
		if (intValue == null) {
			intValue = mapToDomain.size();
			mapToDomain.push(thing);
			mapToDimacs.put(thing, intValue);
		}
		return intValue;
	}

	public IVec<T> getSolution() {
		int[] model = solver.model();
		IVec<T> toInstall = new Vec<T>();
		for (int i : model) {
			if (i > 0) {
				toInstall.push(mapToDomain.get(i));
			}
		}
		return toInstall;
	}

	public boolean hasASolution() throws TimeoutException {
		return solver.isSatisfiable();
	}
	
	/**
	 * Easy way to feed the solver with implications.
	 * 
	 * @param x an array of things such that x[i] -> y for all i.
	 * @param y a thing implied by all the x[i].
	 * @throws ContradictionException
	 */
	public void addImplies(T [] x, T y) throws ContradictionException {
		IVecInt clause = new VecInt();
		for (T t : x) {
			clause.push(-getIntValue(t));
			clause.push(getIntValue(y));
			solver.addClause(clause);
			clause.clear();
		}
	}
	
	/**
	 * Easy way to feed the solver with implications.
	 * 
	 * @param x a thing such that x -> y[1] \/ y[2] ... \/ y[n]
	 * @param y an array of alternatives for x.
	 * @throws ContradictionException
	 */
	public void addImplies(T x, T [] y) throws ContradictionException {
		IVecInt clause = new VecInt();
		clause.push(-getIntValue(x));
		for (T t : y) {
			clause.push(getIntValue(t));
			clause.clear();
		}		
		solver.addClause(clause);
	}
}
