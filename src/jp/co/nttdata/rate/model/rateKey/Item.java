package jp.co.nttdata.rate.model.rateKey;

/**
 * ���W�I��R���{�{�b�N�X�̌^�̃��[�g�L�[�ɂ��āA�I���A�C�e���ł���
 * @author btchoukug
 *
 */
public class Item {
	private String label;
	private int value;
	
	public Item(){
		;
	}
	
	public Item(String label, int value) {
		this.label = label;
		this.value = value;
	}
	
	public String getLabel() {
		return label;
	}
	public void setLabel(String label) {
		this.label = label;
	}
	public int getValue() {
		return value;
	}
	public void setValue(int value) {
		this.value = value;
	}
	
	
}
