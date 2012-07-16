package jp.co.nttdata.rate.ui.view;

import java.util.HashMap;
import java.util.Map;
import jp.co.nttdata.rate.exception.RateException;
import jp.co.nttdata.rate.util.CommonUtil;
import jp.co.nttdata.rate.util.Const;
import org.apache.commons.lang.StringUtils;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowData;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Layout;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;

/**
 * 標準体の場合、コンペア対象の設定グループUI
 * @author zhanghy
 *
 */
public class CompareSettingGroup {

	Shell _shell;
	Group _group;
	
	Label compareOriginal_label;
	Text compareOriginal_text;
	Button compareOriginal_btn;
	Label compareDest_label;
	Text compareDest_text;
	Button compareDest_btn;
	
	//データを持つモジュール
	private String[] inputDataHeaders;
	private String[] inputRateKeyNames;
	private String[] calculableFormulas;
	
	/** 選択された算式のインデックス */
	private int selectedFormulaIndex;
	
	/** 比較元と比較先を格納するMap */
	protected Map<String, String> compareMapping;
	
	public CompareSettingGroup(Composite parent, int style) {
		_group = new Group(parent, style);
		_shell = parent.getShell();
		_createCompareSettingGroup();
	}
	
	private void _createCompareSettingGroup() {

		// 比較元と比較先を設定
		Composite compareOriginal_comp = new Composite(_group,
				SWT.NONE);
		compareOriginal_comp.setLayout(new RowLayout(SWT.HORIZONTAL));
		compareOriginal_label = new Label(compareOriginal_comp,
				SWT.NONE);
		compareOriginal_label.setText("比較元：");
		compareOriginal_text = new Text(compareOriginal_comp, SWT.BORDER);
		compareOriginal_text.setEditable(false);
		RowData rd_compareItem = new RowData();
		rd_compareItem.width = 120;
		rd_compareItem.height = 12;
		compareOriginal_text.setLayoutData(rd_compareItem);
		compareOriginal_btn = new Button(compareOriginal_comp, SWT.NONE);
		compareOriginal_btn.setText("選択");
		compareOriginal_btn.setEnabled(false);

		compareOriginal_btn
				.addMouseListener(new CreateCompareSelectionAction(_group, 
						CreateCompareSelectionAction.ORIGNINAL));
		
		Composite compareDest_comp = new Composite(_group,
				SWT.NONE);
		compareDest_comp.setLayout(new RowLayout(SWT.HORIZONTAL));
		Label compareDest_label = new Label(compareDest_comp, SWT.NONE);
		compareDest_label.setText("比較先：");
		compareDest_text = new Text(compareDest_comp, SWT.BORDER);
		compareDest_text.setEditable(false);
		compareDest_text.setLayoutData(rd_compareItem);
		compareDest_btn = new Button(compareDest_comp, SWT.NONE);
		compareDest_btn.setText("選択");
		
		compareDest_btn.addMouseListener(new CreateCompareSelectionAction(_group, 
				CreateCompareSelectionAction.DESTINATION));

	}

	/**
	 * 選択された算式のインデックスを返す
	 * @return
	 */
	public int getSelectedFormulaIndex() {
		return selectedFormulaIndex;
	}

	public void setInputDataHeaders(String[] inputDataHeaders) {
		if (inputDataHeaders == null) {
			throw new IllegalArgumentException("入力のデータヘッダが指定されない。");
		}
		this.inputDataHeaders = inputDataHeaders;
	}

	public void setInputRateKeyNames(String[] inputRateKeyNames) {
		if (inputDataHeaders == null) {
			throw new IllegalArgumentException("入力のレートキー名が指定されない。");
		}
		this.inputRateKeyNames = inputRateKeyNames;
	}

	public void setCalculableFormulas(String[] calculableFormulas) {
		if (calculableFormulas == null) {
			throw new IllegalArgumentException("計算対象の公式が存在しない。");
		}
		this.calculableFormulas = calculableFormulas;
	}
	
	public void setButtonEnabled() {
		compareOriginal_btn.setEnabled(true);
	}
	
	public void clearTextValue() {
		if (this._group.isDisposed()) return;
		compareOriginal_text.setText("");
		compareDest_text.setText("");
	}

	/**
	 * 比較対象選択ダイアローグを作る
	 * 
	 * @author btchoukug
	 * 
	 */
	class CreateCompareSelectionAction extends MouseAdapter {
		
		public static final int ORIGNINAL = 1;
		public static final int DESTINATION = 2;
		public static final int DEATHINDEX = 3;

		private Text deathIndexText;
		private int type = 0;

		public CreateCompareSelectionAction(Group compareGroup, int type) {
			if (type < 1 || type > 2) {
				throw new IllegalArgumentException(type
						+ "不正のタイプが渡されました。");
			}
			this.type = type;
		}
		
		public CreateCompareSelectionAction(Group compareGroup, Text deathIndexText) {
			this.type = DEATHINDEX;
			if (deathIndexText == null) {
				throw new IllegalArgumentException("特体死亡指数テキストがnull。");
			}
			this.deathIndexText = deathIndexText;
		}

		@Override
		public void mouseUp(MouseEvent arg0) {
			
			String[] inputItems;
			String[] inputDatas;
			
			if (ORIGNINAL == this.type || DEATHINDEX == this.type) {
				inputItems = inputDataHeaders;
				inputDatas = inputRateKeyNames;
			} else {
				inputItems = calculableFormulas;
				inputDatas = calculableFormulas;
			}
			
			_createCompareSelectionDialog(inputItems, inputDatas, this.type);
		}

		/**
		 * Creates and opens the "Compare items selection" dialog.
		 * @param inputItems
		 * @param inputDatas
		 * @param type
		 */
		private void _createCompareSelectionDialog(final String[] inputItems,
				final String[] inputDatas, final int type) {
			
			final Shell dialog = new Shell(_shell, SWT.DIALOG_TRIM
					| SWT.APPLICATION_MODAL);
			dialog.setText("比較の項目");
			dialog.setLayout(new GridLayout(2, false));
			final Table table = new Table(dialog, SWT.BORDER | SWT.V_SCROLL
					| SWT.CHECK);
			GridData data = new GridData(GridData.FILL_BOTH);
			data.verticalSpan = 2;
			data.widthHint = 130;
			data.heightHint = 300;
			table.setLayoutData(data);
			
			for (int i = 0; i < inputItems.length; i++) {
				TableItem item = new TableItem(table, SWT.NONE);
				item.setText(inputItems[i]);
				item.setData(inputDatas[i]);
			}

			// すべて項目を選択
			Button selectAll = new Button(dialog, SWT.PUSH);
			selectAll.setText("Select_All");
			selectAll.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_FILL));
			
			selectAll.addSelectionListener(new SelectionAdapter() {
				public void widgetSelected(SelectionEvent e) {
					TableItem[] items = table.getItems();
					for (int i = 0; i < inputItems.length; i++) {
						items[i].setChecked(true);
					}
				}
			});

			// すべて項目の選択をクリア
			Button deselectAll = new Button(dialog, SWT.PUSH);
			deselectAll.setText("Deselect_All");
			deselectAll.setLayoutData(new GridData(
					GridData.HORIZONTAL_ALIGN_FILL
							| GridData.VERTICAL_ALIGN_BEGINNING));
			deselectAll.addSelectionListener(new SelectionAdapter() {
				public void widgetSelected(SelectionEvent e) {
					TableItem[] items = table.getItems();
					for (int i = 0; i < inputItems.length; i++) {
						items[i].setChecked(false);
					}
				}
			});

			new Label(dialog, SWT.NONE); /* Filler */
			Button ok = new Button(dialog, SWT.PUSH);
			ok.setText("OK");
			dialog.setDefaultButton(ok);
			ok.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_FILL));
			ok.addSelectionListener(new SelectionAdapter() {
				
				public void widgetSelected(SelectionEvent e) {
					
					TableItem[] items = table.getItems();
					int checkedCount = 0;
					
					// TODO 選択された項目をテキストにセット、現時点は１つ項目だけで比較される
					StringBuffer compareObjs = new StringBuffer();
					StringBuffer compareObjsData = new StringBuffer();
					for (int i = 0; i < inputItems.length; i++) {
						if (items[i].getChecked()) {
							checkedCount++;
							compareObjs.append(Const.COMMA).append(
									(String) items[i].getText());
							compareObjsData.append(Const.COMMA).append(
									(String) items[i].getData());
							// 公式選択の場合
							if (type == DESTINATION) {
								selectedFormulaIndex = i;
							}
						}
					}

					if (checkedCount == 1) {

						if (compareObjs.length() > 0) {

							Text targetText = null;
							switch (type) {
							case ORIGNINAL:
								targetText = compareOriginal_text;
								break;
							case DESTINATION:
								targetText = compareDest_text;
								break;
							case DEATHINDEX:
								targetText = deathIndexText;
								break;
							default:
								throw new IllegalArgumentException(type
										+ "不正のタイプが渡されました。");
							}
							
							// 表示＆データを設定する
							targetText.setText(compareObjs.deleteCharAt(0).toString());
							targetText.setData(compareObjsData.deleteCharAt(0).toString());

						}

						dialog.dispose();

					} else {

						MessageBox msgBox = new MessageBox(dialog,
								SWT.ICON_WARNING | SWT.OK);
						String msg = null;
						if (checkedCount == 0) {
							msg = "１つ比較対象を選択してください。";
						} else {
							msg = "１つ比較対象しか選択されません。";
						}

						msgBox.setMessage(msg);
						msgBox.open();
					}

				}
			});
			
			dialog.pack();
			CommonUtil.setShellLocation(dialog);
			dialog.open();
			
			while (!dialog.isDisposed()) {
				if (!dialog.getDisplay().readAndDispatch())
					dialog.getDisplay().sleep();
			}
		}
	}

	public void setLayout(Layout layout) {
		_group.setLayout(layout);		
	}

	public void setLayoutData(Object data) {
		_group.setLayoutData(data);
	}

	public void setText(String string) {
		_group.setText(string);		
	}

	public String getOrigText() {
		
		if (this._group.isDisposed()) return null;
		
		//表示名ではなく、英語のレートキー名を返す
		return (String) compareOriginal_text.getData();
	}

	public String getDestText() {
		if (this._group.isDisposed()) return null;
		
		return (String) compareDest_text.getData();
	}
	
	public Map<String, String> getCompareMapping() throws RateException {
		
		String orig = this.getOrigText();
		String dest = this.getDestText();
		
		if (StringUtils.isEmpty(orig) || StringUtils.isEmpty(dest)) {
			throw new RateException("比較対象を指定ください。");
		}
		compareMapping = new HashMap<String, String>();
		compareMapping.put(orig, dest);
				
		return compareMapping;
	}
	
}
