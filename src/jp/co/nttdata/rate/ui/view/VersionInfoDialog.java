package jp.co.nttdata.rate.ui.view;

import java.awt.Desktop;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import jp.co.nttdata.rate.util.CommonUtil;
import jp.co.nttdata.rate.util.Const;
import jp.co.nttdata.rate.util.ResourceLoader;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Link;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;

/**
 * 検証ツールのバージョンを表すダイアログである
 * 
 * @author zhanghy
 * 
 */
public class VersionInfoDialog extends Dialog {

	private Shell dialog;
	private String versionNo;
	private String releaseDate;

	/** バージョン情報及び修正履歴を格納するファイル */
	final File versionInfoFile;

	public VersionInfoDialog(Shell parent) {
		super(parent);
		versionInfoFile = ResourceLoader
				.getExternalFile(Const.VERSION_INFO_TEXT);
		try {
			BufferedReader reader = new BufferedReader(new FileReader(
					versionInfoFile));
			reader.readLine();
			reader.readLine();
			versionNo = reader.readLine().split(Const.COLON)[1];
			releaseDate = reader.readLine().split(Const.COLON)[1];
		} catch (FileNotFoundException e) {
			throw new RuntimeException("バージョン情報及び修正履歴ファイル存在していない", e);
		} catch (IOException e) {
			throw new RuntimeException("バージョン情報及び修正履歴ファイル読み込むエラー", e);
		}
	}

	public void open() {

		dialog = new Shell(getParent(), SWT.DIALOG_TRIM | SWT.APPLICATION_MODAL);
		GridLayout layout = new GridLayout();
		layout.numColumns = 2;
		layout.marginWidth = 20;
		layout.horizontalSpacing = 20;
		dialog.setLayout(layout);

		Image imgVerInfo = new Image(null,
				ResourceLoader.getExternalResourceAsStream(Const.IMAGEICON));
		dialog.setImage(imgVerInfo);
		
		dialog.setText("バージョン情報について");

		Image imgVer = new Image(null,
				ResourceLoader.getExternalResourceAsStream(Const.IMAGEVER));
		Label lblImg = new Label(dialog, SWT.NONE);
		lblImg.setImage(imgVer);
		
		String sb = "(C)Copyright All Reserved NTT Data co.ltd"
			+ "\n" + "バージョン:" + versionNo + "\n"+ "リリース日付:" + releaseDate;
		
		Label lblCopyRight = new Label(dialog, SWT.NONE);
		lblCopyRight.setText(sb);

		Link link = new Link(dialog, SWT.NONE);
		link.setText("<a>修正履歴</a>");
		link.addListener(SWT.Selection, new Listener() {
			@Override
			public void handleEvent(Event event) {

				try {
					Desktop.getDesktop().open(versionInfoFile);
				} catch (IOException e1) {
					e1.printStackTrace();
				}
			}
		});

		GridData gd = new GridData(GridData.HORIZONTAL_ALIGN_END);
		gd.widthHint = 60;
		gd.heightHint = 20;

		Button cancelBtn = new Button(dialog, SWT.NONE);
		cancelBtn.setText("OK");
		cancelBtn.setLayoutData(gd);
		cancelBtn.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				dialog.dispose();
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent selectionevent) {
			}
		});

		dialog.pack();
		CommonUtil.setShellLocation(dialog);
		dialog.open();
	}

}
