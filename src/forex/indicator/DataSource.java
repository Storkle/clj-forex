package forex.indicator;
public interface DataSource {
	public DataSource update(PriceStream stream);
}
