package jp.co.nttdata.rate.model.datalayout;

import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.configuration.XMLConfiguration;
import org.apache.commons.configuration.beanutils.BeanFactory;
import org.apache.commons.configuration.beanutils.BeanHelper;
import org.apache.log4j.Logger;
import jp.co.nttdata.rate.log.LogFactory;
import jp.co.nttdata.rate.model.rateKey.rule.RateKeyConstraint;
import jp.co.nttdata.rate.model.rateKey.rule.RateKeyRule;
import jp.co.nttdata.rate.util.CommonUtil;
import jp.co.nttdata.rate.util.Const;
import jp.co.nttdata.rate.util.ResourceLoader;

/**
 * 
 * レートキーに関するUI情報およびキーの間の制御関係をマネージすること
 * @author btchoukug *
 */
public class DataLayoutManager {

	private static Logger logger = LogFactory.getInstance(DataLayoutManager.class);

	/** 唯一のインスタンス */
	private static DataLayoutManager INSTANCE;
		
	/** レートキーのBeanFactory名 */
	public static final String DATALAYOUT_BEANFACTORY = "DataLayoutBeanFactory";

	private static final String NODE_KEYS = "Category";	
	
	private XMLConfiguration config;	
	
	/**システム上のすべてのCategoryのリスト*/
	private List<InputDataCategory> categoryList = new ArrayList<InputDataCategory>();
	
	
	/**システム上のすべてのレートキーの制御関係*/
	private List<RateKeyConstraint> constraintList = new ArrayList<RateKeyConstraint>();
	
	/**商品毎に設定されたレートキールールである<br>
	 * （String:レートキー名；RateKeyRule:レートキーのルール）
	 * */
	private Map<String, RateKeyRule> namedRules = new HashMap<String, RateKeyRule>();
			
	/** レートキー定義およびルール、制御関係を管理する  */
	public DataLayoutManager(String insuranceCode) {
		
		//TODO factoryモードでグローバルと商品毎の公式定義、レートキー定義XML及びUI定義、計算基数はを読んでコンバインにする
		
		/**
		 * 商品毎にレートキーのルールと制御関係は違うため、定義は商品毎に行う必要
		 */
		URL xmlUrl = ResourceLoader.getExternalResource(Const.DATALAYOUT_DEF);
		URL dtdUrl = ResourceLoader.getExternalResource(Const.DATALAYOUT_DEF_DTD);
		this.config = CommonUtil.loadXML(xmlUrl, "Category", dtdUrl, false);
				
		//レートキーの定義情報を読み込む
		_editRateKeyDefs(insuranceCode);


	}
	
	public static synchronized void newInstance(String insuranceCode) {
		synchronized (DataLayoutManager.class) {
				INSTANCE = new DataLayoutManager(insuranceCode);
		}
	}
	
	private static DataLayoutManager getInstance() {
		if (INSTANCE == null) {
			throw new IllegalArgumentException("RateKeyManagerは初期化していません。");
		}
		return INSTANCE;
	}
	
	/**
	 * すべてレートキーの定義を取得してリストとして返す
	 */
	private void _editRateKeyDefs(String code) {
		
		int max = this.config.getMaxIndex(NODE_KEYS);
		
		//レートキーを一ずつ読み込む
		for (int i = 0; i <= max; i++) {
			String key = "Category("+ i +")";
			DataLayoutDeclaration decl = new DataLayoutDeclaration(config, key);
			BeanFactory factory = new DataLayoutBeanFactory();
			BeanHelper.registerBeanFactory(DATALAYOUT_BEANFACTORY, factory);
			
			InputDataCategory dl = (InputDataCategory) BeanHelper.createBean(decl);
			categoryList.add(dl);
		}
		
	}
	
//	/**
//	 * キーの制御関係を読み込む
//	 */
//	private void _editRateKeyConstraints() {
//
//		String[] conditions = this.config.getStringArray("Constraints.Constraint[@condition]");
//				
//		for (int i = 0; i < conditions.length; i++) {
//			String[] keys = this.config.getStringArray("Constraints.Constraint(" + i + ")[@keys]");
//			String desc = this.config.getString("Constraints.Constraint(" + i + ")[@desc]");
//			
//			
//			//それとも、XML専用Entity記号（例：>⇒&gt;）を使いますか。
//			RateKeyConstraint rkc = new RateKeyConstraint(keys, desc, conditions[i]);
//			//制御関係の条件のなかにOPを書き換え（例：leを>=に）
//			//RateKeyConstraint rkc = new RateKeyConstraint(keys, convertAbbreviation2OP(conditions[i]));
//			constraintList.add(rkc);
//		}
//		
//	}
	
//	/**
//	 * XMLから取得したレートキーのルールを「レートキー名：ルール」という形で保存する
//	 */
//	private void _editNamedRules() {
//		for (DataLayout dl : this.dataLayoutList) {
//			this.namedRules.put(dl.getName(), dl.getRule());
//		}
//	}
	
	/**
	 * すべてレートキーの定義をリストとして返す
	 * @return
	 */
	public static List<InputDataCategory> getAllDataLayoutDefs() {
		return getInstance().categoryList;
	}
	
	/**
	 * 指定のキーより、サブレートキーの定義リストを返す
	 * @param keys
	 * @return
	 */
	public static List<DataLayout> getRateKeyDefs(String cateName) {
		
		for (InputDataCategory category : getInstance().categoryList) {
			if (cateName.equals(category.getName())) {
				return category.getLayoutData();
			}
		}
		
		return null;
	}

	

	/**
	 * レートキー名に応じてルールを取得する
	 * @param keyName
	 * @return
	 */
	public static RateKeyRule getRateKeyRule(String keyName) {
		return getInstance().namedRules.get(keyName);
	}
	
	/**
	 * 指定のレートキーより、キーの間の制御関係を返す
	 * @return
	 */
	public static List<RateKeyConstraint> getKeyConstraints(String[] keys) {
		List<RateKeyConstraint> subKeyConstraints = new ArrayList<RateKeyConstraint>();
		for (RateKeyConstraint cons : getInstance().constraintList) {
			String[] constraintKeys = cons.getConstraintKeys();
			
			//当該制御関係に関するキーは全部指定のレートキーの範囲になると、有効な制御関係とする
			if (CommonUtil.containsOnly(constraintKeys, keys)) {
				subKeyConstraints.add(cons);
			}
		}
		return subKeyConstraints;
	}
	
}
