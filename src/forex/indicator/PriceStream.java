package forex.indicator;
public class PriceStream {
	public SeqArray High, Open, Low, Close;
	public DataSource source;
	
	public PriceStream(DataSource s) {
		source = s;
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
	public PriceStream set(int index, Double highVal, Double lowVal,
			Double openVal, Double closeVal) {
		High.set(index, highVal);
		Open.set(index, openVal);
		Low.set(index, lowVal);
		Close.set(index, closeVal);
		return this;
	}

	public PriceStream update() {
		source.update(this);
		return this;
	}

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
