package jp.co.nttdata.rate.batch;

import java.util.Map;
import jp.co.nttdata.rate.exception.RateException;

/**
 * それぞれ出力媒体（CSVやEXCELなど）を対応するため、バッチの出力のインタフェース
 * @author btchoukug
 *
 */
public interface IDatFileWriter {
	
	/** 
	 * 複数の計算結果を出力
	 * @param result
	 * @throws RateException
	 */
	public void output(Map<String, Double> result);

	/**レートキーを設定*/
	public void setInput(Map<String, Integer> input);
	
	/**ヘッダのキーの順番を指定（コンマ区切り）*/
	public void setHeader(String header);
	
	/**対象ファイルにすべての内容を書き込んだ後、ファイルをクローズする */
	public void close();

	public String getFilePath();

}
