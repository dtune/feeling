package jp.co.nttdata.rate.ui.view;

import java.util.ArrayList;
import java.util.List;
import jp.co.nttdata.rate.model.rateKey.Item;
import jp.co.nttdata.rate.model.rateKey.RateKey;
import jp.co.nttdata.rate.model.rateKey.SelectableKey;
import jp.co.nttdata.rate.util.Const;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;

/**
 * レートキーより入力項目：コンボボックス
 * @author btchoukug
 *
 */
public class RateKeyComboItem extends RateKeyInputItem {
	
	//コンボボックスあるいはラジオボタンの場合、選択肢Itemのリスト(labelとvalueを含む)
	private List<Item> items;
	
	//コンボボックスあるいはラジオボタンの場合、選択インデックス
	private int selectedIndex = 0;
	
	/** コンボボックスあるいはラジオボタンの場合、選択肢の配列 */
	private String[] itemKeys;
	private String[] itemNames;

	private Combo cmb;
	
	public RateKeyComboItem(Composite parent, RateKey rateKey) {
		super(parent, rateKey);
		//タイプごとの専有UI情報を読み込む
		readUIInfo(rateKey);
		_createComboItem();
	}
	
	private void _createComboItem() {
						
		//レートキーのタイプによって、Comboを作成
		cmb = new Combo(this, SWT.NONE);
		cmb.setLayoutData(getNextInputRowData());

		getItemsKeyAndName(items);
		cmb.setItems(this.itemNames);
		if (selectedIndex >= 0) {
			cmb.select(selectedIndex);	
		}
		
		//リスト設定
		cmb.setData(Const.KEY, this.itemKeys);
		
		//入力して、RateInputのインスタンスに変換するため、マッピングの名称をセットする
		cmb.setData(Const.NAME, keyName);
		
	}
	
	/**
	 * コンボボックスのキー：リストをセットする
	 * <br>例：0:男性,1:女性
	 * @param keyItems
	 */
	public void setKeyItems(List<Item> items) {
		getItemsKeyAndName(items);
		this.cmb.setItems(itemNames);
		this.cmb.setData(Const.KEY, this.itemKeys);		
	}
	
	/**
	 * セレクションを設定
	 * @param index
	 */
	public void select(int index) {
		this.cmb.select(index);
	}
	
	public void addSelectionListener(SelectionListener arg) {
		this.cmb.addSelectionListener(arg);
	}
	
	@SuppressWarnings("unchecked")
	@Override
	protected void readUIInfo(RateKey key) {
		SelectableKey cmbKey = (SelectableKey)key;
    	items = cmbKey.getItems();
    	selectedIndex = cmbKey.getSelectedIndex();
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
		return this.cmb.setFocus();
	}

	@Override
	public void setEnable(boolean arg0) {
		this.cmb.setEnabled(arg0);
	}

	@Override
	public String getValue() {
		this.selectedIndex = this.cmb.getSelectionIndex();
		
		if (this.selectedIndex < 0 || Const.EMPTY.equals(this.cmb.getText()) || this.cmb.getText() == null) {
			return Const.EMPTY;
		} else {
			return this.itemKeys[this.selectedIndex];
		}
		
	}
	
	public void setText(String text) {
		this.cmb.setText(text);
	}
		
	/**
	 * コンボボックスのリストのキーをセットする
	 * @param itemKeys
	 */
	public void setItemKeys(String[] itemKeys) {
		this.itemKeys = itemKeys;
		this.cmb.setData(Const.KEY, itemKeys);
	}

	/**
	 * コンボボックスのリストをセットする
	 * @param itemNames
	 */
	public void setItemNames(String[] itemNames) {
		this.itemNames = itemNames;
		this.cmb.setItems(itemNames);
	}

	@Override
	public void setValue(Object value) {
		if (value == null) return;
		for (int i = 0; i < this.itemKeys.length; i++) {
			if (this.itemKeys[i].equals(String.valueOf(value))) {
				this.cmb.select(i);
				break;
			}
		}
	}

//	@Override
//	public void setErrorStyle() {
//		this.label.setCapture(true);
//		Color red = this.getDisplay().getSystemColor(SWT.COLOR_RED);
//		this.label.setForeground(red);
//		this.cmb.setBackground(red);
//		
//	}
		
}
