package jp.co.nttdata.rate.model.rateKey;

import java.util.Comparator;

/**
 * ���[�g�L�[�͕\�������\�[�g����
 * @author btchoukug
 *
 */
public class RateKeySorter implements Comparator<RateKey> {

	@Override
	public int compare(RateKey o1, RateKey o2) {
		return o1.getDisplayOrder() > o2.getDisplayOrder() ? 1 : 0;
	}

}
