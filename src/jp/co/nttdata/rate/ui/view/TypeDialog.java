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
 * ����̏ꍇ�A���̃_�C�A���O���J���āA���i��I�����惁�C����ʂ��J��
 * <br>�����ď��i��OL��ʂ�����������
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
	 * �_�C�A���O���J���āA�I�����ꂽ���i��ނ�Ԃ�
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
	    dialog.setText("�������[�g�c�[��");
	    Image img = new Image(null, ResourceLoader.getExternalResourceAsStream(Const.IMAGEICON));
	    dialog.setImage(img);
	    
	    Label label = new Label(dialog, SWT.NONE);
	    GridData gdLabel = new GridData();
	    gdLabel.horizontalSpan = 10;
		gdLabel.widthHint = 300;
	    gdLabel.heightHint = 30;
	    label.setLayoutData(gdLabel);
	    label.setText("��_��܂��͓����I�����Ă��������B");
	    
	    //��_��Ɠ���{�^��
	    GridData gdButton = new GridData(GridData.HORIZONTAL_ALIGN_CENTER);
	    gdButton.heightHint = 60;
	    gdButton.widthHint = 120;
	    
	    Button mainBtn = new Button(dialog, SWT.NONE);
	    mainBtn.setText("��_��");
	    //dialog.setDefaultButton(okBtn);
	    mainBtn.setLayoutData(gdButton);
		mainBtn.addSelectionListener(new SelectionAdapter() {

			public void widgetSelected(SelectionEvent e) {
				selectedType = "0";
				dialog.dispose();
			}

		});
	    
	    Button specialBtn = new Button(dialog, SWT.NONE);
	    specialBtn.setText("����");
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