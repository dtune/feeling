package jp.co.nttdata.rate.batch;

import java.util.Map;
import jp.co.nttdata.rate.rateFundation.IRateFundationProvider;

public class RegressionDataFilter implements IBatchDataFilter {

	protected IRateFundationProvider _fundProvider;
	public RegressionDataFilter(IRateFundationProvider fundProvider) {
		_fundProvider= fundProvider;
	}
	
	@Override
	public boolean filter(Map<String, Double> rateKeys) {

		return _xPlustOveromega(rateKeys) || _isNGData(rateKeys);
	}

	private boolean _xPlustOveromega (Map<String, Double> rateKeys) {
		//FIXME: 027など全パターン解約率がある場合、修正要
//		Map groupKeyParas = new HashMap();
//		groupKeyParas.put(IRateFundationProvider.GEN, rateKeys.get("gen").intValue());
//		groupKeyParas.put(IRateFundationProvider.PAYMENT, _getPayment(rateKeys));
//		groupKeyParas.put(IRateFundationProvider.QW, 0.0d);	//最終年齢ωは解約率qwに関わらず
//		groupKeyParas.put(IRateFundationProvider.DIVIDEND, 0); //最終年齢ωは利差配当有無に関わらず
//		groupKeyParas.put(IRateFundationProvider.PVH, "P"); //最終年齢ωは計算基礎に関わらず
//
//		if(rateKeys.get("insuranceCode")==27){
//			groupKeyParas.put(IRateFundationProvider.QW, 0.03d);
//		}

//		String groupKey = _fundProvider.editFundationGroupKey(groupKeyParas);
//		int sex = rateKeys.get("sex").intValue();
//		int omega = (Integer)_fundProvider.loadFundation(sex, groupKey).get(RateFundationGroup.OMEGA);
//		if(rateKeys.containsKey("x") && rateKeys.containsKey("t")){
//			return rateKeys.get("x") + rateKeys.get("t") > omega;
//		} else {
			return false;
//		}
	}
	
	/**
	 * 
	 * @param rateKeys
	 * @return 一時払：1;分割払:2
	 */
	private int _getPayment (Map<String, Double> rateKeys) {
		
		if(rateKeys.containsKey("partOnetime")) {
			return rateKeys.get("partOnetime").intValue() % 2 + 1;
		}
		return rateKeys.get("kaisu").intValue() == 1 ? 1 : 2;
	}
	
	/**
	 * 
	 * @param rateKeys
	 * @return NGデータ：true;計算できるデータ:false
	 */
	private boolean _isNGData (Map<String, Double> rateKeys) {
		
		if(rateKeys.containsKey("isNGflag")) {
			return rateKeys.get("isNGflag").intValue() == 1;
		} else {
			return false;
		}
	}
}
