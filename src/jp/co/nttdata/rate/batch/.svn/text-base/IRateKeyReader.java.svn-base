package jp.co.nttdata.rate.batch;

import java.io.File;
import java.io.IOException;
import java.util.Map;

import jp.co.nttdata.rate.batch.dataConvert.BatchDataLayoutFactory;
import jp.co.nttdata.rate.exception.RateException;

/**
 * 
 * 外部ファイルとかStreamとかからレートキーのデータを読み込んで
 * 計算用Mapの形でバッチに提供する
 * @author btchoukug
 *
 */
public interface IRateKeyReader {
	
	/**
	 * 1行目からレートキーを読み込む（ヘッダの次行）
	 * @return
	 * @throws IOException
	 * @throws RateException
	 */
	public Map<String, Double> readRateKeys() throws IOException, RateException;
	/**
	 * レートキーの名を取得
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
	 * レートキーの他、計算結果の比較元を指定
	 * 複数項目の場合、コンマで区切り
	 * @param dest
	 */
	public void setCompareObject(String dest);
	
	/**
	 * 読み込み先ファイルのレイアウトファクトリーを設定
	 * @param factory
	 */
	public void setLayoutFactory(BatchDataLayoutFactory factory);
	
}
