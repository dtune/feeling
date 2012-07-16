package jp.co.nttdata.rate.fms.calculate;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.beanutils.ConvertUtils;
import org.apache.commons.lang.StringUtils;

import jp.co.nttdata.rate.exception.FmsDefErrorException;
import jp.co.nttdata.rate.exception.FmsRuntimeException;
import jp.co.nttdata.rate.model.formula.FormulaManager;
import jp.co.nttdata.rate.util.Const;
/***
 * 数理レート計算の専用モジュールである
 * <br>計算式定義、レートキー定義及び基数をロードする前提である
 * @author btchoukug
 *
 */
public class RateCalculator extends AbstractCalculator {
	
	String code;
	boolean enableCache;
	FormulaManager formulaMgr;
	
	public RateCalculator(String code) {
		_init(code, true, false, null);
	}	

	/**
	 * lazyモードとは、基数と公式情報は自身でロードしないよう、外部からロードする
	 * @param code
	 * @param enableCache
	 * @param lazyMode
	 */
	public RateCalculator(String code, boolean enableCache, boolean lazyMode, FormulaManager mgr) {
		_init(code, true, lazyMode, mgr);
	}
	
	public RateCalculator(String code, boolean enableCache) {
		_init(code, enableCache, false, null);
	}
	
	private void _init(String code, boolean enableCache, boolean lazyMode, FormulaManager mgr) {
		
		this.code = code;
		this.enableCache = enableCache;

		if (lazyMode) {
			formulaMgr = mgr;			
		} else {
			//lazyモードでない場合、基数マネージメント初期化
			formulaMgr = new FormulaManager(code);
		}
		
		try {
			//コンテキスト初期化
			this.ctx = new RateCalculateContext(code);
			this.ctx.setCacheEnabled(enableCache);

			//コンテキスト毎に算式定義を保存する
			this.ctx.setFormulaManager(formulaMgr);
		} catch (FmsDefErrorException e) {
			throw new FmsRuntimeException("コンテキスト初期化失敗", e);
		}

	}
	
	/**
	 * 指定の計算カテゴリどおり、公式定義をロードする
	 * @param cates
	 */
	public void setCalculateCate(String[] cates) {
		formulaMgr.load(cates);
	}

	@SuppressWarnings("unchecked")
	@Override
	public void setRateKeys(Map rateKeys) {
		this.ctx.setInput(rateKeys);
	}	
	
	@Override
	public Double calculate(String formulaText) {
		Double ret = null;
		try {
			ret = super.calculate(formulaText);
		} catch (Exception e) {
			throw new FmsRuntimeException("算式計算エラー：" + formulaText, e);
		}
		
		return ret;
	}
	
	public ICalculateContext getContext() {
		return this.ctx;
	}
	
	public static void main(String[] args) {
		
		try {
			RateCalculator rc = new RateCalculator("009");
			rc.setCalculateCate(new String[]{"Premium","ReserveFund","SurrenderFee"});
			rc.setRateKeys(_str2Map("{f=3, birthday=20081205, gen=4, sex=0, t1=2, n=22, kaisu=4, state=1, m=18, afterBenifitPay=0, contractorSex=2, contractDate=20090101, t=2, f1=3, z=0, kisoritsu=0, criterionDate=20110331, SA=1000000, y=26, x=0}"));
			
			long t1 = System.currentTimeMillis();
			System.out.println(rc.calculate("ContractorVrate"));	
			long t2 = System.currentTimeMillis();
			
			System.out.println("total time:"+(t2-t1));
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
	
	@SuppressWarnings("unchecked")
	public static Map _str2Map(String ratekeyTxt) {
		
		String pairTxt = ratekeyTxt.substring(1, ratekeyTxt.length() - 1);
		if (StringUtils.isBlank(pairTxt) || pairTxt.indexOf(Const.COMMA) < 0 || pairTxt.indexOf(Const.EQ) < 0) {
			throw new IllegalArgumentException("レートキーの文字列が間違っていた");
		}
		
		Map rateKeys = new HashMap();
		String[] pairs = StringUtils.split(pairTxt, Const.COMMA);
		for (String pair : pairs) {
			String[] entry = StringUtils.deleteWhitespace(pair).split(Const.EQ);
			if (entry.length == 2) {
				rateKeys.put(entry[0], ConvertUtils.convert(entry[1], double.class));
			} else {
				throw new IllegalArgumentException(entry[0] + "にてキー或いは値が漏れ");
			}
		}
				
		return rateKeys;		
		
	}

}
