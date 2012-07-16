package jp.co.nttdata.rate.fms.core;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import jp.co.nttdata.rate.exception.FmsDefErrorException;
import jp.co.nttdata.rate.fms.calculate.ICalculateContext;
import jp.co.nttdata.rate.fms.common.SystemFunctionUtility;
import jp.co.nttdata.rate.fms.core.keyword.Set;
import jp.co.nttdata.rate.model.formula.Formula;
import jp.co.nttdata.rate.util.Const;
/**
 * 
 * それぞれの算式について、自由に書いて自動的に計算されるように定義FUNCTIONである <br>
 * 算式の中に、if,elseIf,else,while,sum,setキーワードやratekey,fundation
 * の記号は使えるし、オペレータより四則計算もできる。
 * 
 * @author btchoukug
 * 
 */
public class DefFunction extends Function implements Cacheable{

	/** 定義の計算式(例：e(t)=if(kaisu==1&&t<=10){0.0008}else{0})*/
	private Formula formula;
	/** FUNCのパラメータリスト */
	private List<String> paraNameList;

	/** DEFFUNCが計算する際に、パラメータ取得については下記のparasを最優先とする 
	 * <p>parasの中に存在していない場合、ほかの取得先（ratekey,fundation,臨時変数）から取得する</p>*/
	private Map<String, Object> paras = null;
	
	public DefFunction(String functionName, ICalculateContext ctx) throws FmsDefErrorException {
		
		super(functionName, ctx);
		
		//FIXME Formulaリストの管理は外部から注入する
		if (_ctx.isFuncExist(this.funcName)) {
			this.formula = _ctx.getFormula(this.funcName);
			// 計算メソッドのパラメータを取得
			this.setParaNameList(this.formula.getParas());
		} else {
			throw new FmsDefErrorException(this.funcName + "当記号が定義されてない。");
		}
		
	}

	@Override
	public BigDecimal result() throws Exception {

		// 算式定義で指定した計算基礎通りに切り替えを行う
		boolean isShifted = _ctx.shiftFundation(this.formula);
		
		// キャッシュ可否より、レートキーと関係性を判明
		// TODO 現時点ではformulaのattr[cacheable]で指定される；
		//　将来的に、プログラムで自動的に判明されるように改善するつもりです
		boolean isRatekeyRelated = !this.formula.isCacheable();
		
		//キャッシュキーを作成したうえ、キャッシュ値を取得
		String key = _ctx.getContextKey(isRatekeyRelated).append("#").append(getCacheKey()).toString();		
		BigDecimal ret = _ctx.getCache().getCacheVaule(key);
		
		// キャッシュしていない場合、計算を行う
		if (ret == null) {
			// まず、パラメータをコンテキストの専用stackに追加
			_ctx.addFunctionPara(this.paras);
			
			Sequence seqBody = _ctx.getParser().parse(this.formula.getBody());
			Double withoutRoundingRet = seqBody.eval().doubleValue();		

			/*
			 * 端数処理を行う（計算式の属性で定義したり、直接roundを書いたり両方ともできる）
			 * 端数処理が-nになると、10のn次方まで四捨五入する
			 * e.g.-1の場合、123->120
			 */
			int scale = this.formula.getFraction();
			if (scale == Integer.MIN_VALUE) {
				ret = new BigDecimal(withoutRoundingRet.toString());
			} else {
				ret = new BigDecimal(Double.toString(SystemFunctionUtility.round(withoutRoundingRet, scale)));						
			}			
						
			//サブ公式のフィルター
			if (_ctx.isFilterFormula(this.formula)) {
				String funcInfo = this.formula.toString() + this.paras.toString();
				if (_ctx.isRounding()) {
					_ctx.getIntermediateValue().put(funcInfo, ret);
				} else {
					_ctx.getIntermediateValue().put(funcInfo + "（端数処理なし）", withoutRoundingRet);
				}				
			}
			
			if (logger.isInfoEnabled()) {
				StringBuilder sb = new StringBuilder("[FUNC]");
				sb.append(this.formula.toString()).append(this.paras.toString()).append("=").append(ret);
				logger.info(sb.toString());	
			}
			
			// 当FUNCが計算完了次第、パラメータをpopして、呼出元のパラメータに戻す
			_ctx.clearCurrentFunctionPara();
			
			
			/*
			 * setキーワードで作った臨時変数のスコープは当該算式に限るため、
			 * 計算完了するときに、コンテキストから外す
			 */
			for (Set set : seqBody.getAllSetBlocks()) {
				_ctx.removeTempVariable(set.getVariantName());	
			}			
			
			//計算値をキャッシュに追加
			_ctx.getCache().addToCache(key, ret);
			
		}

		_ctx.rollbackFundation(isShifted, this.formula);
				
		return ret;
	}

	@Override
	public String toString() {
		return this.funcName + (this.paras == null ? "" : this.paras.toString());
	}

	/**
	 * 普通の場合、FuncKeyを作成するのはパラメータのname:value 
	 * <br>計算基礎を用いている公式の場合、計算基礎を付ける
	 * 
	 * @return
	 */
	@SuppressWarnings("unchecked")
	@Override
	public String getCacheKey() {
		Collection collection = this.paras.values();
		StringBuilder sbKey = new StringBuilder(this.funcName);
		for (Object obj : collection) {
			sbKey.append(Const.DASH).append(obj);
		}
		
		return sbKey.toString();
	}

	public void setParaNameList(List<String> paraNameList) {
		this.paraNameList = paraNameList;
	}
	
	public void setParas(Map<String, Object> paras) {
		this.paras = paras;
	}
	
	@Override
	public Formula getFormula() {
		return formula;
	}

	public List<String> getParaNameList() {
		return paraNameList;
	}

		
}
