//TODO: first settle down design and integrate into clojure, then focus on performance boosts! needs them! badly!
//TODO: MVA produces difference of .0001 value from metatrader - hmmm???
package forex.indicator;
//TODO: catch errors like indexoutofbound and then be awesome and deinitialize!
public class _test {
	public static void main(String[] args) {
		CsvSource source = new CsvSource("/home/seth/Desktop/EURUSD5.csv");
		PriceStream stream = new PriceStream(source);
		stream.update();  
		System.out.println(stream.close());
		MVA mva = new MVA(stream,stream.Close); mva.MA_Length=20;
		mva.Execute();
		//TODO: instead of null when get w/o processing, actually process? no?
		System.out.println("MVA: "+mva.get(0)+" "+mva.get(1)+" "+mva.get(2));
		//System.out.println(stream.Open(0)+" "+stream.Close(0)+" "+stream.High(0)+" "+stream.Low(0));
	}
}
