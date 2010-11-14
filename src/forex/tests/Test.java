package forex.tests;
import forex.core.*;
import forex.indicator.*;

//TODO: getting and setting take up a very significant time - why??? profile! or maybe its read locking....

//TODO: fix bars() and somehow should we actually throw an error if there aren't enough bars to calculate the indicator?
//or should it just be a null indicator?
//TODO: cci doesnt work

public class Test {
	public static void main(String[] args) {
		CsvSource source = new CsvSource("/home/seth/Desktop/eurusd.csv");
		PriceStream stream = new PriceStream(source);
		Indicator ind = new SMA(stream,stream.Close,10);  
		while (source.empty()==false) {
			source.update(); 
		}  
		ind.update();
		System.out.println("HI "+stream.size());
		System.out.println("size: "+ind.size()+" val: "+ind.get());
	}
}
