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
 * FMSシステム上定義させず、直接に使えるファンクションである 
 * <p>SYSFUNCはSysFunctionMapping.propertiesに定義されて前提である、
 * またメソッド自体も実装必要である<p>
 * <br>現時点では、round,roundUp,roundDown,min,maxなどは実現しております
 * @author btchoukug
 *
 */
public class SysFunction extends Function {

	/** SYSFUNCのメソッド自体のパッケージパス */
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

	/** SYSFUNC実体のパラメータタイプ */
	private Class[] paraTypes;

	/** SYSFUNC実体のパラメータの値 */
	private Object[] paraValues = null;

	public SysFunction(String functionName, ICalculateContext ctx) throws FmsDefErrorException {
		super(functionName, ctx);
		_createReflectMethod(functionName);
	}
	
	/**
	 * ax_n=RateUtility.ax_n(int x,int n)の形でメソッド名およびパラメータの情報を取得
	 * 当システムについては、公式のパラメータのタイプは２つしかない：intとdouble<br>
	 * intは外部入力値である；doubleは途中計算値である。
	 * @throws FmsDefErrorException 
	 */
	@SuppressWarnings("unchecked")
	private void _createReflectMethod(String functionName) throws FmsDefErrorException {

		// プロパティに書いたfunc名を編集した上、func詳細を取得
		String methodDetail = methodProps.getProperty(SYS_FUNC + Const.DOT + functionName);
		if (methodDetail == null || methodDetail.equals(Const.EMPTY)) {
			throw new FmsDefErrorException("当SYSFUNC[" + functionName + "]は定義されてない");
		}

		String[] methodDetails = methodDetail.split(DELIMITER);
		if (methodDetails.length != 2) {
			throw new FmsDefErrorException("当SYSFUNC[" + functionName + "]にはシステムメソッドマッピングにが間違えた");
		}

		//TODO SYSFUNCの定義フォーマットは改善予定
		String className = methodDetails[0];
		String methodBody = methodDetails[1];
		String methodName = methodBody.substring(0, methodBody.indexOf("("));

		// パラメータの中身：int x,int nを取得
		String paraBody = methodBody.substring(methodBody.indexOf("(") + 1, methodBody.indexOf(")"));
		
		// Java reflect methodを取得
		if (StringUtils.isBlank(paraBody)) {
			// パラメータがない場合、パラメータのタイプと値を全部Emptyとする
			paraTypes = new Class[]{};
			paraValues = new Object[]{};
		} else {
			//パラメータがある場合、パラメータのタイプと値を取得
			String[] paras = paraBody.split(Const.COMMA);
			paraTypes = new Class[paras.length];
			
			for (int i = 0; i < paras.length; i++) {
				String[] paraDetail = paras[i].split(Const.SPACE);
				if (paraDetail.length != 2) {
					throw new FmsDefErrorException("当SYSFUNCのパラメータには間違えた：" + paraBody);
				}
				paraTypes[i] = _getParaTypeClass(paraDetail[0]);
			}
		}

		try {
			clazz = ClassUtils.getClass(SYSFUNC_PARKAGE_PATH + Const.DOT + className);
			method = clazz.getDeclaredMethod(methodName, paraTypes);
		} catch (ClassNotFoundException e) {
			throw new FmsDefErrorException("SYSFUNCに定義されたクラスが見つからない："
					+ className, e);
		} catch (SecurityException e) {
			throw new FmsDefErrorException("クラスやメソッドアクセスできない：" + methodName, e);
		} catch (NoSuchMethodException e) {
			throw new FmsDefErrorException("Javaメソッドが見つからない：" + methodName, e);
		}

	}

	/**
	 * SYSFUNCの場合、外部からパラメータの値はparaValuesを指定したうえ計算する
	 */
	@Override
	public BigDecimal result() {
		
		Double ret = 0d;
		try {
			if(target == null) {
				target = this.clazz.newInstance();
			}
			//ターゲットのメソッドをコールする			
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
	 * Reflectコールのため、パラメータの型を編集
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
			throw new FmsDefErrorException("intとdouble以外のパラメータタイプを指定されてしまう：" + type);
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
