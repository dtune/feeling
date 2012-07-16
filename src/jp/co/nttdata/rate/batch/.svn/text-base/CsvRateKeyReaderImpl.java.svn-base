package jp.co.nttdata.rate.batch;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.LineNumberReader;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.lang.StringUtils;

import jp.co.nttdata.rate.batch.dataConvert.BatchDataLayoutFactory;
import jp.co.nttdata.rate.batch.dataConvert.Csv2RateKeyConverterImpl;
import jp.co.nttdata.rate.batch.dataConvert.IRateKeyConverter;
import jp.co.nttdata.rate.exception.FmsDefErrorException;
import jp.co.nttdata.rate.exception.FmsRuntimeException;
import jp.co.nttdata.rate.exception.RateException;
import jp.co.nttdata.rate.util.CommonUtil;
import jp.co.nttdata.rate.util.Const;
import jp.co.nttdata.rate.util.PropertiesUtil;
/**
 * 
 * お客様からインプットデータをCSVファイルに整形したうえ、
 * １行ずつ読み込んでレートキーMapという形で返す
 * @author btchoukug
 *
 */
public class CsvRateKeyReaderImpl implements IRateKeyReader {
	
	public static final char DEFAULT_DELIMITER = ',';
	public static final char EQ = '=';
	public static final char SEMICOLON = ';';	

	private static final String INPUT_RYORITSU_SHIBETSU_CN = "料率識別区分CN";
	
	private static final int RADIX16 = 16;
	
	private String[] keyNames;
	
	private File file;
	private BufferedReader reader;
	
	private long lineCount = 0;
	
	/**デフォルト最大計算可の件数*/
	private long maxlineNumber = Long.MAX_VALUE;
	private long totalLineCount = 0;
	private char delimiter;

	/**新旧レートキーの変換モジュール*/
	private IRateKeyConverter convertor;

	private static Properties prop;
	private static String calculateCategory;
	static {
		prop = PropertiesUtil.getExternalProperties(Const.BT_DATA_CSV_PROPERIT_DIR + Const.BT_DATA_CSV_PROPERIT_FILENAME);
		calculateCategory = prop.getProperty(Const.CAlCULATE_TYPE_KEY, "");
	}
	
	public CsvRateKeyReaderImpl(String filePath) throws FmsDefErrorException {
		this(new File(filePath));
	}
	
	public CsvRateKeyReaderImpl(File file) throws FmsDefErrorException {
		this.file = file;
		_countTotalLineNum();
		this.convertor = new Csv2RateKeyConverterImpl(calculateCategory);
	}
	
	
	private void _countTotalLineNum() {
		try {
			LineNumberReader rf = null;
			rf = new LineNumberReader(new FileReader(file));
			long fileLength = file.length();

			if (rf != null) {
				rf.skip(fileLength);
				//総件数を取得
				this.totalLineCount = rf.getLineNumber();
				rf.close();
			}

			//ヘッダを総件数から引く
			this.totalLineCount --;
						
		} catch (FileNotFoundException e) {
			throw new FmsRuntimeException(e);
		} catch (IOException e) {
			throw new FmsRuntimeException(e);
		}
	}
	
	/**
	 * 固定値を指定する<br>
	 * 指定値はファイルから読み込んだ項目値に上書きする
	 * @param fixedValuesText
	 * @throws RateException 
	 */
	public void setFixedValues(String fixedValuesText) throws RateException {
		
		if (StringUtils.isNotBlank(fixedValuesText)) {
			
			// 特殊値は画面上から指定できる（gen=4;sptate=0の形で）
			Map<String, Double> fixedValues = new HashMap<String, Double>();
			String[] keysets = StringUtils.split(fixedValuesText, SEMICOLON);
			for (String keyset : keysets) {
				String[] keyValue = StringUtils.split(keyset, EQ);
				if (keyValue.length < 2) {
					throw new RateException("レートキーの固定値の指定には問題がある。");
				}
				fixedValues.put(keyValue[0], Double.parseDouble(keyValue[1]));			
			}		
			convertor.setFixedValue(fixedValues);			
		}
	}
	
	/**
	 * 最大取扱行数を設定
	 * @param num
	 */
	@Override
	public void setMaxLineNumber(long num) {
		if (num > 0) {
			this.maxlineNumber = num;
		}		
	}
		
	private char _getDelimiter() {
		if (this.delimiter > 0) {
			return this.delimiter;
		}		
		return DEFAULT_DELIMITER;
	}
	
	/**
	 * 分割記号を設定
	 * @param delimiter
	 */
	public void setDelimiter(char delimiter) {
		//TODO tab,commaなど有効チェックを行う？
		this.delimiter = delimiter;
	}

	@Override
	public Map<String, Double> readRateKeys() throws RateException, IOException {
		
		if (this.lineCount == this.maxlineNumber) return null;
		
		if (this.keyNames == null) {
			this.getKeyNames();
			//throw new RuntimeException("レートキーの名をロードしてからレートキーの値を読み込む。");
		}
				
		Map<String, Double> rateKeyValues = new HashMap<String, Double>();		
						
		//レートキーの値を編集
		String strLine = this.reader.readLine();
		if (strLine == null) return null;
		if (strLine.charAt(strLine.length() - 1) == ',') {
			strLine += Const.SPACE;
		}		
		String[] keyValues = strLine.split(String.valueOf(_getDelimiter()));		
		if (keyValues.length != this.keyNames.length) {
			//レートキー不正の場合、計算しなくて、キーと値のみを出力する
			throw new RateException("レートキーと値の数は違う。" + this.keyNames + ":" + strLine);
		}
		
		for (int i = 0; i < keyValues.length; i++) {
			String val = keyValues[i];
			String keyName = this.keyNames[i];
			if (StringUtils.isNotBlank(val)) {
								
				//料率識別区分の場合、8Aと9Aからの16進法の値は十進法に変換する
				if (keyName.equals(INPUT_RYORITSU_SHIBETSU_CN)) {
					int ret = 0;
					if (val.startsWith("8") || val.startsWith("9")) {
						ret = Integer.parseInt(val, RADIX16);
					} else {
						ret = Integer.parseInt(val);
					}
					rateKeyValues.put(INPUT_RYORITSU_SHIBETSU_CN, ret * 1d);
				} else {
					rateKeyValues.put(keyName, new Double(val));	
				}
					
			} else {
				rateKeyValues.put(keyName, 0d);
			}		
		}			
		this.lineCount ++;
		
		//変換したレートキーを返す
		return this.convertor.convert(rateKeyValues);
		
	}	


	@Override
	public void close() {
		try {
			if (this.reader != null) {
				this.reader.close();
			}			
		} catch (IOException e) {
			throw new RuntimeException(e);
		} finally {
			this.reader = null;
			//TODO クローズすると、問題ある
//			if (this.file != null) {
//				this.file = null;
//			}
		}
		
	}

	public String[] getKeyNames() throws RateException {
		
		try {
			//レートキーの読み込み
			this.reader = new BufferedReader(new FileReader(file));
			String strLine = this.reader.readLine();
			
			if (StringUtils.isEmpty(strLine)) {
				throw new RateException("CSVファイルに導入データが存在していない。");
			}
			
			//レートキーの名を編集
			this.keyNames = StringUtils.split(strLine, _getDelimiter());
			
			//お客様からcsvファイルのヘッダ部には、ある場合レーキーの名前は全角である；ある場合、半角である。
			//比較しやすいため、レートキーの中身に英語の全角から半角へ転換
			for (int i = 0, len = this.keyNames.length; i < len; i++) {
				this.keyNames[i] = CommonUtil.ToDBC(this.keyNames[i]);
			}
			
		} catch (FileNotFoundException e) {
			throw new RateException(e);
		} catch (IOException e) {
			throw new RateException(e);
		}
	
		return this.keyNames;
	}
	
	public File getFile() {
		return this.file;
	}
		
	@Override
	public long getReadLineNum() {
		return this.lineCount;
	}

	public long getTotalLineCount() throws RateException {
		return this.totalLineCount;
	}

	@Override
	public void setCompareObject(String dest) {
		String[] compareObjNames = StringUtils.split(dest, ',');
		this.convertor.setCompareObjectNames(compareObjNames);
	}

	@Override
	public void setLayoutFactory(BatchDataLayoutFactory factory) {
		;
	}
		
}
