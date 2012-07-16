package jp.co.nttdata.rate.model.rateKey.rule;

/**
 * レートキーについて、上下限、特殊値、ステップのような
 * <br>入力ルールの基本情報を保存する
 * @author  btchoukug
 */
public class RateKeyRule {

	/** 上限 */
	private int max;
	/** 下限 */
	private int min;
	/** 入力不可の値 */
	private int[] specialValues;
	/** 上下限のステップ */
	private int step;
	
	/** 日付チェック要否
	 * （デフォルトとして日付チェックを行わない） */
	private boolean validateDate = false;
	
	public RateKeyRule(){
		;
	}
	
	/**
	 * 普通の場合、レートキーのステップが１とする
	 * かつ  日付チェックしない
	 * @param min
	 * @param max
	 * @param specialValues
	 */
	public RateKeyRule(int min, int max, int[] specialValues) {
		this.min = min;
		this.max = max;
		this.specialValues = specialValues;
		this.step = 1;
	}
	

	/**
	 * レートキーのステップを指定したうえでルールを生成
	 * @param min
	 * @param max
	 * @param specialValues
	 * @param step
	 * @param validateDate
	 */
	public RateKeyRule(int min, int max, int[] specialValues, int step, boolean validateDate) {
		this.min = min;
		this.max = max;
		this.specialValues = specialValues;
		this.step = step;
		this.validateDate = validateDate;
	}

	public int getMax() {
		return max;
	}

	public void setMax(int max) {
		this.max = max;
	}

	public int getMin() {
		return min;
	}

	public void setMin(int min) {
		this.min = min;
	}

	public int[] getSpecialValues() {
		return specialValues;
	}

	public void setSpecialValues(int[] specialValues) {
		this.specialValues = specialValues;
	}

	public int getStep() {
		return step;
	}

	public void setStep(int step) {
		this.step = step;
	}

	public boolean isValidateDate() {
		return validateDate;
	}

	public void setValidateDate(boolean validateDate) {
		this.validateDate = validateDate;
	}

	
}
