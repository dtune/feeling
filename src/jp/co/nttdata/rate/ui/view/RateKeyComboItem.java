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
 * ���[�g�L�[�����͍��ځF�R���{�{�b�N�X
 * @author btchoukug
 *
 */
public class RateKeyComboItem extends RateKeyInputItem {
	
	//�R���{�{�b�N�X���邢�̓��W�I�{�^���̏ꍇ�A�I����Item�̃��X�g(label��value���܂�)
	private List<Item> items;
	
	//�R���{�{�b�N�X���邢�̓��W�I�{�^���̏ꍇ�A�I���C���f�b�N�X
	private int selectedIndex = 0;
	
	/** �R���{�{�b�N�X���邢�̓��W�I�{�^���̏ꍇ�A�I�����̔z�� */
	private String[] itemKeys;
	private String[] itemNames;

	private Combo cmb;
	
	public RateKeyComboItem(Composite parent, RateKey rateKey) {
		super(parent, rateKey);
		//�^�C�v���Ƃ̐�LUI����ǂݍ���
		readUIInfo(rateKey);
		_createComboItem();
	}
	
	private void _createComboItem() {
						
		//���[�g�L�[�̃^�C�v�ɂ���āACombo���쐬
		cmb = new Combo(this, SWT.NONE);
		cmb.setLayoutData(getNextInputRowData());

		getItemsKeyAndName(items);
		cmb.setItems(this.itemNames);
		if (selectedIndex >= 0) {
			cmb.select(selectedIndex);	
		}
		
		//���X�g�ݒ�
		cmb.setData(Const.KEY, this.itemKeys);
		
		//���͂��āARateInput�̃C���X�^���X�ɕϊ����邽�߁A�}�b�s���O�̖��̂��Z�b�g����
		cmb.setData(Const.NAME, keyName);
		
	}
	
	/**
	 * �R���{�{�b�N�X�̃L�[�F���X�g���Z�b�g����
	 * <br>��F0:�j��,1:����
	 * @param keyItems
	 */
	public void setKeyItems(List<Item> items) {
		getItemsKeyAndName(items);
		this.cmb.setItems(itemNames);
		this.cmb.setData(Const.KEY, this.itemKeys);		
	}
	
	/**
	 * �Z���N�V������ݒ�
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
	 * �R���{�{�b�N�X�܂��̓��W�I�{�^���Ɏw�肵���I��������L�[�܂��͕\�������擾�i��F"1:�j��,2:����"�j
	 * <br>�����擾�ł��Ȃ������ꍇ�A�T�C�Y���O�̔z���Ԃ�
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
	 * �R���{�{�b�N�X�̃��X�g�̃L�[���Z�b�g����
	 * @param itemKeys
	 */
	public void setItemKeys(String[] itemKeys) {
		this.itemKeys = itemKeys;
		this.cmb.setData(Const.KEY, itemKeys);
	}

	/**
	 * �R���{�{�b�N�X�̃��X�g���Z�b�g����
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
