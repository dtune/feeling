package jp.co.nttdata.rate.fms.core;

import java.math.BigDecimal;

import org.apache.log4j.Logger;

import jp.co.nttdata.rate.fms.calculate.ICalculateContext;
import jp.co.nttdata.rate.fms.calculate.RateCalculateContext;
import jp.co.nttdata.rate.log.LogFactory;
/**
 * M[i]の形で配列の値を求る
 * <br>主に計算基礎は配列で保持することである
 * @author btchoukug
 *
 */
public class Array {
	private static Logger logger = LogFactory.getInstance(Array.class);
	
	public String name;
	public Sequence indexToken;

	/**計算コンテキスト*/
	protected ICalculateContext _ctx;
	
	public Array(Token name, Sequence index) {
		//配列の英語名
		this.name = name.toVariable().getName();
		//indexはVariableとなるかもしれないため、初期化時indexの値は求めない
		this.indexToken = index;
		this._ctx = index.getContext();
	}
	
	/**
	 * インデックスより配列の元素を取得
	 * @return
	 */
	public BigDecimal indexValue() {

		/*
		 * 計算するとき、index tokensをコピーしてindex値を取得
		 * （配列のインデックスがxだけではなく、ほかのパラメータと計算するかも）				
		 */
		int index = ((Sequence)indexToken.clone()).eval().intValue();

		//配列自身を取得
		Double[] value = ((RateCalculateContext) _ctx).getFundation(this.name);		
		//配列にてインデックス範囲チェック
		if (index < 0 || index >= value.length) {
			//throw new FmsRuntimeException("インデックス" + index + "が配列"+ this.name +"の範囲外になってしまいました。");
			//　範囲外の場合、ゼロとする
			if (logger.isInfoEnabled()) {
				logger.warn("インデックス" + index + "が配列"+ this.name +"の範囲外になってしまった。");
			}
			return BigDecimal.ZERO;
		}
		
		Double idxVal = value[index];
		BigDecimal ret = new BigDecimal(idxVal.toString());
		if (logger.isDebugEnabled()) {
			logger.debug("[Array]" + this.name + "[" + index + "]=" + idxVal);
		}
		return ret;
	}

	public String toString() {
		return this.name + "[" + this.indexToken.toString() + "]";
	}


}
