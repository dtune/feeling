package jp.co.nttdata.rate.model.datalayout;

import java.util.ArrayList;
import java.util.List;
/**
 * 計算カテゴリに応じてキーのレイアウトを格納する
 * @author btchoukug
 *
 */
public class InputDataCategory {
	
//	private String code;
	/**データレイアウトのカレントカテゴリ */
	private String name;
	private String label;
	/**所属レートキーのレイアウト*/
	private List<DataLayout> layoutData;	
	
	public InputDataCategory(){
		layoutData = new ArrayList<DataLayout>();
	}

	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	public void setLayoutData(List<DataLayout> layoutData) {
		this.layoutData = layoutData;
	}

	public List<DataLayout> getLayoutData() {
		return layoutData;
	}
	
	public int getKeyNum() {
		return layoutData.size();
	}
	
	/**TODO BT計算結果ファイルに出力するとき、日本語名を使う*/
	public String[] getKeyNames() {
		int num = getKeyNum();
		String[] keyNames = new String[num];
		int i = 0;
		for (DataLayout key : this.layoutData) {
			//keyNames[i] = key.getDesc() + "(" + key.getName() + ")";
			keyNames[i] = key.getName();
			i++;
		}
		return keyNames;
	}
	
	public String[] getKeyDescs() {
		int num = getKeyNum();
		String[] keyNames = new String[num];
		int i = 0;
		for (DataLayout key : this.layoutData) {
			//keyNames[i] = key.getDesc() + "(" + key.getName() + ")";
			keyNames[i] = key.getDesc();
			i++;
		}
		return keyNames;
	}
}
