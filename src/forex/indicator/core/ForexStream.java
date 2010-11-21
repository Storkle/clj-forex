package forex.indicator.core;

public class ForexStream  extends PriceStream {
	public String symbol;
	public int timeframe;
	private String id; private int date;
	public ForexStream(String symbol,int timeframe) {
		super();
		this.symbol = symbol;this.timeframe = timeframe;
		this.id =  symbol+" "+timeframe;
	}
	public int setHead (int date) {
		this.date = date; return date;
	}
	public int getHead () {
		return this.date;
	}
	public int id () {
		return (id.hashCode());
	}	
	
}

