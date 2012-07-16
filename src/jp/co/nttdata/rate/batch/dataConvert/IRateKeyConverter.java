package jp.co.nttdata.rate.batch.dataConvert;

import java.util.Map;
import jp.co.nttdata.rate.batch.dataConvert.RateKeyLayout;

/**
 * 
 * お客様からデータを見ると、当月経過年月が１つ項目に纏めたり、支払ステータスが２つ項目以上に関わったり
 * マッピングを定義するうえ自動的に置換するように対応つもり
 * <br>商品毎にマッピング関係も違うかもしれない
 * @author btchoukug
 *
 */
public interface IRateKeyConverter {
	/**
	 * 関連項目を基づいて新レートキーに置換する
	 * @param object
	 * @return
	 */
	public Map<String, Double> convert(Map<String, Double> object);
	public void setFixedValue(Map<String, Double> fixedValue);
	public void setCompareObjectNames(String[] compareObjNames);
	
	/**
	 * レートキーのレイアウトより数字に置換する(単項目)
	 * @param layout
	 * @param value
	 * @return
	 */
	public double convertRateKey(RateKeyLayout layout, String value);
}
