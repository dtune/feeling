package jp.co.nttdata.rate.ui.action;

/**
 * レート計算用イベントタイプ
 * @author btchoukug
 *
 */
public enum RateCalculateEventType {
	
	P_RATE,V_RATE,W_RATE,
	BT_CALCULATE,
	CHANGE_TAB,CHANGE_COMBO,
	CLEAR_INPUT,CLEAR_LOG,
	OPEN_LOG_FILE,
	CREATE_FILE,
	APL_ERROR,
	SYS_ERROR, BT_END;
}
