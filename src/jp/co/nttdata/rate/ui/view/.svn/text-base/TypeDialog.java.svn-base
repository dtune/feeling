package jp.co.nttdata.rate.ui.view;

import jp.co.nttdata.rate.util.CommonUtil;
import jp.co.nttdata.rate.util.Const;
import jp.co.nttdata.rate.util.ResourceLoader;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;

/**
 * 初回の場合、このダイアログを開いて、商品を選択次第メイン画面を開く
 * <br>応じて商品のOL画面を初期化する
 * @author 
 *
 */
public class TypeDialog extends Dialog{
		
	private String selectedType = null;
	
	private Shell dialog;
	
	public TypeDialog(Shell parent) {
		super(parent);
	}
	
	public TypeDialog() {
		super(new Shell());
	}
			
	/**
	 * ダイアログを開いて、選択された商品種類を返す
	 * @return
	 */
	public String open() {
		
	    Shell parent = getParent();
	    dialog = new Shell(parent, SWT.DIALOG_TRIM | SWT.APPLICATION_MODAL);
	    GridLayout gridLayout = new GridLayout(2, true);
	    gridLayout.marginBottom = 20;
	    dialog.setLayout(gridLayout);
	    dialog.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false,
		        true, 1, 1));
	    dialog.setText("数理レートツール");
	    Image img = new Image(null, ResourceLoader.getExternalResourceAsStream(Const.IMAGEICON));
	    dialog.setImage(img);
	    
	    Label label = new Label(dialog, SWT.NONE);
	    GridData gdLabel = new GridData();
	    gdLabel.horizontalSpan = 10;
		gdLabel.widthHint = 300;
	    gdLabel.heightHint = 30;
	    label.setLayoutData(gdLabel);
	    label.setText("主契約または特約を選択してください。");
	    
	    //主契約と特約ボタン
	    GridData gdButton = new GridData(GridData.HORIZONTAL_ALIGN_CENTER);
	    gdButton.heightHint = 60;
	    gdButton.widthHint = 120;
	    
	    Button mainBtn = new Button(dialog, SWT.NONE);
	    mainBtn.setText("主契約");
	    //dialog.setDefaultButton(okBtn);
	    mainBtn.setLayoutData(gdButton);
		mainBtn.addSelectionListener(new SelectionAdapter() {

			public void widgetSelected(SelectionEvent e) {
				selectedType = "0";
				dialog.dispose();
			}

		});
	    
	    Button specialBtn = new Button(dialog, SWT.NONE);
	    specialBtn.setText("特約");
	    specialBtn.setLayoutData(gdButton);
	    specialBtn.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				selectedType = "1";
				dialog.dispose();
			}
		});

	    dialog.pack();
	    CommonUtil.setShellLocation(dialog);
	    dialog.open();
	    
	    while (!dialog.isDisposed()) {
	      if (!dialog.getDisplay().readAndDispatch())
	        dialog.getDisplay().sleep();
	    }

	    return selectedType;
	}

		
}