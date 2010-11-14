package forex.indicator;

import forex.indicator.core.*;

public class RSI extends PriceIndicator {
	public RSI(PriceStream Bars, SeqVar Price, int period) {
		super(Bars, Price);
		this.period = period;
		Init();
	}

	SeqVar averageGain = new SeqVar(this);
	SeqVar averageLoss = new SeqVar(this);
	@input
	int period = 14;

	public double loss(int i) {
		if (price(i) < price(i + 1)) {
			return price(i + 1) - price(i);
		}
		return 0;
	}

	public double gain(int i) {
		if (price(i) > price(i + 1))
			return price(i) - price(i + 1);
		return 0;
	}

	public void Init() {
		Check.is(period>0,"in RSI, period must be >0");
	}

	// TODO: bars needs to be based on price sequence; make it general!
	public int Execute() {
		if (bars()<period) not(period);
		int limit = limit();
		limit--; // initialization starts at limit-1
		// initialize RSI
		if (isStart()) {
			double gain = 0, loss = 0;
			for (int i = limit; i > limit - period; i--) {
				gain += gain(i);
				loss += loss(i);
			}
			averageGain.set(limit - period + 1, gain / period);
			averageLoss.set(limit - period + 1, loss / period);
		}
		// calculate rest of averageGain and averageLoss
		for (int i = limit - period; i >= 0; i--) {
			double gain = (averageGain.get(i + 1) * (period - 1) + gain(i))
					/ period;
			double loss = (averageLoss.get(i + 1) * (period - 1) + loss(i))
					/ period;
			averageGain.set(i, gain);
			averageLoss.set(i, loss);
		}
		// calculate RSI
		for (int i = limit - period + 1; i >= 0; i--) {
			double loss = averageLoss.get(i);
			if (loss == 0) {
				set(i, 100.0);
			} else {
				set(i, 100 - 100 / (1 + (averageGain.get(i) / loss)));
			}
		}
		return 0;
	}

}
