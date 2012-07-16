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
 * Multi-Thread�Ńo�b�`�������s��
 * 
 * @author btchoukug
 * 
 */
public class Batch {

	//private static Logger logger = LogFactory.getInstance(Batch.class);

	private static final int DEFAULT_POOL_SIZE = 2;

	/** �f�t�H���g�L���[�̃T�C�Y */
	private final int DEFAULT_QUEUE_SIZE = 2048;

	// TODO RateCalculator���G���[�������ꍇ�A��p�G���[�n���h���[�ŏ�������iUI�⃍�O�ƃf�[�^�ʐM�j
	private IBatchErrorHandler errorHandler;

	// private String code;

	/** ���[�g�v�Z���W���[�� */
	private final RateCalculator[] rcs;
	
	/**�o�b�`���s�̃C���v�b�g�f�[�^�̃t�B���^�[*/
	private final IBatchDataFilter filter;

	private boolean isStarted = false;
	private boolean isStop = false;
	
	private volatile boolean cancelled = false;

	/**�v�ZThread�̐�*/
	private int maxCalculatorNumber = 2;
	
	/**�v�ZThread�̊Ԃɐ���p*/
	private CyclicBarrier barrier;

	private ExecutorService pool;

	/** �w��̃L���[�T�C�Y */
	// private int queueSize;

	private BlockingQueue<BatchTask> inputQueue = new LinkedBlockingQueue<BatchTask>(
			DEFAULT_QUEUE_SIZE);
	private BlockingQueue<BatchTask> outputQueue = new LinkedBlockingQueue<BatchTask>(
			DEFAULT_QUEUE_SIZE);

	/** BT��ʂ���w��̌v�Z�Ώی��� */
	private Formula currentFormula;
	/** ���̎Z�� */
	private Formula specialFormula;
	
	/** ���𔻒f�R�����i���̎��S�w���F100�ȏ�̏ꍇ�A�����G�ȊO�̏ꍇ�A�W���́j */
	private String deathIndexColumnName;
	/** ���̎��S�w�������N�l */
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
				System.out.println("���ׂČv�ZThread�����~�����A�I���W�����o��");				
			}
			
		});

		this.errorHandler = new DefaultBatchErrorHandler(this);
		
		/*
		 * TODO ���i�R�[�h���Factory����Y������Instance���擾���悤�ɉ��P
		 * e.g:this.filter = BatchDataFilterFactory.getInstance(code);
		 */
		
		this.filter = new DefaultBatchDataFilter(); 

		this.rcs = new RateCalculator[this.maxCalculatorNumber];
		for (int i = 0; i < this.maxCalculatorNumber; i++) {
			// lazy���[�h�ŏ�����
			this.rcs[i] = new RateCalculator(code, cacheEnable, true, formulaMgr);
		}

	}

	public void setEnableCache(boolean cacheEnable) {
		for (RateCalculator rc : this.rcs) {
			rc.getContext().setCacheEnabled(cacheEnable);
		}
	}

	/**
	 * �o�b�`�̌v�Z�J�e�S�����w�肷��
	 * 
	 * @param cate
	 * @return
	 * @throws FmsDefErrorException 
	 */
	public boolean setCalculateCategory(String cate) throws FmsDefErrorException {

		if (StringUtils.isBlank(cate))
			throw new IllegalArgumentException("�v�Z�J�e�S���ݒ肭������");

		for (RateCalculator rc : this.rcs) {
			rc.setCalculateCate(new String[] { cate });
		}

		// BT���s���̏ꍇ�A��������H
		// thread pool restart?
		return false;

	}

	/**
	 * csv������data�t�@�C������ǂݍ��񂾃��[�g�L�[�͓����i�Ɏg���郌�[�g�L�[�� ��v���Ă��邩�ǂ����`�F�b�N���s��
	 * 
	 * @return
	 */
	public boolean validateInputFromFile() {
		return true;
	}

	/**
	 * �o�b�`�v�Z�����~����邩
	 * 
	 * @return
	 */
	public boolean isStarted() {
		return this.isStarted;
	}

	/**
	 * ���ׂ�runnable���I�������A�I������
	 * 
	 * @throws InterruptedException
	 */
	public void shutdown() throws InterruptedException {
		pool.shutdown();
	}

	/**
	 * ���s��thread������ꍇ�Afalse��Ԃ�
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
	 * �o�b�`�����𒆎~����
	 */
	public void stop() {
		this.isStop = true;
		this.isStarted = false;
	}
	
	public void cancel() {
		this.cancelled = true;
	}

	/**
	 * �J�����g�v�Z�Ώۂ̎Z�����w�肷��
	 * @param formula
	 */
	public void setCurrentFormula(Formula formula) {
		this.currentFormula = formula;
	}

	/**
	 * �t�H���_�[�P�ʂŕ����t�@�C�����v�Z����
	 * 
	 * @param adapter
	 * @param dataLayout
	 */
	public void readByAdapter(final BatchInputAdapter adapter,
			final BatchDataLayout dataLayout) {

		while (adapter.haveNextFile()) {

			try {
				IRateKeyReader reader = adapter.getRateKeyReader();

				// �g���q�u.dat�v�̃t�@�C�����[�_�̏ꍇ�A�f�[�^���C�A�E�g�����[�h
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
	 * �o�b�`�������s��
	 * @throws InterruptedException 
	 */
	private void _exec() {

		// �v�Z�����w�肵�Ă��邩�ǂ���
		if (this.currentFormula == null) {
			throw new IllegalArgumentException("Batch�Ɍv�Z�������������w�肵�Ă�������");
		}

		// ���s����CPU�̃R�A�̐��ɑ�������Thread���N������
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

							// �ŏI�^�X�N���ǂ������f����
							if (task.isEOF()) {
								// �ق��̌v�Z���Ă���Thread�Ɂu�v�Z���~�v��ʒm����
								isStop = true;
								// �J�����gThread�𒆎~
								break;
							}

							boolean errFlg = false;
							
							if (task.getRateKeys().containsKey(Const.ERRORDATAFLAG)) {
								errFlg = task.getRateKeys().get(Const.ERRORDATAFLAG) == 0 ? true : false;
							}
							
							if(!errFlg) {
								
								rc.setRateKeys(task.getRateKeys());
								Map<String, Double> results = new HashMap<String, Double>();
								
								//�f�t�H���g�Ƃ��āA�W���̂̃R���y�A�̃}�b�s���O���g��
								String curFormulaDesc = currentFormula.getDesc();
								String curFormulaName = currentFormula.getName();
								results.put(curFormulaDesc, rc.calculate(curFormulaName));
								
								// �������W���̂����f����
								if (task.getRateKeys().containsKey(deathIndexColumnName)) {
									
									int deathIndexVal = task.getRateKeys().get(deathIndexColumnName).intValue();
									//�����̏ꍇ�A�����̃R���y�A�̃}�b�s���O���g��
									curFormulaDesc = specialFormula.getDesc();
									curFormulaName = specialFormula.getName();
									
									if (deathIndexVal > deathIndexRankVal) {
										//task.setSpecial(true);
										results.put(curFormulaDesc, rc.calculate(curFormulaName));
									} else {
										//TODO ���̈ȊO�̏ꍇ�A���̂̃��[�g�͌v�Z���Ȃ��A�[���Ƃ���
										results.put(curFormulaDesc, 0d);
									}
								}								
								
								task.setFormulaResults(results);
								
							}

							// �o�b�`�^�X�N�����ʏo�̓��W���[���ɓn��
							outputQueue.put(task);
						} catch (Exception e) {
							errorHandler.handleError(e);
							// �G���[�������ꍇ�A��U���~
							stop();
							break;
						} 
					}
					
					try {
						//TODO ���L�̃R�[�h��InterruptedException���N�����
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
	 * �C���v�b�g�ǂݍ��ނȂ���A�^�X�N�L���[�ɒǉ����� ���[�g�L�[���k���Ȃ�ƁA�I���Ƃ���
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
						
						// ���[�g�L�[��BatchTask�Ƃ��ăC���v�b�g�L���[�ɒǉ�
						BatchTask task = new BatchTask(rateKeys, reader.getReadLineNum());
						inputQueue.put(task);

						// �t�@�C���̍Ō�̍s�܂œǂݐs���ꍇ�A���邢�͊O���Œ��~����ꍇ
						if (rateKeys == null || isStop) {
							// �I���W����ݒ�
							task.setEOF(true);
							// ���[�g�L�[���k���Ȃ�ƁA�I���Ƃ���
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
	 * �A�E�g�v�b�g�L���[����v�Z�^�X�N���擾���ĂȂ���A csv�t�@�C���ɏ�������
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
						// �ŏI�^�X�N���ǂ������f����
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
					// �v�Z�I���̏�Ԃ��Z�b�g
					stop();
				}
			}

		});

	}

	public void exec(final IRateKeyReader reader, final IBatchWriter writer) {
		// BT�̏ꍇ�A���\����̂��߁A���O�I�t�Ƃ���
		LogFactory.setLoggerLevel(Level.OFF);

		if (pool.isShutdown()) {
			pool = Executors.newFixedThreadPool(DEFAULT_POOL_SIZE
					+ maxCalculatorNumber);
		}

		// ������Ԃ��N���A
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
