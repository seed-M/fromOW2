package org.sat4j.helper;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;
import org.sat4j.minisat.SolverFactory;
import org.sat4j.specs.ContradictionException;
import org.sat4j.specs.TimeoutException;

public class TestDependencyHelper {

	private MappingHelper<String> helper;
	
	@Before
	public void setUp() {
		helper = new MappingHelper<String>(SolverFactory.newDefault());
	}
	
	@Test
	public void testBasicRequirements() throws ContradictionException, TimeoutException {
		helper.addImplies("A",new String[]{"B","C","D"});
		helper.addConflict("B","C");
		helper.setTrue("A");
		assertFalse(helper.hasASolution());		
	}
	
	@Test
	public void testBasicRequirementsDetailedExplanation() throws ContradictionException, TimeoutException {
		helper.addImplies("A","B");
		helper.addImplies("A","C");
		helper.addImplies("A","D");
		helper.addConflict("B","C");
		helper.setTrue("A");
		assertFalse(helper.hasASolution());		
	}
	
	@Test
	public void testDisjunctions() throws ContradictionException, TimeoutException {
		helper.addImplies("A", new String[]{"B","C","D"});
		helper.addImplication("C", new String[] {"C1","C2","C3"});
		helper.addAtMost(new String[] {"C1","C2","C3"},1);
		helper.setTrue("A");
		assertTrue(helper.hasASolution());
		assertTrue(helper.getBooleanValueFor("A"));
		assertTrue(helper.getBooleanValueFor("B"));
		assertTrue(helper.getBooleanValueFor("C"));
		assertTrue(helper.getBooleanValueFor("D"));
		if (helper.getBooleanValueFor("C1")) {
			assertFalse(helper.getBooleanValueFor("C2"));
			assertFalse(helper.getBooleanValueFor("C3"));
		}
		if (helper.getBooleanValueFor("C2")) {
			assertFalse(helper.getBooleanValueFor("C1"));
			assertFalse(helper.getBooleanValueFor("C3"));
		}
		if (helper.getBooleanValueFor("C3")) {
			assertFalse(helper.getBooleanValueFor("C1"));
			assertFalse(helper.getBooleanValueFor("C2"));
		}
	}
	
	@Test
	public void testDisjunctionExplanation() throws ContradictionException, TimeoutException {
		helper.addImplies("A", new String[]{"B","C","D"});
		helper.addConflict("B","C1");
		helper.addConflict("D","C2");
		helper.addImplication("C", new String[] {"C1","C2"});
		helper.addAtMost(new String[] {"C1","C2","C3"},1);
		helper.setTrue("A");
		assertFalse(helper.hasASolution());
	}
}
