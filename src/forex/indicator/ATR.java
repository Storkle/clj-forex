package forex.indicator;

import forex.indicator.core.*;

//TODO: get rid of difference between PriceIndicator and Indicator
//TODO: fix the fact that we cant update ourselves, get into deadlocky?
public class ATR extends Indicator {
	public ATR(PriceStream Bars, int period) {
		super(Bars); this.period = period;
		Init();
	}

	SeqVar tr = new SeqVar(this);
	Indicator tr_average;
	public void Init() {
		Check.isNatural(period);
		tr_average = new SMA(Bars, tr, period);
	}
	//TODO: when should we use Integer vs Double
	//TODO: make this easier please:) its not exactly finished also!  
	@Override
	public Integer start () {
		return tr_average.start();
	}
	@Override
	public int size () {
		return tr_average.size();
	}
    @Override
	public Double get(Integer i) {
		return tr_average.get(i);
	}
	//
	@input
	int period = 14;
	//TODO
	public int Execute() {
		if (bars()<period) not(period);
		int limit = limit();
		limit--; // initialization starts at limit-1;
		// initialize true range
		for (int i = limit; i >= 0; i--) {
			double high = high(i);
			double val1 = Math.abs(high - low(i));
			double val2 = Math.abs(high - close(i + 1));
			double val3 = Math.abs(low(i) - close(i + 1));
			tr.set(i, Math.max(Math.max(val1, val2), val3));
		}
		tr_average.update();
		return 0;
	}
}
