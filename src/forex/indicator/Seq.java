package forex.indicator;

import java.util.HashMap;

// a sequence is an array with infinite size! and if you put something in it,it
//assumes everything started at 0, so there! otherwise with nothing, length is zero!
public class Seq extends HashMap<Integer,Double>{
	private Integer max=null;
	//TODO: make public?
	private Double defaultValue = 0.0;
	
	protected Seq reset () {
		clear(); max = null; return this;
	}
	
	public int size() {
		if (max==null)
			return 0;
		return max+1;
	}

	public Double add(Double obj) {	
		super.put(size(), obj);
		if (max==null) 
			max=0;
		else
			max+=1;	
		return obj;
	}
	
	public Double put(Integer index, Double obj) {
		if (index < 0)
			throw new IndexOutOfBoundsException("invalid index "+index+" less than 0");
		super.put(index, obj);
		if (max == null || index > max) {
			max = index;
		}
		return obj;
	}

	public Double get(Integer index) {
		if (index==null||index>size() || index<0)
			throw new IndexOutOfBoundsException("Attempting to access sequence of size "+size()+" at index "+index);
	    Double result = super.get(index);
	    if (result==null) {
	    	System.out.println("Warning: using default value of 0.0 for sequence");
	    	return defaultValue;
	    }
	    return result;
	}
}
