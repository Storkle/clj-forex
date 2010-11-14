package forex.indicator.core;

public abstract class PriceIndicator extends Indicator {
	@input
	protected SeqVar Price;	
	public Double price(int i) {
		return Price.get(i);
	}
	
	//offsets are relative to size of Price!
	public int bars () {
		return Price.size();
	}
	public int limit () {
		return bars()-1-counted();
	}
	
	public Double price() {
		return Price.get(0);
	}
	public PriceIndicator(PriceStream Bars,SeqVar Price) {
		super(Bars);
		this.Price = Price;
	}

}
