package jp.co.nttdata.rate.batch;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.LinkedBlockingQueue;
import jp.co.nttdata.rate.exception.RateException;
import jp.co.nttdata.rate.model.CategoryManager;
import jp.co.nttdata.rate.model.rateKey.RateKey;
import jp.co.nttdata.rate.model.rateKey.RateKeyManager;
import jp.co.nttdata.rate.util.Const;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.time.DateFormatUtils;

/**
 * 
 * 計算のレートキーと計算結果を編集してCSVファイルに出力する 
 * @author btchoukug
 * 
 */
public class CsvFileWriterImpl implements IBatchWriter {
	
	/** CSVファイルの拡張子 */
	protected final String CSV_SUFFIX = ".csv";
	
	private static final int TASK_SIZE = 4096;
	
	public static LinkedBlockingQueue<BatchTask> taskQueue = new LinkedBlockingQueue<BatchTask>(TASK_SIZE);
	
	/**csvファイルの項目名ヘッダ*/
	protected String header;
	protected String errorHeader;

	/** 出力順番でキーと計算式の名を格納 */
	protected String[] headerArray;
	protected Map<String, Double> rateKeys;

	// TODO パスはUIから指定する　＋　商品コードをサブパスとする
	private String resultBasePath = "batch\\output\\";

	protected static final int CONTENT_SIZE = 128;
	
	/** 日付のフォーマット */
	protected static final String YYYYMMDD = "yyyyMMdd";
	/** コンマ */
	private static final char COMMA = ',';
	/** 行終了符号 */
	private static final String EOR = "\r\n";
	
	private boolean NG = true;
	private boolean COMMON = false;

	/**エクセルシート単位で扱える最大件数で出力ファイルを分割*/
	private static final int EXCEL_SHEET_MAXLINE = 65000;

	private static final char CHAR_COMMA = ',';

	private static final String NG_PREFIX = "NG_";

	private static final String UNDERBAR = "_";

	private static final String LINE_NO_HEADER = "No.";

	protected String ngFileName;
	protected String fileName;
	protected String errorFileName;
	protected String filePath;
	protected File file;
	protected FileWriter ngFileWriter;
	protected FileWriter fileWriter;
	protected FileWriter errorFileWriter;
	
	protected String ngFileWriterName;
	protected String fileWriterName;
	
	/**元のファイルに上書き*/
	protected boolean appendEnable = false;
	/**６万件を上限としてファイルを作るか*/
	protected boolean splitFileEnable = true;
	
	protected String fileSuffix;

	/** 行数 */
	protected long lineCount = 0;
	protected long NGlineCount = 0;
	protected long errorLineCount = 0;
	protected long rightLineCount = 0;
	/** ファイル数 */
	protected int ngFileCount = 0;
	protected int fileCount = 0;
	protected int errorFileCount = 0;
	
	private String outputFileName;
	
	private List<String> listKeyName;
	
	/**計算結果比較IF*/
	protected ICalculateResultComparator comparator;
	
	/**出力モード（NGケースのみ出力か）*/
	protected boolean NGOnly = false;

	/**TODO 外部で商品コードを持ち*/
	protected String code; 
	
	public CsvFileWriterImpl(String code, File inputFile,
			Map<String,String> compareObjectMapping, boolean ngOnly, String cateName, String dest) throws RateException {
		
		String path = inputFile.getAbsolutePath();
		setResultBasePath(path.substring(0,path.lastIndexOf(File.separator) + 1));
		
		this.NGOnly = ngOnly;
		
		_init(code.substring(0, 3), inputFile.getName(), compareObjectMapping, cateName, dest);
	}
	
	public CsvFileWriterImpl(String code, String fileName,
			Map<String,String> compareObjectMapping, boolean ngOnly, String cateName, String dest) throws RateException {
		if (StringUtils.isBlank(fileName)) {
			throw new IllegalArgumentException("ファイル名を指定ください。");
		}
		
		this.NGOnly = ngOnly;
		
		_init(code.substring(0, 3), fileName, compareObjectMapping, cateName, dest);
		
	}
	
	private void _init(String code, String fileName, Map<String, String> compareObjectMapping, String cateName, String dest) throws RateException {
		
		this.comparator = new DefaultCalculateResultComparator(); 
		this.comparator.setCompareMapping(compareObjectMapping);
		
		if (StringUtils.isEmpty(code)) {
			throw new IllegalArgumentException("商品コードを指定ください。");
		}
		this.code = code.substring(0, 3);
		
		listKeyName = new ArrayList<String>();
		
		//　UI設定どおり使われるレートキーを取得し、出力対象とする
		for(RateKey rateKey : CategoryManager.getInstance().getCateInfo(cateName).getKeyList()){
			listKeyName.add(rateKey.getName());
		}
		//　比較対象も出力対象とする
		for(String keyName : StringUtils.split(dest, CHAR_COMMA)) {
			listKeyName.add(keyName);
		}
		
		//出力NGファイル名はインプットファイル名＋日付とする（インプットファイルの拡張子を外し）			
		outputFileName = fileName.substring(0,fileName.lastIndexOf(Const.DOT))
			+ UNDERBAR +DateFormatUtils.format(new Date(System.currentTimeMillis()), YYYYMMDD);
		
		ngFileWriterName = NG_PREFIX + outputFileName;
		if(!this.NGOnly) {
			fileWriterName = fileName;
		}
	}
	
	public CsvFileWriterImpl(String code, String category, boolean ngOnly) throws RateException {
		if (code == null || StringUtils.isEmpty(code)) {
			throw new IllegalArgumentException("商品コードを指定ください。");
		}

		if (category == null || StringUtils.isEmpty(category)) {
			throw new IllegalArgumentException("計算カテゴリ（PVW）を指定ください。");
		}
		
		this.NGOnly = ngOnly;
		
		// 商品コードごとにサブフォルダを作成して、計算結果を出力する
		this.filePath = resultBasePath + File.separator + code.substring(0, 3);
		
		// ファイル名は計算カテゴリ＋日付とする
		String fileName = filePath
				+ File.separator
				+ category
				+ DateFormatUtils.format(new Date(System
						.currentTimeMillis()), YYYYMMDD);

		ngFileWriterName = NG_PREFIX + outputFileName;
		
		if(!this.NGOnly) {
			fileWriterName = fileName;
		}
	}

	private FileWriter _createErrorFile() throws IOException {
		
		String errorFileName = "ErrorData" + "_" + this.outputFileName;
		
		// 複数ファイルの場合、元のファイル名に順番を付け
		if (this.errorFileCount > 0) {
			errorFileName = errorFileName + "_" + this.errorFileCount;
		}
		
		// ファイルが存在しない場合、ファイルを作成する；存在している場合、元のファイルを上書き
		file = new File(_getResultBasePath() + errorFileName + _getFileSuffix());
		
		if (!file.exists()) {
			
			File dir = new File(_getResultBasePath());
			if (!dir.exists() && !dir.isDirectory()) {
				dir.mkdirs();
			}
			file.createNewFile();
		}

		return new FileWriter(file, this.appendEnable);// デフォルト場合追加モードではなく
		
	}
	
	private FileWriter _createFile(String fileName, boolean ngOnly) throws IOException {
		
		if(ngOnly) {
			//複数ファイルを生成するため、一旦保存する
			this.ngFileName = fileName;
			
			//複数ファイルの場合、元のファイル名に順番を付け
			if (this.ngFileCount > 0) {
				fileName = fileName + "_" + this.ngFileCount;
			}
		} else {
			//複数ファイルを生成するため、一旦保存する
			this.fileName = fileName;
			
			//複数ファイルの場合、元のファイル名に順番を付け
			if (this.fileCount > 0) {
				fileName = fileName + "_" + this.fileCount;
			}
		}
		
		// ファイルが存在しない場合、ファイルを作成する；存在している場合、元のファイルを上書き
		file = new File(_getResultBasePath() + fileName + _getFileSuffix());
		
		if (!file.exists()) {
			
			File dir = new File(_getResultBasePath());
			if (!dir.exists() && !dir.isDirectory()) {
				dir.mkdirs();
			}
			file.createNewFile();
		}

		return new FileWriter(file, this.appendEnable);// デフォルト場合追加モードではなく
		
		
	}

	/**
	 * パスは商品コードをサブパスとする
	 * @return
	 */
	private String _getResultBasePath() {
		return resultBasePath + this.code + Const.SEPARATOR;
	}

	/**
	 * ファイルの拡張子を取得<br>
	 * デフォルト場合、.csvを返す
	 * @return
	 */
	private String _getFileSuffix() {
		return this.fileSuffix == null ? CSV_SUFFIX : this.fileSuffix;
	}

	/***
	 * 対象ファイルにすべての内容を書きこんだ後、ファイルをクローズする
	 */
	@Override
	public void close(String type) {
		try {
			if(type.equals("NG")) {
				if (ngFileWriter != null)
					ngFileWriter.close();
			} else if(type.equals("ERROR")) {
				if (errorFileWriter != null)
					errorFileWriter.close();
			} else {
				if (fileWriter != null)
					fileWriter.close();
			}
			
		} catch (IOException e) {
			throw new RuntimeException("ファイルをクローズするときにエラーが発生しまいました。ご確認ください。", e);
		} finally {
			if(type.equals("NG")) {
				ngFileWriter = null;
			} else if(type.equals("ERROR")) {
				errorFileWriter = null;
			} else {
				fileWriter = null;
			}
			
		}
	}
	
	/***
	 * 対象ファイルにすべての内容を書きこんだ後、ファイルをクローズする
	 */
	@Override
	public void close() {
		close("NG");
		close("ERROR");
		close("COMMON");
	}
	
	/**
	 * NGのみ出力と指定する
	 * @param NGOnly
	 */
	public void enableNGOnly(boolean NGOnly) {
		this.NGOnly = NGOnly;
	}

	@Override
	public void output(Map<String, Double> result, long taskNo) {
		// カウンターに１をプラス
		this.lineCount++;

		try {
			
			boolean errorFlag = false;
			
			if (this.rateKeys.containsKey(Const.ERRORDATAFLAG)) {
				errorFlag = this.rateKeys.get(Const.ERRORDATAFLAG) == 0 ? true : false;
			}
			
			this.rateKeys.remove(Const.ERRORDATAFLAG);
			
			// レートキーを編集
			StringBuffer sbContent = new StringBuffer(CONTENT_SIZE);
			if (this.headerArray != null) {
				// ヘッダ指定の場合、指定順番どおりに項目値を取得
				for (String key : this.headerArray) {
					sbContent.append(Const.COMMA).append(this.rateKeys.get(key));
				}
			} else {
				// レートキーデフォルト順番で出力
				for (String key : this.rateKeys.keySet()) {
					if (listKeyName.contains(key)) {
						sbContent.append(Const.COMMA).append(this.rateKeys.get(key));
					}					
				}
				
			}
			
			if(!errorFlag) {
				
				this.rightLineCount++;
				
				// 計算結果の項目名を追加
				if (this.rightLineCount == 1) {
					StringBuffer sbHeader = new StringBuffer(this.header);
					for (String calObjName : result.keySet()) {
						sbHeader.append(Const.COMMA).append(calObjName);
					}
					this.header = this.comparator.appendCompareHeader(sbHeader).toString();
				}
				
				// 計算した結果を編集
				for (Double value : result.values()) {
					sbContent.append(Const.COMMA).append(value);
				}
				
				// 比較元と比較先は画面から指定できる
				boolean isOK = this.comparator.compare(sbContent, rateKeys, result);
				
				// 元のデータの行番号を先頭に挿入する
				sbContent.insert(0, taskNo).append(EOR).toString();
				
				if(!isOK) {
					this.NGlineCount++;
					
					// 初回の場合、ヘッダを書き込み
					if (this.NGlineCount % EXCEL_SHEET_MAXLINE == 1) {
						this.ngFileWriter = _createFile(ngFileWriterName, NG);
						this.ngFileWriter.write(this.header + EOR);
					}
					
					// NGのみ出力を指定すると、比較結果がNGの場合、デフォルトとしてCSVファイルに出力
					this.ngFileWriter.write(sbContent.toString());
				}
				
				if(!this.NGOnly){
						// 初回の場合、ヘッダを書き込み
						if (this.rightLineCount % EXCEL_SHEET_MAXLINE == 1) {
							this.ngFileWriter = _createFile(fileWriterName, COMMON);
							this.fileWriter.write(this.header + EOR);
						}
						
						// NGのみ出力を指定すると、比較結果がNGの場合、デフォルトとしてCSVファイルに出力
						this.fileWriter.write(sbContent.toString());
					
				}
				
				// エクセルでみえるように、6万件毎にファイルを分割する
				if (this.NGlineCount % EXCEL_SHEET_MAXLINE == 0 && this.NGlineCount > 0) {
					//まず、カレントファイルをクローズ
					this.close("NG");
					
					//そして行数クリア
					//this.lineCount = 0;

					//ファイル数をプラスして新たにfileWriterを新規
					this.ngFileCount++;
					this.ngFileWriter = _createFile(this.ngFileName, NG);
				}
				
				// エクセルでみえるように、6万件毎にファイルを分割する
				if (!this.NGOnly && this.rightLineCount % EXCEL_SHEET_MAXLINE == 0 && this.rightLineCount > 0) {
					//まず、カレントファイルをクローズ
					this.close("COMMON");
					
					//そして行数クリア
					//this.lineCount = 0;

					//ファイル数をプラスして新たにfileWriterを新規
					this.fileCount++;
					this.fileWriter = _createFile(this.fileName, COMMON);
				}
			} else {
				
				this.errorLineCount++;
				
				if(this.errorFileWriter == null) {
					errorFileWriter = _createErrorFile();
				}
				
				// 初回の場合、ヘッダを書き込み
				if (this.errorLineCount % EXCEL_SHEET_MAXLINE == 1) {
					// TODO エラーデータの場合、エラーデータの行番目を出力
					if (this.errorLineCount == 1) {
						this.errorHeader += Const.COMMA + "エラーデータ場所（行）";					
					}

					this.errorFileWriter.write(this.errorHeader + EOR);
				}
				
				sbContent.append(Const.COMMA).append(this.lineCount);
				
				sbContent.deleteCharAt(0).append(EOR).toString();
				
				// NGのみ出力を指定すると、比較結果がNGの場合、デフォルトとしてCSVファイルに出力
				this.errorFileWriter.write(sbContent.toString());
				
				// エクセルでみえるように、6万件毎にファイルを分割する
				if (this.errorLineCount % EXCEL_SHEET_MAXLINE == 0) {
					//まず、カレントファイルをクローズ
					this.close("ERROR");
					
					//そして行数クリア
					//this.lineCount = 0;

					//ファイル数をプラスして新たにfileWriterを新規
					this.errorFileCount++;
					this.errorFileWriter = _createErrorFile();
				}
				
			}

		} catch (IOException e) {
			throw new RuntimeException(e);
		}

	}

	/**
	 * 入力のレートキーをセット
	 */
	public void setInput(Map<String, Double> input) {
		this.rateKeys = input;
		
		// ヘッダは指定していない場合、レートキーの名よりヘッダを編集
		if (StringUtils.isEmpty(this.header)) {
						
			StringBuffer sb = new StringBuffer(CONTENT_SIZE);
			for (String key : this.rateKeys.keySet()) {
				
				if(listKeyName.contains(key)) {
					RateKey rateKey = RateKeyManager.getRateKeyDef(key);
					if(rateKey != null) {
						// レートキーの場合、日本語名とともに出力する
						sb.append(Const.COMMA).append(rateKey.getLabel() + "(" + key + ")");
					} else {
						// レートキーではない場合、例えPレートのような項目、定義された英語名をそのまま出力する
						sb.append(Const.COMMA).append(key);
					}
				}
				
			}
			
			// 行目をヘッダの1番目として出力
			this.header = sb.insert(0, LINE_NO_HEADER).toString();
			
		}
		
		this.errorHeader = this.header;
		//TODO XML定義どおりにratekeyの完備性をチェックするのか
	}

	@Override
	public void setHeader(String header) {

		if (StringUtils.isEmpty(header)) {
			throw new IllegalArgumentException("ヘッダにはヌル、空以外指定ください");
		}
		
		this.headerArray = StringUtils.split(header, COMMA);
		if (this.headerArray.length == 0) {
			throw new IllegalArgumentException("ヘッダ指定フォーマット不正");
		}
				
		this.header = header;

	}

	public void setAppendEnable(boolean appendEnable) {
		this.appendEnable = appendEnable;
	}
	
	/**
	 * 6万件毎にファイルを分割するか
	 * @param splitEnable
	 */
	public void setSplitFileEnable(boolean splitEnable) {
		this.splitFileEnable = splitEnable;
	}

//	public String getFileName() {
//		return this.file.getName();
//	}
	
	public long getLineCount() {
		return this.lineCount;
	}
	
	public long getNgCount() {
		return this.NGlineCount;
	}
	
	public long getErrorLineCount() {
		return this.errorLineCount;
	}
	
	/**
	 * 生成したファイルのパスを取得
	 * @return
	 */
	public String getFilePath() {
		return this.file.getAbsolutePath();
	}
	
	/**
	 * 生成したフォルダのパスを取得
	 * @return
	 */
	public String getFolderPath() {
		return resultBasePath;
	}
	
	/**
	 * ベーズパスを指定
	 * @param resultBasePath
	 */
	public void setResultBasePath(String resultBasePath) {
		this.resultBasePath = resultBasePath;
	}

}
