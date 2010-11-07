package forex.indicator;

public class VMA extends Indicator {
    public VMA(PriceStream stream, SeqVar price) {super(stream,price);};
    
	SeqVar MA, ADX, ADXR, sPDI, sMDI, STR, VarMA;
	public int ADX_Length = 2;
	public double Weighting = 2;
	public int MA_Length = 1;
	//TODO: SeriesVar that required indicator as constructor!!!
	public void Init() {
		MA = new SeqVar(this);
		ADX = new SeqVar(this);
		ADXR = new SeqVar(this);
		sPDI = new SeqVar(this);
		sMDI = new SeqVar(this);
		STR = new SeqVar(this);
		VarMA = new SeqVar(this);
	}

	public int Execute() {
		return -1;
		/*if (Bars.index() >=0 && Bars.index()<ADX_Length-1) {
			VarMA.set(Bars.Close());
			MA.set(Bars.Close());
			STR.set(Bars.High() - Bars.Close());
			sPDI.set(0); //sPDI.get(1)
			sMDI.set(0);
			ADX.set(0);
			ADXR.set(0);
			return null;
		} else if (Bars.index()>=ADX_Length-1){
			double hi = Bars.High(), hi1 = Bars.High(1); 
			double lo = Bars.Low(), lo1 = Bars.Low(1);
			double close1 = Bars.Close(1);

			double Bulls = .5 * (Math.abs(hi - hi1) + (hi - hi1));
			double Bears = .5 * (Math.abs(lo1 - lo) + (lo1 - lo1));
			if (Bulls > Bears)
				Bears = 0;
			else if (Bulls < Bears)
				Bulls = 0;
			else if (Bulls == Bears) {
				Bulls = 0;
				Bears = 0;
			}

			sPDI.set((Weighting * sPDI.get(1) + Bulls) / (Weighting + 1));
			sMDI.set((Weighting * sMDI.get(1) + Bears) / (Weighting + 1));
			double TR = Math.max(hi - lo, hi - close1);
			STR.set((Weighting * STR.get(1) + TR) / (Weighting + 1));
			double PDI = 0, MDI = 0, DX = 0;
			if (STR.get() > 0) {
				PDI = sPDI.get() / STR.get();
				MDI = sMDI.get() / STR.get();
			}
			if ((PDI + MDI) > 0)
				DX = Math.abs(PDI - MDI) / (PDI + MDI);
			ADX.set((Weighting * ADX.get(1) + DX) / (Weighting + 1));
			double vADX = ADX.get();
			double ADXmin = Functions.Lowest(ADX, ADX_Length);
			double ADXmax = Functions.Highest(ADX, ADX_Length);
			double Diff = ADXmax - ADXmin;
			double Const = 0;
			if (Diff > 0)
				Const = (vADX - ADXmin) / Diff;
			VarMA.set(((2 - Const) * VarMA.get(1) + Const * Bars.Close()) / 2);
			if (Bars.index() >= MA_Length)
				return Functions.Average(VarMA, MA_Length);
		}
		return null;
		*/
	}
}
