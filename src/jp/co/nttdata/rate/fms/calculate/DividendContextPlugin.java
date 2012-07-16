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
 * �z�����v�Z�����̔z�����Ȃǂ��擾
 * @author zhanghy
 *
 */
public class DividendContextPlugin extends AbstractContextPlugin {

	protected Logger logger = LogFactory.getInstance(DividendContextPlugin.class);
	
	/**�ی�����*/
	private static final String RATEKEY_N = "n";
	/**��������*/
	private static final String RATEKEY_M = "m";
	/** �����i�񐔁j */
	private static final String RATEKEY_PAYMENT = "kaisu";
	/** �ꕔ�ꎞ�����𔻖����鍀�� */
	private static final String RATEKEY_PARTONETIME = "partOnetime";
	/**�J�艺���N��*/
	public static final String RATEKEY_L = "l";
	/**�o�ߔN*/
	private static final String RATEKEY_T = "t";
	private static final String RATEKEY_T1 = "t1";
	private static final String RATEKEY_T2 = "t2";
	private static final String RATEKEY_T3 = "t3";
	private static final String RATEKEY_POLICYT = "policyT";
	/**�����N��*/
	private static final String RATEKEY_TEX = "tEX";
	/**�o�ߌ�*/
	private static final String RATEKEY_F = "f";
	private static final String RATEKEY_F1 = "f1";
	private static final String RATEKEY_F2 = "f2";
	private static final String RATEKEY_F3 = "f3";
	/**��������*/
	private static final String RATEKEY_FEX = "fEX";
	/**���*/
	private static final String RATEKEY_STATE = "state";
	/**������*/
	private static final String RATEKEY_KAISU = "kaisu";
	/**�����ςݕ��z�z���v�Z����Ƃ��A���̏��*/
	private static final String RATEKEY_STATETEMP = "stateTemp";
	/**�z�����v�Z�N��*/
	private static final String RATEKEY_DIVIDENDYEAR = "dividendYear";
	/**�z���v�Z�J�n���i�_��N�����j*/
	private static final String RATEKEY_RATE_STARTDATE = "contractDate";
	/**�ӔC�J�n�N����*/
	private static final String RATEKEY_RATE_SEKINI_DATE = "responsibilityDate";
	/**��_��̌_���*/
	private static final String RATEKEY_RATE_POLICYSTARTDATE = "policyContractDate";
	/**��_��̕���*/
	private static final String RATEKEY_RATE_POLICYKAISU = "policyKaisu";
	/**�z�����*/
	private static final String RATEKEY_RATE_ENDDATE = "divEndDate";
	/**�z����*/
	private static final String RATEKEY_DIV_ID = "id";
	/**�z���ϗ���*/
	private static final String RATEKEY_DIV_ID_NASHU = "id_nashu";
	/**�A�Z�b�g�V�F�A��*/
	private static final String RATEKEY_DIV_ACCID = "accId";
	/**�A�Z�b�g�V�F�A�ϗ���*/
	private static final String RATEKEY_DIV_ACCID_NASHU = "accId_nashu";
	/**�ϗ�����*/
	private static final String RATEKEY_DIV_RATEA = "rateA";
	private static final String RATEKEY_DIV_RATEB = "rateB";
	/**����t���̎�_��R�[�h*/
	private static final String RATEKEY_POLICYCODE = "policyCode";
	/**�O���ƔN�x��������͔������܂ł�,�e���t���}���邩�ǂ���*/
	private static final String RATEKEY_ISCOMEOFDATE = "isComeOfDate";
	/**�O���ƔN�x�����}���Ă��Ȃ����ǂ���*/
	private static final String RATEKEY_ISANNUITYBEGIN = "isAnnuityBegin";
	/**���Y���ƔN�x���ɕی����Ԃ̏I�����}���邩�ǂ���*/
	private static final String RATEKEY_ISENDOFN = "isEndOfN";
	/**4/1���o�߂��邩�ǂ����𔻒f����*/
	private static final String RATEKEY_DIV_ISVALUECHANGED = "isValueChanged";
	/**�ϗ������ϓ���*/
	private static final String RATEKEY_DIV_CHANGEDATEA = "changeDateA";
	private static final String RATEKEY_DIV_CHANGEDATEB = "changeDateB";
	/**�{��N�������敪*/
	private static final String RATEKEY_YOUIKUNENKIN = "youikunenkin";
	/**�N���x���J�n�N����*/
	private static final String RATEKEY_ANNUITY_BEGIN_DATE = "AnnuityBeginDate";
	/**���͔�����*/
	private static final String RATEKEY_EFFECT_BEGIN_DATE = "effectBeginDate";
	/**���N�������N����*/
	private static final String RATEKEY_OLDANNUITY_BUY_DATE = "oldAnnuityBuyDate";
	/**�ŐV�N�������N��*/
	private static final String RATEKEY_NEWANNUITY_BUY_DATE = "newAnnuityBuyDate";
	/**�{��N���x���o�ߔN*/
	private static final String RATEKEY_YOUIKU_ANNUITY_T = "youikuAnnuityT";
	/**�{��N���x���o�ߌ�*/
	private static final String RATEKEY_YOUIKU_ANNUITY_F = "youikuAnnuityF";
	/**��ԕύX�t���O*/
	private static final String RATEKEY_STATE_CHANGED = "StateChanged";
	/**���Z�z�̏�Ԕ��f*/
	private static final String RATEKEY_STATE_DT_PLUS = "StateDtPlus";
	/**�N���x������*/
	private static final String RATEKEY_G = "g";
	/**�N���̎��*/
	private static final String RATEKEY_ANNUITYTYPE = "annuityType";
	/**���z���ƌ_����������̃t���O*/
	private static final String RATEKEY_RATE_XN = "xN";
	/**�v�Z���*/
	private static final String RATEKEY_RATE_STANDARDDATE = "standardDate";
	/**�v�Z�Ώ�*/
	private static final String RATEKEY_RATE_KEISANPTN = "keisanPtn";
	/**�ٓ����*/
	private static final String RATEKEY_RATE_CHANGESTATE = "changeState";
	/**�_�񌻋�*/
	private static final String RATEKEY_RATE_CONSTATE = "contractorState";
	/**�����ƔN�x���N����*/
	private static final String RATEKEY_RATE_DIVTHISYEAR = "dividendThisYear";
	/**�O���ƔN�x���N����*/
	private static final String RATEKEY_RATE_DIVLASYEAR = "dividendLastYear";
	/**���i�R�[�h*/
	private static final String RATEKEY_RATE_INSCODE = "insuranceCode";
	
	/** �z���������߂�SQL�� */
	private final static String DIVIDEND_SQL = "SELECT di.account_code,di.term_code,di.policy_year,di.payment,di.validate_from,"
		+"di.validate_to,di.bonus_year,di.rate,di.dividend_rate,di.acc_rate,di.acc_div_rate "
		+ "FROM `rate_dividend_master` di where di.insurance_code=${code};";
	
	/** ���ԋ敪�����߂�SQL�� */
	private final static String TERM_SQL = "SELECT term.condition,term.term_code "
			+ "FROM `rate_term_master` term where term.insurance_code=${code};";
	
	/** �z�������X�g */
	private List<DataRow> dividendList = null;
	/** ���ԋ敪���X�g */
	private List<DataRow> termConditonlist = null;
	
	public DividendContextPlugin(RateCalculateContext ctx) {
		super(ctx);
		termConditonlist = DBConnection.getInstance().query(TERM_SQL, "code", ctx.insuranceCode);
		if (termConditonlist.size() == 0) {
			throw new FmsRuntimeException("���ԋ敪�e�[�u���ɂ̓}�X�^�f�[�^���ݒ肳��ĂȂ�����");
		}
		dividendList = DBConnection.getInstance().query(DIVIDEND_SQL, "code", ctx.insuranceCode);
		if (dividendList.size() == 0) {
			throw new FmsRuntimeException("�z�����e�[�u���ɂ̓}�X�^�f�[�^���ݒ肳��ĂȂ�����");
		}
		
		DBConnection.getInstance().close();
	}

	@SuppressWarnings("unchecked")
	@Override
	public void contextHandle() {
		
		//�n���N����
		double contractDate = MapUtils.getDoubleValue(_ctx.tokenValueMap, RATEKEY_RATE_STARTDATE);
		//�ӔC�J�n�N����
		double sekiniDate = MapUtils.getDoubleValue(_ctx.tokenValueMap, RATEKEY_RATE_SEKINI_DATE);
		//xtime
		_ctx.tokenValueMap.put(RateCalculateContext.RATEKEY_XTIME, Const.X_TIMES_DEFAULT);
		
		//�z���v�Z�A����̏ꍇ�ӔC�J�n���Ǝn���N�����傫���������Ƃɐ�����Z�o
		_ctx.tokenValueMap.put(RateCalculateContext.RATEKEY_GEN, justifyGeneration(Integer.parseInt(_ctx.insuranceCode) > 300 && sekiniDate > contractDate ? sekiniDate : contractDate));
		
		if (_ctx.tokenValueMap.containsKey(RATEKEY_RATE_ENDDATE)) {
			if ("931".equals(_ctx.insuranceCode)) {
				_ctx.tokenValueMap.put(RATEKEY_RATE_INSCODE, _ctx.insuranceCode);
				_ctx.tokenValueMap.put(RATEKEY_STATE, 3);
				_ctx.tokenValueMap.put(RATEKEY_M, 0);
			}
			// �v�Z�����ݒ�
			_ctx.tokenValueMap.put(RATEKEY_RATE_STANDARDDATE, 
					MapUtils.getInteger(_ctx.tokenValueMap, RATEKEY_RATE_ENDDATE));
			// t,f,t1,f1��ݒ�
			_editElapsedYearMonth(_ctx.tokenValueMap, true);
			// �z�������擾����
			_editDividendRateValue(_ctx.tokenValueMap);
			// �ϗ��������擾����
			_editDivRateValue(_ctx.tokenValueMap);
			// ���Y���ƔN�x���ɕی����Ԃ̏I�����}���邩�ǂ����𔻒�
			_editIsEndOfN(_ctx.tokenValueMap);
			if ("004".equals(_ctx.insuranceCode) || "042".equals(_ctx.insuranceCode)
					|| "308".equals(_ctx.insuranceCode)) {
				// �N�������J�n��A�N�������N�����擾����
				_editAnnuityPaidYearMonth(_ctx.tokenValueMap);
			}
			/* 
			 * �{��N���o�ߌ��̐ݒ�
			 * �{��N���������ǂ����̔��肪���\�b�h�̒��ɂ���
			 */
			if ("009".equals(_ctx.insuranceCode)){
				_editAnnuityBeginMonth(_ctx.tokenValueMap);
			}
			/*
			 * ���Y���ƔN�x���Ɉȉ��̂�������������������Ƃ������肷��
			 * �ی����������Ԃ̏I��
			 * �N���x�����̊J�n
			 * �N���J�艺�����Ԃ̊J�n
			 * �ی����Ԃ̏I��(�폜)
			 */
			_editStateChange(_ctx.tokenValueMap);
			// �v�Z�Ώۂ��擾����
			int keisanPtn = MapUtils.getIntValue(_ctx.tokenValueMap, RATEKEY_RATE_KEISANPTN);
			
			// �O���ƔN�x��������͔������܂ł�,�e���t���}���邩�ǂ����𔻒�
			_editIsComeOfDate(_ctx.tokenValueMap, keisanPtn);
			// �O���ƔN�x�����}���Ă��Ȃ����ǂ���
			_editIsAnnuityBegin(_ctx.tokenValueMap, keisanPtn);
			// ���Z�z�̏�Ԕ��f
			_editDtNashuPlus(_ctx.tokenValueMap, keisanPtn);
			// �����ςݕ��z�z���v�Z����Ƃ��A���̏�Ԃ�ݒ肷��
			_editStateTemp(_ctx.tokenValueMap);
			if (logger.isInfoEnabled()) {
				logger.info("���͂̃��[�g�L�[�i�z�����Ȃǎ擾��j�F" + _ctx.tokenValueMap.toString());
			}
		}
	}
	
	/**
	 * �����ςݕ��z�z���v�Z����Ƃ��A���̏�Ԃ�ݒ肷��
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
			// �����ςݕ��z�z���v�Z����Ƃ��A���̏�ԃf�t�H���g�̏ꍇ�A���͏�Ԃ�ݒ�
			rateKeyMap.put(RATEKEY_STATETEMP, MapUtils.getInteger(rateKeyMap, RATEKEY_STATE));
		}
	}
	
	/**
	 * ���[�g�L�[���A���̏�Ԃ𔻒f����
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
			if (t >= n + l) {// �N���J�n��
				return 3;
			} else if (t >= n && n + l > t) {// �J�艺��
				return 5;
			}
		}
		if ("308".equals(_ctx.insuranceCode)) {
			int annuityBeginDate = MapUtils.getInteger(rateKeyMap, RATEKEY_ANNUITY_BEGIN_DATE);
			if (annuityBeginDate > 0 && t >= n) {// �N���J�n��
				return 3;
			} else {
				return 2;
			}
		}
		int kaisu = MapUtils.getInteger(rateKeyMap, RATEKEY_KAISU);
		// �������Ԓ�
		if (t < m) {
			return 1;
		} else if (m == n && t >= m || m != n && (t >= m && n > t) || (m == 0 && kaisu == 1)) {// �������ԏI����
			return 2;
		}
		throw new FmsRuntimeException("���f�[�^�̏�Ԃ����݂��Ă��܂���B");
	}
	
	/**
	 * ���Y���ƔN�x���Ɍ_���ԕύX�������������Ƃ������肷��
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
	 * ���i004�p
	 * ���Y���ƔN�x���Ɍ_���ԕύX�������������Ƃ������肷��
	 * @param rateKeyMap
	 * @param t
	 */
	@SuppressWarnings("unchecked")
	private void _editStateChange004(Map rateKeyMap, int t){
		int n = MapUtils.getInteger(rateKeyMap, RATEKEY_N);
		int m = MapUtils.getInteger(rateKeyMap, RATEKEY_M);
		int l = MapUtils.getInteger(rateKeyMap, RATEKEY_L);
		if(t == m){
			//�ی����������Ԃ̏I��
			rateKeyMap.put(RATEKEY_STATE_CHANGED, 1);
		}else if(t == n+l){
			//�N���x�����̊J�n
			rateKeyMap.put(RATEKEY_STATE_CHANGED, 1);
		}else if(l != 0 && t == n){
			//�N���J�艺�����Ԃ̊J�n
			rateKeyMap.put(RATEKEY_STATE_CHANGED, 1);
		}else{
			//�ȏア������������Ȃ�
			rateKeyMap.put(RATEKEY_STATE_CHANGED, 0);
		}
	}
	/**
	 * ���i009�p
	 * ���Y���ƔN�x���Ɍ_���ԕύX�������������Ƃ������肷��
	 * @param rateKeyMap
	 * @param t
	 */
	@SuppressWarnings("unchecked")
	private void _editStateChange009(Map rateKeyMap, int t){
		int m = MapUtils.getInteger(rateKeyMap, RATEKEY_M);
		
		if(t == m){
			//���������ŕی����������Ԃ̏I��
			rateKeyMap.put(RATEKEY_STATE_CHANGED, 1);
		}else{
			//�ȏア������������Ȃ�
			rateKeyMap.put(RATEKEY_STATE_CHANGED, 0);
		}
	}
	/**
	 * ���i308�p
	 * ���Y���ƔN�x���Ɍ_���ԕύX�������������ǂ������肷��
	 * @param rateKeyMap
	 * @param t
	 */
	@SuppressWarnings("unchecked")
	private void _editStateChange308(Map rateKeyMap, int t){
		int m = MapUtils.getInteger(rateKeyMap, RATEKEY_M);
		int state = MapUtils.getInteger(rateKeyMap, RATEKEY_STATE);
		
		if(state != 3 && t == m){
			//���������ŕی����������Ԃ̏I��
			rateKeyMap.put(RATEKEY_STATE_CHANGED, 1);
		}else{
			//�ȏア������������Ȃ�
			rateKeyMap.put(RATEKEY_STATE_CHANGED, 0);
		}
	}
	
	/**
	 * ���̏��i�p
	 * ���Y���ƔN�x���Ɍ_���ԕύX�������������Ƃ������肷��
	 * @param rateKeyMap
	 * @param t
	 */
	@SuppressWarnings("unchecked")
	private void _editStateChangeOther(Map rateKeyMap, int t){
		int m = MapUtils.getInteger(rateKeyMap, RATEKEY_M);
		if(t == m){
			//�ی����������Ԃ̏I��
			rateKeyMap.put(RATEKEY_STATE_CHANGED, 1);
		}else{
			rateKeyMap.put(RATEKEY_STATE_CHANGED, 0);
		}
	}
	
	/**
	 * ���Z�z�̌v�Z
	 * �������ŁA���N���̕��z�A�N���x�����Ԗ����i�m��N���i004�j�j�̏�Ԃ𔻒f����
	 * 0:���������܂��@1�F���������܂���
	 */
	@SuppressWarnings("unchecked")
	private void _editDtNashuPlus(Map rateKeyMap, int keisanPtn){

		rateKeyMap.put(RATEKEY_STATE_DT_PLUS, 1);
		if (keisanPtn == 3) {
			// �ٓ���Ԃ��擾����
			int changeState = MapUtils.getIntValue(rateKeyMap, RATEKEY_RATE_CHANGESTATE);
			// �ٓ���Ԃł͂Ȃ�
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
					//��������
					rateKeyMap.put(RATEKEY_STATE_DT_PLUS, 0);
				} else if((t1+1)%dividendYear == 0 && (f1 == 12) && xN == 1) {
					//���N���̕��z
					rateKeyMap.put(RATEKEY_STATE_DT_PLUS, 0);
				} else if ("004".equals(_ctx.insuranceCode)) {
					int annuityType = MapUtils.getInteger(rateKeyMap, RATEKEY_ANNUITYTYPE);
					int state = MapUtils.getInteger(rateKeyMap, RATEKEY_STATE);
					if (annuityType == 4 && state == 3) {
						int annuityBeginDate = MapUtils.getInteger(rateKeyMap, RATEKEY_ANNUITY_BEGIN_DATE);
						int g = MapUtils.getInteger(rateKeyMap, RATEKEY_G);
						int divEndDate = MapUtils.getInteger(rateKeyMap, RATEKEY_RATE_ENDDATE);
						if (divEndDate >= (annuityBeginDate + g*10000)) {
							//�N���x�����Ԗ����i�m��N���i004�j�j
							rateKeyMap.put(RATEKEY_STATE_DT_PLUS, 0);
						}
					}
				}
			}
		}
	}
	
	/**
	 * �z�����v�Z�N�����擾����
	 * 
	 * @param code ���i�R�[�h
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
			throw new FmsRuntimeException(code + "�̔z�����v�Z�N�������݂��Ă��܂���B");
		}
	}
	
	/**
	 * �o���ێ�R�[�h���擾����
	 * 
	 * @param code ���i�R�[�h
	 * @param payment ����
	 * @param policyYear �_���
	 * @return �o���ێ�R�[�h
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
			throw new FmsRuntimeException("���i" + code + "�̌o���ێ�R�[�h�����݂��Ă��܂���B");
		}
	}
	
	/**
	 * �ی����ԁA�N���x�����ԂȂǂ��A���ԋ敪���擾����
	 * @return�@term
	 */
	private int _getTermValue() {
		
		for (DataRow data : this.termConditonlist) {
			String condtion = data.getString("condition");
			int term = data.getInt("term_code");

			//���̓��[�g�L�[���������v�Z����
			if (StringUtils.isNotBlank(condtion)) {
				boolean cond = _ctx.getParser().parse(condtion).getBooleanValue();
				if (cond) {
					//�J�����g�����ɖ������ꍇ�A�����Ċ��ԋ敪��Ԃ�
					if (logger.isDebugEnabled()) {
						logger.debug("���̓��[�g�L�[�ɉ����Ċ��ԋ敪�F" + term);
					}
					return term;
				}
			}else{
				return term;
			}
		}
		//�Ō�܂ň�ł��������Ȃ��ꍇ�A0��Ԃ��i���ԋ敪����łȂ��j
		return 0;
	}

	/**
	 * �z�������擾����
	 * @param rateKeyMap 
	 * 
	 * @return
	 */
	@SuppressWarnings("unchecked")
	private void _editDividendRateValue(Map rateKeyMap) {
		
		// ���ԋ敪���擾����
		int term = _getTermValue();

		// �_������擾����
		int policyYear = MapUtils.getInteger(rateKeyMap,
				RATEKEY_RATE_STARTDATE);

		// �z���K�p�N�x���擾����
		int bonusYear = MapUtils.getInteger(rateKeyMap,
				RATEKEY_RATE_ENDDATE);
		bonusYear = _getBonusYear(bonusYear);
		
		int polcyPayment = 0;
		
		// ����t���̎�_��R�[�h
		String policyCode = null;
		if (rateKeyMap.containsKey(RATEKEY_POLICYCODE)) {
			policyCode = StringUtils.leftPad(String.valueOf(
					MapUtils.getInteger(rateKeyMap, RATEKEY_POLICYCODE)), 3, "0");
			rateKeyMap.put(RATEKEY_POLICYCODE, policyCode);
			// �������擾����
			polcyPayment = MapUtils.getInteger(rateKeyMap, RATEKEY_RATE_POLICYKAISU);
			// poilcyT��ݒ�
			_editElapsedYearMonth(rateKeyMap, false);
		} else {
			policyCode = _ctx.insuranceCode;
			rateKeyMap.put(RATEKEY_POLICYT, MapUtils.getIntValue(rateKeyMap, RATEKEY_T));
		}

		// �ꕔ�ꎞ������
		int partOneTime = MapUtils.getIntValue(rateKeyMap, RATEKEY_PARTONETIME);
		// ��_��̕������݂���ꍇ
		if (polcyPayment != 0) {
			// �������擾����
			polcyPayment = _getPayment(polcyPayment, partOneTime);
		}
		
		// �������擾����
		int payment = MapUtils.getInteger(rateKeyMap, RATEKEY_PAYMENT);
		// �������擾����
		payment = _getPayment(payment, partOneTime);
		
		// �o���ێ�R�[�h���擾����
		int accCode = 0;
		if (polcyPayment != 0) {
			// �N���x���ڍs����̏ꍇ�A��_�񂪖��z���i001�j�̏ꍇ�͗L�z���i011�j�̌o���ێ�ɏ]���B
			if ("931".equals(_ctx.insuranceCode) && "001".equals(policyCode)) {
				policyCode = "011";
			}
			accCode = _getAccountCode(policyCode, polcyPayment, policyYear);
		} else {
			accCode = _getAccountCode(policyCode, payment, policyYear);
		}
		// �z�����v�Z�N�����擾����
		_getDividendYear(policyCode);
		
		for (DataRow data : this.dividendList) {
			// ���̓��[�g�L�[�������𖞂������Ȃ����𔻒f����
			// ���ԃR�[�h���O�̏ꍇ�A�u�z���������ԋ敪�Ɋ֘A���Ȃ��v�Ƃ����Ӗ��ł�
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
			logger.warn("���͂��ꂽ���[�g�L�[�ʂ�ɔz�����͌�����܂���");
		}
	}
    
	/**
	 * �������擾����
	 * @param payment ����
	 * @param partOneTime �ꕔ�ꎞ��
	 * @return
	 */
	private int _getPayment(int payment, int partOneTime) {
		// �������u�������v�̏ꍇ��2�ɓǂݑւ�
		if (payment > 2) {
			payment = 2;
		}
		
		if (partOneTime == 1) {
			// �ꕔ�ꎞ���̏ꍇ�A�z�������擾����ꍇ�A�������Ƃ���
			payment = 2;
		}
		return payment;
	}
	
	/**
	 * �O���ƔN�x���z��������擾����
	 * @param bonusYear 
	 * @return
	 */
	private int _getBonusYear(int bonusYear) {
		// �N���擾����
		int year = bonusYear/10000;
		// �������擾����
		int day = bonusYear%10000;
		if (day < 331) {
			year = year - 1;
		}
		return year*10000 + 331;
	}
	
	/**
	 * �ϗ��������擾����
	 * @param rateKeyMap 
	 * 
	 * @return
	 */
	@SuppressWarnings("unchecked")
	private void _editDivRateValue(Map rateKeyMap) {

		//�_������擾����
		int policyYear = MapUtils.getInteger(rateKeyMap,
				RATEKEY_RATE_STARTDATE);
		//�z��������擾����
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
	 * �ϗ��������擾����
	 * @param Date
	 * @param flg 1:RateA�ϓ������擾�@2�FRateB�ϓ������擾
	 * @param rateKeyMap
	 * @return �ϗ�����
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
				logger.warn("���͂��ꂽ���[�g�L�[�ʂ�ɐϗ������͌�����܂���");
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
     * �z�����v�Z�ɂ����āA
     * �_������玖�ƔN�x���܂ł̌o�ߔN����t,f��
     * �_�������ی��N�x���܂ł̌o�ߔN����t1,f1���擾
     * @param rateKeyMap
     * @param isNotRider
     */
	@SuppressWarnings("unchecked")
    private void _editElapsedYearMonth(Map rateKeyMap, Boolean isNotRider) {
    	// �_��N�x���擾����
		int policyYear = 0;
		if (isNotRider) {
			policyYear = MapUtils.getInteger(rateKeyMap, RATEKEY_RATE_STARTDATE);
		} else {
			// ����̏ꍇ�A��_��̌_����𗘗p���ApolicyT�����߂�
			policyYear = MapUtils.getInteger(rateKeyMap, RATEKEY_RATE_POLICYSTARTDATE);
		}
		// �z���K�p�N�x���擾����
		int bonusYear = MapUtils.getInteger(rateKeyMap,
				RATEKEY_RATE_ENDDATE);

		if ((bonusYear - policyYear)%10000 ==0) {
			//���z���ƌ_����������̃t���O
			rateKeyMap.put(RATEKEY_RATE_XN, 1);
		} else {
			rateKeyMap.put(RATEKEY_RATE_XN, 0);
		}
		// �v�Z�Ώۂ��擾����
		int keisanPtn = MapUtils.getIntValue(rateKeyMap, RATEKEY_RATE_KEISANPTN);
		// �ٓ���Ԃ��擾����
		int changeState = MapUtils.getIntValue(rateKeyMap, RATEKEY_RATE_CHANGESTATE);
		// �_�񌻋����擾����
		int contractorState = MapUtils.getIntValue(rateKeyMap, RATEKEY_RATE_CONSTATE);
		// ���z�z���v�Z����Ƃ��A�ٓ���Ԃł͂Ȃ��A�_�񌻋������݂���ꍇ
		if (keisanPtn == 3 && changeState == 2 && contractorState == 1) {
			// ���z���̑O�����擾
			bonusYear = bonusYear - 1;
		}
		// �����ƔN�x���N�������擾����
		int dividendThisYear = _getBonusYear(bonusYear);
		// �����ƔN�x���N�����ݒ�
	    rateKeyMap.put(RATEKEY_RATE_DIVTHISYEAR, dividendThisYear);
	    // �O���ƔN�x���N�����ݒ�
	    rateKeyMap.put(RATEKEY_RATE_DIVLASYEAR, (dividendThisYear/10000-1)*10000+331);
        // �o�ߔN�����@���@�u�z����N���� - �_��N�����v�i�[�����؂�グ�j
        String tStdDate_ = String.valueOf(dividendThisYear);
        String t1StdDate_ = String.valueOf(bonusYear);
        String contractDate_ = String.valueOf(policyYear);

	    if (isNotRider) {//��_��̏ꍇ
	    	//���͕��z���̑O���ƔN�x���܂ł̌o��
	    	_editTandF(contractDate_, tStdDate_, RATEKEY_T, RATEKEY_F, rateKeyMap);
	    	//��1�͕��z���܂ł̌o�߂ł��B
	    	_editTandF(contractDate_, t1StdDate_, RATEKEY_T1, RATEKEY_F1, rateKeyMap);
        } else {
	    	_editTandF(contractDate_, tStdDate_, RATEKEY_POLICYT, null, rateKeyMap);
        }
    }
	
	/**
	 * �_����������܂ł̌o�ߔN�����i�[�����؏グ�j
	 * <br>������1<=f<=12�Ƃ���A����t�N0����=>t-1�N12����
	 * @param startDate
	 * @param standardDate
	 * @param t
	 * @param f
	 * @param rateKeyMap 
	 */
	@SuppressWarnings("unchecked")
	private void _editTandF(String startDate, String standardDate, String keyName4Year, String keyName4Month, Map rateKeyMap) {
		
		int t = 0, f = 0;
		
        // �J�n�N����
        Date contractDate = null;
        // ��N����
        Date stdDate = null;
        
        SimpleDateFormat sdf = new SimpleDateFormat(Const.YYYYMMDD);
		try {
			stdDate = sdf.parse(standardDate);
		} catch (ParseException e) {
			throw new FmsRuntimeException("���͂��ꂽ�z����N�������t�H�[�}�b�g�ł��܂���F" + standardDate);
		}

		try {
			contractDate = sdf.parse(startDate);
		} catch (ParseException e) {
			throw new FmsRuntimeException("���͂��ꂽ�_��N�������t�H�[�}�b�g�ł��܂���F" + startDate);
		}

        // �v�Z�����̏���
        Calendar tCaldr = Calendar.getInstance();
        Calendar contractDateCaldr = Calendar.getInstance();
        tCaldr.setTime(stdDate);
        contractDateCaldr.setTime(contractDate);

        // �o�ߔN
        t = tCaldr.get(Calendar.YEAR) - contractDateCaldr.get(Calendar.YEAR);
        
	    // ���N�o�ߌ�
	    f = tCaldr.get(Calendar.MONTH) - contractDateCaldr.get(Calendar.MONTH);
	
	    // �����o�ߓ�
	    int monthDays = 0;
	    
	    // �o�ߔN���O���傫���ꍇ
	    if (t >= 0) {
	        // ��������
	        int baseLastDay = tCaldr.getActualMaximum(Calendar.DAY_OF_MONTH);
	        int baseDay = tCaldr.get(Calendar.DAY_OF_MONTH);
	        int startDay = contractDateCaldr.get(Calendar.DAY_OF_MONTH);
	        
	        if (baseLastDay == baseDay) {
	            monthDays = 0;
	        } else {
	            monthDays = baseDay - startDay;
	        }
	        
	        // ���N�o�ߓ���蓖�N�o�ߌ���ݒ肷��
	        f = monthDays < 0 ? --f : f;
	        
	    } else {
	        throw new IllegalArgumentException(MessageFormat.format("�v�Z���{0}�͊J�n��{1}���傫���Ȃ�", standardDate, startDate));
	    }
	
	    // �o�ߌ�
	    if (f < 0) {
	        t--;
	        f += 12;
	    }
	    
	    //�[�����؏グ����
	    f++;
        
	    if (keyName4Year == null) {
	    	return;
	    }
	    // �o�ߔN�ݒ�
    	rateKeyMap.put(keyName4Year, t);
    	
	    if (keyName4Month == null) {
	    	return;
	    }
	    // �o�ߌ��ݒ�
	    rateKeyMap.put(keyName4Month, f);
	}
	
	/**
	 * �O���ƔN�x��������͔������܂ł�,�e���t���}���邩�ǂ����𔻒�
	 * 0:������ 1:�͂�
	 * */
	@SuppressWarnings("unchecked")
	private void _editIsComeOfDate(Map rateKeyMap, int keisanPtn) {
		
		rateKeyMap.put(RATEKEY_ISCOMEOFDATE, 0);
		
		if (keisanPtn == 3) {
			// �_������擾����
			int policyYear = MapUtils.getInteger(rateKeyMap, RATEKEY_RATE_STARTDATE);
			// �������Ԃ��擾����
			int m = MapUtils.getInteger(rateKeyMap, RATEKEY_M);
			// ���z�����擾����
			int bonusYear = MapUtils.getInteger(rateKeyMap, RATEKEY_RATE_ENDDATE);
			// �O���ƔN�x���z��������擾
			int bonusLastEndYear = _getBonusYear(bonusYear);
			// ���͔��������擾����
			int effectBeginDate = MapUtils.getInteger(rateKeyMap, RATEKEY_EFFECT_BEGIN_DATE);
	
			// �������Ԗ��������擾����
			int payOverDate = Integer.valueOf(CommonUtil.getFormatDate(String.valueOf(policyYear + m*10000 - 1)));
			// �O���ƔN�x���N�������������Ԗ����������͔�����
			if(bonusLastEndYear < payOverDate && payOverDate < effectBeginDate){
				rateKeyMap.put(RATEKEY_ISCOMEOFDATE, 1);
			}
			// �J�����J�n�������݂���ꍇ
			if (rateKeyMap.containsKey(RATEKEY_L)) {
				// �J��������
				int l = MapUtils.getInteger(rateKeyMap, RATEKEY_L);
				if (l > 0) {
					// �J�����J�n�����擾����
					int postponementBeginDate = MapUtils.getInteger(rateKeyMap, RATEKEY_ANNUITY_BEGIN_DATE) - l;
					// �O���ƔN�x���N�������J�����J�n�������͔�����
					if(bonusLastEndYear < postponementBeginDate && postponementBeginDate < effectBeginDate){
						rateKeyMap.put(RATEKEY_ISCOMEOFDATE, 1);
					}
				}
			}
			// �N���x���J�n��
			int annuityBeginDate = 0;
			// �N���x���J�n�������݂���ꍇ
			if (rateKeyMap.containsKey(RATEKEY_ANNUITY_BEGIN_DATE)) {
				// �N���x���J�n�����擾����
				annuityBeginDate = MapUtils.getInteger(rateKeyMap, RATEKEY_ANNUITY_BEGIN_DATE);
				// �O���ƔN�x���N�������N���x���J�n�������͔�����
				if(bonusLastEndYear < annuityBeginDate && annuityBeginDate < effectBeginDate){
					rateKeyMap.put(RATEKEY_ISCOMEOFDATE, 1);
				}
			}
			
			if (MapUtils.getInteger(rateKeyMap, RATEKEY_ISCOMEOFDATE) != 1 && Integer.parseInt(_ctx.insuranceCode) > 300) {
				// �_��I����
				int policyEndYear = _getPolicyEndYear(rateKeyMap, policyYear);
				// �O���ƔN�x���N�����������������͔�����
				if(bonusLastEndYear < policyEndYear && policyEndYear < effectBeginDate){
					rateKeyMap.put(RATEKEY_ISCOMEOFDATE, 2);
				}
			}
			// �N���x���J�n�������݂���ꍇ
			if (rateKeyMap.containsKey(RATEKEY_G) && rateKeyMap.containsKey(RATEKEY_ANNUITY_BEGIN_DATE) && annuityBeginDate > 0) {
				if (MapUtils.getInteger(rateKeyMap, RATEKEY_ISCOMEOFDATE) < 3) {
					// �N���x������
					int g = MapUtils.getInteger(rateKeyMap, RATEKEY_G);
					// �N���x��������
					int annuityEndDate = Integer.valueOf(CommonUtil.getFormatDate(String.valueOf(annuityBeginDate + g*10000 - 1)));
					// �O���ƔN�x���N�������N���x�������������͔�����
					if(bonusLastEndYear < annuityEndDate && annuityEndDate < effectBeginDate){
						rateKeyMap.put(RATEKEY_ISCOMEOFDATE, 3);
					}
				}
			}
		}
	}
	
	/**
	 * �O���ƔN�x�����}���Ă��Ȃ����ǂ����𔻒�
	 * 0:������ 1:�͂�
	 * */
	@SuppressWarnings("unchecked")
	private void _editIsAnnuityBegin(Map rateKeyMap, int keisanPtn) {
		
		rateKeyMap.put(RATEKEY_ISANNUITYBEGIN, 0);
		if (keisanPtn == 3) {
			String policyCode = MapUtils.getString(rateKeyMap, RATEKEY_POLICYCODE);
			if (Integer.parseInt(_ctx.insuranceCode) > 300 &&
					("004".equals(policyCode) || "042".equals(policyCode))) {
				// �_������擾����
				int policyYear = MapUtils.getInteger(rateKeyMap, RATEKEY_RATE_STARTDATE);
				// ���z�����擾����
				int bonusYear = MapUtils.getInteger(rateKeyMap, RATEKEY_RATE_ENDDATE);
				// �O���ƔN�x���z��������擾
				int bonusLastEndYear = _getBonusYear(bonusYear);
				// ���N�������N����
				int oldAnnuityBuyDate = MapUtils.getInteger(rateKeyMap, RATEKEY_OLDANNUITY_BUY_DATE);
				// �ŐV�N�������N��
				int newAnnuityBuyDate = MapUtils.getInteger(rateKeyMap, RATEKEY_NEWANNUITY_BUY_DATE);
				// �O���ƔN�x���N�������n���N�������͑����N���̏��񔃑�(�u���N�������N�������O�v���u�ŐV�N�������N�����O���ƔN�x���v)
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
	 * ���Y���ƔN�x���ɕی����Ԃ̏I�����}���邩�ǂ����𔻒�
	 * 0:������ 1:�͂�
	 * */
	@SuppressWarnings("unchecked")
	private void _editIsEndOfN(Map rateKeyMap) {
		// �_������擾����
		int policyYear = MapUtils.getInteger(rateKeyMap,
				RATEKEY_RATE_STARTDATE);
		// ���ƔN�x�I�������擾����
		int bonusEndYear = MapUtils.getInteger(rateKeyMap,
				RATEKEY_RATE_ENDDATE);
		// ���ƔN�x�J�n�����擾����
		int bonusYear = bonusEndYear - bonusEndYear%10000 - 10000 + 401;
		// �_��I����
		int policyEndYear = _getPolicyEndYear(rateKeyMap, policyYear);
		
		if(bonusYear <= policyEndYear && policyEndYear <= bonusEndYear){
			rateKeyMap.put(RATEKEY_ISENDOFN, 1);
		}else{
			rateKeyMap.put(RATEKEY_ISENDOFN, 0);
		}
	}
	
	/**
	 * �_��I�������擾
	 * @param rateKeyMap
	 * @param policyYear
	 * */
	@SuppressWarnings("unchecked")
	private int _getPolicyEndYear(Map rateKeyMap, int policyYear) {
		// �ی��N�����擾����
		int n = 99;
		// �ی��������擾����
		int f = 0;
		// ���������擾����
		int state = MapUtils.getInteger(rateKeyMap, RATEKEY_STATE);
		// �_��I����
		int policyEndYear = 0;
		if (state != 7) {
			if (rateKeyMap.containsKey(RATEKEY_N)) {
				n = MapUtils.getInteger(rateKeyMap, RATEKEY_N);
			}
			// �_��I�������擾����
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
	 * �{��N���̌o�ߌ���ݒ�
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
	 * �����ƔN�x���̕ی��N�x�����Q�b�g
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
			throw new FmsRuntimeException("���͂��ꂽ�_��N�������t�H�[�}�b�g�ł��܂���F" + contractDate);
		}
		try {
			endDate = sdf.parse(String.valueOf(divEndDate));
		} catch (ParseException e) {
			throw new FmsRuntimeException("���͂��ꂽ�_��N�������t�H�[�}�b�g�ł��܂���F" + divEndDate);
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
	 * �N�������J�n��A�N����������ݒ�
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
		//��Ԃ͕ی�������������A���͕ی����Ԍ�A�����ČJ�艺�����Ԃ�����ꍇ
		//�J�艺�����Ԓ��ɔ��f����
		if(state == 2 && l > 0 && t >= n){
			state = 5;
			rateKeyMap.put(RATEKEY_STATE, 5);
		}
		
		//��Ԃ͔N���J�n��̏ꍇ�A�N���J�n�o�ߔN�ƌ���ݒ�
		if(state == 3 && t-n-l >= 0){
			rateKeyMap.put(RATEKEY_T2, t-n-l);
			rateKeyMap.put(RATEKEY_F2, f);
			rateKeyMap.put(RATEKEY_T3, 0);
			rateKeyMap.put(RATEKEY_F3, 0);
		//��Ԃ͌J�艺�����Ԓ��̏ꍇ�A�J�艺���o�ߔN�ƌ���ݒ�
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