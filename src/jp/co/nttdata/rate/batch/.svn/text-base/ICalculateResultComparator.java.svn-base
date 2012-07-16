package jp.co.nttdata.rate.batch;

import java.util.Map;

public interface ICalculateResultComparator  {

	/**
	 * カレント計算にあたって、比較先と比較元を比べ、合否かどうかを返す
	 * @param ratekeys
	 * @param result
	 * @return
	 */
	public boolean compare(StringBuffer sbCompareResult, Map<String, Double> rateKeys, Map<String, Double> result);
	/**
	 * 比較対象のマッピングを設定
	 * @param relations
	 */
	public void setCompareMapping(Map<String,String> compareMapping);
	/***
	 * 比較対象のマッピングより、比較結果のヘッダを取得
	 * @param sbHeader
	 * @return
	 */
	public StringBuffer appendCompareHeader(StringBuffer sbHeader);
	/**
	 * NG件数を取得
	 * @return
	 */
	public long getNgCount();
}
