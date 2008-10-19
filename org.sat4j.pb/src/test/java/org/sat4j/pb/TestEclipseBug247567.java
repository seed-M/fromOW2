package org.sat4j.pb;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.FileNotFoundException;
import java.io.IOException;

import org.junit.Test;
import org.sat4j.pb.reader.OPBEclipseReader2007;
import org.sat4j.reader.ParseFormatException;
import org.sat4j.reader.Reader;
import org.sat4j.specs.ContradictionException;
import org.sat4j.specs.TimeoutException;

/**
 * Test case to prevent a bug occurring with some Eclipse test cases:
 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=247567
 * 
 * @author daniel
 *
 */
public class TestEclipseBug247567 {

	@Test
	public void testReserveVarsButUseLess() throws ContradictionException, TimeoutException, FileNotFoundException, ParseFormatException, IOException {
		IPBSolver solver = SolverFactory.newEclipseP2();
		Reader reader = new OPBEclipseReader2007(solver);
		reader.parseInstance(getClass().getResource("bug247567.opb").getFile());
		assertTrue(solver.isSatisfiable());
		assertTrue(solver.model(1));
		assertTrue(solver.model(2));
		assertTrue(solver.model(3));
		assertFalse(solver.model(4));
		assertFalse(solver.model(5));
		assertFalse(solver.model(6));
	}
}
