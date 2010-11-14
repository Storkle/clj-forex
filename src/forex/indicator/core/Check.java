package forex.indicator.core;

public class Check {
	public static void is(boolean result,String s, Object ... params) {
		if (result==false) {
		  String errMessage = String.format(s,params);
		  if (errMessage=="") {
			  errMessage = "is failed: invalid indicator parameters";
		  }
		  throw new InvalidIndicatorParameter(errMessage);
		}
	}

}
