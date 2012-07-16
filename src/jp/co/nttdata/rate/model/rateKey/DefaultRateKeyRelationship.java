package jp.co.nttdata.rate.model.rateKey;

import java.util.Map;

import jp.co.nttdata.rate.model.rateKey.rule.RateKeyRule;

public class DefaultRateKeyRelationship implements IRateKeyRelationship {

	
	public DefaultRateKeyRelationship() {
		;
	}
	
	@Override
	public boolean validate(Map<String, Integer> keyValues) {
		
		//一時払いの場合、m=0；分割払いの場合、m>0
		if ((keyValues.get("kaisu") == 1 && keyValues.get("m") > 0) ||
				(keyValues.get("kaisu") > 1 && keyValues.get("m") == 0)) return false;
		//m<=n
		if (keyValues.get("m") > keyValues.get("n")) return false; 
			
		return true;
	}
	
	/*
	 *  TODO キーの間の制御でレートキーMAPを作ること
	 *  ただ、制御関係は外部XMLに追定義できるように改善予定
	 *  m<=n,if(kaisu==1){m=0}else{m=rule.max}
	 */
	// 
	@Override
	public int getMinValue(String keyName, RateKeyRule rule, Map<String, Integer> keyValues) {
		if (keyName.equals("n")) {
			//nはmを作成次第作成するため、n>=m
			Integer m = keyValues.get("m");
			if (m == null) {
				return rule.getMin();
			} else {
				return m <= rule.getMin() ? rule.getMin() : m;
			}
			
		}
		
		//分割払の場合、払込期間は0以上で入力ください
		if (keyName.equals("m")) {
			//ローマ字の昇順でソートしたRateKeyDefどおりにキーの値を生成するため、kaisuはmより先に作られた
			Integer kaisu = keyValues.get("kaisu");
			if (kaisu == null) return rule.getMin();
			
			if (kaisu > 1) {
				//分割払
				return rule.getMin() < 1 ? 1 : rule.getMin();
			}
		}
		
		//デフォルト場合
		return rule.getMin();
		
	}
	
	@Override
	public int getMaxValue(String keyName, RateKeyRule rule, Map<String, Integer> keyValues) {
		if (keyName.equals("m")) {
						
			//ローマ字の昇順でソートしたRateKeyDefどおりにキーの値を生成するため、kaisuはmより先に作られた
			Integer kaisu = keyValues.get("kaisu");
			if (kaisu == null) return rule.getMax();
			if (kaisu == 1) {
				//一時払の場合、払込期間は0となければならない
				return 0;
			} 
						
		} 
		
		//デフォルト場合
		return rule.getMax();
		
	}


}
