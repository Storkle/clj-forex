//translated from original indicator by IgorAD,igorad2003@yahoo.co.uk
//@ http://forex-strategies-revealed.com/files/MT4/FantailVMA3.mq4

package forex.indicator;
import forex.indicator.core.*;

//TODO: fix: doesn't quite work  as can be seen with the metatrader indicator plotter
public class FantailVMA extends Indicator {
	public FantailVMA(PriceStream stream, int adx_period, int weight, int period) {
		super(stream);
		this.adx_period = adx_period;
		this.weight = weight;
		this.period = period;
		Init();
	}

	SeqVar MA, ADX, ADXR, sPDI, sMDI, STR, VarMA;
	public Indicator average;
	@input
	int adx_period = 2;
	@input
	double weight = 2;
	@input
	int period = 1;

	// TODO: again, we need to just utilize the average buffer - fix:)
	// TODO: is(a&&b,"ERRORMESSAGE",var1,var2 ...);
	// TODO: make error message Attempting to access sequence with min/max of
	// 0/1 at index 2 better
	// TODO: make all error messages better!
	// TODO: reason Warning: using default value of 0.0 for sequence

	public void Init() {
		MA = new SeqVar(this);
		ADX = new SeqVar(this);
		ADXR = new SeqVar(this);
		sPDI = new SeqVar(this);
		sMDI = new SeqVar(this);
		STR = new SeqVar(this);
		VarMA = new SeqVar(this);
		average = new EMA(Bars, VarMA, period);
		Check
				.is(
						adx_period >= 2 && adx_period <= 8 && weight >= 1
								&& weight <= 8,
						"invalid FantailVMA parameters: should be 2<=adx<=8, 1<=weight<=8, mva period anything");
	}

	// TODO: option to choose between SMA,EMA,etc.
	// TODO: option between having default values? thats what metatrader does.
	// even for price streams! but it is convenient to do it my way for
	// debugging...
	public int Execute() {
		int limit = limit();
		if (isStart()) {
			for (int i = limit; i > limit - adx_period; i--) {
				VarMA.set(i, close(i));
				MA.set(i, close(i));
				STR.set(i, high(i) - low(i));
				sPDI.set(i, 0);
				sMDI.set(i, 0);
				ADX.set(i, 0);
				ADXR.set(i, 0);
			}
		}
		for (int i = limit - adx_period+1; i >= 0; i--) {
			double hi = high(i), hi1 = high(i + 1);
			double lo = low(i), lo1 = low(i + 1);
			double close1 = close(i + 1);
			double Bulls = .5 * (Math.abs(hi - hi1) + (hi - hi1));
			double Bears = .5 * (Math.abs(lo1 - lo) + (lo1 - lo));
			if (Bulls > Bears)
				Bears = 0;
			else if (Bulls < Bears)
				Bulls = 0;
			else if (Bulls == Bears) {
				Bulls = 0;
				Bears = 0;
			}
			sPDI.set(i, (weight * sPDI.get(i + 1) + Bulls) / (weight + 1));
			sMDI.set(i, (weight * sMDI.get(i + 1) + Bears) / (weight + 1));
			double TR = Math.max(hi - lo, hi - close1);
			STR.set(i, (weight * STR.get(i + 1) + TR) / (weight + 1));
			double PDI = 0, MDI = 0, DX = 0;
			if (STR.get(i) > 0) {
				PDI = sPDI.get(i) / STR.get(i);
				MDI = sMDI.get(i) / STR.get(i);
			}
			if ((PDI + MDI) > 0)
				DX = Math.abs(PDI - MDI) / (PDI + MDI);
			ADX.set(i, (weight * ADX.get(i + 1) + DX) / (weight + 1));
			double vADX = ADX.get(i);
			double ADXmin = Functions.Lowest(ADX, i, adx_period);
			double ADXmax = Functions.Highest(ADX, i, adx_period);
			double Diff = ADXmax - ADXmin;
			double Const = 0;
			if (Diff > 0)
				Const = (vADX - ADXmin) / Diff;
			VarMA.set(i,
					((2 - Const) * VarMA.get(i + 1) + Const * close(i)) / 2);
		}
		average.update();
		return 0;
	}
}
