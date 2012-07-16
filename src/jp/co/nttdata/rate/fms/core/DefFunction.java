package jp.co.nttdata.rate.fms.core;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import jp.co.nttdata.rate.exception.FmsDefErrorException;
import jp.co.nttdata.rate.fms.calculate.ICalculateContext;
import jp.co.nttdata.rate.fms.common.SystemFunctionUtility;
import jp.co.nttdata.rate.fms.core.keyword.Set;
import jp.co.nttdata.rate.model.formula.Formula;
import jp.co.nttdata.rate.util.Const;
/**
 * 
 * ���ꂼ��̎Z���ɂ��āA���R�ɏ����Ď����I�Ɍv�Z�����悤�ɒ�`FUNCTION�ł��� <br>
 * �Z���̒��ɁAif,elseIf,else,while,sum,set�L�[���[�h��ratekey,fundation
 * �̋L���͎g���邵�A�I�y���[�^���l���v�Z���ł���B
 * 
 * @author btchoukug
 * 
 */
public class DefFunction extends Function implements Cacheable{

	/** ��`�̌v�Z��(��Fe(t)=if(kaisu==1&&t<=10){0.0008}else{0})*/
	private Formula formula;
	/** FUNC�̃p�����[�^���X�g */
	private List<String> paraNameList;

	/** DEFFUNC���v�Z����ۂɁA�p�����[�^�擾�ɂ��Ă͉��L��paras���ŗD��Ƃ��� 
	 * <p>paras�̒��ɑ��݂��Ă��Ȃ��ꍇ�A�ق��̎擾��iratekey,fundation,�Վ��ϐ��j����擾����</p>*/
	private Map<String, Object> paras = null;
	
	public DefFunction(String functionName, ICalculateContext ctx) throws FmsDefErrorException {
		
		super(functionName, ctx);
		
		//FIXME Formula���X�g�̊Ǘ��͊O�����璍������
		if (_ctx.isFuncExist(this.funcName)) {
			this.formula = _ctx.getFormula(this.funcName);
			// �v�Z���\�b�h�̃p�����[�^���擾
			this.setParaNameList(this.formula.getParas());
		} else {
			throw new FmsDefErrorException(this.funcName + "���L������`����ĂȂ��B");
		}
		
	}

	@Override
	public BigDecimal result() throws Exception {

		// �Z����`�Ŏw�肵���v�Z��b�ʂ�ɐ؂�ւ����s��
		boolean isShifted = _ctx.shiftFundation(this.formula);
		
		// �L���b�V���ۂ��A���[�g�L�[�Ɗ֌W���𔻖�
		// TODO �����_�ł�formula��attr[cacheable]�Ŏw�肳���G
		//�@�����I�ɁA�v���O�����Ŏ����I�ɔ��������悤�ɉ��P�������ł�
		boolean isRatekeyRelated = !this.formula.isCacheable();
		
		//�L���b�V���L�[���쐬���������A�L���b�V���l���擾
		String key = _ctx.getContextKey(isRatekeyRelated).append("#").append(getCacheKey()).toString();		
		BigDecimal ret = _ctx.getCache().getCacheVaule(key);
		
		// �L���b�V�����Ă��Ȃ��ꍇ�A�v�Z���s��
		if (ret == null) {
			// �܂��A�p�����[�^���R���e�L�X�g�̐�pstack�ɒǉ�
			_ctx.addFunctionPara(this.paras);
			
			Sequence seqBody = _ctx.getParser().parse(this.formula.getBody());
			Double withoutRoundingRet = seqBody.eval().doubleValue();		

			/*
			 * �[���������s���i�v�Z���̑����Œ�`������A����round���������藼���Ƃ��ł���j
			 * �[��������-n�ɂȂ�ƁA10��n�����܂Ŏl�̌ܓ�����
			 * e.g.-1�̏ꍇ�A123->120
			 */
			int scale = this.formula.getFraction();
			if (scale == Integer.MIN_VALUE) {
				ret = new BigDecimal(withoutRoundingRet.toString());
			} else {
				ret = new BigDecimal(Double.toString(SystemFunctionUtility.round(withoutRoundingRet, scale)));						
			}			
						
			//�T�u�����̃t�B���^�[
			if (_ctx.isFilterFormula(this.formula)) {
				String funcInfo = this.formula.toString() + this.paras.toString();
				if (_ctx.isRounding()) {
					_ctx.getIntermediateValue().put(funcInfo, ret);
				} else {
					_ctx.getIntermediateValue().put(funcInfo + "�i�[�������Ȃ��j", withoutRoundingRet);
				}				
			}
			
			if (logger.isInfoEnabled()) {
				StringBuilder sb = new StringBuilder("[FUNC]");
				sb.append(this.formula.toString()).append(this.paras.toString()).append("=").append(ret);
				logger.info(sb.toString());	
			}
			
			// ��FUNC���v�Z��������A�p�����[�^��pop���āA�ďo���̃p�����[�^�ɖ߂�
			_ctx.clearCurrentFunctionPara();
			
			
			/*
			 * set�L�[���[�h�ō�����Վ��ϐ��̃X�R�[�v�͓��Y�Z���Ɍ��邽�߁A
			 * �v�Z��������Ƃ��ɁA�R���e�L�X�g����O��
			 */
			for (Set set : seqBody.getAllSetBlocks()) {
				_ctx.removeTempVariable(set.getVariantName());	
			}			
			
			//�v�Z�l���L���b�V���ɒǉ�
			_ctx.getCache().addToCache(key, ret);
			
		}

		_ctx.rollbackFundation(isShifted, this.formula);
				
		return ret;
	}

	@Override
	public String toString() {
		return this.funcName + (this.paras == null ? "" : this.paras.toString());
	}

	/**
	 * ���ʂ̏ꍇ�AFuncKey���쐬����̂̓p�����[�^��name:value 
	 * <br>�v�Z��b��p���Ă�������̏ꍇ�A�v�Z��b��t����
	 * 
	 * @return
	 */
	@SuppressWarnings("unchecked")
	@Override
	public String getCacheKey() {
		Collection collection = this.paras.values();
		StringBuilder sbKey = new StringBuilder(this.funcName);
		for (Object obj : collection) {
			sbKey.append(Const.DASH).append(obj);
		}
		
		return sbKey.toString();
	}

	public void setParaNameList(List<String> paraNameList) {
		this.paraNameList = paraNameList;
	}
	
	public void setParas(Map<String, Object> paras) {
		this.paras = paras;
	}
	
	@Override
	public Formula getFormula() {
		return formula;
	}

	public List<String> getParaNameList() {
		return paraNameList;
	}

		
}
