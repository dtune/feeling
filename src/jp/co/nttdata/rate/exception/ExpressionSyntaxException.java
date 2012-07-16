package jp.co.nttdata.rate.exception;

/**
 * 算式の中身が定義間違ったら、当該異常になる
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
