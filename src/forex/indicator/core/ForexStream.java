package forex.indicator.core;

public class ForexStream  extends PriceStream {
	public String symbol;
	public int timeframe;
	private String id;
	public ForexStream(String symbol,int timeframe) {
		super();
		this.symbol = symbol;this.timeframe = timeframe;
		this.id =  symbol+" "+timeframe;
	}
	public int id () {
		return (id.hashCode());
	}	
	
}
