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
 * O/L�v�Z��UI�t�H�[���ł��� * 
 * @author btchoukug
 * 
 */
public class RateCalculateOLForm {

	private final String SHELL_TITLE = "�������[�g�c�[��OL";

	/** ��ʂ̃R���g���[�� */
	Display display;
	Shell shell;
	TabFolder tabFolder;
	Image img;
	
	/** ���[�g�v�Z���W���[�� */
	private RateCalculateSupport rcs;
	private final String code;

	public RateCalculateOLForm(RateCalculateSupport rcs) {
		
		display = Display.getDefault();
		shell = new Shell(display, SWT.MIN | SWT.CLOSE | SWT.RESIZE);
		shell.setLayout(new FillLayout());
		shell.setText(SHELL_TITLE);
		
		// �v�Z���W���[��������
		this.rcs = rcs;
		this.code = rcs.getCode();
		
	}

	private void _initCalculateArea() {

		shell.setText(SHELL_TITLE + "-" + rcs.getWindowTitle());

		tabFolder = new TabFolder(shell, SWT.NONE);
		tabFolder.addMouseListener(new MouseAdapter() {
			public void mouseDown(MouseEvent event) {
				// �^�u�ɉ����Čv�Z�Ώۂ���у��[�g�L�[�̐���֌W��ݒ�
				int index = tabFolder.getSelectionIndex();

				// ���O�^�u�͑ΏۊO�Ƃ���
				if (index < rcs.getCateInfos().size()) {
					rcs.setCalculateCategory(index);
				}
			}
		});

		// �v�Z�Ɋւ��ĂR�^�uPVW�̏�����
		List<CalculateCategory> cates = rcs.getCateInfos();
		if (cates != null) {

			for (CalculateCategory cate : cates) {

				Composite tabFolderPage = new Composite(tabFolder, SWT.NONE);
				tabFolderPage.setLayout(new GridLayout(1, false));
				tabFolderPage.setLayoutData(new GridData(GridData.FILL_BOTH));

				// ���[�g�L�[���͍��ڂƌv�Z���ʂ̏�����
				RateCalculateComposite rcc = new RateCalculateComposite(rcs, tabFolderPage, cate.getName(), cate.getKeyList());
				rcc.composite_input.setGenerationList(rcs.getGenerationList());

				// �v�Z�p�^�u
				TabItem tabItem = new TabItem(tabFolder, SWT.NONE);
				tabItem.setText(cate.getLabel());
				tabItem.setControl(tabFolderPage);

			}
		}

		// �v�Z���O�R���|
		RateCalculateLogComposite comp_log = new RateCalculateLogComposite(tabFolder);

		// ���O�^�u
		TabItem tabItem_log = new TabItem(tabFolder, SWT.NONE);
		tabItem_log.setText("�v�Z���O");
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
		
		//�u�ԉ�ʃT�C�Y�̕ς���}�~		
		shell.setVisible(false);
		shell.pack();
		CommonUtil.setShellLocation(shell);		
		shell.open();	
		
		shell.addListener(SWT.Close, new Listener() {
			@Override
			public void handleEvent(Event arg0) {
				// UI�����Ƃ��ɁA�L���b�V���f�[�^���f�B�X�N�ɕۑ�����
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

		// �T�u���j���[�u����v
		item = new MenuItem(menuBar, SWT.CASCADE);
		item.setText("����(&O)");
		item.setAccelerator(SWT.MOD1 + SWT.MOD2 + 'O');

		subMenu = new Menu(shell, SWT.DROP_DOWN);
		item.setMenu(subMenu);

		item = new MenuItem(subMenu, SWT.PUSH);
		item.setText("���i�ꗗ��(&L)");
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
		item.setText("�ēx���[�h(&R)");
		item.setAccelerator(SWT.MOD1 + SWT.MOD2 + 'R');
		item.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				_reload(code);
			}
		});

		new MenuItem(subMenu, SWT.SEPARATOR);
		item = new MenuItem(subMenu, SWT.CHECK);
		item.setText("Cache�L��(&C)");
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
		item.setText("�f�o�b�O���[�h(&D)");
		item.setSelection(false);

		// menu�Ń��O���x�����R���g���[������
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

		// �T�u���j���[�u���i��`�v
		item = new MenuItem(menuBar, SWT.CASCADE);
		item.setText("���i��` (&D)");
		item.setAccelerator(SWT.MOD1 + SWT.MOD2 + 'D');

		subMenu = new Menu(shell, SWT.DROP_DOWN);
		item.setMenu(subMenu);

		item = new MenuItem(subMenu, SWT.PUSH);
		item.setText("�v�Z��(&F)");
		item.setAccelerator(SWT.MOD1 + SWT.MOD2 + 'F');
		item.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				System.out.println("open formula definition xml file");
				try {
					// TODO notepad�ł͂Ȃ��āAeclispe�̂悤�ȃG�f�B�^�[�𓱓�����
					Desktop.getDesktop().edit(rcs.getFormulaDefFile());
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		});

		item = new MenuItem(subMenu, SWT.PUSH);
		item.setText("���[�g�L�[ (&R)");
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

		// �T�u���j���[�u���ʒ�`�v
		item = new MenuItem(menuBar, SWT.CASCADE);
		item.setText("���ʒ�`(&C)");
		item.setAccelerator(SWT.MOD1 + SWT.MOD2 + 'C');

		subMenu = new Menu(shell, SWT.DROP_DOWN);
		item.setMenu(subMenu);

		item = new MenuItem(subMenu, SWT.PUSH);
		item.setText("�(&F)");
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
		item.setText("�N������(&L)");
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
		item.setText("���t����(&B)");
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

		// �T�u���j���[�u�w���v�v
		item = new MenuItem(menuBar, SWT.CASCADE);
		item.setText("�w���v(&H)");
		item.setAccelerator(SWT.MOD1 + SWT.MOD2 + 'H');

		subMenu = new Menu(shell, SWT.DROP_DOWN);
		item.setMenu(subMenu);
		
		item = new MenuItem(subMenu, SWT.PUSH);
		item.setText("�o�[�W�������(&V)");
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
		//RCS�������̂Ƃ���ňُ킪��������Anull��Ԃ�����
		if (rcs != null) {
			RateCalculateOLForm window = new RateCalculateOLForm(rcs);
			window.open();			
		}
	}

	/**
	 * �A�v�����X�^�[�g
	 * 
	 * @param args
	 */
	public static void main(String[] args) {

		// ���i�I����ʂ��珤�i�R�[�h�w����s��
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
