package forex.indicator;

public abstract class PriceIndicator extends Indicator {
	@input
	protected SeqVar Price;	
	public Double price(int i) {
		return Price.get(i);
	}
	
	public int limit () {
		return Price.size()-1-counted();
	}
	
	public Double price() {
		return Price.get(0);
	}
	public PriceIndicator(PriceStream Bars,SeqVar Price) {
		super(Bars);
		this.Price = Price;
	}

}
