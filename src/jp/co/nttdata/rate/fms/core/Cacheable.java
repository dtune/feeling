package jp.co.nttdata.rate.fms.core;

import jp.co.nttdata.rate.model.formula.Formula;

/**
 * キャッシュを使えるため、キャッシュキーを予め編集すること
 * @author btchoukug
 *
 */
public interface Cacheable {
	
	/**キャッシュキーを取得*/
	public String getCacheKey();
	
	public Formula getFormula();

}
