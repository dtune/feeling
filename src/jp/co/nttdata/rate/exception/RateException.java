package jp.co.nttdata.rate.exception;

import java.text.MessageFormat;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import jp.co.nttdata.rate.model.rateKey.RateKey;
import jp.co.nttdata.rate.model.rateKey.rule.RateKeyConstraint;
import jp.co.nttdata.rate.util.Const;
import jp.co.nttdata.rate.util.PropertiesUtil;

/**
 * ���[�g�v�Z�ɂ��āA���[�U������͂��N�������Ɩ��G���[�ł���
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
	 * ���̓`�F�b�N�G���[�i�P���ڂƐ���֌W���ނ��܂ށj
	 * @param errorType
	 * @param items
	 */
	public RateException(String errorType, Object src) {

		this.errorType = errorType;	
		
		if (errorType.equals(Const.ERR_CONSTRAINT)) {
			//����֌W�G���[�̏ꍇ
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
				//�͈�
				MessageFormat.format(PropertiesUtil.getErrorTypeMessage(errorType),
						String.valueOf(key.getRule().getMin()), String.valueOf(key.getRule().getMax()));
				//����l
				int[] specialVals = key.getRule().getSpecialValues();
				if (specialVals != null && specialVals.length > 0) {
					this.errorMessage += "\n�܂��A�E�L�̒l�����͕s�ƂȂ�܂��F"+ArrayUtils.toString(specialVals, Const.EMPTY);
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
