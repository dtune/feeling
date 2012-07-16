package jp.co.nttdata.rate.model.rateKey;

import java.util.Map;

import jp.co.nttdata.rate.model.rateKey.rule.RateKeyRule;

public interface IRateKeyRelationship {

	/*
	 *  TODO キーの間の制御でレートキーMAPを作ること
	 *  ただ、制御関係は外部XMLに追定義できるように改善予定
	 *  m<=n,if(kaisu==1){m=0}else{m=rule.max}
	 */
	// 
	/**
	 * 他のキーの値とキールールより最小値を見直し
	 * 主にnとmの間の制御、m<=n
	 * @param keyName 
	 * @param rule
	 * @return
	 */
	public abstract int getMinValue(String keyName, RateKeyRule rule,
			Map<String, Integer> keyValues);

	/**
	 * 他のキーの値とキールールより最大値を見直し
	 * 主にnとmの間の制御、m<=n
	 * @param keyName 
	 * @param rule
	 * @return
	 */
	public abstract int getMaxValue(String keyName, RateKeyRule rule,
			Map<String, Integer> keyValues);
	
	public abstract boolean validate(Map<String, Integer> keyValues);

}