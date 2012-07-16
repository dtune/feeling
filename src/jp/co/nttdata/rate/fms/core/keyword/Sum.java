package jp.co.nttdata.rate.fms.core.keyword;

import jp.co.nttdata.rate.fms.core.Sequence;

/**
 * 合計のキーワード <br>
 * 書き方はsum(begin,end){body}とする <br>
 * bodyの中にINDEXを含む前提である <br>
 * 注：INDEXはキーワードとして他のところに使わないでください
 * 
 * @author btchoukug
 * 
 */
public class Sum extends Loop {

	public Sum(Sequence seq, int pos) {
		super(SUM, seq, pos);
	}

}
