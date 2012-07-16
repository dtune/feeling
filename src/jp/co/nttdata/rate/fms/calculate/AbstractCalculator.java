package jp.co.nttdata.rate.fms.calculate;

import java.util.Map;

import org.apache.log4j.Logger;
import jp.co.nttdata.rate.log.LogFactory;

/**
 * ���ʌ����┻�f���iif,elseIf,else�j�܂ށA�����while�L�[���[�h�Ƃ������[�v�̌v�Z���s��
 * <br>�܂��Aset�L�[���[�h�ňꎞ�ϐ��̐V�K��ύX�i�ݒ�㑼�̌����ɂ͗��p�\�j
 * @author btchoukug
 *
 */
public abstract class AbstractCalculator implements Calculable {
	
	private static Logger logger = LogFactory.getInstance(AbstractCalculator.class);
	
	/**�v�Z�R���e�L�X�g*/
	protected ICalculateContext ctx;
	
	public AbstractCalculator() {
		;
	}
	
	@SuppressWarnings("unchecked")
	public void setRateKeys(Map rateKeys) {
		if (this.ctx == null) {
			throw new IllegalArgumentException("�v�Z�R���e�L�X�g�͏��������Ă��Ȃ��B");
		}
		ctx.setInput(rateKeys);	
	}
	
	@Override
	public Double calculate(String formulaText) throws Exception {
		
		long t1 = System.currentTimeMillis();
		Double ret = this.ctx.getParser().parse(formulaText).eval().doubleValue();
		long t2 = System.currentTimeMillis();
		
		if (logger.isInfoEnabled()) {
			logger.info(">>>>>��L�̌v�Z�́F"+(t2-t1)+"�~���b������<<<<<\n");
		}
		return ret;
		
	}	
		
}
