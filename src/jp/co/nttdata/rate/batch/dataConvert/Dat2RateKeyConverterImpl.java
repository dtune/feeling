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
 * ���q�l����f�[�^(.dat�t�@�C��)�����؃c�[���� ���ʂ���郌�[�g�L�[�ɒu������
 * 
 * @author btchoukug
 * 
 * 
 */
public class Dat2RateKeyConverterImpl implements IRateKeyConverter {

	public static final String INPUT_PERIOD_KBN = "periodKbn";
	public static final String INPUT_PAYMENT_KBN = "paymentKbn";

	/** ���s�V�X�e���̕����ώ�� */
	public static final String INPUT_PAYUP_CN = "payupKbn";

	/** ���s�V�X�e���̕������ */
	public static final String INPUT_PAYMENT_CN = "paymentSts";

	/** �@�o�ߌo�H�@ */
	public static final String INPUT_KEIRO = "keiro";

	/** �@��ی��Ґ��ʁ@ */
	public static final String INPUT_SEX = "sex";

	/** �x���������� */
	private static final String INPUT_KK1 = "kk1";

	/** FMS�V�X�e���Ɏ��ʂ���郌�[�g�L�[ */
	public static final String RK_GEN = "gen"; // ����
	public static final String RK_SEX = "sex"; // ����
	public static final String RK_KAISU = "kaisu"; // ��
	public static final String RK_AGE = "x"; // ��ی��ҔN��
	public static final String RK_N = "n"; // �ی����ԔN
	public static final String RK_M = "m"; // �������ԔN
	public static final String RK_G = "g"; // �N���x������
	public static final String RK_STATE = "state"; // �������
	public static final String RK_KEIRO = "keiro"; // �����o�H
	public static final String RK_SA = "SA"; // �ی����z
	public static final String RK_T = "t"; // �_��o�ߔN
	public static final String RK_F = "f"; // �_��o�ߌ�
	public static final String RK_T1 = "t1"; // �����o�ߔN
	public static final String RK_F1 = "f1"; // �����o�ߌ�
	public static final String RK_T2 = "t2"; // �N�����t�N
	public static final String RK_F2 = "f2";
	public static final String RK_T3 = "t3"; // �J�艺���O�̔N���J�n������̌o�ߔN��
	public static final String RK_F3 = "f3";
	public static final String RK_L = "l"; // �J�艺������
	public static final String RK_CONTRACT_DATE = "contractDate"; // �_��̎n���N����
	public static final String RK_K = "k"; // �N���x����������
	public static final String RK_H = "h"; // �Œ�x���ۏ؊���
	public static final String RK_U = "u"; // ����Ԗߊ���
	public static final String ESTIMATE_RATE_KBN = "estimateRateKbn"; // �\�藘�����ʋ敪
	public static final String RK_ANNUITY_Y = "annuityY"; // 1��ڂ̔N���x�����̔�N��
	public static final String RK_TEX = "tEX"; // �����N
	public static final String RK_CONTRACTORTYPE = "contractorType"; // �������ʋ敪CN
	public static final String RK_TEIZOKBN = "teizoKbn"; // �����^�敪
	public static final String RK_STEP = "step"; // �X�e�b�v����
	public static final String RK_STEPTIME = "stepTime"; // �X�e�b�v���E��standardDate
	public static final String RK_STANDARDDATE = "standardDate"; // P�v�Z��N����
	public static final String RK_BONUS_MONTH = "bonus_month"; // �{�[�i�X��
	public static final String RK_BONUS_MONTH1 = "bonusmonth1"; // �{�[�i�X��1
	public static final String RK_BONUS_MONTH2 = "bonusmonth2"; // �{�[�i�X��2
	private static final String RK_DIVIDEND = "dividend";	//�z���L��
	private static final String RK_PART_ONTTIME = "partOnetime";	//�ꕔ�ꎞ��
	private static final String RK_KAIYAKU_UMU = "kaiyakuUmu";	//���Ԗߋ��L��
	private static final String RK_INSURANCECODE = "insuranceCode"; //���i�R�[�h
	
	/** 005 �����敪�ǉ� */
	public static final String INPUT_RATE_KBN = "ryoritsuKbn";
	public static final String RK_THETA = "theta";

	/** �x���������� */
	public static final String RK_K1 = "k1";
	
	private static final int RADIX16 = 16;
	private static final char PLUS = '+';

	/** BT�v�Z�ɑ΂��āA�Œ�l�̃��[�g�L�[ */
	public Map<String, Double> fixedValues;

	/** ��r���̍��ږ��̔z�� */
	private String[] compareObjNames;

	@Override
	public Map<String, Double> convert(Map<String, Double> oldKeyValues) {

		if (oldKeyValues == null || oldKeyValues.size() == 0) {
			throw new IllegalArgumentException("�p�����[�^oldKeyValues�͋󔒂������B");
		}

		/*
		 * �ϊ��O�̃��[�g�L�[�̒l�𗬗p���� 
		 * ���f�[�^���C�A�E�g�ŐV�����[�g�L�[�̖��݂̂̂�u�������͗v��Ȃ���
		 */
		Map<String, Double> convertedKeyValues = new HashMap<String, Double>(
				oldKeyValues);

		// ���s�̕����ώ�ނƕ�����Ԃ���ɁA�_����state��ҏW
		Double paymentVal = oldKeyValues.get(INPUT_PAYMENT_CN);
		double state = 0d;
		if (paymentVal != null) {
			Double payUpVal = oldKeyValues.get(INPUT_PAYUP_CN);
			int payup = (payUpVal == null) ? 0 : payUpVal.intValue();
			int payment = paymentVal.intValue();
			if ((payup == 1 && payment == 2) || (payup == 2 && payment == 2)) {
				// �������ԏI����
				state = 2d;
			} else if (payup == 6 && payment == 2) {
				// �N���J�n��
				state = 3d;
			} else if (payup == 0 && payment == 1) {
				// �������Ԓ�
				state = 1d;
			} else if (payup == 4 && payment == 2) {
				// ���ϕی�
				state = 4d;
			} else if (payup == 3) {
				// �����Ə�
				state = 6d;
			} else if (payup == 5) {
				// �����ی�
				state = 7d;
			} else {
				throw new IllegalBatchDataException("���q�l����f�[�^�͓]���ł��܂���i�����ςݎ�ށA������Ԃ̒l�͎戵�͈͊O�ɂȂ�܂����B�j");
			}
			convertedKeyValues.put(RK_STATE, state);
		}
		
		// ���i�R�[�h
		Double insuranceCode = oldKeyValues.get(RK_INSURANCECODE);
		// ��ې��ʂ���sex�ɓ]��
		Double sexVal = oldKeyValues.get(INPUT_SEX);
		if (sexVal != null) {

			/*
			 * ���s���ʂɂāA�j���F1�G�����F2 FMS�V�X�e���ł́A�j���F0�G�����F1
			 * 302�Ή��F �j���F1�G�����F2 FMS�V�X�e���ł́A�����F1�G�j���F0
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

		// 005��p�Ή�
		Double rateKbn = oldKeyValues.get(INPUT_RATE_KBN);
		if (rateKbn != null) {
			if (rateKbn != 50d) {
				convertedKeyValues.put(RK_THETA, 5d);
			} else {
				convertedKeyValues.put(RK_THETA, 0d);
			}
		}

		/*
		 * �N�����E�Ζ����E�I�g�ʂ�A�ی�����n�E��������m��ҏW: 1.�����敪���u�Ζ����v�̏ꍇ�Am=m-x�An=n-x�Ƃ���
		 * 2.�ۊ��敪���u�I�g�ۏ�v�̏ꍇ�An=��-x+1�Ƃ���
		 */
		Double x = convertedKeyValues.get(RK_AGE);

		// �ۊ��敪���u�I�g�ۏ�v�̏ꍇ�An=��-x+1�Ƃ���
		Integer periodKbn = oldKeyValues.get(INPUT_PERIOD_KBN).intValue();
		Integer paymentKbn = oldKeyValues.get(INPUT_PAYMENT_KBN).intValue();

		if (periodKbn == null || paymentKbn == null) {
			throw new IllegalBatchDataException(
					"InputLayout.xml�ɂ͓��Y���i�̕ۊ��敪�ƕ����敪����`����Ă��܂���");
		}

		Double n = convertedKeyValues.get(RK_N);
		Double m = convertedKeyValues.get(RK_M);

		// 029,030�Ή��F�Ζ����̏ꍇ�A�N���x���������Ԃƒ��Ԋ��Ԃ����߂�
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
		
		// 1��ڂ̔N���x�����̔�N�߂̎Z�o
		if(periodKbn == 1) {
			if (oldKeyValues.containsKey(RK_ANNUITY_Y)) {
				double y = convertedKeyValues.get(RK_ANNUITY_Y) + x;
				convertedKeyValues.put(RK_ANNUITY_Y, y);
			}
		}

		// �����敪���u�Ζ����v�̏ꍇ�Am=m-x�An=n-x�Ƃ���
		if (paymentKbn == 2) {
			if (convertedKeyValues.get(RK_KAISU) > 1) {
				// ���������̏ꍇ�i�ꎞ�����Am���[���̂��߁j
				m = m - x;
				convertedKeyValues.put(RK_M, m);
			}
		}

		// �I�g�ۏ�܂��͏I�g�����̏ꍇ
		if (periodKbn == 3 || paymentKbn == 3) {

			double wholeLifeMN = 99d;

			// �I�g�ۏ�̏ꍇ
			if (periodKbn == 3) {
				convertedKeyValues.put(RK_N, wholeLifeMN);
			}

			// �I�g�����̏ꍇ
			if (paymentKbn == 3) {
				m = wholeLifeMN;
				convertedKeyValues.put(RK_M, m);				
			}
			
		}
		
		double oncepayM = 0d;
		
		if(convertedKeyValues.get(RK_KAISU) == 1) {
			convertedKeyValues.put(RK_M, oncepayM);
		}

		// TODO t1,f1�����N���́H
		Double f = oldKeyValues.get(RK_F);

		Double l = oldKeyValues.get(RK_L); // �J�艺������
		l = l == null ? 0 : l;
		Double t = convertedKeyValues.get(RK_T); // ���N�o�ߔN

		// �N���J�n��̏ꍇ�A�N���x���N�������߂�
		if (state == 3 && !oldKeyValues.containsKey("Annuity")) {

			// �N���J�n��̏ꍇ�At2�����߂�
			convertedKeyValues.put(RK_T2, convertedKeyValues.get(RK_T)
					- convertedKeyValues.get(RK_N) - l);
			convertedKeyValues.put(RK_F2, f);

		}

		// �u����������v�̏ꍇ�A�u�J�艺�����v���ǂ����𔻖�
		if (state == 2) {

			// �J�艺���N��>0�̏ꍇ�A�X�e�[�^�X�́u�J�艺�����Ԓ��v�Ƃ���
			if (l > 0 && t > n) {
				state = 5;
				convertedKeyValues.put(RK_STATE, state);

				// �J�艺���O�̔N���J�n������̌o�ߔN��t3=���N�o�ߔN-�i�ۊ��i�΁j-X�N��i�΁j�˕ی����ԔN�j
				convertedKeyValues.put(RK_T3, t - convertedKeyValues.get(RK_N));
				convertedKeyValues.put(RK_F3, f);

			}

		}

		// �N���J�n��̏ꍇ�A�N���J�n�N�߂̓]��
		if (state == 3 && l > 0) {
			if (oldKeyValues.containsKey(RK_ANNUITY_Y)) {
				double y = convertedKeyValues.get(RK_ANNUITY_Y) + l;
				convertedKeyValues.put(RK_ANNUITY_Y, y);
			}
		}

		// �o�����@���o�H�ɓ]��
		Double keiroVal = oldKeyValues.get(INPUT_KEIRO);
		double keiro = 0d; // �f�t�H���g�ꍇ�A�[���Ƃ���
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

		// �x����������
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

		// �ꕔ�ꎞ���̏ꍇ�A����CN�̐ݒ�
		if (oldKeyValues.containsKey(ESTIMATE_RATE_KBN)) {
			int estimateRateKbn = oldKeyValues.get(ESTIMATE_RATE_KBN).intValue();
			if ((paymentKbn == 0)
					&& (estimateRateKbn == 3 || estimateRateKbn == 5)) {
				convertedKeyValues.put(RK_KAISU, 2d);
			}
		}
		
		//�����N�̐ݒ�
		if(convertedKeyValues.containsKey(RK_STATE) && convertedKeyValues.get(RK_STATE) == 7) {
			
			convertedKeyValues.put(RK_TEX, convertedKeyValues.get(RK_N));

			
		}
		
		//028��308���i�F�����敪�̐ݒ�
		if(oldKeyValues.containsKey(RK_CONTRACTORTYPE)) {
			int iTeizoKbn = oldKeyValues.get(RK_CONTRACTORTYPE).intValue();
			if (iTeizoKbn >= 144 || iTeizoKbn == 41) {
				//16HEX��90��144�ƃC�R�[��
				convertedKeyValues.put(RK_TEIZOKBN, 1d);
			} else if (iTeizoKbn >= 128 || iTeizoKbn == 40) {
				//16HEX��80��128�ƃC�R�[��
				convertedKeyValues.put(RK_TEIZOKBN, 0d);
			} else {
				;
			}
		}
		
		//�X�e�b�v���E�㔻��
		if(oldKeyValues.containsKey(RK_STEP)) {
			if(!oldKeyValues.containsKey(RK_CONTRACT_DATE) || !oldKeyValues.containsKey(RK_STANDARDDATE)) {
				throw new IllegalBatchDataException("InputLayout.xml�ɂ͓��Y���i�̎n���N������P�v�Z��N��������`����Ă��܂���");
			} else {
				if(oldKeyValues.get(RK_CONTRACT_DATE) +  oldKeyValues.get(RK_STEP)*10000 - oldKeyValues.get(RK_STANDARDDATE) > 0) {
					convertedKeyValues.put(RK_STEPTIME, 1d); 
				} else {
					convertedKeyValues.put(RK_STEPTIME, 2d); 
				}
			}
		}
		
		//�{�[�i�X���擾
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
		 * �{�[�i�X�����̏ꍇ�A�o�ߌ��i�{�[�i�X���܂ށj���Z�o
		 * contractDate: �n���N����
		 * standardDate: ���N����
		 * RK_BONUS_MONTH1: �{�[�i�X��1
		 * RK_BONUS_MONTH2: �{�[�i�X��2
		 * psi: �{�[�i�X���{�� 
		 * psi��0�̏ꍇ�A�{�[�i�X�����ł͂Ȃ�
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

		// 004��005�̔N���^�C�v�𔻖�����
		if(oldKeyValues.containsKey("annuityType")){
			int annuityType = oldKeyValues.get("annuityType").intValue();
			if(annuityType == 4){
				convertedKeyValues.put(RK_G, oldKeyValues.get("g_004"));
			} else if(annuityType == 5){
				convertedKeyValues.put(RK_G, oldKeyValues.get("g_005"));
			}
		}
		
		// �⑰�N���o�ߔN��
		/**
		 * �����N���̏ꍇ�A�⑰�N���o�ߔN�����Z�o
		 * contractDate: �n���N����
		 * annuityDate: �����N�������Z�o�N����
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
			// 009�{��N���̌o�ߔN���̎Z�o
			convertedKeyValues.put(RK_T, t);
			convertedKeyValues.put(RK_F, f);
			if(f < 0) {
				t--;
				f = f + 12;				
			}
			// �Y�����܂ł̌o�ߔN���i �A���At1/t2�N0�����̂Ƃ���t1-1/t2-1�N12�����Ƃ��Ďg�p����j
			if(f == 0) {
				t = t - 1;
				f = 12d;
			}
			convertedKeyValues.put(RK_T1, t);
			convertedKeyValues.put(RK_F1, f);
			convertedKeyValues.put(RK_T2, t);
			convertedKeyValues.put(RK_F2, f);
		}
		
		//FIXME 016�A017�̒��Ԋ���u�̎Z�o
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
		// �`����������
		if(oldKeyValues.containsKey("z")) {
			double zilmer = oldKeyValues.get("z");
			zilmer = Math.min(zilmer,m);
			convertedKeyValues.put("z", zilmer);
		}
		
		// 027 �����j��
		if(oldKeyValues.containsKey("s") && oldKeyValues.containsKey(RK_STANDARDDATE)) {
			double s = oldKeyValues.get("s");
			int monthDay = oldKeyValues.get(RK_STANDARDDATE).intValue() % 10000;
			s = monthDay > 331 ? -1 : s;
			convertedKeyValues.put("s", s);
		}
		
		// UI����w��̌Œ�l���㏑������
		if (this.fixedValues != null) {
			convertedKeyValues.putAll(this.fixedValues);
		}

		// ��r���̍��ڂ����̂܂܃Z�b�g
		if (compareObjNames != null) {
			for (String compObj : compareObjNames) {
				if (!oldKeyValues.containsKey(compObj)) {
					throw new IllegalArgumentException("�w��̔�r���̍��ڂ����݂��Ă��Ȃ�");
				}
				convertedKeyValues.put(compObj, oldKeyValues.get(compObj));
			}
		}

		// �ϊ��������[�g�L�[��Ԃ�
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
		
		// �܂��A�]�v�ȃX�y�[�X���폜����
		value = CommonUtil.deleteWhitespace(value);
		if (StringUtils.isEmpty(value)) {
			return 0;
		}

		double ret = 0d;

		// +/-������擪�ɕt���Ă�ꍇ�A+���O��
		if (value.charAt(0) == PLUS) {
			value = StringUtils.remove(value, PLUS);
		}

//		// ���ʐ����̏ꍇ
//		if (CommonUtil.isNumeric(value)) {
//			ret = Integer.parseInt(value);
//		} else if (StringUtils.containsAny(value, "ABC")) {
//			//80~8C�܂���90~9C�̏ꍇ�A16������10���ɕϊ�����
//			ret = Integer.parseInt(value, RADIX16);
//		} else {
//			;
//		}
//
//		// �������t���{���̏ꍇ�A8A->90
//		if (layout.getName().equals("I")) {
//			int tmp = ret - I80;
//			if(tmp >= 16) {
//				ret = ret - I90;
//			}
//		}

		//8a~8c�A9a~9c�̏ꍇ�Afalse�ɂȂ��āAret��0
		if (CommonUtil.isNumeric(value)) {
			//ret = Integer.parseInt(value);
			ret = (Double) ConvertUtils.convert(value, Double.class);
		}

		if(layout.getName().equals(RK_CONTRACTORTYPE)) {
			if(ret >= 80 || ret == 0){
				//0�̏ꍇ�́A8a~8c�A9a~9c�B
				ret = Integer.parseInt(value, RADIX16);
			}
		}

		// �������t���{���̏ꍇ
		if (layout.getName().equals("I")) {
			ret = Integer.parseInt(value, RADIX16) % 16;
		}

		// 031�`034��Õی��̐�p�Ή�
		// ���[�g�L�[�F�ЊQ���t�s�S�ۓ���t��
		if (layout.getName().equals("fuka")) {
			if (StringUtils.isBlank(value)) {
				ret = 0;
			} else {
				ret = Integer.parseInt(value);
			}
		}

		// 304��p�Ή��F�ŏI�ی����z����
		// �������ʋ敪�˂R�P�F�Q�O���A�R�S�F�S�O���A�R�T�F�U�O��
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

		// 304��p�Ή��F�z��
		// �\�藘�����ʁ˂O�A�Q�A�R�F���z���G�P�A�S�A�T�F�����z��
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
		 * �ꕔ�ꎞ�����𔻖�����
		 * �O�F�Ȃ��G�P�F����
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
		 *  204��p�Ή��F
		 *  �ۊ��敪CN���R�̏ꍇ�A�I�g�ہG���̈ȊO�̏ꍇ�A�m�蕥
		 *  �����敪CN���R�̏ꍇ�A�I�g�ہG���̈ȊO�̏ꍇ�A�m�蕥
		 */
		if ("Htype".equals(layout.getName()) || "Stype".equals(layout.getName())) {
			if (ret == 3) {
				ret = 0;
			} else {
				ret = 1;
			}
		}

		// 204�E235��p�Ή��F��ی��Ҍ^
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

		// 014��p�Ή��F�����^
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

		// 235��p�Ή��F�ЊQ�s�S�ۓ����t���L���i�R�[�h�l�͂��̂܂܁j
		if (layout.getName().equals("fukaumu_z")) {
			;
		}

		// 009��p�Ή�:�{��N���x�����R�����O��
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

		// 235��p�Ή��F���Ԗߋ��L���i���i�R�[�h��蔻�f����j
		if (layout.getName().equals(RK_KAIYAKU_UMU)) {
			//235��236�����Ԗߋ�����
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