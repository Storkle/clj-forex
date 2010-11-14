package forex.indicator.core;

public class Check {
	public static void isNatural(int i) {
		if (i<=0) 
			throw new InvalidIndicatorParameter("parameter "+i+" is less than 1");
	}

}
