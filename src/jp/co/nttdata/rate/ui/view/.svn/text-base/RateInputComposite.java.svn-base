package jp.co.nttdata.rate.ui.view;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import jp.co.nttdata.rate.exception.RateException;
import jp.co.nttdata.rate.model.rateKey.Item;
import jp.co.nttdata.rate.model.rateKey.RateKey;
import jp.co.nttdata.rate.model.rateKey.RateKeyValidator;
import jp.co.nttdata.rate.util.CommonUtil;
import jp.co.nttdata.rate.util.Const;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
/**
 * 入力UI：定義されたレートキーより入力項目を自動的に生成する
 * @author btchoukug
 *
 */
public class RateInputComposite {
	
	/** コントロールの作成参照：レートキー */
	private List<RateKey> keys;
	
	/** レートキーの名と入力項目のマッピング */
	private Map<String, RateKeyInputItem> namedInputItems = new HashMap<String, RateKeyInputItem>();
		
	private Group _group;
	
	/**
	 * レートキーのデフォルト値どおりに、入力画面を初期化
	 * @param keys
	 * @param parent
	 * @param style
	 */
	public RateInputComposite(List<RateKey> keys, Composite parent, int style) {
		
		_init(keys, parent, style);
	}
	
	/**
	 * レートキーのデフォルト値の代わりに、指定レートキーの値どおりに、入力画面を初期化
	 * @param keys
	 * @param keyValues
	 * @param parent
	 * @param style
	 */
	public RateInputComposite(List<RateKey> keys, Map<String, Object> keyValues, Composite parent, int style) {
		
		_init(keys, parent, style);

		// すてべのデフォルト値をクリア
		clearAll();

		// 指定キーの値どおりに、入力画面を初期化
		_setInputedRateKeyValues(keyValues);
	}
	
	/**
	 * 指定キーの値どおりに、入力画面を初期化
	 * @param keyValues
	 */
	private void _setInputedRateKeyValues(Map<String, Object> keyValues) {

		// 指定キーの入力項目にて値をセット
		for (Control ctl : getChildren()) {
			if (ctl instanceof RateKeyInputItem) {
				RateKeyInputItem inputItem = (RateKeyInputItem) ctl;
				Object keyValue = keyValues.get(inputItem.getRateKey()
						.getName());
				inputItem.setValue(keyValue);
			}
		}

	}

	private void _init(List<RateKey> keys, Composite parent, int style) {
		_group = new Group(parent, style);
		
		//Gridレイアウトの設定
		GridLayout gridLayout = new GridLayout();
		gridLayout.verticalSpacing = 5;
		gridLayout.horizontalSpacing = 15;
		gridLayout.marginTop = 10;
		gridLayout.marginWidth = 10;
		gridLayout.marginBottom = 10;
		
		gridLayout.numColumns = 3;
		
		_group.setLayout(gridLayout);
		_group.setLayoutData(new GridData(GridData.GRAB_HORIZONTAL
        | GridData.HORIZONTAL_ALIGN_FILL
        | GridData.VERTICAL_ALIGN_FILL));
		_group.setText("レートキー入力");

		this.keys = keys;

		//入力項目を作成
		_createContent();
		
		//連動処理を追加
		_addInputListener();
	}
	
	/**
	 * 入力項目の間の連動処理
	 */
	private void _addInputListener() {
		
		//回数と払い期間の連動
		final RateKeyTextItem text_n = (RateKeyTextItem) getInputItemByName("n");
		final RateKeyComboItem combo_kaisu = (RateKeyComboItem) getInputItemByName("kaisu");
		final RateKeyTextItem text_m = (RateKeyTextItem) getInputItemByName("m");
		if (text_n != null) {
			text_n.addKeyListener(new KeyAdapter(){
				//デフォルト場合、払い期間は保険期間とする
				public void keyReleased(KeyEvent event) {
					//一時払いの場合、0を保留
					if (!combo_kaisu .getValue().equals("1") && text_m != null) {
						text_m.setText(text_n.getValue());
					}
				}
			});			
		}
		
		if (combo_kaisu != null) {
			combo_kaisu.addSelectionListener(new SelectionAdapter(){			
				//回数と払い期間の連動
				public void widgetSelected(final SelectionEvent arg0) {
					if (combo_kaisu.getValue().equals("1") && text_m != null) {
						text_m.setText("0");
						text_m.setEnabled(false);
					} else {
						if (text_m != null) {
							if (text_m.getValue().equals("0")) {
								text_m.setText("");
							}						
							text_m.setEnabled(true);									
						}
					}
				}
			});			
		}

		
		//契約経過年月と払込経過年月の連動
		final RateKeyTextItem text_t = (RateKeyTextItem) getInputItemByName("t");
		if (text_t != null) {
			text_t.addKeyListener(new KeyAdapter(){
				public void keyReleased(KeyEvent event) {
					RateKeyTextItem text_t1 = (RateKeyTextItem) getInputItemByName("t1");
					if (text_t1 != null) {
						text_t1.setText(text_t.getValue());
					}							
				}
			});			
		}
		
		final RateKeyTextItem text_f = (RateKeyTextItem) getInputItemByName("f");
		if (text_f != null) {
			text_f.addKeyListener(new KeyAdapter(){
				public void keyReleased(KeyEvent event) {
					RateKeyTextItem text_f1 = (RateKeyTextItem) getInputItemByName("f1");
					if (text_f1 != null) {
						text_f1.setText(text_f.getValue());
					}
				}
			});			
		}
		
	}

	/**
	 * レートキーのタイプよりそれぞれ入力項目を作成
	 */
	private void _createContent() {
		
		for (RateKey key : this.keys) {
			if (key.getType().equals(RateKey.TEXT_TYPE)) {
				//テキスト
				this.namedInputItems.put(key.getName(), new RateKeyTextItem(_group, key));
			} else if (key.getType().equals(RateKey.RADIO_TYPE)) {
				//ラジオボタン
				this.namedInputItems.put(key.getName(), new RateKeyRadioItem(_group, key));				
			} else {
				//コンボボックス
				this.namedInputItems.put(key.getName(), new RateKeyComboItem(_group, key));
			}
		}		
	}
		
	/**
	 * レートキー名より、入力コンポジットから入力項目を取得する
	 * @param name
	 * @return RateKeyInputItem
	 */
	public RateKeyInputItem getInputItemByName(String name) {
		return this.namedInputItems.get(name);		
	}
	
	/**
	 * XMLの設定の代わりに、DBから取得した世代情報を世代コンボのリストとする
	 * @param items 
	 */
	public void setGenerationList(List<Item> items) {
		RateKeyComboItem gen = (RateKeyComboItem) getInputItemByName("gen");
		if (gen != null && items != null && items.size() > 0) {
			gen.setKeyItems(items);
			gen.select(0);
		}
	}
	
	public Control[] getChildren() {
		return _group.getChildren();
	}

	public void pack() {
		_group.pack();		
	}

	/**
	 * レートキーの項目の入力値をクリアする
	 */
	public void clearAll() {
		for (Control ctl : getChildren()) {
			if (ctl instanceof RateKeyTextItem) {
				RateKeyTextItem input = (RateKeyTextItem)ctl;
				input.setText(Const.EMPTY);
				input.setEnabled(true);
			}
			if (ctl instanceof RateKeyComboItem) {
				RateKeyComboItem input = (RateKeyComboItem)ctl;
				input.setText(Const.EMPTY);
				input.setEnabled(true);
			}
			if (ctl instanceof RateKeyRadioItem) {
				RateKeyRadioItem radio = (RateKeyRadioItem)ctl;
				radio.setSelection(false);
			}
		}
	}

	public void setLayoutData(Object layoutData) {
		_group.setLayoutData(layoutData);		
	}

	/**
	 * UIから入力値を取得
	 * @param checkRequired
	 * @return
	 * @throws RateException
	 */
	public Map<String, Object> getInputedRateKeyValues(boolean checkRequired) throws RateException {
		
		Map<String, Object> inputKeys = new HashMap<String, Object>();
		
		//画面から入力値を編集してMapにセットする	
		for (Control ctl : getChildren()) {
			if (ctl instanceof RateKeyInputItem) {

				RateKeyInputItem inputItem = (RateKeyInputItem)ctl;
				String value = inputItem.getValue();
				
				//チェック必要指定の場合、必須項目に対して単項目チェックを行う
				RateKey key = inputItem.getRateKey();
				
				/*
				 * 杭州NTTDからFS2_HZ_CSM_SI1_0017故障票のご要望通りに、
				 * 保険金額の業務チェック、保険期間・払込期間・年齢のチェックを外す
				 */
				if (!ArrayUtils.contains(Const.NO_VALIDATE_KEYS, key.getName())) {
					if (checkRequired && key.isRequired()) {
						RateKeyValidator.validteKey(key, value);
					}					
				}
				
				if (StringUtils.isNotEmpty(value)) {
					if (!CommonUtil.isNumeric(value)) {
						throw new RateException(Const.ERR_NOT_NUMERIC, key);
					}
					inputKeys.put(key.getName(), Double.parseDouble(value));
				}

			}
		}

		return inputKeys;
	}
}


