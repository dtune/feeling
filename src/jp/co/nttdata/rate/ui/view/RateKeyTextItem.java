package jp.co.nttdata.rate.ui.view;

import jp.co.nttdata.rate.model.rateKey.RateKey;
import jp.co.nttdata.rate.model.rateKey.TextKey;
import jp.co.nttdata.rate.util.Const;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;
/**
 * レートキーより入力項目：テキスト
 * @author btchoukug
 *
 */
public class RateKeyTextItem extends RateKeyInputItem {
	
	private int initValue = 0;
	private Text text;
	
	public RateKeyTextItem(Composite parent, RateKey key) {
		super(parent, key);
		//タイプごとの専有UI情報を読み込む
		readUIInfo(rateKey);
		_createTextItem();
	}
		
	private void _createTextItem() {
		
		//ラベルとテキスト（コンボ、ラジオなど入力項目）のグループ
		Composite inputItemGroup = new Composite(this, SWT.NONE);
		inputItemGroup.setLayout(new RowLayout(SWT.HORIZONTAL));
				
		//レートキーのタイプによって、Text、Combo、RadioButtonを作成
		text = new Text(inputItemGroup, SWT.BORDER);
		
		text.setLayoutData(getNextInputRowData());
		text.setText(String.valueOf(initValue));
		
		//入力して、RateInputのインスタンスに変換するため、マッピングの名称をセットする
		text.setData(Const.NAME, keyName);
	}
	
	public void setText(String text) {
		this.text.setText(text);
	}
	
	@Override
	protected void readUIInfo(RateKey key) {
		initValue = ((TextKey)key).getInitValue();
	}

	@Override
	public boolean setFocus() {
		return this.text.setFocus();
	}

	@Override
	public void setEnable(boolean arg0) {
		this.text.setEnabled(arg0);
	}

	@Override
	public String getValue() {
		return this.text.getText();
	}

//	@Override
//	public void setErrorStyle() {
//		this.label.setCapture(true);
//		this.label.setForeground(getErrorStyle());
//		this.text.setBackground(this.getErrorStyle());
//		final Color normalStyle = super.getNormalStyle();
//		//3秒あと、元の状態に回復
//		this.getDisplay().asyncExec(new Runnable() {
//			
//			@Override
//			public void run() {
//				try {
//					Thread.sleep(3000);
//				} catch (InterruptedException e) {
//					// TODO Auto-generated catch block
//					e.printStackTrace();
//				}
//				label.setForeground(normalStyle);
//				text.setBackground(getNormalStyle());
//			}
//		});
//	}
//	
//	@Override
//	public Color getNormalStyle() {
//		return this.getDisplay().getSystemColor(SWT.COLOR_WHITE);
//	}
	
	@Override
	public void addKeyListener(KeyListener arg) {
		this.text.addKeyListener(arg);
	}

	@Override
	public void setValue(Object value) {
		if (value == null) return;
		this.text.setText(String.valueOf(value));		
	}
		
}
