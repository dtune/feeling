package jp.co.nttdata.rate.fms.calculate;

import java.util.List;

import jp.co.nttdata.rate.exception.FmsRuntimeException;
import jp.co.nttdata.rate.log.LogFactory;
import jp.co.nttdata.rate.rateFundation.dbConnection.DBConnection;
import jp.co.nttdata.rate.rateFundation.dbConnection.DataRow;

import org.apache.commons.collections.MapUtils;
import org.apache.log4j.Logger;

public class ContextPlugin043 extends AbstractContextPlugin {

	protected Logger logger = LogFactory.getInstance(ContextPlugin043.class);
	
	/**予定利率*/
	private static final String RATEKEY_IJ = "ij";
	/**新契約の積立利率*/
	private static final String RATEKEY_IJNASHU = "ij_nashu";
	/**解約時の積立利率*/
	private static final String RATEKEY_IW = "iw";
	/**積立利率保証期間*/
	private static final String RATEKEY_NJ = "nj";
	/**指標金利*/
	private static final String RATEKEY_IA = "ia";
	/**積立利率変動率*/
	private static final String RATEKEY_IB = "ib";
	/**解約年月日*/
	private static final String RATEKEY_STD_DATE = "standardDate";	
	/**契約日*/
	private static final String RATEKEY_CONTRACT_DATE = "contractDate";
	
	private static final String FIELD_APPLY_DATE = "applyDate";
	
	/** 積立利率を求めるSQL文 */
	private final static String IJ_SQL = "select nj,ij_nashu,ia,ib,gamma_nashu,q_nashu,applyDate from rate_ij_master order by applyDate desc;";
	
	/** 積立利率リスト */
	private List<DataRow> ijList;
	
	public ContextPlugin043(RateCalculateContext ctx) {
		super(ctx);
		
		ijList = DBConnection.getInstance().query(IJ_SQL, null);
		DBConnection.getInstance().close();
		
		if (ijList.size() == 0) {
			throw new FmsRuntimeException("積立利率テーブルにはマスタデータが設定されてなかった");
		}
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public void contextHandle() {
		super.contextHandle();
		
		if (_ctx.tokenValueMap.containsKey(RATEKEY_STD_DATE)) {
			//解約時の積立利率を算出
			int date = MapUtils.getInteger(_ctx.tokenValueMap,RATEKEY_STD_DATE);				
			_ctx.tokenValueMap.put(RATEKEY_IW, _getIjNashu(date));
		}
		
		if (!_ctx.tokenValueMap.containsKey(RATEKEY_CONTRACT_DATE)) {
			throw new FmsRuntimeException("043変動型個人年金の計算のため、契約日が必要です" + RATEKEY_CONTRACT_DATE);
		}
		
		//新契約の積立利率を算出
		int date = MapUtils.getInteger(_ctx.tokenValueMap,RATEKEY_CONTRACT_DATE);
		_ctx.tokenValueMap.put(RATEKEY_IJNASHU, _getIjNashu(date));
		
		if (logger.isInfoEnabled()) {
			logger.info("入力のレートキー（積立利率取得後）：" + _ctx.tokenValueMap.toString());
		}
	}
	
	/**
	 * 043商品にて積立利率ij_nashuを取得する(統計側のため、ijも取得する)
	 * @param date 
	 * @return　ij_nashu
	 */
	@SuppressWarnings("unchecked")
	private double _getIjNashu(int date) {
		
		for (DataRow data : this.ijList) {
			int applyDate = data.getInt(FIELD_APPLY_DATE);
			int nj = data.getInt(RATEKEY_NJ);
			//入力レートキーより条件を判断する
			if (nj == MapUtils.getInteger(_ctx.tokenValueMap,RATEKEY_NJ)
					&& date >= applyDate) {
				double ia = data.getDouble(RATEKEY_IA);
				double ib = data.getDouble(RATEKEY_IB);
				_ctx.tokenValueMap.put(RATEKEY_IJ, (ia - ib));
				// 積立利率を取得
				return data.getDouble(RATEKEY_IJNASHU);
			}
		}
		//最後まで一つでも満たさない場合、0を返す（積立利率がない）
		_ctx.tokenValueMap.put(RATEKEY_IJ, 0d);
		return 0d;
	}

}
