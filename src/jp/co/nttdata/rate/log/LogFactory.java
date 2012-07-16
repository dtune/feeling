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
		//�O���v���p�e�B�t�@�C����log4j��z�u����
		PropertyConfigurator.configure(ResourceLoader.getExternalResource(Const.LOG4J_SETTING));
	}
	
	/**
	 * �f�t�H���g�̏ꍇ�ALogger�̃N���X�Ń��K�[�Ώۂ��擾
	 * @param clazz
	 * @return Logger
	 */
	public static Logger getInstance() {
		return getInstance(Logger.class);
	}
	
	/**
	 * �w��̃J�e�S����Logger���擾
	 * @param cate
	 * @return
	 */
	public static Logger getInstance(String cate) {
		return Logger.getLogger(cate);
	}
	
	/**
	 * �w��̃N���X����ă��O�L�^�Ώۂ��擾
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
	 * log4j�̃��O���e�L�X�g�ɍĂь����邽�߁A��p�A�y���_�[�����O���[�g�ɒǉ�
	 * @param text_log
	 * @param display 
	 */
	public static void setTextAppender(Text text_log) {		
		TextUIAppender ta = getTextAppender();		
		ta.set_logText(text_log);
		//TODO TA�̃��C�A�E�g��log4j�z�u��stdout�Ɠ����悤�ɏ����������͂�
		ta.setLayout(Logger.getRootLogger().getAppender("stdout").getLayout());		
	}
	

	/**
	 * �J�����gLog4j�̃��[�g�ɕt����appender������
	 * LogText�ɏo�͂���TextAppender���擾
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
