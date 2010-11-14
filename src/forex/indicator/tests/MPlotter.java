package forex.indicator.tests;
import forex.indicator.core.*;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;

//TODO: we need to modify plotter so that metatrader can plot the right bars even when it is getting
//data dynamically. This below is best used on daily chart where little is happening. but still....
//things might be offset by 1 day or so.

public class MPlotter {
	static String file = "out.txt";
	static String dir = "";
	public static void out(Indicator ind) {
		String dir = "/home/seth/.wine/dosdevices/c:/Program Files/FXCM MT4 powered by BT/experts/files/";
		PrintStream f;
		try {
			f = new PrintStream(new FileOutputStream(dir+file));
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			throw new RuntimeException(e.getMessage());
		}
		f.println(ind.size());
		for (int i=0;i<ind.size();i++) {
	       f.println(ind.get(i));
		}
	}

}
