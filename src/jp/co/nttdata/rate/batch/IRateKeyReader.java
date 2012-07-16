package jp.co.nttdata.rate.batch;

import java.io.File;
import java.io.IOException;
import java.util.Map;

import jp.co.nttdata.rate.batch.dataConvert.BatchDataLayoutFactory;
import jp.co.nttdata.rate.exception.RateException;

/**
 * 
 * �O���t�@�C���Ƃ�Stream�Ƃ����烌�[�g�L�[�̃f�[�^��ǂݍ����
 * �v�Z�pMap�̌`�Ńo�b�`�ɒ񋟂���
 * @author btchoukug
 *
 */
public interface IRateKeyReader {
	
	/**
	 * 1�s�ڂ��烌�[�g�L�[��ǂݍ��ށi�w�b�_�̎��s�j
	 * @return
	 * @throws IOException
	 * @throws RateException
	 */
	public Map<String, Double> readRateKeys() throws IOException, RateException;
	/**
	 * ���[�g�L�[�̖����擾
	 * @return
	 */
	public String[] getKeyNames() throws RateException;
	public void setMaxLineNumber(long num);
	public long getTotalLineCount() throws RateException;
	public long getReadLineNum();
	public void close();
	public void setFixedValues(String fixedRateKeyValueText) throws RateException;
	public File getFile();
	/**
	 * ���[�g�L�[�̑��A�v�Z���ʂ̔�r�����w��
	 * �������ڂ̏ꍇ�A�R���}�ŋ�؂�
	 * @param dest
	 */
	public void setCompareObject(String dest);
	
	/**
	 * �ǂݍ��ݐ�t�@�C���̃��C�A�E�g�t�@�N�g���[��ݒ�
	 * @param factory
	 */
	public void setLayoutFactory(BatchDataLayoutFactory factory);
	
}
