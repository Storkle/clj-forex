package forex.core;
import forex.indicator.*;
 
public class SMA extends PriceIndicator {
    public SMA(PriceStream Bars, SeqVar Price,int period) {
    	super(Bars, Price); this.period = period; Init();
    }
    
	@input 
    Integer period = 2;
	public void Init () {
		Check.isNatural(period);
	}
	public int Execute() {	
		if (Price.size()<period) return NOT;	
	    int limit = limit();	
	    double sum = 0; 
	    //initialize
	    if (isStart()) {     
	       sum = Functions.Summation(Price, limit, limit-period); //initialize
	       set(limit-period+1,sum/period);   
	    } else {
	    	sum = get(limit-period+1)*period;
	    }
	    limit = limit-period;
	    //this is the recalculation on every bar!
		for (int i=limit;i>=0;i--) {
			sum = sum-price(i+period)+price(i);
			set(i,sum/period);
		}
		return 0;
	}
}
