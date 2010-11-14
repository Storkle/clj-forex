package forex.indicator.tests;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;

import forex.indicator.*;
import forex.indicator.core.*;

//TODO: getting and setting take up a very significant time - why??? profile! or maybe its read locking....
//TODO: and maybe update without locks?
//TODO: add support for going from 0 upto limit?
//TODO: this takes forever
/*
  for (int i=0;i<30;i++)
			source.update();
		while (source.empty()==false) {
			source.update(); ind.update(); 
		}  
 */
//TODO: make cci more general with any stream, not just typical price
//TODO: make the tests!
//cci: 10, -154.91606714625223
//rsi: 14, size: 986 val: 46.84190717109989
//atr: 14 size: 986 start: 985 val: 7.678571428571598E-4

//TODO: it seems that metatrader is cheating and using more than the max bars for moving averages and such! or saving indicators! anyways we need to somehow remedy this so 
//we can succesfully compare our codes!


//TODO: make sure that when a new bar appears, the time() hasnt also incremented unless the actual indicators have incremented?
//ok, first we need to integrate the eas and dynamic data 
//TODO: add max bars for stream and thus for indicators! Much needed! but after we integrate dynamic data feed.

public class Test {
	public static void main(String[] args) throws FileNotFoundException {
		CsvSource source = new CsvSource("/home/seth/Desktop/eurusd.csv");
		PriceStream stream = new PriceStream(source);
		///Indicator ind = new EMA(stream,stream.Close,10);
		FantailVMA ind = new FantailVMA(stream,2,2,1);  
		for (int i=0;i<=30;i++)
			source.update();
		while (source.empty()==false) {
			source.update(); 
		}  		
		
		System.out.println("HI");
		ind.update(); 
		System.out.println("size: "+ind.average.size());
		//output for plotting in metatrader
		MPlotter.dir = "/home/seth/.wine/dosdevices/c:/Program Files/FXCM MT4 powered by BT/experts/files/";
		MPlotter.out(ind.average);
	} 
}
