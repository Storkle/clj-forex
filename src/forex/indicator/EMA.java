package forex.indicator;

import forex.indicator.core.*;

public class EMA extends PriceIndicator {
	public EMA(PriceStream Bars, SeqVar Price, int period) {
		super(Bars, Price);
		this.period = period;
		Init();
	}

	@input
	int period = 2;
	double weight;

	public void Init() {
		Check.isNatural(period);
		weight = 2.0 / (period + 1);
	}

	// Today's Exponential Moving Average=(current day's closing price x
	// Exponent) + (previous day's EMA x (1-Exponent))
	public int Execute() {
		if (bars() < period)
			not(period); 
		int limit = limit();
		// initialize with SMA
		if (isStart()) {
			double sum = Functions.Summation(Price, limit, limit - period);
			set(limit - period + 1, sum / period);
		}
		limit = limit - period;
		for (int i = limit; i >= 0; i--) {
			set(i, price(i) * weight + get(i + 1) * (1 - weight));
		}
		return 0;
	};
}
