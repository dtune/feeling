package jp.co.nttdata.rate.batch;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.HashMap;
import java.util.Map;
import org.apache.commons.lang.StringUtils;
import jp.co.nttdata.rate.batch.dataConvert.BatchDataLayout;
import jp.co.nttdata.rate.batch.dataConvert.BatchDataLayoutFactory;
import jp.co.nttdata.rate.batch.dataConvert.Dat2RateKeyConverterImpl;
import jp.co.nttdata.rate.batch.dataConvert.IRateKeyConverter;
import jp.co.nttdata.rate.batch.dataConvert.RateKeyLayout;
import jp.co.nttdata.rate.exception.RateException;
import jp.co.nttdata.rate.util.Const;

/**
 * 
 * お客様からインプットデータdatファイル １行ずつ読みながら、
 * データレイアウトの通り、レートキーMapに変換したから返す
 * @author btchoukug
 *
 */
public class DatRateKeyReaderImpl implements IRateKeyReader {
	
	public static final char EQ = '=';
	public static final char SEMICOLON = ';';
	
	private String[] keyNames;	
	private File file;
	private BufferedReader reader;
	
	/**デフォルト最大計算可の件数*/
	private long maxlineNumber = Long.MAX_VALUE;
	private long totalLineCount = 0;
	
	private long lineCount = 0;
	private long errorLineCount = 0;

	/**新旧レートキーの変換モジュール*/
	protected IRateKeyConverter converter;
	
	/**データレイアウトFactory*/
	protected BatchDataLayoutFactory factory;
	protected BatchDataLayout dataLayout;
	protected BatchDataLayout errorDataLayout;
	
	//サブモジュール復帰CN
	public static final String SUBMODULEKBN = "subModuleKbn";
	public static final String MODULEID = "moduleID";
	
	public DatRateKeyReaderImpl(String filePath) throws RateException {
		this.file = new File(filePath);
		_construct();
		
	}

	public DatRateKeyReaderImpl(File file) throws RateException {
		this.file = file;
		_construct();
	}

	public DatRateKeyReaderImpl(File curDataFile,
			BatchDataLayoutFactory factory) throws RateException {
		this.file = curDataFile;
		this.factory = factory;
		_construct();
	}

	private void _construct() throws RateException {
		 converter = new Dat2RateKeyConverterImpl();
		 if (this.factory == null) {
			 //外部から設定しない場合、初期化する
			 factory = new BatchDataLayoutFactory();	 
		 }
		 _countTotalLineNum();
	}
	
	@Override
	public void setLayoutFactory(BatchDataLayoutFactory factory) {
		this.factory = factory;
	}
	
	public void loadDataLayout(String code, String cate) {
		dataLayout =  factory.getDataLayout(code, cate);
		errorDataLayout = factory.getErrorDataLayout(cate);
	}
	
	public void setDataLayout(BatchDataLayout dataLayout) {
		this.dataLayout = dataLayout; 
	}
	
	public void setErrorDataLayout(BatchDataLayout errorDataLayout) {
		this.errorDataLayout = errorDataLayout;
	}
	
	private void _countTotalLineNum() throws RateException {
		
		try {
			RandomAccessFile raf = new RandomAccessFile(file, "r");
			long totalLen = raf.length();
			
			//データ単位のサイズを取得(1レコード)
			int unitSize = raf.readLine().getBytes().length + 2;	//一行の内容＋改行符号('\n')2桁
			//ファイルサイズはレコードの単位サイズを割って行数を算出する
			this.totalLineCount = totalLen/unitSize;
						
			this.reader = new BufferedReader(new FileReader(file));
		} catch (FileNotFoundException e) {
			throw new RateException(e);
		} catch (IOException e) {
			throw new RateException(e);
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
			converter.setFixedValue(fixedValues);			
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

	@Override
	public Map<String, Double> readRateKeys() throws RateException, IOException {
		
		// 最大行数に満たす場合、終わりとしてnullを返す
		if (this.lineCount == this.maxlineNumber) return null;
				
		Map<String, Double> rateKeyValues = new HashMap<String, Double>();
		
		// サブモジュール復帰CN,発生元モジュールID
		String subModuleKbn = null; 
		String moduleID = null;
						
		// 行単位でdatファイルから読み込む
		String strLine = this.reader.readLine();
		if (strLine == null) return null;
		
		// データレイアウトの開始位置とレングスより、キーを１ずつ読む		
		char[] lineChars = strLine.toCharArray();
		for (RateKeyLayout keyLayout : dataLayout.getLayoutData()) {
			
			// ☆レイアウトは１から始まる☆
			int start = keyLayout.getPos() - 1;
			int len = keyLayout.getLen();
			char[] data = new char[len];
			int i = 0;
			while (i < len) {
				data[i] = lineChars[start + i];
				i++;
			}
			String val = String.valueOf(data);
			
			rateKeyValues.put(keyLayout.getName(), this.converter.convertRateKey(keyLayout, val));
		}
		
		this.lineCount ++;
		
		// エラーデータの場合、エラーデータの数をカウントする
		for (RateKeyLayout keyLayout : errorDataLayout.getLayoutData()) {
			
			// ☆レイアウトは１から始まる☆
			int start = keyLayout.getPos() - 1;
			int len = keyLayout.getLen();
			char[] data = new char[len];
			int i = 0;
			while (i < len) {
				data[i] = lineChars[start + i];
				i++;
			}
			String val = String.valueOf(data);
			
			if(keyLayout.getName().equals(SUBMODULEKBN)) {
				subModuleKbn = val;
			}else if(keyLayout.getName().equals(MODULEID)) {
				moduleID = val;
			}
		}
		
		if(subModuleKbn != null && !subModuleKbn.equals("0000") && StringUtils.isNotBlank(moduleID)) {
			rateKeyValues.put(Const.ERRORDATAFLAG, 0d);
			this.errorLineCount ++;
		} else {
			rateKeyValues.put(Const.ERRORDATAFLAG, 1d);
		}
		
		// 元データの行番号を記入
		//rateKeyValues.put(LINE_NO, (Integer) ConvertUtils.convert(this.lineCount, Integer.class));
		
		// 関連変換処理したレートキー値を返す
		return this.converter.convert(rateKeyValues);
		
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
//			if (this.file != null) {
//				this.file = null;
//			}
		}
		
	}
	
	public File getFile() {
		return this.file;
	}	

	@Override
	/**
	 * BT画面比較項目選択用
	 */
	public String[] getKeyNames() throws RateException {
		this.keyNames = this.dataLayout.getKeyNames();
		return this.keyNames;
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
		String[] compareObjNames = StringUtils.split(dest, Const.COMMA);
		this.converter.setCompareObjectNames(compareObjNames);
	}
		
}
