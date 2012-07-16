package jp.co.nttdata.rate.batch;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import jp.co.nttdata.rate.batch.dataConvert.BatchDataLayoutFactory;
import jp.co.nttdata.rate.exception.RateException;

/**
 * バッチ計算のインプットデータを取扱するアダプター
 * <p>インプットデータファイル或いはファイルの所属フォルダー、
 * 及び指定された拡張子より、<br>
 * インプットデータをFMSシステムに識別されるレートキーに転換する</p>
 * <ul>現時点では、主に２つ種類のデータファイルを読み込む
 * <li>1.拡張子.datのデータファイル</li>
 * <li>2.拡張子.csvのデータファイル</li>
 * <li>2.拡張子.txtのデータファイル</li>
 * </ul>
 * <p>直下のファイルもサブフォルダー下のファイルも全部一ずつ読みに行く</p>
 * @author btchoukug
 *
 */
public class BatchInputAdapter {

	private static final int DEFAULT_MAX_SIZE = 30;

	public static final String CSV_SUFFIX = ".csv";
	private static final String DAT_SUFFIX = ".dat";	

	private boolean haveNext = true;
	
	/**.datファイルを取扱ため、レイアウト取得ファクトリー*/
	private BatchDataLayoutFactory factory;
	
	/**データファイルを扱うリーダ*/
	private IRateKeyReader reader;
	
	/**フォルダー*/
	private File folder;
	
	/**フォルダーしたの全部ファイル*/
	private List<File> subFileList;
	
	/**カレント取扱ファイルの番号*/
	private int fileCount = 0;
	
	/**最大取扱ファイル数量（0：制限なし）*/
	private int maxFileNum;
	
	/**ファイルフィルター*/
	private BatchInputFileFilter fileFilter;

	/**ファイル種類（拡張子）にチェック可否*/
	private boolean enableSuffixValid = true;
	
	public BatchInputAdapter() {
		this.fileFilter = new BatchInputFileFilter();
	}
	
	/**
	 * 指定の拡張子より、インプットデータアダプターを作成
	 * <br>複数の拡張子を指定可能
	 * @param suffix
	 */
	public BatchInputAdapter(String[] suffix) {
		_construct(suffix, DEFAULT_MAX_SIZE);
	}
	
	public BatchInputAdapter(String[] suffix, int maxFileNum) {
		_construct(suffix, maxFileNum);
	}
	
	private void _construct(String[] suffix, int maxFileNum) {
		
		if (maxFileNum < 0) {
			throw new IllegalArgumentException("パラメータmaxFileNumはゼロ以上してください");
		}
		
		this.fileFilter = new BatchInputFileFilter(suffix);
		if (DAT_SUFFIX.equals(suffix)) {
			this.factory = new BatchDataLayoutFactory();
		}
		this.maxFileNum = maxFileNum;
	}

	/**
	 * ファイルあるいはフォルダ両方とも扱う
	 * @param dataPath
	 */
	public void loadInputData(String dataPath) {
		int size = DEFAULT_MAX_SIZE;
		if (this.maxFileNum > 0) {
			size = this.maxFileNum;
		}
		subFileList = new ArrayList<File>(size);

		this.folder = new File(dataPath);
		if (this.folder.isDirectory()) {	
			_loadSubFolder(this.folder);
		} else {
			subFileList.add(this.folder);
		}
		
	}
	
	public void setFileSuffixValid(boolean enableSuffixValid) {
		this.enableSuffixValid = enableSuffixValid; 
	}
	
	/***
	 * 扱う最大のインプットファイルの数量を設定
	 * @param maxFileNum 
	 */
	public void setMaxInputFileNum(int maxFileNum) {
		this.maxFileNum = maxFileNum;
	}
	
	/**
	 * 再帰ですべてサブフォルダーの下のファイルを読み込む
	 * @param subFolder
	 * @return
	 */
	private void _loadSubFolder(File subFolder) {
		File[] subFiles = subFolder.listFiles(this.fileFilter);
		if (subFiles == null) return;
		for (File subFile : subFiles) {
			if (subFile.isFile()) {
				if (this.maxFileNum > 0 && subFileList.size() >= this.maxFileNum) return;
				subFileList.add(subFile);
			} else {
				_loadSubFolder(subFile);
			}			
		} 
	}
	
	
	/**
	 * カレントのデータファイルに応じて、取扱のファイルリーダを返す
	 * @return
	 * @throws RateException 
	 */
	public IRateKeyReader getRateKeyReader() throws Exception {
		
		File curDataFile = this.subFileList.get(fileCount); 
		this.fileCount ++;
		
		String fileName = curDataFile.getName();
		
		//拡張子よりファイル種別専用のクラスのインスタンスを作成
		if (fileName.endsWith(CSV_SUFFIX)) {
			this.reader = new CsvRateKeyReaderImpl(curDataFile);
		} else {
			//CSV以外は全部datファイルとして扱う
			this.reader = new DatRateKeyReaderImpl(curDataFile, factory);
		}

		return this.reader;
	}
	
	/**
	 * カレントのデータファイルに応じて、取扱のファイルリーダを返す
	 * @return
	 * @throws RateException 
	 */
	public String getFilePath() throws RateException {
		
		File curDataFile = this.subFileList.get(fileCount); 
		this.fileCount ++;
		
		return curDataFile.getAbsolutePath();
//		
//		//拡張子よりファイル種別専用のクラスのインスタンスを作成
//		if (fileName.endsWith(CSV_SUFFIX)) {
//			this.reader = new CsvRateKeyReaderImpl(curDataFile);
//		} else {
//			//CSV以外は全部datファイルとして扱う
//			this.reader = new DatRateKeyReaderImpl(curDataFile, factory);
//		}
//		
////		else {
////			if (this.enableSuffixValid) {
////				throw new RuntimeException("システム上は扱わないデータファイルでした。" + curDataFile);
////			} else {
////				System.out.println("システム上は扱わないデータファイル：" + curDataFile);
////				return null;
////			}
////		}
//			
//		return this.reader;
	}
	
	/**
	 * フォルダーの下に、指定の拡張子のファイルはあるかどうか
	 * @return
	 */
	public boolean haveNextFile() {
		if (this.fileCount >= this.subFileList.size()) {
			this.haveNext = false;
		}
		return this.haveNext;
	}
	
	public static void main(String[] args) {
		
		long t1 = System.currentTimeMillis();
		
		BatchInputAdapter adapter = new BatchInputAdapter(new String[]{".dat",".csv"});
		adapter.setMaxInputFileNum(0);
		adapter.setFileSuffixValid(false);
		adapter.loadInputData("C:\\Documents and Settings\\btchoukug\\デスクトップ\\BT検証作業\\お客様からデータ030\\Vrate\\払込中");
		
		try {			

			while (adapter.haveNextFile()) {
				IRateKeyReader reader = adapter.getRateKeyReader();
				if (reader != null) {
					System.out.println(reader.getFile());
				}				
			}
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		long t2 = System.currentTimeMillis();
		System.out.println((t2-t1)+"ms");
		
	}

	/**
	 * カレント取扱ファイルの番号
	 * @return
	 */
	public int getCurrentFileNum() {
		return this.fileCount;
	}

	public int getTotalFileCount() {
		return subFileList.size();
	}
	
}
