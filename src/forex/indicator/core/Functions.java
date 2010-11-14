package forex.indicator.core;

public class Functions {
	public static Double Summation(SeqVar seq,int from,int to) {
		Double sum=0.0;
		if (from>to) {
			for (int i=from;i>to;i--) {
				sum+=seq.get(i);
			}
			return sum;
		} else {
			for (int i=from;i<to;i++) {
				sum+=seq.get(i);
			}
			return sum;
		}
	}

}
