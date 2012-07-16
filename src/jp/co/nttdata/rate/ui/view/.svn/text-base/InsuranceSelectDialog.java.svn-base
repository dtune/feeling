package jp.co.nttdata.rate.ui.view;

import java.util.List;

import jp.co.nttdata.rate.rateFundation.dbConnection.DBConnection;
import jp.co.nttdata.rate.rateFundation.dbConnection.DataRow;
import jp.co.nttdata.rate.util.CommonUtil;
import jp.co.nttdata.rate.util.Const;
import jp.co.nttdata.rate.util.ResourceLoader;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.TableEditor;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.TableColumn;

/**
 * 初回の場合、このダイアログを開いて、商品を選択次第メイン画面を開く
 * <br>応じて商品のOL画面を初期化する
 * @author btchoukug
 *
 */
public class InsuranceSelectDialog extends Dialog{
		
	private String selectedCode = null;
	public static String insuranceName;
	private final String sql = "SELECT code,name,dividend_flag,special_flag,bonus_flag,paidup_flag,extend_flag,unpaidAnnuity_flag,deathBenefit_flag,basicAnnuity_flag FROM insurance_master where category = ${type}";
	private List<DataRow> insuranceInfoList;
	
	private Shell dialog;
	
	public InsuranceSelectDialog(Shell parent,String type) {
		super(parent);
		insuranceInfoList = _getInsuranceInfo(type);
	}
	
	public InsuranceSelectDialog(String type) {
		super(new Shell());		
		insuranceInfoList = _getInsuranceInfo(type);
	}
			
	/**
	 * ダイアログを開いて、選択された保険商品のコードを返す
	 * @return
	 */
	public String open() {
		
	    Shell parent = getParent();
	    dialog = new Shell(parent, SWT.DIALOG_TRIM | SWT.APPLICATION_MODAL);
	    dialog.setLayout(new GridLayout(2, false));
	    dialog.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false,
		        true, 1, 1));
	    dialog.setText("数理レートツール");
	    Image img = new Image(null, ResourceLoader.getExternalResourceAsStream(Const.IMAGEICON));
	    dialog.setImage(img);
	    Label label = new Label(dialog, SWT.NONE);
	    label.setText("下記の計算対象（保険種類）から１つを選択してください。同一保種に対して複数の保障が付加されます。");
	    	    
	    //保険商品一覧
	    final Table table = _createTree(dialog);
	    
	    //OKとCancelボタン
	    Button okBtn = new Button(dialog, SWT.NONE);
	    okBtn.setText("決定");
	    dialog.setDefaultButton(okBtn);
	    //okBtn.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING));
	    okBtn.addSelectionListener(new SelectionAdapter() {
	    	
	    	public void widgetSelected(SelectionEvent e) {
	    		
	    		int checkedCount = 0;
	    		
	    		Control controls[] = table.getChildren();
	    		for (Control control : controls) {
	    			Button checkbox = (Button)control;
	    			
	    			//保障選択の場合、商品コード＋保障フラグとなる
	    			String checkBoxData = checkbox.getData().toString();
	    			
	    			if (checkbox.getSelection()) {
	    				//まず、商品チェックボックスかどうか判断してから商品コードをセット
	    				if(checkBoxData.length() == 3) {
	    					selectedCode = checkBoxData;
	    					insuranceName = checkbox.getText();
	    					checkedCount ++;
	    				}
	    				
	    				//商品コードが指定されてない場合、ほかのチェックボックスが選択されても次へ
	    				if (selectedCode == null) {
	    					continue;
	    				}
	    				
	    				//既に商品が選択された場合、保障部分のチェックボックスのフラグ値を商品コードの末尾に追記
	    				if(checkBoxData.length() > 3 && checkBoxData.substring(0, 3).equals(selectedCode.substring(0, 3))) {
	    					selectedCode += checkBoxData.substring(3);
	    				}
	    			}
	    		}
	    		
				if (checkedCount == 1 && selectedCode != null) {
					dialog.dispose();
				} else {

					MessageBox msgBox = new MessageBox(dialog, SWT.ICON_WARNING
							| SWT.OK);
					String msg = null;
					if (checkedCount == 0) {
						msg = "１つ保種を選択してください。";
					} else {
						msg = "１つ保種しか選択されません。";
					}

					selectedCode = null;
					msgBox.setMessage(msg);
					msgBox.open();
				}
	    		
	    	}
	    	
	    });
	    
	    Button cancelBtn = new Button(dialog, SWT.NONE);
	    cancelBtn.setText("キャンセル");
	   // cancelBtn.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING));
	    cancelBtn.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				dialog.dispose();
				// 商品選択画面から商品コード指定を行う
				TypeDialog typeDialog = new TypeDialog();
				String type = typeDialog.open();
				if(type != null) {
					InsuranceSelectDialog dialog = new InsuranceSelectDialog(type);
					String code = dialog.open();
				
					if (code != null) {
						selectedCode = code;
					}
				}
			}
			
			@Override
			public void widgetDefaultSelected(SelectionEvent arg0) {
				;
			}
		});

	    dialog.pack();
	    CommonUtil.setShellLocation(dialog);
	    dialog.open();
	    
	    while (!dialog.isDisposed()) {
	      if (!dialog.getDisplay().readAndDispatch())
	        dialog.getDisplay().sleep();
	    }
	    
	    return selectedCode;
	}
		
	/**
	 * DBから保険商品対象を取得して表示する
	 * <br>業務モジュール初期化は一回のみ行うべき
	 * @return
	 */
	private List<DataRow> _getInsuranceInfo(String type) {
		
		List<DataRow> result = DBConnection.getInstance().query(sql, "type", type);
		if (result.size() == 0) {
			throw new RuntimeException("rate_masterテーブルに商品情報は設定されてなかった");
		}
		return result;
		
	}
	
	/**
	 * サイドリスト：保険商品テーブルを作る 
	 * @param shell
	 * @return 
	 */
	private Table _createTree(final Shell shell) {	       
		
		Table table = new Table(dialog, SWT.BORDER | SWT.V_SCROLL);
	    table.setHeaderVisible(true);
	    table.setLinesVisible(true);
	    GridData tableData = new GridData(GridData.FILL_BOTH);
		tableData.horizontalSpan = 2;
		tableData.heightHint = 400;
		table.setLayoutData(tableData);

		TableColumn[] column = new TableColumn[10];
	    for (int i = 0; i < 9; i++) {
	      column[i] = new TableColumn(table, SWT.LEFT);	      
	      column[i].setResizable(true);
	      column[i].setWidth(75);
	      //column[i].pack();
	    }
	    
	    column[0].setWidth(300);
	    column[0].setText("保険種類（コード）PVW");
	    column[1].setText("配当金");
	    column[2].setText("特別条件付");
	    column[3].setText("ボーナス");
	    column[4].setText("払済Ｓ");
	    column[5].setText("延長定期保険");
	    column[6].setText("未払年金");
   	    column[7].setText("死亡給付金");
   	    column[8].setText("基本年金額");
   	    
	    for (int i = 0; i < insuranceInfoList.size(); i++) {
			
	    	TableItem item = new TableItem(table, SWT.NONE);
	    	final TableEditor editor = new TableEditor (table);
	    	TableEditor dividend_editor = new TableEditor (table);
	    	TableEditor special_editor = new TableEditor (table);
	    	TableEditor paidup_editor = new TableEditor (table);
	    	TableEditor extend_editor = new TableEditor (table);
	    	TableEditor bonus_editor = new TableEditor (table);
	    	TableEditor lumpSum_editor = new TableEditor (table);
	    	TableEditor deathBenefit_editor = new TableEditor (table);
	    	TableEditor basicAnnuity_editor = new TableEditor (table);
	        
	    	DataRow data = insuranceInfoList.get(i);
			String name = data.getString("name");
			String code = data.getString("code");
			String dividend = data.getString("dividend_flag");
			String special = data.getString("special_flag");
			String bonus = data.getString("bonus_flag");
			String paidup = data.getString("paidup_flag");
			String extend = data.getString("extend_flag");
			String unpaidAnnuity = data.getString("unpaidAnnuity_flag");
			String deathBenefit = data.getString("deathBenefit_flag");
			String basicAnnuity = data.getString("basicAnnuity_flag");
	    
	        Color btnColor = table.getBackground();
	        
	        final Button check = new Button(table, SWT.CHECK);
	        //check.setBackground(Display.getCurrent().getSystemColor(SWT.COLOR_TITLE_INACTIVE_BACKGROUND_GRADIENT));
	        check.setText("(" + code +")" + name);
	        check.pack();
	        check.setData(code);
	        check.setBackground(btnColor);
	        editor.minimumWidth = check.getSize ().x;
	        editor.horizontalAlignment = SWT.LEFT;
	        editor.setEditor(check, item, 0);
	        item.setData(code);	        

	        if (dividend.equals("1")) {
				final Button dividend_check = new Button(table, SWT.CHECK);
				dividend_check.setText("あり");
				// 配当金に関してはUIに既に定義したため、
				// 算式ロードするため、デフォルトとして選択された。
				dividend_check.setSelection(true);
				dividend_check.setEnabled(false);
				dividend_check.pack();
				dividend_check.setData(code + "7");
				dividend_check.setBackground(btnColor);
				dividend_editor.minimumWidth = dividend_check.getSize().x;
				dividend_editor.horizontalAlignment = SWT.LEFT;
				dividend_editor.setEditor(dividend_check, item, 1);
			}
	        if(special.equals("1")) {
	        	final Button special_check = new Button(table, SWT.CHECK);
	        	special_check.setText("あり");
	        	special_check.pack();
	        	special_check.setData(code+"1");
	        	special_check.setBackground(btnColor);
	        	special_editor.minimumWidth = special_check.getSize ().x;
	        	special_editor.horizontalAlignment = SWT.LEFT;
	        	special_editor.setEditor(special_check, item, 2);
	        }
			if (bonus.equals("1")) {
				final Button bonus_check = new Button(table, SWT.CHECK);
				bonus_check.setText("あり");
				bonus_check.pack();
				bonus_check.setData(code + "2");
				bonus_check.setBackground(btnColor);
				bonus_editor.minimumWidth = bonus_check.getSize().x;
				bonus_editor.horizontalAlignment = SWT.LEFT;
				bonus_editor.setEditor(bonus_check, item, 3);
			}

			if (paidup.equals("1")) {
				final Button paidup_check = new Button(table, SWT.CHECK);
				paidup_check.setText("あり");
				// 払い済みに関してはUIに既に定義したため、
				// 算式ロードするため、デフォルトとして選択された。延長も同じ
				paidup_check.setSelection(true);
				paidup_check.setEnabled(false);
				paidup_check.pack();
				paidup_check.setData(code + "3");
				paidup_check.setBackground(btnColor);
				paidup_editor.minimumWidth = paidup_check.getSize().x;
				paidup_editor.horizontalAlignment = SWT.LEFT;
				paidup_editor.setEditor(paidup_check, item, 4);
			}

			if (extend.equals("1")) {
				final Button extend_check = new Button(table, SWT.CHECK);
				extend_check.setText("あり");
				extend_check.setSelection(true);
				extend_check.setEnabled(false);
				extend_check.pack();
				extend_check.setData(code + "4");
				extend_check.setBackground(btnColor);
				extend_editor.minimumWidth = extend_check.getSize().x;
				extend_editor.horizontalAlignment = SWT.LEFT;
				extend_editor.setEditor(extend_check, item, 5);
			}
			if (unpaidAnnuity.equals("1")) {
				final Button annuity_check = new Button(table, SWT.CHECK);
				annuity_check.setText("あり");
				annuity_check.setSelection(true);
				annuity_check.setEnabled(false);
				annuity_check.pack();
				annuity_check.setData(code + "5");
				annuity_check.setBackground(btnColor);
				lumpSum_editor.minimumWidth = annuity_check.getSize().x;
				lumpSum_editor.horizontalAlignment = SWT.LEFT;
				lumpSum_editor.setEditor(annuity_check, item, 6);
			}
			if (deathBenefit.equals("1")) {
				final Button deathBenefit_check = new Button(table, SWT.CHECK);
				deathBenefit_check.setText("あり");
				deathBenefit_check.setSelection(true);
				deathBenefit_check.setEnabled(false);
				deathBenefit_check.pack();
				deathBenefit_check.setData(code + "6");
				deathBenefit_check.setBackground(btnColor);
				deathBenefit_editor.minimumWidth = deathBenefit_check.getSize().x;
				deathBenefit_editor.horizontalAlignment = SWT.LEFT;
				deathBenefit_editor.setEditor(deathBenefit_check, item, 7);
			}
			if (basicAnnuity.equals("1")) {
				final Button basicAnnuity_check = new Button(table, SWT.CHECK);
				basicAnnuity_check.setText("あり");
				basicAnnuity_check.setSelection(true);
				basicAnnuity_check.setEnabled(false);
				basicAnnuity_check.pack();
				basicAnnuity_check.setData(code + "8");
				basicAnnuity_check.setBackground(btnColor);
				basicAnnuity_editor.minimumWidth = basicAnnuity_check.getSize().x;
				basicAnnuity_editor.horizontalAlignment = SWT.LEFT;
				basicAnnuity_editor.setEditor(basicAnnuity_check, item, 8);
			}
	    }
		
		return table;
	    
	}
	
		
}
