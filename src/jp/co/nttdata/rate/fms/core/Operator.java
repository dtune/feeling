package jp.co.nttdata.rate.fms.core;

/**
 * �v�Z�̃I�y���[�^�ł���
 * @author btchoukug
 *
 */
public class Operator
{
	/** �I�y���[�^���g*/
	public String operator;
	/** �I�y���[�^���f�p */
	private char[] ops;
	/** �D�揇*/
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
