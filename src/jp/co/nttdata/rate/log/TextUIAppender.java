package jp.co.nttdata.rate.log;

import org.apache.log4j.AppenderSkeleton;
import org.apache.log4j.spi.LoggingEvent;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Text;

/**
 * log4jのログ出力をテキストエリアに再び向ける
 * @author btchoukug
 *
 */
public class TextUIAppender extends AppenderSkeleton {
	
	protected static final int MAX_LOG_LINECOUNT = 1000;
	//log4jのログの出力先
	private Text _logText;

	/**
	 * log4jのログ出力をテキストエリアに再び向ける
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
		
		//最初の場合、ＵＩはまだ立ち上げていない		
		if (_logText != null) {
			final String msg = super.getLayout().format(arg0);  
			
			if(Display.getCurrent() != null) {				
				Display.getCurrent().asyncExec(new Runnable() { 
					public void run() { 
						if (_logText != null && !_logText.isDisposed()) {
							//最大表示行数になると、クリアする
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
