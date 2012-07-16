package jp.co.nttdata.rate.model;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.configuration.XMLConfiguration;

import jp.co.nttdata.rate.util.CommonUtil;
import jp.co.nttdata.rate.util.Const;
import jp.co.nttdata.rate.util.ResourceLoader;

/**
 * 計算カテゴリに関して情報をマネージメント
 * @author btchoukug
 *
 */
public class CategoryManager {
	
	private static final String CALCULATION_CATE_ID = "CalculateCategory";

	private static final String CATE_PREFIX = "Categories.Category";

	private static final String CATE_NODE_NAME = CATE_PREFIX + Const.DOT + "name";
	private static final String CATE_NODE_LABEL = CATE_PREFIX + Const.DOT + "label";
	private static final String NODE_RATEKEYS = "RateKeys";
	
	private static CategoryManager INSTANCE;
	
	/** 商品基礎PVWのカテゴリ情報を持つ */
	private XMLConfiguration config;
		
	/** 商品コード */
	private String insuranceCode;

	/** 保障選択する場合、末尾付いたフラグを保持 */
	private String securityFlg;
	
	private XMLConfiguration[] securityConfigs;
	
	private String[] cateNames;
	private String[] cateLabels;
	
	/**計算カテゴリを格納するリスト*/
	private List<CalculateCategory> categories;
	
	public CategoryManager(String code) {
		categories = new ArrayList<CalculateCategory>();
		securityConfigs = new XMLConfiguration[Const.SECURITY_CATE_PREFIXS.length];
		_loadCateInfos(code);
	}
	
	public static synchronized void newInstance(String insuranceCode) {
		synchronized (CategoryManager.class) {
			INSTANCE = new CategoryManager(insuranceCode);				
		}
	}
	
	public static synchronized CategoryManager getInstance() {
		if (INSTANCE == null) {
			throw new IllegalArgumentException("CategoryManagerは初期化していません。");
		}
		return INSTANCE;
	}
	
	/**
	 * 商品コードより計算単位情報をロードする
	 */
	private void _loadCateInfos(String code) {
		
		this.insuranceCode = code.substring(0, 3);
		this.securityFlg = code.substring(3);
		
		//商品単位でPVWのカテゴリ情報を読み込む
		URL dtdUrl = ResourceLoader.getExternalResource(Const.CALCULATION_DEF_DTD);
		URL xmlUrl = ResourceLoader.getExternalResource(Const.CALCULATION_DEF_DIR + this.insuranceCode + Const.XML_SUFFIX);		
		this.config = CommonUtil.loadXML(xmlUrl, CALCULATION_CATE_ID, dtdUrl, false);
		
		//保障部分のカテゴリprefixを編集したうえで保障部分算式をロード
		for (int i = 0; i < Const.SECURITY_CATE_PREFIXS.length; i++) {
			if (this.securityFlg.indexOf(String.valueOf(i+1)) > -1) {
				xmlUrl = ResourceLoader.getExternalResource(Const.CALCULATION_DEF_DIR + Const.SECURITY_CATE_PREFIXS[i] + Const.XML_SUFFIX);
				this.securityConfigs[i] = CommonUtil.loadXML(xmlUrl, CALCULATION_CATE_ID, dtdUrl, false);					
			}
		}
		
		//タブに所属するレートキーのUI情報を取得
		this.cateNames = _getCateNamesFromConfig();
		this.cateLabels = _getCateLabelsFromConfig();
		
		for (int i = 0; i < cateNames.length; i++) {	
			CalculateCategory cate = new CalculateCategory(cateNames[i], cateLabels[i], _getCategoryRateKeys(i));
			this.categories.add(cate);
		}
	}
	
	/**
	 * タブに所属レートキー情報を取得
	 * @param index
	 * @return
	 */
	private String[] _getCategoryRateKeys(int index) {
		
		String idx = "(" + index + ").";
		String keySearch = CATE_PREFIX + idx + NODE_RATEKEYS;
		
		// PVWのレートキー情報を取得
		String[] keys = this.config.getStringArray(keySearch);
		
		//　保障部分のレートキー情報を取得
		for (int i = 0; i < Const.SECURITY_CATE_PREFIXS.length; i++) {
			if (this.securityFlg.indexOf(String.valueOf(i+1)) > -1) {
				String[] securityKeys = this.securityConfigs[i].getStringArray(keySearch);
				String[] key = new String[keys.length + securityKeys.length];
				System.arraycopy(keys, 0, key, 0, keys.length);
				System.arraycopy(securityKeys, 0, key, keys.length, securityKeys.length);
				keys = key;			
			}
		}
		
		return keys;
	}

	/**
	 * 計算カテゴリの情報を取得（UI定義XML）
	 * @return
	 */
	public List<CalculateCategory> getCateInfos() {
		return this.categories;
	}

	public CalculateCategory getCateInfo(String cateName) {
		
		for (int i = 0; i < this.cateNames.length; i++) {
			if (this.cateNames[i].equals(cateName)) {
				return this.categories.get(i);
			}
		}
		
		throw new IllegalArgumentException("カテゴリ名：　" + cateName + "が間違っている");		
	}

	public String[] getCateNames() {
		return this.cateNames;
	}
	
	public String[] getCateLabels() {
		return this.getCateLabels();
	}

	/**
	 * すべてのタブ名を取得
	 * @return
	 */
	private String[] _getCateNamesFromConfig() {
		return this.config.getStringArray(CATE_NODE_NAME);
	}
	
	/**
	 * すべてのタブ表示のラベルを取得
	 * @return
	 */
	private	String[] _getCateLabelsFromConfig() {
		return this.config.getStringArray(CATE_NODE_LABEL);
	}
}
