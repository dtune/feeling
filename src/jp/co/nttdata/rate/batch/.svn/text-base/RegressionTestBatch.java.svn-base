package jp.co.nttdata.rate.batch;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import jp.co.nttdata.rate.exception.FmsDefErrorException;
import jp.co.nttdata.rate.exception.IllegalBatchDataException;
import jp.co.nttdata.rate.exception.RateException;
import jp.co.nttdata.rate.fms.calculate.RateCalculator;
import jp.co.nttdata.rate.fms.common.SystemFunctionUtility;
import jp.co.nttdata.rate.log.LogFactory;
import jp.co.nttdata.rate.model.CalculateCategory;
import jp.co.nttdata.rate.model.CategoryManager;
import jp.co.nttdata.rate.model.rateKey.RateKeyManager;
import jp.co.nttdata.rate.rateFundation.RateFundationManager;
import jp.co.nttdata.rate.util.Const;
import jp.co.nttdata.rate.util.PropertiesUtil;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.time.DateFormatUtils;
import org.apache.log4j.Level;

/**
 * 算式修正にはディクレイを起こらないように回帰テストを行う
 * @author zhanghy
 *
 */
public class RegressionTestBatch {

	/** 特体のランク値 */
	private static final int SPECIAL_INDEX_RANK = 100;
	private static final String SPECIAL_FORMULA_PREFIX = "Special_";
	private static final String BONUS_FORMULA_PREFIX = "Bonus_";
	
	/** 保険料金額 */
	private static final String PRATE_FORMULA = "Premium";
	private static final String SPECIAL_PRATE_FORMULA = "Special_Premium";
	private static final String BONUS_PRATE_FORMULA = "Bonus_Premium";
	private static final String SPECIAL_BONUS_PRATE_FORMULA = "Special_Bonus_Premium";

	/** Vレート */
	private static final String VRATE_FORMULA = "BT_Vrate";
	/** 解約返戻金レート */
	private static final String WRATE_FORMULA = "CashValue";
	/** 払済 */
	private static final String PAIDUP_FORMULA = "paidup_Premium";//paidup_SurrenderFee
	/** 延長 */
	private static final String EXTEND_FORMULA = "extend_live_Premium";
	/** 未割当配当金残高 */
	private static final String DT_FORMULA = "Dt";
	/** 未払年金 */
	private static final String UNPAIDANNUITY = "UnpaidAnnuity";
	
	private static final String ALLOCATION_FORMULA = "Allocation";
	
	/** datファイル計算結果を格納するファイル */
	private static final String DEFAULT_DAT_LIST_PATH = "\\DatList";
	private static final String DEFAULT_OUTPUT_DAT_LIST_PATH = "\\DatListtmp";
	
	/** fileList */
	private static StringBuilder strFileList = new StringBuilder();
	
	/** 商品コードの位置＆長さ （P,V,Wの順番で）*/
	private static int[] POS_INSURANCE_CODE = new int[] { 21, 145, 26, 274, 116, 152 };
	private static int INSURANCE_CODE_LENGTH = 3;

	private static int[] POS_SPECIAL_INDEX = new int[] { 46, 208, 198, 274, 116, 152 };
	private static int SPECIAL_INDEX_LENGTH = 3;

	private static int[] POS_BONUS_SIGN = new int[] { 82, 54, 93, 60, 60, 152};
	private static int BONUS_SIGN_LENGTH = 1;

	/** PVWデータの桁数(改行符号含めてない) */
	public static int[] DATA_LENGTH = new int[] { 356, 563, 350, 492, 284 ,1000};
	
	/** 計算モジュール */
	private RateCalculator rc;
	/** データ読み込むうモジュール */
	private IRateKeyReader reader;
	/** 計算結果を書き出しモジュール */
	private CsvFileWriterImpl writer;
	private static final boolean NG_ONLY = true;
	private static final String COMPARE_MAPPING_PROPERTIES = "batch/compareMapping.properties";
	
	/**バッチ処理のワークデレクトリィ*/
	private String workDir;
	/** カレント処理対象となるファイル */
	private File curWorkFile;
	
	/** ワークデレクトリィ下のdatファイルの計算情報を格納する */
	private File datListFile;
	private File datListFiletmp;
	private BufferedReader datListReader;
	private BufferedWriter datListWriter;
	
	/** 商品コード */
	private String curInsuranceCode;
	
	/** datファイルに応じる計算式の英語名 */
	private String curFormulaName;
	
	/**ファイルフィルター*/
	private BatchInputFileFilter datFilter;
	private BatchInputFileFilter defaultFilter;
	
	/** 特体フラグ */
	private boolean isSpecial = false;
	private boolean isBonus = false;
		
	/** 比較対象マッピング */
	private Map<String, String> compareMapping;
	private Properties compareMappingProp;
	
	/** 計算結果 */
	private Double ret;
	
	/**
	 * 回帰テストのため、それぞれモジュールを初期化する
	 */
	public RegressionTestBatch(String workDir) {
		
		//ログオフにする
		LogFactory.setLoggerLevel(Level.OFF);
		
		this.workDir = workDir;	
		this.curWorkFile = new File(this.workDir);
		if (!this.curWorkFile.exists()) {
			throw new IllegalArgumentException("指定パスはファイル或いはフォルダではありません：" + workDir);
		}
		
		this.compareMappingProp = PropertiesUtil.getExternalProperties(COMPARE_MAPPING_PROPERTIES);
		
		this.datFilter = new BatchInputFileFilter(new String[]{"dat","csv"});
		this.defaultFilter = new BatchInputFileFilter();
		
		this.datListFile = new File(this.workDir + "\\" + DEFAULT_DAT_LIST_PATH +
				DateFormatUtils.format(new Date(System.currentTimeMillis()), "yyyyMMdd") + ".rlt");
		this.datListFiletmp = new File(this.workDir + "\\" + DEFAULT_OUTPUT_DAT_LIST_PATH +
				DateFormatUtils.format(new Date(System.currentTimeMillis()), "yyyyMMdd") + ".rlt");

		try {
			if(!datListFile.exists() || datListFile.length() == 0){
				_getAllDatFilePath();
			} else {
				datListFile.delete();
				datListFile.createNewFile();
				_getAllDatFilePath();
			}
			
		this.datListReader = new BufferedReader(new FileReader(this.datListFile));
		this.datListWriter = new BufferedWriter(new FileWriter(this.datListFiletmp));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void _getAllDatFilePath() throws IOException {

		this.datListWriter = new BufferedWriter(new FileWriter(datListFile));

		StringBuilder sbTitle = new StringBuilder();
		sbTitle.append("商品コード");
		sbTitle.append(",").append("データファイル");
		sbTitle.append(",").append("NG件数(SKIP:カレントデータ計算しない；REDO:カレントデータ再計算)");
		sbTitle.append(",").append("総件数");
		sbTitle.append(",").append("かける期間(秒)");
		sbTitle.append(",").append("性能(秒/千件)");
		this.datListWriter.write(sbTitle.toString());
		this.datListWriter.newLine();
		this.datListWriter.flush();
		
		getFileList(curWorkFile);
		this.datListWriter.write(strFileList.toString());		
		this.datListWriter.flush();
		this.datListWriter.close();
	}
	
	private void getFileList(File file) {
		String filePath;
		if(file.getName().indexOf("検証対象外") > -1)
			return;
		if(file.isDirectory()) {
			File fileList[] = file.listFiles(datFilter);
			for(int i = 0; i < fileList.length; i++){
				getFileList(fileList[i]);
			} 
		} else {
			if(file.length() == 0)
				return;	
			filePath = file.getAbsolutePath();
			if(datFilter.accept(this.curWorkFile)) {
				strFileList.append(filePath);
				strFileList.append("\n");
			}
		}
	}
	
	/**
	 * カレントdatファイルを返す
	 * @return
	 */
	private File _nextFile() {
		return this.curWorkFile;
	}
	
	/**
	 * 指定されていたフォルダの下に次のdatファイルがあるか
	 * @return
	 */
	private boolean _haveNextFile() throws IOException, RateException {

		String data;
		while ((data = datListReader.readLine()) != null) {

			if (StringUtils.isBlank(data)) {
				return false;
			}
			
			/*
			 *  初回計算の場合、ファイル名（パス）だけ書いてある
			 *  計算済みの場合、ファイル名の後ろに、コンマ区切りで「全件数」、「NG件数」、「掛時間」を追加
			 *  かつ、計算済みのファイルに対して、デフォルトとして二度と計算しないようにとする
			 */
			
			String[] dataSplit = data.split(Const.COMMA);
			if (dataSplit.length == 1) {
				this.curWorkFile = new File(data);
				return true;
			} else {
				if(dataSplit.length == 2 || "REDO".equals(dataSplit[2])){
					this.curWorkFile = new File(dataSplit[1]);
					return true;
				}

				datListWriter.write(data);
				datListWriter.newLine();
				datListWriter.flush();
			}
		}
		datListWriter.close();
		return false;
	}

	/**
	 * カレント処理対象となるdatファイルの第１行(CSV用)
	 * を読みから商品コードと計算カテゴリを編集したうえで計算モジュールを初期化
	 * @throws IOException 
	 * @throws RateException 
	 * @throws FmsDefErrorException 
	 */
	private boolean _initRateCalculator4CSV(int kubun) throws IOException, RateException, FmsDefErrorException {

		BufferedReader br = new BufferedReader(new FileReader(this.curWorkFile));
		String head = br.readLine();

		if (StringUtils.isBlank(head)) {
			return false;
		}
		String data = br.readLine();

		String insuranceCode = null;
		String calculateCate = CalculateCategory.DIVIDEND;

		String[] heads = head.split(",");
		String[] datas = data.split(",");
		
		for (int i = 0; i < heads.length; i++){
			if("料率保種CN".equals(heads[i])){
				insuranceCode = datas[i];
				break;
			}
		}
		if(insuranceCode == null){
			throw new IllegalBatchDataException("取扱データ対象外のため、 データレイアウトご確認ください：" + this.curWorkFile.getAbsolutePath());
		}
		
		insuranceCode = _editInsuranceMapping(insuranceCode);
		insuranceCode = insuranceCode + "7";
		
		// 算式名を編集
		switch(kubun){
		case 1:
			this.curFormulaName = DT_FORMULA;
			break;
		case 2:
			this.curFormulaName = ALLOCATION_FORMULA;
			break;
		default:
			throw new IllegalBatchDataException("配当計算対象外のため、 データレイアウトご確認ください：" + this.curWorkFile.getAbsolutePath());
		}
		// 比較対象を編集
		String compOriginal = _editCompareMapping(this.curFormulaName);
		if (!insuranceCode.equals(this.curInsuranceCode)) {			
			this.curInsuranceCode = insuranceCode;
			this.rc = new RateCalculator(insuranceCode, true);
			this.rc.setCalculateCate(new String[]{CalculateCategory.P, CalculateCategory.V, CalculateCategory.W, CalculateCategory.DIVIDEND});

			//計算に必要であるレートキー＆カテゴリマネジャーを初期化
			RateKeyManager.newInstance(insuranceCode);
			CategoryManager.newInstance(insuranceCode);						
		}
		this.reader.setCompareObject(compOriginal);
		// NGのみ出力指定でwriterを初期化
		this.writer = new CsvFileWriterImpl(insuranceCode, this.reader.getFile(), compareMapping, NG_ONLY, calculateCate, compOriginal);
		return true;
	}
	
	/**
	 * カレント処理対象となるdatファイルの第１行
	 * を読みから商品コードと計算カテゴリを編集したうえで計算モジュールを初期化
	 * @throws IOException 
	 * @throws RateException 
	 * @throws FmsDefErrorException 
	 */
	private boolean _initRateCalculator() throws IOException, RateException, FmsDefErrorException {

		BufferedReader br = new BufferedReader(new FileReader(this.curWorkFile));
		String data = br.readLine(); 
		
		if (StringUtils.isBlank(data)) {
			return false;
		}
		
		String insuranceCode = null;
		String calculateCate = null;
		String formulaName = null;
		
		// データの長さより計算種類を判定するうえで商品コードを編集
		int index = -1;
		int len = data.length();		
		switch (len) {
		case 305:
		case 356:
			calculateCate = CalculateCategory.P;
			formulaName = PRATE_FORMULA;
			index = 0;
			break;
		case 563:
			calculateCate = CalculateCategory.V;
			formulaName = VRATE_FORMULA;
			index = 1;
			this.isSpecial = true;
			break;
		case 350:
			calculateCate = CalculateCategory.W;
			formulaName = WRATE_FORMULA;
			index = 2;
			break;
		case 492:
			calculateCate = CalculateCategory.H;
			formulaName = PAIDUP_FORMULA;
			index = 3;
			break;
		case 284:
			calculateCate = CalculateCategory.E;
			formulaName = EXTEND_FORMULA;
			index = 4;
			break;
		case 1000:
			calculateCate = CalculateCategory.A;
			formulaName = UNPAIDANNUITY;
			index = 5;
			break;
		default:
			throw new IllegalBatchDataException("取扱データ対象外のため、 データレイアウトご確認ください：" + this.curWorkFile.getAbsolutePath());
		}

		String origCode = data.substring(POS_INSURANCE_CODE[index], POS_INSURANCE_CODE[index] + INSURANCE_CODE_LENGTH);
		// 未払年金の場合、 主契約：152+3,特約：264+3
		if ("   ".equals(origCode)) {
			origCode = data.substring(264, 267);
		}
		insuranceCode = _editInsuranceMapping(origCode);
			
		// 算式名を編集
		this.curFormulaName = _editFormulaName(formulaName, data, index);
				
		// 比較対象を編集
		String compOriginal = _editCompareMapping(this.curFormulaName);
		
		// 特体の場合、商品コードの末尾に特体フラグを追記
		if (this.isSpecial) {
			insuranceCode += "1";
		}
		if (this.isBonus) {
			insuranceCode += "2";
		}
		if (index == 3) {
			insuranceCode += "3";
		}
		if (index == 4) {
			insuranceCode += "4";
		}
		// 商品コードと計算種類より計算モジュールを初期化
		if (!insuranceCode.equals(this.curInsuranceCode)) {			
			this.curInsuranceCode = insuranceCode;
			this.rc = new RateCalculator(insuranceCode, true);
			this.rc.setCalculateCate(new String[]{CalculateCategory.P, CalculateCategory.V, CalculateCategory.W, CalculateCategory.H, CalculateCategory.E, CalculateCategory.A});

			//計算に必要であるレートキー＆カテゴリマネジャーを初期化
			RateKeyManager.newInstance(insuranceCode);
			CategoryManager.newInstance(insuranceCode);						
		}
		
		//　データ転換用のレイアウトをロード
		if(this.reader instanceof RegressionDatRateKeyReaderImpl){
			((RegressionDatRateKeyReaderImpl) reader).loadDataLayout(insuranceCode, calculateCate);
		}
		// 比較対象を設定
		this.reader.setCompareObject(compOriginal);
		if(calculateCate == CalculateCategory.V){
			this.reader.setFixedValues("f=0;f1=0;fEX=0;accidentUmu=0;j=0");
		}
		if(calculateCate == CalculateCategory.W){
			this.reader.setFixedValues("PremiumAbolishSign=0;SA=10000;accidentUmu=0;j=0;fEX=0");
		}
		if(calculateCate == CalculateCategory.H){
			if(insuranceCode.startsWith("011")){
				this.reader.setFixedValues("gen=4");
			}
		}
		if(calculateCate == CalculateCategory.E){
			if(insuranceCode.startsWith("011")){
				this.reader.setFixedValues("gen=4");
			}
		}
		// NGのみ出力指定でwriterを初期化
		this.writer = new CsvFileWriterImpl(insuranceCode, this.reader.getFile(), compareMapping, NG_ONLY, calculateCate, compOriginal);
		
		return true;
	}
	
	
	/**
	 * それぞれレートキーより行われる算式名を編集
	 * <br>主に保障について算式ネーミングルールに従ってそれぞれのprefixを先頭に追加
	 * @param formulaName
	 * @param data
	 * @param index
	 * @return
	 */
	private String _editFormulaName(String formulaName, String data, int index) {
		String special = data.substring(POS_SPECIAL_INDEX[index], POS_SPECIAL_INDEX[index] + SPECIAL_INDEX_LENGTH);
		String bonus = data.substring(POS_BONUS_SIGN[index], POS_BONUS_SIGN[index] + BONUS_SIGN_LENGTH);
		// 未払年金の場合、specialIndexとbonusSignはNULLの為.
		if("   ".equals(special)) {
			special = "100";
		}
		if(" ".equals(bonus)) {
			bonus = "0";
		}
		int specialIndex = Integer.parseInt(special);
		int bonusSign = Integer.parseInt(bonus);
		String newFormulaName;
		if (formulaName == null) {
			newFormulaName = "";
		} else {
			newFormulaName = new String(formulaName);
		}		
		if(index == 0) {
			
			if(bonusSign == 1 && specialIndex > SPECIAL_INDEX_RANK) {
				this.isBonus = true;
				this.isSpecial = true;
				newFormulaName = SPECIAL_BONUS_PRATE_FORMULA;
			}else if(bonusSign == 1){
				this.isBonus = true;
				newFormulaName = BONUS_PRATE_FORMULA;
			}else if(specialIndex > SPECIAL_INDEX_RANK){
				this.isSpecial = true;
				newFormulaName = SPECIAL_PRATE_FORMULA;
			}else{
				newFormulaName = PRATE_FORMULA;
//				newFormulaName = "adjustPrate";
			}
		} else if(index == 2) {
			// ボーナス払サイン:ボーナス払であることを示すサイン（１：ボーナス払）
			if (bonusSign == 1) {
				this.isBonus = true;
				newFormulaName = "Bonus_SurrenderFee";
			}
			// 特体死亡指数:特体契約の保険料割増率（100：標準体、100より大きい：特体）
			if (specialIndex > SPECIAL_INDEX_RANK) {
				this.isSpecial = true;
				newFormulaName = SPECIAL_FORMULA_PREFIX + newFormulaName;
			}
			
			//TODO 延長保険
			
			//TODO 払込済み
		}
		return newFormulaName;
	}
	
	private String _editCompareMapping(String formulaName) {
		
		this.compareMapping = new HashMap<String, String>();
		String compOriginal = this.compareMappingProp.getProperty("formula." + formulaName);
		if (StringUtils.isBlank(compOriginal)) {
			throw new IllegalBatchDataException("比較マッピングファイルに"+"formulaName"+"を定義されていません：" + COMPARE_MAPPING_PROPERTIES);
		}
		
		this.compareMapping.put(compOriginal, formulaName);
		
		return compOriginal;
	}

	/**
	 * 商品033については、レート計算定義は031商品コードとする
	 * @param code
	 * @return
	 */
    private String _editInsuranceMapping(String code) {

        String insuranceCode = null;
        switch (Integer.parseInt(code)) {
        case 5:
            insuranceCode = "004";
            break;
        case 32:
        case 33:
        case 34:
            insuranceCode = "031";
            break;
        case 932:
        case 933:
        case 934:
            insuranceCode = "931";
            break;
        case 236:
        case 237:
        case 238:
            insuranceCode = "235";
            break;
        case 262:
        case 263:
        case 264:
            insuranceCode = "261";
            break;
        case 266:
        case 267:
        case 268:
            insuranceCode = "265";
            break;
        case 278:
            insuranceCode = "276";
            break;
        case 222:
        	insuranceCode = "221";
        	break;
        case 224:
        	insuranceCode = "223";
        	break;
        case 36:
        	insuranceCode = "035";
        	break;
        default:
            insuranceCode = code;
        }

        return insuranceCode;
    }


	/**
	 * 指定されたフォルダの下のすべてのdatファイルを入力として、１ずつ読みながら
	 * 計算を行って、NG結果をCSVファイルに書き出し
	 * @throws IOException 
	 * @throws RateException 
	 */
	@SuppressWarnings("unchecked")
	private void calculate(int dividendKuben) {

		try{
			
			this.curWorkFile = _nextFile();
			
			System.out.println(this.curWorkFile.getAbsolutePath());
			
			// readerを初期化する
			if(defaultFilter.accept(this.curWorkFile)){
				this.reader = new RegressionDatRateKeyReaderImpl(this.curWorkFile);
			}else{
				this.reader = new CsvRateKeyReaderImpl(this.curWorkFile);
			}
			boolean continueKuben = false;
			if(defaultFilter.accept(this.curWorkFile)){
				if(dividendKuben == 0){
					if(_initRateCalculator()){
						continueKuben = true;
					}
				}
			} else {
				if(dividendKuben != 0){
					if(_initRateCalculator4CSV(dividendKuben)){
						continueKuben = true;
					}
				}
			}
			
			if (continueKuben) {

				RegressionDataFilter dataDilter = new RegressionDataFilter(RateFundationManager.getInstance());
				/*
				 * 有効なdatファイルの場合、計算モジュールを初期化したあと
				 * バッチ処理を行う
				 */
				long a = System.currentTimeMillis();
				long taskNo = 0l;
				Map rateKeys = null;
				
				while ((rateKeys = reader.readRateKeys()) != null) {
					taskNo++;
					if (dividendKuben == 0 && dataDilter.filter(rateKeys)) {
						continue;
					}
					this.rc.setRateKeys(rateKeys);
					ret = this.rc.calculate(curFormulaName);
					_editCompareRateKeys(rateKeys);
					Map<String, Double> result = new HashMap<String, Double>();
					result.put(curFormulaName, ret);
					this.writer.setInput(rateKeys);
					this.writer.output(result, taskNo);
				}
				
				this.writer.close();
				
				/*
				 * TODO ファイル単位で計算が終わったら、datListファイルに計算結果を纏めて追加する
				 * フォーマット：「datファイルパス」,「全件数」、「NG件数」、「掛時間」
				 */
				long b = System.currentTimeMillis() - a;
				StringBuilder sb = new StringBuilder();
				sb.append("'").append(this.curInsuranceCode.substring(0, 3));
				sb.append(",").append(this.curWorkFile.getAbsolutePath());
				sb.append(",").append(this.writer.getNgCount());
				sb.append(",").append(this.writer.getLineCount());
				sb.append(",").append(SystemFunctionUtility.roundUp((double)b/1000,1));
				sb.append(",").append(SystemFunctionUtility.roundDown((double)b/this.writer.getLineCount(),3));
				datListWriter.write(sb.toString());
				datListWriter.newLine();
				datListWriter.flush();
			}
		}catch(Throwable e){
			StringBuilder sb = new StringBuilder();
			sb.append("'").append(this.curInsuranceCode.substring(0, 3));
			sb.append(",").append(this.curWorkFile.getAbsolutePath());
			sb.append(",").append("SKIP");
			sb.append(",").append(e.getMessage());
			try {
				datListWriter.write(sb.toString());
				datListWriter.newLine();
				datListWriter.flush();
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}
	}
	
	public void run(int method) {
		try {
			while(_haveNextFile()){
				calculate(method);
			}
		} catch (Throwable e) {
			// TODO 異常情報はログに（StackTrace内容）出力しように修正　かつ　エラーメッセージ(e.getMessage)はdatListに出力
			e.printStackTrace();
			String data;
			try {
				StringBuilder sb = new StringBuilder();
				sb.append("'").append(curInsuranceCode.substring(0, 3));
				sb.append(",").append(curWorkFile.getAbsolutePath());
				sb.append(",").append("SKIP");
				sb.append(",").append(e.getMessage());
				datListWriter.write(sb.toString());

				datListWriter.newLine();
				datListWriter.flush();

				while ((data = datListReader.readLine()) != null) {

					datListWriter.write(data);
					datListWriter.newLine();
					datListWriter.flush();
				}
				datListWriter.close();
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		} finally {
			try {
				datListReader.close();
				datListWriter.close();
				datListFile.delete();
				datListFiletmp.renameTo(datListFile);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	private void _editCompareRateKeys(Map<String, Double> rateKeys) {
		if(rateKeys.containsKey("CompareValue")) {
			Double compareValue = rateKeys.get("CompareValue");
			if(compareValue != 0d && ret != 0d && (ret / compareValue * 10000 % 10 == 0
			        || compareValue / ret * 10000 % 10 == 0)) {
				rateKeys.put("CompareValue", ret);	
			} else {
				rateKeys.put("CompareValue", compareValue);
			}
		}
		if(rateKeys.containsKey("specialPremium")
				&& rateKeys.containsKey("PrateUnit")){
			rateKeys.put("specialPremium",
					rateKeys.get("specialPremium") * rateKeys.get("SA")
							/ rateKeys.get("PrateUnit"));
		}
		if(rateKeys.containsKey("bonusPremium")
				&& rateKeys.containsKey("PrateUnit")){
			rateKeys.put("bonusPremium",
					rateKeys.get("bonusPremium") * rateKeys.get("SA")
							/ rateKeys.get("PrateUnit"));
		}
		if(rateKeys.containsKey("specialbonusPremium")
				&& rateKeys.containsKey("PrateUnit")){
			rateKeys.put("specialbonusPremium",
					rateKeys.get("specialbonusPremium") * rateKeys.get("SA")
							/ rateKeys.get("PrateUnit"));
		}
	}
	
	public static void main(String[] args) {
		
		if (args.length < 2) {
			throw new IllegalArgumentException("RegressionTestBatchにはデータフォルダのパスと計算類別を指定ください。");
		}

		String filePath = args[0];

		int method = Integer.valueOf(args[1]);
		if (method > 2 || method < 0) {
			throw new IllegalArgumentException("計算種別は「0:PVW 1:未割当配当金残高 2:分配額」のいずれを指定ください。");
		}
		RegressionTestBatch rtb = new RegressionTestBatch(filePath);
		rtb.run(method);
	}
}