package org.sat4j.minisat.orders;

import java.io.PrintWriter;
import java.util.Random;

import org.sat4j.core.LiteralsUtils;
import org.sat4j.minisat.core.ILits;
import org.sat4j.minisat.core.IOrder;
import org.sat4j.minisat.core.IPhaseSelectionStrategy;
import org.sat4j.specs.IVecInt;

public class RandomOrder implements IOrder {

	private IPhaseSelectionStrategy strategy;
	private ILits lits;

	private final Random rand = new Random();

	public void assignLiteral(int p) {
		// TODO Auto-generated method stub

	}

	public IPhaseSelectionStrategy getPhaseSelectionStrategy() {
		return strategy;
	}

	public void init() {
		// TODO Auto-generated method stub

	}

	public void printStat(PrintWriter out, String prefix) {
		// TODO Auto-generated method stub

	}

	public int select() {
		int i = LiteralsUtils.posLit(rand.nextInt(lits.nVars()) + 1);
		while (!lits.isUnassigned(i)) {
			i = LiteralsUtils.posLit(rand.nextInt(lits.nVars()) + 1);
		}
		return rand.nextBoolean() ? i : LiteralsUtils.neg(i);
	}

	public void setLits(ILits lits) {
		this.lits = lits;
	}

	public void setPhaseSelectionStrategy(IPhaseSelectionStrategy strategy) {
		this.strategy = strategy;
	}

	public void setVarDecay(double d) {
		// TODO Auto-generated method stub

	}

	public void undo(int x) {
		// TODO Auto-generated method stub

	}

	public void updateVar(int p) {
		// TODO Auto-generated method stub

	}

	public double varActivity(int p) {
		// TODO Auto-generated method stub
		return 0;
	}

	public void varDecayActivity() {
		// TODO Auto-generated method stub

	}

	public void setFixedOrder(IVecInt order) {
		// TODO Auto-generated method stub

	}

}
