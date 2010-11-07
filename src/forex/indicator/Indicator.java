package forex.indicator;

import java.util.ArrayList;
import java.util.Hashtable;

public  abstract class Indicator extends SeqVar implements IndicatorInterface {
	SeqVar Price;
	PriceStream Bars;

	
	public Indicator(PriceStream Bars, SeqVar Price) {
		super(Bars);
		this.Bars = Bars;
		this.Price = Price;
		Init();    
	} 
	
    public void Init() {};
	public void Destroy() {};
	
	private Hashtable<Integer,Boolean> changed = new Hashtable<Integer,Boolean>();
	public Double set(Integer obj) {	
		return set(obj*1.0);
	}
	public Double set(Integer index,Integer obj) {
		return set(index,obj*1.0);
	}
	public Double set(Double obj) {
		return set(0,obj);
	}
	//TODO: make thread safe!
	public Double set(Integer index, Double obj) {
		Double result = super.set(index,obj);
		changed.put(index,true);
        return result;
	}
	
	private int _counted = 0;
	public int counted () {
		//return counted-1
		//TODO: why? metatrader does it?
		if (_counted==0) 
			return 0;
		return _counted-1;
	}
	
	//TODO: actually  deinit indicator
	protected int process() {
		//basically we record how many bars are 'counted'. So this is why we 'rewrite' the set methods of SeqVar below
		changed.clear();
		int ret = Execute();
		if (ret==0) 
			_counted = changed.size();
		else  {
			_counted = 0;
			throw new RuntimeException("deinitialized indicator due to error code "+ret+" ...");
		}
		return ret;
	}
	
	ArrayList<SeqVar> sequences = new ArrayList<SeqVar>(8);
	//TODO: should really only be public to SeqVar
	public void addSeq (SeqVar seq) {
		sequences.add(seq);
	}	

	//TODO: put in a interface?
	public Double price() {
		return Price.get(0);
	}
	public Double price(int index) {
		return Price.get(index);
	}
	public Double open(int index) {
		return Bars.Open.get(index);
	}

	public Double open() {
		return Bars.Open.get(0);
	}

	public Double low(int index) {
		return Bars.Low.get(index);
	}

	public Double low() {
		return Bars.Low.get(0);
	}

	public Double close(int index) {
		return Bars.Close.get(index);
	}

	public Double close() {
		return Bars.Close.get(0);
	}

	public Double high(int index) {
		return Bars.High.get(index);
	}

	public Double high() {
		return Bars.High.get(0);
	}

}
