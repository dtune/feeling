package jp.co.nttdata.rate.ui.view;

import java.awt.Desktop;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import jp.co.nttdata.rate.batch.Batch;
import jp.co.nttdata.rate.batch.BatchInputAdapter;
import jp.co.nttdata.rate.batch.CsvFileWriterImpl;
import jp.co.nttdata.rate.batch.CsvRateKeyReaderImpl;
import jp.co.nttdata.rate.batch.DatRateKeyReaderImpl;
import jp.co.nttdata.rate.batch.IBatchWriter;
import jp.co.nttdata.rate.batch.IRateKeyReader;
import jp.co.nttdata.rate.batch.dataConvert.BatchDataLayout;
import jp.co.nttdata.rate.batch.dataConvert.BatchDataLayoutFactory;
import jp.co.nttdata.rate.exception.FmsDefErrorException;
import jp.co.nttdata.rate.exception.RateException;
import jp.co.nttdata.rate.fms.calculate.CacheManagerSupport;
import jp.co.nttdata.rate.log.LogFactory;
import jp.co.nttdata.rate.model.CalculateCategory;
import jp.co.nttdata.rate.model.CategoryManager;
import jp.co.nttdata.rate.model.formula.Formula;
import jp.co.nttdata.rate.model.formula.FormulaManager;
import jp.co.nttdata.rate.model.rateKey.RateKey;
import jp.co.nttdata.rate.rateFundation.RateFundationManager;
import jp.co.nttdata.rate.util.CommonUtil;
import jp.co.nttdata.rate.util.Const;
import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.statistics.LiveCacheStatistics;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.time.DateFormatUtils;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CLabel;
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
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
/**
 * 
 * B/T計算のUIフォームである
 * 
 * @author btchoukug
 * 
 */
public class RateCalculateBTForm {

	private static final String BT_LOGGER = "BTLogger";
	private Logger logger = LogFactory.getInstance(BT_LOGGER);
	
	/** 画面タイトル */
	private static final String title = "数理レート検証ツールBT";
	/** 日付フォーマットyyyyMMdd */
	private static final String YYYYMMDD = "yyyyMMdd";
	
	/** 検証結果を格納するテンプレート */
	private String logFilePath = "log\\上海側作業の検証結果.xls";
	private String logFolderPath = "log";

	Display display;
	Shell shell;
	
	// ================画面アイテム　 ここから================//
	Composite code_comp;
	Label name_label;
	
	/** 計算カテゴリコンポジット */
	Composite cate_comp;
	/**BT実行の最大件数*/
	Text maxLine_text;
	/**BT実行の固定値設定テキスト*/
	Text fixedVal_text;
	
	/** BT進捗情報グループ */
	Group progressInfoGroup;
	BatchTaskTableView tblTask;
	
	/** データファイル操作コンポジット */
	Composite bt_comp;
	Button btnAddFile;
	Button btnAddFolder;
	Button btnRemoveFile;
	
	/**コンペアアリアのボタン＆テキスト（普通体）*/
	CompareSettingGroup normalCompareGroup;
	/**コンペアアリアのボタン＆テキスト（特体）*/
	SpecialCompareSettingGroup specialCompareGroup;
		
	Composite bt_start;
	/** BT計算開始ボタン */
	Button startBtn;
	
	Button cacheEnableBtn;
	Button ngOnlyBtn;
	Button shutdownBtn;
	
	/** ログUI */
	RateCalculateLogComposite logComp;
	/**ロググループ*/
	Group logGroup;	
	
	// ================BT計算モジュール　ここから================//
	/** 商品コード */
	String code;
	/** 商品名 */
	String name;
	/** 特体選択かどうか */
	static boolean isSpecialMode = false;
	/** カレント計算カテゴリ */
	String cate = CalculateCategory.P; // デフォルトとして「保険料」
	
	/** バッチ計算モジュール */
	Batch bt;
	BatchDataLayoutFactory factory;
	BatchDataLayout dataLayout;
	IRateKeyReader reader;
	IBatchWriter writer;

	/** 指定のレートキーの固定値（複数件の場合、コンマで分割） */
	String fixedRateKeyValueText;
	Map<String, Object> fixedValues;

	Map<String, String> compareMapping;
	Map<String, String> specialCompareMapping;

	/** タスク進捗情報リスト */
	List<ProgressInfo> listProgressInfo = new ArrayList<ProgressInfo>();
	/** 進捗更新 */
	UpdateProgressAction updateProgressAction;
	/** 更新用の進捗情報Bean */
	ProgressInfo progressInfo;
	/** タスクリストに格納されるファイルの数 */
	int fileCount = 0;

	/** 画面から指定の最大計算行数 */
	long specifyLineCount = 0;

	/** 比較元の項目（インプットデータのヘッダ・表示用） */
	String[] inputDataHeaders;
	/** 比較元の項目（インプットデータのヘッダ・計算用） */
	String[] inputRateKeyNames;

	/** 比較先の項目（計算可の公式） */
	String[] calculableFormulas;
	List<Formula> currentFormulas;

	/** 選択された計算対象の公式 */
	int selectedFormulaIndex = 0;

	boolean cacheEnable = true;
	boolean ngOnly = false;
	boolean isShutdownAfterExec = false;

	FormulaManager formulaMgr;
	RateFundationManager fundMgr;
	
	/**
	 * BTフォーム初期化 
	 * @param code 保障のフラグも含める
	 * @throws FmsDefErrorException 
	 * @throws FmsDefErrorException 
	 */
	public RateCalculateBTForm(String code) throws FmsDefErrorException {
		this.code = code.substring(0, 3);
		
		//基数と公式のマネージメントは1回しか初期化しない
		formulaMgr = new FormulaManager(code);	
		
		_loadCurrentCalculateInfo(this.code, CalculateCategory.P);
		this.bt = new Batch(code, cacheEnable, formulaMgr);
	}

	/**
	 * 
	 * 固定値指定のダイアログを作る 固定値を指定したレートキーに対して、 key-value ペアの形（コンマ付）の文字列を返す
	 * 
	 * @return
	 * @throws RateException 
	 * 
	 */
	@SuppressWarnings("unchecked")
	private String _createFixedValueInputDialog() throws RateException {

		final Shell dialog = new Shell(shell, SWT.DIALOG_TRIM
				| SWT.APPLICATION_MODAL);
		dialog.setText("BT計算用レートキーに固定値を指定");
		dialog.setLayout(new GridLayout(2, false));
		GridData dialogData = new GridData(SWT.FILL, SWT.FILL, false, true, 1,
				1);
		dialogData.widthHint = 320;
		dialogData.heightHint = 360;
		dialog.setLayoutData(dialogData);

		// カレントのカテゴリに応じて、レートキー入力項目のコンポジットを初期化
		List<RateKey> keys = CategoryManager.getInstance().getCateInfo(cate).getKeyList();
				
		// 固定値テキストに内容がある場合、その内容を元に編集を行う
		fixedValues = CommonUtil.JSONString2Map(fixedVal_text.getText(), Integer.class);
		
		final RateInputComposite fixedValueInput;
		if (fixedValues == null) {
			fixedValueInput = new RateInputComposite(keys, dialog, SWT.NONE);
			// 初期値をクリア
			fixedValueInput.clearAll();
			
		} else {
			fixedValueInput = new RateInputComposite(keys, fixedValues,
					dialog, SWT.NONE);
		}
		
		GridData inputData = new GridData();
		inputData.horizontalSpan = 2;
		fixedValueInput.setLayoutData(inputData);

		Button ok = new Button(dialog, SWT.PUSH);
		ok.setText("決定");
		dialog.setDefaultButton(ok);
		GridData okData = new GridData(GridData.HORIZONTAL_ALIGN_END);
		okData.widthHint = 60;
		ok.setLayoutData(okData);
		ok.addSelectionListener(new SelectionAdapter() {

			public void widgetSelected(SelectionEvent e) {

				try {
					// 固定値指定の項目を編集（コンマ区切り）
					fixedValues = fixedValueInput
							.getInputedRateKeyValues(false);
					String fixedValueText = fixedValues.toString().replaceAll(
							"\\{", "").replaceAll("\\}", "").replaceAll(", ",
							";");
					fixedVal_text.setText(fixedValueText);

					dialog.dispose();
				} catch (RateException ex) {

					logger.error("固定値指定エラー", ex);

					MessageBox msgBox = new MessageBox(shell, SWT.ERROR
							| SWT.OK);
					msgBox.setText("エラー");
					msgBox.setMessage("固定値指定エラー:" + ex.getErrorMessage());
					msgBox.open();

				}
			}
		});

		Button clr = new Button(dialog, SWT.PUSH);
		clr.setText("クリア");
		GridData clrData = new GridData(GridData.HORIZONTAL_ALIGN_END);
		clrData.widthHint = 60;
		clrData.horizontalIndent = 20;
		clr.setLayoutData(clrData);
		clr.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseUp(MouseEvent arg0) {
				fixedValueInput.clearAll();
			}
		});

		dialog.pack();
		CommonUtil.setShellLocation(dialog);
		dialog.open();
		while (!dialog.isDisposed()) {
			if (!dialog.getDisplay().readAndDispatch())
				dialog.getDisplay().sleep();
		}

		return "";
	}

	private void _processInputData(final String inputDataFilePath) {
		if (inputDataFilePath != null) {
			
			_getfile(inputDataFilePath);

			// 選択したファイルを読み込んで、比較元の配列を編集
			display.asyncExec(new Runnable() {

				@Override
				public void run() {

					try {
						IRateKeyReader reader = null;

						if (inputDataFilePath.endsWith(".csv")) {
							reader = new CsvRateKeyReaderImpl(inputDataFilePath);
							// ロードファイルのヘッダを取得
							inputDataHeaders = reader.getKeyNames();
							inputRateKeyNames = reader.getKeyNames();
							reader.close();

						} else {
							// .datファイル或いはフォルダの場合
							dataLayout = factory.getDataLayout(code, cate);
							inputRateKeyNames = dataLayout.getKeyNames();
							inputDataHeaders = dataLayout.getKeyDescs();

						}

						// フォルダの場合、計算最大件数の設定無効にする
						if (new File(inputDataFilePath).isDirectory()) {
							maxLine_text.setText("0");
							maxLine_text.setEditable(false);
						} else {
							maxLine_text.setEditable(true);
						}

						// 比較元の選択ボタンを有効にする
						normalCompareGroup.setButtonEnabled();
						specialCompareGroup.setButtonEnabled();

						// プログレスバーなどの状態をクリア
						startBtn.setText("BT計算開始");

					} catch (Exception e) {
						logger.error("比較元を選択するときにエラー：", e);
						e.printStackTrace();
					}
				}
			});
		}
	}


	
	/**
	 * Open the window
	 */
	public void open() {

		display = Display.getDefault();
		shell = new Shell(display, SWT.MIN | SWT.CLOSE);
		shell.setLayout(new RowLayout(SWT.VERTICAL));
		shell.setSize(600, 600);
		shell.setText(title);

		{
			// 計算対象商品のコードを入力
			code_comp = new Composite(shell, SWT.NONE);
			code_comp.setLayout(new RowLayout(SWT.HORIZONTAL));
			CLabel code_label = new CLabel(code_comp, SWT.SHADOW_NONE);
			code_label.setText("対象商品：");
			
			name_label = new Label(code_comp, SWT.BORDER | SWT.SHADOW_NONE);
			name_label.setText(InsuranceSelectDialog.insuranceName);
			
			RowData rdText = new RowData();
			rdText.width = 320;
			rdText.height = 20;
			name_label.setLayoutData(rdText);

			// 計算対象変更ボタン（商品選択）
			Button select_btn = new Button(code_comp, SWT.NONE);
			select_btn.setText("計算対象変更");
			select_btn.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent arg0) {
					TypeDialog typeDialog = new TypeDialog();
					String type = typeDialog.open();
					if (type != null) {
						InsuranceSelectDialog dialog = new InsuranceSelectDialog(
								shell, type);
						String newCode = dialog.open();

						if (newCode != null) {

							code = newCode;
							name_label.setText(InsuranceSelectDialog.insuranceName);

							// 選んだ商品より、定義情報などを再ロード
							LoadingShell loadShell = new LoadingShell();
							loadShell.loadBatchContext(code);

							_loadCurrentCalculateInfo(code, cate);

							// バッチ自身初期化
							try {
								bt = new Batch(code, cacheEnable, formulaMgr);
							} catch (FmsDefErrorException e) {
								MessageBox msgBox = new MessageBox(shell, SWT.ICON_ERROR | SWT.OK);
								msgBox.setMessage(e.getMessage());
								msgBox.open();
							}
							code_comp.pack();
						}
					}

				}
			});

			
			Group input_grp = new Group(shell, SWT.NONE);
			RowLayout rl = new RowLayout(SWT.VERTICAL);
			rl.spacing = 10;
			RowData rd = new RowData();
			rd.width = 555;
			input_grp.setLayout(rl);
			input_grp.setLayoutData(rd);
			input_grp.setText("BT計算入力設定");
			

			// 計算カテゴリ選択
			cate_comp = new Composite(input_grp, SWT.NONE);
			cate_comp.setLayout(new RowLayout(SWT.HORIZONTAL));
			Label cate_label = new Label(cate_comp, SWT.NONE);
			cate_label.setText("計算カテゴリ：");
			for (CalculateCategory cate : CategoryManager.getInstance()
					.getCateInfos()) {
				Button cate_btn = new Button(cate_comp, SWT.RADIO);
				cate_btn.setText(cate.getLabel());
				cate_btn.setData("cate", cate.getName());
				if (CalculateCategory.P.equals(cate.getName())) {
					// デフォルト場合、Prateを選択
					cate_btn.setSelection(true);
				}
				cate_btn.addListener(SWT.MouseUp,
						new LoadCalculableFormulaAction());
			}

			// 計算可件数
			Composite maxLine_comp = new Composite(input_grp, SWT.NONE);
			maxLine_comp.setLayout(new RowLayout(SWT.HORIZONTAL));
			Label maxLine_label = new Label(maxLine_comp, SWT.NONE);
			maxLine_label.setText("最大計算件数(0：制限なし)：");
			maxLine_text = new Text(maxLine_comp, SWT.BORDER);
			RowData rd_maxLine = new RowData();
			rd_maxLine.width = 40;
			rd_maxLine.height = 12;
			maxLine_text.setLayoutData(rd_maxLine);
			maxLine_text.setText("10000");

			// レートキーの固定値を指定
			Composite fixedVal_comp = new Composite(input_grp, SWT.NONE);
			fixedVal_comp.setLayout(new RowLayout(SWT.HORIZONTAL));
			Label fixedVal_label = new Label(fixedVal_comp, SWT.NONE);
			fixedVal_label.setText("レートキーの固定値を指定：\n（例：gen=4;sptate=0）");
			fixedVal_text = new Text(fixedVal_comp, SWT.BORDER);
			//fixedVal_text.setText("gen=5;sptate=0;t=0");
			RowData rd_fixedVal_text = new RowData();
			rd_fixedVal_text.width = 280;
			rd_fixedVal_text.height = 12;
			fixedVal_text.setLayoutData(rd_fixedVal_text);
			
			Button fixedVal_btn = new Button(fixedVal_comp, SWT.NONE);
			fixedVal_btn.setText("画面参照で指定");
			fixedVal_btn.addMouseListener(new MouseAdapter() {
				@Override
				public void mouseUp(MouseEvent arg0) {
					try {
						_createFixedValueInputDialog();
					} catch (RateException ex) {
						logger.error("固定値指定エラー", ex);

						MessageBox msgBox = new MessageBox(shell, SWT.ERROR
								| SWT.OK);
						msgBox.setText("エラー");
						msgBox.setMessage("固定値指定エラー:" + ex.getErrorMessage());
						msgBox.open();
					}
				}
			});

			// Create a group for the progress bar
			progressInfoGroup = new Group(shell, SWT.NONE);
			progressInfoGroup.setLayout(new RowLayout(SWT.VERTICAL));
			progressInfoGroup.setLayoutData(rd);
			progressInfoGroup.setText("BT計算");
			
			bt_comp = new Composite(progressInfoGroup, SWT.NONE);
			bt_comp.setLayout(new RowLayout(SWT.HORIZONTAL));
			
			RowData start_rd = new RowData();
			start_rd.width = 100;
			
			// フォルダ追加ボタン
			btnAddFolder = new Button(bt_comp, SWT.NONE);
			btnAddFolder.setLayoutData(start_rd);
			btnAddFolder.setText("フォルダ追加");

			btnAddFolder.addMouseListener(new MouseAdapter() {

				@Override
				public void mouseUp(MouseEvent arg0) {
					DirectoryDialog dirDialog = new DirectoryDialog(shell);
					final String dirPath = dirDialog.open();
					if (dirPath != null) {
						// input_filePath.setText(dirPath);
						_processInputData(dirPath);
					}
				}

			});

			// ファイル追加ボタン
			btnAddFile = new Button(bt_comp, SWT.NONE);
			btnAddFile.setLayoutData(start_rd);
			btnAddFile.setText("ファイル追加");
			class FileUpAction extends MouseAdapter {
				@Override
				public void mouseUp(MouseEvent arg0) {
					FileDialog fd = new FileDialog(shell, SWT.NORMAL);

					// デフォルト場合、csvファイルを取り扱う dataファイルも扱う
					fd.setFilterExtensions(new String[] { "*.dat", "*.csv",
							"*.txt", "*.*" });
					// 選択ファイルのパスを取得
					final String inputDataFilePath = fd.open();
					// TODO 修正
					_processInputData(inputDataFilePath);

				}
			}
			btnAddFile.addMouseListener(new FileUpAction());

			// ファイル追加ボタン
			btnRemoveFile = new Button(bt_comp, SWT.NONE);
			btnRemoveFile.setLayoutData(start_rd);
			btnRemoveFile.setText("ファイル削除");
			class FileRemoveAction extends MouseAdapter {
				@Override
				public void mouseUp(MouseEvent arg0) {
					int[] index = tblTask.itemRemove(shell);
					for (int i = index.length - 1; i >= 0; i--) {
						listProgressInfo.remove(index[i]);
						if (fileCount >= index[i] + 1) {
							fileCount --;
						}
					}

					if (listProgressInfo.size() == 0) {
						normalCompareGroup.setButtonEnabled();
						normalCompareGroup.clearTextValue();
						specialCompareGroup.setButtonEnabled();
						specialCompareGroup.clearTextValue();
					}
				}
			}
			btnRemoveFile.addMouseListener(new FileRemoveAction());

			tblTask = new BatchTaskTableView(progressInfoGroup);

			// 計算結果出力先
			Composite output_comp = new Composite(progressInfoGroup, SWT.NONE);
			output_comp.setLayout(new RowLayout(SWT.HORIZONTAL));
			Button btnExport = new Button(output_comp, SWT.NONE);
			btnExport.setText("エクスポート");
			btnExport.setLayoutData(start_rd);

			Composite compareObjsComp = new Composite(progressInfoGroup, SWT.NONE);
			compareObjsComp.setLayout(new RowLayout(SWT.HORIZONTAL));		
			
			// 普通体コンペアグループここから
			normalCompareGroup = new CompareSettingGroup(compareObjsComp, SWT.NONE);
			normalCompareGroup.setLayout(new RowLayout(SWT.VERTICAL));
			RowData compareGrpRd = new RowData();
			compareGrpRd.width = 240;
			normalCompareGroup.setLayoutData(compareGrpRd);
			normalCompareGroup.setText("標準体コンペア");
			normalCompareGroup.setCalculableFormulas(calculableFormulas);
			normalCompareGroup.setInputDataHeaders(inputDataHeaders);
			normalCompareGroup.setInputRateKeyNames(inputRateKeyNames);
			
			// 特体コンペアグループここから
			specialCompareGroup = new SpecialCompareSettingGroup(compareObjsComp, SWT.NONE);
			specialCompareGroup.setLayout(new RowLayout(SWT.HORIZONTAL));
			RowData specialCompareGrpRd = new RowData();
			specialCompareGrpRd.width = 260;
			specialCompareGroup.setLayoutData(specialCompareGrpRd);
			specialCompareGroup.setText("特体コンペア");
			specialCompareGroup.setCalculableFormulas(calculableFormulas);
			specialCompareGroup.setInputDataHeaders(inputDataHeaders);
			specialCompareGroup.setInputRateKeyNames(inputRateKeyNames);
			//　特体の場合のみ表示させる
			specialCompareGroup.setVisiable(isSpecialMode);
			
			RowData rd_separator = new RowData();
			rd_separator.width = 16;
			rd_separator.height = 16;			

			// cache使用可否
			Composite cache_comp = new Composite(progressInfoGroup, SWT.NONE);
			cache_comp.setLayout(new RowLayout(SWT.HORIZONTAL));
			cacheEnableBtn = new Button(cache_comp, SWT.CHECK);
			cacheEnableBtn.setText("Cache有効");
			cacheEnableBtn.setSelection(true);

			Label separator = new Label(cache_comp, SWT.SEPARATOR);
			separator.setLayoutData(rd_separator);

			// NGケース出力のみ
			ngOnlyBtn = new Button(cache_comp, SWT.CHECK);
			ngOnlyBtn.setText("NGケースのみ出力");
			ngOnlyBtn.setSelection(false);

			Label separator2 = new Label(cache_comp, SWT.SEPARATOR);
			separator2.setLayoutData(rd_separator);

			// 計算完了シャットダウン
			Label shutdown_label = new Label(cache_comp, SWT.NONE);
			shutdownBtn = new Button(cache_comp, SWT.CHECK);
			shutdown_label.setText("計算完了シャットダウン");
			shutdownBtn.setSelection(false);

			// BT計算ボタン
			bt_start = new Composite(progressInfoGroup, SWT.NONE);
			bt_start.setLayout(new RowLayout(SWT.HORIZONTAL));

			startBtn = new Button(bt_start, SWT.NONE);
			startBtn.setLayoutData(start_rd);
			startBtn.setText("BT計算開始");

			startBtn.addMouseListener(new MouseAdapter() {

				@Override
				public void mouseUp(MouseEvent arg0) {

					if (!bt.isStarted()) {
						
						_executeBatch();
						
					} else {
						
						// 開始・停止の切り替え
						bt.stop();
						updateProgressAction.stop();
						startBtn.setText("BT計算開始");
						btnAddFile.setEnabled(true);
						btnAddFolder.setEnabled(true);
						btnRemoveFile.setEnabled(true);

					}

				}

			});

			

			class CmdExecAction extends MouseAdapter {
				public static final int FOLDER = 1;
				public static final int EXCEL = 2;

				private int mode = 0;

				public CmdExecAction(int mode) {
					this.mode = mode;
				}

				@Override
				public void mouseUp(MouseEvent arg0) {
					if (listProgressInfo.size() > 0) {

						if (FOLDER == this.mode) {
							String filePath = listProgressInfo.get(0)
									.getFileFullName();
							try {
								String cmd = "cmd /c start "
									+ filePath.substring(0, filePath.lastIndexOf(File.separator) + 1);
								Runtime.getRuntime().exec(cmd);
							} catch (IOException e) {
								e.printStackTrace();
								logger.error(filePath + "ファイルを開いては失敗した。", e);
							}

						} else {

							try {
								File logFile = new File(logFilePath);
								if (!logFile.exists()) {
									File dir = new File(logFolderPath);
									if (!dir.exists() && !dir.isDirectory()) {
										dir.mkdirs();
									}
									logFile.createNewFile();
								}

								_createCSVReport(logFile);
								Desktop.getDesktop().open(new File(logFilePath));

							} catch (IOException e) {
								logger.error("エクスポートファイル失敗！", e);
								e.printStackTrace();
							}
						}
					}
				}

			}
			btnExport.addMouseListener(new CmdExecAction(CmdExecAction.EXCEL));

			Button output_excel_open = new Button(output_comp, SWT.NONE);
			output_excel_open.setText("出力フォルダ開く");
			output_excel_open.setLayoutData(start_rd);
			output_excel_open.addMouseListener(new CmdExecAction(
					CmdExecAction.FOLDER));

			// ログアリア
			logGroup = new Group(shell, SWT.NONE);
			logGroup.setLayout(new RowLayout(SWT.VERTICAL));
			logGroup.setText("BT計算ログ");
			logComp = new RateCalculateLogComposite(logGroup);
			logComp.setSize(500, 120);

		}

		// コンボよりサイズを再計算する
		shell.pack();
		CommonUtil.setShellLocation(shell);
		shell.open();

		shell.addListener(SWT.Close, new Listener() {
			@Override
			public void handleEvent(Event ev) {

				MessageBox msgBox = new MessageBox(shell, SWT.ICON_WARNING
						| SWT.OK | SWT.CANCEL);
				msgBox.setText("検証ツールBT終了");

				String msg = "検証ツールBTを終了してよろしいですか？";
				if (bt.isStarted()) {
					msg = "検証ツールBTが実行中です、終了してよろしいですか？";
					updateProgressAction.stop();
					updateProgressAction.shutdown();
				}

				msgBox.setMessage(msg);
				if (msgBox.open() == SWT.OK) {

					// bt中止
					System.out.println("batch shutdown");
					// TODO BT windowのクローズとともに、threadをshutdown、
					bt.stop();
					bt.shutdownAndAwaitTermination();

					// caches shutdown
					System.out.println("cache shutdown");
					CacheManager.getInstance().shutdown();

					ev.doit = true;

				} else {
					// イベントのデフォルト動作をキャンセール
					ev.doit = false;
				}

			}
		});

		while (!shell.isDisposed()) {
			if (!display.readAndDispatch())
				display.sleep();
		}
		display.dispose();
	}

	/**
	 * 当該商品に応じて計算公式をロードする
	 * 
	 * @author btchoukug
	 * 
	 */
	class LoadCalculableFormulaAction implements Listener {

		@Override
		public void handleEvent(Event arg0) {
			// コードは3桁とする

			display.asyncExec(new Runnable() {

				@Override
				public void run() {
					try {
						
						cate = _getSelectedCate();
						_loadCurrentCalculateInfo(code, cate);
						
						//　選択されたカテゴリより、標準体のコンペアグループに持たせるデータをセットし直す
						normalCompareGroup.setCalculableFormulas(calculableFormulas);
						normalCompareGroup.setInputDataHeaders(inputDataHeaders);
						normalCompareGroup.setInputRateKeyNames(inputRateKeyNames);
						// 選択されたカテゴリより、特体のコンペアグループに持たせるデータをセットし直す
						specialCompareGroup.setCalculableFormulas(calculableFormulas);
						specialCompareGroup.setInputDataHeaders(inputDataHeaders);
						specialCompareGroup.setInputRateKeyNames(inputRateKeyNames);
						
						normalCompareGroup.clearTextValue();
						//normalCompareGroup.setButtonEnabled();
						
						specialCompareGroup.clearTextValue();
						//specialCompareGroup.setButtonEnabled();						

					} catch (RateException e) {
						logger.error("計算カテゴリ指定されてない：", e);
					}
				}

			});

		}
	}

	/**
	 * 画面上のファイル処理進捗を更新する <br>
	 * また、フォルダを選択する場合、カレント処理完了次第次のファイルを続く
	 * 
	 * @author btchoukug
	 * 
	 */
	class UpdateProgressAction {
		ExecutorService es;

		long _totalLineCount;
		IBatchWriter _csvWriter;
		boolean isStop = false;

		public UpdateProgressAction() {
			es = Executors.newSingleThreadExecutor();
		}

		public void setProgressObject(long totalLineCount,
				IBatchWriter csvWriter) {
			this._totalLineCount = totalLineCount;
			this._csvWriter = csvWriter;
			this.isStop = false;
		}

		public void stop() {
			this.isStop = true;
			// es.shutdownNow();
		}

		public void shutdown() {
			this.es.shutdownNow();
		}

		/**
		 * 進捗更新を行う
		 */
		public void go() {

			if (es.isShutdown()) {
				es = Executors.newSingleThreadExecutor();
			}

			final long t1 = System.currentTimeMillis();

			es.execute(new Runnable() {

				@Override
				public void run() {
					while (true) {

						if (isStop) return;

						try {
							Thread.sleep(100);
						} catch (InterruptedException e) {
							logger.error("プログレス更新エラー", e);
							e.printStackTrace();
						}

						final long processLineCount = _csvWriter.getLineCount();

						/*
						 * 現時点まで、経験よると、3万件ぐらいの場合、計算の速さは一番速いですので、
						 * ３万件毎に、GCを実行させます。
						 */
//						if (processLineCount % 30000 == 0) {
//							Runtime.getRuntime().gc();
//						}

						final long ngCount = _csvWriter.getNgCount();
						final String outputFilePath = _csvWriter.getFilePath();

						if (display.isDisposed()) return;
						
						display.asyncExec(new Runnable() {

							@Override
							public void run() {

								int percentage = (int) (processLineCount * 100 / _totalLineCount);
								progressInfo.setPercentage(percentage + "%");
								progressInfo.setErrorDataCount(_csvWriter.getErrorLineCount());
								tblTask.updateFileInfo(fileCount, progressInfo);

								// 全部処理完了の場合、計算結果ファイルのパスを表示する
								if (processLineCount == _totalLineCount) {
									long t2 = System.currentTimeMillis();

									progressInfo.setCompleteDate(new Date());
									progressInfo.setExpendTime((t2 - t1) / 1000d);
									progressInfo.setNGCount(ngCount);
									progressInfo.setTotalCount(_totalLineCount);
									
									tblTask.updateFileInfo(fileCount, progressInfo);

									/*
									 * 計算レポート： ログにバッチ計算結果をまとめて出力：何件計算したのか、
									 * 一致は何件、不一致は何件、かかる時間
									 */
									StringBuffer sb = new StringBuffer(200);
									sb.append("計算レポート：\r\n")
									.append("入力データ：").append(reader.getFile().getAbsolutePath()).append("\r\n")
									.append("計算結果：").append(outputFilePath).append("\r\n")
									.append("総件数：").append(_totalLineCount).append("\r\n")
									.append("NG件数：").append(ngCount).append("\r\n")
									.append("ERR件数：").append(_csvWriter.getErrorLineCount()).append("\r\n")
									.append("かかり時間：").append((t2 - t1) / 1000d).append("秒").append("\r\n");

									String resultText = sb.toString();

									// 計算完了したら、ログ再開
									LogFactory.setLoggerLevel(Level.INFO);
									logger.info(resultText);

									if (cacheEnable) {
										logger.info("キャッシュ情報：\n" + _getCacheLiveSts());
									}

									// フォカースをセット
									logComp.text_log.setEditable(false);
									logComp.text_log.setFocus();

									fileCount++;

									if (fileCount < listProgressInfo.size()) {
										
										Runtime.getRuntime().gc();
										Cache var_cache = CacheManager.getInstance().getCache(CacheManagerSupport.VAR_CACHE);
										var_cache.removeAll();
										var_cache.clearStatistics();
										
										//次のファイル単位のバッチを行う
										_executeBatch();
										
									} else {
										
										startBtn.setText("BT計算完了");
										bt.shutdownAndAwaitTermination();
										es.shutdown();

										if (isShutdownAfterExec) {
											// 計算完了次第シャットダウン
											_shutdownAfterExec();
										}
										
										// ボタン制御を行う
										btnAddFile.setEnabled(true);
										btnAddFolder.setEnabled(true);
										btnRemoveFile.setEnabled(true);

									}

								}
							}

						});

						// 全部処理完了の場合、ループを中止
						if (processLineCount == _totalLineCount) {
							break;
						}
					}
				}

			});
		}

	}



	private void _createCSVReport(File logFile) throws IOException {
		
		FileWriter fileWriter = new FileWriter(logFile, false);
		
		//　ヘッダを書込
		fileWriter.write("検証日" + "\t");
		fileWriter.write("検証完了日" + "\t");
		fileWriter.write("検証対象" + "\t");
		fileWriter.write("データフォルダ" + "\t");
		fileWriter.write("データファイル" + "\t");
		fileWriter.write("総件数" + "\t");
		fileWriter.write("NG件数" + "\t");
		fileWriter.write("NG比率" + "\t");
		fileWriter.write("性能（件/秒）" + "\t");
		fileWriter.write("掛かり時間（秒）" + "\t");
		fileWriter.write("備考" + "\r\n");

		//　タスクリストどおりに検証結果を書込
		for (int i = 0; i < listProgressInfo.size(); i++) {
			ProgressInfo progressInfo = listProgressInfo.get(i);
			fileWriter.write(DateFormatUtils.format(new Date(System
					.currentTimeMillis()), YYYYMMDD)
					+ "\t");
			fileWriter.write(DateFormatUtils.format(new Date(System
					.currentTimeMillis()), YYYYMMDD)
					+ "\t");
			fileWriter.write(CategoryManager.getInstance().getCateInfo(cate)
					.getLabel()
					+ "\t");
			fileWriter.write(progressInfo.getFilePath() + "\t");
			fileWriter.write(progressInfo.getFileName() + "\t");
			fileWriter.write(progressInfo.getTotalCount() + "\t");
			fileWriter.write(progressInfo.getNGCount() + "\t");
			fileWriter.write("\t");
			fileWriter.write("\t");
			fileWriter.write(progressInfo.getExpendTime() + "\t");
			fileWriter.write("\r\n");

		}
		
		fileWriter.close();
	}
	
	/**
	 * 入力のファイルを取得
	 */
	private void _getfile(final String inputDataFilePath) {

		try {

			// インプットデータを読み込むモジュールを初期化
			File inputFile = new File(inputDataFilePath);
			if (inputFile.isFile()) {
				// ファイルの場合
				ProgressInfo fileInfo = new ProgressInfo(inputDataFilePath);
				listProgressInfo.add(fileInfo);
				tblTask.displayFileInfo(fileInfo);
			} else {
				BatchInputAdapter adapter = new BatchInputAdapter(new String[] {
						".dat", ".txt" });

				// 　.datファイルを選択
				adapter.setMaxInputFileNum(0);
				adapter.setFileSuffixValid(true);
				adapter.loadInputData(inputDataFilePath);
				int curFileCount = 0;
				while (adapter.haveNextFile()) {
					curFileCount++;
					ProgressInfo fileInfo = new ProgressInfo(adapter
							.getFilePath());
					listProgressInfo.add(fileInfo);
					tblTask.displayFileInfo(fileInfo);
				}

				// TODO ファイルチックを行う
				if (curFileCount == 0) {
					throw new RateException("選択したフォルダの下に.datファイルがありません：　"
							+ inputDataFilePath);
				}

			}
		} catch (RateException e) {

			MessageBox msgBox = new MessageBox(shell, SWT.ICON_WARNING | SWT.OK);
			msgBox.setText("BT入力エラー");
			msgBox.setMessage(e.getMessage());
			msgBox.open();

			// batchを実行しないで中止
			return;
		}

	}

	/**
	 * 毎回計算ボタンを押すとBT計算を行う
	 */
	private void _executeBatch() {

		if (updateProgressAction == null) {
			updateProgressAction = new UpdateProgressAction();
		}

		try {

			if (fileCount == listProgressInfo.size()) {
				fileCount = 0;
			}

			// 入力パラメータを取得
			cate = _getSelectedCate();
			cacheEnable = cacheEnableBtn.getSelection();

			// バッチ計算モジュールを初期化
			bt.setEnableCache(cacheEnable);
			bt.setCalculateCategory(cate);
			
			// 選択された公式を計算対象に指定、また指定の計算基礎を指定
			Formula selectedFormula = currentFormulas.get(normalCompareGroup.getSelectedFormulaIndex());
			bt.setCurrentFormula(selectedFormula);
			
			int specialFormulaIndex = specialCompareGroup.getSelectedFormulaIndex();
			if (specialFormulaIndex >= 0) {
				bt.setSpecialFormula(currentFormulas.get(specialFormulaIndex));
			}		

			ngOnly = ngOnlyBtn.getSelection();
			isShutdownAfterExec = shutdownBtn.getSelection();

			String maxLine = maxLine_text.getText();
			fixedRateKeyValueText = fixedVal_text.getText();

			if (!CommonUtil.isNumeric(maxLine)) {
				throw new RateException("バッチ計算の回数上限を数字で指定ください。");
			}

			// 普通体の場合、比較対象のマッピングを編集
			compareMapping = normalCompareGroup.getCompareMapping();			
			// 特体の場合、比較対象の編集（選択されない場合、nullを返す）
			specialCompareMapping = specialCompareGroup.getCompareMapping();
			if (specialCompareMapping != null) {
				compareMapping.putAll(specialCompareMapping);
			}
			
			// 特体の判断元を設定
			bt.setDeathIndexColumnName(specialCompareGroup.getDeathIndexColumnName());
			// 死亡指数ランクの値を設定
			bt.setDeathIndexRankVal(specialCompareGroup.getDeathIndexRankValue());
		
			// ボタンを禁止にする
			btnAddFile.setEnabled(false);
			btnAddFolder.setEnabled(false);
			btnRemoveFile.setEnabled(false);

			// インプットデータを読み込むモジュールを初期化
			String dataFilePath = listProgressInfo.get(fileCount).getFileFullName();
			File inputFile = new File(dataFilePath);
			
			if (inputFile.isFile()) {
				// ファイルの場合
				long lMaxLineNum = Long.parseLong(maxLine);
				if (dataFilePath.endsWith(".csv")) {
					reader = new CsvRateKeyReaderImpl(dataFilePath);
				} else {
					reader = new DatRateKeyReaderImpl(dataFilePath);
					if (factory != null) {
						reader.setLayoutFactory(factory);
					}
					((DatRateKeyReaderImpl) reader).loadDataLayout(code, cate);
				}

				// UI指定とファイルの総行数より最大行数編集
				long totalCount = reader.getTotalLineCount();
				if (lMaxLineNum > 0l) {
					specifyLineCount = totalCount > lMaxLineNum ? lMaxLineNum
							: totalCount;
				} else {
					specifyLineCount = totalCount;
				}
				reader.setMaxLineNumber(lMaxLineNum);

			}

			String orig = normalCompareGroup.getOrigText();
			String specOrig = specialCompareGroup.getOrigText();
			orig = (specOrig == null) ? orig : orig + Const.COMMA + specOrig;
			// 比較対象を設定
			reader.setCompareObject(orig);
			// 画面から固定値のレートキーを設定
			reader.setFixedValues(fixedRateKeyValueText);

			// csvファイルWriterの初期化
			writer = new CsvFileWriterImpl(code, reader.getFile(),
					compareMapping, ngOnly, cate, orig);

		} catch (RateException e) {

			MessageBox msgBox = new MessageBox(shell, SWT.ICON_WARNING | SWT.OK);
			msgBox.setText("BT入力エラー");
			msgBox.setMessage(e.getMessage());
			msgBox.open();

			// batchを実行しないで中止
			return;
		} catch (FmsDefErrorException e) {
			MessageBox msgBox = new MessageBox(shell, SWT.ICON_WARNING | SWT.OK);
			msgBox.setText("算式やUI定義エラー");
			msgBox.setMessage(e.getMessage());
			msgBox.open();
			
			return;
		}

		// プロセスバーの進捗を更新のため
		updateProgressAction.setProgressObject(specifyLineCount, writer);
		bt.exec(reader, writer);

		progressInfo = listProgressInfo.get(fileCount);
		progressInfo.setTotalCount(specifyLineCount);

		updateProgressAction.go();

		// 計算ボタンの文字を「中止」に変更
		startBtn.setText("BT計算中止");

	}

	/**
	 * 現時点キャッシュステータスを取得
	 * 
	 * @return
	 */
	private String _getCacheLiveSts() {
	
		String cacheName = CacheManagerSupport.VAR_CACHE;
		LiveCacheStatistics sts = CacheManager.getInstance()
				.getCache(cacheName).getLiveCacheStatistics();
		
		if (sts.isStatisticsEnabled()) {
			StringBuffer sb = new StringBuffer(200);
			sb.append("The statistics of " + cacheName).append("\n");
			sb.append("Total Hit Count=" + sts.getCacheHitCount()).append("\n");
			sb.append("Total Miss Count=" + sts.getCacheMissCount()).append("\n");
			sb.append("Average GetTime(ms)=" + sts.getAverageGetTimeMillis())
					.append("\n");
			return sb.toString();
		}
		
		return "キャッシュ状況は統計されていません。";		
	}

	/**
	 * BT計算はかなり時間がかかるため、計算完了次第30秒後シャットダウン
	 */
	private void _shutdownAfterExec() {
		try {
			Runtime.getRuntime().exec("shutdown -t 30");
		} catch (IOException e) {
			logger.error("BT計算完了次第シャットダウンにはエラーが発生した", e);
			try {
				Runtime.getRuntime().exec("shutdown -s");
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}
	}

	private String _getSelectedCate() throws RateException {
		String cate = null;
		for (Control ctrl : cate_comp.getChildren()) {
			if (ctrl instanceof Button) {
				Button rad = (Button) ctrl;
				if (rad.getSelection()) {
					cate = (String) rad.getData("cate");
				}
			}
		}

		if (cate == null || StringUtils.isEmpty(cate)) {
			throw new RateException("計算カテゴリを指定ください。");
		}

		return cate;

	}

	/**
	 * 保険商品コード、選択したカテゴリより、UI用のレートキーと公式定義情報をロードする
	 * 
	 * @param code
	 * @param cate
	 * @throws FmsDefErrorException 
	 * @throws FmsDefErrorException 
	 * @throws RateException
	 */
	private void _loadCurrentCalculateInfo(String code, String cate) {

		// 選択されたカテゴリの公式をロード
		formulaMgr.load(new String[] { CalculateCategory.P, CalculateCategory.V, CalculateCategory.W });
		
		// 選択されたカテゴリのデータレイアウトをロード
		factory = new BatchDataLayoutFactory();
		dataLayout = factory.getDataLayout(code, cate);
		inputRateKeyNames = dataLayout.getKeyNames();
		inputDataHeaders = dataLayout.getKeyDescs();

		// 選択されたカテゴリの公式定義をロード
		currentFormulas = formulaMgr.getAccessableFormulaList(cate);

		calculableFormulas = new String[currentFormulas.size()];
		int i = 0;
		for (Formula formula : currentFormulas) {
			calculableFormulas[i] = formula.getDesc();
			i++;
		}
	}

	/** バッチＵＩエントリー */
	public static void main(String[] args) {
		// 商品選択画面から商品コード指定を行う
		TypeDialog typeDialog = new TypeDialog();
		String type = typeDialog.open();
		if (type != null) {
			InsuranceSelectDialog dialog = new InsuranceSelectDialog(type);
			String code = dialog.open();

			if (code != null) {
				if (code.substring(3).indexOf("1") > -1) {
					//特体を選択した場合
					isSpecialMode = true;
				}
				LoadingShell loadShell = new LoadingShell();
				loadShell.loadBatchContext(code);

				try {
					RateCalculateBTForm form = new RateCalculateBTForm(code);
					form.open();
				} catch (FmsDefErrorException e) {
					MessageBox msgBox = new MessageBox(Display.getDefault().getActiveShell(), SWT.ICON_ERROR | SWT.OK);
					msgBox.setMessage(e.getMessage());
					msgBox.open();
				}
				
			}
		}

	}

}