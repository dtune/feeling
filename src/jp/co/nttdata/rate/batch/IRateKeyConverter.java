package jp.co.nttdata.rate.batch;

import java.util.Map;

/**
 * 
 * ���q�l����f�[�^������ƁA�����o�ߔN�����P���ڂɓZ�߂���A�x���X�e�[�^�X���Q���ڈȏ�Ɋւ������
 * �}�b�s���O���`���邤�������I�ɒu������悤�ɑΉ�����
 * <br>���i���Ƀ}�b�s���O�֌W���Ⴄ��������Ȃ�
 * @author btchoukug
 *
 */
public interface IRateKeyConverter {
	public Map<String, Integer> convert(Map<String, Integer> object);
	public void setFixedValue(Map<String, Integer> fixedValue);
}
