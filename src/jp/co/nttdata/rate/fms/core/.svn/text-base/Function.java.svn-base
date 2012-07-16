package jp.co.nttdata.rate.fms.core;

import java.math.BigDecimal;

import jp.co.nttdata.rate.fms.calculate.ICalculateContext;
import jp.co.nttdata.rate.log.LogFactory;
import org.apache.log4j.Logger;

/**
 * 汎用型のファンクションである
 * <p>subClassはSYSFUNCとDEFFUNC２つとなる</p>
 * @author btchoukug
 *
 */
public abstract class Function {
	
	protected static Logger logger = LogFactory.getInstance(Function.class);

	protected String funcName;

	/** 計算コンテキスト */
	protected ICalculateContext _ctx;
	
	public Object[] paraValues;
		
	public Function(String functionName, ICalculateContext ctx) {
		this.funcName = functionName;
		this._ctx = ctx;
	}
		
	/**
	 * <p>SYSFUNCはReflectで実なメソッドをコールして結果を算出する</p>
	 * <p>DEFFUNCは解析して結果を算出する</p>
	 * @return
	 */
	public abstract BigDecimal result() throws Exception;
	
}
