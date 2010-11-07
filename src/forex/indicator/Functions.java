package forex.indicator;

public class Functions {
	public static Double Summation(SeqVar seq,int from,int length) {
		Double sum=0.0;
		if (length<0) {
			if (from+length<0) 
				throw new IndexOutOfBoundsException("In Funcions.Summation, attempting to access negative index");
			for (int i=from;i>from+length;i--) {
				sum+=seq.get(i);
			}
			return sum;
		} else {
			for (int i=from;i<from+length;i++) {
				sum+=seq.get(i);
			}
			return sum;
		}
	}

}
