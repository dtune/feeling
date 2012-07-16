package jp.co.nttdata.rate.log;

import java.io.File;
import jp.co.nttdata.rate.util.Const;
import jp.co.nttdata.rate.util.ResourceLoader;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.eclipse.swt.widgets.Text;
/**
 * LogFactory
 * @author btchoukug
 *
 */
public class LogFactory {

	private static Logger logger = null ;
	
	static {
		//外部プロパティファイルでlog4jを配置する
		PropertyConfigurator.configure(ResourceLoader.getExternalResource(Const.LOG4J_SETTING));
	}
	
	/**
	 * デフォルトの場合、Loggerのクラスでロガー対象を取得
	 * @param clazz
	 * @return Logger
	 */
	public static Logger getInstance() {
		return getInstance(Logger.class);
	}
	
	/**
	 * 指定のカテゴリのLoggerを取得
	 * @param cate
	 * @return
	 */
	public static Logger getInstance(String cate) {
		return Logger.getLogger(cate);
	}
	
	/**
	 * 指定のクラスよってログ記録対象を取得
	 * @param clazz
	 * @return Logger
	 */
	public static Logger getInstance(Class<? extends Object> clazz) {
		logger =  Logger.getLogger(clazz);
		return logger;
	}
		
	public static String getLogPath() {			
		return ResourceLoader.getProjectDir() + File.separator + Const.LOG_DIR;
	}
	
	/**
	 * log4jのログをテキストに再び向けるため、専用アペンダーをログルートに追加
	 * @param text_log
	 * @param display 
	 */
	public static void setTextAppender(Text text_log) {		
		TextUIAppender ta = getTextAppender();		
		ta.set_logText(text_log);
		//TODO TAのレイアウトはlog4j配置でstdoutと同じように初期化されるはず
		ta.setLayout(Logger.getRootLogger().getAppender("stdout").getLayout());		
	}
	

	/**
	 * カレントLog4jのルートに付いたappender等から
	 * LogTextに出力するTextAppenderを取得
	 * @return TextAppender
	 */
	public static TextUIAppender getTextAppender() {
		TextUIAppender ta = (TextUIAppender) Logger.getRootLogger().getAppender("TA");		
		return ta;
	}

	public static void setLoggerLevel(Level level) {
		Logger.getRootLogger().setLevel(level);
	}
	
}
