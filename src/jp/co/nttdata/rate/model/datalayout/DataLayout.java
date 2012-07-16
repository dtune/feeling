package jp.co.nttdata.rate.model.datalayout;

import jp.co.nttdata.rate.model.rateKey.rule.RateKeyRule;
/**
 * 数理レート計算用のレートキーである
 * @author btchoukug
 *
 */
public class DataLayout implements Comparable<DataLayout> {
	

	
	private String name;
	private String desc;
	private int pos;
	private int len;
	private String initValue;
	
	/** レートキールール */
	private RateKeyRule rule;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public RateKeyRule getRule() {
		return rule;
	}

	public String getDesc() {
		return desc;
	}

	public void setDesc(String desc) {
		this.desc = desc;
	}

	public void setRule(RateKeyRule rule) {
		this.rule = rule;
	}

	public int getPos() {
		return pos;
	}

	public void setPos(int pos) {
		this.pos = pos;
	}



	@Override
	public int compareTo(DataLayout arg0) {
		// レートキーの英字の順番でソート
		return this.name.compareTo(arg0.name);
	}

	public int getLen() {
		return len;
	}

	public void setLen(int len) {
		this.len = len;
	}

	public String getInitValue() {
		return initValue;
	}

	public void setInitValue(String initValue) {
		this.initValue = initValue;
	}
}
