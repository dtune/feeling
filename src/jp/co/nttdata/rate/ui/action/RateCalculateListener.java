/**
 * 
 */
package jp.co.nttdata.rate.ui.action;

import java.util.EventListener;

/**
 * UI�������ŃC�x���g���[�X�i�[
 * @author btchoukug
 *
 */
public interface RateCalculateListener extends EventListener {
	void update(RateCalculateEvent event);
}
