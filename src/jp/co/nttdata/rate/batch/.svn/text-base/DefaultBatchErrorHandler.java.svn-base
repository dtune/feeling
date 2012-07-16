package jp.co.nttdata.rate.batch;

import java.io.PrintStream;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

import jp.co.nttdata.rate.exception.RateException;
import jp.co.nttdata.rate.log.LogFactory;

public class DefaultBatchErrorHandler implements IBatchErrorHandler {

	private static Logger logger = LogFactory.getInstance(DefaultBatchErrorHandler.class);
	
	Shell shell;
	
	Batch bt;
	
	/**エラー出力先ストリーム*/
	PrintStream err;

	public DefaultBatchErrorHandler(Batch bt) {
		shell = new Shell(Display.getCurrent().getActiveShell());
		this.err = System.out;
		this.bt = bt;
	}
	
	public DefaultBatchErrorHandler(Shell parent, Batch bt) {
		shell = new Shell(parent);
		this.err = System.err;
		this.bt = bt;
	}
	
	@Override
	public void handleError(Object error) {
		
		Logger.getRootLogger().setLevel(Level.INFO);
		
		if (error instanceof Exception) {
			//業務エラーの場合
			if (error instanceof RateException) {
				
//				MessageBox msgBox = new MessageBox(shell, SWT.ICON_ERROR | SWT.OK);
//				msgBox.setText("業務エラー");
				String msg = ((RateException) error).getErrorMessage();
//				msgBox.setMessage(msg);
//				msgBox.open();
				logger.error("業務エラーが発生しました：" + msg);
				
			} else {
			
				//err.println("BT計算はエラーが発生しました。");
				//((Exception) error).printStackTrace(err);
				logger.error("BT計算はエラーが発生しました。", (Throwable) error);
				
			}
			
			//TODO Batch　stop
			bt.stop();
			try {
				bt.shutdown();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} else {
			System.out.println(error);
		}
		
	}

}
