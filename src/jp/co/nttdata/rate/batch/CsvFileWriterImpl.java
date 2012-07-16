package jp.co.nttdata.rate.batch;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.LinkedBlockingQueue;
import jp.co.nttdata.rate.exception.RateException;
import jp.co.nttdata.rate.model.CategoryManager;
import jp.co.nttdata.rate.model.rateKey.RateKey;
import jp.co.nttdata.rate.model.rateKey.RateKeyManager;
import jp.co.nttdata.rate.util.Const;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.time.DateFormatUtils;

/**
 * 
 * �v�Z�̃��[�g�L�[�ƌv�Z���ʂ�ҏW����CSV�t�@�C���ɏo�͂��� 
 * @author btchoukug
 * 
 */
public class CsvFileWriterImpl implements IBatchWriter {
	
	/** CSV�t�@�C���̊g���q */
	protected final String CSV_SUFFIX = ".csv";
	
	private static final int TASK_SIZE = 4096;
	
	public static LinkedBlockingQueue<BatchTask> taskQueue = new LinkedBlockingQueue<BatchTask>(TASK_SIZE);
	
	/**csv�t�@�C���̍��ږ��w�b�_*/
	protected String header;
	protected String errorHeader;

	/** �o�͏��ԂŃL�[�ƌv�Z���̖����i�[ */
	protected String[] headerArray;
	protected Map<String, Double> rateKeys;

	// TODO �p�X��UI����w�肷��@�{�@���i�R�[�h���T�u�p�X�Ƃ���
	private String resultBasePath = "batch\\output\\";

	protected static final int CONTENT_SIZE = 128;
	
	/** ���t�̃t�H�[�}�b�g */
	protected static final String YYYYMMDD = "yyyyMMdd";
	/** �R���} */
	private static final char COMMA = ',';
	/** �s�I������ */
	private static final String EOR = "\r\n";
	
	private boolean NG = true;
	private boolean COMMON = false;

	/**�G�N�Z���V�[�g�P�ʂň�����ő匏���ŏo�̓t�@�C���𕪊�*/
	private static final int EXCEL_SHEET_MAXLINE = 65000;

	private static final char CHAR_COMMA = ',';

	private static final String NG_PREFIX = "NG_";

	private static final String UNDERBAR = "_";

	private static final String LINE_NO_HEADER = "No.";

	protected String ngFileName;
	protected String fileName;
	protected String errorFileName;
	protected String filePath;
	protected File file;
	protected FileWriter ngFileWriter;
	protected FileWriter fileWriter;
	protected FileWriter errorFileWriter;
	
	protected String ngFileWriterName;
	protected String fileWriterName;
	
	/**���̃t�@�C���ɏ㏑��*/
	protected boolean appendEnable = false;
	/**�U����������Ƃ��ăt�@�C������邩*/
	protected boolean splitFileEnable = true;
	
	protected String fileSuffix;

	/** �s�� */
	protected long lineCount = 0;
	protected long NGlineCount = 0;
	protected long errorLineCount = 0;
	protected long rightLineCount = 0;
	/** �t�@�C���� */
	protected int ngFileCount = 0;
	protected int fileCount = 0;
	protected int errorFileCount = 0;
	
	private String outputFileName;
	
	private List<String> listKeyName;
	
	/**�v�Z���ʔ�rIF*/
	protected ICalculateResultComparator comparator;
	
	/**�o�̓��[�h�iNG�P�[�X�̂ݏo�͂��j*/
	protected boolean NGOnly = false;

	/**TODO �O���ŏ��i�R�[�h������*/
	protected String code; 
	
	public CsvFileWriterImpl(String code, File inputFile,
			Map<String,String> compareObjectMapping, boolean ngOnly, String cateName, String dest) throws RateException {
		
		String path = inputFile.getAbsolutePath();
		setResultBasePath(path.substring(0,path.lastIndexOf(File.separator) + 1));
		
		this.NGOnly = ngOnly;
		
		_init(code.substring(0, 3), inputFile.getName(), compareObjectMapping, cateName, dest);
	}
	
	public CsvFileWriterImpl(String code, String fileName,
			Map<String,String> compareObjectMapping, boolean ngOnly, String cateName, String dest) throws RateException {
		if (StringUtils.isBlank(fileName)) {
			throw new IllegalArgumentException("�t�@�C�������w�肭�������B");
		}
		
		this.NGOnly = ngOnly;
		
		_init(code.substring(0, 3), fileName, compareObjectMapping, cateName, dest);
		
	}
	
	private void _init(String code, String fileName, Map<String, String> compareObjectMapping, String cateName, String dest) throws RateException {
		
		this.comparator = new DefaultCalculateResultComparator(); 
		this.comparator.setCompareMapping(compareObjectMapping);
		
		if (StringUtils.isEmpty(code)) {
			throw new IllegalArgumentException("���i�R�[�h���w�肭�������B");
		}
		this.code = code.substring(0, 3);
		
		listKeyName = new ArrayList<String>();
		
		//�@UI�ݒ�ǂ���g���郌�[�g�L�[���擾���A�o�͑ΏۂƂ���
		for(RateKey rateKey : CategoryManager.getInstance().getCateInfo(cateName).getKeyList()){
			listKeyName.add(rateKey.getName());
		}
		//�@��r�Ώۂ��o�͑ΏۂƂ���
		for(String keyName : StringUtils.split(dest, CHAR_COMMA)) {
			listKeyName.add(keyName);
		}
		
		//�o��NG�t�@�C�����̓C���v�b�g�t�@�C�����{���t�Ƃ���i�C���v�b�g�t�@�C���̊g���q���O���j			
		outputFileName = fileName.substring(0,fileName.lastIndexOf(Const.DOT))
			+ UNDERBAR +DateFormatUtils.format(new Date(System.currentTimeMillis()), YYYYMMDD);
		
		ngFileWriterName = NG_PREFIX + outputFileName;
		if(!this.NGOnly) {
			fileWriterName = fileName;
		}
	}
	
	public CsvFileWriterImpl(String code, String category, boolean ngOnly) throws RateException {
		if (code == null || StringUtils.isEmpty(code)) {
			throw new IllegalArgumentException("���i�R�[�h���w�肭�������B");
		}

		if (category == null || StringUtils.isEmpty(category)) {
			throw new IllegalArgumentException("�v�Z�J�e�S���iPVW�j���w�肭�������B");
		}
		
		this.NGOnly = ngOnly;
		
		// ���i�R�[�h���ƂɃT�u�t�H���_���쐬���āA�v�Z���ʂ��o�͂���
		this.filePath = resultBasePath + File.separator + code.substring(0, 3);
		
		// �t�@�C�����͌v�Z�J�e�S���{���t�Ƃ���
		String fileName = filePath
				+ File.separator
				+ category
				+ DateFormatUtils.format(new Date(System
						.currentTimeMillis()), YYYYMMDD);

		ngFileWriterName = NG_PREFIX + outputFileName;
		
		if(!this.NGOnly) {
			fileWriterName = fileName;
		}
	}

	private FileWriter _createErrorFile() throws IOException {
		
		String errorFileName = "ErrorData" + "_" + this.outputFileName;
		
		// �����t�@�C���̏ꍇ�A���̃t�@�C�����ɏ��Ԃ�t��
		if (this.errorFileCount > 0) {
			errorFileName = errorFileName + "_" + this.errorFileCount;
		}
		
		// �t�@�C�������݂��Ȃ��ꍇ�A�t�@�C�����쐬����G���݂��Ă���ꍇ�A���̃t�@�C�����㏑��
		file = new File(_getResultBasePath() + errorFileName + _getFileSuffix());
		
		if (!file.exists()) {
			
			File dir = new File(_getResultBasePath());
			if (!dir.exists() && !dir.isDirectory()) {
				dir.mkdirs();
			}
			file.createNewFile();
		}

		return new FileWriter(file, this.appendEnable);// �f�t�H���g�ꍇ�ǉ����[�h�ł͂Ȃ�
		
	}
	
	private FileWriter _createFile(String fileName, boolean ngOnly) throws IOException {
		
		if(ngOnly) {
			//�����t�@�C���𐶐����邽�߁A��U�ۑ�����
			this.ngFileName = fileName;
			
			//�����t�@�C���̏ꍇ�A���̃t�@�C�����ɏ��Ԃ�t��
			if (this.ngFileCount > 0) {
				fileName = fileName + "_" + this.ngFileCount;
			}
		} else {
			//�����t�@�C���𐶐����邽�߁A��U�ۑ�����
			this.fileName = fileName;
			
			//�����t�@�C���̏ꍇ�A���̃t�@�C�����ɏ��Ԃ�t��
			if (this.fileCount > 0) {
				fileName = fileName + "_" + this.fileCount;
			}
		}
		
		// �t�@�C�������݂��Ȃ��ꍇ�A�t�@�C�����쐬����G���݂��Ă���ꍇ�A���̃t�@�C�����㏑��
		file = new File(_getResultBasePath() + fileName + _getFileSuffix());
		
		if (!file.exists()) {
			
			File dir = new File(_getResultBasePath());
			if (!dir.exists() && !dir.isDirectory()) {
				dir.mkdirs();
			}
			file.createNewFile();
		}

		return new FileWriter(file, this.appendEnable);// �f�t�H���g�ꍇ�ǉ����[�h�ł͂Ȃ�
		
		
	}

	/**
	 * �p�X�͏��i�R�[�h���T�u�p�X�Ƃ���
	 * @return
	 */
	private String _getResultBasePath() {
		return resultBasePath + this.code + Const.SEPARATOR;
	}

	/**
	 * �t�@�C���̊g���q���擾<br>
	 * �f�t�H���g�ꍇ�A.csv��Ԃ�
	 * @return
	 */
	private String _getFileSuffix() {
		return this.fileSuffix == null ? CSV_SUFFIX : this.fileSuffix;
	}

	/***
	 * �Ώۃt�@�C���ɂ��ׂĂ̓��e���������񂾌�A�t�@�C�����N���[�Y����
	 */
	@Override
	public void close(String type) {
		try {
			if(type.equals("NG")) {
				if (ngFileWriter != null)
					ngFileWriter.close();
			} else if(type.equals("ERROR")) {
				if (errorFileWriter != null)
					errorFileWriter.close();
			} else {
				if (fileWriter != null)
					fileWriter.close();
			}
			
		} catch (IOException e) {
			throw new RuntimeException("�t�@�C�����N���[�Y����Ƃ��ɃG���[���������܂��܂����B���m�F���������B", e);
		} finally {
			if(type.equals("NG")) {
				ngFileWriter = null;
			} else if(type.equals("ERROR")) {
				errorFileWriter = null;
			} else {
				fileWriter = null;
			}
			
		}
	}
	
	/***
	 * �Ώۃt�@�C���ɂ��ׂĂ̓��e���������񂾌�A�t�@�C�����N���[�Y����
	 */
	@Override
	public void close() {
		close("NG");
		close("ERROR");
		close("COMMON");
	}
	
	/**
	 * NG�̂ݏo�͂Ǝw�肷��
	 * @param NGOnly
	 */
	public void enableNGOnly(boolean NGOnly) {
		this.NGOnly = NGOnly;
	}

	@Override
	public void output(Map<String, Double> result, long taskNo) {
		// �J�E���^�[�ɂP���v���X
		this.lineCount++;

		try {
			
			boolean errorFlag = false;
			
			if (this.rateKeys.containsKey(Const.ERRORDATAFLAG)) {
				errorFlag = this.rateKeys.get(Const.ERRORDATAFLAG) == 0 ? true : false;
			}
			
			this.rateKeys.remove(Const.ERRORDATAFLAG);
			
			// ���[�g�L�[��ҏW
			StringBuffer sbContent = new StringBuffer(CONTENT_SIZE);
			if (this.headerArray != null) {
				// �w�b�_�w��̏ꍇ�A�w�菇�Ԃǂ���ɍ��ڒl���擾
				for (String key : this.headerArray) {
					sbContent.append(Const.COMMA).append(this.rateKeys.get(key));
				}
			} else {
				// ���[�g�L�[�f�t�H���g���Ԃŏo��
				for (String key : this.rateKeys.keySet()) {
					if (listKeyName.contains(key)) {
						sbContent.append(Const.COMMA).append(this.rateKeys.get(key));
					}					
				}
				
			}
			
			if(!errorFlag) {
				
				this.rightLineCount++;
				
				// �v�Z���ʂ̍��ږ���ǉ�
				if (this.rightLineCount == 1) {
					StringBuffer sbHeader = new StringBuffer(this.header);
					for (String calObjName : result.keySet()) {
						sbHeader.append(Const.COMMA).append(calObjName);
					}
					this.header = this.comparator.appendCompareHeader(sbHeader).toString();
				}
				
				// �v�Z�������ʂ�ҏW
				for (Double value : result.values()) {
					sbContent.append(Const.COMMA).append(value);
				}
				
				// ��r���Ɣ�r��͉�ʂ���w��ł���
				boolean isOK = this.comparator.compare(sbContent, rateKeys, result);
				
				// ���̃f�[�^�̍s�ԍ���擪�ɑ}������
				sbContent.insert(0, taskNo).append(EOR).toString();
				
				if(!isOK) {
					this.NGlineCount++;
					
					// ����̏ꍇ�A�w�b�_����������
					if (this.NGlineCount % EXCEL_SHEET_MAXLINE == 1) {
						this.ngFileWriter = _createFile(ngFileWriterName, NG);
						this.ngFileWriter.write(this.header + EOR);
					}
					
					// NG�̂ݏo�͂��w�肷��ƁA��r���ʂ�NG�̏ꍇ�A�f�t�H���g�Ƃ���CSV�t�@�C���ɏo��
					this.ngFileWriter.write(sbContent.toString());
				}
				
				if(!this.NGOnly){
						// ����̏ꍇ�A�w�b�_����������
						if (this.rightLineCount % EXCEL_SHEET_MAXLINE == 1) {
							this.ngFileWriter = _createFile(fileWriterName, COMMON);
							this.fileWriter.write(this.header + EOR);
						}
						
						// NG�̂ݏo�͂��w�肷��ƁA��r���ʂ�NG�̏ꍇ�A�f�t�H���g�Ƃ���CSV�t�@�C���ɏo��
						this.fileWriter.write(sbContent.toString());
					
				}
				
				// �G�N�Z���ł݂���悤�ɁA6�������Ƀt�@�C���𕪊�����
				if (this.NGlineCount % EXCEL_SHEET_MAXLINE == 0 && this.NGlineCount > 0) {
					//�܂��A�J�����g�t�@�C�����N���[�Y
					this.close("NG");
					
					//�����čs���N���A
					//this.lineCount = 0;

					//�t�@�C�������v���X���ĐV����fileWriter��V�K
					this.ngFileCount++;
					this.ngFileWriter = _createFile(this.ngFileName, NG);
				}
				
				// �G�N�Z���ł݂���悤�ɁA6�������Ƀt�@�C���𕪊�����
				if (!this.NGOnly && this.rightLineCount % EXCEL_SHEET_MAXLINE == 0 && this.rightLineCount > 0) {
					//�܂��A�J�����g�t�@�C�����N���[�Y
					this.close("COMMON");
					
					//�����čs���N���A
					//this.lineCount = 0;

					//�t�@�C�������v���X���ĐV����fileWriter��V�K
					this.fileCount++;
					this.fileWriter = _createFile(this.fileName, COMMON);
				}
			} else {
				
				this.errorLineCount++;
				
				if(this.errorFileWriter == null) {
					errorFileWriter = _createErrorFile();
				}
				
				// ����̏ꍇ�A�w�b�_����������
				if (this.errorLineCount % EXCEL_SHEET_MAXLINE == 1) {
					// TODO �G���[�f�[�^�̏ꍇ�A�G���[�f�[�^�̍s�Ԗڂ��o��
					if (this.errorLineCount == 1) {
						this.errorHeader += Const.COMMA + "�G���[�f�[�^�ꏊ�i�s�j";					
					}

					this.errorFileWriter.write(this.errorHeader + EOR);
				}
				
				sbContent.append(Const.COMMA).append(this.lineCount);
				
				sbContent.deleteCharAt(0).append(EOR).toString();
				
				// NG�̂ݏo�͂��w�肷��ƁA��r���ʂ�NG�̏ꍇ�A�f�t�H���g�Ƃ���CSV�t�@�C���ɏo��
				this.errorFileWriter.write(sbContent.toString());
				
				// �G�N�Z���ł݂���悤�ɁA6�������Ƀt�@�C���𕪊�����
				if (this.errorLineCount % EXCEL_SHEET_MAXLINE == 0) {
					//�܂��A�J�����g�t�@�C�����N���[�Y
					this.close("ERROR");
					
					//�����čs���N���A
					//this.lineCount = 0;

					//�t�@�C�������v���X���ĐV����fileWriter��V�K
					this.errorFileCount++;
					this.errorFileWriter = _createErrorFile();
				}
				
			}

		} catch (IOException e) {
			throw new RuntimeException(e);
		}

	}

	/**
	 * ���͂̃��[�g�L�[���Z�b�g
	 */
	public void setInput(Map<String, Double> input) {
		this.rateKeys = input;
		
		// �w�b�_�͎w�肵�Ă��Ȃ��ꍇ�A���[�g�L�[�̖����w�b�_��ҏW
		if (StringUtils.isEmpty(this.header)) {
						
			StringBuffer sb = new StringBuffer(CONTENT_SIZE);
			for (String key : this.rateKeys.keySet()) {
				
				if(listKeyName.contains(key)) {
					RateKey rateKey = RateKeyManager.getRateKeyDef(key);
					if(rateKey != null) {
						// ���[�g�L�[�̏ꍇ�A���{�ꖼ�ƂƂ��ɏo�͂���
						sb.append(Const.COMMA).append(rateKey.getLabel() + "(" + key + ")");
					} else {
						// ���[�g�L�[�ł͂Ȃ��ꍇ�A�ႦP���[�g�̂悤�ȍ��ځA��`���ꂽ�p�ꖼ�����̂܂܏o�͂���
						sb.append(Const.COMMA).append(key);
					}
				}
				
			}
			
			// �s�ڂ��w�b�_��1�ԖڂƂ��ďo��
			this.header = sb.insert(0, LINE_NO_HEADER).toString();
			
		}
		
		this.errorHeader = this.header;
		//TODO XML��`�ǂ����ratekey�̊��������`�F�b�N����̂�
	}

	@Override
	public void setHeader(String header) {

		if (StringUtils.isEmpty(header)) {
			throw new IllegalArgumentException("�w�b�_�ɂ̓k���A��ȊO�w�肭������");
		}
		
		this.headerArray = StringUtils.split(header, COMMA);
		if (this.headerArray.length == 0) {
			throw new IllegalArgumentException("�w�b�_�w��t�H�[�}�b�g�s��");
		}
				
		this.header = header;

	}

	public void setAppendEnable(boolean appendEnable) {
		this.appendEnable = appendEnable;
	}
	
	/**
	 * 6�������Ƀt�@�C���𕪊����邩
	 * @param splitEnable
	 */
	public void setSplitFileEnable(boolean splitEnable) {
		this.splitFileEnable = splitEnable;
	}

//	public String getFileName() {
//		return this.file.getName();
//	}
	
	public long getLineCount() {
		return this.lineCount;
	}
	
	public long getNgCount() {
		return this.NGlineCount;
	}
	
	public long getErrorLineCount() {
		return this.errorLineCount;
	}
	
	/**
	 * ���������t�@�C���̃p�X���擾
	 * @return
	 */
	public String getFilePath() {
		return this.file.getAbsolutePath();
	}
	
	/**
	 * ���������t�H���_�̃p�X���擾
	 * @return
	 */
	public String getFolderPath() {
		return resultBasePath;
	}
	
	/**
	 * �x�[�Y�p�X���w��
	 * @param resultBasePath
	 */
	public void setResultBasePath(String resultBasePath) {
		this.resultBasePath = resultBasePath;
	}

}
