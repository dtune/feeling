package jp.co.nttdata.rate.exception;

/**
 * FMS�Ɋւ���`���s���ŋN�������ُ�Ǝw��
 * @author zhanghy
 *
 */
public class FmsDefErrorException extends Exception {

	private static final long serialVersionUID = -3186120415464011368L;

	public FmsDefErrorException(String msg) {
		super(msg);
	}

	public FmsDefErrorException(String msg, Exception e) {
		super(msg, e);
	}

}
