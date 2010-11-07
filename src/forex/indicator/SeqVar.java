package forex.indicator;
//A SeqVar allows one to reference an array in reverse order! Yay! Anyways.

//TODO: allow prepending of data!??

public class SeqVar extends Seq {
	PriceStream Bars;
	public SeqVar() {
		
	}
	public SeqVar(Indicator ind) {
		Bars = ind.Bars;
	}
	public SeqVar (PriceStream bars) {
		//used to synchronize size of sequence with indicator data amount
		this.Bars = bars;
	}
	
	public int bars() {
		return Bars.size()-1;
	}
	//get functions...
	public Double get() {
		return get(0);
	}
	public Double get(Integer index) {
		return super.get(bars()-index);
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
	public Double set(Integer index, Double obj) {
        super.put(bars()-index, obj);
		return obj;
	}
}
