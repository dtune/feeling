package jp.co.nttdata.rate.ui.action;

import java.util.Enumeration;
import java.util.Vector;
import jp.co.nttdata.rate.log.LogFactory;
import org.apache.log4j.Logger;

/**
 * Observer���[�h�ŃC�x���g�������s��
 * @author btchoukug
 *
 */
public class RateCalculateObserver {
	
	private Logger logger = LogFactory.getInstance(getClass());
	
	private static RateCalculateObserver observer;
	private Vector<RateCalculateListener> repository = new Vector<RateCalculateListener>();
    
	private RateCalculateObserver(){
		;
	}
	
	/**
	 * Factory���[�h�ŃC���X�^���X���擾
	 * @return
	 */
	public static RateCalculateObserver getInstance(){
		if(observer== null){
			observer = new RateCalculateObserver();
		}
		return observer;
	}
	
	/**
	 * synchronized�ŃC�x���g���X�i�[��ǉ�
	 * @param listener
	 */
	public synchronized void addRateCalculateListener(RateCalculateListener listener) {
		repository.addElement(listener);
		logger.debug("addRateCalculateListener: " + listener.getClass().getSimpleName());
	}

	/**
	 * synchronized�ŃC�x���g���X�i�[���O��
	 * @param listener
	 */
	public synchronized void removeRateCalculateListener(RateCalculateListener listener) {
		repository.removeElement(listener);
		logger.debug("removeRateCalculateListener: " + listener.getClass().getSimpleName());
	}

	@SuppressWarnings("unchecked")
	public void notifyEvents(RateCalculateEvent rateEvent) {
		Vector<RateCalculateListener> tempVector = null;

		synchronized (this) {
			tempVector = (Vector<RateCalculateListener>) repository.clone();
			Enumeration e = tempVector.elements();
	
			int size = tempVector.size();
		     while(e.hasMoreElements())
		     {
		    	 RateCalculateListener listener = (RateCalculateListener)e.nextElement();
		    	 logger.debug("RateCalculateObserver.notifyEvents "+ (size--)+ " = " + listener);
		    	 listener.update(rateEvent);
		    	 
		     }
		}

	}
}
