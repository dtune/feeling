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
 * ���[�g�L�[�����͍��ځF���W�I�{�^���O���[�v
 * @author btchoukug
 *
 */
public class RateKeyRadioItem extends RateKeyInputItem {

	//�R���{�{�b�N�X���邢�̓��W�I�{�^���̏ꍇ�A�I����Item�̃��X�g(label��value���܂�)
	private List<Item> items;
	//�R���{�{�b�N�X���邢�̓��W�I�{�^���̏ꍇ�A�I���C���f�b�N�X
	private int selectedIndex = 0;
	
	/** �R���{�{�b�N�X���邢�̓��W�I�{�^���̏ꍇ�A�I�����̔z�� */
	private String[] itemKeys;
	private String[] itemNames;
	
	private Button[] radioBtns; 
	
	public RateKeyRadioItem(Composite parent, RateKey rateKey) {
		super(parent, rateKey);
		//�^�C�v���Ƃ̐�LUI����ǂݍ���
		readUIInfo(rateKey);
		_createRadioItem();
	}
		
	private void _createRadioItem() {
						
		RowData rd_input = getNextInputRowData();
		
		//���W�I�{�^���O���[�v
		Composite radioGroup = new Composite(this, SWT.NONE);
		radioGroup.setLayout(new RowLayout(SWT.HORIZONTAL));
		
		//�I���������A���W�I�A�C�e�����쐬
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
		
		//�O���[�v�̃��C�A�E�g���Đݒ�
		rd_input.width = (int) (LABEL_WIDTH * 1.5);
		
		//���͂��āARateInput�̃C���X�^���X�ɕϊ����邽�߁A�}�b�s���O�̖��̂��Z�b�g����
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
	 * �I�����ꂽ���W�I�{�^���̃C���f�b�N�X���擾
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
