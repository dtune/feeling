package jp.co.nttdata.rate.ui.view;

import java.util.ArrayList;
import java.util.List;
import jp.co.nttdata.rate.model.rateKey.Item;
import jp.co.nttdata.rate.model.rateKey.RateKey;
import jp.co.nttdata.rate.model.rateKey.SelectableKey;
import jp.co.nttdata.rate.util.Const;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.RowData;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;

/**
 * レートキーより入力項目：ラジオボタングループ
 * @author btchoukug
 *
 */
public class RateKeyRadioItem extends RateKeyInputItem {

	//コンボボックスあるいはラジオボタンの場合、選択肢Itemのリスト(labelとvalueを含む)
	private List<Item> items;
	//コンボボックスあるいはラジオボタンの場合、選択インデックス
	private int selectedIndex = 0;
	
	/** コンボボックスあるいはラジオボタンの場合、選択肢の配列 */
	private String[] itemKeys;
	private String[] itemNames;
	
	private Button[] radioBtns; 
	
	public RateKeyRadioItem(Composite parent, RateKey rateKey) {
		super(parent, rateKey);
		//タイプごとの専有UI情報を読み込む
		readUIInfo(rateKey);
		_createRadioItem();
	}
		
	private void _createRadioItem() {
						
		RowData rd_input = getNextInputRowData();
		
		//ラジオボタングループ
		Composite radioGroup = new Composite(this, SWT.NONE);
		radioGroup.setLayout(new RowLayout(SWT.HORIZONTAL));
		
		//選択肢情報より、ラジオアイテムを作成
		getItemsKeyAndName(items);
		
		radioBtns = new Button[this.itemKeys.length];
		for (int i = 0; i < this.itemKeys.length; i++) {
			
			Button radBtn = new Button(radioGroup, SWT.RADIO);
			radBtn.setText(this.itemNames[i]);
			if (i == selectedIndex) {
				radBtn.setSelection(true);
			}
			radBtn.setData(Const.NAME, keyName);
			radBtn.setData(Const.KEY, this.itemKeys[i]);
			
			radioBtns[i] = radBtn;
		}
		
		//グループのレイアウトを再設定
		rd_input.width = (int) (LABEL_WIDTH * 1.5);
		
		//入力して、RateInputのインスタンスに変換するため、マッピングの名称をセットする
		radioGroup.setData(Const.NAME, keyName);

	}

	@SuppressWarnings("unchecked")
	@Override
	protected void readUIInfo(RateKey key) {
		SelectableKey radioKey = (SelectableKey)key;
    	items = radioKey.getItems();
    	selectedIndex = radioKey.getSelectedIndex();	
	}
	
	/**
	 * コンボボックスまたはラジオボタンに指定した選択肢からキーまたは表示名を取得（例："1:男性,2:女性"）
	 * <br>何も取得できなかった場合、サイズが０の配列を返す
	 * @param keyItems
	 * 
	 */
	protected void getItemsKeyAndName(List<Item> items) {
		List<String> itemKeysList = new ArrayList<String>();
		List<String> itemNamesList = new ArrayList<String>();
		
		for (Item item : items) {
			itemKeysList.add(String.valueOf(item.getValue()));
			itemNamesList.add(item.getLabel());
		}
		
		itemKeys = itemKeysList.toArray(new String[]{});
		itemNames = itemNamesList.toArray(new String[]{});
	}

	@Override
	public boolean setFocus() {
		return this.radioBtns[this.selectedIndex].setFocus();
	}

	@Override
	public void setEnable(boolean arg0) {
		for (Button btn : this.radioBtns) {
			btn.setEnabled(arg0);
		}
	}

	@Override
	public String getValue() {
		//return (String) this.radioBtns[this.selectedIndex].getData(Const.KEY);
		int index = getSelectedIndex();
		if(index == -1) return "";
		return this.itemKeys[index];
	}
	
	/**
	 * 選択されたラジオボタンのインデックスを取得
	 * @return
	 */
	public int getSelectedIndex() {
		int index = -1;
		for (int i = 0; i < this.radioBtns.length; i++) {
			if (this.radioBtns[i].getSelection()) {
				index = i;
				break;
			}
		}
		
		return index;
	}

	public void setSelection(boolean b) {
		for (int i = 0; i < this.radioBtns.length; i++) {
			this.radioBtns[i].setSelection(b);
		}
	}

	@Override
	public void setValue(Object value) {
		if (value == null) return;
		for (int i = 0; i < this.itemKeys.length; i++) {
			if (this.itemKeys[i].equals(String.valueOf(value))) {
				this.radioBtns[i].setSelection(true);
				break;
			}
		}
	}

//	@Override
//	public void setErrorStyle() {
//		this.label.setCapture(true);
//		Color red = this.getDisplay().getSystemColor(SWT.COLOR_RED);
//		this.label.setForeground(red);		
//	}

}
