package forex.indicator;
//A SeqVar allows one to reference an array in reverse order! Yay! Anyways.

//TODO: allow prepending of data!??

public class SeqVar extends Seq implements AbsoluteSequence {
	private AbsoluteSequence Bars;
	protected SeqVar () {};
	public SeqVar (Indicator ind) {
		this.Bars = ind.Bars;
	}
	public SeqVar (AbsoluteSequence Bars) {
		this.Bars = Bars;
	}
	
	public Integer start () {
		Integer bars = bars()-1;
		if (super.start()==null || bars ==null) 
			return null;
		return bars-super.start();
	}
	
	public int bars() {
		return Bars.size();
	}
	//get functions...
	public Double get() {
		return get(0);
	}
	public Double get(Integer index) {
		return super.get(bars()-1-index);
	}

	//Set functions...
	public Double set(Integer obj) {	
		return set(obj*1.0);
	}
	public Double set(Integer index,Integer obj) {
		return set(index,obj*1.0);
	}
	public Double set(Double obj) {
		return set(0,obj);
	}
	//TODO: fix SeqArray?
	public Double set(Integer index, Double obj) {
        super.put(bars()-1-index, obj);
		return obj;
	}
}
