package jp.co.nttdata.rate.model.rateKey;

import java.util.List;

import org.apache.commons.beanutils.ConvertUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import jp.co.nttdata.rate.exception.RateException;
import jp.co.nttdata.rate.fms.calculate.ICalculateContext;
import jp.co.nttdata.rate.log.LogFactory;
import jp.co.nttdata.rate.model.rateKey.rule.RateKeyConstraint;
import jp.co.nttdata.rate.model.rateKey.rule.RateKeyRule;

import jp.co.nttdata.rate.util.CommonUtil;
import jp.co.nttdata.rate.util.Const;

/**
 * 画面からレートキーについて単項目や制御関係のバリューデションを行う
 * @author btchoukug
 *
 */
public class RateKeyValidator {
	
	private static Logger logger = LogFactory.getInstance(RateKeyValidator.class);
	
	private static List<RateKeyConstraint> constraints;
		
	/**
	 * レートキーを編集する伴に、単項目チェックを行う
	 * @param keyName
	 * @param keyValue
	 * @return
	 * @throws RateException 
	 */
	public static void validteKey(RateKey key, String keyValue) throws RateException{
		boolean flg = true;
		String errType = null;
				
		if (StringUtils.isEmpty(keyValue)) {
			flg = false;
			errType = Const.ERR_EMPTY;
		} else if (CommonUtil.isNumeric(keyValue)) {
				
				RateKeyRule rule = key.getRule();				
				
				//レートキールールが設定された場合
				if (rule != null) {
					
					/*
					 * 0609追加：
					 * レートキーのなかに、日付の項目はよく使われてるため、
					 * 日付チェックを追加した
					 * 20120518追加：
					 * YYYYMM日付の項目の検証
					 */					
					if (rule.isValidateDate()) {
						if (keyValue.length() == 6) {
							keyValue = keyValue + "01";
						}
						if (!CommonUtil.isDateType(keyValue)) {
							flg = false;
							errType = Const.ERR_NOT_DATE;
						}
					} else {
						int value = (Integer) ConvertUtils.convert(keyValue, Integer.class);
						//上下限チェック及び特殊値チェック
						//特殊値のエラーメッセージを出す必要
						if (value <= rule.getMax() && value >= rule.getMin() && !isSpecialValue(value, rule.getSpecialValues())) {
							;
						} else {
							flg = false;
							errType = Const.ERR_SCOPE;
						}
					}
					
				}
				
		} else {
			flg = false;
			errType = Const.ERR_NOT_NUMERIC;
		}
		
		if (!flg) {
			throw new RateException(errType, key);
		}
		
	}
	
	/**
	 * すべての制御関係をセットする
	 * @param cons
	 */
	public static void setAllConstraint(List<RateKeyConstraint> cons) {
		constraints = cons;
	}
	
	/**
	 * OLの場合、すべての制御関係の検証を行って、エラーをスローする
	 * @param ctx 
	 * @throws Exception 
	 */
	public static void validateAllConstraint(ICalculateContext ctx) throws Exception {
		if (constraints != null) {
			if (logger.isInfoEnabled()){
				logger.info("☆レートキーに対して、制御関係の検証　開始☆");
			}
			for (RateKeyConstraint cons : constraints) {
				if (!_validateConstraint(ctx, cons)) {
					throw new RateException(Const.ERR_CONSTRAINT, cons);
					//入力チェックエラーとする
				}
			}
			if (logger.isInfoEnabled()) {
				logger.info("☆レートキーに対して、制御関係の検証　終了☆");
			}
		}
	}
	
	/**
	 * レートキーが編集終了の後、キーの制御関係の検証を行う
	 * @param ctx 
	 * @param constraint
	 * @param input
	 * @throws Exception 
	 */
	private static boolean _validateConstraint(ICalculateContext ctx, RateKeyConstraint constraint) throws Exception {

		//余計なスペースを外す
		String constraintCondition = CommonUtil.deleteWhitespace(constraint.getCondition());
		
		//制御関係の条件を計算する
		boolean ret = ctx.getParser().parse(constraintCondition).getBooleanValue();
		if (logger.isDebugEnabled()) {
			logger.debug("制御関係:" + constraintCondition + "=" + ret);
		}				
		return ret;					
	}
	
	/**
	 * 特殊値かどうかチェックを行う
	 * @param value
	 * @param specialValues
	 * @return
	 * @throws RateException
	 */
	public static boolean isSpecialValue(int value, int[] specialValues) {
		boolean rtn = false;
		
		if (specialValues != null) {
			for (int i = 0; i < specialValues.length; i++){
				if (specialValues[i] == value) {
					rtn = true;
					break;
				}
			}
		}
			
		return rtn;
	}
}
