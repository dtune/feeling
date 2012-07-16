package jp.co.nttdata.rate.exception;

import java.text.MessageFormat;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import jp.co.nttdata.rate.model.rateKey.RateKey;
import jp.co.nttdata.rate.model.rateKey.rule.RateKeyConstraint;
import jp.co.nttdata.rate.util.Const;
import jp.co.nttdata.rate.util.PropertiesUtil;

/**
 * レート計算について、ユーザから入力より起こった業務エラーである
 * @author btchoukug
 *
 */
public class RateException extends Exception {

	private static final long serialVersionUID = 7256682095749117668L;
	
	private String errorType;
	
	private String errorMessage;
	
	private String errorCodition;
	private String[] errorItems;
		
	public RateException() {
		;
	}

	/**
	 * @param message
	 */
	public RateException(String message) {
		super(message);
	}

	/**
	 * @param cause
	 */
	public RateException(Throwable cause) {
		super(cause);
	}

	/**
	 * @param message
	 * @param cause
	 */
	public RateException(String message, Throwable cause) {
		super(message, cause);
	}
		

	/**
	 * 入力チェックエラー（単項目と制御関係二種類を含む）
	 * @param errorType
	 * @param items
	 */
	public RateException(String errorType, Object src) {

		this.errorType = errorType;	
		
		if (errorType.equals(Const.ERR_CONSTRAINT)) {
			//制御関係エラーの場合
			RateKeyConstraint cons = (RateKeyConstraint)src;
			this.errorItems = cons.getConstraintKeys();
			this.errorCodition = cons.getCondition();
			String msg = cons.getDesc() == null || StringUtils.isEmpty(cons.getDesc()) ? this.errorCodition : cons.getDesc();
			this.errorMessage = MessageFormat.format(PropertiesUtil.getErrorTypeMessage(errorType), msg);
		} else {
			RateKey key = (RateKey)src;
			this.errorItems = new String[]{key.getName()};
			
			if (errorType.equals(Const.ERR_SCOPE)) {
				this.errorMessage = key.getLabel() + 
				//範囲
				MessageFormat.format(PropertiesUtil.getErrorTypeMessage(errorType),
						String.valueOf(key.getRule().getMin()), String.valueOf(key.getRule().getMax()));
				//特殊値
				int[] specialVals = key.getRule().getSpecialValues();
				if (specialVals != null && specialVals.length > 0) {
					this.errorMessage += "\nまた、右記の値が入力不可となります："+ArrayUtils.toString(specialVals, Const.EMPTY);
				}
				
			} else {
				this.errorMessage = key.getLabel() + PropertiesUtil.getErrorTypeMessage(errorType);
			}
		}
		
	}

	public String getErrorType() {
		return errorType;
	}
	
	public String[] getErrorItems() {
		return this.errorItems;
	}

	public String getErrorMessage() {
		return errorMessage;
	}
	
	

}
