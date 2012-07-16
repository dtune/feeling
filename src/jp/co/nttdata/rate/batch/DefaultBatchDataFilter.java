package jp.co.nttdata.rate.batch;

import java.util.Map;

public class DefaultBatchDataFilter implements IBatchDataFilter {

	@Override
	public boolean filter(Map<String, Double> rateKeys) {
		// 全てレートキーは計算可にする
		return true;
	}

}
