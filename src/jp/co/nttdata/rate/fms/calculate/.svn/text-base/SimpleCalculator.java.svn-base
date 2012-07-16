package jp.co.nttdata.rate.fms.calculate;

import java.util.HashMap;
import java.util.Map;
import org.apache.log4j.Level;
import jp.co.nttdata.rate.log.LogFactory;

/**
 * レート基数なし、公式定義なし、レートキー定義なし
 * シンプルな計算モジュールである
 * <br>普通の四則演算ができる
 * @author btchoukug
 *
 */
public class SimpleCalculator extends AbstractCalculator {

	public SimpleCalculator() {
		super();
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public void setRateKeys(Map rateKeys) {
		this.ctx = new DefaultCalculateContext();
		this.ctx.setInput(rateKeys);	
	}
	
	@SuppressWarnings("unchecked")
	public static void main(String[] args) {
		SimpleCalculator sc = new SimpleCalculator();
		Map paras = new HashMap();
		paras.put("x", 11);
		paras.put("t", 1);
		paras.put("n", 9);
		paras.put("m", 6);
		sc.setRateKeys(paras);
		
		LogFactory.setLoggerLevel(Level.OFF);
		
		String fomulaText ="1000+100.0*99-(600-3*15)%(((68-9)-3)*2-100)+10000%7*71";	
		
		//String fomulaText ="0.003*min(round(n/10,3),1)*max((min(m,10)-t)/min(m,10),0)";
		//String f1 ="0.0003*max(1-t/min(m,10),0)*0.9";
		//f1 = "0.0025*0.9";
		
		try {
			Double ret = sc.calculate(fomulaText);
			//Double ret = SystemFunctionUtility.round(sc.calculate(fomulaText), 4);
			System.out.println(fomulaText+ "=" +ret);

			long t1 = System.nanoTime();
			
			int i = 1000*10000;
			while (i-->0) {
				Double ret1 = sc.calculate(fomulaText);
				//System.out.println(fomulaText+ "=" +ret1);	
			}
			
			long t2 = System.nanoTime();
			System.out.println("execute(ms)：" + (t2 - t1)/1000000);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}		

	}

}
