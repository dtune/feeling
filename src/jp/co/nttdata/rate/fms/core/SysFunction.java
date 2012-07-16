package jp.co.nttdata.rate.fms.core;

import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.util.Properties;
import org.apache.commons.lang.ClassUtils;
import org.apache.commons.lang.StringUtils;

import jp.co.nttdata.rate.exception.FmsDefErrorException;
import jp.co.nttdata.rate.exception.FmsRuntimeException;
import jp.co.nttdata.rate.fms.calculate.ICalculateContext;
import jp.co.nttdata.rate.fms.common.SystemFunctionUtility;
import jp.co.nttdata.rate.util.Const;
import jp.co.nttdata.rate.util.PropertiesUtil;

/**
 * 
 * FMS�V�X�e�����`�������A���ڂɎg����t�@���N�V�����ł��� 
 * <p>SYSFUNC��SysFunctionMapping.properties�ɒ�`����đO��ł���A
 * �܂����\�b�h���̂������K�v�ł���<p>
 * <br>�����_�ł́Around,roundUp,roundDown,min,max�Ȃǂ͎������Ă���܂�
 * @author btchoukug
 *
 */
public class SysFunction extends Function {

	/** SYSFUNC�̃��\�b�h���̂̃p�b�P�[�W�p�X */
	public static final String SYSFUNC_PARKAGE_PATH = "jp.co.nttdata.rate.fms.common";
	public static final String SYS_FUNC = "sys_func";
	private static final String DELIMITER = "\\.";
	
	private static final String INT = "int";
	private static final String INTEGER = "Integer";
	private static final String PRIMITIVE_DOUBLE = "double";
	private static final String DOUBLE = "Double";	
	
	private static Properties methodProps = PropertiesUtil.getPropertiesByClass(SystemFunctionUtility.class, Const.SYSFUNC_MAPPING);
			
	/** reflect call java class & method */
	private Class clazz;
	private Object target;
	private Method method;

	/** SYSFUNC���̂̃p�����[�^�^�C�v */
	private Class[] paraTypes;

	/** SYSFUNC���̂̃p�����[�^�̒l */
	private Object[] paraValues = null;

	public SysFunction(String functionName, ICalculateContext ctx) throws FmsDefErrorException {
		super(functionName, ctx);
		_createReflectMethod(functionName);
	}
	
	/**
	 * ax_n=RateUtility.ax_n(int x,int n)�̌`�Ń��\�b�h������уp�����[�^�̏����擾
	 * ���V�X�e���ɂ��ẮA�����̃p�����[�^�̃^�C�v�͂Q�����Ȃ��Fint��double<br>
	 * int�͊O�����͒l�ł���Gdouble�͓r���v�Z�l�ł���B
	 * @throws FmsDefErrorException 
	 */
	@SuppressWarnings("unchecked")
	private void _createReflectMethod(String functionName) throws FmsDefErrorException {

		// �v���p�e�B�ɏ�����func����ҏW������Afunc�ڍׂ��擾
		String methodDetail = methodProps.getProperty(SYS_FUNC + Const.DOT + functionName);
		if (methodDetail == null || methodDetail.equals(Const.EMPTY)) {
			throw new FmsDefErrorException("��SYSFUNC[" + functionName + "]�͒�`����ĂȂ�");
		}

		String[] methodDetails = methodDetail.split(DELIMITER);
		if (methodDetails.length != 2) {
			throw new FmsDefErrorException("��SYSFUNC[" + functionName + "]�ɂ̓V�X�e�����\�b�h�}�b�s���O�ɂ��ԈႦ��");
		}

		//TODO SYSFUNC�̒�`�t�H�[�}�b�g�͉��P�\��
		String className = methodDetails[0];
		String methodBody = methodDetails[1];
		String methodName = methodBody.substring(0, methodBody.indexOf("("));

		// �p�����[�^�̒��g�Fint x,int n���擾
		String paraBody = methodBody.substring(methodBody.indexOf("(") + 1, methodBody.indexOf(")"));
		
		// Java reflect method���擾
		if (StringUtils.isBlank(paraBody)) {
			// �p�����[�^���Ȃ��ꍇ�A�p�����[�^�̃^�C�v�ƒl��S��Empty�Ƃ���
			paraTypes = new Class[]{};
			paraValues = new Object[]{};
		} else {
			//�p�����[�^������ꍇ�A�p�����[�^�̃^�C�v�ƒl���擾
			String[] paras = paraBody.split(Const.COMMA);
			paraTypes = new Class[paras.length];
			
			for (int i = 0; i < paras.length; i++) {
				String[] paraDetail = paras[i].split(Const.SPACE);
				if (paraDetail.length != 2) {
					throw new FmsDefErrorException("��SYSFUNC�̃p�����[�^�ɂ͊ԈႦ���F" + paraBody);
				}
				paraTypes[i] = _getParaTypeClass(paraDetail[0]);
			}
		}

		try {
			clazz = ClassUtils.getClass(SYSFUNC_PARKAGE_PATH + Const.DOT + className);
			method = clazz.getDeclaredMethod(methodName, paraTypes);
		} catch (ClassNotFoundException e) {
			throw new FmsDefErrorException("SYSFUNC�ɒ�`���ꂽ�N���X��������Ȃ��F"
					+ className, e);
		} catch (SecurityException e) {
			throw new FmsDefErrorException("�N���X�⃁�\�b�h�A�N�Z�X�ł��Ȃ��F" + methodName, e);
		} catch (NoSuchMethodException e) {
			throw new FmsDefErrorException("Java���\�b�h��������Ȃ��F" + methodName, e);
		}

	}

	/**
	 * SYSFUNC�̏ꍇ�A�O������p�����[�^�̒l��paraValues���w�肵�������v�Z����
	 */
	@Override
	public BigDecimal result() {
		
		Double ret = 0d;
		try {
			if(target == null) {
				target = this.clazz.newInstance();
			}
			//�^�[�Q�b�g�̃��\�b�h���R�[������			
			ret = (Double) this.method.invoke(target, this.paraValues);
		} catch (Exception e) {
			StringBuilder sb = new StringBuilder(this.method.getName() + "(");
			for (Object val : this.paraValues) {
				sb.append(val).append(Const.COMMA);
			}
			throw new FmsRuntimeException(sb.deleteCharAt(sb.length()-1).append(")").toString(), e);
		}
		
		return new BigDecimal(Double.toString(ret));
	}
	
	/**
	 * Reflect�R�[���̂��߁A�p�����[�^�̌^��ҏW
	 * @param type
	 * @return
	 * @throws FmsDefErrorException
	 */
	@SuppressWarnings("unchecked")
	private Class _getParaTypeClass(String type) throws FmsDefErrorException {
		Class clz;
		if (type.equals(INT)) {
			clz = int.class;
		} else if (type.equals(INTEGER)) {
			clz = Integer.class;
		} else if (type.equals(PRIMITIVE_DOUBLE)) {
			clz = double.class;
		} else if (type.equals(DOUBLE)) {
			clz = Double.class;
		} else {
			throw new FmsDefErrorException("int��double�ȊO�̃p�����[�^�^�C�v���w�肳��Ă��܂��F" + type);
		}
		
		return clz;
	}

	public void setParaValues(Object[] paraValues) {
		this.paraValues = paraValues;
	}

	public Class[] getParaTypes() {
		return paraTypes;
	}
	
}
