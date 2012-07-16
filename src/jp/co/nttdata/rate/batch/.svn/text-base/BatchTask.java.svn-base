package jp.co.nttdata.rate.batch;

import java.util.Map;

/**
 * バッチ処理のタスクである
 * <br>中で、一回計算の入力のレートキー、応じて計算式の結果、
 * およびバッチ処理の最終標識フラグを格納する。
 * @author btchoukug
 *
 */
public class BatchTask {

	/**レートキーを格納する*/
	private Map<String, Double> rateKeys;
	/**入力されたレートキーより計算式の値を格納する*/
	private Map<String, Double> formulaResults;
	/**最終フラグ*/
	private boolean isEOF = false;
	/**レートキーチェックには何かエラーだった場合、falseにする*/
	private boolean isCalculable = true;
	/**特体フラグ*/
	private boolean isSpecial = false;
	/**入力データの行目*/
	private long taskNo = 0;

	public BatchTask(Map<String, Double> rateKeys) {
		this.rateKeys = rateKeys;
	}
	
	public BatchTask(Map<String, Double> rateKeys, long taskNo) {
		this.rateKeys = rateKeys;
		this.taskNo = taskNo;
	}

	public void setFormulaResults(Map<String, Double> results) {
		this.formulaResults = results;
	}
	
	public Map<String, Double> getRateKeys() {
		return rateKeys;
	}

	public Map<String, Double> getFormulaResults() {
		return formulaResults;
	}

	public boolean isEOF() {
		return isEOF;
	}

	public void setEOF(boolean isEOF) {
		this.isEOF = isEOF;
	}

	public void setCalculable(boolean isCalculable) {
		this.isCalculable = isCalculable;
	}

	public boolean isCalculable() {
		return isCalculable;
	}

	public void setSpecial(boolean isSpecial) {
		this.isSpecial = isSpecial;
	}

	public boolean isSpecial() {
		return isSpecial;
	}

	public void setTaskNo(long taskNo) {
		this.taskNo = taskNo;
	}

	public long getTaskNo() {
		return taskNo;
	}
	
}
