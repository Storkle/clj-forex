package forex.indicator.core;

import java.util.ArrayList;

public class PriceStream implements AbsoluteSequence {
	public SeqArray High, Open, Low, Close;
	//TODO: change id
	public int id () {
		return "".hashCode();
	}	
	public PriceStream() {
		High = new SeqArray();
		Open = new SeqArray();
		Low = new SeqArray();
		Close = new SeqArray();
	}

	public PriceStream reset() {
		High.reset();Open.reset();Low.reset();Close.reset();
		return this;
	}
	
	public PriceStream add(double highVal, double lowVal,
			double openVal, double closeVal) { 
		High.add(highVal);
		Open.add(openVal);
		Low.add(lowVal);
		Close.add(closeVal);
		return this;
	}
	public int size() {
		return High.size();
	}
	public PriceStream put(int index, Double highVal, Double lowVal,
			Double openVal, Double closeVal) {
		High.put(index, highVal);
		Open.put(index, openVal);
		Low.put(index, lowVal);
		Close.put(index, closeVal);
		return this;
	}

	
	ArrayList<Indicator> indicators = new ArrayList<Indicator>();	
	public PriceStream addIndicator(Indicator ind) {
		indicators.add(ind);return this;
	}
	
/*	public PriceStream update() {
		source.update(this);
		return this;
	}*/

	public Double open(int index) {
		return Open.get(index);
	}

	public Double open() {
		return Open.get(0);
	}

	public Double low(int index) {
		return Low.get(index);
	}

	public Double low() {
		return Low.get(0);
	}

	public Double close(int index) {
		return Close.get(index);
	}

	public Double close() {
		return Close.get(0);
	}

	public Double high(int index) {
		return High.get(index);
	}

	public Double high() {
		return High.get(0);
	}

}
