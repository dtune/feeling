package jp.co.nttdata.rate.model.rateKey;

import java.util.List;

/**
 * ���W�I��R���{�{�b�N�X�Ƃ����I������郌�[�g�L�[�ł���
 * @author btchoukug
 *
 */
public class SelectableKey extends RateKey {
	
	@SuppressWarnings("unchecked")
	private List items;
	private int selectedIndex;
	
	@SuppressWarnings("unchecked")
	public List getItems() {
		return items;
	}
	@SuppressWarnings("unchecked")
	public void setItems(List items) {
		this.items = items;
	}
	public int getSelectedIndex() {
		return selectedIndex;
	}
	public void setSelectedIndex(int selectedIndex) {
		this.selectedIndex = selectedIndex;
	}
	
}
