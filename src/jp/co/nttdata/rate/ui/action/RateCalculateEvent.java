package jp.co.nttdata.rate.ui.action;

import java.util.EventObject;

/**
 * レート計算用イベント
 * @author btchoukug
 *
 */
public class RateCalculateEvent extends EventObject {

	private static final long serialVersionUID = 12L;
	
	private RateCalculateEventType eventType;

	public RateCalculateEvent(Object arg0) {
		super(arg0);
	}
	
	public RateCalculateEvent(Object arg0, RateCalculateEventType eventType) {
		super(arg0);
		this.eventType = eventType;
	}
	
	public RateCalculateEventType getEventType() {
		return this.eventType;
	}

}
