package jp.co.nttdata.rate.batch.dataConvert;

import java.util.HashMap;
import java.util.Map;
import org.apache.commons.beanutils.ConvertUtils;
import org.apache.commons.lang.StringUtils;
import jp.co.nttdata.rate.batch.dataConvert.RateKeyLayout;
import jp.co.nttdata.rate.exception.IllegalBatchDataException;
import jp.co.nttdata.rate.fms.common.SystemFunctionUtility;
import jp.co.nttdata.rate.util.CommonUtil;

/**
 * お客様からデータ(.datファイル)を検証ツールに 識別されるレートキーに置換する
 * 
 * @author btchoukug
 * 
 * 
 */
public class Dat2RateKeyConverterImpl implements IRateKeyConverter {

	public static final String INPUT_PERIOD_KBN = "periodKbn";
	public static final String INPUT_PAYMENT_KBN = "paymentKbn";

	/** 現行システムの払込済種類 */
	public static final String INPUT_PAYUP_CN = "payupKbn";

	/** 現行システムの払込状態 */
	public static final String INPUT_PAYMENT_CN = "paymentSts";

	/** 　経過経路　 */
	public static final String INPUT_KEIRO = "keiro";

	/** 　被保険者性別　 */
	public static final String INPUT_SEX = "sex";

	/** 支払制限期間 */
	private static final String INPUT_KK1 = "kk1";

	/** FMSシステムに識別されるレートキー */
	public static final String RK_GEN = "gen"; // 世代
	public static final String RK_SEX = "sex"; // 性別
	public static final String RK_KAISU = "kaisu"; // 回数
	public static final String RK_AGE = "x"; // 被保険者年齢
	public static final String RK_N = "n"; // 保険期間年
	public static final String RK_M = "m"; // 払込期間年
	public static final String RK_G = "g"; // 年金支払期間
	public static final String RK_STATE = "state"; // 払込状態
	public static final String RK_KEIRO = "keiro"; // 払込経路
	public static final String RK_SA = "SA"; // 保険金額
	public static final String RK_T = "t"; // 契約経過年
	public static final String RK_F = "f"; // 契約経過月
	public static final String RK_T1 = "t1"; // 払込経過年
	public static final String RK_F1 = "f1"; // 払込経過月
	public static final String RK_T2 = "t2"; // 年金給付年
	public static final String RK_F2 = "f2";
	public static final String RK_T3 = "t3"; // 繰り下げ前の年金開始日からの経過年数
	public static final String RK_F3 = "f3";
	public static final String RK_L = "l"; // 繰り下げ期間
	public static final String RK_CONTRACT_DATE = "contractDate"; // 契約の始期年月日
	public static final String RK_K = "k"; // 年金支払満了期間
	public static final String RK_H = "h"; // 最低支払保証期間
	public static final String RK_U = "u"; // 低解約返戻期間
	public static final String ESTIMATE_RATE_KBN = "estimateRateKbn"; // 予定利率識別区分
	public static final String RK_ANNUITY_Y = "annuityY"; // 1回目の年金支払日の被年令
	public static final String RK_TEX = "tEX"; // 延長年
	public static final String RK_CONTRACTORTYPE = "contractorType"; // 料率識別区分CN
	public static final String RK_TEIZOKBN = "teizoKbn"; // 逓増型区分
	public static final String RK_STEP = "step"; // ステップ期間
	public static final String RK_STEPTIME = "stepTime"; // ステップ中・後standardDate
	public static final String RK_STANDARDDATE = "standardDate"; // P計算基準年月日
	public static final String RK_BONUS_MONTH = "bonus_month"; // ボーナス月
	public static final String RK_BONUS_MONTH1 = "bonusmonth1"; // ボーナス月1
	public static final String RK_BONUS_MONTH2 = "bonusmonth2"; // ボーナス月2
	private static final String RK_DIVIDEND = "dividend";	//配当有無
	private static final String RK_PART_ONTTIME = "partOnetime";	//一部一時払
	private static final String RK_KAIYAKU_UMU = "kaiyakuUmu";	//解約返戻金有無
	private static final String RK_INSURANCECODE = "insuranceCode"; //商品コード
	
	/** 005 料率区分追加 */
	public static final String INPUT_RATE_KBN = "ryoritsuKbn";
	public static final String RK_THETA = "theta";

	/** 支払制限期間 */
	public static final String RK_K1 = "k1";
	
	private static final int RADIX16 = 16;
	private static final char PLUS = '+';

	/** BT計算に対して、固定値のレートキー */
	public Map<String, Double> fixedValues;

	/** 比較元の項目名の配列 */
	private String[] compareObjNames;

	@Override
	public Map<String, Double> convert(Map<String, Double> oldKeyValues) {

		if (oldKeyValues == null || oldKeyValues.size() == 0) {
			throw new IllegalArgumentException("パラメータoldKeyValuesは空白だった。");
		}

		/*
		 * 変換前のレートキーの値を流用する 
		 * ★データレイアウトで新旧レートキーの名称のみを置き換えは要らない★
		 */
		Map<String, Double> convertedKeyValues = new HashMap<String, Double>(
				oldKeyValues);

		// 現行の払込済種類と払込状態を基に、契約状態stateを編集
		Double paymentVal = oldKeyValues.get(INPUT_PAYMENT_CN);
		double state = 0d;
		if (paymentVal != null) {
			Double payUpVal = oldKeyValues.get(INPUT_PAYUP_CN);
			int payup = (payUpVal == null) ? 0 : payUpVal.intValue();
			int payment = paymentVal.intValue();
			if ((payup == 1 && payment == 2) || (payup == 2 && payment == 2)) {
				// 払込期間終了後
				state = 2d;
			} else if (payup == 6 && payment == 2) {
				// 年金開始後
				state = 3d;
			} else if (payup == 0 && payment == 1) {
				// 払込期間中
				state = 1d;
			} else if (payup == 4 && payment == 2) {
				// 払済保険
				state = 4d;
			} else if (payup == 3) {
				// 払込免除
				state = 6d;
			} else if (payup == 5) {
				// 延長保険
				state = 7d;
			} else {
				throw new IllegalBatchDataException("お客様からデータは転換できません（払込済み種類、払込状態の値は取扱範囲外になりました。）");
			}
			convertedKeyValues.put(RK_STATE, state);
		}
		
		// 商品コード
		Double insuranceCode = oldKeyValues.get(RK_INSURANCECODE);
		// 被保性別からsexに転換
		Double sexVal = oldKeyValues.get(INPUT_SEX);
		if (sexVal != null) {

			/*
			 * 現行性別にて、男性：1；女性：2 FMSシステムでは、男性：0；女性：1
			 * 302対応： 男性：1；女性：2 FMSシステムでは、女性：1；男性：0
			 */
			if(insuranceCode != null && insuranceCode == 302d) {
				if(sexVal == 1) {
					sexVal = 2d;
				} else {
					sexVal = 1d;
				}
			}
			convertedKeyValues.put(RK_SEX, sexVal - 1d);
		}

		// 005専用対応
		Double rateKbn = oldKeyValues.get(INPUT_RATE_KBN);
		if (rateKbn != null) {
			if (rateKbn != 50d) {
				convertedKeyValues.put(RK_THETA, 5d);
			} else {
				convertedKeyValues.put(RK_THETA, 0d);
			}
		}

		/*
		 * 年満期・歳満期・終身通り、保険期間n・払込期間mを編集: 1.払込区分が「歳満期」の場合、m=m-x、n=n-xとする
		 * 2.保期区分が「終身保障」の場合、n=ω-x+1とする
		 */
		Double x = convertedKeyValues.get(RK_AGE);

		// 保期区分が「終身保障」の場合、n=ω-x+1とする
		Integer periodKbn = oldKeyValues.get(INPUT_PERIOD_KBN).intValue();
		Integer paymentKbn = oldKeyValues.get(INPUT_PAYMENT_KBN).intValue();

		if (periodKbn == null || paymentKbn == null) {
			throw new IllegalBatchDataException(
					"InputLayout.xmlには当該商品の保期区分と払期区分が定義されていません");
		}

		Double n = convertedKeyValues.get(RK_N);
		Double m = convertedKeyValues.get(RK_M);

		// 029,030対応：歳満期の場合、年金支払満了期間と低解返期間を求める
		if (periodKbn == 2) {
			convertedKeyValues.put(RK_N, n - x);

			Double K = convertedKeyValues.get(RK_K);
			if (K != null) {
				convertedKeyValues.put(RK_K, K - x);
			}
			Double U = convertedKeyValues.get(RK_U);
			if (U != null) {
				convertedKeyValues.put(RK_U, U - x);
			}
		}
		
		// 1回目の年金支払日の被年令の算出
		if(periodKbn == 1) {
			if (oldKeyValues.containsKey(RK_ANNUITY_Y)) {
				double y = convertedKeyValues.get(RK_ANNUITY_Y) + x;
				convertedKeyValues.put(RK_ANNUITY_Y, y);
			}
		}

		// 払込区分が「歳満期」の場合、m=m-x、n=n-xとする
		if (paymentKbn == 2) {
			if (convertedKeyValues.get(RK_KAISU) > 1) {
				// 分割払いの場合（一時払い、mがゼロのため）
				m = m - x;
				convertedKeyValues.put(RK_M, m);
			}
		}

		// 終身保障まだは終身払いの場合
		if (periodKbn == 3 || paymentKbn == 3) {

			double wholeLifeMN = 99d;

			// 終身保障の場合
			if (periodKbn == 3) {
				convertedKeyValues.put(RK_N, wholeLifeMN);
			}

			// 終身払いの場合
			if (paymentKbn == 3) {
				m = wholeLifeMN;
				convertedKeyValues.put(RK_M, m);				
			}
			
		}
		
		double oncepayM = 0d;
		
		if(convertedKeyValues.get(RK_KAISU) == 1) {
			convertedKeyValues.put(RK_M, oncepayM);
		}

		// TODO t1,f1払込年月は？
		Double f = oldKeyValues.get(RK_F);

		Double l = oldKeyValues.get(RK_L); // 繰り下げ期間
		l = l == null ? 0 : l;
		Double t = convertedKeyValues.get(RK_T); // 当年経過年

		// 年金開始後の場合、年金支払年数を求める
		if (state == 3 && !oldKeyValues.containsKey("Annuity")) {

			// 年金開始後の場合、t2を求める
			convertedKeyValues.put(RK_T2, convertedKeyValues.get(RK_T)
					- convertedKeyValues.get(RK_N) - l);
			convertedKeyValues.put(RK_F2, f);

		}

		// 「払込満了後」の場合、「繰り下げ中」かどうかを判明
		if (state == 2) {

			// 繰り下げ年数>0の場合、ステータスは「繰り下げ期間中」とする
			if (l > 0 && t > n) {
				state = 5;
				convertedKeyValues.put(RK_STATE, state);

				// 繰り下げ前の年金開始日からの経過年数t3=当年経過年-（保期（歳）-X年齢（歳）⇒保険期間年）
				convertedKeyValues.put(RK_T3, t - convertedKeyValues.get(RK_N));
				convertedKeyValues.put(RK_F3, f);

			}

		}

		// 年金開始後の場合、年金開始年令の転換
		if (state == 3 && l > 0) {
			if (oldKeyValues.containsKey(RK_ANNUITY_Y)) {
				double y = convertedKeyValues.get(RK_ANNUITY_Y) + l;
				convertedKeyValues.put(RK_ANNUITY_Y, y);
			}
		}

		// Ｐ扱方法より経路に転換
		Double keiroVal = oldKeyValues.get(INPUT_KEIRO);
		double keiro = 0d; // デフォルト場合、ゼロとする
		if (keiroVal != null) {
			switch (keiroVal.intValue()) {
			case 1:
				keiro = 1d;
				break;
			case 2:
				keiro = 3d;
				break;
			case 3:
				keiro = 4d;
				break;
			case 4:
				keiro = 5d;
				break;
			case 6:
				keiro = 2d;
				break;
			default:
				;
			}
		}
		convertedKeyValues.put(RK_KEIRO, keiro);

		// 支払制限期間
		Double kk1 = oldKeyValues.get(INPUT_KK1);
		double k1 = 0d;
		if (kk1 != null) {
			switch (kk1.intValue()) {
			case 72:
				k1 = 2d;
				break;
			case 73:
				k1 = 4d;
				break;
			default:
				;
			}
		}
		convertedKeyValues.put(RK_K1, k1);

		// 一部一時払の場合、払方CNの設定
		if (oldKeyValues.containsKey(ESTIMATE_RATE_KBN)) {
			int estimateRateKbn = oldKeyValues.get(ESTIMATE_RATE_KBN).intValue();
			if ((paymentKbn == 0)
					&& (estimateRateKbn == 3 || estimateRateKbn == 5)) {
				convertedKeyValues.put(RK_KAISU, 2d);
			}
		}
		
		//延長年の設定
		if(convertedKeyValues.containsKey(RK_STATE) && convertedKeyValues.get(RK_STATE) == 7) {
			
			convertedKeyValues.put(RK_TEX, convertedKeyValues.get(RK_N));

			
		}
		
		//028と308商品：逓増区分の設定
		if(oldKeyValues.containsKey(RK_CONTRACTORTYPE)) {
			int iTeizoKbn = oldKeyValues.get(RK_CONTRACTORTYPE).intValue();
			if (iTeizoKbn >= 144 || iTeizoKbn == 41) {
				//16HEXの90が144とイコール
				convertedKeyValues.put(RK_TEIZOKBN, 1d);
			} else if (iTeizoKbn >= 128 || iTeizoKbn == 40) {
				//16HEXの80が128とイコール
				convertedKeyValues.put(RK_TEIZOKBN, 0d);
			} else {
				;
			}
		}
		
		//ステップ中・後判定
		if(oldKeyValues.containsKey(RK_STEP)) {
			if(!oldKeyValues.containsKey(RK_CONTRACT_DATE) || !oldKeyValues.containsKey(RK_STANDARDDATE)) {
				throw new IllegalBatchDataException("InputLayout.xmlには当該商品の始期年月日とP計算基準年月日が定義されていません");
			} else {
				if(oldKeyValues.get(RK_CONTRACT_DATE) +  oldKeyValues.get(RK_STEP)*10000 - oldKeyValues.get(RK_STANDARDDATE) > 0) {
					convertedKeyValues.put(RK_STEPTIME, 1d); 
				} else {
					convertedKeyValues.put(RK_STEPTIME, 2d); 
				}
			}
		}
		
		//ボーナス月取得
		if(oldKeyValues.containsKey(RK_BONUS_MONTH1) && oldKeyValues.containsKey(RK_BONUS_MONTH2)) {
			int bonusMonth1 = oldKeyValues.get(RK_BONUS_MONTH1).intValue();
			int bonusMonth2 = oldKeyValues.get(RK_BONUS_MONTH2).intValue();
			if (bonusMonth1 != 0 && bonusMonth2 != 0) {

				if ((bonusMonth1 == 1 && bonusMonth2 == 7) || (bonusMonth2 == 1 && bonusMonth1 == 7)) {
					convertedKeyValues.put(RK_BONUS_MONTH, 1d);
				} else if ((bonusMonth1 == 6 && bonusMonth2 == 12) || (bonusMonth2 == 6 && bonusMonth1 == 12)) {
					convertedKeyValues.put(RK_BONUS_MONTH, 2d);
				} else if ((bonusMonth1 == 7 && bonusMonth2 == 12) || (bonusMonth2 == 7 && bonusMonth1 == 12)) {
					convertedKeyValues.put(RK_BONUS_MONTH, 3d);
				} else {
					convertedKeyValues.put(RK_BONUS_MONTH, 0d);
				}
				
			}
		}

		/**
		 * ボーナス払いの場合、経過月（ボーナスを含む）を算出
		 * contractDate: 始期年月日
		 * standardDate: 解約年月日
		 * RK_BONUS_MONTH1: ボーナス月1
		 * RK_BONUS_MONTH2: ボーナス月2
		 * psi: ボーナス払倍数 
		 * psiが0の場合、ボーナス払いではない
		 */
		if (( oldKeyValues.containsKey("psi")
				&& oldKeyValues.get("psi").intValue() != 0
				&& oldKeyValues.containsKey(RK_BONUS_MONTH1)
				&& oldKeyValues.containsKey(RK_BONUS_MONTH2))
				&& oldKeyValues.containsKey("standardDate")
				&& oldKeyValues.containsKey("contractDate")
				&& oldKeyValues.containsKey("f1")
		) {

			int ContractMonth = oldKeyValues.get("contractDate").intValue() % 10000 / 100;
			int KaiyakuMonth = oldKeyValues.get("standardDate").intValue() % 10000 / 100;
			int bonusMonth1 = oldKeyValues.get(RK_BONUS_MONTH1).intValue();
			int bonusMonth2 = oldKeyValues.get(RK_BONUS_MONTH2).intValue();

			if (bonusMonth1 < ContractMonth) {
				bonusMonth1 += 12;
			}
			if (bonusMonth2 < ContractMonth) {
				bonusMonth2 += 12;
			}
			if (KaiyakuMonth < ContractMonth) {
				KaiyakuMonth += 12;
			}

			if (KaiyakuMonth > bonusMonth1 && KaiyakuMonth > bonusMonth2) {
				convertedKeyValues.put("bonus_f", oldKeyValues.get("f1")
						+ oldKeyValues.get("psi") * 2 - 2);
			} else if (KaiyakuMonth > bonusMonth1 || KaiyakuMonth > bonusMonth2) {
				convertedKeyValues.put("bonus_f", oldKeyValues.get("f1")
						+ oldKeyValues.get("psi") - 1);
			} else {
				convertedKeyValues.put("bonus_f", oldKeyValues.get("f1"));
			}
		}

		// 004と005の年金タイプを判明する
		if(oldKeyValues.containsKey("annuityType")){
			int annuityType = oldKeyValues.get("annuityType").intValue();
			if(annuityType == 4){
				convertedKeyValues.put(RK_G, oldKeyValues.get("g_004"));
			} else if(annuityType == 5){
				convertedKeyValues.put(RK_G, oldKeyValues.get("g_005"));
			}
		}
		
		// 遺族年金経過年月
		/**
		 * 未払年金の場合、遺族年金経過年月を算出
		 * contractDate: 始期年月日
		 * annuityDate: 未払年金現価算出年月日
		 */
		if(oldKeyValues.containsKey("annuityDate")) {
			double annuityDate = oldKeyValues.get("annuityDate");
			double contractDate = oldKeyValues.get(RK_CONTRACT_DATE);
			double annuityT = SystemFunctionUtility.roundDown(annuityDate/10000, 0);
			double annuityF = SystemFunctionUtility.roundDown(annuityDate/100, 0) % 100;
			double contractT = SystemFunctionUtility.roundDown(contractDate/10000, 0);
			double contractF = SystemFunctionUtility.roundDown(contractDate/100, 0) % 100;
			t = annuityT - contractT;
			f = annuityF - contractF;
			// 009養育年金の経過年月の算出
			convertedKeyValues.put(RK_T, t);
			convertedKeyValues.put(RK_F, f);
			if(f < 0) {
				t--;
				f = f + 12;				
			}
			// 該当時までの経過年月（ 但し、t1/t2年0ヶ月のときはt1-1/t2-1年12ヶ月として使用する）
			if(f == 0) {
				t = t - 1;
				f = 12d;
			}
			convertedKeyValues.put(RK_T1, t);
			convertedKeyValues.put(RK_F1, f);
			convertedKeyValues.put(RK_T2, t);
			convertedKeyValues.put(RK_F2, f);
		}
		
		//FIXME 016、017の低解返期間uの算出
		if(oldKeyValues.containsKey(RK_U) && oldKeyValues.containsKey("u1")) {
			double U = oldKeyValues.get(RK_U);
			double U1 = oldKeyValues.get("u1");
			if (U == 0 || U == U1) {
				if(U1 >= x){
					convertedKeyValues.put(RK_U, U1 - x);
				} else {
					convertedKeyValues.put(RK_U, convertedKeyValues.get(RK_M));
				}
			}
		}
		// チルメル期間
		if(oldKeyValues.containsKey("z")) {
			double zilmer = oldKeyValues.get("z");
			zilmer = Math.min(zilmer,m);
			convertedKeyValues.put("z", zilmer);
		}
		
		// 027 長寿祝金
		if(oldKeyValues.containsKey("s") && oldKeyValues.containsKey(RK_STANDARDDATE)) {
			double s = oldKeyValues.get("s");
			int monthDay = oldKeyValues.get(RK_STANDARDDATE).intValue() % 10000;
			s = monthDay > 331 ? -1 : s;
			convertedKeyValues.put("s", s);
		}
		
		// UIから指定の固定値を上書きする
		if (this.fixedValues != null) {
			convertedKeyValues.putAll(this.fixedValues);
		}

		// 比較元の項目をそのままセット
		if (compareObjNames != null) {
			for (String compObj : compareObjNames) {
				if (!oldKeyValues.containsKey(compObj)) {
					throw new IllegalArgumentException("指定の比較元の項目が存在していない");
				}
				convertedKeyValues.put(compObj, oldKeyValues.get(compObj));
			}
		}

		// 変換したレートキーを返す
		return convertedKeyValues;
	}

	@Override
	public void setFixedValue(Map<String, Double> fixedValues) {
		this.fixedValues = fixedValues;
	}

	@Override
	public void setCompareObjectNames(String[] compareObjNames) {
		this.compareObjNames = compareObjNames;
	}

	@Override
	public double convertRateKey(RateKeyLayout layout, String value) {
		
		// まず、余計なスペースを削除する
		value = CommonUtil.deleteWhitespace(value);
		if (StringUtils.isEmpty(value)) {
			return 0;
		}

		double ret = 0d;

		// +/-符号を先頭に付いてる場合、+を外す
		if (value.charAt(0) == PLUS) {
			value = StringUtils.remove(value, PLUS);
		}

//		// 普通数字の場合
//		if (CommonUtil.isNumeric(value)) {
//			ret = Integer.parseInt(value);
//		} else if (StringUtils.containsAny(value, "ABC")) {
//			//80~8Cまたは90~9Cの場合、16製から10製に変換する
//			ret = Integer.parseInt(value, RADIX16);
//		} else {
//			;
//		}
//
//		// 満期給付金倍率の場合、8A->90
//		if (layout.getName().equals("I")) {
//			int tmp = ret - I80;
//			if(tmp >= 16) {
//				ret = ret - I90;
//			}
//		}

		//8a~8c、9a~9cの場合、falseになって、retは0
		if (CommonUtil.isNumeric(value)) {
			//ret = Integer.parseInt(value);
			ret = (Double) ConvertUtils.convert(value, Double.class);
		}

		if(layout.getName().equals(RK_CONTRACTORTYPE)) {
			if(ret >= 80 || ret == 0){
				//0の場合は、8a~8c、9a~9c。
				ret = Integer.parseInt(value, RADIX16);
			}
		}

		// 満期給付金倍率の場合
		if (layout.getName().equals("I")) {
			ret = Integer.parseInt(value, RADIX16) % 16;
		}

		// 031～034医療保険の専用対応
		// レートキー：災害給付不担保特約付加
		if (layout.getName().equals("fuka")) {
			if (StringUtils.isBlank(value)) {
				ret = 0;
			} else {
				ret = Integer.parseInt(value);
			}
		}

		// 304専用対応：最終保険金額割合
		// 料率識別区分⇒３１：２０％、３４：４０％、３５：６０％
		if (layout.getName().equals("finalPercent")) {
			switch ((int)ret) {
			case 31:
				ret = 0;
				break;
			case 34:
				ret = 1;
				break;
			default:
				ret = 2;
			}
		}

		// 304専用対応：配当
		// 予定利率識別⇒０、２、３：無配当；１、４、５：利差配当
		if (layout.getName().equals(RK_DIVIDEND)) {
			switch ((int)ret) {
			case 0:
			case 2:
			case 3:
				ret = 0;
				break;
			default:
				ret = 1;
			}
		}

		/*
		 * 一部一時払いを判明する
		 * ０：なし；１：あり
		 */
		if (RK_PART_ONTTIME.equals(layout.getName())) {
			switch ((int)ret) {
			case 3:
			case 5:
				ret = 1;
				break;
			case 2:
			case 4:
				ret = 0;
				break;
			default:
				;
			}			
		}

		/*
		 *  204専用対応：
		 *  保期区分CNが３の場合、終身保；その以外の場合、確定払
		 *  払期区分CNが３の場合、終身保；その以外の場合、確定払
		 */
		if ("Htype".equals(layout.getName()) || "Stype".equals(layout.getName())) {
			if (ret == 3) {
				ret = 0;
			} else {
				ret = 1;
			}
		}

		// 204・235専用対応：被保険者型
		if (layout.getName().equals(RK_CONTRACTORTYPE)) {
			switch ((int)ret) {
			case 19:
				ret = 1;
				break;
			case 20:
				ret = 3;
				break;
			case 21:
				ret = 2;
				break;
			case 22:
				ret = 4;
				break;
			default:
				;
			}
		}

		// 014専用対応：逓増型
		if (layout.getName().equals("teizoKbn")) {
			switch ((int)ret) {
			case 32:
			case 40:
				ret = 0;
				break;
			case 33:
			case 41:
				ret = 1;
				break;
			default:
				;
			}
		}

		// 235専用対応：災害不担保特則付加有無（コード値はそのまま）
		if (layout.getName().equals("fukaumu_z")) {
			;
		}

		// 009専用対応:養育年金支払事由発生前後
		if (layout.getName().equals("afterBenifitPay")) {
			switch((int)ret){
			case 10:
				ret = 0;
				break;
			case 20:
				ret = 1;
				break;
			default:
				;
			}
		}

		// 235専用対応：解約返戻金有無（商品コードより判断する）
		if (layout.getName().equals(RK_KAIYAKU_UMU)) {
			//235と236が解約返戻金あり
			switch((int)ret){
			case 31:
			case 32:
			case 235:
			case 236:
			case 261:
			case 262:
			case 265:
			case 266:
			case 276:
				ret = 1;
				break;
			case 33:
			case 34:
			case 237:
			case 238:
			case 263:
			case 264:
			case 267:
			case 268:
			case 278:
				ret = 0;
			}
		}
		return ret;
	}
}