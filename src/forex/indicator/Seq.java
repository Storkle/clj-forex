package forex.indicator;

import java.util.HashMap;

// a sequence is an array with infinite size! and if you put something in it,it
//assumes everything started at 0, so there! otherwise with nothing, length is zero!
public class Seq extends HashMap<Integer,Double>{
	private Integer max=null, min = null;
	//TODO: make public?
	private Double defaultValue = 0.0;
	
	protected Seq reset () {
		clear(); max = null; min=null; return this;
	}
	
	public Integer start() {
		return min;
	}
	public int size() {
		if (max==null)
			return 0;
		return max-min+1;
	}

	public Double add(Double obj) {	
		super.put(size(), obj);
		if (max==null) 
			max=0;
		else
			max+=1;	
		if (min==null)
			min=0;
		return obj;
	}
	
	public Double put(Integer index, Double obj) {
		if (index < 0)
			throw new IndexOutOfBoundsException("invalid index "+index+" less than 0");
		super.put(index, obj);
		if (max == null || index > max) {
			max = index;
		}
		if (min==null || index<min) 
			min = index;
		return obj;
	}

	//once youve started a sequence at an index, WE CANT GO SMALLER! Yay!
	//TODO: change?
	public Double get(Integer index) {
		if (index==null||index>max || index<min)
			throw new IndexOutOfBoundsException("Attempting to access sequence with min/max of "+min+"/"+max+" at index "+index);
	    Double result = super.get(index);
	    if (result==null) {
	    	System.out.println("Warning: using default value of 0.0 for sequence");
	    	return defaultValue;
	    }
	    return result;
	}
}
