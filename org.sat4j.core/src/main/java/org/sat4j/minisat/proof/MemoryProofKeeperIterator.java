package org.sat4j.minisat.proof;

import java.util.Iterator;

import org.sat4j.specs.IVecInt;

public class MemoryProofKeeperIterator implements Iterator<Integer> {
	private IVecInt content;
	private int index;
	
	
	public MemoryProofKeeperIterator(IVecInt content){
		this.content = content;
		this.index   = 0;
	}

	
	public boolean hasNext() {
		return this.index < this.content.size();
	}

	
	public Integer next() {
		return this.content.get(this.index++);
	}

	
	public void remove() {
	}
}
