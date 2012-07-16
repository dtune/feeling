package jp.co.nttdata.rate.model.formula;

import java.util.List;
import org.apache.commons.lang.StringUtils;
import jp.co.nttdata.rate.util.Const;

/**
 * �Z���̉p�ꖼ����{�ꖼ�A�{�f�B�A�p�����[�^�ȂǁA���ꂼ��Z����`����������<br>
 * ������`XML�ƃ}�b�s���O�����
 * @author btchoukug
 *
 */
public class Formula implements Comparable<Formula> {
	
	/** �������i���̌����Ɏg����j */
	private String name;
	/** �v�Z�Ώۃt���O */
	private boolean accessable;
	/** �����̐��� */
	private String desc = Const.EMPTY;
	/** �z��ҕW�� */
	private boolean mate = false;
	/** �N��� */
	private int limitedAge = 0;
	/** ���ʐ��� */
	private String limitedSex = Const.EMPTY;
	/**
	 * ����̌v�Z�p��̏��i�R�[�h ������`�Ɏw�肵�Ă��Ȃ��ꍇ�A �v�Z����Ƃ��ɃJ�����g���i�Ƃ���
	 * */
	private String fundCode = Const.EMPTY;
	/**
	 * �@ �{���́A���ʏ����t���i�ۏ�j�ƈꏏ�Ɏg����
	 * <ul>
	 * <li>���̂̏ꍇ�A4�{�̈���1�{�̂�ݒ肷�ׂ�<br>
	 * 4�{�́F4��ݒ�G1�{�́F1��ݒ�</li>
	 * <li>���ʂ�PVW���v�Z����ꍇ�A�W���̂�0��ݒ�</li>
	 * <ul>
	 * */
	private int xtime = -1;

	/** �����̌v�Z��b(������`�Ɏw�肵�Ă��Ȃ��ꍇ�A�󕶎��Ƃ���) */
	private String pvh = Const.EMPTY;
	/** �[������(�f�t�H���g�F�����Ȃ�) */
	private int fraction = Integer.MIN_VALUE;
	/** �����̃p�����[�^ */
	private List<String> paras;
	/** �����̃{�f�B */
	private String body;
	/** �Œ�l�̏ꍇ */
	private double value;

	/** �T�u�����̃��X�g */
	private List<Formula> subFormulaList;
	
	/**
	 * �Z���̒l�̓��[�g�L�[�Ɋւ�炸�A��ƃp�����[�^�Ɉ˗�����ꍇ�A
	 * cacheable�Ōv�Z�S�̎g����悤�Ɏw�肷��
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
	 * �\�����\�[�g�p(describe)
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
	 * �����̒l�̓��[�g�L�[�Ɋւ�炸�A��ƃp�����[�^�Ɉ˗�����ꍇ�A
	 * BT�v�Z�S�̓I�ɋ��L��cache����邩�ǂ���
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
