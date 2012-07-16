package jp.co.nttdata.rate.fms.calculate;

import java.util.Map;
import jp.co.nttdata.rate.fms.core.FormulaParser;
import jp.co.nttdata.rate.model.formula.Formula;
import jp.co.nttdata.rate.model.formula.FormulaManager;

/**
 * 計算に向け、それぞれ計算にあたって使われる因子や変数を提供する
 * @author zhanghy
 *
 */
public interface ICalculateContext {
	
	public void setFormulaManager(FormulaManager mgr);

	public FormulaManager getFormulaManager();
	
	public Formula getFormula(String var);
	
	public boolean isFuncExist(String funcName);

	public boolean isFilterFormula(Formula formula);

	public boolean isRounding();
	
	/**
	 * 計算用の入力パラメータをセットする
	 * @param input
	 */
	@SuppressWarnings("unchecked")
	public void setInput(final Map input);
	
	@SuppressWarnings("unchecked")
	public Map getInput();
	
	/**
	 * 分解したTokensを計算されるために、それぞれのtokenの値を取得する
	 * @return
	 */
	public Object getTokenValue(String tokenName);	
	/**
	 * 臨時変数を追加
	 * @param varName
	 * @param varValue
	 * @return
	 */
	public boolean addTempVariable(String varName, Object varValue);
	/**
	 * 臨時変数の値を取得
	 * @param varName
	 * @return
	 */
	public Object getTempVariable(String varName);
	/**
	 * 臨時変数を外す
	 * @param varName
	 * @return
	 */
	public boolean removeTempVariable(String varName);
	
	/**
	 * それぞれFUNCのパラメータを分けるように、呼び順よりstackにpushする
	 * FUNCが計算終了次第、応じたパラメータをpopする
	 * @param paras
	 */
	public void addFunctionPara(Map<String, Object> paras);
	/**
	 * カレント計算公式に応じてパラメータの値を取得
	 * @param name
	 * @return
	 */
	public Object getFunctionPara(String paraName);
	/**
	 * FUNCが計算終了次第、応じたパラメータをpopする
	 */
	public void clearCurrentFunctionPara();
	
	/**
	 * 計算基数を切り替え
	 * @param formula
	 * @return
	 */
	public boolean shiftFundation(Formula formula) throws Exception;

	/**
	 * 計算基数を基に戻す
	 * @param isShifted
	 */
	public void rollbackFundation(boolean isShifted, Formula formula) throws Exception;

	public FormulaParser getParser();
	public ResultEvaluator getEvaluator();

	public Map<String, Object> getLastParas();

	/**
	 * 途中値を返す
	 * @return
	 */
	public Map<String, Object> getIntermediateValue();

	public CacheManagerSupport getCache();

	public void setCacheEnabled(boolean enableCache);

	/**
	 * 新たに計算するため、計算コンテキストをリセットして初期化状態に戻す
	 * <br>それぞれの基数とか途中値とか、レートキーとかをクリア
	 * */
	public void reset();
	
	/**
	 * キャッシュ用のコンテキストキーを作成
	 * <br>公式の値はレートキーに関わらず、基数とパラメータのみに依頼する場合
	 * レートキーのコンテキストキーをキャッシュキーから外す
	 * @param ctx
	 * @param isRatekeyRelated formulaのattr「cacheable」で指定
	 * @return
	 */
	public StringBuilder getContextKey(boolean isRatekeyRelated);
	
}
