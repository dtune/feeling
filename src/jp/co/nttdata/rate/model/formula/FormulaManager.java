package jp.co.nttdata.rate.model.formula;

import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.Map.Entry;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.ConfigurationUtils;
import org.apache.commons.configuration.DefaultConfigurationBuilder;
import org.apache.commons.configuration.HierarchicalConfiguration;
import org.apache.commons.configuration.beanutils.BeanFactory;
import org.apache.commons.configuration.beanutils.BeanHelper;
import org.apache.commons.lang.StringUtils;

import jp.co.nttdata.rate.exception.FmsRuntimeException;
import jp.co.nttdata.rate.fms.core.FormulaParser;
import jp.co.nttdata.rate.util.Const;
import jp.co.nttdata.rate.util.ResourceLoader;

/**
 * 商品毎に定義の公式を管理するモジュールである
 * <p>指定のカテゴリ通りそれぞれ定義XMLから算式情報をロードする。
 * <br>また、算式名より算式情報を提供され、OL機能からアクセスできる算式を提供される</p>
 * 算式定義のデレクトリィは下記となり：<br>
 * settings<br>
 * 	|-formula<br>
 *		|-bonus //保障のボーナス<br>
 *		|-common<br>
 *		|-special //保障の特体<br>
 *		|-......<br>
 *		|-insurance<br>
 *	  		|-001.xml //001商品のPVW算式<br>
 *			|-002.xml<br>
 *	  		|-......<br>
 *		|-001.xml //商品単位の統括定義XML<br>
 *		|-002.xml<br>
 *		|-．．．．．．
 * @author btchoukug
 * 
 */
public class FormulaManager {

	// private static Logger logger =
	// LogFactory.getInstance(FormulaManager.class);

	public static final String FORMULA_BEANFACTORY = "FormulaBeanFactory";
	public static final String BIZEXPENSES = "BizExpenses";
	public static final String COMMON = "Common";
	public static final String FORMULA_XML_NODE = "formula";
		
	/** 保障選択する場合、末尾付いたフラグを保持 */
	private String securityFlg;
	
	/** 商品毎のすべての公式定義を格納するリスト、トップからサブまで階層という構造
	 * <br>共通算式も含める */
	private List<Formula> formulas = new ArrayList<Formula>();

	/** 計算種類(Premiumなど)に応じて公式のマッピング */
	private Map<String, List<Formula>> categoriedFormulas = new HashMap<String, List<Formula>>();

	/** 公式名よりクエリーのためのマップ */
	private Map<String, Formula> formulaPool = new HashMap<String, Formula>();

	/** サブ公式のフィルターリスト */
	private List<Formula> formulaFilterList;
	
	/** 端数処理入れるか入れないか */
	private boolean isRounding = true;
	
	/** 商品コード */
	private String insuranceCode;
	
	/**当商品に関する算式定義XMLを統括しようにロードBuilder*/
	private DefaultConfigurationBuilder builder;
	
	/**formulaオブジェクト生成工場*/
	private BeanFactory factory;
	
	public FormulaManager(String code) {

		this.insuranceCode = code.substring(0, 3);
		this.securityFlg = code.substring(3);
				
		// 算式をXMLからJavaBeanに置換するため、factoryをレジスター
		factory = new FormulaBeanFactory();
		BeanHelper.registerBeanFactory(FORMULA_BEANFACTORY, factory);
    	
		builder = new DefaultConfigurationBuilder();
		/*
		 * コンマ区切り禁止（算式の中身にあり）
		 * 現在のバージョンでは、下記のコードは有効ではない（バッグ）
		 */		
		builder.setDelimiterParsingDisabled(true);	
		//builder.setValidating(true);
		
		// 統括定義XMLのパスを編集＆設定
		URL configUrl = ResourceLoader.getExternalResource(Const.FORMULA_ROOT_DIR + this.insuranceCode + Const.XML_SUFFIX);
		builder.setURL(configUrl);		
		
	}


	/**
	 * トップから一番下まで公式を展開する（階層からマップに変換する）
	 * 
	 * @param f
	 */
	private void _expandAllFormula(Formula f, Map<String, Formula> namedFormulas) {
		if (namedFormulas == null) {
			namedFormulas = new HashMap<String, Formula>();
		}
		namedFormulas.put(f.getName(), f);
		if (f.getSubFormulaList() != null) {
			for (Formula subFormula : f.getSubFormulaList()) {
				_expandAllFormula(subFormula, namedFormulas);
			}
		}
	}

	/**
	 * 計算カテゴリより計算公式をロードする
	 * 
	 * @param categories
	 * @throws ConfigurationException 
	 */
    public void load(String[] categories) {
    	
		if (categories == null || categories.length == 0) {
			throw new IllegalArgumentException("計算カテゴリは指定していないため、算式が正常にロードされません。");
		}
    	
    	// Unionの形で計算定義をロード
    	try {
    		//複数XMLを纏めて持つconfiguration
    		HierarchicalConfiguration conf = ConfigurationUtils.convertToHierarchical(builder.getConfiguration());
    	
    		// 事業費率の公式を取得
			this.formulas.addAll(_getFormulaListByCates(conf, BIZEXPENSES));
			// 商品内部共通公式を取得
			this.formulas.addAll(_getFormulaListByCates(conf, COMMON));
		
			// 保険料、保険料積立金、解約返戻金に応じて公式を取得（保障部分も含めて）
			for (String cate : categories) {
				List<Formula> cateFormulas = _getFormulaListByCates(conf, cate);
				
				//保障部分のカテゴリprefixを編集したうえで保障部分算式をロード
				for (int i = 0; i < Const.SECURITY_CATE_PREFIXS.length; i++) {
					if (StringUtils.contains(this.securityFlg, String.valueOf(i+1))) {
						String secCatePrefix = Const.SECURITY_CATE_PREFIXS[i];
						cateFormulas.addAll(_getFormulaListByCates(conf, secCatePrefix + Const.DOT + cate));					
					}
				}
				
				categoriedFormulas.put(cate, cateFormulas);
				this.formulas.addAll(cateFormulas);
			}

			// トップから一番下まですべての公式をネーミングマッピングにセット
			for (Formula topFormula : this.formulas) {
				_expandAllFormula(topFormula, this.formulaPool);
			}

		} catch (ConfigurationException e) {
			throw new FmsRuntimeException("商品の統括定義XMLロード失敗でした：" + builder.getURL(), e);
		}
		// if (logger.isInfoEnabled()) {
		// logger.info("当商品の公式定義をロードしました。");
		// }
	}

	/**
	 * UI上に表示できる公式　即ち最終計算対象という公式のリストを返す <br>
	 * 普通としては、トップ公式は１つしかない
	 * 
	 * @param cate
	 * @return
	 */
    public List<Formula> getTopCalculateFormula(String cate) {
        return this.categoriedFormulas.get(cate);
	}

	/**
	 * カテゴリに応じて、公式定義の中に属性accessableがtrueという公式を返す
	 * 
	 * @return
	 */
	private List<Formula> _getTopFormulas(String cate) {
		Map<String, Formula> formulas = new HashMap<String, Formula>();

		List<Formula> formulaList = this.categoriedFormulas.get(cate);

		if (formulaList == null) {
			throw new FmsRuntimeException(cate + "に応じて算式は正常にロードされていませんでした。");
		}

		for (Formula topFormula : formulaList) {
			_expandAllFormula(topFormula, formulas);
		}

		List<Formula> accessableFormulas = new ArrayList<Formula>();
		for (Iterator<Entry<String, Formula>> it = formulas.entrySet()
				.iterator(); it.hasNext();) {
			Formula formula = it.next().getValue();
			if (formula.isAccessable()) {
				accessableFormulas.add(formula);
			}
		}
		return accessableFormulas;
	}

	/**
	 * 指定公式の直下のサブ公式を取得する <br>
	 * 重複の場合、１つとする
	 * 
	 * @param f
	 * @return
	 */
    public List<Formula> getSubFormulas(Formula f) {

		if (StringUtils.isEmpty(f.getBody())) {
			return null;
		}

		Map<String, Formula> subFormulaMap = new HashMap<String, Formula>();

		// オペレータで分割する
		StringTokenizer st = new StringTokenizer(f.getBody(),
				FormulaParser.OPS, false);
		while (st.hasMoreTokens()) {
			// 最小単位まで分割して、リストに追加する
			String subFormulaName = st.nextToken();
			Formula subFormula = getFormula(subFormulaName);
			if (subFormula != null) {
				// 重複であれば、１つとまとめ
				subFormulaMap.put(subFormulaName, subFormula);
			}
		}

		List<Formula> subFormulaList = new ArrayList<Formula>();
		subFormulaList.addAll(subFormulaMap.values());

		return subFormulaList;
	}

	/**
	 * タブに応じて、公式定義の中に属性accessableがtrueという公式を返す
	 * <br>デフォルートソートする
	 * 
	 * @return
	 */
    public List<Formula> getAccessableFormulaList(
			String cate) {
        List<Formula> list = this._getTopFormulas(cate);
		Collections.sort(list);
		return list;
	}

	/**
	 * 定義の計算記号より公式を返す
	 * 
	 * @param mark
	 * @return
	 */
    public Formula getFormula(String mark) {
        if (this == null)
			return null;
        return this.formulaPool.get(mark);
	}

	/**
	 * 指定の計算記号は定義されるかどうか
	 * 
	 * @param Mark
	 * @return
	 */
    public boolean isExist(String mark) {
        return this.formulaPool.containsKey(mark);
	}

    public void setFormulaFilterList(List<Formula> formulaFilterList) {
        this.formulaFilterList = formulaFilterList;
	}

    public boolean isFilterFormula(Formula f) {
        if (this.formulaFilterList == null)
			return false;

        for (Formula cur : this.formulaFilterList) {
			if (cur.getName().equals(f.getName())) {
				return true;
			}
		}

		return false;
	}

    public void setRounding(boolean isRounding) {
        this.isRounding = isRounding;
	}

    public boolean isRounding() {
        return this.isRounding;
	}
    
    /**
     * １つエントリーxmlファイルで当商品に関する算式定義XMLを統括しようにロードする
     * <p>算式が重複する可能性がある<p>
     * @param cates
     * @param conf 
     * @return
     * @throws ConfigurationException
     */
    private List<Formula> _getFormulaListByCates(HierarchicalConfiguration conf, String... cates) {
    	List<Formula> formulaList = new ArrayList<Formula>();
    	
    	for (String cate : cates) {
    		String key = cate + Const.DOT + FORMULA_XML_NODE;
    		int max = conf.getMaxIndex(key);
			for (int i = 0; i <= max; i++) {
				String subKey = key + "(" + i + ")";
				FormulaDeclaration decl = new FormulaDeclaration(conf, subKey);				
				Formula f = (Formula) BeanHelper.createBean(decl);
				formulaList.add(f);				
			}
    	}
    	
    	return formulaList;
		
    }

}
