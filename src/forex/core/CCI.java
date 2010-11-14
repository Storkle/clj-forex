package forex.core;
import forex.indicator.*;

/*
Calculate the last period's Typical Price (TP) = (H+L+C)/3 where H = high, L = low, and C = close.
Calculate the 20-period Simple Moving Average of the Typical Price (SMATP).
Calculate the Mean Deviation. First, calculate the absolute value of the difference between the last period's SMATP and the typical price for each of the past 20 periods. Add all of these absolute values together and divide by 20 to find the Mean Deviation.
The final step is to apply the Typical Price (TP), the Simple Moving Average of the Typical Price (SMATP), the Mean Deviation and a Constant (.015) to the following formula:
CCI = ( Typical Price - SMATP ) / ( .015 X Mean Deviation )
*/

public class CCI extends Indicator {
    public CCI(PriceStream Bars,int period) {
    	super(Bars);
    	this.period = period;
    	Init();
    }
    Indicator sma;
    Indicator meanDev;
    SeqVar tp = new SeqVar(this);
    SeqVar dev = new SeqVar(this);
    @input 
    Integer period = 2;
    public void Init () {
    	sma = new SMA(Bars,tp,period);
    	meanDev= new SMA(Bars,dev,period);
    }
    //TODO: filter out beginning automagically!
    public int Execute() { 
    	//calculate TP
    	int limit = limit(); 
    	for (int i=0;i<=limit;i++)
    		tp.set(i,(close(i)+high(i)+low(i))/3.0);
    	//calculate simple moving average of typical price (SMATP)
    	sma.update();
    	//calculate absolute value of different between SMATP and typical price
    	for (int i=sma.start();i>=0;i--) 
    		dev.set(i,Math.abs(sma.get(i)-tp.get(i)));
    	//calculate moving average of dev
    	//TODO: somehow get rid of bars()
    	meanDev.update();
    	//calculate cci
    	for (int i=meanDev.start();i>=0;i--)
    		set(i,(tp.get(i)-sma.get(i))/(.015*meanDev.get(i)));
    	return 0;    	
    }
}
