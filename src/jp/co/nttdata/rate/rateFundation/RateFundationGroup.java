package jp.co.nttdata.rate.rateFundation;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import jp.co.nttdata.rate.exception.FmsDefErrorException;
import jp.co.nttdata.rate.exception.FmsRuntimeException;
import jp.co.nttdata.rate.fms.common.SystemFunctionUtility;
import jp.co.nttdata.rate.log.LogFactory;
import jp.co.nttdata.rate.rateFundation.dbConnection.DataRow;
import jp.co.nttdata.rate.util.Const;
import jp.co.nttdata.rate.util.Interpolation;

/**
 * 基数パタンごとに基数TBLを格納する
 * @author   btchoukug
 */
public class RateFundationGroup implements Cloneable  {
	
	private static Logger logger = LogFactory.getInstance(RateFundationGroup.class); 
	
	/**性別：男性、女性*/
	public enum Sex {male, female};
	
	/** X倍体:４倍体、１倍体 */
	public enum Xtimes {x4, x1}
	
	/**
	 * グループキー名
	 * Fundation.XMLに定義するとき、${generation}の形で
	 * パラメータとしても使われている
	 * */
	public static final String GEN = "generation"; //世代
	public static final String DIVIDEND = "dividend"; //配当有無
	public static final String PAYMENT = "payment"; //払込方法
	public static final String PVH = "pvh"; //計算基礎 
	/**基数グループキーのsuffix：計算基礎*/
	public static final String PVH_VAR = "${pvh}";
	
	/**予定利率*/
	public static final String RATE = "rate";	
	/**現価率*/
	public static final String V = "v";
	/**予定解約率*/
	public static final String QW = "qw";
	/**最終年齢*/
	public static final String OMEGA = "omega";
	/** x倍体 */
	private static final String X_TIMES = "x_times";
	
	/**特別条件付suffix(４倍体)*/
	public static final String SUFFIX_FUND_SPECIAL = "+X4";
	
	/**029、016、017専用対応：Wレート計算する際に、別の商品の基数を使うため*/
	public static final String SYS_FUND_CODE = "fundCode";
	
	private FundationGroupDef fundGroupDef;
	
	/** 基数グループキー*/
	private String groupKey;
	
	/** 予定利率 */
	private double interest = 0d;
	/** 現価率 */
	private double v = 0d;
	/** 解約率 */
	private double qw = 0d;	
	
	public FundationGroupDef getFundGroupDef() {
		return fundGroupDef;
	}

	/**
	 * グループキーの要素
	 * <br>基数TBLを決まる要素である
	 * <br>予定利率i、生命表No、予定解約率など
	 */ 
	private DataRow fundTblParas;
	
	/** パターン毎の基数TBL */
	private Map<String, FundationTable> fundTbls = new HashMap<String, FundationTable>();
	
	/** コンテキストにアクセスされるため、パターンマージした全量基数を格納するマップ */
	private Map<String, Double[]> maleFundMap = new HashMap<String, Double[]>();
	private Map<String, Double[]> femaleFundMap = new HashMap<String, Double[]>();
	
	private List<Integer> maleOmega = new ArrayList<Integer>(3);	//普通の場合、３パターンの基数TBLがある
	private List<Integer> femaleOmega = new ArrayList<Integer>(3);
	
	/** X倍体（普通が1倍体（標準体）；特体が4倍体） */
	private Xtimes xtimesVal = Xtimes.x1;
	
	public RateFundationGroup(FundationGroupDef fundGrpDef) {
		this.fundGroupDef = fundGrpDef;
		List<FundationPatternDef> fundPtnList = this.fundGroupDef.getPatternList();
		
		// 基数TBLはパタン毎に初期化する
		for (FundationPatternDef fundPtnDef : fundPtnList) {
			fundTbls.put(fundPtnDef.getPtn(), new FundationTable(fundPtnDef));
		}
	}
	
	/**
	 * FUNDATION XMLに定義されたgroupkeyの形（${generation}${payment}${dividend}${qw}のような形）に従って、
	 * 入力レートキーとPVHという組み合わせより基数コントロールの グループキーを作る
	 * <p>注：現時点では世代、払込方法、配当、解約率、種類PVHをグループキーとする</P>
	 * @return
	 */
	public String getGroupKey() {
		if (this.fundTblParas == null) {
			throw new IllegalArgumentException("基数因子が設定されなかった");
		}
		String groupKeyRule = this.fundGroupDef.getGroupkey() + PVH_VAR;
		// PVHは大文字とする
		this.fundTblParas.put(PVH, StringUtils.upperCase((String) this.fundTblParas.get(PVH)));
		this.groupKey = Interpolation.interpolate(groupKeyRule, this.fundTblParas);			
		
		if (this.xtimesVal == Xtimes.x4) {
			//特体の場合、suffix「+special」を追加
			this.groupKey += SUFFIX_FUND_SPECIAL;
		}
		
		return this.groupKey;
	}
	
	public Map<String, Double[]> getMaleFundMap() {
		return maleFundMap;
	}

	public Map<String, Double[]> getFemaleFundMap() {
		return femaleFundMap;
	}

	public double getInterest() {
		return interest;
	}

	public double getV() {
		return v;
	}

	public double getQw() {
		return qw;
	}
		
	public DataRow getFundTblParas() {
		return fundTblParas;
	}

	public void setFundTblParas(DataRow paras) {
		this.fundTblParas = paras;
		
		// システム項目「予定利率」をセット
		this.interest = paras.getDouble("i");
		if (this.interest == 0d) {
			throw new FmsRuntimeException("予定利率の値は取得できなかった：" + paras.toString());
		}

		// システム項目「現価率v」が予定利率iより計算してセット
		this.v = SystemFunctionUtility.roundDown(1d / (this.interest + 1d), 11);

		// システム項目「予定解約率qw」をセット
		this.qw = paras.getDouble(QW);
	}
	
	public Map<String, FundationTable> getFundTbls() {
		return fundTbls;
	}

	public void setFundTbls(Map<String, FundationTable> fundTbls) {
		this.fundTbls = fundTbls;
	}
	
	public FundationTable getFundationTable(String ptn) throws FmsDefErrorException {
		if (!fundTbls.containsKey(ptn)) {
			throw new FmsDefErrorException("該当パタンの基数が定義されてなかった：" + ptn);
		}
		return fundTbls.get(ptn);
	}
	
	/**
	 * 基数クエリーSQLのパラメータを作成
	 * @return
	 */
	public Map<String, Object> getFundtionQueryParas() {
		Map<String, Object> paras = new HashMap<String, Object>(this.fundTblParas);
		
		paras.put(X_TIMES, this.xtimesVal == Xtimes.x1 ? Const.X_TIMES_1 : Const.X_TIMES_4);		
		return paras;
	}

	/**
	 * @param xtimesVal the xtimesVal to set
	 */
	public void setXtimesVal(Xtimes xtimesVal) {
		this.xtimesVal = xtimesVal;
	}

	/**
	 * @return the xtimesVal
	 */
	public Xtimes getXtimesVal() {
		return xtimesVal;
	}

	public String toString() {
		StringBuffer sb = new StringBuffer("{");
		sb.append(this.groupKey).append(":")
		.append(fundTbls.values().toString())
		.append("}");
		
		return sb.toString();
	}
	
	/**
	 * 同じグループキーの場合、基数パターンごとに基数TBL単位で合弁する
	 * <p>空いた基数TBLが合弁対象とする。</p>
	 * @param rfg
	 * @throws FmsDefErrorException 
	 */
	public void mergeWithAnther(RateFundationGroup rfg) throws FmsDefErrorException {
		if (!this.groupKey.equals(rfg.groupKey)) {
			throw new FmsDefErrorException("同じグループキーではないため、合弁できません：" + this.groupKey + ":" + rfg.groupKey);
		}
		
		for (FundationPatternDef fundPtnDef : this.fundGroupDef.getPatternList()) {
			String ptn = fundPtnDef.getPtn();
			if (this.fundTbls.get(ptn).isEmpty()) {
				this.fundTbls.put(ptn, rfg.getFundTbls().get(ptn));
			}
		}
	}

	/**
	 * 基数パターンごとに基数TBL単位で合弁する
	 * <p>最後ではぞれぞれの物理的な基数TBLを１つロジック的な基数TBLにする</p>
	 * @return RateFundationGroup
	 * @throws FmsDefErrorException 
	 */
	public RateFundationGroup merge() throws FmsDefErrorException {
		//パターン単位の基数Mapをアクセスしやすいため１つに合弁する
		for(FundationTable ptnTbl : this.fundTbls.values()){
			this.maleFundMap.putAll(ptnTbl.getMaleFundMap());
			this.femaleFundMap.putAll(ptnTbl.getFemaleFundMap());
			this.maleOmega.add(ptnTbl.getMaleOmega());
			this.femaleOmega.add(ptnTbl.getFemaleOmega());
		}
		
		//制限条件(性別)より編集を行う
		_editFundtionByLimitCondtion(this.fundTblParas);
		
		//昇順ソート
		Collections.sort(this.femaleOmega);
		Collections.sort(this.maleOmega);
		
		return this;
	}

	/**
	 * パターンごとに最小の最終年齢を返す
	 * @return
	 */
	public int getOmega(int sex) {
		
		int omega = 0;
		
		if (Const.SEX_MALE_0 == sex) {
			//パタンごとの基数TBKのうちに最小のωを返す
			omega = this.maleOmega.get(0);
		} else {
			omega = this.femaleOmega.get(0);
		}

		return omega;
	}
	
	/**
	 * 計算記号は計算基礎か否か判断する
	 * 
	 * @param tokenName
	 * @return
	 */
	public boolean isFundation(String tokenName) {
		return this.fundGroupDef.getFundationNameList().contains(tokenName);
	}
	
	/**
	 * 基数TBLロードしてから性別制限より基数の編集を行う
	 * 
	 * @param rfg
	 * @param data
	 * @throws FmsDefErrorException 
	 */
	private void _editFundtionByLimitCondtion(DataRow paras) throws FmsDefErrorException {
		
		String limitSex = paras.getString(RateFundationManager.FIELD_LIMIT_SEX);
		if (StringUtils.isBlank(limitSex)) {
			return;
		}
		
		//比較するため、小文字にする
		limitSex = StringUtils.lowerCase(limitSex);
		
		// 性別限定ありの場合
		if (StringUtils.isNotBlank(limitSex)) {
			if (logger.isInfoEnabled()) {
				logger.info(MessageFormat.format("{0}には性別限定あり：{1}", this.fundTbls.values() + "@" + this.groupKey, limitSex));
			}
			if (Const.MALE.equals(limitSex)) {
				// 男性とする
				this.femaleFundMap = this.maleFundMap;
			} else if (Const.FEMALE.equals(limitSex)) {
				// 女性とする
				this.maleFundMap = this.femaleFundMap;
			} else {
				throw new FmsDefErrorException("性別制限には正しく設定されてなかった：" + limitSex);
			}
		}
	}
	
}
