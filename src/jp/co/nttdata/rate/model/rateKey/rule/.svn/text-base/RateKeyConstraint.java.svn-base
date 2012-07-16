package jp.co.nttdata.rate.model.rateKey.rule;

/**
 * レートキールールより自動的に生成されたレートキーに対して、
 * <br>業務上にレートキーのフィルターまたは、レートキーの間に制御関係を検証する。
 * @author btchoukug
 *
 */
public class RateKeyConstraint {

	private String[] constraintKeys;
	private String desc;
	private String condition;
	
	public RateKeyConstraint(String[] constraintKeys, String desc, String condition) {
		this.constraintKeys = constraintKeys;
		this.desc = desc;
		this.condition = condition;
	}

	/** 制御関係ありのキーを取得 */
	public String[] getConstraintKeys() {
		return constraintKeys;
	}

	public String getCondition() {
		return condition;
	}

	public String getDesc() {
		return desc;
	}
	
}
