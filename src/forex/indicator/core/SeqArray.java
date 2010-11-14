package forex.indicator.core;
//TODO: delete
//a SeqVar not linked to an indicator
public class SeqArray extends SeqVar implements AbsoluteSequence {

    //TODO NullPriceStream
    public SeqArray() {
    	this.Bars = this;
    }

    public int size () {
    	return super.size();
    }
}
