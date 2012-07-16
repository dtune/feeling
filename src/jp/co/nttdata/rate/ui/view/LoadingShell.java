package jp.co.nttdata.rate.ui.view;

import jp.co.nttdata.rate.exception.FmsDefErrorException;
import jp.co.nttdata.rate.log.LogFactory;
import jp.co.nttdata.rate.model.CategoryManager;
import jp.co.nttdata.rate.model.RateCalculateSupport;
import jp.co.nttdata.rate.model.rateKey.RateKeyManager;
import jp.co.nttdata.rate.util.CommonUtil;
import jp.co.nttdata.rate.util.Const;
import jp.co.nttdata.rate.util.ResourceLoader;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

/**
 * 計算に関するデータと公式、レートキーの定義をロードするのはかなり時間かかります。
 * ユーザビリティ向上するため、当該ロード中画面を用意する
 * @author btchoukug
 *
 */
public class LoadingShell {
	
	private static final String SHELL_TITLE = "数理レートツールロード中";

	private Shell shell;
	private Text loadingText;
	
	//private ProgressBar loadingBar;	
	public LoadingShell() {
		
		shell = new Shell(SWT.TITLE | SWT.PRIMARY_MODAL);
		GridLayout layout = new GridLayout();
		layout.marginTop = 6;
		layout.verticalSpacing = 6;
		shell.setLayout(layout);
		shell.setSize(360, 120);
		shell.setText(SHELL_TITLE);
		Image img = new Image(null, ResourceLoader.getExternalResourceAsStream(Const.IMAGEICON));
		shell.setImage(img);
		
		// 位置を再計算
		CommonUtil.setShellLocation(shell);

		// ロード情報を表す
		loadingText = new Text(shell, SWT.WRAP | SWT.V_SCROLL | SWT.MULTI | SWT.NONE);
		GridData dataText = new GridData(GridData.FILL_HORIZONTAL);
		dataText.heightHint = 80;
		loadingText.setLayoutData(dataText);
		loadingText.setEditable(false);
		loadingText.setText("検証ツール初期化：計算基数データと公式定義をロード中\n");
		
//		loadingBar = new ProgressBar(shell, SWT.INDETERMINATE);
//		GridData dataBar = new GridData(GridData.FILL_HORIZONTAL);
//		loadingBar.setLayoutData(dataBar);
		shell.open();
				
	}
	
	public RateCalculateSupport load(final String code) {
		
		LogFactory.setTextAppender(loadingText);
		loadingText.setFocus();
		
		// 計算モジュール初期化
		RateCalculateSupport rcs = null;
		try {
			rcs = new RateCalculateSupport(code, true);
		} catch (FmsDefErrorException e) {
			MessageBox msgBox = new MessageBox(shell, SWT.ICON_ERROR | SWT.OK);
			msgBox.setText("システムエラー");
			msgBox.setMessage(e.getMessage());
			msgBox.open();
		}
		shell.dispose();

		return rcs;
	}
	
	/**
	 * Batchロード
	 * @param code
	 */
	public void loadBatchContext(final String code) {

		RateKeyManager.newInstance(code);
		CategoryManager.newInstance(code);		
		
		shell.dispose();
	}

}
