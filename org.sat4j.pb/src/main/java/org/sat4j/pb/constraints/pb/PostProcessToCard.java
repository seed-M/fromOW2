package org.sat4j.pb.constraints.pb;

import java.math.BigInteger;

import org.sat4j.core.VecInt;
import org.sat4j.specs.IVecInt;
import org.sat4j.specs.IteratorInt;

/*******************************************************************************
 * SAT4J: a SATisfiability library for Java Copyright (C) 2004, 2012 Artois
 * University and CNRS
 *
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Alternatively, the contents of this file may be used under the terms of
 * either the GNU Lesser General Public License Version 2.1 or later (the
 * "LGPL"), in which case the provisions of the LGPL are applicable instead of
 * those above. If you wish to allow use of your version of this file only under
 * the terms of the LGPL, and not to allow others to use your version of this
 * file under the terms of the EPL, indicate your decision by deleting the
 * provisions above and replace them with the notice and other provisions
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
 * Contributors: CRIL - initial API and implementation
 *******************************************************************************/
public class PostProcessToCard implements IPostProcess {
    /**
     * 
     */
    private final ConflictMap conflictMap;

    /**
     * @param conflictMap
     */
    PostProcessToCard(ConflictMap conflictMap) {
        this.conflictMap = conflictMap;
    }

    public void postProcess(int dl) {
        // procedure Reduce-to-cardinality 4.3.9 proposed by H. Dixon
        // (Dixon's dissertation, page 67)
        if (this.conflictMap.isAssertive(dl)
                && (!this.conflictMap.degree.equals(BigInteger.ONE))) {
            int lit, litLevel, ilit;
            BigInteger coefLit;
            if (this.conflictMap.assertiveLiteral != -1) {
                this.conflictMap.assertiveLiteral = this
                        .chooseAssertiveLiteral(dl);
                coefLit = this.conflictMap.weightedLits
                        .getCoef(this.conflictMap.assertiveLiteral);

                // compute sum of coefficients of confl
                BigInteger sumCoefsTmp = BigInteger.ZERO;
                for (int i = 0; i < this.conflictMap.size(); i++) {
                    sumCoefsTmp = sumCoefsTmp
                            .add(this.conflictMap.weightedLits.getCoef(i));
                }

                // if it is already a cardinality constraint, return
                if (sumCoefsTmp.compareTo(
                        BigInteger.valueOf(this.conflictMap.size())) == 0) {
                    return;
                }
                IVecInt compLSet = new VecInt();
                BigInteger coefMax = coefLit;
                BigInteger coefTmp;

                // construct lSet with all falsified literals s.t. sum of
                // the coefs of compl(lSet) < degree
                // first we add the assertive literal
                sumCoefsTmp = sumCoefsTmp.subtract(coefLit);
                this.conflictMap.changeCoef(this.conflictMap.assertiveLiteral,
                        BigInteger.ONE);
                // then the needed falsified literals
                for (int i = 0; i < this.conflictMap.size(); i++) {
                    ilit = this.conflictMap.weightedLits.getLit(i);
                    lit = this.conflictMap.weightedLits
                            .getLit(this.conflictMap.assertiveLiteral);
                    litLevel = this.conflictMap.voc.getLevel(ilit);
                    coefTmp = this.conflictMap.weightedLits.getCoef(i);
                    if (ilit != lit) {
                        if (litLevel < this.assertiveLevel
                                && this.conflictMap.voc.isFalsified(ilit)) {
                            this.conflictMap.changeCoef(i, BigInteger.ONE);
                            sumCoefsTmp = sumCoefsTmp.subtract(coefTmp);
                            if (coefMax.compareTo(coefTmp) < 0)
                                coefMax = coefTmp;
                        } else
                            compLSet.push(ilit);
                    }
                }
                assert sumCoefsTmp.compareTo(this.conflictMap.degree) < 0;

                // add into lSet the sSet literals which are not already in
                // L and with coef > coefMax

                lit = this.conflictMap.weightedLits
                        .getLit(this.conflictMap.assertiveLiteral);
                int degreeCard = 1;
                for (int i = 0; i < compLSet.size(); i++) {
                    ilit = this.conflictMap.weightedLits
                            .getFromAllLits(compLSet.get(i));
                    if (coefMax.compareTo(
                            this.conflictMap.weightedLits.getCoef(ilit)) <= 0) {
                        this.conflictMap.changeCoef(ilit, BigInteger.ONE);
                        degreeCard++;
                    } else {
                        this.conflictMap.removeCoef(compLSet.get(i));
                    }
                }

                this.conflictMap.degree = BigInteger.valueOf(degreeCard);
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
        VecInt lits;
        int level;
        int indStop = ConflictMap.levelToIndex(maxLevel); // ou maxLevel - 1 ???
        int indStart = ConflictMap.levelToIndex(0);
        BigInteger slack = this.conflictMap.computeSlack(0)
                .subtract(this.conflictMap.degree);
        int previous = 0;
        IVecInt literals = new VecInt();
        for (int indLevel = indStart; indLevel <= indStop; indLevel++) {
            if (this.conflictMap.byLevel[indLevel] != null) {
                level = ConflictMap.indexToLevel(indLevel);
                assert this.conflictMap.computeSlack(level)
                        .subtract(this.conflictMap.degree).equals(slack);
                if (this.conflictMap.isImplyingLiteralOrdered(level, slack,
                        literals)) {
                    this.assertiveLevel = level;
                    this.conflictMap.backtrackLevel = previous;
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

        assert literals.size() > 0;
        BigInteger coef;
        int maxLit = literals.get(0);
        BigInteger maxCoef = this.conflictMap.weightedLits.getCoef(maxLit);
        for (int i = 1; i < literals.size(); i++) {
            coef = this.conflictMap.weightedLits.getCoef(literals.get(i));
            if (coef.compareTo(maxCoef) > 0) {
                maxLit = literals.get(i);
                maxCoef = coef;
            }
        }

        assert this.conflictMap.backtrackLevel == this.conflictMap
                .oldGetBacktrackLevel(maxLevel);
        assert literals.size() > 0;
        return maxLit;
    }

}