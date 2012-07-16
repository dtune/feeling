package jp.co.nttdata.rate.ui.view;

import jp.co.nttdata.rate.model.rateKey.RateKey;
import jp.co.nttdata.rate.util.Const;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.RowData;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Composite;

/**
 * レートキーより入力項目グループ（ラベルとテキストなど）：レートキーの入力グループCompositeおよびラベルを生成する
 * @author btchoukug
 *
 */
public abstract class RateKeyInputItem extends Composite {
	
	/**五字以上の場合、省略で表示する*/
	//private static final int DISPLAY_WIDTH = 7;
	
	/** ラベルのサイズ(px) */
	protected static final int LABEL_HEIGHT = 20;
	protected static final int LABEL_WIDTH = 120;
	/** 入力欄のサイズ(px) */
	private static final int INPUT_HEIGHT = 20;
	private static final int INPUT_WIDTH = 60;
		
	protected RateKey rateKey;
	protected CLabel label;
	protected RowData rd_label;
	
	//レートキー名
	protected String keyName = null;	
	//ラベル名
	protected String labelText = null;
	//必須項目かどうか
	protected boolean isRequired = true;
	
	/** 入力項目（テキスト、コンボボックス、ラジオの第一番目）をフォーカスする */
	public abstract boolean setFocus();
	
	/** 入力項目（テキスト、コンボボックス、ラジオ）を有効・無効する */
	public abstract void setEnable(boolean arg0);
	
	/** 入力項目の値（テキスト、コンボボックスの選択インデックス、ラジオの選択インデックス）取得する */
	public abstract String getValue();
	
	/** 指定の値どおりに（テキスト、コンボボックスの選択インデックス、ラジオの選択インデックス）を設定する */
	public abstract void setValue(Object value);
		
	/**
	 * テキスト、コンボボックス、ラジオボタン専用UI情報を読み込む
	 * @param key
	 */
	protected abstract void readUIInfo(RateKey key);

	public RateKeyInputItem(Composite parent, RateKey rateKey) {
		//ラベルとテキスト（コンボ、ラジオなど入力項目）のグループ
		super(parent, SWT.NONE);
		this.setLayout(new RowLayout(SWT.HORIZONTAL));
		
		this.rateKey = rateKey;
		
		//レートキーより、UI情報を取得
		readFieldBasicInfo(rateKey);
		//まず、ラベルを作成
		createLabel();
	}
	
	public String getKeyName() {
		return keyName;
	}

	public String getLabelText() {
		return labelText;
	}

	public boolean isRequired() {
		return isRequired;
	}
	
	/**
	 * ラベルおよびUIの基本情報を読み込む
	 * @param key
	 */
	private void readFieldBasicInfo(RateKey key) {
		keyName = key.getName();
    	labelText = key.getLabel();
    	isRequired = key.isRequired();
	}
	
	private void createLabel() {
		
		label = new CLabel(this, SWT.NONE | SWT.LEFT);
		
		labelText = (labelText == null) ? keyName : labelText;
		//必須であれば、*を付ける
		if (isRequired) {
			labelText = labelText + Const.REQUIRED;
		}
		
		//Automatically shorten text
		label.setText(labelText);
		//label.setText(StringUtils.abbreviate(labelText, DISPLAY_WIDTH));
		label.setToolTipText(labelText + rateKey.getName());
		
		rd_label = new RowData();
		rd_label.width = LABEL_WIDTH;
		rd_label.height = LABEL_HEIGHT;
		label.setLayoutData(rd_label);

	}
	
	/**
	 * 次の入力項目のレイアウトデータを取得
	 * @return
	 */
	protected RowData getNextInputRowData() {
		//RowLayoutのため、詳細な位置の算出が必要ない
		RowData rd_input = new RowData();
		rd_input.width = INPUT_WIDTH;
		rd_input.height = INPUT_HEIGHT;
		
		return rd_input;
	}
	
	public RateKey getRateKey() {
		return this.rateKey;
	}
	
	/**
	 * 入力チェックエラーの場合、入力のレートキーの背景色を赤色にする
	 */
	public void setErrorStyle() {
		this.label.setCapture(true);
		this.label.setForeground(getErrorStyle());
		//3秒あと、元の状態に回復
		this.getDisplay().asyncExec(new Runnable() {
			
			@Override
			public void run() {
				try {
					Thread.sleep(3000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				label.setForeground(getNormalStyle());
			}
		});
	}
	
	public Color getErrorStyle() {
		return this.getDisplay().getSystemColor(SWT.COLOR_RED);
	}
	
	public Color getNormalStyle() {
		return this.getDisplay().getSystemColor(SWT.COLOR_BLACK);
	}
}
