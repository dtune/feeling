package jp.co.nttdata.rate.fms.core.keyword;

import java.math.BigDecimal;

import jp.co.nttdata.rate.fms.core.Parentheses;
import jp.co.nttdata.rate.fms.core.Range;
import jp.co.nttdata.rate.fms.core.Sequence;

public class Body implements Cloneable {

//	private static Logger logger = LogFactory.getInstance(Body.class); 
	
	/**カレントif,elseIf,else,while,sumキーワードのボディtokens*/
	public Sequence seq;
	
	/** 全体tokensの中の範囲 */
	public Range r;
		
	public Body(Sequence seq, int startPos) {
		int closeParenthese = Parentheses.posMatchCloseParenthese(seq, startPos + 1);
		r = new Range(startPos + 1, closeParenthese);
		this.seq = seq.subSequence(r);
		this.seq.initKeywordBlock();
	}
	
	public BigDecimal value() throws Exception {
		BigDecimal ret = seq.eval();
		return ret;
	}
	
	@Override
	public Object clone() {
		Body copy = null;
		try {
			copy = (Body) super.clone();
			copy.seq = (Sequence) this.seq.clone();
		} catch (CloneNotSupportedException e) {
			e.printStackTrace();
		}
		return copy;		
	}
	
	@Override
	public String toString() {
		return this.seq.toString();
	}
	
}
