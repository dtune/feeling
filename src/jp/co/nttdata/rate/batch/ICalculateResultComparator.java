package jp.co.nttdata.rate.batch;

import java.util.Map;

public interface ICalculateResultComparator  {

	/**
	 * �J�����g�v�Z�ɂ������āA��r��Ɣ�r�����ׁA���ۂ��ǂ�����Ԃ�
	 * @param ratekeys
	 * @param result
	 * @return
	 */
	public boolean compare(StringBuffer sbCompareResult, Map<String, Double> rateKeys, Map<String, Double> result);
	/**
	 * ��r�Ώۂ̃}�b�s���O��ݒ�
	 * @param relations
	 */
	public void setCompareMapping(Map<String,String> compareMapping);
	/***
	 * ��r�Ώۂ̃}�b�s���O���A��r���ʂ̃w�b�_���擾
	 * @param sbHeader
	 * @return
	 */
	public StringBuffer appendCompareHeader(StringBuffer sbHeader);
	/**
	 * NG�������擾
	 * @return
	 */
	public long getNgCount();
}
