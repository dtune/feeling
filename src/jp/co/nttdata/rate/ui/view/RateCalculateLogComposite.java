package jp.co.nttdata.rate.ui.view;

import java.io.IOException;

import jp.co.nttdata.rate.log.LogFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.layout.RowData;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

/**
 * リアルログのコンポジット
 * @author btchoukug
 *
 */
public class RateCalculateLogComposite extends Composite {

	Label label_head;
	Text text_log;
	
	Composite comp_btn;
	Button btn_open;
	Button btn_clear;
	
	Composite comp_logPath;
	Label label_logHead;
	Text text_logPath;
	
	private String logFilePath;
	
	public RateCalculateLogComposite(Composite composite) {
		super(composite, SWT.NONE);
		this.setLayout(new RowLayout(SWT.VERTICAL));
		
		_createLogComposite();

		//ログファイルパスを取得
		this.logFilePath = LogFactory.getLogPath();
		this.setLogPath(this.logFilePath);
		
		//テキストエリアにログを出力するため、log4jのappenderをここに追加する
		LogFactory.setTextAppender(text_log);
		
	}
	
	@Override
	public void setSize(int width, int height){
		
		final RowData new_rd_label = new RowData();
		new_rd_label.width = width;
		label_head.setLayoutData(new_rd_label);
		
		final RowData new_rd_text_log = new RowData();
		new_rd_text_log.height = height;
		new_rd_text_log.width = width;
		text_log.setLayoutData(new_rd_text_log);
		
		super.setSize(width, height);
	}
	
	private void _createLogComposite() {
		
		//リアルのログテキストエリア
		label_head = new Label(this, SWT.NONE);
		final RowData rd_label = new RowData();
		rd_label.width = 365;
		label_head.setLayoutData(rd_label);
		label_head.setText("計算ログおよび途中値は下記のテキストボックスに表示されます。" +
				"\n同時に外部ログファイルにも書き込みます。");

		text_log = new Text(this, SWT.WRAP | SWT.V_SCROLL | SWT.MULTI | SWT.BORDER);
		final RowData rd_text_log = new RowData();
		rd_text_log.height = 450;
		rd_text_log.width = 650;
		text_log.setLayoutData(rd_text_log);
		text_log.addKeyListener(new KeyAdapter() {
	    	//すべて選択
	    	public void keyPressed(KeyEvent e) {
	    		if ((e.keyCode == 'A' || e.keyCode == 'a')
	    				&& (e.stateMask & SWT.MOD1) != 0) {
	    			text_log.selectAll();
	    			e.doit = false;
	    		}
	    	}
	    });

		//ボタンのエリア
		comp_btn = new Composite(this, SWT.NONE);
		final RowData rd_composite_1 = new RowData();
		rd_composite_1.height = 32;
		rd_composite_1.width = 600;
		comp_btn.setLayoutData(rd_composite_1);
		final RowLayout rowLayout1 = new RowLayout(SWT.HORIZONTAL);
		comp_btn.setLayout(rowLayout1);

		btn_clear = new Button(comp_btn, SWT.NONE);
		btn_clear.setText("上記ログをクリア");
		btn_clear.addMouseListener(new MouseAdapter(){
			public void mouseUp(MouseEvent arg0) {
				//TextAppenderのStringBufferをクリアした上、テキストもクリアする
				LogFactory.getTextAppender().clear();
			}
		});

		btn_open = new Button(comp_btn, SWT.NONE);
		btn_open.setText("ログフォルダを開く");
		btn_open.addMouseListener(new MouseListener() {
			
			@Override
			public void mouseUp(MouseEvent arg0) {
				try {
					Runtime.getRuntime().exec("explorer \"" + getLogPath() + "\"");
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			
			@Override
			public void mouseDown(MouseEvent arg0) {
				;
			}
			
			@Override
			public void mouseDoubleClick(MouseEvent arg0) {
				;
			}
		});

		//ログパスのエリア
		comp_logPath = new Composite(this, SWT.NONE);
		final RowData rd_composite_2 = new RowData();
		rd_composite_2.height = 32;
		rd_composite_2.width = 600;
		comp_logPath.setLayoutData(rd_composite_2);
		final RowLayout rowLayout2 = new RowLayout(SWT.HORIZONTAL);
		comp_logPath.setLayout(rowLayout2);

		label_logHead = new Label(comp_logPath, SWT.NONE);
		label_logHead.setText("ログファイルのパス：");

		text_logPath = new Text(comp_logPath, SWT.BORDER);
		final RowData rd_logPath = new RowData();
		rd_logPath.width = 500;
		text_logPath.setLayoutData(rd_logPath);
		text_logPath.setEditable(false);
		
	}
	
	public String getLogPath() {
		return text_logPath.getText();
	}
	
	public void setLogPath(String logPath) {
		//絶対パスを取得して表示する
		text_logPath.setText(logPath);
	}

	public void setText(String content) {
		text_log.setText(content);
	}

	public void appendText(String content) {
		text_log.append(content);		
	}


}
