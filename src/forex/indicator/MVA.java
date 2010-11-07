package forex.indicator;
 
public class MVA extends Indicator {
    public MVA(PriceStream stream, SeqVar price) {super(stream,price);};
    
	SeqVar summation = new SeqVar(this);
    public int MA_Length = 2;
    
	public int Execute() {
		if (bars()<MA_Length) return -1;
	
		int counted = counted();
		if (counted<0) return -1;
		if (counted>0) --counted;
		int limit = bars()-counted;
		//calculate!
        double sum = Functions.Summation(Price,limit,MA_Length*-1);	
		for (int i=limit-MA_Length;i>=0;i--) {
			sum = sum-price(i+MA_Length)+price(i);
			set(i,sum/MA_Length);
		}
		return 0;
	}
}
