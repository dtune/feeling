package jp.co.nttdata.rate.ui.view;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import jp.co.nttdata.rate.exception.FmsDefErrorException;
import jp.co.nttdata.rate.exception.RateException;
import jp.co.nttdata.rate.fms.calculate.DefaultCalculateContext;
import jp.co.nttdata.rate.fms.calculate.ICalculateContext;
import jp.co.nttdata.rate.fms.core.Sequence;
import jp.co.nttdata.rate.fms.core.Token;
import jp.co.nttdata.rate.log.LogFactory;
import jp.co.nttdata.rate.model.RateCalculateSupport;
import jp.co.nttdata.rate.model.formula.Formula;
import jp.co.nttdata.rate.model.formula.FormulaManager;
import jp.co.nttdata.rate.model.rateKey.RateKey;
import jp.co.nttdata.rate.util.Const;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ExtendedModifyEvent;
import org.eclipse.swt.custom.ExtendedModifyListener;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;

/**
 * レート計算の共通コンポジット、商品毎に入出力項目が追加・削除可能
 * <br>（RateInputクラスにレートキーにUIのアノテーションを追加することで画面入力項目が変える）
 * <br>保険料計算、積立金解約返戻金計算両方とも使える
 * @author btchoukug
 *
 */
public class RateCalculateComposite extends Composite {
	
	private static Logger logger = LogFactory.getInstance(RateCalculateComposite.class);
	
	FormulaManager formulaMgr;
	
	/**レートキー入力項目のコンポジット*/
	RateInputComposite composite_input;
		
	/**計算カテゴリ*/
	String cate;
		
	/**計算対象コンボ*/
	Group calculateGroup;
	Combo calFormulaCmb;
	List<Formula> calculableFormulaList;
	
	/**操作ボタンエリア*/
	Button btn_OL;
	Button btn_clear;
	
	/**計算結果エリア*/
	Text textarea_result;

	private ExecutorService executorService;
	
	/**カレント選択された公式*/
	private Formula curFormula;
	
	/**
	 * 
	 * @param rcs
	 * @param composite
	 * @param cate
	 * @param keys
	 * @throws RateException
	 * @throws FmsDefErrorException 
	 */
	public RateCalculateComposite(final RateCalculateSupport rcs, Composite composite, String cate, List<RateKey> keys) {
		
		super(composite, SWT.NONE);
		this.setLayout(new GridLayout(1,false));
		
		//レートキー入力項目のコンポジットを初期化
		composite_input = new RateInputComposite(keys, this, SWT.NONE);
		
		this.cate = cate;
		//該当カテゴリに応じて計算対象リストを取得
		formulaMgr = rcs.getFormulaManager();
		this.calculableFormulaList = formulaMgr.getAccessableFormulaList(cate);		
		_createCalculateGroup(rcs);
				
		//計算結果テキストエリアを初期化
		_createResultGroup();
		
		composite_input.pack();

	}
	
	private void _createCalculateGroup(final RateCalculateSupport rcs) {
		//計算ボタンを初期化		
		calculateGroup = new Group(this, SWT.NONE);
		calculateGroup.setText("計算対象設定");
		GridLayout gl = new GridLayout(3,false);
		gl.horizontalSpacing = 16;
		calculateGroup.setLayout(gl);
		calculateGroup.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_FILL));
				
		//計算対象コンボを初期化
		calFormulaCmb = new Combo(calculateGroup, SWT.NONE);
		calFormulaCmb.setItems(_toFormulaArray(calculableFormulaList));
		calFormulaCmb.select(0);
		
		executorService = Executors.newSingleThreadExecutor();
		
		{
			btn_OL = new Button(calculateGroup, SWT.NONE);
			btn_OL.addMouseListener(new MouseAdapter() {

				@Override
				public void mouseUp(MouseEvent arg0) {

						//まず、結果テキストの中身をクリア
						textarea_result.setText("");
						//レート計算自身はUI以外のthreadでやる
						//executorService.execute(new Runnable(){
						Display.getCurrent().asyncExec(new Runnable(){

							@Override
							public void run() {

								try {
									//画面から入力キーを編集
									final Map<String, Object> input = composite_input.getInputedRateKeyValues(true);
									curFormula = _getCalculateFormula();
									_displayResult(rcs.calculate(input, curFormula));
									
								} catch (Exception ex) {
									
									//MSGBOXでエラーを出す			
									MessageBox msgbox = new MessageBox(Display.getCurrent().getActiveShell(), SWT.ICON_WARNING);
									
									if (ex instanceof RateException) {
										RateException re = (RateException)ex; 
										msgbox.setText("入力エラー");
										msgbox.setMessage(re.getErrorMessage() == null ? re.getMessage() : re.getErrorMessage());
										if (msgbox.open() == SWT.OK) {
											if (re.getErrorItems() != null) {
												composite_input.getInputItemByName(re.getErrorItems()[0]).setFocus();
											}
										}
									} else if (ex instanceof FmsDefErrorException) {
										FmsDefErrorException fde = (FmsDefErrorException)ex;
										msgbox.setText("定義エラー");
										msgbox.setMessage(fde.getMessage());
										msgbox.open();
									} else {
										;
									}
									
									//ログ出力
									logger.error(ex.getMessage(), ex);

								}
							}
							
						});
						
						executorService.shutdown();
						
				}
				
			});
			btn_OL.setText("OL計算");				
		}

		{
			btn_clear = new Button(calculateGroup, SWT.NONE);
			btn_clear.addMouseListener(new MouseAdapter() {
				//レートキーの入力値をクリアする
				public void mouseUp(MouseEvent arg0) {
					composite_input.clearAll();
				}
			});
			btn_clear.setText("入力クリア");				
		}
	}
	
	/**
	 * 指定公式よりサブ公式をツリーの形で展開する
	 * @param formulaTree
	 * @param curTreeItem
	 * @param f
	 */
	private void _expandFormula(final Tree formulaTree, TreeItem curTreeItem, Formula f) {
		
		List<Formula> subFormulaList = formulaMgr.getSubFormulas(f);
		if (subFormulaList == null) return;
		
	    for (int i = 0; i < subFormulaList.size(); i++) {
	    	TreeItem item;
	    	if (formulaTree != null) {
	    		item = new TreeItem(formulaTree, SWT.NONE);
	    	} else if (curTreeItem != null) {
	    		item = new TreeItem(curTreeItem, SWT.NONE);
	    	} else {
	    		throw new IllegalArgumentException("サブ計算式のツリーは展開失敗しました。");
	    	}
	    	
	    	Formula subFormula = subFormulaList.get(i);
			item.setText(subFormula.toString());
			item.setData(subFormula);
			
			//次の階層を展開
			_expandFormula(null, item, subFormula);
	    }
	    
	}

	private void _setTreeCheck(TreeItem item, boolean isChecked) {
		item.setChecked(isChecked);
		TreeItem[] subItems = item.getItems();
		if (subItems != null) {
    		for (int i = 0; i < subItems.length; i++) {
    			_setTreeCheck(subItems[i], isChecked);
    		}
		}
	}
	
	/**
	 * 指定公式の下のサブ公式をツリー形で表示して、選択されたサブ公式のリストを返す
	 */
	private List<Formula> _createSubFormulaSelectionDialog() {
		
		final List<Formula> selectedSubFormulaList = new ArrayList<Formula>();
		
	    final Shell dialog = new Shell(this.getShell(),
		        SWT.DIALOG_TRIM | SWT.APPLICATION_MODAL | SWT.RESIZE);
	    dialog.setText("計算対象の直下のサブ公式リスト");
	    dialog.setLayout(new GridLayout(2, false));
	    GridData dialogData = new GridData(SWT.FILL, SWT.FILL, false, true, 1, 1);
	    dialogData.widthHint = 500;
	    dialogData.heightHint = 400;
	    dialog.setLayoutData(dialogData);
	    
	    final Tree formulaTree = new Tree(dialog, SWT.CHECK);
	    GridData treeData = new GridData(GridData.FILL_BOTH);
	    treeData.verticalSpan = 2;//２つセルのサイズを設定
	    treeData.widthHint = 500;
	    treeData.heightHint = 320;
	    formulaTree.setLayoutData(treeData);
	    
		//カレント選択された計算対象の公式
		Formula f = _getCalculateFormula();
	    
	    _expandFormula(formulaTree, null, f);
	    //ツリー全体のサイズ再計算
	    formulaTree.layout();
	   	
	    
	    //ボタン「すべて選択」
	    Button selectAll = new Button(dialog, SWT.PUSH);
	    selectAll.setText("すべて選択");
	    selectAll.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_FILL));
	    selectAll.addSelectionListener(new SelectionAdapter() {
	    	
	    	public void widgetSelected(SelectionEvent e) {
	    		TreeItem[] items = formulaTree.getItems();
	    		for (int i = 0; i < items.length; i++) {
	    			_setTreeCheck(items[i], true);
	    		}
	    	}
	    	
	    });
	    
	    //ボタン「すべて選択解除」
	    Button deselectAll = new Button(dialog, SWT.PUSH);
	    deselectAll.setText("選択解除");
	    deselectAll.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_FILL
	        | GridData.VERTICAL_ALIGN_BEGINNING));
	    deselectAll.addSelectionListener(new SelectionAdapter() {
	    	
	    	public void widgetSelected(SelectionEvent e) {
	    		TreeItem[] items = formulaTree.getItems();
	    		for (int i = 0; i < items.length; i++) {
	    			_setTreeCheck(items[i], false);
	    		}	    		
	    	}
	    	
	    });
	    
	    final Label formulaInfoLabel = new Label(dialog, SWT.NONE);
	    formulaInfoLabel.setText("算式基本情報：");
	    
	    new Label(dialog, SWT.NONE);
	    
	    final Text formulaInfoText = new Text(dialog, SWT.BORDER | SWT.MULTI
		        | SWT.V_SCROLL | SWT.H_SCROLL | SWT.WRAP);
		GridData gd_formulaInfoText = new GridData(GridData.FILL_BOTH);
	    gd_formulaInfoText.widthHint = 500;
	    gd_formulaInfoText.heightHint = 60;	    
	    formulaInfoText.setLayoutData(gd_formulaInfoText);
	    
	    new Label(dialog, SWT.NONE);
	    
	    //サブ公式を選択次第、サブ公式のボディを表示する
	    final Label formulaBodyLabel = new Label(dialog, SWT.NONE);
	    formulaBodyLabel.setText("算式ボディ：");
	    new Label(dialog, SWT.NONE);
	    final StyledText styledBodyText = new StyledText(dialog, SWT.BORDER | SWT.MULTI
		        | SWT.V_SCROLL | SWT.H_SCROLL | SWT.WRAP);

	    //サイズ設定
		GridData gd_formulaBodyText = new GridData(GridData.FILL_BOTH);
		gd_formulaBodyText.widthHint = 500;
		gd_formulaBodyText.heightHint = 120;	    
	    styledBodyText.setLayoutData(gd_formulaBodyText);
	    
	    //TODO highlighting keyword and foundation mark(array)
	    
	    final ICalculateContext ctx = new DefaultCalculateContext();
	    ctx.setFormulaManager(formulaMgr);
	    styledBodyText.addExtendedModifyListener(new ExtendedModifyListener() {

	    	@Override
			public void modifyText(ExtendedModifyEvent event) {
				
	    		int end = event.start + event.length - 1;				
	    		if (event.start <= end) {
					
					List<StyleRange> ranges = new ArrayList<StyleRange>();
					
					String text = styledBodyText.getText(event.start, end);
					Sequence seq = ctx.getParser().parse(StringUtils.deleteWhitespace(text));
					int startPos = 0;
					
					for (int i = 0, len = seq.size(); i < len ; i++) {
						Token t = seq.get(i);
						String textToken = t.toString();
						int position = StringUtils.indexOf(text, t.toString(), startPos);
						int curLength = textToken.length();
						startPos += curLength;
						
						Color color = dialog.getDisplay().getSystemColor(SWT.COLOR_BLACK);
						int font = SWT.NORMAL;
						
						if (t.isKeyword()) {
							color = dialog.getDisplay().getSystemColor(SWT.COLOR_DARK_RED);
							font = SWT.BOLD;
						} else if (t.isVariable() && formulaMgr.isExist(t.toVariable().getName())) {
							color = dialog.getDisplay().getSystemColor(SWT.COLOR_BLUE);	
						} else if (t.isArray()) {
							color = dialog.getDisplay().getSystemColor(SWT.COLOR_DARK_GREEN);
							font = SWT.BOLD;
						} else {
							;
						}
						
						ranges.add(new StyleRange(position, curLength, color, null, font));
					}

					if (!ranges.isEmpty()) {
						styledBodyText.setStyleRanges(ranges.toArray(new StyleRange[0]));
					}
				}
			}
	    });
	    
		formulaTree.addSelectionListener(new SelectionAdapter() {
	    	public void widgetSelected(SelectionEvent e) {
	    		Formula curFormula = (Formula) formulaTree.getSelection()[0].getData();
	    		if (curFormula == null) return;
	    		String body = curFormula.getBody();
	    		
	    		//TODO 画面は別々項目として表示して、編集＆保存可能にする？
	    		//追加ボタンも用意する
	    		StringBuffer formulaInfo = new StringBuffer("名称：")
	    		.append(curFormula.getName())
	    		.append("\n");
	    			    		
	    		if (curFormula.getParas() != null) {
	    			formulaInfo.append("パラメータ：");
	    			for (String para : curFormula.getParas()) {
	    				formulaInfo.append(para).append(Const.COMMA);
	    			}
	    			formulaInfo.deleteCharAt(formulaInfo.length() - 1);
	    			formulaInfo.append("\n");	
	    		}
	    			    		
	    		if (curFormula.getFraction() >= 0) {
	    			formulaInfo.append("端数処理：" + curFormula.getFraction()).append("\n");
	    		}
	    		
	    		if (StringUtils.isNotEmpty(curFormula.getPvh())) {
	    			formulaInfo.append("計算基礎：" + curFormula.getPvh());
	    		}

	    		formulaInfoText.setText(formulaInfo.toString());
	    		//TODO add indent
	    		body = body == null ? String.valueOf(curFormula.getValue()) : body;
//	    		Sequence seq = parser.parse(body);
//	    		styledText.setText(seq.getIndentedBody());	    		
	    		styledBodyText.setText(body);
	    	}
		});
	    
	    Button ok = new Button(dialog, SWT.PUSH);
	    ok.setText("OK");
	    dialog.setDefaultButton(ok);
	    ok.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_FILL));
	    ok.addSelectionListener(new SelectionAdapter() {
	    	
			public void widgetSelected(SelectionEvent e) {				
				//選択した公式の名をメイン画面に返す
	    		for (TreeItem item : formulaTree.getItems()) {
	    			_getNextLevelCheckedItems(item);
	    		}
				dialog.dispose();
			}
			
			private void _getNextLevelCheckedItems(TreeItem item) {
				if (item.getChecked()) {
					selectedSubFormulaList.add((Formula) item.getData());
				}				
				if (item.getItemCount() > 0) {
					for (TreeItem subItem : item.getItems()) {
						_getNextLevelCheckedItems(subItem);
					}
				}
			}
			
	    });
	    
	    dialog.pack();
	    dialog.open();
	    while (!dialog.isDisposed()) {
	    	if (!dialog.getDisplay().readAndDispatch())
	    		dialog.getDisplay().sleep();
	    }
	    
	    return selectedSubFormulaList;
	}
	
	private void _createResultGroup() {
		
	    Group resultGroup = new Group(this, SWT.NONE);
	    resultGroup.setLayout(new GridLayout(4, false));
	    resultGroup.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, true, 3, 1));
	    resultGroup.setText("計算結果");

	    Button listenersButton = new Button(resultGroup, SWT.PUSH);
	    listenersButton.setText("サブ公式フィルター");

	    final Label roundMode_label = new Label(resultGroup, SWT.NONE);
	    roundMode_label.setText("端数処理入れ");
	    final Button roundMode_Chkbox = new Button(resultGroup, SWT.CHECK);
	    roundMode_Chkbox.setSelection(true);
	    roundMode_Chkbox.addSelectionListener(new SelectionAdapter() {
	    	public void widgetSelected(SelectionEvent e) {
	    		System.out.println("サブ公式には端数処理を行う");
	    		formulaMgr.setRounding(roundMode_Chkbox.getSelection());
	    	}
	    });
//	    final Text subFormulaText = new Text(resultGroup, SWT.NONE | SWT.WRAP);
//	    GridData dataSf = new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING);
//	    dataSf.widthHint = 220;
//	    dataSf.heightHint = 20;
//	    subFormulaText.setLayoutData(dataSf);
//	    subFormulaText.setText("選択したサブ公式");
	    
	    listenersButton.addSelectionListener(new SelectionAdapter() {
	    	public void widgetSelected(SelectionEvent e) {
	    		//公式マネージャにセットする
	    	    //TODO 途中値とするvar或いはfuncを計算したら、選択されたサブ公式リストより計算値をいったん保存する
	    	    //最終レート値と一緒に画面上に出力する
	    		List<Formula> subFormulaList = _createSubFormulaSelectionDialog();
	    		formulaMgr.setFormulaFilterList(subFormulaList);
	    		
	    	}
	    });

	    Button clearButton = new Button(resultGroup, SWT.PUSH);
	    clearButton.setText("結果クリア");
	    clearButton.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_END));
	    clearButton.addSelectionListener(new SelectionAdapter() {
	      public void widgetSelected(SelectionEvent e) {
	    	  textarea_result.setText("");
	      }
	    });
	    
	    textarea_result = new Text(resultGroup, SWT.BORDER | SWT.MULTI
	        | SWT.V_SCROLL | SWT.H_SCROLL);
	    GridData data = new GridData(GridData.FILL_BOTH);
	    data.horizontalSpan = 4;
	    data.heightHint = 50;
	    //textarea_result.setEditable(false);
	    textarea_result.setLayoutData(data);
	    textarea_result.addKeyListener(new KeyAdapter() {
	    	//すべて選択
	    	public void keyPressed(KeyEvent e) {
	    		if ((e.keyCode == 'A' || e.keyCode == 'a')
	    				&& (e.stateMask & SWT.MOD1) != 0) {
	    			textarea_result.selectAll();
	    			e.doit = false;
	    		}
	    	}
	    });
	}

	/**
	 * 計算対象コンボリストを編集
	 * @return
	 */
	private String[] _toFormulaArray(List<Formula> formulaList) {
		String[] formulas = new String[formulaList.size()];
		int i = 0;
		for (Formula calFormula : formulaList) {
			formulas[i] = calFormula.toString();
			i++;
		}
		
		return formulas;
	}
	
	/**
	 * 選択した計算対象の公式を返す
	 * @return
	 */
	private Formula _getCalculateFormula() {
		int selectedIndex = calFormulaCmb.getSelectionIndex();
		if (selectedIndex < 0) {
			return null;
		}
		
		return this.calculableFormulaList.get(selectedIndex);
		
	}
	
		
	/**
	 * 計算終了したら、業務モジュールから渡せたイベントの中から
	 * 計算結果を取得したうえUI上に表示する
	 * @param result
	 */
	private void _displayResult(Map<String, Double> result) {
		
		StringBuffer sb = new StringBuffer();
		
		String curFormulaText = curFormula.toString();
		Double output_ = result.get(curFormulaText);
		String output;
		//PVW改善事項No.33：延長期間・年月は「10年04月」の形にフォーマットした
		if (curFormula.getName().equals("extend_tf")) {
			//3年02月は0302に変換(４桁)
			output = StringUtils.leftPad(String.valueOf(output_.intValue()), 4, '0');
		} else {
			output = output_.toString();
		}
		
		//画面から選択した計算対象を先頭に出力する		
		sb.append(curFormulaText).append("=")
		.append(output).append("\t\n");
		result.remove(curFormulaText);
		
		//サブ公式を出力時にキーでソート
		List<String> valueNames = new ArrayList<String>();
		valueNames.addAll(result.keySet());
		Collections.sort(valueNames);
		
		for (String name : valueNames) {
			sb.append(name).append("=")
			.append(result.get(name));
			sb.append("\t\n");
		}
				
		this.textarea_result.setText(sb.toString());
	}
	
}
