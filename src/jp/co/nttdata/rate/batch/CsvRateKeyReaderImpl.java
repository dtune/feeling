package jp.co.nttdata.rate.batch;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.LineNumberReader;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.lang.StringUtils;

import jp.co.nttdata.rate.batch.dataConvert.BatchDataLayoutFactory;
import jp.co.nttdata.rate.batch.dataConvert.Csv2RateKeyConverterImpl;
import jp.co.nttdata.rate.batch.dataConvert.IRateKeyConverter;
import jp.co.nttdata.rate.exception.FmsDefErrorException;
import jp.co.nttdata.rate.exception.FmsRuntimeException;
import jp.co.nttdata.rate.exception.RateException;
import jp.co.nttdata.rate.util.CommonUtil;
import jp.co.nttdata.rate.util.Const;
import jp.co.nttdata.rate.util.PropertiesUtil;
/**
 * 
 * ���q�l����C���v�b�g�f�[�^��CSV�t�@�C���ɐ��`���������A
 * �P�s���ǂݍ���Ń��[�g�L�[Map�Ƃ����`�ŕԂ�
 * @author btchoukug
 *
 */
public class CsvRateKeyReaderImpl implements IRateKeyReader {
	
	public static final char DEFAULT_DELIMITER = ',';
	public static final char EQ = '=';
	public static final char SEMICOLON = ';';	

	private static final String INPUT_RYORITSU_SHIBETSU_CN = "�������ʋ敪CN";
	
	private static final int RADIX16 = 16;
	
	private String[] keyNames;
	
	private File file;
	private BufferedReader reader;
	
	private long lineCount = 0;
	
	/**�f�t�H���g�ő�v�Z�̌���*/
	private long maxlineNumber = Long.MAX_VALUE;
	private long totalLineCount = 0;
	private char delimiter;

	/**�V�����[�g�L�[�̕ϊ����W���[��*/
	private IRateKeyConverter convertor;

	private static Properties prop;
	private static String calculateCategory;
	static {
		prop = PropertiesUtil.getExternalProperties(Const.BT_DATA_CSV_PROPERIT_DIR + Const.BT_DATA_CSV_PROPERIT_FILENAME);
		calculateCategory = prop.getProperty(Const.CAlCULATE_TYPE_KEY, "");
	}
	
	public CsvRateKeyReaderImpl(String filePath) throws FmsDefErrorException {
		this(new File(filePath));
	}
	
	public CsvRateKeyReaderImpl(File file) throws FmsDefErrorException {
		this.file = file;
		_countTotalLineNum();
		this.convertor = new Csv2RateKeyConverterImpl(calculateCategory);
	}
	
	
	private void _countTotalLineNum() {
		try {
			LineNumberReader rf = null;
			rf = new LineNumberReader(new FileReader(file));
			long fileLength = file.length();

			if (rf != null) {
				rf.skip(fileLength);
				//���������擾
				this.totalLineCount = rf.getLineNumber();
				rf.close();
			}

			//�w�b�_�𑍌����������
			this.totalLineCount --;
						
		} catch (FileNotFoundException e) {
			throw new FmsRuntimeException(e);
		} catch (IOException e) {
			throw new FmsRuntimeException(e);
		}
	}
	
	/**
	 * �Œ�l���w�肷��<br>
	 * �w��l�̓t�@�C������ǂݍ��񂾍��ڒl�ɏ㏑������
	 * @param fixedValuesText
	 * @throws RateException 
	 */
	public void setFixedValues(String fixedValuesText) throws RateException {
		
		if (StringUtils.isNotBlank(fixedValuesText)) {
			
			// ����l�͉�ʏォ��w��ł���igen=4;sptate=0�̌`�Łj
			Map<String, Double> fixedValues = new HashMap<String, Double>();
			String[] keysets = StringUtils.split(fixedValuesText, SEMICOLON);
			for (String keyset : keysets) {
				String[] keyValue = StringUtils.split(keyset, EQ);
				if (keyValue.length < 2) {
					throw new RateException("���[�g�L�[�̌Œ�l�̎w��ɂ͖�肪����B");
				}
				fixedValues.put(keyValue[0], Double.parseDouble(keyValue[1]));			
			}		
			convertor.setFixedValue(fixedValues);			
		}
	}
	
	/**
	 * �ő�戵�s����ݒ�
	 * @param num
	 */
	@Override
	public void setMaxLineNumber(long num) {
		if (num > 0) {
			this.maxlineNumber = num;
		}		
	}
		
	private char _getDelimiter() {
		if (this.delimiter > 0) {
			return this.delimiter;
		}		
		return DEFAULT_DELIMITER;
	}
	
	/**
	 * �����L����ݒ�
	 * @param delimiter
	 */
	public void setDelimiter(char delimiter) {
		//TODO tab,comma�ȂǗL���`�F�b�N���s���H
		this.delimiter = delimiter;
	}

	@Override
	public Map<String, Double> readRateKeys() throws RateException, IOException {
		
		if (this.lineCount == this.maxlineNumber) return null;
		
		if (this.keyNames == null) {
			this.getKeyNames();
			//throw new RuntimeException("���[�g�L�[�̖������[�h���Ă��烌�[�g�L�[�̒l��ǂݍ��ށB");
		}
				
		Map<String, Double> rateKeyValues = new HashMap<String, Double>();		
						
		//���[�g�L�[�̒l��ҏW
		String strLine = this.reader.readLine();
		if (strLine == null) return null;
		if (strLine.charAt(strLine.length() - 1) == ',') {
			strLine += Const.SPACE;
		}		
		String[] keyValues = strLine.split(String.valueOf(_getDelimiter()));		
		if (keyValues.length != this.keyNames.length) {
			//���[�g�L�[�s���̏ꍇ�A�v�Z���Ȃ��āA�L�[�ƒl�݂̂��o�͂���
			throw new RateException("���[�g�L�[�ƒl�̐��͈Ⴄ�B" + this.keyNames + ":" + strLine);
		}
		
		for (int i = 0; i < keyValues.length; i++) {
			String val = keyValues[i];
			String keyName = this.keyNames[i];
			if (StringUtils.isNotBlank(val)) {
								
				//�������ʋ敪�̏ꍇ�A8A��9A�����16�i�@�̒l�͏\�i�@�ɕϊ�����
				if (keyName.equals(INPUT_RYORITSU_SHIBETSU_CN)) {
					int ret = 0;
					if (val.startsWith("8") || val.startsWith("9")) {
						ret = Integer.parseInt(val, RADIX16);
					} else {
						ret = Integer.parseInt(val);
					}
					rateKeyValues.put(INPUT_RYORITSU_SHIBETSU_CN, ret * 1d);
				} else {
					rateKeyValues.put(keyName, new Double(val));	
				}
					
			} else {
				rateKeyValues.put(keyName, 0d);
			}		
		}			
		this.lineCount ++;
		
		//�ϊ��������[�g�L�[��Ԃ�
		return this.convertor.convert(rateKeyValues);
		
	}	


	@Override
	public void close() {
		try {
			if (this.reader != null) {
				this.reader.close();
			}			
		} catch (IOException e) {
			throw new RuntimeException(e);
		} finally {
			this.reader = null;
			//TODO �N���[�Y����ƁA��肠��
//			if (this.file != null) {
//				this.file = null;
//			}
		}
		
	}

	public String[] getKeyNames() throws RateException {
		
		try {
			//���[�g�L�[�̓ǂݍ���
			this.reader = new BufferedReader(new FileReader(file));
			String strLine = this.reader.readLine();
			
			if (StringUtils.isEmpty(strLine)) {
				throw new RateException("CSV�t�@�C���ɓ����f�[�^�����݂��Ă��Ȃ��B");
			}
			
			//���[�g�L�[�̖���ҏW
			this.keyNames = StringUtils.split(strLine, _getDelimiter());
			
			//���q�l����csv�t�@�C���̃w�b�_���ɂ́A����ꍇ���[�L�[�̖��O�͑S�p�ł���G����ꍇ�A���p�ł���B
			//��r���₷�����߁A���[�g�L�[�̒��g�ɉp��̑S�p���甼�p�֓]��
			for (int i = 0, len = this.keyNames.length; i < len; i++) {
				this.keyNames[i] = CommonUtil.ToDBC(this.keyNames[i]);
			}
			
		} catch (FileNotFoundException e) {
			throw new RateException(e);
		} catch (IOException e) {
			throw new RateException(e);
		}
	
		return this.keyNames;
	}
	
	public File getFile() {
		return this.file;
	}
		
	@Override
	public long getReadLineNum() {
		return this.lineCount;
	}

	public long getTotalLineCount() throws RateException {
		return this.totalLineCount;
	}

	@Override
	public void setCompareObject(String dest) {
		String[] compareObjNames = StringUtils.split(dest, ',');
		this.convertor.setCompareObjectNames(compareObjNames);
	}

	@Override
	public void setLayoutFactory(BatchDataLayoutFactory factory) {
		;
	}
		
}
