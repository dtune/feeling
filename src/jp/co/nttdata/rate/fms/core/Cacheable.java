package jp.co.nttdata.rate.fms.core;

import jp.co.nttdata.rate.model.formula.Formula;

/**
 * �L���b�V�����g���邽�߁A�L���b�V���L�[��\�ߕҏW���邱��
 * @author btchoukug
 *
 */
public interface Cacheable {
	
	/**�L���b�V���L�[���擾*/
	public String getCacheKey();
	
	public Formula getFormula();

}
