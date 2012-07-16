package jp.co.nttdata.rate.rateFundation;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jp.co.nttdata.rate.exception.FmsDefErrorException;
import jp.co.nttdata.rate.rateFundation.RateFundationGroup.Sex;

/**
 * 性別単位でNx,Mxなどの基数をｘ年齢ごとに配列の形に格納するエンティティである
 * <p>最終年齢ωも持つ</p>
 * @author jiangy
 *
 */
public class FundationTable implements IFundation {
	
	public static int MAX_AGE = 127;
	
	/** 最終年齢 (男性)*/
	private int maleOmega = 0;
	/** 最終年齢 (女性)*/
	private int femaleOmega = 0;
	
	/** 基数TBL番号 */
	private String fundTblNo;
	
	private FundationPatternDef fundPtnDef;
	
	/** 基数名ごとに格納するマップ */
	private Map<String, Double[]> maleFundMap = new HashMap<String, Double[]>();
	private Map<String, Double[]> femaleFundMap = new HashMap<String, Double[]>();
	
	public FundationTable(FundationPatternDef ptnDef) {
		
		this.fundPtnDef = ptnDef;
		List<FundationDef> fundList = this.fundPtnDef.getFundList();
		int len = fundList.size();
		for (int i = 0; i < len; i++) {
			FundationDef fundDef = fundList.get(i);			
			// 基数定義どおりに基数配列を初期化
			maleFundMap.put(fundDef.getName(), _newFundArray());
			femaleFundMap.put(fundDef.getName(), _newFundArray());
		}
		
	}
		
	private Double[] _newFundArray() {
		// 生命表２には16歳以前はないので、全部0とする
		Double[] array = new Double[MAX_AGE];
		for (int i = 0; i < array.length; i++) {
			array[i] = 0d;
		}
		return array;
	}

	@Override
	public Double[] getFundation(String name, Sex sex) throws FmsDefErrorException {
		Map<String, Double[]> fundMap = _getFundTblBySex(sex);
		if (!fundMap.containsKey(name)) {
			throw new FmsDefErrorException("基数記号「" + name + "」が取得されない");
		}
		return fundMap.get(name);
	}
	
	public FundationPatternDef getFundPtnDef() {
		return this.fundPtnDef;
	}
	
	private Map<String, Double[]> _getFundTblBySex(Sex sex) {
		if (Sex.male == sex) {
			return this.maleFundMap;
		} else {
			return this.femaleFundMap;
		}
	}
	
	/**
	 * 基数名及び年齢と性別で基数を編集
	 * @param fundName
	 * @param age
	 * @param fundVal
	 */
	public void setFund(String fundName, Sex sex, int age, double fundVal) {
		_getFundTblBySex(sex).get(fundName)[age] = fundVal;		
	}

	public void setFundTblNo(String fundTblNo) {
		this.fundTblNo = fundTblNo;
	}

	public String getFundTblNo() {
		return fundTblNo;
	}
	
	public String toString() {
		return "FundationTable:" + this.fundTblNo + "#ptn" + this.fundPtnDef.toString();
	}
	
	/**
	 * 当該基数TBLがDBから基数値をロードされたかないか
	 * <br>基数TBL番号がないということ 
	 * @return
	 */
	public boolean isEmpty() {
		return this.fundTblNo == null ? true : false;
	}

	public void setMaleOmega(int maleOmega) {
		this.maleOmega = maleOmega;
	}

	public int getMaleOmega() {
		return maleOmega;
	}

	public void setFemaleOmega(int femaleOmega) {
		this.femaleOmega = femaleOmega;
	}

	public int getFemaleOmega() {
		return femaleOmega;
	}

	public Map<String, Double[]> getMaleFundMap() {
		return maleFundMap;
	}

	public Map<String, Double[]> getFemaleFundMap() {
		return femaleFundMap;
	}

}
