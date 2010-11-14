package forex.indicator.tests;
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
//cci: 10, -154.91606714625223
//rsi: 14, size: 986 val: 46.84190717109989
//atr: 14 size: 986 start: 985 val: 7.678571428571598E-4
public class Test {
	public static void main(String[] args) {
		CsvSource source = new CsvSource("/home/seth/Desktop/eurusd.csv");
		PriceStream stream = new PriceStream(source);
		Indicator ind = new ATR(stream,14);  
		for (int i=0;i<=30;i++)
			source.update();
		while (source.empty()==false) {
			source.update(); ind.update();
		}  	
		//ind.update(); //ind.get(0);
		System.out.println("HI "+stream.size());
		System.out.println("size: "+ind.size()+" start: "+ind.start()+" val: "+ind.get()); 
	} 
}
