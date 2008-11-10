package org.sat4j.tools;

import java.util.Collection;
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
	
	public MappingHelper(ISolver solver) {
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
	 * @param x a collection of things such that x[i] -> y for all i.
	 * @param y a thing implied by all the x[i].
	 * @throws ContradictionException
	 */
	public void addImplies(Collection<T> x, T y) throws ContradictionException {
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
	 * @param x a thing such that x -> y[i]  for all i
	 * @param y an array of things implied by y.
	 * @throws ContradictionException if a trivial inconsistency is detected.
	 */
	public void addImplies(T x, T [] y) throws ContradictionException {
		IVecInt clause = new VecInt();
		for (T t : y) {
			clause.push(-getIntValue(x));
			clause.push(getIntValue(t));
			solver.addClause(clause);
			clause.clear();
		}
	}
	
	/**
	 * Easy way to feed the solver with implications.
	 * 
	 * @param x a thing such that x -> y[i]  for all i
	 * @param y a collection of things implied by y.
	 * @throws ContradictionException if a trivial inconsistency is detected.
	 */
	public void addImplies(T x, Collection<T> y) throws ContradictionException {
		IVecInt clause = new VecInt();
		for (T t : y) {
			clause.push(-getIntValue(x));
			clause.push(getIntValue(t));
			solver.addClause(clause);
			clause.clear();
		}
	}
	
	/**
	 * Easy way to enter in the solver that at least 
	 * degree x[i] must be satisfied.
	 * 
	 * @param x an array of things.
	 * @param degree the minimal number of elements in x that must be satisfied.
	 * @throws ContradictionException if a trivial inconsistency is detected.
	 */
	public void addAtLeast(T [] x, int degree) throws ContradictionException {
		IVecInt literals = new VecInt(x.length);
		for (T t : x) {
			literals.push(getIntValue(t));
		}
		solver.addAtLeast(literals, degree);
	}
	
	/**
	 * Easy way to enter in the solver that at least 
	 * degree x[i] must be satisfied.
	 * 
	 * @param x an array of things.
	 * @param degree the minimal number of elements in x that must be satisfied.
	 * @throws ContradictionException if a trivial inconsistency is detected.
	 */
	public void addAtLeast(Collection<T> x, int degree) throws ContradictionException {
		IVecInt literals = new VecInt(x.size());
		for (T t : x) {
			literals.push(getIntValue(t));
		}
		solver.addAtLeast(literals, degree);
	}
	
	/**
	 * Easy way to enter in the solver that at most 
	 * degree x[i] must be satisfied.
	 * 
	 * @param x an array of things.
	 * @param degree the maximal number of elements in x that must be satisfied.
	 * @throws ContradictionException if a trivial inconsistency is detected.
	 */
	public void addAtMost(T [] x, int degree) throws ContradictionException {
		IVecInt literals = new VecInt(x.length);
		for (T t : x) {
			literals.push(getIntValue(t));
		}
		solver.addAtMost(literals, degree);
	}
	
	/**
	 * Easy way to enter in the solver that at most 
	 * degree x[i] must be satisfied.
	 * 
	 * @param x an array of things.
	 * @param degree the maximal number of elements in x that must be satisfied.
	 * @throws ContradictionException if a trivial inconsistency is detected.
	 */
	public void addAtMost(Collection<T> x, int degree) throws ContradictionException {
		IVecInt literals = new VecInt(x.size());
		for (T t : x) {
			literals.push(getIntValue(t));
		}
		solver.addAtMost(literals, degree);
	}
}
