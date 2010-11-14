package forex.indicator.core;

public interface DataSource {
	public DataSource update();
	public DataSource update(PriceStream stream);
	public DataSource resetAll();
	public void addStream(PriceStream stream);
}
