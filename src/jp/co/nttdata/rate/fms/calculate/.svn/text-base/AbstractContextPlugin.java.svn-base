package jp.co.nttdata.rate.fms.calculate;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import jp.co.nttdata.rate.exception.FmsRuntimeException;
import jp.co.nttdata.rate.rateFundation.dbConnection.DBConnection;
import jp.co.nttdata.rate.rateFundation.dbConnection.DataRow;
import jp.co.nttdata.rate.util.Const;

import org.apache.commons.collections.MapUtils;

/**
 * 世代訂正処理を行う
 * @author zhanghy
 *
 */
public class AbstractContextPlugin implements ContextPlugin {
	
	/** 商品コードより有効な世代を取得SQL */
	private final static String INSURANCE_GEN_SQL = "select generation "
				+ "from rate_master where insurance_code = ${code} "
				+ "group by generation";

	private static final String FIELD_GENERATION = "generation";
	
	/** 商品コード取得 */
	private static final String RK_INSURANCECODE = "insuranceCode";
	
	/** 世代を格納するリスト */
	private List<Integer> generationList = new ArrayList<Integer>();
	
	/**配当計算開始日（契約年月日）*/
	private static final String RATEKEY_RATE_STARTDATE = "contractDate";
	
	private static final String RATEKEY_T = "t";
	private static final String RATEKEY_T1 = "t1";
	private static final String RATEKEY_F1 = "f1";
	private static final String RATEKEY_027FLG = "flg";
	
	RateCalculateContext _ctx;
	String insCode;
	
	public AbstractContextPlugin(RateCalculateContext ctx) {
		_ctx = ctx;
		insCode = ctx.insuranceCode;
		_getGenerationList(insCode);
	}

	@SuppressWarnings("unchecked")
	@Override
	public void contextHandle() {
		
		double contractDate = MapUtils.getDoubleValue(_ctx.tokenValueMap, RATEKEY_RATE_STARTDATE);
		_ctx.tokenValueMap.put(RateCalculateContext.RATEKEY_GEN, justifyGeneration(contractDate));
		_ctx.tokenValueMap.put(RateCalculateContext.RATEKEY_XTIME, Const.X_TIMES_DEFAULT);
		if ("027".equals(insCode)) {
			_ctx.tokenValueMap.put(RATEKEY_027FLG, 0);
			double t = MapUtils.getDoubleValue(_ctx.tokenValueMap, RATEKEY_T);
			double t1 = MapUtils.getDoubleValue(_ctx.tokenValueMap, RATEKEY_T1);
			double f1 = MapUtils.getDoubleValue(_ctx.tokenValueMap, RATEKEY_F1);
			if ((t1 == t + 1) && f1 == 0) {
				_ctx.tokenValueMap.put(RATEKEY_027FLG, 1);
			}
		}
	}

	/**
	 * 契約日より世代を求める
	 * <br>DB上の設定より一部訂正も行う
	 * @param contractDate
	 * @return
	 */
	protected int justifyGeneration(double contractDate) {
		int inputGen = 0;
		int insuranceCode = MapUtils.getIntValue(_ctx.tokenValueMap, RK_INSURANCECODE);
		/*
		 *  042の世代の開始日と終了日は、他の商品と違っているため。
		 *  030の世代５の開始日は、他の商品と違っているため。  
		 *    
		 */
		if ("042".equals(insCode)) {
			
			if (contractDate <= 20090401d) {
				inputGen = 4;
			} else if (contractDate <= 20100930d) {
				inputGen = 5;
			} else if (contractDate <= 20110331d) {
				inputGen = 6;
			} else if (contractDate <= 20111231d) {
				inputGen = 7;
			} else {
				inputGen = 8;
			}
			
		} else if ("030".equals(insCode) || "311".equals(insCode)) {
			if (contractDate >= 20081201d) {
				return 5;
			} else {
				throw new FmsRuntimeException(insCode + "商品にて契約日の有効範囲は「20081201～99999999」です:" + String.valueOf(contractDate));
			}
		} else if (	insuranceCode == 31 || insuranceCode == 33 ||
					insuranceCode == 235 || insuranceCode == 237 ||
					insuranceCode == 261 || insuranceCode == 263 ||
					insuranceCode == 265 || insuranceCode == 267
					) {
			if (contractDate <= 19990401d) {
				inputGen = 1;
			} else if (contractDate <= 20010401d) {
				inputGen = 2;
			} else if (contractDate <= 20070401d) {
				inputGen = 3;
			} else {
				inputGen = 4;
			}			
		} else {
			if (contractDate <= 19990401d) {
				inputGen = 1;
			} else if (contractDate <= 20010401d) {
				inputGen = 2;
			} else if (contractDate <= 20070401d) {
				inputGen = 3;
			} else if (contractDate <= 20090331d) {
				inputGen = 4;
			} else if (contractDate <= 20100930d){
				inputGen = 5;
			} else if (contractDate <= 20110401d){
				inputGen = 6;
			} else if (contractDate <= 20111231d) {
				inputGen = 7;
			} else if (contractDate <= 20120731){
				inputGen = 8;
			} else {
				inputGen = 9;
			}
		}
		
		
		/*
		 *  最大世代から最小世代まで、１ずつ比較し一番近いの世代に読み替え
		 *  ケース１：入力は世代５、DB上の最大世代が４となってるため、最後では４と見なす；
		 *  ケース２：入力は世代５、DB上は連続ではなく、１～４,６,７と設定されて、最後は４とする;
		 *  ケース３：016(017)の場合、一時払　かつ　世代３を入力、
		 *  DB上は一時払が世代２だけを設定されて、最後は世代２とする。 	
		 *    
		 */
		int minDBGen = generationList.get(0);
		if (inputGen < minDBGen) {
			throw new FmsRuntimeException(MessageFormat.format(
					"{0}商品にて世代{1}からです、契約日{2}は世代{3}なので計算できない。", insCode, minDBGen,
					String.valueOf(contractDate), inputGen));
		}
		
		int maxDBGen = generationList.get(generationList.size() - 1);		
		
		if ("016".equals(insCode) || "017".equals(insCode)) {
			int kaisu = MapUtils.getIntValue(_ctx.tokenValueMap, RateCalculateContext.RATEKEY_PAYMENT); 
			int partOnetime = MapUtils.getIntValue(_ctx.tokenValueMap, RateCalculateContext.RATEKEY_PARTONETIME);
			// 一時払の場合(一部一時払の場合は分割払いと見なす)
			if (kaisu == 1 && partOnetime == 0) {
				// ケース３
				return 2;
			}
		}
		
		if (inputGen > maxDBGen) {
			//ケース１
			inputGen = maxDBGen;
		} else {
			//ケース２
			while (!generationList.contains(inputGen)) {
				inputGen --;
			} 
		}

		// 訂正後の世代を返す		
		return inputGen;

	}
	
	/**
	 * 商品コードより有効な世代を取得
	 * 
	 * @param aInsuranceNo
	 * @return String[]
	 */
	private void _getGenerationList(String code) {
		
		List<DataRow> list = DBConnection.getInstance().query(INSURANCE_GEN_SQL, "code", code);
		DBConnection.getInstance().close();
				
		// 当該商品について有効な世代を一ずつ取得
		for (DataRow data : list) {
			generationList.add(data.getInt(FIELD_GENERATION));
		}		
		// 昇順でソートする
		Collections.sort(generationList);
	}

}
