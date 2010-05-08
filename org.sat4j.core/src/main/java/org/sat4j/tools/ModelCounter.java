package org.sat4j.tools;

import org.sat4j.specs.ModelListener;

public class ModelCounter implements ModelListener {
	private int counter = 0;

	public void onModel(int[] model) {
		counter++;
	}

	public int counterValue() {
		return counter;
	}

}