package jp.co.nttdata.rate.fms.calculate;

import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

import jp.co.nttdata.rate.exception.FmsRuntimeException;
import jp.co.nttdata.rate.log.LogFactory;
import jp.co.nttdata.rate.model.formula.Formula;
import jp.co.nttdata.rate.model.formula.FormulaManager;
import org.apache.log4j.Logger;

public abstract class AbstractCalculateContext implements ICalculateContext {

	protected Logger logger = LogFactory.getInstance(this.getClass());
	/**
	 * ��{�I�Ɍv�Z�̊�b�P�ʂ��i�[����
	 * <br>�i���[�g�L�[�A���SYS�ϐ��j
	 */
	@SuppressWarnings("unchecked")
	protected Map tokenValueMap;
	
	/** �w�肳�ꂽ�Z���̒l���t�B���^�[���邩�Ȃ���(�f�t�H���g�Ƃ��ăt�B���^�[���Ȃ�) */
	protected boolean enableFilter = false;
	
	/**
	 * ���t�@���N�V������葼�̃t�@���N�V�������Ăяo���Ɣ��ɁA �p�����[�^�͌Ăя��ŕێ����邱�� ��
	 */
	protected Stack<Map<String, Object>> funcParameterStack = new Stack<Map<String, Object>>();
	
	/** set�L�[���[�h�ŗՎ��ϐ��̊i�[��@*/
	protected Map<String, Object> setValues = new HashMap<String, Object>();	
	
	protected Map<String, Object> intermediateValues;

	protected FormulaManager formulaMgr;
	
	public AbstractCalculateContext() {
		;
	}
	@Override
	public FormulaManager getFormulaManager() {
		if (this.formulaMgr == null) {
			throw new RuntimeException("Formula�}�l�W��������������Ă��܂���");
		}
		return this.formulaMgr;
	}
	
	@Override
	public void setFormulaManager(FormulaManager mgr) {
		formulaMgr = mgr;		
	}
	
	@Override
	public Formula getFormula(String var) {
		if (this.formulaMgr == null) {
			return null;
		}
		return formulaMgr.getFormula(var);
	}
	
	@Override
	public boolean isFuncExist(String funcName) {
		if (formulaMgr == null) return false;
		return formulaMgr.isExist(funcName);
	}
	
	@Override
	public boolean isFilterFormula(Formula formula) {
		
		if (enableFilter) {
			if (formulaMgr == null) return false;
			return formulaMgr.isFilterFormula(formula);			
		} 
		
		return false;
	}
	
	@Override
	public boolean isRounding() {
		if (formulaMgr == null) return false;
		return formulaMgr.isRounding();
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public void setInput(final Map input) {
		reset();
		this.tokenValueMap = input;		
		if (logger.isInfoEnabled()) {
			logger.info("���͂̃��[�g�L�[�F" + input.toString());
		}
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public Map getInput() {
		return this.tokenValueMap;
	}
	
	/**
	 * ���ꂼ��FUNC�̃p�����[�^�𕪂���悤�ɁA�Ăя����stack��push����
	 * FUNC���v�Z�I������A�������p�����[�^��pop����
	 * @param paras
	 */
	@Override
	public void addFunctionPara(Map<String, Object> paras) {
		this.funcParameterStack.push(paras);
	}

	/**
	 * �Q�����v�Z���A���I�Ƀp�����[�^��ҏW���邽�߁A�R���e�L�X�g�Ƀp�����[�^��ǉ� <br>
	 * set�L�[���[�h�ňꎞ�ϐ��̐ݒ� ���s��<br>
	 * �p�����[�^�����ɑ��݂��Ă����ꍇ�A�㏑���Ƃ���
	 */
	@Override
	public boolean addTempVariable(String varName, Object varValue) {
		setValues.put(varName, varValue);
		if (logger.isDebugEnabled()) {
			logger.debug("set�L�[���[�h�ō��ꂽ�ꎞ�I�ȕϐ��F" + varName + "=" + varValue);
		}
		return true;
	}

	@Override
	public void clearCurrentFunctionPara() {
		this.funcParameterStack.pop();
	}

	@Override
	public Object getFunctionPara(String paraName) {
		if (this.funcParameterStack.isEmpty()) return null; 
		return this.funcParameterStack.peek().get(paraName);
	}

	@Override
	public Object getTempVariable(String varName) {
		return setValues.get(varName);
	}

	@Override
	public Object getTokenValue(String tokenName) {
		if (this.tokenValueMap == null) {
			throw new FmsRuntimeException("���[�g�L�[��ݒ肵�Ă��������B");
		}

		Object value = null;
		//�܂��A�Վ��ϐ��Ƃ݂Ȃ��Ď擾���Ă݂�
		if (setValues.containsKey(tokenName)) {
			return setValues.get(tokenName);
		} else {
			//��ł͂Ȃ��ꍇ�A���[�g�L�[����擾
			value = this.tokenValueMap.get(tokenName);
		}

		//��L�Ŏ擾�ł��Ȃ������ꍇ�A�G���[�Ƃ���
		if (value == null) {
			throw new FmsRuntimeException("���[�g�L�[��Վ��ϐ�[" + tokenName + "]�̒l���擾�ł��Ȃ�����!");			
		}

		return value;
	}

	@Override
	public boolean removeTempVariable(String varName) {
		setValues.remove(varName);	
		return true;		
	}
	
	@Override
	public Map<String, Object> getLastParas() {
		if (funcParameterStack.isEmpty()) return null;
		return funcParameterStack.peek();		
	}
	

	@Override
	public void reset() {
		this.tokenValueMap = null;
		this.funcParameterStack.clear();
		
		this.setValues.clear();
		this.intermediateValues = null;	
	}
	
}
