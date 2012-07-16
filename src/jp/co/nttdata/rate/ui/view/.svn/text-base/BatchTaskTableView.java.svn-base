package jp.co.nttdata.rate.ui.view;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import jp.co.nttdata.rate.batch.IBatchWriter;
import jp.co.nttdata.rate.batch.IRateKeyReader;
import jp.co.nttdata.rate.log.LogFactory;

import org.apache.log4j.Logger;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.RowData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;

/**
 * 選択されたBatchインプットデータファイルについて、それぞれの計算状況
 * <br>ファイルパス、ファイル名、総件数、NG件数、
 * <br>進捗（11/10000の比率と進捗バー）、操作ボタン（場所開くとか）など
 * を表示されるテーブルビューである
 * @author btchoukug
 *
 */
public class BatchTaskTableView extends Table {
	
	private static final String BT_LOGGER = "BTLogger"; 
	private Logger logger = LogFactory.getInstance(BT_LOGGER);

	private Display display;
	private int fileCount = 0;
	
	/**進捗更新スレッド*/
	private ExecutorService es;
	
	protected void checkSubclass() {
		;
	}
	
	public BatchTaskTableView(Composite parent) {
		super(parent, SWT.BORDER | SWT.MULTI | SWT.FULL_SELECTION);
		this.setHeaderVisible(true);
        this.setLinesVisible(true);
        RowData tableData = new RowData();
	    tableData.height = 100;
	    tableData.width = 500;
        this.setLayoutData(tableData);
        
        _addColumns();
        
        display = parent.getDisplay();
		es = Executors.newSingleThreadExecutor();
        
	}
	
	/**
	 * テーブルヘッダを追加
	 */
	private void _addColumns() {
	      TableColumn taskNo = new TableColumn(this, SWT.LEFT);
	      taskNo.setResizable(true);
	      taskNo.setText("No.");
	      taskNo.setWidth(30);
	      
	      TableColumn taskFilePath = new TableColumn(this, SWT.LEFT);
	      taskFilePath.setResizable(true);
	      taskFilePath.setText("ファイル名");
	      taskFilePath.setWidth(120);

	      TableColumn taskFile = new TableColumn(this, SWT.LEFT);
	      taskFile.setResizable(true);
	      taskFile.setText("場所");
	      taskFile.setWidth(100);
	      
	      TableColumn taskFileName = new TableColumn(this, SWT.CENTER);
	      taskFileName.setResizable(true);
	      taskFileName.setText("進歩率");
	      taskFileName.setWidth(60);

	      TableColumn taskTotalCount = new TableColumn(this, SWT.CENTER);
	      taskTotalCount.setResizable(true);
	      taskTotalCount.setText("総件数");
	      taskTotalCount.setWidth(60);
	      
	      TableColumn taskNGCount = new TableColumn(this, SWT.CENTER);
	      taskNGCount.setResizable(true);
	      taskNGCount.setText("NG件数");
	      taskNGCount.setWidth(60);
	      
	      TableColumn taskErrorCount = new TableColumn(this, SWT.CENTER);
	      taskErrorCount.setResizable(true);
	      taskErrorCount.setText("ERR件数");
	      taskErrorCount.setWidth(70);
	      
	      TableColumn taskProgressText = new TableColumn(this, SWT.CENTER);
	      taskProgressText.setResizable(true);
	      taskProgressText.setText("掛り時間（s）");
	      taskProgressText.setWidth(75);
	      
	}
	
	/**
	 * 新たにBatch計算用のデータファイルを計算するともに、そのファイルの計算状況も
	 * Batchタスクテーブルビューに追加して表示させる
	 * @param reader
	 * @param writer
	 */
	public void addBatchTask(long specifyLineCount,int fileCOunt,IRateKeyReader reader, IBatchWriter writer) {
		//TODO データファイルがタスク単位で追加して、計算状況を反映されるように
		UpdateProgressAction updateProgress = new UpdateProgressAction();
		
		// プロセスバーの進捗を更新のため
		updateProgress.setProgressObject(specifyLineCount, writer);
		updateProgress.go();
		
	}
	
	/**
	 * 入力のファイルを取得
	 */
	public void updateFileInfo(int fileCount,ProgressInfo progressInfo) {
		//TODO シングルのタスク情報を更新
		this.getItem(fileCount).setText(progressInfo.getProgressInfo(fileCount+1));
	}
	
	/**
	 * 入力のファイルを取得
	 */
	public void displayFileInfo(ProgressInfo progressInfo) {
		TableItem item = new TableItem(this, SWT.NONE);
		item.setText(progressInfo.getProgressInfo(++this.fileCount));
	}
	
	/**
	 * 入力のファイルを取得
	 */
	public int[] itemRemove(Shell shell) {
		int[] index = this.getSelectionIndices();
		for(int i = index.length - 1; i >= 0; i--) {
			this.getItem(index[i]).dispose();
			this.fileCount-- ;
			for(int j = index[i]; j < this.getItemCount(); j++) {
				this.getItem(j).setText(((Integer)(j+1)).toString());
			}
		}
		return index;
	}
	
	
	/**
	 * 画面上のファイル処理進捗を更新する
	 * FIXME observe modeで改善つもりです
	 * <br>また、フォルダを選択する場合、カレント処理完了次第次のファイルを続く
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
		
		public void setProgressObject(long totalLineCount, IBatchWriter csvWriter) {
			this._totalLineCount = totalLineCount;
			this._csvWriter = csvWriter;
			this.isStop = false;
		}

		public void stop() {
			this.isStop = true;
			//es.shutdownNow();
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

						if (isStop)
							return;

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
						if (processLineCount % 30000 == 0) {
							Runtime.getRuntime().gc();
						}						
						
						// TODO　UI更新
						final long ngCount = _csvWriter.getNgCount();
						final String outputFilePath = _csvWriter.getFilePath();

						if (display.isDisposed())
							return;
						display.asyncExec(new Runnable() {

							@Override
							public void run() {

								// 全部処理完了の場合、計算結果ファイルのパスを表示する
								if (processLineCount == _totalLineCount) {
									es.shutdown();

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

}
