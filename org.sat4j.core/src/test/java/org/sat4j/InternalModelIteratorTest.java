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

package org.sat4j;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.junit.Test;
import org.sat4j.core.VecInt;
import org.sat4j.minisat.SolverFactory;
import org.sat4j.minisat.core.DataStructureFactory;
import org.sat4j.minisat.core.Solver;
import org.sat4j.specs.ContradictionException;
import org.sat4j.specs.ISolver;
import org.sat4j.specs.IVecInt;
import org.sat4j.specs.TimeoutException;
import org.sat4j.tools.Minimal4CardinalityModel;
import org.sat4j.tools.Minimal4InclusionModel;
import org.sat4j.tools.ModelCounter;

/**
 * @author leberre
 * 
 *         To change the template for this generated type comment go to
 *         Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
public class InternalModelIteratorTest {

	@Test
	public void testModelIterator() {
		try {
			ModelCounter mc = new ModelCounter();
			Solver<DataStructureFactory> solver = SolverFactory.newBestWL();
			solver.setModelAnalyzer(solver.ITERATE_OVER_ALL_MODELS);
			solver.setModelListener(mc);
			solver.newVar(3);
			IVecInt clause = new VecInt();
			clause.push(1);
			clause.push(2);
			clause.push(3);
			solver.addClause(clause);
			clause.clear();
			clause.push(-1);
			clause.push(-2);
			clause.push(-3);
			solver.addClause(clause);
			assertTrue(solver.isSatisfiable());
			assertEquals(6, mc.counterValue());
		} catch (ContradictionException e) {
			fail();
		} catch (TimeoutException e) {
			fail();
		}
	}

	@Test
	public void testModelIteratorLimit() {
		try {
			ModelCounter mc = new ModelCounter();
			Solver<DataStructureFactory> solver = SolverFactory.newBestWL();
			solver.setModelAnalyzer(solver.buildModelIterator(3));
			solver.setModelListener(mc);
			solver.newVar(3);
			IVecInt clause = new VecInt();
			clause.push(1);
			clause.push(2);
			clause.push(3);
			solver.addClause(clause);
			clause.clear();
			clause.push(-1);
			clause.push(-2);
			clause.push(-3);
			solver.addClause(clause);
			solver.isSatisfiable();
			assertEquals(3, mc.counterValue());
		} catch (ContradictionException e) {
			fail();
		} catch (TimeoutException e) {
			fail();
		}
	}

	// Is not Implemented Yet. We need a Backup/Restore solution to do so.
	// public void testIncMinModel() {
	// try {
	// ISolver solver = new ModelIterator(new
	// Minimal4InclusionModel(SolverFactory.newMiniLearning()));
	// solver.newVar(3);
	// VecInt clause = new VecInt();
	// clause.push(1);
	// clause.push(2);
	// clause.push(3);
	// solver.addClause(clause);
	// clause.clear();
	// clause.push(-1);
	// clause.push(-2);
	// clause.push(-3);
	// solver.addClause(clause);
	// int counter = 0;
	// while (solver.isSatisfiable()) {
	// int[] model = solver.model();
	// counter++;
	// }
	// assertEquals(3, counter);
	// } catch (ContradictionException e) {
	// fail();
	// } catch (TimeoutException e) {
	// fail();
	// }
	// }
	//
	// public void testCardMinModel() {
	// try {
	// ISolver solver = new ModelIterator(new
	// Minimal4CardinalityModel(SolverFactory.newMiniLearning()));
	// solver.newVar(3);
	// VecInt clause = new VecInt();
	// clause.push(1);
	// clause.push(2);
	// clause.push(3);
	// solver.addClause(clause);
	// clause.clear();
	// clause.push(-1);
	// clause.push(-2);
	// clause.push(-3);
	// solver.addClause(clause);
	// int counter = 0;
	// while (solver.isSatisfiable()) {
	// int[] model = solver.model();
	// counter++;
	// }
	// assertEquals(3, counter);
	// } catch (ContradictionException e) {
	// fail();
	// } catch (TimeoutException e) {
	// fail();
	// }
	// }

	@Test
	public void testCardModel() {
		try {
			ISolver solver = new Minimal4CardinalityModel(
					SolverFactory.newDefault());
			solver.newVar(3);
			IVecInt clause = new VecInt();
			clause.push(1);
			clause.push(-2);
			clause.push(3);
			solver.addClause(clause);
			clause.clear();
			clause.push(-1);
			clause.push(2);
			clause.push(-3);
			solver.addClause(clause);
			int counter = 0;
			while (solver.isSatisfiable() && (counter < 10)) {
				solver.model();
				counter++;
			}
			assertEquals(10, counter);
		} catch (ContradictionException e) {
			fail();
		} catch (TimeoutException e) {
			fail();
		}
	}

	@Test
	public void testIncModel() {
		try {
			ISolver solver = new Minimal4InclusionModel(
					SolverFactory.newDefault());
			solver.newVar(3);
			IVecInt clause = new VecInt();
			clause.push(1);
			clause.push(-2);
			clause.push(3);
			solver.addClause(clause);
			clause.clear();
			clause.push(-1);
			clause.push(2);
			clause.push(-3);
			solver.addClause(clause);
			int counter = 0;
			while (solver.isSatisfiable() && (counter < 10)) {
				solver.model();
				counter++;
			}
			assertEquals(10, counter);
		} catch (ContradictionException e) {
			fail();
		} catch (TimeoutException e) {
			fail();
		}
	}

	@Test
	public void testIsSatisfiableVecInt() {
		try {
			ISolver solver = SolverFactory.newDefault();
			solver.newVar(3);
			IVecInt clause = new VecInt();
			clause.push(1);
			clause.push(2);
			clause.push(3);
			solver.addClause(clause);
			clause.clear();
			clause.push(-1);
			clause.push(-2);
			clause.push(-3);
			solver.addClause(clause);
			assertTrue(solver.isSatisfiable());
			IVecInt cube = new VecInt();
			cube.push(1);
			assertTrue(solver.isSatisfiable(cube));
			// printModel(solver.model());
			cube.push(2);
			assertEquals(2, cube.size());
			assertTrue(solver.isSatisfiable(cube));
			// printModel(solver.model());
			cube.push(-3);
			assertEquals(3, cube.size());
			assertTrue(solver.isSatisfiable(cube));
			// printModel(solver.model());
			cube.pop();
			cube.push(3);
			assertEquals(3, cube.size());
			// System.out.println(" cube " + cube);
			boolean sat = solver.isSatisfiable(cube);
			// if (sat) {
			// printModel(solver.model());
			// }
			assertFalse(sat);
		} catch (ContradictionException e) {
			fail();
		} catch (TimeoutException e) {
			fail();
		}
	}

	@Test(timeout = 5000)
	public void testGlobalTimeoutCounter() {
		ModelCounter mc = new ModelCounter();
		Solver<DataStructureFactory> solver = SolverFactory.newBestWL();
		solver.setModelAnalyzer(solver.ITERATE_OVER_ALL_MODELS);
		solver.setModelListener(mc);
		IVecInt clause = new VecInt();
		for (int i = 1; i < 100; i++) {
			clause.push(i);
		}
		try {
			solver.addClause(clause);
			solver.setTimeout(3);
			solver.isSatisfiable();
		} catch (TimeoutException e) {
			assertTrue(mc.counterValue() > 0);
			System.out.println(mc.counterValue());
		} catch (ContradictionException e) {
			fail();
		}
	}

	@Test(timeout = 5000)
	public void testGlobalTimeoutIterator() {
		ModelCounter mc = new ModelCounter();
		Solver<DataStructureFactory> solver = SolverFactory.newBestWL();
		solver.setModelAnalyzer(solver.ITERATE_OVER_ALL_MODELS);
		solver.setModelListener(mc);
		IVecInt clause = new VecInt();
		for (int i = 1; i < 100; i++) {
			clause.push(i);
		}
		try {
			solver.addClause(clause);
			solver.setTimeout(3);
			solver.isSatisfiable();
		} catch (TimeoutException e) {
			System.out.println(mc.counterValue());
		} catch (ContradictionException e) {
			fail();
		}
	}

	@Test(timeout = 11000)
	public void testSpecificValues() throws ContradictionException,
			TimeoutException {
		assertEquals(3L, count(2));
		assertEquals(7L, count(3));
		assertEquals(15L, count(4));
		assertEquals(31L, count(5));
		assertEquals(63L, count(6));
		assertEquals(127L, count(7));
		assertEquals(255L, count(8));
		assertEquals(511L, count(9));
		assertEquals(1023L, count(10));
		assertEquals(2047L, count(11));
		assertEquals(4095L, count(12));
	}

	private long count(int size) throws ContradictionException,
			TimeoutException {
		ModelCounter mc = new ModelCounter();
		Solver<DataStructureFactory> solver = SolverFactory.newBestWL();
		solver.setModelAnalyzer(solver.ITERATE_OVER_ALL_MODELS);
		solver.setModelListener(mc);
		IVecInt clause = new VecInt();
		for (int i = 1; i <= size; i++) {
			clause.push(i);
		}
		solver.addClause(clause);
		solver.setTimeout(10);
		solver.isSatisfiable();
		return mc.counterValue();
	}
}