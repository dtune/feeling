package jp.co.nttdata.rate.exception;

public class IllegalBatchDataException extends RuntimeException {

	private static final long serialVersionUID = -5582094445622218608L;

	public IllegalBatchDataException(String msg) {
		super(msg);
	}
	
	public IllegalBatchDataException(String msg, Exception e) {
		super(msg, e);
	}

}
