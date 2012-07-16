package jp.co.nttdata.rate.fms.core;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;

import jp.co.nttdata.rate.exception.FmsDefErrorException;
import jp.co.nttdata.rate.exception.FmsRuntimeException;
import jp.co.nttdata.rate.fms.calculate.ResultEvaluator;
import jp.co.nttdata.rate.fms.common.SystemFunctionUtility;
import jp.co.nttdata.rate.util.Const;
import jp.co.nttdata.rate.util.PropertiesUtil;

/**
 * �t�@���N�V�������쐬���邽�߂�FunctionFactory
 * @author btchoukug
 *
 */
public class FunctionFactory {

	private static final String SYS_FUNCTIONS = "sys_func";
	
	private static Properties funcProp = PropertiesUtil.getPropertiesByClass(SystemFunctionUtility.class, Const.SYSFUNC_MAPPING);	
	
	private Map<String,Function> functionPool;
	private ResultEvaluator _evaluator;
	private String[] sysFuncs;
	
	//FIXME �����_��Context��formulaManager�ŎZ���Ǘ��̂����͂����ł��
	//I/F�̌`��DefFunction�����^�C�~���O�ŎZ�����ۂ𔻖�����
	//protected IFormulaManager formulaMgr;
	
	//TODO singleton�ɕύX�\��
	public FunctionFactory() {

		functionPool = new HashMap<String,Function>();
		_evaluator = new ResultEvaluator();
		
		String sysFuncDef = funcProp.getProperty(SYS_FUNCTIONS, "");
		//SYSFUNC���͊O���v���p�e�B�t�@�C���ɒ�`�\�ɂ���	
		if (StringUtils.isBlank(sysFuncDef)) {
			throw new FmsRuntimeException("SysFunctionMapping.properties�ɂ́ASYSFUNC���K���I�ɐݒ肳��ĂȂ�");
		}
		sysFuncs = StringUtils.split(sysFuncDef, Const.COMMA);
	}
		
	/**
	 * �t�@���N�V�����̃C���X�^���X��Ԃ�
	 * @param funcName
	 * @return
	 * @throws Exception 
	 */
	public Function getInstance(String funcName, Sequence seq, Range r) throws Exception {

		Function func = null;
		
		if (functionPool == null) {
			throw new IllegalArgumentException("FunctionFactory�͐���ɏ���������Ȃ��B");
		}
		
		//���łɍ쐬���Ƃ�����΁A���̃I�u�W�F�N�g��Ԃ�
		if (functionPool.containsKey(funcName)) {
			func = functionPool.get(funcName);
			//�J�����g�R���e�L�X�g���㏑��
			func._ctx = seq.getContext();
		} else {
			//SYSFUNC����
			if (ArrayUtils.contains(sysFuncs, funcName)) {
				func = new SysFunction(funcName, seq.getContext());
			} else {
				func = new DefFunction(funcName, seq.getContext());
			}
			
			functionPool.put(funcName, func);
		}
		
		//����V����tokens����p�����[�^�̒l���Z�b�g����
		/*
		 * SYSFUNC�̏ꍇ�A�p�����[�^���擾���āA�u�p�����[�^���F�p�����[�^�l�v�Ƃ����`�ŃR���e�L�X�g��ComputeContext�ɃZ�b�g����
		 * DefFunc�̏ꍇ�AfuncParameterStack��push����
		 */
		if (func instanceof SysFunction) {
			_setFuncParaValues((SysFunction)func, seq, r);
		} else {
			_setFuncParaValues((DefFunction)func, seq, r);
		}
		
		return func;	
	}
	
	/**
	 * �v�Ztokens����SYSFUN�̃p�����[�^��ݒ�
	 * <br>��Fround(AAA,5)
	 * @param func
	 * @param r 
	 * @param tokens 
	 * @throws FmsDefErrorException 
	 */
	@SuppressWarnings("unchecked")
	private void _setFuncParaValues(SysFunction func, Sequence seq, Range r) throws Exception {

		int j = 0;
		List values = new ArrayList();

		for (int i = r.start; i < r.end; i++) {
			
			Token t = seq.get(i);				
			if (t.isDecimal() || t.isVariable() || t.isArray()) {
				
				//reflect�ŃR�[�����邽�߁A�\�߃p�����[�^�̒l�����߂�
				Class type = func.getParaTypes()[j];
				Double doubleValue = _evaluator.getOperandValue(t).doubleValue();
				
				//���\�b�h�̃p�����[�^�^�C�v�ǂ���A�p�����[�^�l��ҏW����
				//primitive�^�C�v�̏ꍇ�Aunbox��box���ł��邽�߁Aprimitive����wrapper�^�C�v�͂ǂ�����g����
				Object value = null;
				if (type == Integer.class || type == int.class) {
					value = doubleValue.intValue();
				} else if (type == Double.class || type == double.class) {
					value = doubleValue;
				} else {
					throw new FmsDefErrorException("SYSFUNC�̃p�����[�^��int(Integer)��double(Double)�ȊO�̃p�����[�^�^�C�v�͎w��ł��Ȃ��B");
				}

				values.add(value);
				j++;
			}
		}
		
		func.setParaValues(values.toArray());
		
	}
	
	/**
	 * �v�Ztokens����DEFFUN�̃p�����[�^��ݒ�
	 * <br>��FVpn(n)
	 * @param func
	 * @param r 
	 * @param tokens
	 * @throws Exception 
	 */
	private void _setFuncParaValues(DefFunction func, Sequence seq, Range r) throws Exception {

		List<String> paraNameList = func.getParaNameList();		
		
		//�p�����[�^��`�L���`�F�b�N
		if (paraNameList == null || paraNameList.size() == 0) {
			throw new FmsDefErrorException(func.getFormula().toString() + "�Ƀp�����[�^�͒�`�����g���Ă��܂����B");
		}
		
		//����v�Z�O�A����������
		Map<String, Object> paras = new HashMap<String, Object>();		
		
		//r�Ƃ����͈͂͌Ăь��̃p�����[�^�͈�
		int paraNum = paraNameList.size();
		int j = 0;
		
		for (int i = r.start; i < r.end; i++) {
			
			Token t = seq.get(i);
			/*
			 * �p�����[�^�l�ҏW����
			 * ���i���ɒ�`�̌����ɂ��āA���g�ɃT�u�����̌v�Z���邽�߁A�p�����[�^��ێ�����
			 * ��Fe(t)=alpha2(t)+gamma1
			 */
			if (t.isDecimal() || t.isVariable() || t.isArray()) {
				
				//�p�����[�^���ϐ��̏ꍇ�A���Y�p�����[�^����`����邩�ǂ����`�F�b�N
				if (j >= paraNum) {
					// 0702�����̃p�����[�^�`�F�b�N�ɂ��āA���b�Z�[�W�C��
					throw new FmsDefErrorException(func.getFormula().toString() + "�Ɏg����Ƃ���A�p�����[�^���͊Ԉ���Ă��܂��܂����B");										
				}
				
				String name = paraNameList.get(j);
				BigDecimal value = _evaluator.getOperandValue(t);
				paras.put(name, value);
				
				j++;				
			}
		}
		
		func.setParas(paras);

	}
	
}
