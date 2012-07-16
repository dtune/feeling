package jp.co.nttdata.rate.model.rateKey;

import java.util.Map;

import jp.co.nttdata.rate.model.rateKey.rule.RateKeyRule;

public class DefaultRateKeyRelationship implements IRateKeyRelationship {

	
	public DefaultRateKeyRelationship() {
		;
	}
	
	@Override
	public boolean validate(Map<String, Integer> keyValues) {
		
		//�ꎞ�����̏ꍇ�Am=0�G���������̏ꍇ�Am>0
		if ((keyValues.get("kaisu") == 1 && keyValues.get("m") > 0) ||
				(keyValues.get("kaisu") > 1 && keyValues.get("m") == 0)) return false;
		//m<=n
		if (keyValues.get("m") > keyValues.get("n")) return false; 
			
		return true;
	}
	
	/*
	 *  TODO �L�[�̊Ԃ̐���Ń��[�g�L�[MAP����邱��
	 *  �����A����֌W�͊O��XML�ɒǒ�`�ł���悤�ɉ��P�\��
	 *  m<=n,if(kaisu==1){m=0}else{m=rule.max}
	 */
	// 
	@Override
	public int getMinValue(String keyName, RateKeyRule rule, Map<String, Integer> keyValues) {
		if (keyName.equals("n")) {
			//n��m���쐬����쐬���邽�߁An>=m
			Integer m = keyValues.get("m");
			if (m == null) {
				return rule.getMin();
			} else {
				return m <= rule.getMin() ? rule.getMin() : m;
			}
			
		}
		
		//�������̏ꍇ�A�������Ԃ�0�ȏ�œ��͂�������
		if (keyName.equals("m")) {
			//���[�}���̏����Ń\�[�g����RateKeyDef�ǂ���ɃL�[�̒l�𐶐����邽�߁Akaisu��m����ɍ��ꂽ
			Integer kaisu = keyValues.get("kaisu");
			if (kaisu == null) return rule.getMin();
			
			if (kaisu > 1) {
				//������
				return rule.getMin() < 1 ? 1 : rule.getMin();
			}
		}
		
		//�f�t�H���g�ꍇ
		return rule.getMin();
		
	}
	
	@Override
	public int getMaxValue(String keyName, RateKeyRule rule, Map<String, Integer> keyValues) {
		if (keyName.equals("m")) {
						
			//���[�}���̏����Ń\�[�g����RateKeyDef�ǂ���ɃL�[�̒l�𐶐����邽�߁Akaisu��m����ɍ��ꂽ
			Integer kaisu = keyValues.get("kaisu");
			if (kaisu == null) return rule.getMax();
			if (kaisu == 1) {
				//�ꎞ���̏ꍇ�A�������Ԃ�0�ƂȂ���΂Ȃ�Ȃ�
				return 0;
			} 
						
		} 
		
		//�f�t�H���g�ꍇ
		return rule.getMax();
		
	}


}
