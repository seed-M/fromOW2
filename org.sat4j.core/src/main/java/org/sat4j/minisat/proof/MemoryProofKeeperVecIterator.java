package org.sat4j.minisat.proof;

import java.util.Iterator;

import org.sat4j.core.Vec;
import org.sat4j.core.VecInt;
import org.sat4j.specs.IVec;
import org.sat4j.specs.IVecInt;

public class MemoryProofKeeperVecIterator implements Iterator<IVecInt> {
	private final IVec<IVecInt> content;
	private int index;
	
	
	public MemoryProofKeeperVecIterator(IVecInt content){
		this.index   = 0;
		this.content = new Vec<IVecInt>();
		IVecInt current = new VecInt();
		
		for (int i = 0 ; i < content.size() ; i++){
			if (content.get(i) != 0){
				current.push(content.get(i));
			}
			else{
				this.content.push(current);
				current.clear();
			}
		}
	}

	public boolean hasNext() {
		return index == content.size();
	}

	public IVecInt next() {
		return content.get(index++);
	}

	public void remove() {
	}

}
