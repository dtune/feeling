package jp.co.nttdata.rate.batch.dataConvert;

import java.util.ArrayList;
import java.util.List;
/**
 * 計算カテゴリに応じてキーのレイアウトを格納する
 * @author btchoukug
 *
 */
public class BatchDataLayout {
	
	private String code;
	/**データレイアウトのカレントカテゴリ */
	private String cate;
	/**所属レートキーのレイアウト*/
	private List<RateKeyLayout> layoutData;	
	
	public BatchDataLayout(){
		layoutData = new ArrayList<RateKeyLayout>();
	}

	public void setCode(String code) {
		this.code = code;
	}

	public String getCode() {
		return code;
	}

	public void setCate(String cate) {
		this.cate = cate;
	}

	public String getCate() {
		return cate;
	}

	public void setLayoutData(List<RateKeyLayout> layoutData) {
		this.layoutData = layoutData;
	}

	public List<RateKeyLayout> getLayoutData() {
		return layoutData;
	}
	
	public int getRateKeyNum() {
		return layoutData.size();
	}
	
	/**TODO BT計算結果ファイルに出力するとき、日本語名を使う*/
	public String[] getKeyNames() {
		int num = getRateKeyNum();
		String[] keyNames = new String[num];
		int i = 0;
		for (RateKeyLayout key : this.layoutData) {
			//keyNames[i] = key.getDesc() + "(" + key.getName() + ")";
			keyNames[i] = key.getName();
			i++;
		}
		return keyNames;
	}
	
	public String[] getKeyDescs() {
		int num = getRateKeyNum();
		String[] keyNames = new String[num];
		int i = 0;
		for (RateKeyLayout key : this.layoutData) {
			//keyNames[i] = key.getDesc() + "(" + key.getName() + ")";
			keyNames[i] = key.getDesc();
			i++;
		}
		return keyNames;
	}
}
