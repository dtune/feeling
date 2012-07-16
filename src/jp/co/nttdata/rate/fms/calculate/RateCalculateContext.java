package jp.co.nttdata.rate.fms.calculate;

import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

import jp.co.nttdata.rate.exception.FmsDefErrorException;
import jp.co.nttdata.rate.exception.FmsRuntimeException;
import jp.co.nttdata.rate.model.formula.Formula;
import jp.co.nttdata.rate.rateFundation.RateFundationGroup;
import jp.co.nttdata.rate.rateFundation.RateFundationManager;
import jp.co.nttdata.rate.rateFundation.dbConnection.DataRow;
import jp.co.nttdata.rate.util.CommonUtil;
import jp.co.nttdata.rate.util.Const;
import org.apache.commons.beanutils.ConvertUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
/**
 * ���i��`�ɂ��āA���ꂼ��v�Z���̒l�����߂邽�߂̌v�Z�R���e�L�X�g�ł���
 * <br>���ʂ̃R���e�L�X�g�Ɣ�ׁADB����v�Z���������
 * @author btchoukug
 *
 */
public class RateCalculateContext extends DefaultCalculateContext {

	/** ��ێҔN�� */
	public static final String RATEKEY_AGE = "x";
	/** ��ێҐ��� */
	public static final String RATEKEY_SEX = "sex";
	/** ���� */
	public static final String RATEKEY_GEN = "gen";
	/** �����i�񐔁j */
	public static final String RATEKEY_PAYMENT = "kaisu";
	/** �z���L�� */
	public static final String RATEKEY_DIVIDEND = "dividend";
	/** �ꕔ�ꎞ�����𔻖����鍀�� */
	public static final String RATEKEY_PARTONETIME = "partOnetime";
	/**�o�ߔN*/
	public static final String RATEKEY_T = "t";
	/**xtime*/
	public static final String RATEKEY_XTIME = "xtime";
	
	/** ��l���i�[����}�b�v�i��؂�ւ��ŏ��������邪�A�v�Z���Œ��g�ύX�s�j*/
	private Map<String, Double[]> fundationMap = new HashMap<String, Double[]>();

	/**�v�Z�r���̌������̔z��҂� �֌W���邩�ǂ����i���͐��ʁj�ŕێ����邱��*/
	private Stack<Integer> mateStack = new Stack<Integer>();

	/**�v�Z�r���̊�O���[�v�L�[��ێ����邱��*/
	private Stack<String> fundGroupKeyStack = new Stack<String>();

	/** ��͉�񗦂Ɋւ�邩 */
	private boolean isQwRelated = false;
	
	/** ���̂̂S�{�́E�P�{�̈��͕W���̂�\�� */
	private Stack<Integer> xtimeStack = new Stack<Integer>();
	
	private RateFundationManager fundMgr;
	
	private CacheManagerSupport cache;
	
	private ContextPlugin ctxPlugin;
	
	/**�z������̏��i*/
	private String[] dividendCodes = {"004", "008", "009", "011", "013", "017", "042",
			"301", "302", "303", "304", "307", "308", "309", "931"};
	
	/**�J�����g���i�R�[�h*/
	protected String insuranceCode;
	private boolean isSpecial = false;
	private String contextBaseKey;
	
	public RateCalculateContext(String code) throws FmsDefErrorException{
		super();

		//�f�t�H���g�͕W���̂Ƃ���
		xtimeStack.push(Const.X_TIMES_DEFAULT);
		
		//�n�����R�[�h�̑O3���͏��i�R�[�h�A����ȍ~�͕ۏ�̃t���O�ƂȂ�
		this.insuranceCode = code.substring(0, 3);
		this.isSpecial = code.substring(3).indexOf("1") > -1 ? true : false;
		
		//�L���b�V��������
		this.cache = new CacheManagerSupport(this.insuranceCode);
		
		//DB���������[�h
		this.fundMgr = new RateFundationManager(this.insuranceCode);
		
		//�W���̊�����[�h
		this.fundMgr.loadRateFundation(false);
		
		if (isSpecial) {
			//���̊�����[�h
			this.fundMgr.loadRateFundation(true);	
		}
		
		if ("043".equals(this.insuranceCode)) {
			ctxPlugin = new ContextPlugin043(this);
		} else {
			//�z�����v�Z�����������[�h
			if (ArrayUtils.contains(dividendCodes, this.insuranceCode)) {
				ctxPlugin = new DividendContextPlugin(this);
			} else {
				ctxPlugin = new DefaultContextPlugin(this);
			}			
		}
	}
	
	@Override
	public CacheManagerSupport getCache() {
		return this.cache;
	}
	
	@Override
	public void setCacheEnabled(boolean enableCache) {
		this.cache.setCacheEnable(enableCache);
	}
	
	/**
	 * ����A�o�u�g����񗦂��Z�o
	 * @param gen
	 * @param pvh
	 * @return
	 * @throws Exception 
	 */
	@SuppressWarnings("unchecked")
	private double _getQwValue(int gen, String pvh) throws Exception {
		
		for (DataRow data : this.fundMgr.getQwConditonList()) {
			int qwGen = data.getInt(RateFundationGroup.GEN);
			String qwPVH = data.getString(RateFundationGroup.PVH);
			if ((gen == qwGen || qwGen == 0) && (StringUtils.lowerCase(pvh).equals(qwPVH) || qwPVH == null)) {
				
				String condtion = data.getString("condition");
				double qw = data.getDouble(RateFundationGroup.QW);
				
				/*
				 * �ۑ�R�R�F Prate�v�Z���鎞�ɁAt�����͂��Ȃ��͂��Ȃ̂ŁA��񗦂��R���g���[�����邽�߁A
				 * �ꎞ�����̏ꍇ�At>m�Ƃ��G���������̏ꍇ�At=0�Ƃ���悤�ɏC�����܂��B
				 * qw_master�e�[�u���̕��́Acondition�R������kaisu���̏������O��
				 */	
				int kaisu = MapUtils.getIntValue(this.tokenValueMap, RATEKEY_PAYMENT);
				//P���[�g���v�Z����ꍇ�AUI����o�ߔN���Ȃ�����
				boolean isNeedTempT = !this.tokenValueMap.containsKey(RATEKEY_T);
				if (isNeedTempT) {
					if (kaisu == 1) {
						//t>m�̂��߁At���ő�l�Ƃ���
						this.tokenValueMap.put(RATEKEY_T, 999);
					} else {
						this.tokenValueMap.put(RATEKEY_T, 0);
					}
				}
				
				//���̓��[�g�L�[���������v�Z����
				if (condtion != null && StringUtils.isNotEmpty(condtion)) {
					boolean cond = this.getParser().parse(condtion).getBooleanValue();
					if (cond) {
						//�J�����g�����ɖ������ꍇ�A�����ĉ�񗦂�Ԃ�
						if (logger.isDebugEnabled()) {
							logger.debug("���̓��[�g�L�[�ɉ����ĉ�񗦁F" + qw);
						}
						
						if (isNeedTempT) {
							//�v�Z�I���̏ꍇ�At���R���e�L�X����O��
							this.tokenValueMap.remove(RATEKEY_T);
						}						
						
						return qw;
					}
				}
			}
			
		}
		
		//�Ō�܂ň�ł��������Ȃ��ꍇ�A0��Ԃ��i��񗦍���łȂ��j
		return 0d;		
	}
		
	/**
	 * 
	 * �O��Ƃ��āA���ׂČ������`���鎞�ɁA
	 * �v�Z��bPVH��z��ҁA���ʐ����A�N������w�肵�Ă��邱�ƁB
	 * <br>����ԏ�̃G���g���[�����ɂ͕K����b���w�肷�邱�ƁB 
	 * �������v�Z����ꍇ�A��`�����擾���āA���ǂݍ���
	 * <p>
	 * �v�Z��b�̐؂�ւ���������P�ʂƂȂ��Ă� �����v�Z�J�n�O�A
	 * ���̌����Ɏg�����b����U�ۑ����āG
	 * �v�Z�I����A�g������b���|�b�v���Č��̊�b�ɖ߂�
	 * </P>
	 * 
	 * @param formula
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public boolean shiftFundation(Formula formula) throws Exception {

		// �f�t�H���g�ꍇ�A��؂�ւ���true�Ƃ���
		boolean shiftable = true;
		isQwRelated = false;

		// PVH��b���擾
		String pvh = formula.getPvh();

		// �O���[�v�L�[�ҏW�v�ۂ̔��f
		boolean toEditGroupKey = StringUtils.isNotBlank(pvh);

		// �O���[�v�L�[�̏�����
		String fundGroupKey = null;
		if (this.fundGroupKeyStack.isEmpty()) {
			// ����̏ꍇ�Astack��empty�̂��߁A���L�������X�L�b�v����
			if (!toEditGroupKey) {
				throw new FmsDefErrorException("�G���g���[�̎Z���ɂ�PVH��b���w�肵�Ȃ���΂Ȃ�Ȃ�:"
						+ formula.getName());
			}
		}

		// ���̎Z���̏ꍇ�A��b�͓��Ɏw�肵�Ȃ��ƁA��ʎZ���̊�b�𗬗p����
		if (!toEditGroupKey && formula.getXtime() > Const.X_TIMES_DEFAULT) {
			String lastGroupKey = this.fundGroupKeyStack.peek();
			// ��ʎZ���͓��̂̎Z���ixtime=4�j�̏ꍇ�APVH�́{�̑O�ƂȂ�
			// ��F11P+X4
			int pos = lastGroupKey.indexOf("+X4");
			pos = pos > 1 ? pos : lastGroupKey.length();
			pvh = String.valueOf(lastGroupKey.charAt(pos - 1));
			toEditGroupKey = true;
		}

		// ��b���w�肵�Ă�ꍇ�i���̂��j�A��O���[�v�L�[��ҏW
		if (toEditGroupKey) {

			// �v�Z��b��P|V|H�̒��ɂP���w�肵�Ă���ꍇ
			if (pvh.length() == 1 && StringUtils.containsOnly(pvh, "PVH")) {
				;
			} else {
				// �v�Z��b�͏����Ƃ��Ē�`�����ꍇ�iif(gen>2){H}else{V}�̂悤�ȎZ���j
				Double dPVH = this._parser.parse(pvh).eval().doubleValue();
				pvh = _editPVH(dPVH);
			}

			int inputGen = MapUtils
					.getIntValue(this.tokenValueMap, RATEKEY_GEN);
			int kaisu = MapUtils.getIntValue(this.tokenValueMap,
					RATEKEY_PAYMENT);
			// �ꕔ�ꎞ���t���O,0 : �ꕔ�ꎞ�����w�肵�Ȃ��A 1 : �ꕔ�ꎞ��
			int partOnetime = MapUtils.getIntValue(this.tokenValueMap,
					RATEKEY_PARTONETIME);

			Map valuesMap = new HashMap();
			valuesMap.put(RateFundationGroup.GEN, inputGen);
			// �񐔂��ꎞ�����ƕ��������Q�p�^���ɒu��
			valuesMap.put(RateFundationGroup.PAYMENT, kaisu > 1
					|| partOnetime > 0 ? 2 : 1);
			valuesMap.put(RateFundationGroup.PVH, pvh);

			// �z���L���Ɋւ�ꍇ�A�z���L�����܂߂Ċ�O���[�v�L�[��ҏW
			if (this.tokenValueMap.containsKey(RATEKEY_DIVIDEND)) {
				valuesMap.put(RateFundationGroup.DIVIDEND, MapUtils
						.getIntValue(this.tokenValueMap, RATEKEY_DIVIDEND));
			}

			/**
			 * 20110915�@by�@zhanghy �Z����`��xtime���S�{�́A�P�{�̈��͕W���̂���ʂ��悤���P����
			 */
			int xtime = formula.getXtime();
			if (xtime > Const.X_TIMES_DEFAULT) {
				xtimeStack.push(xtime);
				this.tokenValueMap.put(RATEKEY_XTIME, xtimeStack.peek());
			}

			/**
			 * 20110402���v���̎��Ɣ�g����v�]���ȉ��ǂ���F
			 * �����Z���ɁA4�{�̂�1�{�̂�����܂����A1�{�͕̂��ʑ́iqw�̓[���ł͂Ȃ��j�Ƃ��Čv�Z�������ł� �΍�F
			 * Formula��`(xml)�ɑ΂��āA�P��Attribute�u�{����xtime�v��ǉ����邤���ŁA��؂�ւ��̂Ƃ���
			 * �Z����`���𔻒f���āA033&034,237&238���i��4�{�́E1�{�̗����͉�񗦍��݂̊���g���G
			 * ���̂��͉��L�̃��[�����]���F �����@���@4�{�̂̏ꍇ�A���0�̓��̊�����[�h�G
			 * �����@���@1�{�̂̏ꍇ�A���0�̕W���̊�����[�h�G ����ȊO�A���ʂƂ��Ċ�����[�h�B
			 */
			if (this.insuranceCode.equals("031")
					|| this.insuranceCode.equals("235")) {
				isQwRelated = true;
			} else {
				if (xtimeStack.peek() == Const.X_TIMES_DEFAULT) {
					// �J�����g�Z������ʎZ�������̂ł͂Ȃ��@
					isQwRelated = true;
				}
			}

			// �O���[�v�L�[���[���ɉ�񗦂��܂߂�ꍇ�A��񗦂��Z�o
			if (StringUtils.contains(this.fundMgr.getCurrentGroupKeyDef(),
					RateFundationGroup.QW)) {

				Double qw = 0d;

				if (isQwRelated) {
					// ����̏��i�R�[�h����񗦂����߂�
					String curFundCode = formula.getFundCode();
					if (StringUtils.isBlank(curFundCode)) {
						// ���Ɏw�肵�Ă��Ȃ��i�f�t�H�[���g�j�ꍇ�A�J�����g���i�R�[�h�Ƃ���
						curFundCode = this.insuranceCode;
					} else {
						if (!StringUtils.isNumeric(curFundCode)) {
							throw new FmsDefErrorException(
									MessageFormat.format(
													"�Z��{0}�Ɋ�R�[�h{1}�͏��i�R�[�h�i�����j��ݒ肳��Ă��܂���",
													formula.toString(),
													curFundCode));
						}
					}
					this.setValues.put(RateFundationGroup.SYS_FUND_CODE,
							ConvertUtils.convert(curFundCode, Double.class));
					// ������W���̂ǂ���ł���񗦂ɂ������ƁADB��qwMaster�ݒ�ǂ���ɉ�񗦂����߂�
					qw = _getQwValue(inputGen, pvh);
				}

				valuesMap.put(RateFundationGroup.QW, qw);
			}

			// ���[�g�L�[�����PVH�A��񗦂����ւ�
			fundGroupKey = this.fundMgr.editFundationGroupKey(valuesMap);

		}

		// �J�����g�����ɓK�p���ʂ��擾���A�f�t�H���g�Ƃ��Ă͓��͂������ʂƂ���
		int fundSex = MapUtils.getIntValue(this.tokenValueMap, RATEKEY_SEX);

		// �����ɂĐ��ʂ���Ɏw�肵�Ă���ꍇ�A���͂̐��ʂ��z��҂̐��ʂ��Z�o
		if (formula.isMate()) {
			// �j��Switch
			fundSex = (fundSex == 0) ? 1 : 0;
		} else {
			if (!this.mateStack.isEmpty()) {
				// �w�肵�Ă��Ȃ��ꍇ�A�O��̐��ʂ����̂܂܎g��
				fundSex = this.mateStack.peek();
			}
		}

		/*
		 * �����I�ɐ��ʐ�������ꍇ�A���[�g�L�[�isex�j�ƌ����̔z��ғ���imate�j �Ɋւ�炸���ׂĊ�͎w�萫�ʂ̊�Ƃ���
		 */
		if (StringUtils.isNotEmpty(formula.getLimitedSex())) {
			fundSex = formula.getLimitedSex().equals(Const.MALE) ? 0 : 1;
			if (logger.isInfoEnabled())
				logger.info("[FUND]�����ꂩ���͑S����������: " + _editSex(fundSex)
						+ "�ň�����");
		}

		/*
		 * �����I�ɔN�������ꍇ�A���[�g�L�[x�Ɋւ�炸�A ���ׂĊ��limitedAge�ɉ����Ēl�Ƃ���
		 * 20110403:�����_�ł́A�Z�����őΉ��ς݂ł��A������x=40�ɂ��邾��
		 */
		int limitedAge = formula.getLimitedAge();
		if (limitedAge > 0) {
			// Array��index���Z�o���邽�߁A��U�Վ��ϐ��Ƃ��Ēǉ�����
			this.addTempVariable(RATEKEY_AGE, limitedAge);
			if (logger.isInfoEnabled())
				logger.info("[FUND]�����ꂩ���͑S�������N��: " + limitedAge + "�΂Ŏ�荞�݁�");
		}

		// �؂�ւ��v�ۂ𔻒f
		String lastGroupKey = null;
		int lastFundSex = -1;
		if (!this.fundGroupKeyStack.isEmpty()) {
			lastGroupKey = this.fundGroupKeyStack.peek();
			if (!toEditGroupKey) {
				// �ҏW�K�v�Ȃ��ꍇ�A�O��̃O���[�v�L�[�����̂܂܎g��
				fundGroupKey = lastGroupKey;
			}
			lastFundSex = this.mateStack.peek();
		}

		// 4�{�̂̏ꍇ�A�������K�p����iPVH���w�肵���ꍇ�A�K���O���[�v�L�[��ҏW�j
		if (xtimeStack.peek() == Const.X_TIMES_4
				&& (toEditGroupKey || lastGroupKey
						.indexOf(RateFundationGroup.SUFFIX_FUND_SPECIAL) < 0)) {
			fundGroupKey += RateFundationGroup.SUFFIX_FUND_SPECIAL;
		}

		// ����̊�͑O��Ɠ����ł���΁A�؂�ւ��Ȃ�
		if (fundGroupKey.equals(lastGroupKey) && lastFundSex == fundSex) {
			shiftable = false;
		}

		// ��L�ŕҏW�������ʂƃO���[�v�L�[��������[�h
		if (shiftable) {
			// ��̃O���[�v�L�[�Ɛ��ʂ�ۑ�
			this.fundGroupKeyStack.push(fundGroupKey);
			this.mateStack.push(fundSex);

			RateFundationGroup rfg = this.fundMgr
					.loadFundationGroup(fundGroupKey);
			_editFundMapBySex(rfg, fundSex);

		}

		return shiftable;
	}
	
	@SuppressWarnings("unchecked")
	private void _editFundMapBySex(RateFundationGroup rfg, int fundSex) {
		if (fundSex == Const.SEX_MALE_0) {
			this.fundationMap = rfg.getMaleFundMap();
		} else {
			this.fundationMap = rfg.getFemaleFundMap();
		}
		
		//�\�藘���A�������A��񗦂Ȃ�(�z��ł͂Ȃ�)
		this.tokenValueMap.put(RateFundationGroup.RATE, rfg.getInterest());
		this.tokenValueMap.put(RateFundationGroup.V, rfg.getV());
		this.tokenValueMap.put(RateFundationGroup.QW, rfg.getQw());
		
		//�ŏI�N��֎擾
		int omega = rfg.getOmega(fundSex);
		this.tokenValueMap.put(RateFundationGroup.OMEGA, omega);
		if (logger.isInfoEnabled()) {
			logger.info("[FUND]�ŏI�N��ցF" + omega);	
		}
	}
	
	/**
	 * �O��̌v�Z���Ɏg��ꂽ��֖߂�
	 * 
	 * @param shiftable
	 * @param formula 
	 * @throws Exception 
	 */
	@SuppressWarnings("unchecked")
	public void rollbackFundation(boolean shiftable, Formula formula) throws Exception {
		
		//�����N����݂���ꍇ�A�Վ��ϐ�����O��
		int limitedAge = formula.getLimitedAge();
		if (limitedAge > 0) {
			this.removeTempVariable(RATEKEY_AGE);
			if (logger.isInfoEnabled()) {
				logger.info("[FUND]�����ꂩ���͑S�������N����O����");						
			}				
		}
		
		if (formula.getXtime() > Const.X_TIMES_DEFAULT) {
			xtimeStack.pop();
			this.tokenValueMap.put(RATEKEY_XTIME, xtimeStack.peek());
		}
		
		// �J�����g�����̌v�Z�O�ɁA����Ɛ��ʂ����ɖ߂�
		if (shiftable) {
			this.fundGroupKeyStack.pop();
			this.mateStack.pop();
			
			if (!this.fundGroupKeyStack.isEmpty()) {
				String lastGroupKey = this.fundGroupKeyStack.peek();
				int lastSex = this.mateStack.peek();
				//���O�̊����
				RateFundationGroup rfg = this.fundMgr.loadFundationGroup(lastGroupKey);
				_editFundMapBySex(rfg, lastSex);
			}
		}
	}
	
	private String _editPVH(double dBase) {
		String pvh = null;
		if (dBase == 0d) {
			pvh = "P";
		} else if (dBase == 1d) {
			pvh = "V";
		} else {
			pvh = "H";
		}
		return pvh;
	}

	private String _editSex(int fundSex) {
		return fundSex == 0 ? "�j��" : "����";
	}
	
	/**
	 * Cachekey���쐬���邽�߁A�J�����g�v�Z�ɉ����鐧���N���Ԃ�
	 * @return
	 */
	public int getLimitedAge() {
		Object ret = this.getTempVariable(RATEKEY_AGE);
		if (ret == null) return 0;
		return (Integer)ret;
	}

	/**
	 * �J�����g�v�Z�ɂāA�g�����ʂ�Ԃ�
	 * @return
	 */
	public int getCurrentSex() {
		if (this.mateStack.isEmpty()) return -1;
		return this.mateStack.peek();
	}
	
	/**
	 * �J�����g�v�Z�ɂāA��̃O���[�v�L�[��Ԃ�
	 * @return
	 */
	public String getCurrentFundationGroupKey() {
		if (this.fundGroupKeyStack.isEmpty()) return null;
		return this.fundGroupKeyStack.peek();
	}
	
	@Override
	public void reset() {
		super.reset();
		this.mateStack.clear();
		this.fundGroupKeyStack.clear();
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public void setInput(final Map input) {
		super.setInput(input);
		
		/*
		 * ��ʓI�Ɍv�Z���ʂ̓��[�g�L�[�Ɋւ邽�߁A���͂̃��[�g�L�[Map
		 * ��hashcode���x�[�X�L�[�Ƃ���
		 */
		this.contextBaseKey = String.valueOf(CommonUtil.FNVHash1(input.toString()));
		
		//���ꂼ�ꋤ�ʈȊO�̏������s��(��Ƀ��[�g�L�[�̓]������)
		ctxPlugin.contextHandle();
	}
	
	/**
	 * ������z��̊��Ԃ�
	 * <br>������v�⎀�S��qx�Ȃǔz��ł͂Ȃ��̍��ڂ͑ΏۊO�Ƃ���
	 * @return 
	 */
	public Double[] getFundation(String fundName) {
		
		Object fundValue = this.fundationMap.get(fundName);
		if (fundValue == null) {
			throw new FmsRuntimeException("�" + fundName + "���擾����Ȃ��B");
		}
		
		return (Double[]) fundValue;
		
	}
	
	@Override
	public StringBuilder getContextKey(boolean isRatekeyRelated) {
				
		//�L���b�V���Ώۂɑ΂��ẮA���i�P�ʂŊ�O���[�v�L�[�{���ʁi�z��҂Ɛ������ʂɊ֌W����j�{�����N��
		StringBuilder contextKey = new StringBuilder(this.insuranceCode);
		
		if (isRatekeyRelated) {
			contextKey.append(Const.DASH).append(this.contextBaseKey);
		}

		contextKey.append(Const.DASH)
				.append(this.getCurrentFundationGroupKey()).append(Const.DASH)
				.append(this.getCurrentSex());
		
		//�Վ��ϐ����Z���̒l�ɉe������̂��߁A�L�[�ɂ��ǉ�
		if (!this.setValues.isEmpty()) {			
			contextKey.append(Const.DASH).append(this.setValues.toString());	
		}
		
		return contextKey;
	}
}