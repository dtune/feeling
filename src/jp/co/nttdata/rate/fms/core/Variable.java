package jp.co.nttdata.rate.fms.core;


import java.math.BigDecimal;

import org.apache.commons.beanutils.ConvertUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import jp.co.nttdata.rate.fms.calculate.ICalculateContext;
import jp.co.nttdata.rate.fms.common.SystemFunctionUtility;
import jp.co.nttdata.rate.fms.core.keyword.Set;
import jp.co.nttdata.rate.log.LogFactory;
import jp.co.nttdata.rate.model.formula.Formula;

/**
 * �����̒��ɕϐ��ł���
 * <p>SYSFUNC���`FUNC���ΏۂƂ���A�������L�̈Ⴂ������B</p>
 * <p>
 * Variable�̓��[�g�L�[�ƒ��ڂɊ֌W������A�v�Z�r���̗Վ��ϐ��Ɋ֌W�Ȃ����ƁG <br>
 * Function�̓��[�g�L�[�Ɗ֌W�����锺�ɁA�v�Z�r���̗Վ��ϐ��ɂ��֌W�����邱�ƁB
 * </p>
 * 
 * @author btchoukug
 * 
 */
public class Variable implements Cacheable {

	private static Logger logger = LogFactory.getInstance(Variable.class);

	/** �ϐ��� */
	private String name;
	
	/** �ϐ��ɉ���������{�f�B */
	public Formula formula;
	
	/** �v�Z�R���e�L�X�g */
	private ICalculateContext _ctx;
	
	/** �v�Z�� */
	public boolean isFormula = false;

	public Variable(String var, ICalculateContext ctx) {
		
		this.name = var;
		this._ctx = ctx;
		
		/*
		 * FIXME �ϐ������v�Z���{�f�B���擾����̂�FormulaParser�Ɉڂ��������悢
		 * formula���Œ�l�̏ꍇ�Avalue�𐔎��Ƃ���token���쐬���������A�Վ��ϐ�map�ɕۑ�����
		 * Variable��Factory���[�h�ō��悤�ɉ��P�F�O���Ōv�Z�����X�g���w�肷��΁A�v�Z�ۂ𔻒f����B
		 * �Ȃ���΁A����input��map����擾����B
		 * DefFunction�̕���
		 */
		//Formula���X�g�̊Ǘ��͊O�����璍������
		this.formula = _ctx.getFormula(var);
		if (formula != null) {
			isFormula = true;
		}
	}
	
	public String getName() {
		return this.name;
	}

	@Override
	public String toString() {
		return this.name;
	}

	public BigDecimal value() throws Exception {

		BigDecimal ret = (BigDecimal) _ctx.getFunctionPara(name);
		
		if (ret != null) return ret;
		
		// �v�Z�L���i���g��XML�ɒ�`����Ă���v�Z����萔�j�̏ꍇ
		if (isFormula) {
			String body = this.formula.getBody();
			if (StringUtils.isEmpty(body)) {
				// �Œ�l�̏ꍇ
				// FIXME �Œ�l�ł���΁AFormulaParser�̂Ƃ���Œ���D�����Ƃ���Token����肢������Ȃ��ł���
				ret = new BigDecimal(Double.toString(this.formula.getValue()));

			} else {
				// ������`�Ŏw�肵���v�Z��b�ǂ���ɐ؂�ւ�				
				boolean isShifted = _ctx.shiftFundation(this.formula);
				
				//�L���b�V���L�[���쐬���������A�L���b�V���l���擾
				String key = _ctx.getContextKey(true).append("#").append(getCacheKey()).toString();
				ret = _ctx.getCache().getCacheVaule(key);
				
				if (ret == null){
					//�L���b�V�����Ă��Ȃ��ꍇ�A�v�Z���s��
					Sequence seqBody = _ctx.getParser().parse(this.formula.getBody());
					Double noRoundingRet = seqBody.eval().doubleValue();
					
					/*
					 * �[���������s��
					 * �[��������-n�ɂȂ�ƁA10��n�����܂Ŏl�̌ܓ�����
					 * e.g.-1�̏ꍇ�A123->120
					 */
					int scale = this.formula.getFraction();
					if (scale == Integer.MIN_VALUE) {
						ret = new BigDecimal(noRoundingRet.toString());
					} else {
						ret = new BigDecimal(Double.toString(SystemFunctionUtility.round(noRoundingRet, scale)));						
					}
					
					String varInfo = this.formula.toString();
					//�T�u�����̃t�B���^�[
					if (_ctx.isFilterFormula(this.formula)) {
						if (_ctx.isRounding()) {
							_ctx.getIntermediateValue().put(varInfo, ret);
						} else {
							_ctx.getIntermediateValue().put(varInfo + "�i�[�������Ȃ��j", noRoundingRet);
						}
					}
					if (logger.isInfoEnabled()) {
						logger.info("[VAR]" + varInfo + "=" + ret);
					}
					
					//set�L�[���[�h�ō�����Վ��ϐ��̃X�R�[�v�͓��Y�Z���Ɍ��邽�߁A
					//�v�Z��������Ƃ��ɁA�R���e�L�X�g����O��
					for (Set set : seqBody.getAllSetBlocks()) {
						_ctx.removeTempVariable(set.getVariantName());	
					}
					
					//�v�Z�l���L���b�V���ɒǉ�
					_ctx.getCache().addToCache(key, ret);
					
				}
				
				_ctx.rollbackFundation(isShifted, this.formula);
				
			}
		} else {
			/*
			 * ���ϐ��̒l�̎擾�ɂ��āA�D�揇�� cache > func parameter context 
			 * > compute context > ratekey context > SYSFUNC reflect call�Ƃ���
			 * �ϐ��i�����ɒ�`���ꂽ�ꎞ�ϐ������SYSFUNC�A�v�Z�L���j
			 */
			Object tokenVal = _ctx.getTokenValue(name);
			ret = (BigDecimal)ConvertUtils.convert(tokenVal, BigDecimal.class);

		}
				
		return ret;
	}

	@Override
	public String getCacheKey() {
				
		/*
		 * TODO ���������̏ꍇ�A�N���E���N�����A�������Ɋւ�炸�A
		 * �Prate�������Ȃ̂ŁA�Prate�P�ʂ�cache���悤�ɁB
		 */
		return this.name;
	}
	
	@Override
	public Formula getFormula() {
		return this.formula;
	}


}
