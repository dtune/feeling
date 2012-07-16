package jp.co.nttdata.rate.fms.core;


import java.math.BigDecimal;

import org.apache.commons.beanutils.ConvertUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import jp.co.nttdata.rate.fms.calculate.ICalculateContext;
import jp.co.nttdata.rate.fms.common.SystemFunctionUtility;
import jp.co.nttdata.rate.fms.core.keyword.Set;
import jp.co.nttdata.rate.log.LogFactory;
import jp.co.nttdata.rate.model.formula.Formula;

/**
 * 公式の中に変数である
 * <p>SYSFUNCや定義FUNCが対象とする、ただ下記の違いがある。</p>
 * <p>
 * Variableはレートキーと直接に関係がある、計算途中の臨時変数に関係ないこと； <br>
 * Functionはレートキーと関係がある伴に、計算途中の臨時変数にも関係があること。
 * </p>
 * 
 * @author btchoukug
 * 
 */
public class Variable implements Cacheable {

	private static Logger logger = LogFactory.getInstance(Variable.class);

	/** 変数名 */
	private String name;
	
	/** 変数に応じる公式ボディ */
	public Formula formula;
	
	/** 計算コンテキスト */
	private ICalculateContext _ctx;
	
	/** 計算可否 */
	public boolean isFormula = false;

	public Variable(String var, ICalculateContext ctx) {
		
		this.name = var;
		this._ctx = ctx;
		
		/*
		 * FIXME 変数名より計算式ボディを取得するのはFormulaParserに移した方がよい
		 * formulaが固定値の場合、valueを数字としてtokenを作成したうえ、臨時変数mapに保存する
		 * VariableはFactoryモードで作るように改善：外部で計算式リストを指定すれば、計算可否を判断する。
		 * なければ、直接inputのmapから取得する。
		 * DefFunctionの方も
		 */
		//Formulaリストの管理は外部から注入する
		this.formula = _ctx.getFormula(var);
		if (formula != null) {
			isFormula = true;
		}
	}
	
	public String getName() {
		return this.name;
	}

	@Override
	public String toString() {
		return this.name;
	}

	public BigDecimal value() throws Exception {

		BigDecimal ret = (BigDecimal) _ctx.getFunctionPara(name);
		
		if (ret != null) return ret;
		
		// 計算記号（中身がXMLに定義されている計算式や定数）の場合
		if (isFormula) {
			String body = this.formula.getBody();
			if (StringUtils.isEmpty(body)) {
				// 固定値の場合
				// FIXME 固定値であれば、FormulaParserのところで直接D数字としてTokenを作りいいじゃないですか
				ret = new BigDecimal(Double.toString(this.formula.getValue()));

			} else {
				// 公式定義で指定した計算基礎どおりに切り替え				
				boolean isShifted = _ctx.shiftFundation(this.formula);
				
				//キャッシュキーを作成したうえ、キャッシュ値を取得
				String key = _ctx.getContextKey(true).append("#").append(getCacheKey()).toString();
				ret = _ctx.getCache().getCacheVaule(key);
				
				if (ret == null){
					//キャッシュしていない場合、計算を行う
					Sequence seqBody = _ctx.getParser().parse(this.formula.getBody());
					Double noRoundingRet = seqBody.eval().doubleValue();
					
					/*
					 * 端数処理を行う
					 * 端数処理が-nになると、10のn次方まで四捨五入する
					 * e.g.-1の場合、123->120
					 */
					int scale = this.formula.getFraction();
					if (scale == Integer.MIN_VALUE) {
						ret = new BigDecimal(noRoundingRet.toString());
					} else {
						ret = new BigDecimal(Double.toString(SystemFunctionUtility.round(noRoundingRet, scale)));						
					}
					
					String varInfo = this.formula.toString();
					//サブ公式のフィルター
					if (_ctx.isFilterFormula(this.formula)) {
						if (_ctx.isRounding()) {
							_ctx.getIntermediateValue().put(varInfo, ret);
						} else {
							_ctx.getIntermediateValue().put(varInfo + "（端数処理なし）", noRoundingRet);
						}
					}
					if (logger.isInfoEnabled()) {
						logger.info("[VAR]" + varInfo + "=" + ret);
					}
					
					//setキーワードで作った臨時変数のスコープは当該算式に限るため、
					//計算完了するときに、コンテキストから外す
					for (Set set : seqBody.getAllSetBlocks()) {
						_ctx.removeTempVariable(set.getVariantName());	
					}
					
					//計算値をキャッシュに追加
					_ctx.getCache().addToCache(key, ret);
					
				}
				
				_ctx.rollbackFundation(isShifted, this.formula);
				
			}
		} else {
			/*
			 * ☆変数の値の取得について、優先順は cache > func parameter context 
			 * > compute context > ratekey context > SYSFUNC reflect callとする
			 * 変数（公式に定義された一時変数およびSYSFUNC、計算記号）
			 */
			Object tokenVal = _ctx.getTokenValue(name);
			ret = (BigDecimal)ConvertUtils.convert(tokenVal, BigDecimal.class);

		}
				
		return ret;
	}

	@Override
	public String getCacheKey() {
				
		/*
		 * TODO 分割払いの場合、年払・半年払い、月払いに関わらず、
		 * 基準Prateが同じなので、基準Prate単位でcacheしように。
		 */
		return this.name;
	}
	
	@Override
	public Formula getFormula() {
		return this.formula;
	}


}
