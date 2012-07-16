package jp.co.nttdata.rate.log;

import org.apache.log4j.AppenderSkeleton;
import org.apache.log4j.spi.LoggingEvent;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Text;

/**
 * log4j�̃��O�o�͂��e�L�X�g�G���A�ɍĂь�����
 * @author btchoukug
 *
 */
public class TextUIAppender extends AppenderSkeleton {
	
	protected static final int MAX_LOG_LINECOUNT = 1000;
	//log4j�̃��O�̏o�͐�
	private Text _logText;

	/**
	 * log4j�̃��O�o�͂��e�L�X�g�G���A�ɍĂь�����
	 */
	public TextUIAppender() {
		super();
	}
	
	/**
	 * @param isActive
	 */
	public TextUIAppender(boolean isActive) {
		super(isActive);
	}
	
	public void set_logText(Text logText) {
		_logText = logText;
	}

	/* (non-Javadoc)
	 * @see org.apache.log4j.AppenderSkeleton#append(org.apache.log4j.spi.LoggingEvent)
	 */
	@Override
	protected void append(LoggingEvent arg0) {
		
		//�ŏ��̏ꍇ�A�t�h�͂܂������グ�Ă��Ȃ�		
		if (_logText != null) {
			final String msg = super.getLayout().format(arg0);  
			
			if(Display.getCurrent() != null) {				
				Display.getCurrent().asyncExec(new Runnable() { 
					public void run() { 
						if (_logText != null && !_logText.isDisposed()) {
							//�ő�\���s���ɂȂ�ƁA�N���A����
							if (_logText.getLineCount() > MAX_LOG_LINECOUNT)
								_logText.setText("");

							_logText.append(msg);
						}
				    } 
				});	
			   }
			}
	}
	
	/* (non-Javadoc)
	 * @see org.apache.log4j.Appender#close()
	 */
	public void close() {
		if (this._logText != null) {
			this._logText.setText("");	
		}
	}

	/* (non-Javadoc)
	 * @see org.apache.log4j.Appender#requiresLayout()
	 */
	public boolean requiresLayout() {
		return false;
	}
	
	
	public void clear() {
		if (this._logText != null) {
			this._logText.setText("");	
		}
	}
	
}
