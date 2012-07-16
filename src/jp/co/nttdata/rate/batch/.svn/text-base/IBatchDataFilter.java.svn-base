package jp.co.nttdata.rate.batch;

import java.util.Map;

public interface IBatchDataFilter {
	/**
	 * 渡されるレートキーに対して、バッチで計算しますかないか判断する
	 * trueの場合、計算します；falseの場合、スキップします。
	 * @param rateKeys
	 * @return
	 */
	public boolean filter(Map<String, Double> rateKeys);
}
