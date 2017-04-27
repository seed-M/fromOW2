/*******************************************************************************
 * SAT4J: a SATisfiability library for Java Copyright (C) 2004, 2012 Artois University and CNRS
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
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
 * Contributors:
 *   CRIL - initial API and implementation
 *******************************************************************************/
package org.sat4j.pb.constraints.pb;

import java.math.BigInteger;

import org.sat4j.core.VecInt;
import org.sat4j.specs.IVecInt;
import org.sat4j.specs.IteratorInt;

public class PostProcessToClause implements IPostProcess {

    /**
     * 
     */
    private final ConflictMap conflictMap;

    /**
     * @param conflictMap
     */
    PostProcessToClause(ConflictMap conflictMap) {
        this.conflictMap = conflictMap;
    }

    public void postProcess(int dl) {
        if (this.conflictMap.isAssertive(dl)
                && (!this.conflictMap.degree.equals(BigInteger.ONE))) {
            int litLevel, ilit;
            if (this.conflictMap.assertiveLiteral != -1) {
                this.conflictMap.assertiveLiteral = this
                        .chooseAssertiveLiteral(dl);
                int lit = this.conflictMap.weightedLits
                        .getLit(this.conflictMap.assertiveLiteral);

                IVecInt toSuppress = new VecInt();

                for (int i = 0; i < this.conflictMap.size(); i++) {
                    ilit = this.conflictMap.weightedLits.getLit(i);
                    litLevel = this.conflictMap.voc.getLevel(ilit);
                    if ((litLevel < this.assertiveLevel)
                            && this.conflictMap.voc.isFalsified(ilit))
                        this.conflictMap.weightedLits.changeCoef(i,
                                BigInteger.ONE);
                    else if (ilit != lit) {
                        toSuppress.push(ilit);
                    }
                }

                this.conflictMap.weightedLits.changeCoef(
                        this.conflictMap.assertiveLiteral, BigInteger.ONE);

                for (int i = 0; i < toSuppress.size(); i++)
                    this.conflictMap.removeCoef(toSuppress.get(i));

                this.conflictMap.degree = BigInteger.ONE;
                this.conflictMap.assertiveLiteral = this.conflictMap.weightedLits
                        .getFromAllLits(lit);
                assert this.conflictMap.backtrackLevel == this.conflictMap
                        .oldGetBacktrackLevel(dl);
            }
        }
    }

    private int assertiveLevel;

    public int chooseAssertiveLiteral(int maxLevel) {
        // we are looking for a level higher than maxLevel
        // where the constraint is still assertive
        // update ConflictMap.this.assertiveLiteral
        VecInt lits;
        int level;

        int indStop = ConflictMap.levelToIndex(maxLevel); // ou maxLevel - 1 ???
        int indStart = ConflictMap.levelToIndex(0);
        BigInteger slack = this.conflictMap.computeSlack(0)
                .subtract(this.conflictMap.degree);
        int previous = 0;
        for (int indLevel = indStart; indLevel <= indStop; indLevel++) {
            if (this.conflictMap.byLevel[indLevel] != null) {
                level = ConflictMap.indexToLevel(indLevel);
                assert this.conflictMap.computeSlack(level)
                        .subtract(this.conflictMap.degree).equals(slack);
                if (this.conflictMap.isImplyingLiteralOrdered(level, slack)) {
                    this.conflictMap.backtrackLevel = previous;
                    assertiveLevel = level;
                    break;
                }
                // updating the new slack
                lits = this.conflictMap.byLevel[indLevel];
                int lit;
                for (IteratorInt iterator = lits.iterator(); iterator
                        .hasNext();) {
                    lit = iterator.next();
                    if (this.conflictMap.voc.isFalsified(lit)
                            && this.conflictMap.voc.getLevel(lit) == ConflictMap
                                    .indexToLevel(indLevel)) {
                        slack = slack.subtract(
                                this.conflictMap.weightedLits.get(lit));
                    }
                }
                if (!lits.isEmpty()) {
                    previous = level;
                }
            }
        }

        assert this.conflictMap.backtrackLevel == this.conflictMap
                .oldGetBacktrackLevel(maxLevel);
        return this.conflictMap.assertiveLiteral;
    }

}