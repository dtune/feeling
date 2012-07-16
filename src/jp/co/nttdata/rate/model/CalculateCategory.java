package jp.co.nttdata.rate.model;

import java.util.List;
import jp.co.nttdata.rate.model.rateKey.RateKey;
import jp.co.nttdata.rate.model.rateKey.RateKeyManager;

/**
 * 計算カテゴリ
 * <p>（OLの方はTab；BTの方は出力ファイル種類）</p>
 * @author btchoukug
 *
 */
public class CalculateCategory {
	
	/**保険料*/
	public static final String P = "Premium";
	/**積立金*/
	public static final String V = "ReserveFund";
	/**解約返戻金*/
	public static final String W = "SurrenderFee";
	/**払済*/
	public static final String H = "Paidup";
	/**延長*/
	public static final String E = "Extend";
	/**未払年金*/
	public static final String A = "UnpaidAnnuity";
	/**配当金*/
	public static final String DIVIDEND = "Dividend";
	/**基本年金額*/
	public static final String BA = "BasicAnnuity";
	
	private String name;
	private String label;
	private String[] keys;
	private List<RateKey> keyList;
	
	public CalculateCategory(String name, String label, String[] keys) {
		this.name = name;
		this.label = label;
		this.setKeys(keys);
		
		this.keyList = RateKeyManager.getRateKeyDefs(keys);
	}
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getLabel() {
		return label;
	}
	public void setLabel(String label) {
		this.label = label;
	}
	public List<RateKey> getKeyList() {
		return keyList;
	}
	public void setKeyList(List<RateKey> keyList) {
		this.keyList = keyList;
	}

	public void setKeys(String[] keys) {
		this.keys = keys;
	}

	public String[] getKeys() {
		return keys;
	}	
	
}
