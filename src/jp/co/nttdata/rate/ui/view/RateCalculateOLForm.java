package jp.co.nttdata.rate.ui.view;

import java.awt.Desktop;
import java.io.IOException;
import java.util.List;

import jp.co.nttdata.rate.log.LogFactory;
import jp.co.nttdata.rate.model.CalculateCategory;
import jp.co.nttdata.rate.model.RateCalculateSupport;
import jp.co.nttdata.rate.ui.view.RateCalculateLogComposite;
import jp.co.nttdata.rate.util.CommonUtil;
import jp.co.nttdata.rate.util.Const;
import jp.co.nttdata.rate.util.ResourceLoader;

import org.apache.log4j.Level;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;

/**
 * 
 * O/L計算のUIフォームである * 
 * @author btchoukug
 * 
 */
public class RateCalculateOLForm {

	private final String SHELL_TITLE = "数理レートツールOL";

	/** 画面のコントロール */
	Display display;
	Shell shell;
	TabFolder tabFolder;
	Image img;
	
	/** レート計算モジュール */
	private RateCalculateSupport rcs;
	private final String code;

	public RateCalculateOLForm(RateCalculateSupport rcs) {
		
		display = Display.getDefault();
		shell = new Shell(display, SWT.MIN | SWT.CLOSE | SWT.RESIZE);
		shell.setLayout(new FillLayout());
		shell.setText(SHELL_TITLE);
		
		// 計算モジュール初期化
		this.rcs = rcs;
		this.code = rcs.getCode();
		
	}

	private void _initCalculateArea() {

		shell.setText(SHELL_TITLE + "-" + rcs.getWindowTitle());

		tabFolder = new TabFolder(shell, SWT.NONE);
		tabFolder.addMouseListener(new MouseAdapter() {
			public void mouseDown(MouseEvent event) {
				// タブに応じて計算対象およびレートキーの制御関係を設定
				int index = tabFolder.getSelectionIndex();

				// ログタブは対象外とする
				if (index < rcs.getCateInfos().size()) {
					rcs.setCalculateCategory(index);
				}
			}
		});

		// 計算に関して３つタブPVWの初期化
		List<CalculateCategory> cates = rcs.getCateInfos();
		if (cates != null) {

			for (CalculateCategory cate : cates) {

				Composite tabFolderPage = new Composite(tabFolder, SWT.NONE);
				tabFolderPage.setLayout(new GridLayout(1, false));
				tabFolderPage.setLayoutData(new GridData(GridData.FILL_BOTH));

				// レートキー入力項目と計算結果の初期化
				RateCalculateComposite rcc = new RateCalculateComposite(rcs, tabFolderPage, cate.getName(), cate.getKeyList());
				rcc.composite_input.setGenerationList(rcs.getGenerationList());

				// 計算用タブ
				TabItem tabItem = new TabItem(tabFolder, SWT.NONE);
				tabItem.setText(cate.getLabel());
				tabItem.setControl(tabFolderPage);

			}
		}

		// 計算ログコンポ
		RateCalculateLogComposite comp_log = new RateCalculateLogComposite(tabFolder);

		// ログタブ
		TabItem tabItem_log = new TabItem(tabFolder, SWT.NONE);
		tabItem_log.setText("計算ログ");
		tabItem_log.setControl(comp_log);

	}

	/**
	 * Open the window
	 */
	public void open() {
		
		img = new Image(display, ResourceLoader.getExternalResourceAsStream(Const.IMAGEICON));
		shell.setImage(img);
		
		_createMenu();

		_initCalculateArea();
		
		//瞬間画面サイズの変わりを抑止		
		shell.setVisible(false);
		shell.pack();
		CommonUtil.setShellLocation(shell);		
		shell.open();	
		
		shell.addListener(SWT.Close, new Listener() {
			@Override
			public void handleEvent(Event arg0) {
				// UIを閉じるときに、キャッシュデータをディスクに保存する
				//rcs.cache2Disk();
			}
		});
		
		while (!shell.isDisposed()) {
			if (!display.readAndDispatch())
				display.sleep();
		}
		display.dispose();
	}

	private void _createMenu() {

		/* Create menu bar. */
		Menu menuBar = new Menu(shell, SWT.BAR);
		shell.setMenuBar(menuBar);

		MenuItem item;
		Menu subMenu;

		// サブメニュー「操作」
		item = new MenuItem(menuBar, SWT.CASCADE);
		item.setText("操作(&O)");
		item.setAccelerator(SWT.MOD1 + SWT.MOD2 + 'O');

		subMenu = new Menu(shell, SWT.DROP_DOWN);
		item.setMenu(subMenu);

		item = new MenuItem(subMenu, SWT.PUSH);
		item.setText("商品一覧へ(&L)");
		item.setAccelerator(SWT.MOD1 + SWT.MOD2 + 'L');
		item.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent arg0) {

				TypeDialog typeDialog = new TypeDialog();
				String type = typeDialog.open();

				if(type != null) {
					InsuranceSelectDialog dialog = new InsuranceSelectDialog(shell,type);
					String code = dialog.open();
					if (code != null) {
						_reload(code);
					}
				}
			}
		});

		item = new MenuItem(subMenu, SWT.PUSH);
		item.setText("再度ロード(&R)");
		item.setAccelerator(SWT.MOD1 + SWT.MOD2 + 'R');
		item.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				_reload(code);
			}
		});

		new MenuItem(subMenu, SWT.SEPARATOR);
		item = new MenuItem(subMenu, SWT.CHECK);
		item.setText("Cache有効(&C)");
		item.setSelection(true);
		item.setAccelerator(SWT.MOD1 + SWT.MOD2 + 'C');
		item.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				if (((MenuItem) arg0.getSource()).getSelection()) {
					rcs.setCacheEnable(true);
				} else {
					rcs.setCacheEnable(false);
				}
			}
		});

		item = new MenuItem(subMenu, SWT.CHECK);
		item.setText("デバッグモード(&D)");
		item.setSelection(false);

		// menuでログレベルをコントロールする
		LogFactory.setLoggerLevel(Level.INFO);

		item.setAccelerator(SWT.MOD1 + SWT.MOD2 + 'D');
		item.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				if (((MenuItem) arg0.getSource()).getSelection()) {
					System.out.println("set log level to debug");
					LogFactory.setLoggerLevel(Level.DEBUG);
				} else {
					System.out.println("set log level to info");
					LogFactory.setLoggerLevel(Level.INFO);
				}
			}
		});

		// サブメニュー「商品定義」
		item = new MenuItem(menuBar, SWT.CASCADE);
		item.setText("商品定義 (&D)");
		item.setAccelerator(SWT.MOD1 + SWT.MOD2 + 'D');

		subMenu = new Menu(shell, SWT.DROP_DOWN);
		item.setMenu(subMenu);

		item = new MenuItem(subMenu, SWT.PUSH);
		item.setText("計算式(&F)");
		item.setAccelerator(SWT.MOD1 + SWT.MOD2 + 'F');
		item.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				System.out.println("open formula definition xml file");
				try {
					// TODO notepadではなくて、eclispeのようなエディターを導入する
					Desktop.getDesktop().edit(rcs.getFormulaDefFile());
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		});

		item = new MenuItem(subMenu, SWT.PUSH);
		item.setText("レートキー (&R)");
		item.setAccelerator(SWT.MOD1 + SWT.MOD2 + 'R');
		item.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				System.out.println("open ratekey definition xml file");
				try {
					Desktop.getDesktop().edit(rcs.getRatekeyDefFile());
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		});

		item = new MenuItem(subMenu, SWT.PUSH);
		item.setText("&UI");
		item.setAccelerator(SWT.MOD1 + SWT.MOD2 + 'U');
		item.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				System.out.println("open UI definition xml file");
				try {
					Desktop.getDesktop().edit(rcs.getUIDefFile());
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		});

		// サブメニュー「共通定義」
		item = new MenuItem(menuBar, SWT.CASCADE);
		item.setText("共通定義(&C)");
		item.setAccelerator(SWT.MOD1 + SWT.MOD2 + 'C');

		subMenu = new Menu(shell, SWT.DROP_DOWN);
		item.setMenu(subMenu);

		item = new MenuItem(subMenu, SWT.PUSH);
		item.setText("基数(&F)");
		item.setAccelerator(SWT.MOD1 + SWT.MOD2 + 'F');
		item.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				System.out.println("open fundation definition xml file");
				try {
					Desktop.getDesktop().edit(rcs.getFundationDefFile());
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		});

		item = new MenuItem(subMenu, SWT.PUSH);
		item.setText("年金現価(&L)");
		item.setAccelerator(SWT.MOD1 + SWT.MOD2 + 'L');
		item.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				System.out.println("open LifePension definition xml file");
				try {
					Desktop.getDesktop().edit(rcs.getLifePersionDefFile());
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		});
		
		item = new MenuItem(subMenu, SWT.PUSH);
		item.setText("給付現価(&B)");
		item.setAccelerator(SWT.MOD1 + SWT.MOD2 + 'B');
		item.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				System.out.println("open Benefit definition xml file");
				try {
					Desktop.getDesktop().edit(rcs.getBenefitDefFile());
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		});

		// サブメニュー「ヘルプ」
		item = new MenuItem(menuBar, SWT.CASCADE);
		item.setText("ヘルプ(&H)");
		item.setAccelerator(SWT.MOD1 + SWT.MOD2 + 'H');

		subMenu = new Menu(shell, SWT.DROP_DOWN);
		item.setMenu(subMenu);
		
		item = new MenuItem(subMenu, SWT.PUSH);
		item.setText("バージョン情報(&V)");
		item.setAccelerator(SWT.MOD1 + SWT.MOD2 + 'V');
		item.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				System.out.println("open Version Info");
				VersionInfoDialog verDialog = new VersionInfoDialog(shell);
				verDialog.open();
			}
		});
	}
	
	private void _reload(String code) {
		display.dispose();
		LoadingShell loadShell = new LoadingShell();
		rcs = loadShell.load(code);
		//RCS初期化のところで異常があったら、nullを返すため
		if (rcs != null) {
			RateCalculateOLForm window = new RateCalculateOLForm(rcs);
			window.open();			
		}
	}

	/**
	 * アプリをスタート
	 * 
	 * @param args
	 */
	public static void main(String[] args) {

		// 商品選択画面から商品コード指定を行う
		TypeDialog typeDialog = new TypeDialog();
		String type = typeDialog.open();
		if(type != null) {
			InsuranceSelectDialog dialog = new InsuranceSelectDialog(type);
			String code = dialog.open();
			if (code != null) {
				LoadingShell loadShell = new LoadingShell();
				RateCalculateSupport rcs = loadShell.load(code);
				if (rcs != null) {
					RateCalculateOLForm window = new RateCalculateOLForm(rcs);
					window.open();
				}		
			}
		}
	}

}
