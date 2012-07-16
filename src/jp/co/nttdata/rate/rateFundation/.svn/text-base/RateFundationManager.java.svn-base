package jp.co.nttdata.rate.rateFundation;

import java.net.URL;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.configuration.XMLConfiguration;
import org.apache.commons.configuration.beanutils.BeanFactory;
import org.apache.commons.configuration.beanutils.BeanHelper;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import jp.co.nttdata.rate.exception.FmsDefErrorException;
import jp.co.nttdata.rate.exception.FmsRuntimeException;
import jp.co.nttdata.rate.fms.common.SystemFunctionUtility;
import jp.co.nttdata.rate.log.LogFactory;
import jp.co.nttdata.rate.rateFundation.IRateFundationProvider;
import jp.co.nttdata.rate.rateFundation.dbConnection.DBConnection;
import jp.co.nttdata.rate.rateFundation.dbConnection.DataRow;
import jp.co.nttdata.rate.rateFundation.RateFundationGroup.Sex;
import jp.co.nttdata.rate.rateFundation.RateFundationGroup.Xtimes;
import jp.co.nttdata.rate.util.AirthUtil;
import jp.co.nttdata.rate.util.CommonUtil;
import jp.co.nttdata.rate.util.Const;
import jp.co.nttdata.rate.util.Interpolation;
import jp.co.nttdata.rate.util.ResourceLoader;

/**
 * DBからFundation.xmlに定義された計算基数を読み込む
 * 
 * @author btchoukug
 */
public class RateFundationManager implements IRateFundationProvider {

	private static Logger logger = LogFactory.getInstance(RateFundationManager.class);

	private static IRateFundationProvider INSTANCE;
	
	/** 009と209商品に使われる基数dyとly */
	private static final String FUND_FEMALE_DY = "femaleDy";
	private static final String FUND_FEMALE_LY = "femaleLy";	
	private static final String FUND_MALE_DY = "maleDy";
	private static final String FUND_MALE_LY = "maleLy";	
	private static final String FUND_MALE_QY = "maleQy";
	private static final String FUND_FEMALE_QY = "femaleQy";

	/** 商品単位で世代、払方、PVH、配当など基数因子とマッピング検索SQL */
	private final static String FUNDATION_TBL_UNIT_SQL = 
		"select dividend, generation, payment, pvh, limit_sex, i, " +
		"rate_table_1, rate_table_2, rate_table_3, rate_table_4, qw from "
			+ "(SELECT fm.*, ifnull(qw.qw,0) as qw, qw.condition FROM v_ins_unit_fund_mapping fm "
			+ "left join "
			+ "qw_master qw "
			+ "on((qw.insurance_code = fm.insurance_code) and "
			+ "((qw.generation = fm.generation) or (qw.generation = 0))"
			+ " and ((qw.pvh = fm.pvh) or isnull(qw.pvh)))) main "
			+ "where main.insurance_code = ${code} ";
	
	/** 商品基数因子で基数TBL番号を検索SQL */
	private static final String FUND_TBL_NO_QUERY_SQL = 
		"select fund_tbl_no, x_times, fund_ptn from t_fund_def_mapping " +
			"where i = ${i} and qw=${qw} " +
			"and rate_table_1 =${rate_table_1} " +
			"and (rate_table_2 =${rate_table_2} or rate_table_2 is null) " +
			"and (rate_table_3 =${rate_table_3} or rate_table_3 is null) " +
			"and (rate_table_4 =${rate_table_4} or rate_table_4 is null) " + 
			"and x_times = ${x_times} " + 
			"and fund_ptn = ${fund_ptn}";

	/** 予定解約率を求めるSQL文 */
	private final static String QW_SQL = 
		"select qm.generation,qm.pvh,qm.condition,qm.qw "
			+ "from qw_master qm where insurance_code=${code};";
	
	/** 計算基数のXMLノード名 */
	private static final String FUNDATION_BEANFACTORY = "FundationBeanFactory";
	private static final String FUNDATION_PATTERN_KEY = "FundationPattern";
	private static final String FUNDATION_LOAD_KEY = "FundationLoad.Pattern";

	/** DBコラム名 */
	public static final String FIELD_FUND_TBL_NO = "fund_table_no";
	public static final String FIELD_AGE = "age";
	public static final String FIELD_SEX = "sex";
	public static final String FIELD_FUND_PTN = "fund_ptn";
	public static final String FIELD_X_TIMES = "x_times";
	public static final String FIELD_QW = "qw";
	public static final String FIELD_QX = "q";
	public static final String FIELD_LIMIT_SEX = "limit_sex";

	/** 特体の場合でも解約率を使う商品 */
	private static final String[] XTIMES_4_QW_INSURANCES = new String[]{"031","035","235","221"};

	private boolean isDefault = true;
		
	/** 当該計算対象商品、かつ入力レートキーに応じて計算基礎 */
	private FundationGroupDef curFundGroupDef;
	
	/** 予定解約率絞り込み条件リスト */
	private List<DataRow> qwConditionList;

	/** 基数パタン毎にSQL文を格納する */
	private Map<String, String> fundPtnSQL;
	private String[] sqlFundPtns;
	private String defaultSQL;
	
	/** 基数TBL番号毎に基数TBLを持つ */
	private Map<String, FundationTable> fundTblPool;
	
	/** グループキーと基数のマッピング */
	private Map<String, RateFundationGroup> fundTblMappings;
	
	/** 商品コード */
	private String insuranceCode;
	
	/** 普通体か特条かを判断するフラグ（デフォルトは普通体とする） */
	private boolean isSpecial = false;

	/** カレント商品に使われる基数パタンの配列 */
	private String[] curFundPtns;
	
	/** 基数パタン毎に標準体とするかないか配列 */
	private boolean[] curFundStdXtimeFixed;

	public static synchronized IRateFundationProvider getInstance() {
		if (INSTANCE == null) {
			throw new IllegalArgumentException("RateFundationManagerは初期化していません。");
		}
		return INSTANCE;
	}

	public RateFundationManager(String code) throws FmsDefErrorException {

		this.insuranceCode = code;		
		
		this.fundTblPool = new HashMap<String, FundationTable>();
		// 指定商品に該当する全部の基礎率マッピング
		this.fundTblMappings = new HashMap<String, RateFundationGroup>();
		
		//基数ロードSQLを取得
		_readSQLFromXMLDef();
		
		// 計算基礎定義をロード
		_getFundDefFromXML(insuranceCode);

		// 解約率の絞り込み条件をロード
		_loadQwMgrList(insuranceCode);
		
		DBConnection.getInstance().close();
		
		INSTANCE = this;
	}
	

	@Override
	public RateFundationGroup loadFundationGroup(String groupKey) throws FmsDefErrorException {

		RateFundationGroup rfg = fundTblMappings.get(groupKey);
		if (rfg == null) {
			String message = "基数グループキー[{0}]に応じて基数TBLが存在していない、DB設定ご確認ください";
			throw new FmsDefErrorException(MessageFormat.format(message, groupKey));
		}
		
		if (logger.isInfoEnabled()) {
			String fundTblNos = rfg.getFundTbls().values().toString();
			logger.info("[FUND]これから計算は以下の基数TBLを使いになります：" + fundTblNos + "@" + groupKey);
		}
		
		return rfg;

	}

	/**
	 * 計算基礎の定義をロードする
	 * 
	 * @param code
	 * @throws FmsDefErrorException 
	 */
	private void _getFundDefFromXML(String code) throws FmsDefErrorException {
		
		URL xmlUrl = ResourceLoader.getExternalResource(Const.FUNDATION_DEF);
		URL dtdUrl = ResourceLoader.getExternalResource(Const.FUNDATION_DEF_DTD);
		String publicId = "Fundation";

		XMLConfiguration config = CommonUtil.loadXML(xmlUrl, publicId, dtdUrl, true);

		// 商品コードに応じて基数グループを探す
		String[] fundGrpCodes = config.getStringArray("FundationGroup[@code]");
		
		String[] grpCodes = null;
		String groupKey = null;
		String groupDesc = null;
		int index = 0;
		for (String grpCode : fundGrpCodes) {
			/*
			 * 計算基礎グループのコードに指定の商品コードを含む場合、使用する基数パターンを取得
			 */
			if (grpCode.indexOf(code) > -1) {
				this.isDefault = false; // 当該商品に対して専用定義がある
				grpCodes = grpCode.split(Const.COMMA);
				groupKey = config.getString("FundationGroup(" + index
						+ ")[@groupkey]");
				groupDesc = config.getString("FundationGroup(" + index
						+ ")[@desc]");

				this.curFundPtns = config.getStringArray("FundationGroup("
						+ index + ").FundationPattern");
				this.curFundStdXtimeFixed = new boolean[this.curFundPtns.length];
				for (int i = 0; i < this.curFundPtns.length; i++) {
					this.curFundStdXtimeFixed[i] = config.getBoolean(
							"FundationGroup(" + index + ").FundationPattern("
									+ i + ")[@std-xtime-fixed]", false);
				}
				break;
			}
			index++;
		}	
		
		// 商品コードに応じた計算基礎定義が取得されなければ、デフォルト基礎を使う
		if (this.isDefault) {
			groupKey = config.getString("DefaultFundationGroup[@groupkey]");
			groupDesc = config.getString("DefaultFundationGroup[@desc]");

			this.curFundPtns = config.getStringArray("DefaultFundationGroup.FundationPattern");
			this.curFundStdXtimeFixed = new boolean[this.curFundPtns.length];
			for (int i = 0; i < this.curFundPtns.length; i++) {
				this.curFundStdXtimeFixed[i] = config.getBoolean(
						"DefaultFundationGroup.FundationPattern(" + i
								+ ")[@std-xtime-fixed]", false);
			}

		}

		// 基数グループキーの定義チェック
		if (StringUtils.isEmpty(groupKey)) {
			throw new FmsDefErrorException(code + "商品の基数定義" + groupDesc
					+ "にはグループキーが定義されていない");
		}

		// 基数グループを初期化
		this.curFundGroupDef = new FundationGroupDef();

		// コードやグループキーの編集を行う
		this.curFundGroupDef.setCode(grpCodes);
		this.curFundGroupDef.setGroupkey(groupKey);
		this.curFundGroupDef.setDesc(groupDesc);

		BeanFactory factory = new FundationBeanFactory();
		BeanHelper.registerBeanFactory(FUNDATION_BEANFACTORY, factory);

		// まず、上記で取得した基数パタン通りに基数定義を取得
		int size = this.curFundGroupDef.getPatternList().size();
		int max = config.getMaxIndex(FUNDATION_PATTERN_KEY);
		for (int i = 0; i <= max; i++) {

			String key = FUNDATION_PATTERN_KEY + "(" + i + ")";

			FundationDeclaration decl = new FundationDeclaration(config, key);
			FundationPatternDef fundPtn = (FundationPatternDef) BeanHelper
					.createBean(decl);

			// 所属の基数パタンであれば、基数グループに追加
			for (int j = 0; j < this.curFundPtns.length; j++) {
				if (fundPtn.getPtn().equals(this.curFundPtns[j])) {

					fundPtn.setStdXtimesFixed(this.curFundStdXtimeFixed[j]);
					this.curFundGroupDef.addPattern(fundPtn);

					// 定義されたパタンは全部ロードしたら、ループを中止する
					if (size == this.curFundPtns.length)
						break;
				}
			}

		}

	}

	/**
	 * 商品のグループ単位（世代、払込方法、配当）でレート計算基数の初期化
	 * 
	 * @param isSpecial 特体かないか
	 * @throws FmsDefErrorException  
	 */
	public void loadRateFundation(boolean isSpecial) throws FmsDefErrorException {

		try {

			// 基数TBL単位ごとに取得
			List<DataRow> unitParasList = DBConnection.getInstance().query(FUNDATION_TBL_UNIT_SQL, "code", this.insuranceCode);			

			// グループ毎にPVH基礎を一ずつ読み込む
			for (DataRow unitParas : unitParasList) {
				
				//基数グループを初期化
				RateFundationGroup rfg = new RateFundationGroup(curFundGroupDef);
				
				//特体の場合、４倍体の基数をロード
				if (isSpecial) {
					rfg.setXtimesVal(Xtimes.x4);
					if (!ArrayUtils.contains(XTIMES_4_QW_INSURANCES, this.insuranceCode)) {
						unitParas.put(RateFundationGroup.QW, 0d);	
					}
				}
				
				//基数因子を設定
				rfg.setFundTblParas(unitParas);
				
				for (Iterator<Entry<String, FundationTable>>  it = rfg.getFundTbls().entrySet().iterator(); it.hasNext();) {
					Entry<String, FundationTable> entry = it.next();
					String ptn = entry.getKey();
					FundationTable fundTbl = entry.getValue();
					
					//まず、外部SQL定義ファイルからパタンに応じるSQL文を取得
					String loadFundTblSql = _getSQLByPtn(ptn);
										
					Map<String, Object> fundTblQueryParas = rfg.getFundtionQueryParas();
					//災疾系商品にて、基数パタン１と２は標準体をとする
					if (fundTbl.getFundPtnDef().isStdXtimesFixed()) {
						fundTblQueryParas.put(FIELD_X_TIMES, Const.X_TIMES_1);	
					}	
					fundTblQueryParas.put(FIELD_FUND_PTN, ptn);
					
					// 基数パタンに対して、余計な因子を固定値にする
					if ("1".equals(ptn)) {
						//パターン１の基数TBLに対して、必ず解約率がゼロとなるため
						fundTblQueryParas.put(FIELD_QW, 0d);

					} else if ("2".equals(ptn)) {
						if ((Double)fundTblQueryParas.get(FIELD_QW) == 0d) {
							//パターン２　かつ　解約率がゼロの場合、パターン１の基数TBLを見なす
							fundTblQueryParas.put(FIELD_FUND_PTN, "1");
						}
					} else if ("9".equals(ptn)) {
						//基数パタン９ 基数TBL126、245２つが存在している
						_readFundationTable(unitParas, loadFundTblSql, fundTbl);
						continue;
					} else if ("009".equals(ptn) || "209".equals(ptn)) {
						//009と209は保険契約者の死亡率を使うため
						DataRow paras = new DataRow(unitParas);
						paras.put("code", this.insuranceCode);
						_readFundationTable(paras, loadFundTblSql, fundTbl);
						continue;
					}

					List<DataRow> fundTblNoList = DBConnection.getInstance().query(FUND_TBL_NO_QUERY_SQL, fundTblQueryParas);
					//マスタデータ件数チェック
					int size = fundTblNoList.size();
					if (size == 0) {
						String msg = "[FUND]基数マッピングTBL[t_fund_def_mapping]に該当する基数TBLが存在してない：" + fundTblQueryParas.toString();
						logger.warn(msg);

						//次のグループの編集へ
						continue;
					} else if (size == 1) {						
						
						//基数単位のパラメータをコピー
						DataRow paras = fundTblNoList.get(0);
						paras.putAll(unitParas);
						
						//ロード済みチェック(SQL単位で基数TBLを保存)
						String fundTblKey = Interpolation.interpolate(loadFundTblSql, paras) + "@" + ptn;
						if (logger.isDebugEnabled()) {
							logger.debug("基数TBLのキャッシュキー(SQL文+基数パタン)：" + fundTblKey);
						}
						if (fundTblPool.containsKey(fundTblKey)) {
							//同じ基数TBLが存在した場合、再び編集しない
							entry.setValue(fundTblPool.get(fundTblKey));
						} else {							
							_readFundationTable(paras, loadFundTblSql, fundTbl);
							//再利用するため、一旦保存する
							fundTblPool.put(fundTblKey, fundTbl);							
						}						
					} else {
						String msg = "[FUND]基数マッピングTBL[t_fund_def_mapping]に該当する複数基数TBLが存在している：" + rfg.getFundtionQueryParas();
						throw new FmsDefErrorException(msg);
					}
						
				}
								
				//グループキーを編集したうえ、マッピングに格納する
				String groupKey = rfg.getGroupKey();
				if (this.fundTblMappings.containsKey(groupKey)) {
					//重複している場合、合弁する
					rfg.mergeWithAnther(this.fundTblMappings.get(groupKey));
				}
				
				this.fundTblMappings.put(groupKey, rfg.merge());				
					
				}
			
				if (this.fundTblMappings.size() == 0) {
					throw new FmsDefErrorException(insuranceCode + "当該商品に応じて計算基礎は存在していません。");
				}


		} finally {
			// ＤＢ接続をクローズ
			DBConnection.getInstance().close();
		}
	}
	
	/**
	 * 009と209商品について、保険契約者の予定死亡率と生存者数を編集
	 * @param qyList
	 * @param fundTbl
	 * @throws FmsDefErrorException
	 */
	private void _editQyLy(List<DataRow> qyList, FundationTable fundTbl) throws FmsDefErrorException {
		
		
		double ly = 0;
		double dy = 0d;
		double lastDy = 0d;
		int i = 0, j = 0;
		String limitSex = null;
		
		for (DataRow qyData : qyList) {
			int sex = qyData.getInt(FIELD_SEX);
			int age = qyData.getInt(FIELD_AGE);
			double qy = qyData.getDouble(FIELD_QX);

			if (age == 0) {
				//性別制限を取得
				limitSex = qyData.getString(FIELD_LIMIT_SEX);
				// 生存者数の初期値を100000とする
				ly = 100000d;
				lastDy = 0d;
			}

			// 生存者数Lyを算出
			ly = SystemFunctionUtility.roundDown(AirthUtil.sub(ly, lastDy), 6);			
			// 死亡者数Dyを算出
			dy = SystemFunctionUtility.roundDown(AirthUtil.mul(ly, qy), 6);						
			lastDy = dy;

			if (sex == Const.SEX_MALE_0) {
				// 男性の場合
				fundTbl.setFund(FUND_MALE_QY, Sex.male, age, qy);
				fundTbl.setFund(FUND_MALE_DY, Sex.male, age, dy);
				fundTbl.setFund(FUND_MALE_LY, Sex.male, age, ly);
				fundTbl.setFund(FUND_MALE_QY, Sex.female, age, qy);
				fundTbl.setFund(FUND_MALE_DY, Sex.female, age, dy);
				fundTbl.setFund(FUND_MALE_LY, Sex.female, age, ly);
				i++;				
			} else {
				// 女性の場合
				fundTbl.setFund(FUND_FEMALE_QY, Sex.female, age, qy);
				fundTbl.setFund(FUND_FEMALE_DY, Sex.female, age, dy);
				fundTbl.setFund(FUND_FEMALE_LY, Sex.female, age, ly);
				fundTbl.setFund(FUND_FEMALE_QY, Sex.male, age, qy);
				fundTbl.setFund(FUND_FEMALE_DY, Sex.male, age, dy);
				fundTbl.setFund(FUND_FEMALE_LY, Sex.male, age, ly);
				j++;
			}

		}

		//　合弁するときに有効な基数TBLとするため、仮に基数TBL番号にゼロをセット
		fundTbl.setFundTblNo("0");
		
		// 男女の最終年齢ωを設定
		fundTbl.setMaleOmega(i-1);	//0からのため、最後ではマイナス１が必要
		fundTbl.setFemaleOmega(j-1);
		
		// 性別制限より編集
		if (StringUtils.isBlank(limitSex)) {
			return;
		}
		
		limitSex = StringUtils.lowerCase(limitSex);
		if (Const.MALE.equals(limitSex)) {
			Double[] qyArray = fundTbl.getMaleFundMap().get(FUND_MALE_QY);
			Double[] dyArray = fundTbl.getMaleFundMap().get(FUND_MALE_DY);
			Double[] lyArray = fundTbl.getMaleFundMap().get(FUND_MALE_LY);
			
			fundTbl.getMaleFundMap().put(FUND_FEMALE_QY, qyArray);
			fundTbl.getMaleFundMap().put(FUND_FEMALE_DY, dyArray);
			fundTbl.getMaleFundMap().put(FUND_FEMALE_LY, lyArray);
			fundTbl.getFemaleFundMap().put(FUND_FEMALE_QY, qyArray);
			fundTbl.getFemaleFundMap().put(FUND_FEMALE_DY, dyArray);
			fundTbl.getFemaleFundMap().put(FUND_FEMALE_LY, lyArray);
			fundTbl.setFemaleOmega(fundTbl.getMaleOmega());			
		} else if (Const.FEMALE.equals(limitSex)) {
			Double[] qyArray = fundTbl.getMaleFundMap().get(FUND_FEMALE_QY);
			Double[] dyArray = fundTbl.getMaleFundMap().get(FUND_FEMALE_DY);
			Double[] lyArray = fundTbl.getMaleFundMap().get(FUND_FEMALE_LY);
			
			fundTbl.getMaleFundMap().put(FUND_MALE_QY, qyArray);
			fundTbl.getMaleFundMap().put(FUND_MALE_DY, dyArray);
			fundTbl.getMaleFundMap().put(FUND_MALE_LY, lyArray);
			fundTbl.getFemaleFundMap().put(FUND_MALE_QY, qyArray);
			fundTbl.getFemaleFundMap().put(FUND_MALE_DY, dyArray);
			fundTbl.getFemaleFundMap().put(FUND_MALE_LY, lyArray);
			fundTbl.setMaleOmega(fundTbl.getFemaleOmega());
		} else {
			throw new FmsDefErrorException("性別制限(male or female)には正しく設定されてなかった：" + limitSex);
		}

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * jp.co.nttdata.rate.rateFundation.IRateFundationProvider#editFundationGroupKey
	 * (java.util.Map)
	 */
	@SuppressWarnings("unchecked")
	public String editFundationGroupKey(Map data) {
		String groupKeyRule = this.curFundGroupDef.getGroupkey() + RateFundationGroup.PVH_VAR;

		// PVHは大文字とする
		data.put(RateFundationGroup.PVH, StringUtils.upperCase((String) data.get(RateFundationGroup.PVH)));
		String groupKey = Interpolation.interpolate(groupKeyRule, data);
		
		if (logger.isDebugEnabled()) {
			logger.debug("編集した基数グループキー：" + groupKey);
		}
		
		return groupKey;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * jp.co.nttdata.rate.rateFundation.IRateFundationProvider#getCurrentGroupKeyDef
	 * ()
	 */
	public String getCurrentGroupKeyDef() {
		return this.curFundGroupDef.getGroupkey();
	}

	/**
	 * 基数パタンより基数ロードSQLを取得
	 * 
	 * @param ptn
	 */
	private String _getSQLByPtn(String ptn) {
				
		if (this.fundPtnSQL.containsKey(ptn)) {
			return this.fundPtnSQL.get(ptn);
		}
		
		// 商品コードに応じた基数ロードSQLが取得されなければ、デフォルトSQLを使う
		return defaultSQL;
	}
	
	/**
	 * 予めSQL定義XMLからSQL文を読込
	 * @throws FmsDefErrorException
	 */
	private void _readSQLFromXMLDef() throws FmsDefErrorException {
		this.fundPtnSQL = new HashMap<String, String>();
		
		URL xmlUrl = ResourceLoader.getExternalResource(Const.FUNDATIONLOAD_DEF);
		URL dtdUrl = ResourceLoader.getExternalResource(Const.FUNDATIONLOAD_DEF_DTD);
		String publicId = "FundationLoad";

		XMLConfiguration sqlConfig = CommonUtil.loadXML(xmlUrl, publicId, dtdUrl, true);
		sqlFundPtns = sqlConfig.getStringArray(FUNDATION_LOAD_KEY);
		defaultSQL = sqlConfig.getString("DefaultFundationLoad.SQL");
		
		// 基数パターンより基数ロードSQLを取得してから格納する
		for (int i = 0; i < sqlFundPtns.length; i++) {
			String[] ptns = sqlFundPtns[i].split(Const.COMMA);
			String strSQL = sqlConfig.getString("FundationLoad(" + i + ").SQL");
			for (String ptn : ptns) {
				this.fundPtnSQL.put(ptn, strSQL);
			}
		}
	}

	/**
	 * 初回以降の場合、DBから作成された基数を読み込む（男女両方）
	 * <p>基数因子に応じて基数TBLが存在していない場合、エラーとする</p>
	 * @param fundTblQueryData
	 * @param sql
	 * @param fundTbl
	 * @return
	 * @throws FmsDefErrorException
	 */
	private void _readFundationTable(DataRow fundTblQueryData, String sql, FundationTable fundTbl) throws FmsDefErrorException {
		
		if (fundTbl == null) {
			throw new IllegalArgumentException("基数TBLが初期化されてなかった");
		}

		List<DataRow> dataList = DBConnection.getInstance().query(sql, fundTblQueryData);
		
		if (dataList.size() == 0) {
			throw new FmsDefErrorException("基数因子に応じて基数TBLが存在していない:" + fundTblQueryData);
		}
		
		String curFundPtn = fundTbl.getFundPtnDef().getPtn();
		if (curFundPtn.equals("009") || curFundPtn.equals("209")) {
			_editQyLy(dataList, fundTbl);
			return;
		}	
		
		// 基数を男女別で、年齢・男女限定で編集
		int maleAge = 0, femaleAge = 0;

		// 基数TBL存在チェック
		DataRow head = dataList.get(0);
		if (!head.containsKey(FIELD_FUND_TBL_NO)) {
			throw new FmsRuntimeException("基数TBL番号は取得されなかった：" + sql);
		}
		if (!head.containsKey(FIELD_SEX) || !head.containsKey(FIELD_AGE)) {
			throw new FmsRuntimeException("基数編集には性別と年齢が必要だった：" + FIELD_SEX + "/" + FIELD_AGE);
		}
		
		// 基数TBL番号を設定
		fundTbl.setFundTblNo(head.getString(FIELD_FUND_TBL_NO));
		
		for (DataRow data : dataList) {					
			// 商品毎に計算基礎定義より、それぞれ男女の基数を編集
			if (data.getInt(FIELD_SEX) == Const.SEX_MALE_0) {
				maleAge =_editFundationTable(data, fundTbl, Sex.male);
			} else {
				femaleAge = _editFundationTable(data, fundTbl, Sex.female);
			}
		}
		
		// 男女の最終年齢ωを設定
		fundTbl.setMaleOmega(maleAge);
		fundTbl.setFemaleOmega(femaleAge);

	}

	/**
	 * DBから取得した値をもとに基数TBLを編集
	 * @param data
	 * @param rowNo
	 * @param fundTbl
	 * @param sex 
	 * @return next rowNo
	 * @throws FmsDefErrorException 
	 */
	private int _editFundationTable(DataRow data, FundationTable fundTbl, Sex sex) throws FmsDefErrorException {
		
		int age = data.getInt(FIELD_AGE);
		
		for (FundationDef fundDef : fundTbl.getFundPtnDef().getFundList()) {
			String fundColumnName = fundDef.getColumn();
			if (!data.containsKey(fundColumnName)) {
				throw new FmsDefErrorException("基数定義に応じて基数の値は取得できなかった：" + fundDef);
			}
			double fundVal = data.getDouble(fundColumnName);
			fundTbl.setFund(fundDef.getName(), sex, age, fundVal);			
		}
		
		return age;
		
	}


	/**
	 * qw_masterテーブルから指定商品の解約率を読み込む<br>
	 * 
	 * @param code
	 */
	private void _loadQwMgrList(String code) {
		qwConditionList = DBConnection.getInstance().query(QW_SQL, "code", code);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * jp.co.nttdata.rate.rateFundation.IRateFundationProvider#getQwConditonList
	 * ()
	 */
	public List<DataRow> getQwConditonList() {
		return this.qwConditionList;
	}

	public boolean isSpecial() {
		return isSpecial;
	}
	
	public String getInsuranceCode() {
		return insuranceCode;
	}

	public static void main(String args[]) {
		
		try {
			RateFundationManager rfm = new RateFundationManager("261");

			long t1 = System.currentTimeMillis();
			rfm.loadRateFundation(false);
			long t2 = System.currentTimeMillis();
			
			System.out.println("=========計算基礎のロードは" + (t2 - t1) + "ミリ秒かかる=========");
			
			for (Iterator<Entry<String, RateFundationGroup>> it = rfm.fundTblMappings
					.entrySet().iterator(); it.hasNext();) {
				Entry<String, RateFundationGroup> entry = it.next();
				System.out.println(entry.toString());
			}

		} catch (FmsDefErrorException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

}