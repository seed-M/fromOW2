/*
 * Created on 15 juin 2004
 *
 * To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package org.sat4j.minisat;

import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * @author leberre
 * 
 * To change the template for this generated type comment go to Window -
 * Preferences - Java - Code Generation - Code and Comments
 */
public class AllTests {

    public static Test suite() {
        TestSuite suite = new TestSuite("Test for org.sat4j.minisat");
        // $JUnit-BEGIN$
        suite.addTestSuite(TestAssertion.class);
        suite.addTestSuite(TestsFonctionnels.class);
        suite.addTest(GenericM2Test.suite());
        // $JUnit-END$
        return suite;
    }
}