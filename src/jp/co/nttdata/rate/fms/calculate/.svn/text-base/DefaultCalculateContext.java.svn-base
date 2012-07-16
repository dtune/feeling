package jp.co.nttdata.rate.fms.calculate;

import java.util.HashMap;
import java.util.Map;
import jp.co.nttdata.rate.fms.core.FormulaParser;
import jp.co.nttdata.rate.model.formula.Formula;
import jp.co.nttdata.rate.util.CommonUtil;

/**
 * 商品の特性に関わらず、普通の四則演算を行う
 * @author btchoukug
 *
 */
public class DefaultCalculateContext extends AbstractCalculateContext {
	
	protected FormulaParser _parser;
	protected ResultEvaluator _evaluator;
	
	public DefaultCalculateContext() {
		_parser = new FormulaParser(this);
		_evaluator = new ResultEvaluator();
	}
	
	/*
	 * (non-Javadoc)
	 * DefaultCalculateContextに対して、基数の切り替えは要らない
	 * @see jp.co.nttdata.rate.fms.calculate.ICalculateContext#rollbackFundation(boolean)
	 */
	@Override
	public void rollbackFundation(boolean isShifted, Formula formula) throws Exception {
		;		
	}

	@Override
	public boolean shiftFundation(Formula formula) throws Exception {
		return false;
	}

	public FormulaParser getParser() {
		return this._parser;
	}
	
	public ResultEvaluator getEvaluator() {
		return this._evaluator;
	}

	@Override
	public Map<String, Object> getIntermediateValue() {
		if (this.intermediateValues == null) {
			this.intermediateValues = new HashMap<String, Object>();
		}
		return this.intermediateValues;
	}

	@Override
	public CacheManagerSupport getCache() {
		throw new IllegalAccessError("普通の四則演算にはキャッシュなし");
	}

	@Override
	public void setCacheEnabled(boolean enableCache) {
		throw new IllegalAccessError("普通の四則演算にはキャッシュなし");
	}

	@Override
	public StringBuilder getContextKey(boolean isRatekeyRelated) {
		return new StringBuilder(CommonUtil.FNVHash1(this.tokenValueMap.toString()));
	}
}
