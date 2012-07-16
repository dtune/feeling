package jp.co.nttdata.rate.model.rateKey;

import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.configuration.XMLConfiguration;
import org.apache.commons.configuration.beanutils.BeanFactory;
import org.apache.commons.configuration.beanutils.BeanHelper;
import org.apache.log4j.Logger;
import jp.co.nttdata.rate.exception.RateException;
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
public class RateKeyManager {

	private static Logger logger = LogFactory.getInstance(RateKeyManager.class);

	/** 唯一のインスタンス */
	private static RateKeyManager INSTANCE;
		
	/** レートキーのBeanFactory名 */
	public static final String RATE_KEY_BEANFACTORY = "RateKeyBeanFactory";

	private static final String NODE_KEYS = "RateKeys.RateKey";	
	
	private XMLConfiguration config;
	
	/** 商品コード */
	private String insuranceCode;

	/** 保障選択する場合、末尾付いたフラグを保持 */
	private String securityFlg;

	private XMLConfiguration[] securityConfigs;
	
	/**システム上のすべてのレートキーのリスト*/
	private List<RateKey> rateKeyList = new ArrayList<RateKey>();
	/**システム上のすべてのレートキーの制御関係*/
	private List<RateKeyConstraint> constraintList = new ArrayList<RateKeyConstraint>();
	
	/**商品毎に設定されたレートキールールである<br>
	 * （String:レートキー名；RateKeyRule:レートキーのルール）
	 * */
	private Map<String, RateKeyRule> namedRules = new HashMap<String, RateKeyRule>();
			
	/** レートキー定義およびルール、制御関係を管理する 
	 * @throws RateException */
	public RateKeyManager(String code) {

		this.insuranceCode = code.substring(0, 3);
		this.securityFlg = code.substring(3);
		
		/**
		 * 商品毎にレートキーのルールと制御関係は違うため、定義は商品毎に行う必要
		 */
		URL xmlUrl = ResourceLoader.getExternalResource(Const.RATEKEY_DEF_DIR + insuranceCode + Const.XML_SUFFIX);
		URL dtdUrl = ResourceLoader.getExternalResource(Const.RATEKEY_DEF_DTD);
		this.config = CommonUtil.loadXML(xmlUrl, "RateKey", dtdUrl, false);
		
		securityConfigs = new XMLConfiguration[Const.SECURITY_CATE_PREFIXS.length];
		
		//保障部分のカテゴリprefixを編集したうえで保障部分レートキーをロード
		for (int i = 0; i < Const.SECURITY_CATE_PREFIXS.length; i++) {
			if (this.securityFlg.indexOf(String.valueOf(i+1)) > -1) {
				xmlUrl = ResourceLoader.getExternalResource(Const.RATEKEY_DEF_DIR + Const.SECURITY_CATE_PREFIXS[i] + Const.XML_SUFFIX);
				if (xmlUrl == null) {
					throw new RuntimeException("レートキー定義ファイルがありません：" + Const.SECURITY_CATE_PREFIXS[i]);
				}
				this.securityConfigs[i] = CommonUtil.loadXML(xmlUrl, "RateKey", dtdUrl, false);					
			}
		}
						
		//レートキーの定義情報を読み込む
		this.rateKeyList = _editRateKeyDefs();
		
		//レートキーのソート順で並べる
		Collections.sort(this.rateKeyList, new RateKeySorter());
		
		_editNamedRules();
		
		//キーの間の制限関係を編集
		_editRateKeyConstraints();

	}
	
	public static synchronized void newInstance(String insuranceCode) {
		synchronized (RateKeyManager.class) {
				INSTANCE = new RateKeyManager(insuranceCode);
		}
	}
	
	private static synchronized RateKeyManager getInstance() {
		if (INSTANCE == null) {
			throw new IllegalArgumentException("RateKeyManagerは初期化していません。");
		}
		return INSTANCE;
	}
	
	/**
	 * すべてレートキーの定義を取得してリストとして返す
	 */
	private List<RateKey> _editRateKeyDefs() {
		
		List<RateKey> rateKeyList = new ArrayList<RateKey>();
		
		int max = this.config.getMaxIndex(NODE_KEYS);
		
		//レートキーを一ずつ読み込む
		for (int i = 0; i <= max; i++) {
			String key = "RateKeys.RateKey("+ i +")";
			RateKeyDeclaration decl = new RateKeyDeclaration(config, key);
			BeanFactory factory = new RateKeyBeanFactory();
			BeanHelper.registerBeanFactory(RATE_KEY_BEANFACTORY, factory);
			
			RateKey rk = (RateKey) BeanHelper.createBean(decl);
			rateKeyList.add(rk);
		}
		
		//　保障部分のレートキー情報を取得
		for (int i = 0; i < Const.SECURITY_CATE_PREFIXS.length; i++) {
			
			if (this.securityFlg.indexOf(String.valueOf(i+1)) > -1) {
				
				max = this.securityConfigs[i].getMaxIndex(NODE_KEYS);
				
				for (int j = 0; j <= max; j++) {
					String key = "RateKeys.RateKey("+ j +")";
					RateKeyDeclaration decl = new RateKeyDeclaration(this.securityConfigs[i], key);
					BeanFactory factory = new RateKeyBeanFactory();
					BeanHelper.registerBeanFactory(RATE_KEY_BEANFACTORY, factory);
					
					RateKey rk = (RateKey) BeanHelper.createBean(decl);
					rateKeyList.add(rk);
				}	
			}
		}
		
		return rateKeyList;
		
	}
	
	/**
	 * キーの制御関係を読み込む
	 */
	private void _editRateKeyConstraints() {

		String[] conditions = this.config.getStringArray("Constraints.Constraint[@condition]");
				
		for (int i = 0; i < conditions.length; i++) {
			String[] keys = this.config.getStringArray("Constraints.Constraint(" + i + ")[@keys]");
			String desc = this.config.getString("Constraints.Constraint(" + i + ")[@desc]");			
			
			//それとも、XML専用Entity記号（例：>⇒&gt;）を使いますか。
			RateKeyConstraint rkc = new RateKeyConstraint(keys, desc, conditions[i]);
			//制御関係の条件のなかにOPを書き換え（例：leを>=に）
			//RateKeyConstraint rkc = new RateKeyConstraint(keys, convertAbbreviation2OP(conditions[i]));
			constraintList.add(rkc);
		}
		
	}
	
	/**
	 * XMLから取得したレートキーのルールを「レートキー名：ルール」という形で保存する
	 */
	private void _editNamedRules() {
		for (RateKey rk : this.rateKeyList) {
			this.namedRules.put(rk.getName(), rk.getRule());
		}
	}
	
	/**
	 * すべてレートキーの定義をリストとして返す
	 * @return
	 */
	public static List<RateKey> getAllRateKeyDefs() {
		return getInstance().rateKeyList;
	}
	
	/**
	 * 指定のキーより、サブレートキーの定義リストを返す
	 * @param keys
	 * @return
	 */
	public static List<RateKey> getRateKeyDefs(String[] keyNames) {
		List<RateKey> subKeyDefs = new ArrayList<RateKey>();
		
		for (String key : keyNames) {
			for (int i = 0; i < getInstance().rateKeyList.size(); i++) {
				RateKey rk = getInstance().rateKeyList.get(i);
				if (key.equals(rk.getName())) {
					subKeyDefs.add(rk);
					break;
				}
			}
		}
		
		//キーの並び順
		Collections.sort(subKeyDefs, new RateKeySorter());
		
		return subKeyDefs;
	}
	
	/**
	 * 単にレートキーの定義を取得
	 * @param keyName
	 * @return
	 */
	public static RateKey getRateKeyDef(String keyName) {
		for (int i = 0; i < getInstance().rateKeyList.size(); i++) {
			RateKey rk = getInstance().rateKeyList.get(i);
			if (keyName.equals(rk.getName())) {
				return rk;
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
