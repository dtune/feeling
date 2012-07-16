package jp.co.nttdata.rate.batch;

import java.util.Map;

public interface IBatchDataFilter {
	/**
	 * �n����郌�[�g�L�[�ɑ΂��āA�o�b�`�Ōv�Z���܂����Ȃ������f����
	 * true�̏ꍇ�A�v�Z���܂��Gfalse�̏ꍇ�A�X�L�b�v���܂��B
	 * @param rateKeys
	 * @return
	 */
	public boolean filter(Map<String, Double> rateKeys);
}
