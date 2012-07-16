package jp.co.nttdata.rate.fms.calculate;

import java.text.MessageFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;

import jp.co.nttdata.rate.exception.FmsRuntimeException;
import jp.co.nttdata.rate.log.LogFactory;
import jp.co.nttdata.rate.rateFundation.dbConnection.DBConnection;
import jp.co.nttdata.rate.rateFundation.dbConnection.DataRow;
import jp.co.nttdata.rate.util.CommonUtil;
import jp.co.nttdata.rate.util.Const;

import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

/**
 * 配当金計算向けの配当率などを取得
 * @author zhanghy
 *
 */
public class DividendContextPlugin extends AbstractContextPlugin {

	protected Logger logger = LogFactory.getInstance(DividendContextPlugin.class);
	
	/**保険期間*/
	private static final String RATEKEY_N = "n";
	/**払込期間*/
	private static final String RATEKEY_M = "m";
	/** 払方（回数） */
	private static final String RATEKEY_PAYMENT = "kaisu";
	/** 一部一時払いを判明する項目 */
	private static final String RATEKEY_PARTONETIME = "partOnetime";
	/**繰り下げ年数*/
	public static final String RATEKEY_L = "l";
	/**経過年*/
	private static final String RATEKEY_T = "t";
	private static final String RATEKEY_T1 = "t1";
	private static final String RATEKEY_T2 = "t2";
	private static final String RATEKEY_T3 = "t3";
	private static final String RATEKEY_POLICYT = "policyT";
	/**延長年数*/
	private static final String RATEKEY_TEX = "tEX";
	/**経過月*/
	private static final String RATEKEY_F = "f";
	private static final String RATEKEY_F1 = "f1";
	private static final String RATEKEY_F2 = "f2";
	private static final String RATEKEY_F3 = "f3";
	/**延長月数*/
	private static final String RATEKEY_FEX = "fEX";
	/**状態*/
	private static final String RATEKEY_STATE = "state";
	/**払込回数*/
	private static final String RATEKEY_KAISU = "kaisu";
	/**割当済み分配額を計算するとき、ｔの状態*/
	private static final String RATEKEY_STATETEMP = "stateTemp";
	/**配当金計算年数*/
	private static final String RATEKEY_DIVIDENDYEAR = "dividendYear";
	/**配当計算開始日（契約年月日）*/
	private static final String RATEKEY_RATE_STARTDATE = "contractDate";
	/**責任開始年月日*/
	private static final String RATEKEY_RATE_SEKINI_DATE = "responsibilityDate";
	/**主契約の契約日*/
	private static final String RATEKEY_RATE_POLICYSTARTDATE = "policyContractDate";
	/**主契約の払方*/
	private static final String RATEKEY_RATE_POLICYKAISU = "policyKaisu";
	/**配当基準日*/
	private static final String RATEKEY_RATE_ENDDATE = "divEndDate";
	/**配当率*/
	private static final String RATEKEY_DIV_ID = "id";
	/**配当積立率*/
	private static final String RATEKEY_DIV_ID_NASHU = "id_nashu";
	/**アセットシェア率*/
	private static final String RATEKEY_DIV_ACCID = "accId";
	/**アセットシェア積立率*/
	private static final String RATEKEY_DIV_ACCID_NASHU = "accId_nashu";
	/**積立利率*/
	private static final String RATEKEY_DIV_RATEA = "rateA";
	private static final String RATEKEY_DIV_RATEB = "rateB";
	/**特約付加の主契約コード*/
	private static final String RATEKEY_POLICYCODE = "policyCode";
	/**前事業年度末から効力発生日までに,各日付を迎えるかどうか*/
	private static final String RATEKEY_ISCOMEOFDATE = "isComeOfDate";
	/**前事業年度末を迎えていないかどうか*/
	private static final String RATEKEY_ISANNUITYBEGIN = "isAnnuityBegin";
	/**当該事業年度中に保険期間の終了を迎えるかどうか*/
	private static final String RATEKEY_ISENDOFN = "isEndOfN";
	/**4/1を経過するかどうかを判断する*/
	private static final String RATEKEY_DIV_ISVALUECHANGED = "isValueChanged";
	/**積立利率変動日*/
	private static final String RATEKEY_DIV_CHANGEDATEA = "changeDateA";
	private static final String RATEKEY_DIV_CHANGEDATEB = "changeDateB";
	/**養育年金部分区分*/
	private static final String RATEKEY_YOUIKUNENKIN = "youikunenkin";
	/**年金支払開始年月日*/
	private static final String RATEKEY_ANNUITY_BEGIN_DATE = "AnnuityBeginDate";
	/**効力発生日*/
	private static final String RATEKEY_EFFECT_BEGIN_DATE = "effectBeginDate";
	/**旧年金買増年月日*/
	private static final String RATEKEY_OLDANNUITY_BUY_DATE = "oldAnnuityBuyDate";
	/**最新年金買増年月*/
	private static final String RATEKEY_NEWANNUITY_BUY_DATE = "newAnnuityBuyDate";
	/**養育年金支払経過年*/
	private static final String RATEKEY_YOUIKU_ANNUITY_T = "youikuAnnuityT";
	/**養育年金支払経過月*/
	private static final String RATEKEY_YOUIKU_ANNUITY_F = "youikuAnnuityF";
	/**状態変更フラグ*/
	private static final String RATEKEY_STATE_CHANGED = "StateChanged";
	/**加算額の状態判断*/
	private static final String RATEKEY_STATE_DT_PLUS = "StateDtPlus";
	/**年金支払期間*/
	private static final String RATEKEY_G = "g";
	/**年金の種類*/
	private static final String RATEKEY_ANNUITYTYPE = "annuityType";
	/**分配日と契約日が同じのフラグ*/
	private static final String RATEKEY_RATE_XN = "xN";
	/**計算基準日*/
	private static final String RATEKEY_RATE_STANDARDDATE = "standardDate";
	/**計算対象*/
	private static final String RATEKEY_RATE_KEISANPTN = "keisanPtn";
	/**異動状態*/
	private static final String RATEKEY_RATE_CHANGESTATE = "changeState";
	/**契約現況*/
	private static final String RATEKEY_RATE_CONSTATE = "contractorState";
	/**当事業年度末年月日*/
	private static final String RATEKEY_RATE_DIVTHISYEAR = "dividendThisYear";
	/**前事業年度末年月日*/
	private static final String RATEKEY_RATE_DIVLASYEAR = "dividendLastYear";
	/**商品コード*/
	private static final String RATEKEY_RATE_INSCODE = "insuranceCode";
	
	/** 配当率を求めるSQL文 */
	private final static String DIVIDEND_SQL = "SELECT di.account_code,di.term_code,di.policy_year,di.payment,di.validate_from,"
		+"di.validate_to,di.bonus_year,di.rate,di.dividend_rate,di.acc_rate,di.acc_div_rate "
		+ "FROM `rate_dividend_master` di where di.insurance_code=${code};";
	
	/** 期間区分を求めるSQL文 */
	private final static String TERM_SQL = "SELECT term.condition,term.term_code "
			+ "FROM `rate_term_master` term where term.insurance_code=${code};";
	
	/** 配当率リスト */
	private List<DataRow> dividendList = null;
	/** 期間区分リスト */
	private List<DataRow> termConditonlist = null;
	
	public DividendContextPlugin(RateCalculateContext ctx) {
		super(ctx);
		termConditonlist = DBConnection.getInstance().query(TERM_SQL, "code", ctx.insuranceCode);
		if (termConditonlist.size() == 0) {
			throw new FmsRuntimeException("期間区分テーブルにはマスタデータが設定されてなかった");
		}
		dividendList = DBConnection.getInstance().query(DIVIDEND_SQL, "code", ctx.insuranceCode);
		if (dividendList.size() == 0) {
			throw new FmsRuntimeException("配当率テーブルにはマスタデータが設定されてなかった");
		}
		
		DBConnection.getInstance().close();
	}

	@SuppressWarnings("unchecked")
	@Override
	public void contextHandle() {
		
		//始期年月日
		double contractDate = MapUtils.getDoubleValue(_ctx.tokenValueMap, RATEKEY_RATE_STARTDATE);
		//責任開始年月日
		double sekiniDate = MapUtils.getDoubleValue(_ctx.tokenValueMap, RATEKEY_RATE_SEKINI_DATE);
		//xtime
		_ctx.tokenValueMap.put(RateCalculateContext.RATEKEY_XTIME, Const.X_TIMES_DEFAULT);
		
		//配当計算、特約の場合責任開始日と始期年月日大きい方をもとに世代を算出
		_ctx.tokenValueMap.put(RateCalculateContext.RATEKEY_GEN, justifyGeneration(Integer.parseInt(_ctx.insuranceCode) > 300 && sekiniDate > contractDate ? sekiniDate : contractDate));
		
		if (_ctx.tokenValueMap.containsKey(RATEKEY_RATE_ENDDATE)) {
			if ("931".equals(_ctx.insuranceCode)) {
				_ctx.tokenValueMap.put(RATEKEY_RATE_INSCODE, _ctx.insuranceCode);
				_ctx.tokenValueMap.put(RATEKEY_STATE, 3);
				_ctx.tokenValueMap.put(RATEKEY_M, 0);
			}
			// 計算基準日を設定
			_ctx.tokenValueMap.put(RATEKEY_RATE_STANDARDDATE, 
					MapUtils.getInteger(_ctx.tokenValueMap, RATEKEY_RATE_ENDDATE));
			// t,f,t1,f1を設定
			_editElapsedYearMonth(_ctx.tokenValueMap, true);
			// 配当率を取得する
			_editDividendRateValue(_ctx.tokenValueMap);
			// 積立利率を取得する
			_editDivRateValue(_ctx.tokenValueMap);
			// 当該事業年度中に保険期間の終了を迎えるかどうかを判定
			_editIsEndOfN(_ctx.tokenValueMap);
			if ("004".equals(_ctx.insuranceCode) || "042".equals(_ctx.insuranceCode)
					|| "308".equals(_ctx.insuranceCode)) {
				// 年金払い開始後、年金払込年月を取得する
				_editAnnuityPaidYearMonth(_ctx.tokenValueMap);
			}
			/* 
			 * 養育年金経過月の設定
			 * 養育年金部分かどうかの判定がメソッドの中にいる
			 */
			if ("009".equals(_ctx.insuranceCode)){
				_editAnnuityBeginMonth(_ctx.tokenValueMap);
			}
			/*
			 * 当該事業年度中に以下のいずれもが発生したかとうか判定する
			 * 保険料払込期間の終了
			 * 年金支払いの開始
			 * 年金繰り下げ期間の開始
			 * 保険期間の終了(削除)
			 */
			_editStateChange(_ctx.tokenValueMap);
			// 計算対象を取得する
			int keisanPtn = MapUtils.getIntValue(_ctx.tokenValueMap, RATEKEY_RATE_KEISANPTN);
			
			// 前事業年度末から効力発生日までに,各日付を迎えるかどうかを判定
			_editIsComeOfDate(_ctx.tokenValueMap, keisanPtn);
			// 前事業年度末を迎えていないかどうか
			_editIsAnnuityBegin(_ctx.tokenValueMap, keisanPtn);
			// 加算額の状態判断
			_editDtNashuPlus(_ctx.tokenValueMap, keisanPtn);
			// 割当済み分配額を計算するとき、ｔの状態を設定する
			_editStateTemp(_ctx.tokenValueMap);
			if (logger.isInfoEnabled()) {
				logger.info("入力のレートキー（配当率など取得後）：" + _ctx.tokenValueMap.toString());
			}
		}
	}
	
	/**
	 * 割当済み分配額を計算するとき、ｔの状態を設定する
	 * @param rateKeyMap
	 */
	@SuppressWarnings("unchecked")
	private void _editStateTemp(Map rateKeyMap){
		int t = MapUtils.getInteger(rateKeyMap, RATEKEY_T);
		int t1 = MapUtils.getInteger(rateKeyMap, RATEKEY_T1);
		if (t != t1) {
			int m = MapUtils.getInteger(rateKeyMap, RATEKEY_M);
			int stateTemp = _getStateTemp(t, m, rateKeyMap);
			rateKeyMap.put(RATEKEY_STATETEMP, stateTemp);
		} else {
			// 割当済み分配額を計算するとき、ｔの状態デフォルトの場合、入力状態を設定
			rateKeyMap.put(RATEKEY_STATETEMP, MapUtils.getInteger(rateKeyMap, RATEKEY_STATE));
		}
	}
	
	/**
	 * レートキーより、ｔの状態を判断する
	 * @param t
	 * @param m
	 * @param rateKeyMap
	 * return
	 */
	@SuppressWarnings("unchecked")
	private int _getStateTemp(int t, int m, Map rateKeyMap){
		
		int n = 99;
		if (MapUtils.getInteger(rateKeyMap, RATEKEY_N) != null) {
			n = MapUtils.getInteger(rateKeyMap, RATEKEY_N);
		}
		
		if ("004".equals(_ctx.insuranceCode) || "042".equals(_ctx.insuranceCode)) {
			int l = 0;
			if(rateKeyMap.containsKey(RATEKEY_L)){
				l = MapUtils.getInteger(rateKeyMap, RATEKEY_L);
			}
			if (t >= n + l) {// 年金開始後
				return 3;
			} else if (t >= n && n + l > t) {// 繰り下げ
				return 5;
			}
		}
		if ("308".equals(_ctx.insuranceCode)) {
			int annuityBeginDate = MapUtils.getInteger(rateKeyMap, RATEKEY_ANNUITY_BEGIN_DATE);
			if (annuityBeginDate > 0 && t >= n) {// 年金開始後
				return 3;
			} else {
				return 2;
			}
		}
		int kaisu = MapUtils.getInteger(rateKeyMap, RATEKEY_KAISU);
		// 払込期間中
		if (t < m) {
			return 1;
		} else if (m == n && t >= m || m != n && (t >= m && n > t) || (m == 0 && kaisu == 1)) {// 払込期間終了後
			return 2;
		}
		throw new FmsRuntimeException("当データの状態が存在していません。");
	}
	
	/**
	 * 当該事業年度中に契約状態変更が発生したかとうか判定する
	 * @param rateKeyMap
	 */
	@SuppressWarnings("unchecked")
	private void _editStateChange(Map rateKeyMap){
		int t = MapUtils.getInteger(rateKeyMap, RATEKEY_T);
		
		if("004".equals(_ctx.insuranceCode) || "042".equals(_ctx.insuranceCode)){
			_editStateChange004(rateKeyMap, t);
		}else if("009".equals(_ctx.insuranceCode)){
			_editStateChange009(rateKeyMap, t);
		}else if("308".equals(_ctx.insuranceCode)){
			_editStateChange308(rateKeyMap, t);
		}else{
			_editStateChangeOther(rateKeyMap, t);
		}
	}
	/**
	 * 商品004用
	 * 当該事業年度中に契約状態変更が発生したかとうか判定する
	 * @param rateKeyMap
	 * @param t
	 */
	@SuppressWarnings("unchecked")
	private void _editStateChange004(Map rateKeyMap, int t){
		int n = MapUtils.getInteger(rateKeyMap, RATEKEY_N);
		int m = MapUtils.getInteger(rateKeyMap, RATEKEY_M);
		int l = MapUtils.getInteger(rateKeyMap, RATEKEY_L);
		if(t == m){
			//保険料払込期間の終了
			rateKeyMap.put(RATEKEY_STATE_CHANGED, 1);
		}else if(t == n+l){
			//年金支払いの開始
			rateKeyMap.put(RATEKEY_STATE_CHANGED, 1);
		}else if(l != 0 && t == n){
			//年金繰り下げ期間の開始
			rateKeyMap.put(RATEKEY_STATE_CHANGED, 1);
		}else{
			//以上いずれも発生しない
			rateKeyMap.put(RATEKEY_STATE_CHANGED, 0);
		}
	}
	/**
	 * 商品009用
	 * 当該事業年度中に契約状態変更が発生したかとうか判定する
	 * @param rateKeyMap
	 * @param t
	 */
	@SuppressWarnings("unchecked")
	private void _editStateChange009(Map rateKeyMap, int t){
		int m = MapUtils.getInteger(rateKeyMap, RATEKEY_M);
		
		if(t == m){
			//払込満了で保険料払込期間の終了
			rateKeyMap.put(RATEKEY_STATE_CHANGED, 1);
		}else{
			//以上いずれも発生しない
			rateKeyMap.put(RATEKEY_STATE_CHANGED, 0);
		}
	}
	/**
	 * 商品308用
	 * 当該事業年度中に契約状態変更が発生したかどうか判定する
	 * @param rateKeyMap
	 * @param t
	 */
	@SuppressWarnings("unchecked")
	private void _editStateChange308(Map rateKeyMap, int t){
		int m = MapUtils.getInteger(rateKeyMap, RATEKEY_M);
		int state = MapUtils.getInteger(rateKeyMap, RATEKEY_STATE);
		
		if(state != 3 && t == m){
			//払込満了で保険料払込期間の終了
			rateKeyMap.put(RATEKEY_STATE_CHANGED, 1);
		}else{
			//以上いずれも発生しない
			rateKeyMap.put(RATEKEY_STATE_CHANGED, 0);
		}
	}
	
	/**
	 * 他の商品用
	 * 当該事業年度中に契約状態変更が発生したかとうか判定する
	 * @param rateKeyMap
	 * @param t
	 */
	@SuppressWarnings("unchecked")
	private void _editStateChangeOther(Map rateKeyMap, int t){
		int m = MapUtils.getInteger(rateKeyMap, RATEKEY_M);
		if(t == m){
			//保険料払込期間の終了
			rateKeyMap.put(RATEKEY_STATE_CHANGED, 1);
		}else{
			rateKeyMap.put(RATEKEY_STATE_CHANGED, 0);
		}
	}
	
	/**
	 * 加算額の計算
	 * 満期消滅、○年毎の分配、年金支払期間満了（確定年金（004））の状態を判断する
	 * 0:条件合います　1：条件合いません
	 */
	@SuppressWarnings("unchecked")
	private void _editDtNashuPlus(Map rateKeyMap, int keisanPtn){

		rateKeyMap.put(RATEKEY_STATE_DT_PLUS, 1);
		if (keisanPtn == 3) {
			// 異動状態を取得する
			int changeState = MapUtils.getIntValue(rateKeyMap, RATEKEY_RATE_CHANGESTATE);
			// 異動状態ではない
			if (changeState == 2) {
				int t1 = MapUtils.getInteger(rateKeyMap, RATEKEY_T1);
				int f1 = MapUtils.getInteger(rateKeyMap, RATEKEY_F1);
				int xN = MapUtils.getInteger(rateKeyMap, RATEKEY_RATE_XN);
				int n = 99;
				if (MapUtils.getInteger(rateKeyMap, RATEKEY_N) != null) {
					n = MapUtils.getInteger(rateKeyMap, RATEKEY_N);
				}
				int dividendYear = MapUtils.getInteger(rateKeyMap, RATEKEY_DIVIDENDYEAR);
				
				if ((t1 + 1 == n) && (f1 == 12)) {
					//満期消滅
					rateKeyMap.put(RATEKEY_STATE_DT_PLUS, 0);
				} else if((t1+1)%dividendYear == 0 && (f1 == 12) && xN == 1) {
					//○年毎の分配
					rateKeyMap.put(RATEKEY_STATE_DT_PLUS, 0);
				} else if ("004".equals(_ctx.insuranceCode)) {
					int annuityType = MapUtils.getInteger(rateKeyMap, RATEKEY_ANNUITYTYPE);
					int state = MapUtils.getInteger(rateKeyMap, RATEKEY_STATE);
					if (annuityType == 4 && state == 3) {
						int annuityBeginDate = MapUtils.getInteger(rateKeyMap, RATEKEY_ANNUITY_BEGIN_DATE);
						int g = MapUtils.getInteger(rateKeyMap, RATEKEY_G);
						int divEndDate = MapUtils.getInteger(rateKeyMap, RATEKEY_RATE_ENDDATE);
						if (divEndDate >= (annuityBeginDate + g*10000)) {
							//年金支払期間満了（確定年金（004））
							rateKeyMap.put(RATEKEY_STATE_DT_PLUS, 0);
						}
					}
				}
			}
		}
	}
	
	/**
	 * 配当金計算年数を取得する
	 * 
	 * @param code 商品コード
	 * @return
	 */
	@SuppressWarnings("unchecked")
	private void _getDividendYear(String code) {
		if ("004".equals(code) || "008".equals(code) || "009".equals(code)
				|| "011".equals(code) || "013".equals(code) || "017".equals(code)) {
			_ctx.tokenValueMap.put(RATEKEY_DIVIDENDYEAR, 5);
		} else if ("042".equals(code)) {
			_ctx.tokenValueMap.put(RATEKEY_DIVIDENDYEAR, 3);
		} else {
			throw new FmsRuntimeException(code + "の配当金計算年数が存在していません。");
		}
	}
	
	/**
	 * 経理保種コードを取得する
	 * 
	 * @param code 商品コード
	 * @param payment 払方
	 * @param policyYear 契約日
	 * @return 経理保種コード
	 */
	private int _getAccountCode(String code, int payment, int policyYear) {
		if (("008".equals(code) || "009".equals(code) || "011".equals(code)
				|| "013".equals(code) || "017".equals(code))
				&& payment == 2) {
			return 110;
		} else if (("008".equals(code) || "011".equals(code) || "013".equals(code) || "017".equals(code))
				&& payment == 1) {
			return 111;
		} else if (("004".equals(code) || "005".equals(code)) && payment == 2) {
			return 200;
		} else if (("004".equals(code) || "005".equals(code)) && payment == 1) {
			return 201;
		} else if ("042".equals(code) && payment == 2) {
			return 210;
		} else if ("042".equals(code) && payment == 1
				&& ((20080401 <= policyYear) && (policyYear <= 20100930))) {
			return 211;
		} else if ("042".equals(code) && payment == 1
				&& ((20101001 <= policyYear) && (policyYear <= 20120331))) {
			return 212;
		} else {
			throw new FmsRuntimeException("商品" + code + "の経理保種コードが存在していません。");
		}
	}
	
	/**
	 * 保険期間、年金支払期間などより、期間区分を取得する
	 * @return　term
	 */
	private int _getTermValue() {
		
		for (DataRow data : this.termConditonlist) {
			String condtion = data.getString("condition");
			int term = data.getInt("term_code");

			//入力レートキーより条件を計算する
			if (StringUtils.isNotBlank(condtion)) {
				boolean cond = _ctx.getParser().parse(condtion).getBooleanValue();
				if (cond) {
					//カレント条件に満たす場合、応じて期間区分を返す
					if (logger.isDebugEnabled()) {
						logger.debug("入力レートキーに応じて期間区分：" + term);
					}
					return term;
				}
			}else{
				return term;
			}
		}
		//最後まで一つでも満たさない場合、0を返す（期間区分込んでない）
		return 0;
	}

	/**
	 * 配当率を取得する
	 * @param rateKeyMap 
	 * 
	 * @return
	 */
	@SuppressWarnings("unchecked")
	private void _editDividendRateValue(Map rateKeyMap) {
		
		// 期間区分を取得する
		int term = _getTermValue();

		// 契約日を取得する
		int policyYear = MapUtils.getInteger(rateKeyMap,
				RATEKEY_RATE_STARTDATE);

		// 配当適用年度を取得する
		int bonusYear = MapUtils.getInteger(rateKeyMap,
				RATEKEY_RATE_ENDDATE);
		bonusYear = _getBonusYear(bonusYear);
		
		int polcyPayment = 0;
		
		// 特約付加の主契約コード
		String policyCode = null;
		if (rateKeyMap.containsKey(RATEKEY_POLICYCODE)) {
			policyCode = StringUtils.leftPad(String.valueOf(
					MapUtils.getInteger(rateKeyMap, RATEKEY_POLICYCODE)), 3, "0");
			rateKeyMap.put(RATEKEY_POLICYCODE, policyCode);
			// 払方を取得する
			polcyPayment = MapUtils.getInteger(rateKeyMap, RATEKEY_RATE_POLICYKAISU);
			// poilcyTを設定
			_editElapsedYearMonth(rateKeyMap, false);
		} else {
			policyCode = _ctx.insuranceCode;
			rateKeyMap.put(RATEKEY_POLICYT, MapUtils.getIntValue(rateKeyMap, RATEKEY_T));
		}

		// 一部一時払判定
		int partOneTime = MapUtils.getIntValue(rateKeyMap, RATEKEY_PARTONETIME);
		// 主契約の払方存在する場合
		if (polcyPayment != 0) {
			// 払方を取得する
			polcyPayment = _getPayment(polcyPayment, partOneTime);
		}
		
		// 払方を取得する
		int payment = MapUtils.getInteger(rateKeyMap, RATEKEY_PAYMENT);
		// 払方を取得する
		payment = _getPayment(payment, partOneTime);
		
		// 経理保種コードを取得する
		int accCode = 0;
		if (polcyPayment != 0) {
			// 年金支払移行特約の場合、主契約が無配当（001）の場合は有配当（011）の経理保種に従う。
			if ("931".equals(_ctx.insuranceCode) && "001".equals(policyCode)) {
				policyCode = "011";
			}
			accCode = _getAccountCode(policyCode, polcyPayment, policyYear);
		} else {
			accCode = _getAccountCode(policyCode, payment, policyYear);
		}
		// 配当金計算年数を取得する
		_getDividendYear(policyCode);
		
		for (DataRow data : this.dividendList) {
			// 入力レートキーより条件を満たすかないかを判断する
			// 期間コードが０の場合、「配当率が期間区分に関連しない」という意味です
			if ((term == data.getInt("term_code") || 0 == data.getInt("term_code"))
					&& Integer.valueOf(data.getString("validate_from")) <= policyYear
					&& policyYear < Integer.valueOf(data
							.getString("validate_to"))
					&& String.valueOf(bonusYear).equals(
							data.getString("bonus_year"))
					&& payment == data.getInt("payment")
					&& accCode == data.getInt("account_code")) {
				Double id = data.getDouble("rate");
				Double id_nashu = data.getDouble("dividend_rate");
				Double accId = data.getDouble("acc_rate");
				Double accId_nashu = data.getDouble("acc_div_rate");
				rateKeyMap.put(RATEKEY_DIV_ID, id);
				rateKeyMap.put(RATEKEY_DIV_ID_NASHU, id_nashu);
				rateKeyMap.put(RATEKEY_DIV_ACCID, accId);
				rateKeyMap.put(RATEKEY_DIV_ACCID_NASHU, accId_nashu);
				return;
			}
		}
		rateKeyMap.put(RATEKEY_DIV_ID, 0);
		rateKeyMap.put(RATEKEY_DIV_ID_NASHU, 0);
		rateKeyMap.put(RATEKEY_DIV_ACCID, 0);
		rateKeyMap.put(RATEKEY_DIV_ACCID_NASHU, 0);
		if (logger.isInfoEnabled()) {
			logger.warn("入力されたレートキー通りに配当率は見つかりません");
		}
	}
    
	/**
	 * 払方を取得する
	 * @param payment 払方
	 * @param partOneTime 一部一時払
	 * @return
	 */
	private int _getPayment(int payment, int partOneTime) {
		// 払方が「分割払」の場合は2に読み替え
		if (payment > 2) {
			payment = 2;
		}
		
		if (partOneTime == 1) {
			// 一部一時払の場合、配当率を取得する場合、分割払とする
			payment = 2;
		}
		return payment;
	}
	
	/**
	 * 前事業年度末配当基準日を取得する
	 * @param bonusYear 
	 * @return
	 */
	private int _getBonusYear(int bonusYear) {
		// 年を取得する
		int year = bonusYear/10000;
		// 月日を取得する
		int day = bonusYear%10000;
		if (day < 331) {
			year = year - 1;
		}
		return year*10000 + 331;
	}
	
	/**
	 * 積立利率を取得する
	 * @param rateKeyMap 
	 * 
	 * @return
	 */
	@SuppressWarnings("unchecked")
	private void _editDivRateValue(Map rateKeyMap) {

		//契約日を取得する
		int policyYear = MapUtils.getInteger(rateKeyMap,
				RATEKEY_RATE_STARTDATE);
		//配当基準日を取得する
		int dividendYear = MapUtils.getInteger(rateKeyMap,
				RATEKEY_RATE_ENDDATE);
		double rateA = _getDivRateValue(policyYear, 1, rateKeyMap);
		double rateB = _getDivRateValue(dividendYear, 2, rateKeyMap);
		
		if (rateA != rateB) {
			rateKeyMap.put(RATEKEY_DIV_ISVALUECHANGED, 1);
			rateKeyMap.put(RATEKEY_DIV_RATEA, rateA);
			rateKeyMap.put(RATEKEY_DIV_RATEB, rateB);
		} else {
			rateKeyMap.put(RATEKEY_DIV_ISVALUECHANGED, 0);
			rateKeyMap.put(RATEKEY_DIV_RATEA, rateA);
		}
	}
	
	/**
	 * 積立利率を取得する
	 * @param Date
	 * @param flg 1:RateA変動日を取得　2：RateB変動日を取得
	 * @param rateKeyMap
	 * @return 積立利率
	 */
	@SuppressWarnings("unchecked")
	private double _getDivRateValue(int Date, int flg, Map rateKeyMap) {
		if ((19961001 <= Date) && (Date < 19990402)) {
			if (flg == 1) {
				rateKeyMap.put(RATEKEY_DIV_CHANGEDATEA, 19990401);
			} else {
				rateKeyMap.put(RATEKEY_DIV_CHANGEDATEB, 19961001);
			}
			return 0.029;
		} else if ((19990402 <= Date) && (Date < 20010402)) {
			if (flg == 1) {
				rateKeyMap.put(RATEKEY_DIV_CHANGEDATEA, 20010401);
			} else {
				rateKeyMap.put(RATEKEY_DIV_CHANGEDATEB, 19990402);
			}
			return 0.0215;
		} else if ((20010402 <= Date) && (Date < 20101001)) {
			if (flg == 1) {
				rateKeyMap.put(RATEKEY_DIV_CHANGEDATEA, 20100930);
			} else {
				rateKeyMap.put(RATEKEY_DIV_CHANGEDATEB, 20010402);
			}
			return 0.01;
		} else if (20101001 <= Date) {
			if (flg == 1) {
				rateKeyMap.put(RATEKEY_DIV_CHANGEDATEA, 20101001);
			} else {
				rateKeyMap.put(RATEKEY_DIV_CHANGEDATEB, 20101001);
			}
			return 0.0015;
		} else {
			if (logger.isInfoEnabled()) {
				logger.warn("入力されたレートキー通りに積立利率は見つかりません");
			}
			if (flg == 1) {
				rateKeyMap.put(RATEKEY_DIV_CHANGEDATEA, 0);
			} else {
				rateKeyMap.put(RATEKEY_DIV_CHANGEDATEB, 0);
			}
			return 0d;
		}
	}
	
    /**
     * 
     * 配当金計算において、
     * 契約日から事業年度末までの経過年月数t,fと
     * 契約日から保険年度末までの経過年月数t1,f1を取得
     * @param rateKeyMap
     * @param isNotRider
     */
	@SuppressWarnings("unchecked")
    private void _editElapsedYearMonth(Map rateKeyMap, Boolean isNotRider) {
    	// 契約年度を取得する
		int policyYear = 0;
		if (isNotRider) {
			policyYear = MapUtils.getInteger(rateKeyMap, RATEKEY_RATE_STARTDATE);
		} else {
			// 特約の場合、主契約の契約日を利用し、policyTを求める
			policyYear = MapUtils.getInteger(rateKeyMap, RATEKEY_RATE_POLICYSTARTDATE);
		}
		// 配当適用年度を取得する
		int bonusYear = MapUtils.getInteger(rateKeyMap,
				RATEKEY_RATE_ENDDATE);

		if ((bonusYear - policyYear)%10000 ==0) {
			//分配日と契約日が同じのフラグ
			rateKeyMap.put(RATEKEY_RATE_XN, 1);
		} else {
			rateKeyMap.put(RATEKEY_RATE_XN, 0);
		}
		// 計算対象を取得する
		int keisanPtn = MapUtils.getIntValue(rateKeyMap, RATEKEY_RATE_KEISANPTN);
		// 異動状態を取得する
		int changeState = MapUtils.getIntValue(rateKeyMap, RATEKEY_RATE_CHANGESTATE);
		// 契約現況を取得する
		int contractorState = MapUtils.getIntValue(rateKeyMap, RATEKEY_RATE_CONSTATE);
		// 分配額を計算するとき、異動状態ではない、契約現況が存在する場合
		if (keisanPtn == 3 && changeState == 2 && contractorState == 1) {
			// 分配日の前日を取得
			bonusYear = bonusYear - 1;
		}
		// 当事業年度末年月日を取得する
		int dividendThisYear = _getBonusYear(bonusYear);
		// 当事業年度末年月日設定
	    rateKeyMap.put(RATEKEY_RATE_DIVTHISYEAR, dividendThisYear);
	    // 前事業年度末年月日設定
	    rateKeyMap.put(RATEKEY_RATE_DIVLASYEAR, (dividendThisYear/10000-1)*10000+331);
        // 経過年月数　＝　「配当基準年月日 - 契約年月日」（端日数切り上げ）
        String tStdDate_ = String.valueOf(dividendThisYear);
        String t1StdDate_ = String.valueOf(bonusYear);
        String contractDate_ = String.valueOf(policyYear);

	    if (isNotRider) {//主契約の場合
	    	//ｔは分配日の前事業年度末までの経過
	    	_editTandF(contractDate_, tStdDate_, RATEKEY_T, RATEKEY_F, rateKeyMap);
	    	//ｔ1は分配日までの経過です。
	    	_editTandF(contractDate_, t1StdDate_, RATEKEY_T1, RATEKEY_F1, rateKeyMap);
        } else {
	    	_editTandF(contractDate_, tStdDate_, RATEKEY_POLICYT, null, rateKeyMap);
        }
    }
	
	/**
	 * 契約日から基準日までの経過年月数（端日数切上げ）
	 * <br>ただし1<=f<=12とする、即ちt年0ヶ月=>t-1年12ヶ月
	 * @param startDate
	 * @param standardDate
	 * @param t
	 * @param f
	 * @param rateKeyMap 
	 */
	@SuppressWarnings("unchecked")
	private void _editTandF(String startDate, String standardDate, String keyName4Year, String keyName4Month, Map rateKeyMap) {
		
		int t = 0, f = 0;
		
        // 開始年月日
        Date contractDate = null;
        // 基準年月日
        Date stdDate = null;
        
        SimpleDateFormat sdf = new SimpleDateFormat(Const.YYYYMMDD);
		try {
			stdDate = sdf.parse(standardDate);
		} catch (ParseException e) {
			throw new FmsRuntimeException("入力された配当基準年月日がフォーマットできません：" + standardDate);
		}

		try {
			contractDate = sdf.parse(startDate);
		} catch (ParseException e) {
			throw new FmsRuntimeException("入力された契約年月日がフォーマットできません：" + startDate);
		}

        // 計算処理の準備
        Calendar tCaldr = Calendar.getInstance();
        Calendar contractDateCaldr = Calendar.getInstance();
        tCaldr.setTime(stdDate);
        contractDateCaldr.setTime(contractDate);

        // 経過年
        t = tCaldr.get(Calendar.YEAR) - contractDateCaldr.get(Calendar.YEAR);
        
	    // 当年経過月
	    f = tCaldr.get(Calendar.MONTH) - contractDateCaldr.get(Calendar.MONTH);
	
	    // 当月経過日
	    int monthDays = 0;
	    
	    // 経過年が０より大きい場合
	    if (t >= 0) {
	        // 月末判定
	        int baseLastDay = tCaldr.getActualMaximum(Calendar.DAY_OF_MONTH);
	        int baseDay = tCaldr.get(Calendar.DAY_OF_MONTH);
	        int startDay = contractDateCaldr.get(Calendar.DAY_OF_MONTH);
	        
	        if (baseLastDay == baseDay) {
	            monthDays = 0;
	        } else {
	            monthDays = baseDay - startDay;
	        }
	        
	        // 当年経過日より当年経過月を設定する
	        f = monthDays < 0 ? --f : f;
	        
	    } else {
	        throw new IllegalArgumentException(MessageFormat.format("計算基準日{0}は開始日{1}より大きくない", standardDate, startDate));
	    }
	
	    // 経過月
	    if (f < 0) {
	        t--;
	        f += 12;
	    }
	    
	    //端日数切上げ処理
	    f++;
        
	    if (keyName4Year == null) {
	    	return;
	    }
	    // 経過年設定
    	rateKeyMap.put(keyName4Year, t);
    	
	    if (keyName4Month == null) {
	    	return;
	    }
	    // 経過月設定
	    rateKeyMap.put(keyName4Month, f);
	}
	
	/**
	 * 前事業年度末から効力発生日までに,各日付を迎えるかどうかを判定
	 * 0:いいえ 1:はい
	 * */
	@SuppressWarnings("unchecked")
	private void _editIsComeOfDate(Map rateKeyMap, int keisanPtn) {
		
		rateKeyMap.put(RATEKEY_ISCOMEOFDATE, 0);
		
		if (keisanPtn == 3) {
			// 契約日を取得する
			int policyYear = MapUtils.getInteger(rateKeyMap, RATEKEY_RATE_STARTDATE);
			// 払込期間を取得する
			int m = MapUtils.getInteger(rateKeyMap, RATEKEY_M);
			// 分配日を取得する
			int bonusYear = MapUtils.getInteger(rateKeyMap, RATEKEY_RATE_ENDDATE);
			// 前事業年度末配当基準日を取得
			int bonusLastEndYear = _getBonusYear(bonusYear);
			// 効力発生日を取得する
			int effectBeginDate = MapUtils.getInteger(rateKeyMap, RATEKEY_EFFECT_BEGIN_DATE);
	
			// 払込期間満了日を取得する
			int payOverDate = Integer.valueOf(CommonUtil.getFormatDate(String.valueOf(policyYear + m*10000 - 1)));
			// 前事業年度末年月日＜払込期間満了日＜効力発生日
			if(bonusLastEndYear < payOverDate && payOverDate < effectBeginDate){
				rateKeyMap.put(RATEKEY_ISCOMEOFDATE, 1);
			}
			// 繰下げ開始日が存在する場合
			if (rateKeyMap.containsKey(RATEKEY_L)) {
				// 繰下げ期間
				int l = MapUtils.getInteger(rateKeyMap, RATEKEY_L);
				if (l > 0) {
					// 繰下げ開始日を取得する
					int postponementBeginDate = MapUtils.getInteger(rateKeyMap, RATEKEY_ANNUITY_BEGIN_DATE) - l;
					// 前事業年度末年月日＜繰下げ開始日＜効力発生日
					if(bonusLastEndYear < postponementBeginDate && postponementBeginDate < effectBeginDate){
						rateKeyMap.put(RATEKEY_ISCOMEOFDATE, 1);
					}
				}
			}
			// 年金支払開始日
			int annuityBeginDate = 0;
			// 年金支払開始日が存在する場合
			if (rateKeyMap.containsKey(RATEKEY_ANNUITY_BEGIN_DATE)) {
				// 年金支払開始日を取得する
				annuityBeginDate = MapUtils.getInteger(rateKeyMap, RATEKEY_ANNUITY_BEGIN_DATE);
				// 前事業年度末年月日＜年金支払開始日＜効力発生日
				if(bonusLastEndYear < annuityBeginDate && annuityBeginDate < effectBeginDate){
					rateKeyMap.put(RATEKEY_ISCOMEOFDATE, 1);
				}
			}
			
			if (MapUtils.getInteger(rateKeyMap, RATEKEY_ISCOMEOFDATE) != 1 && Integer.parseInt(_ctx.insuranceCode) > 300) {
				// 契約終了日
				int policyEndYear = _getPolicyEndYear(rateKeyMap, policyYear);
				// 前事業年度末年月日＜満期日＜効力発生日
				if(bonusLastEndYear < policyEndYear && policyEndYear < effectBeginDate){
					rateKeyMap.put(RATEKEY_ISCOMEOFDATE, 2);
				}
			}
			// 年金支払開始日が存在する場合
			if (rateKeyMap.containsKey(RATEKEY_G) && rateKeyMap.containsKey(RATEKEY_ANNUITY_BEGIN_DATE) && annuityBeginDate > 0) {
				if (MapUtils.getInteger(rateKeyMap, RATEKEY_ISCOMEOFDATE) < 3) {
					// 年金支払期間
					int g = MapUtils.getInteger(rateKeyMap, RATEKEY_G);
					// 年金支払満了日
					int annuityEndDate = Integer.valueOf(CommonUtil.getFormatDate(String.valueOf(annuityBeginDate + g*10000 - 1)));
					// 前事業年度末年月日＜年金支払満了日＜効力発生日
					if(bonusLastEndYear < annuityEndDate && annuityEndDate < effectBeginDate){
						rateKeyMap.put(RATEKEY_ISCOMEOFDATE, 3);
					}
				}
			}
		}
	}
	
	/**
	 * 前事業年度末を迎えていないかどうかを判定
	 * 0:いいえ 1:はい
	 * */
	@SuppressWarnings("unchecked")
	private void _editIsAnnuityBegin(Map rateKeyMap, int keisanPtn) {
		
		rateKeyMap.put(RATEKEY_ISANNUITYBEGIN, 0);
		if (keisanPtn == 3) {
			String policyCode = MapUtils.getString(rateKeyMap, RATEKEY_POLICYCODE);
			if (Integer.parseInt(_ctx.insuranceCode) > 300 &&
					("004".equals(policyCode) || "042".equals(policyCode))) {
				// 契約日を取得する
				int policyYear = MapUtils.getInteger(rateKeyMap, RATEKEY_RATE_STARTDATE);
				// 分配日を取得する
				int bonusYear = MapUtils.getInteger(rateKeyMap, RATEKEY_RATE_ENDDATE);
				// 前事業年度末配当基準日を取得
				int bonusLastEndYear = _getBonusYear(bonusYear);
				// 旧年金買増年月日
				int oldAnnuityBuyDate = MapUtils.getInteger(rateKeyMap, RATEKEY_OLDANNUITY_BUY_DATE);
				// 最新年金買増年月
				int newAnnuityBuyDate = MapUtils.getInteger(rateKeyMap, RATEKEY_NEWANNUITY_BUY_DATE);
				// 前事業年度末年月日＜始期年月日又は増加年金の初回買増(「旧年金買増年月日＝０」かつ「最新年金買増年月＞前事業年度末」)
				if (bonusLastEndYear < policyYear ||
						(oldAnnuityBuyDate == 0 && newAnnuityBuyDate > bonusLastEndYear)) {
					if (!"931".equals(_ctx.insuranceCode)) {
						rateKeyMap.put(RATEKEY_ISANNUITYBEGIN, 1);
					}
				}
			}
		}
	}
	
	/**
	 * 当該事業年度中に保険期間の終了を迎えるかどうかを判定
	 * 0:いいえ 1:はい
	 * */
	@SuppressWarnings("unchecked")
	private void _editIsEndOfN(Map rateKeyMap) {
		// 契約日を取得する
		int policyYear = MapUtils.getInteger(rateKeyMap,
				RATEKEY_RATE_STARTDATE);
		// 事業年度終了日を取得する
		int bonusEndYear = MapUtils.getInteger(rateKeyMap,
				RATEKEY_RATE_ENDDATE);
		// 事業年度開始日を取得する
		int bonusYear = bonusEndYear - bonusEndYear%10000 - 10000 + 401;
		// 契約終了日
		int policyEndYear = _getPolicyEndYear(rateKeyMap, policyYear);
		
		if(bonusYear <= policyEndYear && policyEndYear <= bonusEndYear){
			rateKeyMap.put(RATEKEY_ISENDOFN, 1);
		}else{
			rateKeyMap.put(RATEKEY_ISENDOFN, 0);
		}
	}
	
	/**
	 * 契約終了日を取得
	 * @param rateKeyMap
	 * @param policyYear
	 * */
	@SuppressWarnings("unchecked")
	private int _getPolicyEndYear(Map rateKeyMap, int policyYear) {
		// 保険年数を取得する
		int n = 99;
		// 保険月数を取得する
		int f = 0;
		// 払い方を取得する
		int state = MapUtils.getInteger(rateKeyMap, RATEKEY_STATE);
		// 契約終了日
		int policyEndYear = 0;
		if (state != 7) {
			if (rateKeyMap.containsKey(RATEKEY_N)) {
				n = MapUtils.getInteger(rateKeyMap, RATEKEY_N);
			}
			// 契約終了日を取得する
			policyEndYear = policyYear + n * 10000;
		} else {
			n = MapUtils.getInteger(rateKeyMap, RATEKEY_TEX);
			f = MapUtils.getInteger(rateKeyMap, RATEKEY_FEX) * 100;
			int pF = policyYear%10000;
			if (pF + f > 12) {
				policyEndYear = policyYear + (n + 1) * 10000 + (f/100 - 12) * 100;
			} else {
				policyEndYear = policyYear + n * 10000 + f * 100;
			}
		}
		return policyEndYear;
	}
	
	/**
	 * 養育年金の経過月を設定
	 * @param rateKeyMap
	 */
	@SuppressWarnings("unchecked")
	private void _editAnnuityBeginMonth(Map rateKeyMap){
		Integer youikunenkin = MapUtils.getInteger(rateKeyMap,
				RATEKEY_YOUIKUNENKIN);
		if(youikunenkin == 1){
			int annuityBeginDate = MapUtils.getInteger(rateKeyMap, RATEKEY_ANNUITY_BEGIN_DATE);
			String annuityBeginDate_ = String.valueOf(annuityBeginDate);
			String bonusEndYear_ = _getHokenNendoDate(rateKeyMap);
			_editTandF(annuityBeginDate_, bonusEndYear_, RATEKEY_YOUIKU_ANNUITY_T, RATEKEY_YOUIKU_ANNUITY_F, rateKeyMap);
		}else{
			rateKeyMap.put(RATEKEY_YOUIKU_ANNUITY_T, 0);
			rateKeyMap.put(RATEKEY_YOUIKU_ANNUITY_F, 0);
		}
	}
	
	/**
	 * 当事業年度中の保険年度末をゲット
	 * @param rateKeyMap
	 * */
	@SuppressWarnings("unchecked")
	private String _getHokenNendoDate(Map rateKeyMap){
		int contractDate = MapUtils.getInteger(rateKeyMap,
				RATEKEY_RATE_STARTDATE);
		int divEndDate = MapUtils.getInteger(rateKeyMap,
				RATEKEY_RATE_ENDDATE);

		Date startDate = null;
		Date endDate = null;
		SimpleDateFormat sdf = new SimpleDateFormat(Const.YYYYMMDD);
		
		try {
			startDate = sdf.parse(String.valueOf(contractDate));
		} catch (ParseException e) {
			throw new FmsRuntimeException("入力された契約年月日がフォーマットできません：" + contractDate);
		}
		try {
			endDate = sdf.parse(String.valueOf(divEndDate));
		} catch (ParseException e) {
			throw new FmsRuntimeException("入力された契約年月日がフォーマットできません：" + divEndDate);
		}
		
        Calendar startCaldr = Calendar.getInstance();
        Calendar endCaldr = Calendar.getInstance();
        startCaldr.setTime(startDate);
        endCaldr.setTime(endDate);

        startCaldr.add(Calendar.DATE, -1);

        while(startCaldr.before(endCaldr)){
        	startCaldr.add(Calendar.YEAR, 1);
        }
        startCaldr.add(Calendar.YEAR, -1);

		return sdf.format(startCaldr.getTime());
	}
	
	/***
	 * 年金払い開始後、年金払込月を設定
	 * @param rateKeyMap
	 */
	@SuppressWarnings("unchecked")
	private void _editAnnuityPaidYearMonth(Map rateKeyMap){
		int t = MapUtils.getInteger(rateKeyMap, RATEKEY_T);
		int f = MapUtils.getInteger(rateKeyMap, RATEKEY_F);
		int n = MapUtils.getInteger(rateKeyMap, RATEKEY_N);
		int l = 0;
		if(rateKeyMap.containsKey(RATEKEY_L)){
			l = MapUtils.getInteger(rateKeyMap, RATEKEY_L);
		}
		int state = MapUtils.getInteger(rateKeyMap, RATEKEY_STATE);
		//状態は保険料払込完了後、時は保険期間後、そして繰り下げ時間がある場合
		//繰り下げ期間中に判断する
		if(state == 2 && l > 0 && t >= n){
			state = 5;
			rateKeyMap.put(RATEKEY_STATE, 5);
		}
		
		//状態は年金開始後の場合、年金開始経過年と月を設定
		if(state == 3 && t-n-l >= 0){
			rateKeyMap.put(RATEKEY_T2, t-n-l);
			rateKeyMap.put(RATEKEY_F2, f);
			rateKeyMap.put(RATEKEY_T3, 0);
			rateKeyMap.put(RATEKEY_F3, 0);
		//状態は繰り下げ期間中の場合、繰り下げ経過年と月を設定
		}else if(state == 5 && t-n >= 0){
			rateKeyMap.put(RATEKEY_T3, t-n);
			rateKeyMap.put(RATEKEY_F3, f);
			rateKeyMap.put(RATEKEY_T2, 0);
			rateKeyMap.put(RATEKEY_F2, 0);
		}else{
			rateKeyMap.put(RATEKEY_T2, t);
			rateKeyMap.put(RATEKEY_F2, 0);
			rateKeyMap.put(RATEKEY_T3, 0);
			rateKeyMap.put(RATEKEY_F3, 0);
		}
	}
}