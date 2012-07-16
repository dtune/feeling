package jp.co.nttdata.rate.fms.core.keyword;

import org.apache.log4j.Logger;

import jp.co.nttdata.rate.fms.core.Parentheses;
import jp.co.nttdata.rate.fms.core.Range;
import jp.co.nttdata.rate.fms.core.Sequence;
import jp.co.nttdata.rate.log.LogFactory;

/**
 * if,while,sum�ȂǃL�[���[�h�̈ꕔ�����ł���
 * @author btchoukug
 *
 */
public class Condition implements Cloneable {

	private static Logger logger = LogFactory.getInstance(Condition.class);
	
	/**�J�����gif,elseIf,else,while�L�[���[�h�̏���tokens*/
	public Sequence seq;
	private String text;
	public Range r;
	private boolean value = false;
	
	public Condition(Sequence seq, int startPos) {
		int closeParenthese = Parentheses.posMatchCloseParenthese(seq, startPos + 1);
		r = new Range(startPos + 1, closeParenthese);
		this.seq = seq.subSequence(r);
		this.seq.initKeywordBlock();
		this.text = this.seq.toString();
	}
	
	public Condition(String text, boolean value) {
		this.text = text;
		this.value = value;
	}
	
	public boolean isTrue() throws Exception{
		
		boolean ret = false;
		
		//else�̏ꍇ�A�������Ȃ����߁A����true��\���������쐬����
		if (seq == null) {
			ret = this.value;
		} else {
			ret = seq.getBooleanValue();
		}		
		
		if (logger.isDebugEnabled()) {
			logger.debug("\tcondition[" + this.text + "]=" + ret);
		}
		
		return ret;
	}
	
	@Override
	public Object clone() {
		Condition copy = null;
		try {
			copy = (Condition) super.clone();
			if (this.seq != null) {
				copy.seq = (Sequence) this.seq.clone();	
			}			
		} catch (CloneNotSupportedException e) {
			e.printStackTrace();
		}
		return copy;		
	}
	
	@Override
	public String toString() {
		if (this.seq == null) return String.valueOf(this.value);
		return this.seq.toString();
	}

}
