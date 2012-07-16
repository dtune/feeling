package jp.co.nttdata.rate.fms.calculate;

import java.util.Map;

import org.apache.log4j.Logger;
import jp.co.nttdata.rate.log.LogFactory;

/**
 * 普通公式や判断式（if,elseIf,else）含む、およびwhileキーワードというループの計算を行う
 * <br>また、setキーワードで一時変数の新規や変更（設定後他の公式には利用可能）
 * @author btchoukug
 *
 */
public abstract class AbstractCalculator implements Calculable {
	
	private static Logger logger = LogFactory.getInstance(AbstractCalculator.class);
	
	/**計算コンテキスト*/
	protected ICalculateContext ctx;
	
	public AbstractCalculator() {
		;
	}
	
	@SuppressWarnings("unchecked")
	public void setRateKeys(Map rateKeys) {
		if (this.ctx == null) {
			throw new IllegalArgumentException("計算コンテキストは初期化していない。");
		}
		ctx.setInput(rateKeys);	
	}
	
	@Override
	public Double calculate(String formulaText) throws Exception {
		
		long t1 = System.currentTimeMillis();
		Double ret = this.ctx.getParser().parse(formulaText).eval().doubleValue();
		long t2 = System.currentTimeMillis();
		
		if (logger.isInfoEnabled()) {
			logger.info(">>>>>上記の計算は："+(t2-t1)+"ミリ秒かかる<<<<<\n");
		}
		return ret;
		
	}	
		
}
