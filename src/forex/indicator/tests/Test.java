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
//TODO: make the tests!
//TODO: final verification - indicators which read csv file we shall produce and displays it so we can compare!

//cci: 10, -154.91606714625223
//rsi: 14, size: 986 val: 46.84190717109989
//atr: 14 size: 986 start: 985 val: 7.678571428571598E-4
//fantail: 2,2,100 size: 901 start: 900 val: 1.3693416179468705


public class Test {
	public static void main(String[] args) {
		CsvSource source = new CsvSource("/home/seth/Desktop/eurusd.csv");
		PriceStream stream = new PriceStream(source);
		FantailVMA ind = new FantailVMA(stream,2,2,100);  
		for (int i=0;i<=30;i++)
			source.update();
		while (source.empty()==false) {
			source.update(); 
		}  		
		System.out.println("HI");
		ind.update(); 
		System.out.println("size: "+ind.average.size()+" start: "+ind.average.start()+" val: "+ind.average.get(9)); 
	} 
}
