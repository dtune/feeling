package jp.co.nttdata.rate.fms.common;

import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import jp.co.nttdata.rate.util.Const;

/**
 *
 * roundやmax,minなどのSYSFUNCの物理的なメソッドであるモジュール 
 * @author btchoukug
 * 
 */
public class SystemFunctionUtility {
	
	/** 端数処理（四捨五入） */
	public static double round(Double doubleValue, int scale) {
		
		//計算の異常値だったら、ゼロを返す
		if (doubleValue.equals(Double.NaN)) {
			return 0d;
		}
		
		String text = doubleValue.toString();
		BigDecimal bd = new BigDecimal(text).setScale(scale,
				BigDecimal.ROUND_HALF_UP);
		return bd.doubleValue();
	}
	
	public static BigDecimal round(BigDecimal decimalValue, int scale) {
		
		//計算の異常値だったら、ゼロを返す
		if (decimalValue == null) {
			return new BigDecimal(0);
		}
		
		return decimalValue.setScale(scale,
				BigDecimal.ROUND_HALF_UP);
	}

	/** 端数処理（切り上げ） */
	public static double roundUp(Double doubleValue, int scale) {
		String text = doubleValue.toString();
		BigDecimal bd = new BigDecimal(text).setScale(scale,
				BigDecimal.ROUND_UP);
		return bd.doubleValue();
	}

	/** 端数処理（切り捨て） */
	public static double roundDown(Double doubleValue, int scale) {
		String text = doubleValue.toString();
		BigDecimal bd = new BigDecimal(text).setScale(scale,
				BigDecimal.ROUND_DOWN);
		return bd.doubleValue();
	}
	
	/** 最小値を算出 */
	public static double min(double num1, double num2){
		return Math.min(num1, num2);
		
	}
	
	/** 最大値を算出 */
	public static double max(double num1, double num2){
		return Math.max(num1, num2);		
	}
	
	/** 
	 * 給付金を支払われる保険年度を算出する（009こども保険給付金割合用）
	 * @param x 支払年齢
	 * @param t 経過年
	 * @param birthDay 誕生日(yyyyMMdd)
	 * @param contractDay 契約日(yyyyMMdd)
	 * @param StandardDate 計算基準日(yyyyMMdd)
	 * @return double 実際の保険年度
	 * @throws ParseException
	 */
	public static int celebrationPolicyYear(double x, double t, double birthDay,
			double contractDay, double standardDate) throws ParseException {
		/*実際保険年度*/
		int tJisai = 0;

		String strBirthDay = String.valueOf((int)(birthDay * 1));
		String strContractDay = String.valueOf((int)(contractDay * 1));
		String strStandardDate = String.valueOf((int)(standardDate * 1));

		SimpleDateFormat sdf = new SimpleDateFormat(Const.YYYYMMDD);
		Date dateBirthDay = sdf.parse(strBirthDay);		
		Calendar calBirthDay = Calendar.getInstance();
		calBirthDay.clear();
		calBirthDay.setTime(dateBirthDay);

		Date dateContractDay = sdf.parse(strContractDay);
		Calendar calContractDay = Calendar.getInstance();
		calContractDay.clear();
		calContractDay.setTime(dateContractDay);
		
		Date dateStandardDate = sdf.parse(strStandardDate);
		Calendar calStandardDate = Calendar.getInstance();
		calStandardDate.clear();
		calStandardDate.setTime(dateStandardDate);
		
		Calendar calPaymentDay = Calendar.getInstance();
		calPaymentDay.clear();

		switch((int)x) {
		case 6:
			/* ５歳１０ヵ月 */
			calBirthDay.add(Calendar.YEAR, 5);
			break;
		case 12:
			/* １１歳１０ヵ月 */
			calBirthDay.add(Calendar.YEAR, 11);
			break;
		case 15:
			/* １４歳１０ヵ月 */
			calBirthDay.add(Calendar.YEAR, 14);
			break;
		case 18:
			/* 18歳の年単位の契約応当日に生存している場合 */
			return _18AgePay(calBirthDay,calContractDay,calStandardDate);
		default:
			;
		}
		
		/* １０ヵ月経過 */
		calBirthDay.add(Calendar.MONTH, 10);
				
		/* 支払うべきの日付をセット */
		calPaymentDay.set(Calendar.YEAR, calBirthDay.get(Calendar.YEAR));
		calPaymentDay.set(Calendar.MONTH, Calendar.FEBRUARY);
		calPaymentDay.set(Calendar.DAY_OF_MONTH, 1);

		/* 支払日と比較して、直後の２月１日で支払いようにとして実際の保険年度を算出 */
		//00歳出生前ケース３の対応
		if(calBirthDay.compareTo(calPaymentDay) <= 0){
			;
		}else{			
			//翌年にする
			calPaymentDay.add(Calendar.YEAR, 1);
		}
		
		//経過年月を算出
		tJisai = calPaymentDay.get(Calendar.YEAR) - calContractDay.get(Calendar.YEAR);
		int f = calPaymentDay.get(Calendar.MONTH) - calContractDay.get(Calendar.MONTH);
		int day = calContractDay.get(Calendar.DAY_OF_MONTH);
		//月数の差がマイナスになると、前の年となる
		if (f < 0 || (f == 0 && day > 1)) {
			tJisai--;
		}
		//修正後の経過年を返す
		return ++tJisai;
	}
	
	private static int _18AgePay(Calendar calBirthDay, Calendar calContractDay, Calendar calStandardDate ) {
		// 支払日
		Calendar calPaymentDay = Calendar.getInstance();
		calPaymentDay.clear();
		
		calBirthDay.add(Calendar.YEAR, 18);
		calPaymentDay.set(Calendar.YEAR, calBirthDay.get(Calendar.YEAR));
		calPaymentDay.set(Calendar.MONTH, calContractDay.get(Calendar.MONTH));
		calPaymentDay.set(Calendar.DAY_OF_MONTH, calContractDay.get(Calendar.DAY_OF_MONTH));
		if(calPaymentDay.compareTo(calBirthDay) < 0) {
			calPaymentDay.add(Calendar.YEAR, 1);
		}
		calPaymentDay.add(Calendar.YEAR, 1);
		if(calStandardDate.compareTo(calPaymentDay) >= 0) {
			return 1;
		}
		return -1;				
	}
	
	/** 
	 * 給付金を支払われる保険年度を算出する（009こども保険給付金割合用）
	 * @param x 支払年齢
	 * @param t 経過年
	 * @param birthDay 誕生日(yyyyMMdd)
	 * @param contractDay 契約日(yyyyMMdd)
	 * @param StandardDate 計算基準日(yyyyMMdd)
	 * @return double 実際の祝金の給付割合の合計額
	 * @throws ParseException
	 */
	public static double sum_tJx(double x, double t, double birthDay,
			double contractDay, double standardDate) throws ParseException {
		double sum_tJx = 0;
		boolean is18agePay = true;
		double calStandardDate = standardDate;

		if (x + t >= 19) {
			return x <=3 ? 2.0d : 1.8d;
		}
		
		int _6agePay = celebrationPolicyYear(6d,t,birthDay,contractDay,standardDate);
		int _12agePay = celebrationPolicyYear(12d,t,birthDay,contractDay,standardDate);
		int _15agePay = celebrationPolicyYear(15d,t,birthDay,contractDay,standardDate);
		
		for(int i = 0; i <= t; i++) {
			if(i == _6agePay) {
				if(x <= 3) {
					sum_tJx += 0.2;
				}
			} else if(i == _12agePay) {
				sum_tJx += 0.3;
			} else if(i == _15agePay) {
				sum_tJx += 0.5;
			} else if(is18agePay && x + i >= 18) {
				if(celebrationPolicyYear(18d,t,birthDay,contractDay,calStandardDate) >= 0){
					sum_tJx += 1.0;
					is18agePay = false;
				} else {
					calStandardDate += 10000d;
				}
			} else {
				;
			}
		}
		return sum_tJx;
	}
	
	/** 
	 * 計算基準日までに支払事由の発生した祝金の給付割合の合計額
	 * （009こども保険実際の給付割合の合計額）
	 * @param x 年齢
	 * @param birthDay 誕生日(yyyyMMdd)
	 * @param StandardDate 計算基準日(yyyyMMdd)
	 * @param contractDay 契約日(yyyyMMdd)
	 * @return double 祝金の給付割合の合計額
	 * @throws ParseException
	 */
	public static double sJx(double x, double birthDay, double standardDate, double contractDay) throws ParseException {
		
		String strBirthDay = String.valueOf((int)(birthDay * 1));
		String strStandardDate = String.valueOf((int)(standardDate * 1));
		String strContractDay = String.valueOf((int)contractDay*1); 
		
		SimpleDateFormat sdf = new SimpleDateFormat(Const.YYYYMMDD);

		Date dateBirthDay = sdf.parse(strBirthDay);
		Calendar calBirthDay = Calendar.getInstance();
		calBirthDay.clear();
		calBirthDay.setTime(dateBirthDay);

		Date dateStandardDate = sdf.parse(strStandardDate);
		Calendar calStandardDate = Calendar.getInstance();
		calStandardDate.clear();
		calStandardDate.setTime(dateStandardDate);
		
		Date dateContractDay = sdf.parse(strContractDay);
		Calendar calContractDay = Calendar.getInstance();
		calContractDay.clear();
		calContractDay.setTime(dateContractDay);
		
		Calendar calPaymentDay = Calendar.getInstance();
		calPaymentDay.clear();
		
		/* １０ヵ月経過 */
		calBirthDay.add(Calendar.MONTH, 10);
		
		/* 支払うべきの日付をセット */
		calPaymentDay.set(Calendar.YEAR, calBirthDay.get(Calendar.YEAR));
		calPaymentDay.set(Calendar.MONTH, Calendar.FEBRUARY);
		calPaymentDay.set(Calendar.DAY_OF_MONTH, 1);
		
		/* 支払日と比較して、直後の２月１日で支払いようにとして実際の保険年度を算出 */
		if(calBirthDay.compareTo(calPaymentDay) <= 0){
			;
		}else{			
			//翌年にする
			calPaymentDay.add(Calendar.YEAR, 1);
		}
		/* 契約日＋（１８－ｘ）年≦計算基準日 */
		calContractDay.add(Calendar.YEAR, 18 - (int)x);
		if(calContractDay.compareTo(calStandardDate) <= 0) {
			if(x <= 3) {
				return 2.0d;
			} else {
				return 1.8d;
			}
		}		
		/* 14歳１０ヵ月 */
		calPaymentDay.add(Calendar.YEAR, 14);
		if(calPaymentDay.compareTo(calStandardDate) <= 0) {
			if(x <= 3) {
				return 1.0d;
			} else {
				return 0.8d;
			}
		}
		/* 11歳１０ヵ月 */
		calPaymentDay.add(Calendar.YEAR, -3);
		if(calPaymentDay.compareTo(calStandardDate) <= 0) {
			if(x <= 3) {
				return 0.5d;
			} else {
				return 0.3d;
			}
		}
		/* 5歳１０ヵ月 */
		calPaymentDay.add(Calendar.YEAR, -6);
		if(calPaymentDay.compareTo(calStandardDate) <= 0) {
			if(x <= 3) {
				return 0.2d;
			} else {
				return 0.0d;
			}
		}
		return 0.0d;
	}
	/**
	 * @param f
	 * @return round(f/12,5)
	 */
	public static double  monthDiv12(double f){
		return round(f/12d,5);
	}
	
	/** 
	 * 実際の長寿祝金の給付割合給付かどうかを判断する（027無選択終身保険長寿祝金用）
	 * @param x 被保険者年齢
	 * @param birthDay 誕生日(yyyyMMdd)
	 * @param contractDay 契約日(yyyyMMdd)
	 * @param standardDate 基準日（解約日）(yyyyMMdd)
	 * @return double 実際の長寿祝金の給付割合給付かどうか
	 */
	public static double blSIx(double x, double birthDay,
			double contractDay, double standardDate) {
        // 基準日
        String kijyun_date = String.valueOf((int)(standardDate * 1));
        // 始期日 
        String shiki_date = String.valueOf((int)(contractDay * 1));
        // 生日
        String sei_date = String.valueOf((int)(birthDay * 1));
        double s = -1;
        
        // 基準日の経過年
        int year = Integer.parseInt(kijyun_date.substring(0, 4))-Integer.parseInt(sei_date.substring(0, 4));
        if (kijyun_date.substring(4).compareTo(sei_date.substring(4)) < 0) {
            year -- ;
        }
        
        // 応答日
        String outou_date = null;
        if (shiki_date.substring(4).compareTo(sei_date.substring(4)) > 0) {
            outou_date = (Integer.parseInt(sei_date.substring(0, 4)) + year) + shiki_date.substring(4);
        }else{
            outou_date = (Integer.parseInt(sei_date.substring(0, 4)) + year + 1) + shiki_date.substring(4);
        }
        
        // 保険年度進む
        if (kijyun_date.compareTo(outou_date) > 0) {
        	s = -1;
        }else{
			s = year - (int) x;
        }
        return s;
	}
	/** 
	 * 経過日をカウントする
	 * 
	 * @param startDate　契約日(yyyyMMdd)
	 * @param endDate 配当基準日(yyyyMMdd)
	 * @return double
	 * @throws ParseException 
	 */
	public static double getElapsedDays(double startDate, double endDate) throws ParseException{
		
		String strDateFrom = String.valueOf((int)(startDate * 1));
		String strDateTo = String.valueOf((int)(endDate * 1));
		
		SimpleDateFormat sdf = new SimpleDateFormat(Const.YYYYMMDD);
		Date dateDateFrom = sdf.parse(strDateFrom);
		Date dateDateTo = sdf.parse(strDateTo);
		
		// 経過した秒数を取得する
		long sec = dateDateTo.getTime() - dateDateFrom.getTime();
		
		// 日数を戻る
		return sec / Const.SECONDS_OF_ONEDAY;
	}
	
	/** 
	 * 
	 * 年日数を判断する
	 * 2/29を存在する場合、366を戻る；以外、365を戻る	 * 
	 * @param startDate　契約日(yyyyMMdd)
	 * @param endDate 配当基準日(yyyyMMdd)
	 * @return double
	 * @throws ParseException 
	 */
	public static double getDaysOfYear(double startDate, double endDate) throws ParseException{

		int yearFrom = (int) (startDate / 10000);
		int monthDayFrom = (int) (startDate % 10000);
		int yearTo = (int) (endDate / 10000);
		int monthDayTo = (int) (endDate % 10000);

		// 同年度の場合and閏年の場合
		if (yearFrom == yearTo && (yearFrom % 4 == 0 || yearFrom % 400 == 0)) {
			if ((monthDayFrom - 229 <= 0) && (monthDayTo - 229 >= 0)) {
				return 366d;
			}
			// 異年度の場合
		} else if (yearFrom != yearTo) {
			// nFromは閏年の場合and2/29を経過する or nToは閏年の場合and2/29を経過する
			if (((yearFrom % 4 == 0 || yearFrom % 400 == 0) && (monthDayFrom - 229 <= 0))
					|| ((yearTo % 4 == 0 || yearTo % 400 == 0) && (monthDayTo - 229 >= 0))) {
				return 366d;
			}
		} else {
			return 365d;
		}
		return 365d;
	}
	
	/**
	 * 
	 * @param kaisu 
	 * @param contractDay 契約日(yyyyMMdd)
	 * @param AnnuityBeginDate 年金支払日(yyyyMMdd)
	 * @return double
	 * @throws ParseException
	 */
	public static double getResponseBefore(double kaisu, double contractDay, double AnnuityBeginDate) throws ParseException {
		if (AnnuityBeginDate == 0) {
			return 0;
		}
		
		String strContractDay = String.valueOf((int)contractDay*1);
		String strAnnuityBeginDate = String.valueOf((int)AnnuityBeginDate*1);
		SimpleDateFormat sdf = new SimpleDateFormat(Const.YYYYMMDD);
		
		Date dateContractDay = sdf.parse(strContractDay);
		Calendar calContractDay = Calendar.getInstance();
		calContractDay.clear();
		calContractDay.setTime(dateContractDay);
		
		Date dateAnnuityBeginDate = sdf.parse(strAnnuityBeginDate);
		Calendar calAnnuityBeginDate = Calendar.getInstance();
		calAnnuityBeginDate.clear();
		calAnnuityBeginDate.setTime(dateAnnuityBeginDate);
		
		int dayForMonth = calAnnuityBeginDate.get(Calendar.DAY_OF_MONTH);
		int dayForYear = calAnnuityBeginDate.get(Calendar.DAY_OF_YEAR);
		
		if(kaisu == 2) {
			calContractDay.add(Calendar.DAY_OF_YEAR, -1);
			return dayForYear == calContractDay.get(Calendar.DAY_OF_YEAR) ? 0 : 1;
		}else if(kaisu == 3) {
			calContractDay.add(Calendar.DAY_OF_YEAR, -1);
			if(dayForYear == calContractDay.get(Calendar.DAY_OF_YEAR)){
				return 0;
			} else {
				calContractDay.add(Calendar.DAY_OF_YEAR, 183);
				if(calContractDay.get(Calendar.DAY_OF_YEAR) == calAnnuityBeginDate.get(Calendar.DAY_OF_YEAR)){
					return 0;
				} else {
					return 1;
				}
			}			
		}else if(kaisu == 4){
			if(dayForMonth == calAnnuityBeginDate.getActualMaximum(Calendar.DAY_OF_MONTH)) {
				if(calContractDay.get(Calendar.DAY_OF_MONTH) == 1) {
					return 0;
				} else {
					return 1;
				}
			}
			calContractDay.add(Calendar.DAY_OF_MONTH, -1);
			if (dayForMonth == calContractDay.get(Calendar.DAY_OF_MONTH) || dayForMonth > calContractDay.getActualMaximum(Calendar.DAY_OF_MONTH) ) {
				return 0;
			} else {
				return 1;
			}
		}else {
			return 1;
		}
	}
	public static void main(String[] args) throws ParseException{		
//		(6,birthday,contractDate)
//		double birthday[];
//		double contractDate[];
//		contractDate = new double[] { 20000101d, 20000110d, 20000131d, 20000201d, 20000401d, 20000301d, 20000402d, 20000501d, 20001231d, 20000102d, 20000131d, 20000201d, 20000301d, 20000401d, 20000401d, 20000501d, 20000501d, 20001231d, 20000501d, 20000501d, 20001231d, 20000101d, 20000101d, 20000110d, 20000101d, 20000101d, 20000110d, 20000201d, 20000201d, 20000201d, 20000201d, 20000301d, 20000402d, 20000403d, 20000501d, 20000110d, 20000131d, 20000201d, 20000401d, 20001231d, 20000501d, 20000501d, 20001231d};
//		birthday = new double[] { 19910101d, 19910110d, 19910131d, 19910201d, 19910401d, 19910301d, 19910402d, 19910501d, 19911231d, 19910101d, 19910102d, 19910101d, 19910228d, 19910101d, 19910102d, 19910101d, 19910401d, 19910110d, 19910402d, 19910430d, 19910402d, 19900102d, 19900401d, 19900301d, 19900402d, 19901231d, 19900501d, 19900301d, 19900401d, 19900402d, 19901231d, 19900501d, 19900403d, 19901231d, 19900601d, 19900101d, 19900102d, 19900101d, 19900110d, 19900401d, 19900301d, 19900402d, 19900501d};
//		for (int i = 0; i < birthday.length; i++) {
//			System.out.println(celebrationPolicyYear(15,birthday[i],contractDate[i]));
//		}
//		System.out.println(blSIx(0,20100520,20100620,20290520));
		System.out.println(sum_tJx(0,19,20100620,20100510,2020520));
//		System.out.println(sJx(0,20080101,20130309,20080310));
//		System.out.println(getResponseBefore(4,19940301,20000229));		
	}

}
