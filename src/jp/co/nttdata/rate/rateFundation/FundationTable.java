package jp.co.nttdata.rate.rateFundation;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jp.co.nttdata.rate.exception.FmsDefErrorException;
import jp.co.nttdata.rate.rateFundation.RateFundationGroup.Sex;

/**
 * ���ʒP�ʂ�Nx,Mx�Ȃǂ̊�����N��Ƃɔz��̌`�Ɋi�[����G���e�B�e�B�ł���
 * <p>�ŏI�N��ւ�����</p>
 * @author jiangy
 *
 */
public class FundationTable implements IFundation {
	
	public static int MAX_AGE = 127;
	
	/** �ŏI�N�� (�j��)*/
	private int maleOmega = 0;
	/** �ŏI�N�� (����)*/
	private int femaleOmega = 0;
	
	/** �TBL�ԍ� */
	private String fundTblNo;
	
	private FundationPatternDef fundPtnDef;
	
	/** ������ƂɊi�[����}�b�v */
	private Map<String, Double[]> maleFundMap = new HashMap<String, Double[]>();
	private Map<String, Double[]> femaleFundMap = new HashMap<String, Double[]>();
	
	public FundationTable(FundationPatternDef ptnDef) {
		
		this.fundPtnDef = ptnDef;
		List<FundationDef> fundList = this.fundPtnDef.getFundList();
		int len = fundList.size();
		for (int i = 0; i < len; i++) {
			FundationDef fundDef = fundList.get(i);			
			// ���`�ǂ���Ɋ�z���������
			maleFundMap.put(fundDef.getName(), _newFundArray());
			femaleFundMap.put(fundDef.getName(), _newFundArray());
		}
		
	}
		
	private Double[] _newFundArray() {
		// �����\�Q�ɂ�16�ΈȑO�͂Ȃ��̂ŁA�S��0�Ƃ���
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
			throw new FmsDefErrorException("��L���u" + name + "�v���擾����Ȃ�");
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
	 * ����y�єN��Ɛ��ʂŊ��ҏW
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
	 * ���Y�TBL��DB�����l�����[�h���ꂽ���Ȃ���
	 * <br>�TBL�ԍ����Ȃ��Ƃ������� 
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
