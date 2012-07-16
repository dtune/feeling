package jp.co.nttdata.rate.batch;

import java.util.Map;
import jp.co.nttdata.rate.batch.IBatchWriter;

public interface ICallback {
	/**呼び元からコールするエントリー*/
	public void execute(Map<String, Integer> keys);
	/**BT処理のインプットデータを読む込みのため、readerを設定*/
	public void registerReader(IRateKeyReader reader);
	/**BT処理の計算結果データを書き込みのため、writerを設定*/
	public void registerWriter(IBatchWriter writer);
}
