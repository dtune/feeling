package jp.co.nttdata.rate.model.rateKey;

import java.util.Map;

import jp.co.nttdata.rate.model.rateKey.rule.RateKeyRule;

public interface IRateKeyRelationship {

	/*
	 *  TODO �L�[�̊Ԃ̐���Ń��[�g�L�[MAP����邱��
	 *  �����A����֌W�͊O��XML�ɒǒ�`�ł���悤�ɉ��P�\��
	 *  m<=n,if(kaisu==1){m=0}else{m=rule.max}
	 */
	// 
	/**
	 * ���̃L�[�̒l�ƃL�[���[�����ŏ��l��������
	 * ���n��m�̊Ԃ̐���Am<=n
	 * @param keyName 
	 * @param rule
	 * @return
	 */
	public abstract int getMinValue(String keyName, RateKeyRule rule,
			Map<String, Integer> keyValues);

	/**
	 * ���̃L�[�̒l�ƃL�[���[�����ő�l��������
	 * ���n��m�̊Ԃ̐���Am<=n
	 * @param keyName 
	 * @param rule
	 * @return
	 */
	public abstract int getMaxValue(String keyName, RateKeyRule rule,
			Map<String, Integer> keyValues);
	
	public abstract boolean validate(Map<String, Integer> keyValues);

}