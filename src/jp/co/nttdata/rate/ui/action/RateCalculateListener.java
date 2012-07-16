/**
 * 
 */
package jp.co.nttdata.rate.ui.action;

import java.util.EventListener;

/**
 * UI初期化でイベントレースナー
 * @author btchoukug
 *
 */
public interface RateCalculateListener extends EventListener {
	void update(RateCalculateEvent event);
}
