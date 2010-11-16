package forex.indicator.core;

import java.util.ArrayList;

public class CsvSource implements DataSource {
	String fileName; 
	TextFile file;
	
	public boolean empty() {
		return file.iterator().hasNext()==false;
	}

	public CsvSource(String fileName) {
		this.fileName = fileName;
		file = new TextFile(fileName);
	}

	Integer line = 0;
	//private cause we update all price streams at the same time for this one
	public DataSource update(PriceStream stream) {
		// read all of CsvSource file and fill stream
		if (file.iterator().hasNext()==false) {
			return this;
		}
		String line = file.iterator().next();
		String[] dat = line.split(",");
		Double high = Double.parseDouble(dat[3]), low = Double
				.parseDouble(dat[4]);
		Double open = Double.parseDouble(dat[2]), close = Double
				.parseDouble(dat[5]);
		stream.add(high, low, open, close);
		return this;
	}

	@Override
	public DataSource update() {
		// TODO Auto-generated method stub
		return null;
	}
	


}
