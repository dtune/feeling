package jp.co.nttdata.rate.model.rateKey;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import jp.co.nttdata.rate.batch.ICallback;
import jp.co.nttdata.rate.exception.RateException;
import jp.co.nttdata.rate.model.rateKey.rule.RateKeyRule;

/**
 * �o�b�`�v�Z�̏ꍇ�A���[����背�[�g�L�[�𐶐����Čv�Z���s���W�F�l���[�^�ł���
 * 
 * @author btchoukug
 */
public class RateKeyGenerator {

	/** �������[�g�L�[�̏�� */
	private long maxNum = 99999999;
	/** �������ꂽ�L�[�̐��� */
	private long generatedKeyNum = 0l;

	/** BT�v�Z�̏ꍇ�A�����I�ɐ��������L�[�̒l���i�[���� */
	private Map<String, Integer> dataLayoutValues;

	/** ���[�g�L�[��`�̃��X�g */
	private List<RateKey> dataLayoutDefs;

	private int rateKeyNum = 0;

	private boolean isStop;

	private IRateKeyRelationship rateKeyRelation;

	/***
	 * ���͂�RateInput�̃N���X����у��[�g�L�[�̃o���f�[�V�����Ń��[�g�L�[�̃W�F�l���[�^�[������������
	 * 
	 * @param c
	 * @param insurance
	 * @param validator
	 * @throws RateException
	 */
	public RateKeyGenerator() {
		
		this.dataLayoutValues = new HashMap<String, Integer>();
		// TODO �������[�g�L�[�̊Ԃ͉�������֌W���ς������A���L�̃N���X������������
		this.rateKeyRelation = new DefaultRateKeyRelationship();
		
	}

	/***
	 * �J��Ԃ��Ń��[�g�L�[���쐬����
	 * 
	 * @param callback
	 */
	public void generateKeyValue(ICallback callback) {
		int i;
		int keyNum = this.dataLayoutDefs.size();

		Map<String, Integer> values = new HashMap<String, Integer>();

		// ���ׂẴL�[���ŏ��l�ŏ���������
		for (i = 0; i < keyNum; i++) {
			// �J�����g���[�g�L�[���ƃ��[�����擾
			RateKey key = this.dataLayoutDefs.get(i);
			String keyName = key.getName();
//			RateKeyRule rule = RateKeyManager.getRateKeyRule(keyName);
			RateKeyRule rule = key.getRule();

			// �f�t�H���g�ꍇ�A0�Ƃ���
			int value = 0;

			// ���[�g�L�[�̏�����
			if (rule != null) {
				// ���[���w�肵�Ă���ꍇ�A�ŏ��l�Ƃ���
				// value = rule.getMin();
				value = this.rateKeyRelation.getMinValue(keyName, rule, values);
			}
			values.put(keyName, value);
		}

		while (this.generatedKeyNum < this.maxNum && this.maxNum > 0) {

			// �������~���ǂ�������
			if (this.isStop) return;

			//�L���ȃ��[�g�L�[�̑g�ݍ��킹�ł���΁Adat�t�@�C���ɏ�������
			if (this.rateKeyRelation.validate(values)) {
				//System.out.println(values);
				callback.execute(values);
				generatedKeyNum++;				
			}

			// ��L�ɏ]���āA�Ō�̃L�[����t�ɌJ��Ԃ��ă��[�g�L�[�̒l��ݒ肷��
			for (i = keyNum - 1; i >= 0; i--) {
				// �ő�l��ҏW
				RateKey key = this.dataLayoutDefs.get(i);
				String keyName = key.getName();
//				RateKeyRule rule = RateKeyManager.getRateKeyRule(keyName);
				RateKeyRule rule = key.getRule();
				
				int max = this.rateKeyRelation.getMaxValue(keyName, rule, values);
				int step = rule.getStep();
				
				//�J�����g�L�[�ɃX�e�b�v���v���X
				int value = values.get(keyName) + step;
				
				// �J�����g�L�[�͍ő�l�ɂȂ�ƁA�ŏ��l�ɖ߂�
				if (value > max) {
					values.put(keyName, this.rateKeyRelation.getMinValue(
							keyName, rule, values));
				} else {
					//����l�̏ꍇ�A�X�e�b�v�v���X�œ���l���X�L�b�v���܂�
					while (RateKeyValidator.isSpecialValue(value, rule.getSpecialValues())) {
						value += step;
					}
					
					values.put(keyName, value);
					break;
				}
			}

			// ��Ԗڂ̃��[�g�ɂȂ�ƁAwhile���[�v�𒆎~����
			if (i < 0) break;

		}
	}

	/**
	 * �ċA�Ń��[�g�L�[�𐶐����� <br>
	 * ���ʂȃL�[�̑g�ݍ��킹�𐶐����Ȃ��悤�ɍŏI�I�Ɉꊇ���؂���ł͂Ȃ��A <br>
	 * �����̊ԂɃL�[����`�F�b�N���s��
	 * 
	 * @param index
	 * @param callback
	 */
	public void generateKeyValue(int index, ICallback callback) {

		if (this.generatedKeyNum >= this.maxNum && this.maxNum > 0) {
			// �ċA�Ń��[�g�L�[�𐶐����I���ɂ���
			return;
		}

		// �J�����g���[�g�L�[���ƃ��[�����擾
		RateKey key = this.dataLayoutDefs.get(index);
		String keyName = key.getName();
		RateKeyRule rule = key.getRule();

		// �f�t�H���g�ꍇ�A0�Ƃ���
		int value = 0;

		// ���[�g�L�[�̏�����
		if (rule != null) {
			// ���[���w�肵�Ă���ꍇ�A�ŏ��l�Ƃ���
			// value = rule.getMin();
			value = this.rateKeyRelation.getMinValue(keyName, rule,
					this.dataLayoutValues);
		}

		// �֘A�̃��[�g�L�[�̒l���n�l��ҏW
		int max = this.rateKeyRelation.getMaxValue(keyName, rule,
				this.dataLayoutValues);

		// �����l����UMap�ɕۑ�����
		this.dataLayoutValues.put(keyName, value);

		// �ő�l�܂ŌJ��Ԃ��ă��[�g�L�[�̒l�𐶐�����
		if (rule != null) {
			while (value <= max) {
				// ����l�`�F�b�N
				if (!RateKeyValidator.isSpecialValue(value, rule
						.getSpecialValues())) {
					// �����l�ł͂Ȃ��ꍇ�A�ċA�Ŏ��̃��[�g�L�[�̒l�𐶐�����
					if (index < this.rateKeyNum - 1) {
						generateKeyValue(index + 1, callback);
					} else {
						// �Ō�̍��ڂƂȂ�ƁA�Ɩ���̃o���f�[�V�����Ō��؂���
						// OK�Ȃ�A�R�[���o�b�N�̃��\�b�h���Ăяo��
						callback.execute(dataLayoutValues);
						this.generatedKeyNum++;
					}
				}

				// �X�e�b�v�l���v���X���āA���[�g�L�[Map�ɕۑ�����
				value = value + rule.getStep();
				this.dataLayoutValues.put(keyName, value);
			}
		} else {
			if (index < this.rateKeyNum - 1) {
				generateKeyValue(index + 1, callback);
			} else {
				// �Ō�̍��ڂƂȂ�ƁA�Ɩ���̃o���f�[�V�����Ō��؂���
				// OK�Ȃ�A�R�[���o�b�N���\�b�h���Ăяo��
				callback.execute(dataLayoutValues);
				this.generatedKeyNum++;
			}
		}

	}

	/**
	 * �Q�ƌ��̃��[�g�L�[�̒�`��ݒ肷��
	 * 
	 * @param rateKeyDefs
	 */
	public void setRateKeys(List<RateKey> rateKeyDefs) {
		this.dataLayoutDefs = rateKeyDefs;
		// ���[�}���̏����Ń\�[�g����RateKeyDef�ǂ���ɃL�[�̒l�𐶐�����
		Collections.sort(this.dataLayoutDefs);
		this.rateKeyNum = rateKeyDefs.size();
	}

	/**
	 * �����I�ɐ������ꂽ�L�[�̐��̏����ݒ�
	 * 
	 * @param maxNum
	 */
	public void setMaxNum(long maxNum) {
		this.maxNum = maxNum;
	}

	/** �����I�ɐ������ꂽ�L�[�̐����擾 */
	public long getGeneratedKeyNum() {
		return this.generatedKeyNum;
	}

	public void stop() {
		this.isStop = true;
	}
	
	public void start() {
		this.isStop = false;
	}

}
