package jp.co.nttdata.rate.ui.view;

import java.util.HashMap;
import java.util.Map;

import jp.co.nttdata.rate.exception.RateException;

import org.apache.commons.lang.StringUtils;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.RowData;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

/**
 * 特体の場合、コンペア対象の設定グループUI
 * @author zhanghy
 *
 */
public class SpecialCompareSettingGroup extends CompareSettingGroup {

	/** 現時点死亡指数ランクが100とする、100以上が特体 */
	private static final String STD_INDEX = "100";
	Text deathIndexText;
	Button deathIndexSelectBtn;
	Text deathIndexRankText;
	
	public SpecialCompareSettingGroup(Composite parent, int style) {
		super(parent, style);
		//特体の判断元：特体死亡指数とランク値を追加
		_createDeathIndexUI();
	}

	private void _createDeathIndexUI() {
		
		// 特体の判断元：特体死亡指数　ここから
		Composite deathIndex_comp = new Composite(_group,
				SWT.NONE);
		deathIndex_comp.setLayout(new RowLayout(SWT.HORIZONTAL));
		Label deathIndex_label = new Label(deathIndex_comp,
				SWT.NONE);
		deathIndex_label.setText("判断元：");
		
		deathIndexText = new Text(deathIndex_comp, SWT.BORDER);
		deathIndexText.setEditable(false);
		RowData rd_compareItem = new RowData();
		rd_compareItem.width = 120;
		rd_compareItem.height = 12;
		deathIndexText.setLayoutData(rd_compareItem);
		
		deathIndexSelectBtn = new Button(deathIndex_comp, SWT.NONE);
		deathIndexSelectBtn.setText("選択");
		deathIndexSelectBtn.setEnabled(false);

		deathIndexSelectBtn.addMouseListener(
				new CreateCompareSelectionAction(_group, deathIndexText));
		
		// ランク値のテキスト　ここから
		Composite deathIndexRank_comp = new Composite(_group,
				SWT.NONE);
		deathIndexRank_comp.setLayout(new RowLayout(SWT.HORIZONTAL));
		Label deathIndexRank_label = new Label(deathIndexRank_comp,
				SWT.NONE);
		deathIndexRank_label.setText("死亡指数ランク：");
		deathIndexRankText = new Text(deathIndexRank_comp, SWT.BORDER);
		deathIndexRankText.setLayoutData(rd_compareItem);
		deathIndexRankText.setText(STD_INDEX);
	}
	
	@Override
	public void setButtonEnabled() {
		if (this._group.isDisposed()) return;		
		compareOriginal_btn.setEnabled(true);
		deathIndexSelectBtn.setEnabled(true);
	}
	
	/**
	 * 選択された「特体死亡指数」の項目名を返す
	 * @return
	 */
	public String getDeathIndexColumnName() {
		
		if (this._group.isDisposed()) return null;
		
		//レートキーの英語名はdataに格納したため
		String name = (String) this.deathIndexText.getData();
		return name;
	}
	
	/**
	 * 入力された死亡指数ランクの値を返す
	 * @return
	 * @throws RateException 
	 */
	public int getDeathIndexRankValue() throws RateException {
		int rank = 0;
		
		if (this._group.isDisposed()) return 0;
		
		try {
			rank = Integer.parseInt(this.deathIndexRankText.getText());
		} catch (NumberFormatException nfe) {
			throw new RateException("死亡指数ランクに数字を入力してください。");
		}
		
		//TODO 合法性チェック要る？（100~400）
		if (rank < 100 || rank > 400) {
			throw new RateException("死亡指数ランクに100～400範囲で入力してください。");
		}
		
		return rank;
	}
	
	@Override
	public Map<String, String> getCompareMapping() throws RateException {
		
		if (this._group.isDisposed()) return null;
		
		String orig = this.getOrigText();
		String dest = this.getDestText();
		
		if (StringUtils.isEmpty(orig) || StringUtils.isEmpty(dest)) {
			return null;
		} else {
			compareMapping = new HashMap<String, String>();
			compareMapping.put(orig, dest);
			return compareMapping;
		}
		
	}

	public void setVisiable(boolean visible) {
		if (!visible){
			this._group.dispose();
		}		
	}

}
