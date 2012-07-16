package jp.co.nttdata.rate.fms.calculate;

import java.util.Map;

import jp.co.nttdata.rate.model.formula.Formula;
import jp.co.nttdata.rate.util.Const;

/**
 * 算式の計算結果をキャッシュに一時保存するため、キーを編集する
 * @author zhanghy
 *
 */
public class FormulaCacheKey {
	
	private Formula formula;
	private ICalculateContext ctx;
	private String paraValTxt;
	
	public FormulaCacheKey(Formula f, ICalculateContext ctx, Map<String, Object> paras) {
		this.formula = f;
		this.ctx = ctx;

		StringBuilder sbKey = new StringBuilder(this.formula.getName());
		for (Object obj : paras.values()) {
			sbKey.append(Const.DASH).append(obj);
		}
		this.paraValTxt = sbKey.toString();
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + formula.hashCode();
		result = prime * result + ctx.hashCode();
		result = prime * result + paraValTxt.hashCode();
		return result;
	}
 
	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()){
			return false;
		}
		FormulaCacheKey other = (FormulaCacheKey) obj;
		if (!formula.getName().equals(this.formula.getName())) {
			return false;
		}
		if (!other.ctx.equals(this.ctx)) {
			return false;
		}
		if (!other.paraValTxt.equals(this.paraValTxt)) {
			return false;
		}
		
		return true;
	}

}
