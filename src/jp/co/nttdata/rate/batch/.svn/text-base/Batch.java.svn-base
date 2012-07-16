package jp.co.nttdata.rate.batch;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Level;
import jp.co.nttdata.rate.batch.dataConvert.BatchDataLayout;
import jp.co.nttdata.rate.exception.FmsDefErrorException;
import jp.co.nttdata.rate.exception.RateException;
import jp.co.nttdata.rate.fms.calculate.RateCalculateContext;
import jp.co.nttdata.rate.fms.calculate.RateCalculator;
import jp.co.nttdata.rate.log.LogFactory;
import jp.co.nttdata.rate.model.formula.Formula;
import jp.co.nttdata.rate.model.formula.FormulaManager;
import jp.co.nttdata.rate.util.Const;

/**
 * Multi-Threadでバッチ処理を行う
 * 
 * @author btchoukug
 * 
 */
public class Batch {

	//private static Logger logger = LogFactory.getInstance(Batch.class);

	private static final int DEFAULT_POOL_SIZE = 2;

	/** デフォルトキューのサイズ */
	private final int DEFAULT_QUEUE_SIZE = 2048;

	// TODO RateCalculatorがエラーだった場合、専用エラーハンドラーで処理する（UIやログとデータ通信）
	private IBatchErrorHandler errorHandler;

	// private String code;

	/** レート計算モジュール */
	private final RateCalculator[] rcs;
	
	/**バッチ実行のインプットデータのフィルター*/
	private final IBatchDataFilter filter;

	private boolean isStarted = false;
	private boolean isStop = false;
	
	private volatile boolean cancelled = false;

	/**計算Threadの数*/
	private int maxCalculatorNumber = 2;
	
	/**計算Threadの間に制御用*/
	private CyclicBarrier barrier;

	private ExecutorService pool;

	/** 指定のキューサイズ */
	// private int queueSize;

	private BlockingQueue<BatchTask> inputQueue = new LinkedBlockingQueue<BatchTask>(
			DEFAULT_QUEUE_SIZE);
	private BlockingQueue<BatchTask> outputQueue = new LinkedBlockingQueue<BatchTask>(
			DEFAULT_QUEUE_SIZE);

	/** BT画面から指定の計算対象公式 */
	private Formula currentFormula;
	/** 特体算式 */
	private Formula specialFormula;
	
	/** 特条判断コラム（特体死亡指数：100以上の場合、特条；以外の場合、標準体） */
	private String deathIndexColumnName;
	/** 特体死亡指数ランク値 */
	private int deathIndexRankVal = 100;

	public Batch(String code, boolean cacheEnable, FormulaManager formulaMgr) throws FmsDefErrorException {
		// this.code = code;
		int processorNum = Runtime.getRuntime().availableProcessors();
		this.maxCalculatorNumber = processorNum > maxCalculatorNumber ? maxCalculatorNumber
				: processorNum;
		this.pool = Executors.newFixedThreadPool(DEFAULT_POOL_SIZE
				+ maxCalculatorNumber);
		this.barrier = new CyclicBarrier(maxCalculatorNumber, new Runnable(){

			@Override
			public void run() {
				BatchTask EOFTask = new BatchTask(null);
				EOFTask.setEOF(true);
				try {
					outputQueue.put(EOFTask);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				System.out.println("すべて計算Threadが中止した、終了標識を出力");				
			}
			
		});

		this.errorHandler = new DefaultBatchErrorHandler(this);
		
		/*
		 * TODO 商品コードよりFactoryから該当するInstanceを取得しように改善
		 * e.g:this.filter = BatchDataFilterFactory.getInstance(code);
		 */
		
		this.filter = new DefaultBatchDataFilter(); 

		this.rcs = new RateCalculator[this.maxCalculatorNumber];
		for (int i = 0; i < this.maxCalculatorNumber; i++) {
			// lazyモードで初期化
			this.rcs[i] = new RateCalculator(code, cacheEnable, true, formulaMgr);
		}

	}

	public void setEnableCache(boolean cacheEnable) {
		for (RateCalculator rc : this.rcs) {
			rc.getContext().setCacheEnabled(cacheEnable);
		}
	}

	/**
	 * バッチの計算カテゴリを指定する
	 * 
	 * @param cate
	 * @return
	 * @throws FmsDefErrorException 
	 */
	public boolean setCalculateCategory(String cate) throws FmsDefErrorException {

		if (StringUtils.isBlank(cate))
			throw new IllegalArgumentException("計算カテゴリ設定ください");

		for (RateCalculator rc : this.rcs) {
			rc.setCalculateCate(new String[] { cate });
		}

		// BT実行中の場合、何かする？
		// thread pool restart?
		return false;

	}

	/**
	 * csv或いはdataファイルから読み込んだレートキーは当商品に使われるレートキーと 一致しているかどうかチェックを行う
	 * 
	 * @return
	 */
	public boolean validateInputFromFile() {
		return true;
	}

	/**
	 * バッチ計算が中止されるか
	 * 
	 * @return
	 */
	public boolean isStarted() {
		return this.isStarted;
	}

	/**
	 * すべてrunnableが終わったら、終了する
	 * 
	 * @throws InterruptedException
	 */
	public void shutdown() throws InterruptedException {
		pool.shutdown();
	}

	/**
	 * 執行中threadがある場合、falseを返す
	 * 
	 * @return
	 * @throws InterruptedException
	 */
	public void shutdownAndAwaitTermination() {

		pool.shutdown(); // Disable new tasks from being submitted

		try {
			// Wait a while for existing tasks to terminate
			if (!pool.awaitTermination(10, TimeUnit.SECONDS)) {
				pool.shutdownNow(); // Cancel currently executing tasks
				// Wait a while for tasks to respond to being cancelled
				if (!pool.awaitTermination(10, TimeUnit.SECONDS)) {
					System.err.println("Pool did not terminate");
				}
			}
		} catch (InterruptedException ie) {
			// (Re-)Cancel if current thread also interrupted
			pool.shutdownNow();
			// Preserve interrupt status
			Thread.currentThread().interrupt();
		}

	}

	/**
	 * バッチ処理を中止する
	 */
	public void stop() {
		this.isStop = true;
		this.isStarted = false;
	}
	
	public void cancel() {
		this.cancelled = true;
	}

	/**
	 * カレント計算対象の算式を指定する
	 * @param formula
	 */
	public void setCurrentFormula(Formula formula) {
		this.currentFormula = formula;
	}

	/**
	 * フォルダー単位で複数ファイルを計算する
	 * 
	 * @param adapter
	 * @param dataLayout
	 */
	public void readByAdapter(final BatchInputAdapter adapter,
			final BatchDataLayout dataLayout) {

		while (adapter.haveNextFile()) {

			try {
				IRateKeyReader reader = adapter.getRateKeyReader();

				// 拡張子「.dat」のファイルリーダの場合、データレイアウトをロード
				if (reader instanceof DatRateKeyReaderImpl) {
					DatRateKeyReaderImpl datReader = (DatRateKeyReaderImpl) reader;
					datReader.setDataLayout(dataLayout);
				}

				_accept(reader);

			} catch (Exception e) {
				errorHandler.handleError(e);
			}

		}
	}

	/**
	 * バッチ処理を行う
	 * @throws InterruptedException 
	 */
	private void _exec() {

		// 計算公式指定しているかどうか
		if (this.currentFormula == null) {
			throw new IllegalArgumentException("Batchに計算したい公式を指定してください");
		}

		// 実行環境のCPUのコアの数に相当するThreadを起動する
		for (int i = 0; i < this.maxCalculatorNumber; i++) {

			final RateCalculator rc = rcs[i];

			pool.execute(new Runnable() {

				@Override
				public void run() {

					while (!cancelled) {

						try {

							if (isStop) {
								break;
							}

							BatchTask task = inputQueue.take();

							// 最終タスクかどうか判断する
							if (task.isEOF()) {
								// ほかの計算しているThreadに「計算中止」を通知する
								isStop = true;
								// カレントThreadを中止
								break;
							}

							boolean errFlg = false;
							
							if (task.getRateKeys().containsKey(Const.ERRORDATAFLAG)) {
								errFlg = task.getRateKeys().get(Const.ERRORDATAFLAG) == 0 ? true : false;
							}
							
							if(!errFlg) {
								
								rc.setRateKeys(task.getRateKeys());
								Map<String, Double> results = new HashMap<String, Double>();
								
								//デフォルトとして、標準体のコンペアのマッピングを使う
								String curFormulaDesc = currentFormula.getDesc();
								String curFormulaName = currentFormula.getName();
								results.put(curFormulaDesc, rc.calculate(curFormulaName));
								
								// 特条か標準体か判断する
								if (task.getRateKeys().containsKey(deathIndexColumnName)) {
									
									int deathIndexVal = task.getRateKeys().get(deathIndexColumnName).intValue();
									//特条の場合、特条のコンペアのマッピングを使う
									curFormulaDesc = specialFormula.getDesc();
									curFormulaName = specialFormula.getName();
									
									if (deathIndexVal > deathIndexRankVal) {
										//task.setSpecial(true);
										results.put(curFormulaDesc, rc.calculate(curFormulaName));
									} else {
										//TODO 特体以外の場合、特体のレートは計算しない、ゼロとする
										results.put(curFormulaDesc, 0d);
									}
								}								
								
								task.setFormulaResults(results);
								
							}

							// バッチタスクを結果出力モジュールに渡す
							outputQueue.put(task);
						} catch (Exception e) {
							errorHandler.handleError(e);
							// エラーだった場合、一旦中止
							stop();
							break;
						} 
					}
					
					try {
						//TODO 下記のコードはInterruptedExceptionを起きれる
						barrier.await();
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (BrokenBarrierException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}

				}

			});

		}
		
	}

	/**
	 * インプット読み込むながら、タスクキューに追加する レートキーがヌルなると、終了とする
	 * 
	 * @param reader
	 */
	private void _accept(final IRateKeyReader reader) {

		pool.execute(new Runnable() {
						
			@Override
			public void run() {
				try {

					while (!cancelled) {
						
						Map<String, Double> rateKeys = reader.readRateKeys();

						if (!filter.filter(rateKeys)) continue;
						
						// レートキーをBatchTaskとしてインプットキューに追加
						BatchTask task = new BatchTask(rateKeys, reader.getReadLineNum());
						inputQueue.put(task);

						// ファイルの最後の行まで読み尽し場合、あるいは外部で中止する場合
						if (rateKeys == null || isStop) {
							// 終了標識を設定
							task.setEOF(true);
							// レートキーがヌルなると、終了とする
							break;
						}
					}
				} catch (InterruptedException e) {
					errorHandler.handleError(e);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					errorHandler.handleError(e);
					e.printStackTrace();
				} catch (RateException e) {
					// TODO Auto-generated catch block
					errorHandler.handleError(e);
					e.printStackTrace();
				} finally {
					reader.close();
				}
			}

		});

	}

	/**
	 * アウトプットキューから計算タスクを取得してながら、 csvファイルに書き込む
	 * 
	 * @param writer
	 */
	private void _writeBy(final IBatchWriter writer) {

		pool.execute(new Runnable() {

			@Override
			public void run() {
				try {

					while (!cancelled) {

						BatchTask task = outputQueue.take();
						// 最終タスクかどうか判断する
						if (task.isEOF()) {
							break;
						}

						writer.setInput(task.getRateKeys());
						writer.output(task.getFormulaResults(), task.getTaskNo());
					}

				} catch (InterruptedException e) {
					errorHandler.handleError(e);
				} finally {
					writer.close();
					// 計算終了の状態をセット
					stop();
				}
			}

		});

	}

	public void exec(final IRateKeyReader reader, final IBatchWriter writer) {
		// BTの場合、性能向上のため、ログオフとする
		LogFactory.setLoggerLevel(Level.OFF);

		if (pool.isShutdown()) {
			pool = Executors.newFixedThreadPool(DEFAULT_POOL_SIZE
					+ maxCalculatorNumber);
		}

		// 内部状態をクリア
		this.isStarted = true;
		this.isStop = false;
		//stopCount.set(0);

		_accept(reader);
		_exec();
		_writeBy(writer);

	}

	public void setDeathIndexColumnName(String deathIndexColumnName) {
		this.deathIndexColumnName = deathIndexColumnName;
	}

	public String getDeathIndexColumnName() {
		return deathIndexColumnName;
	}

	public void setSpecialFormula(Formula formula) {
		this.specialFormula = formula;		
	}
	
	public void setDeathIndexRankVal(int rank) {
		this.deathIndexRankVal = rank;
	}

}
