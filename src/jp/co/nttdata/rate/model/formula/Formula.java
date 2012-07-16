package jp.co.nttdata.rate.model.formula;

import java.util.List;
import org.apache.commons.lang.StringUtils;
import jp.co.nttdata.rate.util.Const;

/**
 * 算式の英語名や日本語名、ボディ、パラメータなど、それぞれ算式定義情報を持つもの<br>
 * 公式定義XMLとマッピングされる
 * @author btchoukug
 *
 */
public class Formula implements Comparable<Formula> {
	
	/** 公式名（他の公式に使われる） */
	private String name;
	/** 計算対象フラグ */
	private boolean accessable;
	/** 公式の説明 */
	private String desc = Const.EMPTY;
	/** 配偶者標識 */
	private boolean mate = false;
	/** 年齢制限 */
	private int limitedAge = 0;
	/** 性別制限 */
	private String limitedSex = Const.EMPTY;
	/**
	 * 特定の計算用基数の商品コード 公式定義に指定していない場合、 計算するときにカレント商品とする
	 * */
	private String fundCode = Const.EMPTY;
	/**
	 * 　 倍数体、特別条件付き（保障）と一緒に使われる
	 * <ul>
	 * <li>特体の場合、4倍体或は1倍体を設定すべき<br>
	 * 4倍体：4を設定；1倍体：1を設定</li>
	 * <li>普通のPVWを計算する場合、標準体で0を設定</li>
	 * <ul>
	 * */
	private int xtime = -1;

	/** 公式の計算基礎(公式定義に指定していない場合、空文字とする) */
	private String pvh = Const.EMPTY;
	/** 端数処理(デフォルト：処理なし) */
	private int fraction = Integer.MIN_VALUE;
	/** 公式のパラメータ */
	private List<String> paras;
	/** 公式のボディ */
	private String body;
	/** 固定値の場合 */
	private double value;

	/** サブ公式のリスト */
	private List<Formula> subFormulaList;
	
	/**
	 * 算式の値はレートキーに関わらず、基数とパラメータに依頼する場合、
	 * cacheableで計算全体使われるように指定する
	 */
	private boolean cacheable = false;
	
	@Override
	public String toString() {
		if (StringUtils.isBlank(desc)) {
			return name;
		} else {
			return desc;
		}
	}
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public boolean isAccessable() {
		return accessable;
	}
	public void setAccessable(boolean accessable) {
		this.accessable = accessable;
	}
	public String getDesc() {
		return StringUtils.isBlank(desc) ? name : desc;
	}
	public void setDesc(String desc) {
		this.desc = desc;
	}
	public List<String> getParas() {
		return paras;
	}
	public void setParas(List<String> args) {
		this.paras = args;
	}
	public String getBody() {
		return body;
	}
	public void setBody(String body) {
		this.body = body;
	}
	public double getValue() {
		return value;
	}
	public void setValue(double value) {
		this.value = value;
	}
	public List<Formula> getSubFormulaList() {
		return subFormulaList;
	}
	public void setSubFormulaList(List<Formula> subFormulaList) {
		this.subFormulaList = subFormulaList;
	}
	public String getPvh() {
		return pvh;
	}
	public void setPvh(String base) {
		this.pvh = base;
	}
	
	/**
	 * 表示順ソート用(describe)
	 */
	@Override
	public int compareTo(Formula arg0) {
		//compare by describe, not name
		if (arg0.desc == null) {
			return 1;
		} else if (this.desc == null) {
			return -1;
		} else {
			return this.desc.compareTo(arg0.desc);
		}
	}

	public void setMate(boolean mate) {
		this.mate = mate;
	}

	public boolean isMate() {
		return mate;
	}

	public void setLimitedAge(int limitedAge) {
		this.limitedAge = limitedAge;
	}

	public int getLimitedAge() {
		return limitedAge;
	}

	public void setLimitedSex(String limitedSex) {
		this.limitedSex = limitedSex;
	}

	public String getLimitedSex() {
		return limitedSex;
	}

	public void setFraction(int fraction) {
		this.fraction = fraction;
	}

	public int getFraction() {
		return fraction;
	}

	public void setCacheable(boolean cacheable) {
		this.cacheable = cacheable;
	}

	/**
	 * 公式の値はレートキーに関わらず、基数とパラメータに依頼する場合、
	 * BT計算全体的に共有でcacheされるかどうか
	 */
	public boolean isCacheable() {
		return cacheable;
	}

	public void setFundCode(String fundCode) {
		this.fundCode = fundCode;
	}

	public String getFundCode() {
		return fundCode;
	}

	public void setXtime(int xtime) {
		this.xtime = xtime;
	}

	public int getXtime() {
		return xtime;
	}

}
