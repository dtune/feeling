package jp.co.nttdata.rate.fms.calculate;

import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

import jp.co.nttdata.rate.exception.FmsRuntimeException;
import jp.co.nttdata.rate.log.LogFactory;
import jp.co.nttdata.rate.model.formula.Formula;
import jp.co.nttdata.rate.model.formula.FormulaManager;
import org.apache.log4j.Logger;

public abstract class AbstractCalculateContext implements ICalculateContext {

	protected Logger logger = LogFactory.getInstance(this.getClass());
	/**
	 * 基本的に計算の基礎単位を格納する
	 * <br>（レートキー、基数とSYS変数）
	 */
	@SuppressWarnings("unchecked")
	protected Map tokenValueMap;
	
	/** 指定された算式の値をフィルターするかないか(デフォルトとしてフィルターしない) */
	protected boolean enableFilter = false;
	
	/**
	 * ☆ファンクションより他のファンクションを呼び出しと伴に、 パラメータは呼び順で保持すること ☆
	 */
	protected Stack<Map<String, Object>> funcParameterStack = new Stack<Map<String, Object>>();
	
	/** setキーワードで臨時変数の格納先　*/
	protected Map<String, Object> setValues = new HashMap<String, Object>();	
	
	protected Map<String, Object> intermediateValues;

	protected FormulaManager formulaMgr;
	
	public AbstractCalculateContext() {
		;
	}
	@Override
	public FormulaManager getFormulaManager() {
		if (this.formulaMgr == null) {
			throw new RuntimeException("Formulaマネジャが初期化されていません");
		}
		return this.formulaMgr;
	}
	
	@Override
	public void setFormulaManager(FormulaManager mgr) {
		formulaMgr = mgr;		
	}
	
	@Override
	public Formula getFormula(String var) {
		if (this.formulaMgr == null) {
			return null;
		}
		return formulaMgr.getFormula(var);
	}
	
	@Override
	public boolean isFuncExist(String funcName) {
		if (formulaMgr == null) return false;
		return formulaMgr.isExist(funcName);
	}
	
	@Override
	public boolean isFilterFormula(Formula formula) {
		
		if (enableFilter) {
			if (formulaMgr == null) return false;
			return formulaMgr.isFilterFormula(formula);			
		} 
		
		return false;
	}
	
	@Override
	public boolean isRounding() {
		if (formulaMgr == null) return false;
		return formulaMgr.isRounding();
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public void setInput(final Map input) {
		reset();
		this.tokenValueMap = input;		
		if (logger.isInfoEnabled()) {
			logger.info("入力のレートキー：" + input.toString());
		}
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public Map getInput() {
		return this.tokenValueMap;
	}
	
	/**
	 * それぞれFUNCのパラメータを分けるように、呼び順よりstackにpushする
	 * FUNCが計算終了次第、応じたパラメータをpopする
	 * @param paras
	 */
	@Override
	public void addFunctionPara(Map<String, Object> paras) {
		this.funcParameterStack.push(paras);
	}

	/**
	 * 漸化式計算中、動的にパラメータを編集するため、コンテキストにパラメータを追加 <br>
	 * setキーワードで一時変数の設定 も行う<br>
	 * パラメータが既に存在していた場合、上書きとする
	 */
	@Override
	public boolean addTempVariable(String varName, Object varValue) {
		setValues.put(varName, varValue);
		if (logger.isDebugEnabled()) {
			logger.debug("setキーワードで作られた一時的な変数：" + varName + "=" + varValue);
		}
		return true;
	}

	@Override
	public void clearCurrentFunctionPara() {
		this.funcParameterStack.pop();
	}

	@Override
	public Object getFunctionPara(String paraName) {
		if (this.funcParameterStack.isEmpty()) return null; 
		return this.funcParameterStack.peek().get(paraName);
	}

	@Override
	public Object getTempVariable(String varName) {
		return setValues.get(varName);
	}

	@Override
	public Object getTokenValue(String tokenName) {
		if (this.tokenValueMap == null) {
			throw new FmsRuntimeException("レートキーを設定してください。");
		}

		Object value = null;
		//まず、臨時変数とみなして取得してみて
		if (setValues.containsKey(tokenName)) {
			return setValues.get(tokenName);
		} else {
			//基数ではない場合、レートキーから取得
			value = this.tokenValueMap.get(tokenName);
		}

		//上記で取得できなかった場合、エラーとする
		if (value == null) {
			throw new FmsRuntimeException("レートキーや臨時変数[" + tokenName + "]の値が取得できなかった!");			
		}

		return value;
	}

	@Override
	public boolean removeTempVariable(String varName) {
		setValues.remove(varName);	
		return true;		
	}
	
	@Override
	public Map<String, Object> getLastParas() {
		if (funcParameterStack.isEmpty()) return null;
		return funcParameterStack.peek();		
	}
	

	@Override
	public void reset() {
		this.tokenValueMap = null;
		this.funcParameterStack.clear();
		
		this.setValues.clear();
		this.intermediateValues = null;	
	}
	
}
