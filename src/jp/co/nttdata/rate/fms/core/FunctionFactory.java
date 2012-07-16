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
 * ファンクションを作成するためのFunctionFactory
 * @author btchoukug
 *
 */
public class FunctionFactory {

	private static final String SYS_FUNCTIONS = "sys_func";
	
	private static Properties funcProp = PropertiesUtil.getPropertiesByClass(SystemFunctionUtility.class, Const.SYSFUNC_MAPPING);	
	
	private Map<String,Function> functionPool;
	private ResultEvaluator _evaluator;
	private String[] sysFuncs;
	
	//FIXME 現時点のContextでformulaManagerで算式管理のやり方はここでやれ
	//I/Fの形でDefFunction生成タイミングで算式是否を判明する
	//protected IFormulaManager formulaMgr;
	
	//TODO singletonに変更予定
	public FunctionFactory() {

		functionPool = new HashMap<String,Function>();
		_evaluator = new ResultEvaluator();
		
		String sysFuncDef = funcProp.getProperty(SYS_FUNCTIONS, "");
		//SYSFUNC名は外部プロパティファイルに定義可能にする	
		if (StringUtils.isBlank(sysFuncDef)) {
			throw new FmsRuntimeException("SysFunctionMapping.propertiesには、SYSFUNCが適当的に設定されてない");
		}
		sysFuncs = StringUtils.split(sysFuncDef, Const.COMMA);
	}
		
	/**
	 * ファンクションのインスタンスを返す
	 * @param funcName
	 * @return
	 * @throws Exception 
	 */
	public Function getInstance(String funcName, Sequence seq, Range r) throws Exception {

		Function func = null;
		
		if (functionPool == null) {
			throw new IllegalArgumentException("FunctionFactoryは正常に初期化されない。");
		}
		
		//すでに作成ことがあれば、元のオブジェクトを返す
		if (functionPool.containsKey(funcName)) {
			func = functionPool.get(funcName);
			//カレントコンテキストを上書き
			func._ctx = seq.getContext();
		} else {
			//SYSFUNC判定
			if (ArrayUtils.contains(sysFuncs, funcName)) {
				func = new SysFunction(funcName, seq.getContext());
			} else {
				func = new DefFunction(funcName, seq.getContext());
			}
			
			functionPool.put(funcName, func);
		}
		
		//毎回新たにtokensからパラメータの値をセットする
		/*
		 * SYSFUNCの場合、パラメータを取得して、「パラメータ名：パラメータ値」という形でコンテキストのComputeContextにセットする
		 * DefFuncの場合、funcParameterStackにpushする
		 */
		if (func instanceof SysFunction) {
			_setFuncParaValues((SysFunction)func, seq, r);
		} else {
			_setFuncParaValues((DefFunction)func, seq, r);
		}
		
		return func;	
	}
	
	/**
	 * 計算tokensからSYSFUNのパラメータを設定
	 * <br>例：round(AAA,5)
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
				
				//reflectでコールするため、予めパラメータの値を求める
				Class type = func.getParaTypes()[j];
				Double doubleValue = _evaluator.getOperandValue(t).doubleValue();
				
				//メソッドのパラメータタイプどおり、パラメータ値を編集する
				//primitiveタイプの場合、unboxやboxができるため、primitive或はwrapperタイプはどちらも使える
				Object value = null;
				if (type == Integer.class || type == int.class) {
					value = doubleValue.intValue();
				} else if (type == Double.class || type == double.class) {
					value = doubleValue;
				} else {
					throw new FmsDefErrorException("SYSFUNCのパラメータはint(Integer)とdouble(Double)以外のパラメータタイプは指定できない。");
				}

				values.add(value);
				j++;
			}
		}
		
		func.setParaValues(values.toArray());
		
	}
	
	/**
	 * 計算tokensからDEFFUNのパラメータを設定
	 * <br>例：Vpn(n)
	 * @param func
	 * @param r 
	 * @param tokens
	 * @throws Exception 
	 */
	private void _setFuncParaValues(DefFunction func, Sequence seq, Range r) throws Exception {

		List<String> paraNameList = func.getParaNameList();		
		
		//パラメータ定義有無チェック
		if (paraNameList == null || paraNameList.size() == 0) {
			throw new FmsDefErrorException(func.getFormula().toString() + "にパラメータは定義せず使われてしまった。");
		}
		
		//毎回計算前、初期化する
		Map<String, Object> paras = new HashMap<String, Object>();		
		
		//rという範囲は呼び元のパラメータ範囲
		int paraNum = paraNameList.size();
		int j = 0;
		
		for (int i = r.start; i < r.end; i++) {
			
			Token t = seq.get(i);
			/*
			 * パラメータ値編集だけ
			 * 商品毎に定義の公式について、中身にサブ公式の計算するため、パラメータを保持する
			 * 例：e(t)=alpha2(t)+gamma1
			 */
			if (t.isDecimal() || t.isVariable() || t.isArray()) {
				
				//パラメータが変数の場合、当該パラメータが定義されるかどうかチェック
				if (j >= paraNum) {
					// 0702公式のパラメータチェックについて、メッセージ修正
					throw new FmsDefErrorException(func.getFormula().toString() + "に使われるところ、パラメータ数は間違ってしまいました。");										
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
