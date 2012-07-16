package jp.co.nttdata.rate.fms.core;

import java.util.Vector;

/**
 * 公式の有効性チェックを行う
 * @author btchoukug
 *
 */
public class ExpressionValidate {

	public static final String BEGIN_OPERATOR = "begin with operator";
	public static final String BEGIN_PARENTHESIS = "begin with ( or )";
	public static final String BEGIN_COMMA = "begin with ,";
	
	@SuppressWarnings("unchecked")
	public boolean beginCheck(Vector tokens, StringBuffer err) {
		boolean flg = true;
		char mark;
		Token t;
		t = (Token)tokens.elementAt(0);
		mark = t.mark;
		
		//オペレータで
		if (mark == 'P') {
			err.append(BEGIN_OPERATOR);
		} else if (mark == ')') {
			err.append(BEGIN_PARENTHESIS);
		} else if (mark == 'Z') {
			err.append(BEGIN_COMMA);
		}
		
		return flg;
	}
	
	/*
	 * １、先頭チェック
	 * ２、末尾チェック
	 * ３、シーケンス合法性チェック
	 * ４、関数チェック
	 * ５、コマチェック（パラメータの分割）
	 * この語義チェックは後で実装してもよい
	 */
}
