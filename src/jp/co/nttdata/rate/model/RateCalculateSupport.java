package jp.co.nttdata.rate.model;

import java.io.File;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import org.apache.commons.beanutils.ConvertUtils;
import org.apache.commons.lang.StringUtils;

import jp.co.nttdata.rate.exception.FmsDefErrorException;
import jp.co.nttdata.rate.exception.RateException;
import jp.co.nttdata.rate.fms.calculate.RateCalculator;
import jp.co.nttdata.rate.model.formula.Formula;
import jp.co.nttdata.rate.model.formula.FormulaManager;
import jp.co.nttdata.rate.model.rateKey.Item;
import jp.co.nttdata.rate.model.rateKey.RateKeyManager;
import jp.co.nttdata.rate.model.rateKey.RateKeyValidator;
import jp.co.nttdata.rate.rateFundation.dbConnection.DBConnection;
import jp.co.nttdata.rate.rateFundation.dbConnection.DataRow;
import jp.co.nttdata.rate.ui.view.InsuranceSelectDialog;
import jp.co.nttdata.rate.util.Const;
import jp.co.nttdata.rate.util.ResourceLoader;
/**
 * UIマネージメントはレート計算ツールOL部分のエントリーである
 * <br>タブ情報および所属計算対象情報を取得する
 * @author btchoukug
 *
 */
public class RateCalculateSupport {
	
	private String code;
	/**商品コード*/
	private String insuranceCode;
	/**商品名称*/
	private String name;
	
	/**　世代情報SQL文*/
	private final static String INSURANCE_GEN_SQL = "select generation "
		+ "from rate_master where insurance_code = ${code} "
		+ "group by generation";
	
	/**計算モジュール*/
	private RateCalculator rc;
	
	/*6つ定義ファイル*/
	private File formulaDefFile;
	private File rateKeyDefFile;
	private File uiDefFile;
	
	private File fundationDefFile;
	private File benefitDefFile;
	private File lifePersionDefFile;

	private List<Item> generationList;
		
	/** 
	 * 商品コードより計算単位の定義XMLを読み込んで、
	 * レートキーの入力UIあるいはBT計算の初期情報を作成する 
	 * @param insuranceCode
	 * @throws FmsDefErrorException 
	 */
	public RateCalculateSupport(String code) throws FmsDefErrorException {
		_init(code);
		this.rc.getContext().setCacheEnabled(true);
	}
	
	public RateCalculateSupport(String code, boolean enableCache) throws FmsDefErrorException {
		_init(code);
		this.rc.getContext().setCacheEnabled(enableCache);
	}
	
	public void setCacheEnable(boolean enableCache) {
		this.rc.getContext().setCacheEnabled(enableCache);
	}
	
	/**
	 * XML配置とかDB基数とか変わる場合、再度ロードする
	 * @throws FmsDefErrorException 
	 * @throws FmsDefErrorException 
	 */
	public void reload() throws FmsDefErrorException {
		_init(this.code);
	}
	
	private void _init(String code) throws FmsDefErrorException {
		this.code = code;
		
		this.insuranceCode = code.substring(0, 3);
		this.name = _getDisplayName(code);
		
		//計算モジュール初期化
		this.rc = new RateCalculator(code);
		
		RateKeyManager.newInstance(code);
		//計算カテゴリ情報を読み込む（主にタブ表示用）
		CategoryManager.newInstance(code);
		
		//すべての計算範囲の中の計算公式をロード、共通の部分も含める
		this.rc.setCalculateCate(CategoryManager.getInstance().getCateNames());
		
		//デフォルト場合、保険料レートの公式およびキーの制御関係を設定
		setCalculateCategory(0);		
		
		//世代情報を取得
		this.generationList = _getGenerationList();
		
		//３つ定義ファイルの情報を取得
		_getDefFile();
		
	}
	
	private void _getDefFile() {

		this.formulaDefFile = ResourceLoader.getExternalFile(Const.FORMULA_DEF_DIR + this.insuranceCode + Const.XML_SUFFIX);
		this.rateKeyDefFile = ResourceLoader.getExternalFile(Const.RATEKEY_DEF_DIR + this.insuranceCode + Const.XML_SUFFIX);
		this.uiDefFile = ResourceLoader.getExternalFile(Const.CALCULATION_DEF_DIR + this.insuranceCode + Const.XML_SUFFIX);
		this.fundationDefFile = ResourceLoader.getExternalFile(Const.FUNDATION_DEF);
		this.benefitDefFile = ResourceLoader.getExternalFile(Const.FORMULA_BENEFIT_DIR);
		this.lifePersionDefFile = ResourceLoader.getExternalFile(Const.FORMULA_LIFEPENSION_DIR);
		
	}

	private String _getDisplayName(String insuranceCode) {
		return InsuranceSelectDialog.insuranceName;
		
	}
	
	public String getWindowTitle() {
		return this.name;
	}

	/**
	 * タブのインデックスよりUIに使われているレートキーの制御関係を設定
	 * @param tabIndex
	 */
	public void setCalculateCategory(int tabIndex) {
		String[] keys = CategoryManager.getInstance().getCateInfos().get(tabIndex).getKeys();
		RateKeyValidator.setAllConstraint(RateKeyManager.getKeyConstraints(keys));
	}

	/**
	 * XMLに定義された公式を取得して計算する
	 * @param input 
	 * @param input
	 * @return 
	 * @return
	 * @throws Exception 
	 */
	public Map<String, Double> calculate(Map<String, Object> input, Formula f) throws Exception {
		
		//コンテキストの初期化（制御関係を検証する際にコンテキストが利用必要）		
		this.rc.setRateKeys(input);
				
		//OLの場合、計算カテゴリに応じてレートキーに対して、制御関係を検証
		RateKeyValidator.validateAllConstraint(this.rc.getContext());	

		//入力キーが問題がなければ、公式計算を行う
		if (f == null || StringUtils.isBlank(f.getName())) {
			throw new RateException("計算対象には算式がありません：" + f.toString());
		} 
		
		//計算結果
		Map<String, Double> result = new HashMap<String, Double>();
		
		//選んだ計算式を計算したうえ、結果を返す
		result.put(f.toString(), this.rc.calculate(f.getName()));
				
		//途中値も差し込み
		Iterator<Entry<String, Object>> it = this.rc.getContext().getIntermediateValue().entrySet().iterator();
		for (; it.hasNext(); ) {
			Entry<String, Object> entry = it.next();
			result.put(entry.getKey(), ((Double)ConvertUtils.convert(entry.getValue(), Double.class)));
		}
						
		return result;

	}
	
	/**
	 * 世代リストを取得
	 * @param code
	 * @return
	 * @throws SQLException 
	 */
	private List<Item> _getGenerationList() {
		
		DBConnection.getInstance().open();
				
		List<Item> items = new ArrayList<Item>();		
		
		try {
			List<DataRow> list = DBConnection.getInstance().query(INSURANCE_GEN_SQL, "code", this.insuranceCode);
			// 当該商品について有効な世代を一ずつ取得
			for (DataRow data : list) {
				int gen = data.getInt("generation");
				items.add(new Item(String.valueOf(gen), gen));
			}
		} finally {
			DBConnection.getInstance().close();	
		}		
		
		return items;
	}

	public File getFormulaDefFile() {
		return this.formulaDefFile;
	}

	public File getRatekeyDefFile() {
		return this.rateKeyDefFile;
	}

	public File getUIDefFile() {
		return this.uiDefFile;	
	}
	
	public File getFundationDefFile() {
		return fundationDefFile;
	}

	public File getBenefitDefFile() {
		return benefitDefFile;
	}

	public File getLifePersionDefFile() {
		return lifePersionDefFile;
	}
	
	public void cache2Disk() {
		this.rc.getContext().getCache().cache2Disk();		
	}

	public List<CalculateCategory> getCateInfos() {
		return CategoryManager.getInstance().getCateInfos();
	}

	public List<Item> getGenerationList() {
		return generationList;
	}

	public String getCode() {
		return this.code;
	}

	public FormulaManager getFormulaManager() {
		return rc.getContext().getFormulaManager();
	}

}
