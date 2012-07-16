package jp.co.nttdata.rate.fms.core;

import java.math.BigDecimal;

/**
 * 計算の最小単位(オペランドとオペレータ含む)
 * @author btchoukug
 *
 */
public class Token {
	
	public static final char OP = 'P';
	public static final char KEYWORD = 'K';
	public static final char ARRAY = 'A';
	public static final char DECIMAL = 'D';
	public static final char VARIABLE = 'V';	
	public static final char COMMA = 'C';
	public static final char BRACE_BEGIN = '(';
	public static final char BRACE_END = ')';
	
	/**オペレータの場合、Operatorのオブジェクトを格納する*/
	public Object token;
	
	/**当該Tokenのタイプ標識*/
	public char mark;
	
	/**記号の位置と長さでExpressionから取得できる*/
	public int position;
	public int length;
	
	public Token (Object token, char mark) {
		this.token = token;
		this.mark = mark;
		this.position = 0;
		this.length = 0;
	}
	
	public Token (Object token, char mark, int position, int length) {
		this.token = token;
		this.mark = mark;
		this.position = position;
		this.length = length;
	}
	
	public Variable toVariable() {
		return (Variable)this.token;
	}
	
	public Operator toOperator() {
		return (Operator)this.token;
	}
	
	public Array toArray() {
		return (Array)this.token;
	}
	
	public Double toDecimal() {
		return Double.valueOf((String) this.token);
	}
	
	@Override
	public String toString() {
		if (isOperator()) return ((Operator)this.token).toString();	
		if (isArray()) return ((Array)this.token).toString();
		if (isVariable()) return ((Variable)this.token).toString();
		if (isDecimal()) return ((BigDecimal)this.token).toPlainString(); 
		
		return this.token.toString();
	}
	
	public boolean isOperator() {
		return _checkMark(OP);
	}
	
	public boolean isKeyword() {
		return _checkMark(KEYWORD);
	}
	
	public boolean isArray() {
		return _checkMark(ARRAY);
	}
	
	public boolean isDecimal() {
		return _checkMark(DECIMAL);
	}
	
	public boolean isVariable() {
		return _checkMark(VARIABLE);
	}
	
	private boolean _checkMark(char mark) {
		if (this.mark == mark) return true;
		return false;
	}	
	
}
