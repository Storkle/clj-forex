package forex.indicator.core;

public class Functions {
	
	public static double Highest (SeqVar seq, int from, int length) {
		if (length<1) 
			throw new RuntimeException("Invalid length "+length+" passed to Highest");
		Double max = null;
		for (int i=from;i<from+length;i++) {
			double val = seq.get(i);
			if (max==null || val>max)
				max = val;
		}
		return max;
	}
	
	public static double Lowest (SeqVar seq, int from, int length) {
		if (length<1) 
			throw new RuntimeException("Invalid length "+length+" passed to Lowest");
		Double min = null;
		for (int i=from;i<from+length;i++) {
			double val = seq.get(i);
			if (min==null || val<min)
				min = val;
		}
		return min;
	}
	
	public static double Summation(SeqVar seq,int from,int to) {
		double sum=0.0;
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
