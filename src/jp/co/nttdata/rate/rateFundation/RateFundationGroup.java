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
 * ��p�^�����ƂɊTBL���i�[����
 * @author   btchoukug
 */
public class RateFundationGroup implements Cloneable  {
	
	private static Logger logger = LogFactory.getInstance(RateFundationGroup.class); 
	
	/**���ʁF�j���A����*/
	public enum Sex {male, female};
	
	/** X�{��:�S�{�́A�P�{�� */
	public enum Xtimes {x4, x1}
	
	/**
	 * �O���[�v�L�[��
	 * Fundation.XML�ɒ�`����Ƃ��A${generation}�̌`��
	 * �p�����[�^�Ƃ��Ă��g���Ă���
	 * */
	public static final String GEN = "generation"; //����
	public static final String DIVIDEND = "dividend"; //�z���L��
	public static final String PAYMENT = "payment"; //�������@
	public static final String PVH = "pvh"; //�v�Z��b 
	/**��O���[�v�L�[��suffix�F�v�Z��b*/
	public static final String PVH_VAR = "${pvh}";
	
	/**�\�藘��*/
	public static final String RATE = "rate";	
	/**������*/
	public static final String V = "v";
	/**�\����*/
	public static final String QW = "qw";
	/**�ŏI�N��*/
	public static final String OMEGA = "omega";
	/** x�{�� */
	private static final String X_TIMES = "x_times";
	
	/**���ʏ����tsuffix(�S�{��)*/
	public static final String SUFFIX_FUND_SPECIAL = "+X4";
	
	/**029�A016�A017��p�Ή��FW���[�g�v�Z����ۂɁA�ʂ̏��i�̊���g������*/
	public static final String SYS_FUND_CODE = "fundCode";
	
	private FundationGroupDef fundGroupDef;
	
	/** ��O���[�v�L�[*/
	private String groupKey;
	
	/** �\�藘�� */
	private double interest = 0d;
	/** ������ */
	private double v = 0d;
	/** ��� */
	private double qw = 0d;	
	
	public FundationGroupDef getFundGroupDef() {
		return fundGroupDef;
	}

	/**
	 * �O���[�v�L�[�̗v�f
	 * <br>�TBL�����܂�v�f�ł���
	 * <br>�\�藘��i�A�����\No�A�\���񗦂Ȃ�
	 */ 
	private DataRow fundTblParas;
	
	/** �p�^�[�����̊TBL */
	private Map<String, FundationTable> fundTbls = new HashMap<String, FundationTable>();
	
	/** �R���e�L�X�g�ɃA�N�Z�X����邽�߁A�p�^�[���}�[�W�����S�ʊ���i�[����}�b�v */
	private Map<String, Double[]> maleFundMap = new HashMap<String, Double[]>();
	private Map<String, Double[]> femaleFundMap = new HashMap<String, Double[]>();
	
	private List<Integer> maleOmega = new ArrayList<Integer>(3);	//���ʂ̏ꍇ�A�R�p�^�[���̊TBL������
	private List<Integer> femaleOmega = new ArrayList<Integer>(3);
	
	/** X�{�́i���ʂ�1�{�́i�W���́j�G���̂�4�{�́j */
	private Xtimes xtimesVal = Xtimes.x1;
	
	public RateFundationGroup(FundationGroupDef fundGrpDef) {
		this.fundGroupDef = fundGrpDef;
		List<FundationPatternDef> fundPtnList = this.fundGroupDef.getPatternList();
		
		// �TBL�̓p�^�����ɏ���������
		for (FundationPatternDef fundPtnDef : fundPtnList) {
			fundTbls.put(fundPtnDef.getPtn(), new FundationTable(fundPtnDef));
		}
	}
	
	/**
	 * FUNDATION XML�ɒ�`���ꂽgroupkey�̌`�i${generation}${payment}${dividend}${qw}�̂悤�Ȍ`�j�ɏ]���āA
	 * ���̓��[�g�L�[��PVH�Ƃ����g�ݍ��킹����R���g���[���� �O���[�v�L�[�����
	 * <p>���F�����_�ł͐���A�������@�A�z���A��񗦁A���PVH���O���[�v�L�[�Ƃ���</P>
	 * @return
	 */
	public String getGroupKey() {
		if (this.fundTblParas == null) {
			throw new IllegalArgumentException("����q���ݒ肳��Ȃ�����");
		}
		String groupKeyRule = this.fundGroupDef.getGroupkey() + PVH_VAR;
		// PVH�͑啶���Ƃ���
		this.fundTblParas.put(PVH, StringUtils.upperCase((String) this.fundTblParas.get(PVH)));
		this.groupKey = Interpolation.interpolate(groupKeyRule, this.fundTblParas);			
		
		if (this.xtimesVal == Xtimes.x4) {
			//���̂̏ꍇ�Asuffix�u+special�v��ǉ�
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
		
		// �V�X�e�����ځu�\�藘���v���Z�b�g
		this.interest = paras.getDouble("i");
		if (this.interest == 0d) {
			throw new FmsRuntimeException("�\�藘���̒l�͎擾�ł��Ȃ������F" + paras.toString());
		}

		// �V�X�e�����ځu������v�v���\�藘��i���v�Z���ăZ�b�g
		this.v = SystemFunctionUtility.roundDown(1d / (this.interest + 1d), 11);

		// �V�X�e�����ځu�\����qw�v���Z�b�g
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
			throw new FmsDefErrorException("�Y���p�^���̊����`����ĂȂ������F" + ptn);
		}
		return fundTbls.get(ptn);
	}
	
	/**
	 * ��N�G���[SQL�̃p�����[�^���쐬
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
	 * �����O���[�v�L�[�̏ꍇ�A��p�^�[�����ƂɊTBL�P�ʂō��ق���
	 * <p>�󂢂��TBL�����ّΏۂƂ���B</p>
	 * @param rfg
	 * @throws FmsDefErrorException 
	 */
	public void mergeWithAnther(RateFundationGroup rfg) throws FmsDefErrorException {
		if (!this.groupKey.equals(rfg.groupKey)) {
			throw new FmsDefErrorException("�����O���[�v�L�[�ł͂Ȃ����߁A���قł��܂���F" + this.groupKey + ":" + rfg.groupKey);
		}
		
		for (FundationPatternDef fundPtnDef : this.fundGroupDef.getPatternList()) {
			String ptn = fundPtnDef.getPtn();
			if (this.fundTbls.get(ptn).isEmpty()) {
				this.fundTbls.put(ptn, rfg.getFundTbls().get(ptn));
			}
		}
	}

	/**
	 * ��p�^�[�����ƂɊTBL�P�ʂō��ق���
	 * <p>�Ō�ł͂��ꂼ��̕����I�ȊTBL���P���W�b�N�I�ȊTBL�ɂ���</p>
	 * @return RateFundationGroup
	 * @throws FmsDefErrorException 
	 */
	public RateFundationGroup merge() throws FmsDefErrorException {
		//�p�^�[���P�ʂ̊Map���A�N�Z�X���₷�����߂P�ɍ��ق���
		for(FundationTable ptnTbl : this.fundTbls.values()){
			this.maleFundMap.putAll(ptnTbl.getMaleFundMap());
			this.femaleFundMap.putAll(ptnTbl.getFemaleFundMap());
			this.maleOmega.add(ptnTbl.getMaleOmega());
			this.femaleOmega.add(ptnTbl.getFemaleOmega());
		}
		
		//��������(����)���ҏW���s��
		_editFundtionByLimitCondtion(this.fundTblParas);
		
		//�����\�[�g
		Collections.sort(this.femaleOmega);
		Collections.sort(this.maleOmega);
		
		return this;
	}

	/**
	 * �p�^�[�����Ƃɍŏ��̍ŏI�N���Ԃ�
	 * @return
	 */
	public int getOmega(int sex) {
		
		int omega = 0;
		
		if (Const.SEX_MALE_0 == sex) {
			//�p�^�����Ƃ̊TBK�̂����ɍŏ��̃ւ�Ԃ�
			omega = this.maleOmega.get(0);
		} else {
			omega = this.femaleOmega.get(0);
		}

		return omega;
	}
	
	/**
	 * �v�Z�L���͌v�Z��b���ۂ����f����
	 * 
	 * @param tokenName
	 * @return
	 */
	public boolean isFundation(String tokenName) {
		return this.fundGroupDef.getFundationNameList().contains(tokenName);
	}
	
	/**
	 * �TBL���[�h���Ă��琫�ʐ�������̕ҏW���s��
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
		
		//��r���邽�߁A�������ɂ���
		limitSex = StringUtils.lowerCase(limitSex);
		
		// ���ʌ��肠��̏ꍇ
		if (StringUtils.isNotBlank(limitSex)) {
			if (logger.isInfoEnabled()) {
				logger.info(MessageFormat.format("{0}�ɂ͐��ʌ��肠��F{1}", this.fundTbls.values() + "@" + this.groupKey, limitSex));
			}
			if (Const.MALE.equals(limitSex)) {
				// �j���Ƃ���
				this.femaleFundMap = this.maleFundMap;
			} else if (Const.FEMALE.equals(limitSex)) {
				// �����Ƃ���
				this.maleFundMap = this.femaleFundMap;
			} else {
				throw new FmsDefErrorException("���ʐ����ɂ͐������ݒ肳��ĂȂ������F" + limitSex);
			}
		}
	}
	
}
