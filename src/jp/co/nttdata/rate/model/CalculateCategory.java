package jp.co.nttdata.rate.model;

import java.util.List;
import jp.co.nttdata.rate.model.rateKey.RateKey;
import jp.co.nttdata.rate.model.rateKey.RateKeyManager;

/**
 * vZJeS
 * <p>iOLΜϋΝTabGBTΜϋΝoΝt@Cνήj</p>
 * @author btchoukug
 *
 */
public class CalculateCategory {
	
	/**Ϋ―Ώ*/
	public static final String P = "Premium";
	/**Ο§ΰ*/
	public static final String V = "ReserveFund";
	/**πρΤίΰ*/
	public static final String W = "SurrenderFee";
	/**₯Ο*/
	public static final String H = "Paidup";
	/**·*/
	public static final String E = "Extend";
	/**’₯Nΰ*/
	public static final String A = "UnpaidAnnuity";
	/**zΰ*/
	public static final String DIVIDEND = "Dividend";
	/**ξ{Nΰz*/
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
