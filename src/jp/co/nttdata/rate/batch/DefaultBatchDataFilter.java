package jp.co.nttdata.rate.batch;

import java.util.Map;

public class DefaultBatchDataFilter implements IBatchDataFilter {

	@Override
	public boolean filter(Map<String, Double> rateKeys) {
		// �S�ă��[�g�L�[�͌v�Z�ɂ���
		return true;
	}

}
