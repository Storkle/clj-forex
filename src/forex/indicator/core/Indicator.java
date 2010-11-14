package forex.indicator.core;


import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

//get read lock when reading! but only for pooled indicators!

public abstract class Indicator extends SeqVar implements IndicatorInterface {
	public void Init() {};
	public void Destroy() {};
	public Indicator(PriceStream Bars) {
		super(Bars);
		this.Bars = Bars;
	}
		
	private static final int NOT = -22;
	public static final int EXCEPTION = -2222;
	protected PriceStream Bars;
	ReadWriteLock lock = new ReentrantReadWriteLock();

	public static class ReturnCodeException extends RuntimeException{
		public int ret;
		ReturnCodeException(int ret) {
			this.ret = ret;
		}
		public String getMessage () {
			return "return code "+ret+" received";
		}
		
	}
	public static class NotEnoughBarsException extends RuntimeException{
		public int bars; public int amount;
		NotEnoughBarsException(int bars,int amount) {
			this.bars = bars; this.amount = amount;
		}
		public String getMessage () {
			return ""+bars+" bars needed, only "+amount+" avaiable";
		}
		
	}
	protected void not (int bars_needed) {
		throw new Indicator.NotEnoughBarsException(bars_needed,bars());
	}
	
	//updating indicator
	public int update() {
		int ret = -1;
		if (stopped==true) {
			throw new RuntimeException("indicator is deinitialized - cant use it!");
		}
		try {
			lock();
			ret = Execute();
		} 
		finally {
			if (ret!=0) {
				_counted = 0;stopped=true;Destroy();
			}
			unlock();
		}
		if (ret!=0) 
			fail(ret);
		return ret;
	}
	
    //locking the indicator 
	public void lock () {
		lock.writeLock().lock();
	}
	public void unlock () {
		lock.writeLock().unlock();
	}
	public void readLock () {
		lock.readLock().lock();
	}
	public void readUnlock() {
		lock.readLock().unlock();
	}	
	public boolean tryRead() {
		return lock.readLock().tryLock();
	}
	public boolean tryWrite() {
		return lock.writeLock().tryLock();
	}
	
	//setting the indicator - overriden because we need to record changes in changed???
	private Hashtable<Integer, Boolean> changed = new Hashtable<Integer, Boolean>();
	public Double set(Integer index, Double obj) {
		Double result = super.set(index, obj);
		changed.put(index, true);
		return result;
	}
	private int _counted = 0;
	
	protected int counted() {
		// return counted-1
		// TODO: why? metatrader does it?
		Integer s = size();
		if (s==null || s==0)
			return 0;
		return s-1;
	}
	
	//TODO: does above affect this?
	public boolean isStart () {
		return counted()==0; 
	}
	
	public int bars () {
		return Bars.size();
	}
	public int limit () {
		return bars()-1-counted();
	}
	
	//deinitialize indicator
	public String parseError(int error) {
		switch (error) {
		case EXCEPTION:
			return "java exception";
		default:
			return "";
		}
	}	
	protected boolean stopped = false;
	public void fail (int error) {
		throw new Indicator.ReturnCodeException(error);
	}
	public void warn(String msg) {
		System.out.println("Warning: "+msg);
	}

	//more convenient interface with the price stream
	public Double open(int index) {
		return Bars.Open.get(index);
	}

	public Double open() {
		return Bars.Open.get(0);
	}

	public Double low(int index) {
		return Bars.Low.get(index);
	}

	public Double low() {
		return Bars.Low.get(0);
	}

	public Double close(int index) {
		return Bars.Close.get(index);
	}

	public Double close() {
		return Bars.Close.get(0);
	}

	public Double high(int index) {
		return Bars.High.get(index);
	}

	public Double high() {
		return Bars.High.get(0);
	}

}
