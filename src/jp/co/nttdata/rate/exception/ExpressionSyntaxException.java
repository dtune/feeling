package jp.co.nttdata.rate.exception;

/**
 * �Z���̒��g����`�Ԉ������A���Y�ُ�ɂȂ�
 * @author zhanghy
 *
 */
public class ExpressionSyntaxException extends RuntimeException {
	
	private static final long serialVersionUID = 1L;

	public ExpressionSyntaxException(String message, Throwable cause) {
		super(message, cause);
	}

	public ExpressionSyntaxException(String message) {
		super(message);
	}
}
