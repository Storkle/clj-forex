package forex.indicator.core;
//A SeqVar allows one to reference an array in reverse order! Yay! Anyways.

//TODO: allow prepending of data!??

public class SeqVar extends Seq implements AbsoluteSequence {
	protected AbsoluteSequence Bars;
	protected SeqVar () {}; 
	public SeqVar (Indicator ind) {
		this.Bars = ind.Bars;
	}
	public SeqVar (AbsoluteSequence Bars) {
		this.Bars = Bars;
	}
	
	public Integer start () {
		Integer bars = Bars.size()-1;
		if (super.start()==null || bars ==null) 
			return null;
		return bars-super.start();
	}
	

	//get functions...
	public Double get() {
		return get(0);
	}
	public Double get(Integer index) {
		return super.get(Bars.size()-1-index);
	}

	//Set functions...

	public Double set(Integer index,Integer obj) {
		return set(index,obj*1.0);
	}

	//TODO: fix SeqArray?
	public Double set(Integer index, Double obj) {
        super.put(Bars.size()-1-index, obj);
		return obj;
	}
}
