package jp.co.nttdata.rate.model.rateKey;

import jp.co.nttdata.rate.model.rateKey.rule.RateKeyRule;
/**
 * 数理レート計算用のレートキーである
 * @author btchoukug
 *
 */
public class RateKey implements Comparable<RateKey> {
	
	public static final String TEXT_TYPE = "text";
	public static final String RADIO_TYPE = "radio";
	public static final String COMBO_TYPE = "combo";
	
	private String name;
	private String type;
	private String label;
	private boolean required;
	private int displayOrder;
	private int value;
	
	/** レートキールール */
	private RateKeyRule rule;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}
	
	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	public boolean isRequired() {
		return required;
	}

	public void setRequired(boolean required) {
		this.required = required;
	}

	public int getDisplayOrder() {
		return displayOrder;
	}

	public void setDisplayOrder(int displayOrder) {
		this.displayOrder = displayOrder;
	}

	public RateKeyRule getRule() {
		return rule;
	}

	public void setRule(RateKeyRule rule) {
		this.rule = rule;
	}

	public int getValue() {
		return value;
	}

	public void setValue(int value) {
		this.value = value;
	}

	@Override
	public int compareTo(RateKey arg0) {
		// レートキーの英字の順番でソート
		return this.name.compareTo(arg0.name);
	}
	
	
	
}
