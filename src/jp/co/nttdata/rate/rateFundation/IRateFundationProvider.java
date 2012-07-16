package jp.co.nttdata.rate.rateFundation;

import java.util.Map;

import jp.co.nttdata.rate.exception.FmsDefErrorException;

/**
 * XMLやDBから基数を読み込む
 * <br>また、基数のコントロール用のグループキーも編集する
 * @author btchoukug
 *
 */
public interface IRateFundationProvider {

	/**
	 * 性別と基数のグループキーより、計算基数をロードする
	 * @param sex
	 * @param groupKey
	 * @return
	 * @throws FmsDefErrorException
	 */
	public RateFundationGroup loadFundationGroup(String groupKey) throws FmsDefErrorException;

	/**
	 * 基数グループキー編集
	 * @param valuesMap
	 * @return
	 */
	public String editFundationGroupKey(Map valuesMap);

}