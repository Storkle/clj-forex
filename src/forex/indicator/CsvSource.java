package forex.indicator;
public class CsvSource implements DataSource {
	String fileName; 
	public CsvSource(String fileName) {
		this.fileName = fileName;
	}

	public DataSource update(PriceStream stream) {
		// read all of CsvSource file and fill stream
		TextFile file = new TextFile(fileName);
		stream.reset();
		for (String line : file) {
			String[] dat = line.split(",");
			Double high = Double.parseDouble(dat[3]), low = Double
					.parseDouble(dat[4]);
			Double open = Double.parseDouble(dat[2]), close = Double
					.parseDouble(dat[5]);
			stream.add(high, low, open, close);
		}
		return this;
	}
}
