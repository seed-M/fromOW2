package org.sat4j.minisat.proof;

import java.io.Serializable;
import java.util.Iterator;

import org.sat4j.core.VecInt;
import org.sat4j.specs.IVecInt;

public class MemoryProofKeeper implements IProofKeeper, Serializable {
	private static final long serialVersionUID = 1L;
	private IVecInt content;
	
	
	public MemoryProofKeeper(){
		this.content = new VecInt();
	}
	
	
	public Iterator<Integer> iterator(){
		return new MemoryProofKeeperIterator(this.content);
	}
	
	
	public void putInt(int i) {
		this.content.push(i);
	}
	
	
	public String toString(){
		StringBuffer sb = new StringBuffer();
		int index = 0;
		boolean displayIndex = true;
		
		for (int i = 0 ; i < this.content.size() ; i++){
			if (displayIndex){
				sb.append (index++ + " : ");
				displayIndex = false;
			}
			
			if (this.content.get(i) == 0){
				sb.append ("\n");
				displayIndex = true;
			}
			else{
				sb.append(this.content.get(i) + " ");
			}
		}
		
		return sb.toString();
	}
}
