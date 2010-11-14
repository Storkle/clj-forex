package forex.indicator;


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
		
	public static final int NOT = -22;
	public static final int EXCEPTION = -2222;
	protected PriceStream Bars;
	ReadWriteLock lock = new ReentrantReadWriteLock();

	//updating indicator
	public int update() {
		int ret = -1; Exception except = null;
		try {
			lock();
			ret = Execute();
		} catch (Exception e) {
			except = e;
		} finally {
			unlock();
		}
		if (ret == 0)
			_counted = changed.size();
		else {
			_counted = 0;
			if (except!=null) {
				fail(EXCEPTION,"deinitializing indicator "+this.toString()+":: "+except.toString());
			} else {
			  fail(ret,"deinitialized indicator " + this.toString()
					+ " due to error code " + ret + " ...");		  
			}
		}
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
	public boolean stopped = false;
	public void fail (int error) {
		fail(error,"");
	}
	public void warn(String msg) {
		System.out.println("Warning: "+msg);
	}
	public void fail(int error,String s) {
		if (error!=NOT) {
           stopped = true; 
		   throw new RuntimeException("fail: error "+parseError(error)+": "+s);
		} else {
			warn("not enough bars for indicator "+this.toString());
		}
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
