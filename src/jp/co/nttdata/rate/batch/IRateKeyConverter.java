package jp.co.nttdata.rate.batch;

import java.util.Map;

/**
 * 
 * お客様からデータを見ると、当月経過年月が１つ項目に纏めたり、支払ステータスが２つ項目以上に関わったり
 * マッピングを定義するうえ自動的に置換するように対応つもり
 * <br>商品毎にマッピング関係も違うかもしれない
 * @author btchoukug
 *
 */
public interface IRateKeyConverter {
	public Map<String, Integer> convert(Map<String, Integer> object);
	public void setFixedValue(Map<String, Integer> fixedValue);
}
