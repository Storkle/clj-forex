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
public class Test {
	public static void main(String[] args) {
		CsvSource source = new CsvSource("/home/seth/Desktop/eurusd.csv");
		PriceStream stream = new PriceStream(source);
		while (source.empty()==false) {
			source.update(); 
		}  
		Indicator ind = new CCI(stream,10);  
		ind.update();
		System.out.println("HI "+stream.size());
		System.out.println("size: "+ind.size()+" val: "+ind.get());
	} //1.3699220000000023
}
