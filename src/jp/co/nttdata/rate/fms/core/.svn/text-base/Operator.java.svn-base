package jp.co.nttdata.rate.fms.core;

/**
 * 計算のオペレータである
 * @author btchoukug
 *
 */
public class Operator
{
	/** オペレータ自身*/
	public String operator;
	/** オペレータ判断用 */
	private char[] ops;
	/** 優先順*/
	public int priority;
	
	public Operator(String op, int priority) {
	   this.operator = op;
	   this.priority = priority;	   
	   this.ops = op.toCharArray();	   
	}
	
	public String toString() {
		return operator;
	}

	public char[] getOps() {
		return ops;
	}
	
}
