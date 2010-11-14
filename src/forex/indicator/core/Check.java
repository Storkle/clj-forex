package forex.indicator.core;

public class Check {
	static String defaultAssertionError = "assertion failed: invalid indicator parameter(s)";
	public static void is(boolean result,String s, Object ... params) {
		if (result==false) {
			String errMessage = defaultAssertionError;
			try {
		      errMessage = String.format(s,params);
			} catch (RuntimeException e) {
				errMessage = defaultAssertionError;
				System.out.println("Warning: when calling Check.is(...) and formatting error string, error was thrown: "+e.getMessage());
			}
		  if (errMessage=="") {
			  errMessage = defaultAssertionError;
		  }
		  throw new InvalidIndicatorParameter(errMessage);
		}
	}
}
