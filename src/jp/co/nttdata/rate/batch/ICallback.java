package jp.co.nttdata.rate.batch;

import java.util.Map;
import jp.co.nttdata.rate.batch.IBatchWriter;

public interface ICallback {
	/**�Ăь�����R�[������G���g���[*/
	public void execute(Map<String, Integer> keys);
	/**BT�����̃C���v�b�g�f�[�^��ǂލ��݂̂��߁Areader��ݒ�*/
	public void registerReader(IRateKeyReader reader);
	/**BT�����̌v�Z���ʃf�[�^���������݂̂��߁Awriter��ݒ�*/
	public void registerWriter(IBatchWriter writer);
}
