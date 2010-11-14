package forex.indicator;

import forex.indicator.core.*;

public class SMA extends PriceIndicator {
	public SMA(PriceStream Bars, SeqVar Price, int period) {
		super(Bars, Price);
		this.period = period;
		Init();
	}

	@input
	int period = 2;

	public void Init() {
		Check.isNatural(period);
	}

	public int Execute() {
		if (bars() < period)
			not(period,bars());
		int limit = limit();
		double sum = 0;
		// initialize
		if (isStart()) {
			sum = Functions.Summation(Price, limit, limit - period); // initialize
			set(limit - period + 1, sum / period);
		} else {
			sum = get(limit - period + 1) * period;
		}
		limit = limit - period;
		//this is the recalculation on every bar!
		for (int i = limit; i >= 0; i--) {
			sum = sum - price(i + period) + price(i);
			set(i, sum / period);
		}
		return 0;
	}
}
