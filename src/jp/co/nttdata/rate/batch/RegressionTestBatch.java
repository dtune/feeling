package jp.co.nttdata.rate.batch;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import jp.co.nttdata.rate.exception.FmsDefErrorException;
import jp.co.nttdata.rate.exception.IllegalBatchDataException;
import jp.co.nttdata.rate.exception.RateException;
import jp.co.nttdata.rate.fms.calculate.RateCalculator;
import jp.co.nttdata.rate.fms.common.SystemFunctionUtility;
import jp.co.nttdata.rate.log.LogFactory;
import jp.co.nttdata.rate.model.CalculateCategory;
import jp.co.nttdata.rate.model.CategoryManager;
import jp.co.nttdata.rate.model.rateKey.RateKeyManager;
import jp.co.nttdata.rate.rateFundation.RateFundationManager;
import jp.co.nttdata.rate.util.Const;
import jp.co.nttdata.rate.util.PropertiesUtil;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.time.DateFormatUtils;
import org.apache.log4j.Level;

/**
 * �Z���C���ɂ̓f�B�N���C���N����Ȃ��悤�ɉ�A�e�X�g���s��
 * @author zhanghy
 *
 */
public class RegressionTestBatch {

	/** ���̂̃����N�l */
	private static final int SPECIAL_INDEX_RANK = 100;
	private static final String SPECIAL_FORMULA_PREFIX = "Special_";
	private static final String BONUS_FORMULA_PREFIX = "Bonus_";
	
	/** �ی������z */
	private static final String PRATE_FORMULA = "Premium";
	private static final String SPECIAL_PRATE_FORMULA = "Special_Premium";
	private static final String BONUS_PRATE_FORMULA = "Bonus_Premium";
	private static final String SPECIAL_BONUS_PRATE_FORMULA = "Special_Bonus_Premium";

	/** V���[�g */
	private static final String VRATE_FORMULA = "BT_Vrate";
	/** ���Ԗߋ����[�g */
	private static final String WRATE_FORMULA = "CashValue";
	/** ���� */
	private static final String PAIDUP_FORMULA = "paidup_Premium";//paidup_SurrenderFee
	/** ���� */
	private static final String EXTEND_FORMULA = "extend_live_Premium";
	/** �������z�����c�� */
	private static final String DT_FORMULA = "Dt";
	/** �����N�� */
	private static final String UNPAIDANNUITY = "UnpaidAnnuity";
	
	private static final String ALLOCATION_FORMULA = "Allocation";
	
	/** dat�t�@�C���v�Z���ʂ��i�[����t�@�C�� */
	private static final String DEFAULT_DAT_LIST_PATH = "\\DatList";
	private static final String DEFAULT_OUTPUT_DAT_LIST_PATH = "\\DatListtmp";
	
	/** fileList */
	private static StringBuilder strFileList = new StringBuilder();
	
	/** ���i�R�[�h�̈ʒu������ �iP,V,W�̏��ԂŁj*/
	private static int[] POS_INSURANCE_CODE = new int[] { 21, 145, 26, 274, 116, 152 };
	private static int INSURANCE_CODE_LENGTH = 3;

	private static int[] POS_SPECIAL_INDEX = new int[] { 46, 208, 198, 274, 116, 152 };
	private static int SPECIAL_INDEX_LENGTH = 3;

	private static int[] POS_BONUS_SIGN = new int[] { 82, 54, 93, 60, 60, 152};
	private static int BONUS_SIGN_LENGTH = 1;

	/** PVW�f�[�^�̌���(���s�����܂߂ĂȂ�) */
	public static int[] DATA_LENGTH = new int[] { 356, 563, 350, 492, 284 ,1000};
	
	/** �v�Z���W���[�� */
	private RateCalculator rc;
	/** �f�[�^�ǂݍ��ނ����W���[�� */
	private IRateKeyReader reader;
	/** �v�Z���ʂ������o�����W���[�� */
	private CsvFileWriterImpl writer;
	private static final boolean NG_ONLY = true;
	private static final String COMPARE_MAPPING_PROPERTIES = "batch/compareMapping.properties";
	
	/**�o�b�`�����̃��[�N�f���N�g���B*/
	private String workDir;
	/** �J�����g�����ΏۂƂȂ�t�@�C�� */
	private File curWorkFile;
	
	/** ���[�N�f���N�g���B����dat�t�@�C���̌v�Z�����i�[���� */
	private File datListFile;
	private File datListFiletmp;
	private BufferedReader datListReader;
	private BufferedWriter datListWriter;
	
	/** ���i�R�[�h */
	private String curInsuranceCode;
	
	/** dat�t�@�C���ɉ�����v�Z���̉p�ꖼ */
	private String curFormulaName;
	
	/**�t�@�C���t�B���^�[*/
	private BatchInputFileFilter datFilter;
	private BatchInputFileFilter defaultFilter;
	
	/** ���̃t���O */
	private boolean isSpecial = false;
	private boolean isBonus = false;
		
	/** ��r�Ώۃ}�b�s���O */
	private Map<String, String> compareMapping;
	private Properties compareMappingProp;
	
	/** �v�Z���� */
	private Double ret;
	
	/**
	 * ��A�e�X�g�̂��߁A���ꂼ�ꃂ�W���[��������������
	 */
	public RegressionTestBatch(String workDir) {
		
		//���O�I�t�ɂ���
		LogFactory.setLoggerLevel(Level.OFF);
		
		this.workDir = workDir;	
		this.curWorkFile = new File(this.workDir);
		if (!this.curWorkFile.exists()) {
			throw new IllegalArgumentException("�w��p�X�̓t�@�C�������̓t�H���_�ł͂���܂���F" + workDir);
		}
		
		this.compareMappingProp = PropertiesUtil.getExternalProperties(COMPARE_MAPPING_PROPERTIES);
		
		this.datFilter = new BatchInputFileFilter(new String[]{"dat","csv"});
		this.defaultFilter = new BatchInputFileFilter();
		
		this.datListFile = new File(this.workDir + "\\" + DEFAULT_DAT_LIST_PATH +
				DateFormatUtils.format(new Date(System.currentTimeMillis()), "yyyyMMdd") + ".rlt");
		this.datListFiletmp = new File(this.workDir + "\\" + DEFAULT_OUTPUT_DAT_LIST_PATH +
				DateFormatUtils.format(new Date(System.currentTimeMillis()), "yyyyMMdd") + ".rlt");

		try {
			if(!datListFile.exists() || datListFile.length() == 0){
				_getAllDatFilePath();
			} else {
				datListFile.delete();
				datListFile.createNewFile();
				_getAllDatFilePath();
			}
			
		this.datListReader = new BufferedReader(new FileReader(this.datListFile));
		this.datListWriter = new BufferedWriter(new FileWriter(this.datListFiletmp));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void _getAllDatFilePath() throws IOException {

		this.datListWriter = new BufferedWriter(new FileWriter(datListFile));

		StringBuilder sbTitle = new StringBuilder();
		sbTitle.append("���i�R�[�h");
		sbTitle.append(",").append("�f�[�^�t�@�C��");
		sbTitle.append(",").append("NG����(SKIP:�J�����g�f�[�^�v�Z���Ȃ��GREDO:�J�����g�f�[�^�Čv�Z)");
		sbTitle.append(",").append("������");
		sbTitle.append(",").append("���������(�b)");
		sbTitle.append(",").append("���\(�b/�猏)");
		this.datListWriter.write(sbTitle.toString());
		this.datListWriter.newLine();
		this.datListWriter.flush();
		
		getFileList(curWorkFile);
		this.datListWriter.write(strFileList.toString());		
		this.datListWriter.flush();
		this.datListWriter.close();
	}
	
	private void getFileList(File file) {
		String filePath;
		if(file.getName().indexOf("���ؑΏۊO") > -1)
			return;
		if(file.isDirectory()) {
			File fileList[] = file.listFiles(datFilter);
			for(int i = 0; i < fileList.length; i++){
				getFileList(fileList[i]);
			} 
		} else {
			if(file.length() == 0)
				return;	
			filePath = file.getAbsolutePath();
			if(datFilter.accept(this.curWorkFile)) {
				strFileList.append(filePath);
				strFileList.append("\n");
			}
		}
	}
	
	/**
	 * �J�����gdat�t�@�C����Ԃ�
	 * @return
	 */
	private File _nextFile() {
		return this.curWorkFile;
	}
	
	/**
	 * �w�肳��Ă����t�H���_�̉��Ɏ���dat�t�@�C�������邩
	 * @return
	 */
	private boolean _haveNextFile() throws IOException, RateException {

		String data;
		while ((data = datListReader.readLine()) != null) {

			if (StringUtils.isBlank(data)) {
				return false;
			}
			
			/*
			 *  ����v�Z�̏ꍇ�A�t�@�C�����i�p�X�j���������Ă���
			 *  �v�Z�ς݂̏ꍇ�A�t�@�C�����̌��ɁA�R���}��؂�Łu�S�����v�A�uNG�����v�A�u�|���ԁv��ǉ�
			 *  ���A�v�Z�ς݂̃t�@�C���ɑ΂��āA�f�t�H���g�Ƃ��ē�x�ƌv�Z���Ȃ��悤�ɂƂ���
			 */
			
			String[] dataSplit = data.split(Const.COMMA);
			if (dataSplit.length == 1) {
				this.curWorkFile = new File(data);
				return true;
			} else {
				if(dataSplit.length == 2 || "REDO".equals(dataSplit[2])){
					this.curWorkFile = new File(dataSplit[1]);
					return true;
				}

				datListWriter.write(data);
				datListWriter.newLine();
				datListWriter.flush();
			}
		}
		datListWriter.close();
		return false;
	}

	/**
	 * �J�����g�����ΏۂƂȂ�dat�t�@�C���̑�P�s(CSV�p)
	 * ��ǂ݂��珤�i�R�[�h�ƌv�Z�J�e�S����ҏW���������Ōv�Z���W���[����������
	 * @throws IOException 
	 * @throws RateException 
	 * @throws FmsDefErrorException 
	 */
	private boolean _initRateCalculator4CSV(int kubun) throws IOException, RateException, FmsDefErrorException {

		BufferedReader br = new BufferedReader(new FileReader(this.curWorkFile));
		String head = br.readLine();

		if (StringUtils.isBlank(head)) {
			return false;
		}
		String data = br.readLine();

		String insuranceCode = null;
		String calculateCate = CalculateCategory.DIVIDEND;

		String[] heads = head.split(",");
		String[] datas = data.split(",");
		
		for (int i = 0; i < heads.length; i++){
			if("�����ێ�CN".equals(heads[i])){
				insuranceCode = datas[i];
				break;
			}
		}
		if(insuranceCode == null){
			throw new IllegalBatchDataException("�戵�f�[�^�ΏۊO�̂��߁A �f�[�^���C�A�E�g���m�F���������F" + this.curWorkFile.getAbsolutePath());
		}
		
		insuranceCode = _editInsuranceMapping(insuranceCode);
		insuranceCode = insuranceCode + "7";
		
		// �Z������ҏW
		switch(kubun){
		case 1:
			this.curFormulaName = DT_FORMULA;
			break;
		case 2:
			this.curFormulaName = ALLOCATION_FORMULA;
			break;
		default:
			throw new IllegalBatchDataException("�z���v�Z�ΏۊO�̂��߁A �f�[�^���C�A�E�g���m�F���������F" + this.curWorkFile.getAbsolutePath());
		}
		// ��r�Ώۂ�ҏW
		String compOriginal = _editCompareMapping(this.curFormulaName);
		if (!insuranceCode.equals(this.curInsuranceCode)) {			
			this.curInsuranceCode = insuranceCode;
			this.rc = new RateCalculator(insuranceCode, true);
			this.rc.setCalculateCate(new String[]{CalculateCategory.P, CalculateCategory.V, CalculateCategory.W, CalculateCategory.DIVIDEND});

			//�v�Z�ɕK�v�ł��郌�[�g�L�[���J�e�S���}�l�W���[��������
			RateKeyManager.newInstance(insuranceCode);
			CategoryManager.newInstance(insuranceCode);						
		}
		this.reader.setCompareObject(compOriginal);
		// NG�̂ݏo�͎w���writer��������
		this.writer = new CsvFileWriterImpl(insuranceCode, this.reader.getFile(), compareMapping, NG_ONLY, calculateCate, compOriginal);
		return true;
	}
	
	/**
	 * �J�����g�����ΏۂƂȂ�dat�t�@�C���̑�P�s
	 * ��ǂ݂��珤�i�R�[�h�ƌv�Z�J�e�S����ҏW���������Ōv�Z���W���[����������
	 * @throws IOException 
	 * @throws RateException 
	 * @throws FmsDefErrorException 
	 */
	private boolean _initRateCalculator() throws IOException, RateException, FmsDefErrorException {

		BufferedReader br = new BufferedReader(new FileReader(this.curWorkFile));
		String data = br.readLine(); 
		
		if (StringUtils.isBlank(data)) {
			return false;
		}
		
		String insuranceCode = null;
		String calculateCate = null;
		String formulaName = null;
		
		// �f�[�^�̒������v�Z��ނ𔻒肷�邤���ŏ��i�R�[�h��ҏW
		int index = -1;
		int len = data.length();		
		switch (len) {
		case 305:
		case 356:
			calculateCate = CalculateCategory.P;
			formulaName = PRATE_FORMULA;
			index = 0;
			break;
		case 563:
			calculateCate = CalculateCategory.V;
			formulaName = VRATE_FORMULA;
			index = 1;
			this.isSpecial = true;
			break;
		case 350:
			calculateCate = CalculateCategory.W;
			formulaName = WRATE_FORMULA;
			index = 2;
			break;
		case 492:
			calculateCate = CalculateCategory.H;
			formulaName = PAIDUP_FORMULA;
			index = 3;
			break;
		case 284:
			calculateCate = CalculateCategory.E;
			formulaName = EXTEND_FORMULA;
			index = 4;
			break;
		case 1000:
			calculateCate = CalculateCategory.A;
			formulaName = UNPAIDANNUITY;
			index = 5;
			break;
		default:
			throw new IllegalBatchDataException("�戵�f�[�^�ΏۊO�̂��߁A �f�[�^���C�A�E�g���m�F���������F" + this.curWorkFile.getAbsolutePath());
		}

		String origCode = data.substring(POS_INSURANCE_CODE[index], POS_INSURANCE_CODE[index] + INSURANCE_CODE_LENGTH);
		// �����N���̏ꍇ�A ��_��F152+3,����F264+3
		if ("   ".equals(origCode)) {
			origCode = data.substring(264, 267);
		}
		insuranceCode = _editInsuranceMapping(origCode);
			
		// �Z������ҏW
		this.curFormulaName = _editFormulaName(formulaName, data, index);
				
		// ��r�Ώۂ�ҏW
		String compOriginal = _editCompareMapping(this.curFormulaName);
		
		// ���̂̏ꍇ�A���i�R�[�h�̖����ɓ��̃t���O��ǋL
		if (this.isSpecial) {
			insuranceCode += "1";
		}
		if (this.isBonus) {
			insuranceCode += "2";
		}
		if (index == 3) {
			insuranceCode += "3";
		}
		if (index == 4) {
			insuranceCode += "4";
		}
		// ���i�R�[�h�ƌv�Z��ނ��v�Z���W���[����������
		if (!insuranceCode.equals(this.curInsuranceCode)) {			
			this.curInsuranceCode = insuranceCode;
			this.rc = new RateCalculator(insuranceCode, true);
			this.rc.setCalculateCate(new String[]{CalculateCategory.P, CalculateCategory.V, CalculateCategory.W, CalculateCategory.H, CalculateCategory.E, CalculateCategory.A});

			//�v�Z�ɕK�v�ł��郌�[�g�L�[���J�e�S���}�l�W���[��������
			RateKeyManager.newInstance(insuranceCode);
			CategoryManager.newInstance(insuranceCode);						
		}
		
		//�@�f�[�^�]���p�̃��C�A�E�g�����[�h
		if(this.reader instanceof RegressionDatRateKeyReaderImpl){
			((RegressionDatRateKeyReaderImpl) reader).loadDataLayout(insuranceCode, calculateCate);
		}
		// ��r�Ώۂ�ݒ�
		this.reader.setCompareObject(compOriginal);
		if(calculateCate == CalculateCategory.V){
			this.reader.setFixedValues("f=0;f1=0;fEX=0;accidentUmu=0;j=0");
		}
		if(calculateCate == CalculateCategory.W){
			this.reader.setFixedValues("PremiumAbolishSign=0;SA=10000;accidentUmu=0;j=0;fEX=0");
		}
		if(calculateCate == CalculateCategory.H){
			if(insuranceCode.startsWith("011")){
				this.reader.setFixedValues("gen=4");
			}
		}
		if(calculateCate == CalculateCategory.E){
			if(insuranceCode.startsWith("011")){
				this.reader.setFixedValues("gen=4");
			}
		}
		// NG�̂ݏo�͎w���writer��������
		this.writer = new CsvFileWriterImpl(insuranceCode, this.reader.getFile(), compareMapping, NG_ONLY, calculateCate, compOriginal);
		
		return true;
	}
	
	
	/**
	 * ���ꂼ�ꃌ�[�g�L�[���s����Z������ҏW
	 * <br>��ɕۏ�ɂ��ĎZ���l�[�~���O���[���ɏ]���Ă��ꂼ���prefix��擪�ɒǉ�
	 * @param formulaName
	 * @param data
	 * @param index
	 * @return
	 */
	private String _editFormulaName(String formulaName, String data, int index) {
		String special = data.substring(POS_SPECIAL_INDEX[index], POS_SPECIAL_INDEX[index] + SPECIAL_INDEX_LENGTH);
		String bonus = data.substring(POS_BONUS_SIGN[index], POS_BONUS_SIGN[index] + BONUS_SIGN_LENGTH);
		// �����N���̏ꍇ�AspecialIndex��bonusSign��NULL�̈�.
		if("   ".equals(special)) {
			special = "100";
		}
		if(" ".equals(bonus)) {
			bonus = "0";
		}
		int specialIndex = Integer.parseInt(special);
		int bonusSign = Integer.parseInt(bonus);
		String newFormulaName;
		if (formulaName == null) {
			newFormulaName = "";
		} else {
			newFormulaName = new String(formulaName);
		}		
		if(index == 0) {
			
			if(bonusSign == 1 && specialIndex > SPECIAL_INDEX_RANK) {
				this.isBonus = true;
				this.isSpecial = true;
				newFormulaName = SPECIAL_BONUS_PRATE_FORMULA;
			}else if(bonusSign == 1){
				this.isBonus = true;
				newFormulaName = BONUS_PRATE_FORMULA;
			}else if(specialIndex > SPECIAL_INDEX_RANK){
				this.isSpecial = true;
				newFormulaName = SPECIAL_PRATE_FORMULA;
			}else{
				newFormulaName = PRATE_FORMULA;
//				newFormulaName = "adjustPrate";
			}
		} else if(index == 2) {
			// �{�[�i�X���T�C��:�{�[�i�X���ł��邱�Ƃ������T�C���i�P�F�{�[�i�X���j
			if (bonusSign == 1) {
				this.isBonus = true;
				newFormulaName = "Bonus_SurrenderFee";
			}
			// ���̎��S�w��:���̌_��̕ی����������i100�F�W���́A100���傫���F���́j
			if (specialIndex > SPECIAL_INDEX_RANK) {
				this.isSpecial = true;
				newFormulaName = SPECIAL_FORMULA_PREFIX + newFormulaName;
			}
			
			//TODO �����ی�
			
			//TODO �����ς�
		}
		return newFormulaName;
	}
	
	private String _editCompareMapping(String formulaName) {
		
		this.compareMapping = new HashMap<String, String>();
		String compOriginal = this.compareMappingProp.getProperty("formula." + formulaName);
		if (StringUtils.isBlank(compOriginal)) {
			throw new IllegalBatchDataException("��r�}�b�s���O�t�@�C����"+"formulaName"+"���`����Ă��܂���F" + COMPARE_MAPPING_PROPERTIES);
		}
		
		this.compareMapping.put(compOriginal, formulaName);
		
		return compOriginal;
	}

	/**
	 * ���i033�ɂ��ẮA���[�g�v�Z��`��031���i�R�[�h�Ƃ���
	 * @param code
	 * @return
	 */
    private String _editInsuranceMapping(String code) {

        String insuranceCode = null;
        switch (Integer.parseInt(code)) {
        case 5:
            insuranceCode = "004";
            break;
        case 32:
        case 33:
        case 34:
            insuranceCode = "031";
            break;
        case 932:
        case 933:
        case 934:
            insuranceCode = "931";
            break;
        case 236:
        case 237:
        case 238:
            insuranceCode = "235";
            break;
        case 262:
        case 263:
        case 264:
            insuranceCode = "261";
            break;
        case 266:
        case 267:
        case 268:
            insuranceCode = "265";
            break;
        case 278:
            insuranceCode = "276";
            break;
        case 222:
        	insuranceCode = "221";
        	break;
        case 224:
        	insuranceCode = "223";
        	break;
        case 36:
        	insuranceCode = "035";
        	break;
        default:
            insuranceCode = code;
        }

        return insuranceCode;
    }


	/**
	 * �w�肳�ꂽ�t�H���_�̉��̂��ׂĂ�dat�t�@�C������͂Ƃ��āA�P���ǂ݂Ȃ���
	 * �v�Z���s���āANG���ʂ�CSV�t�@�C���ɏ����o��
	 * @throws IOException 
	 * @throws RateException 
	 */
	@SuppressWarnings("unchecked")
	private void calculate(int dividendKuben) {

		try{
			
			this.curWorkFile = _nextFile();
			
			System.out.println(this.curWorkFile.getAbsolutePath());
			
			// reader������������
			if(defaultFilter.accept(this.curWorkFile)){
				this.reader = new RegressionDatRateKeyReaderImpl(this.curWorkFile);
			}else{
				this.reader = new CsvRateKeyReaderImpl(this.curWorkFile);
			}
			boolean continueKuben = false;
			if(defaultFilter.accept(this.curWorkFile)){
				if(dividendKuben == 0){
					if(_initRateCalculator()){
						continueKuben = true;
					}
				}
			} else {
				if(dividendKuben != 0){
					if(_initRateCalculator4CSV(dividendKuben)){
						continueKuben = true;
					}
				}
			}
			
			if (continueKuben) {

				RegressionDataFilter dataDilter = new RegressionDataFilter(RateFundationManager.getInstance());
				/*
				 * �L����dat�t�@�C���̏ꍇ�A�v�Z���W���[������������������
				 * �o�b�`�������s��
				 */
				long a = System.currentTimeMillis();
				long taskNo = 0l;
				Map rateKeys = null;
				
				while ((rateKeys = reader.readRateKeys()) != null) {
					taskNo++;
					if (dividendKuben == 0 && dataDilter.filter(rateKeys)) {
						continue;
					}
					this.rc.setRateKeys(rateKeys);
					ret = this.rc.calculate(curFormulaName);
					_editCompareRateKeys(rateKeys);
					Map<String, Double> result = new HashMap<String, Double>();
					result.put(curFormulaName, ret);
					this.writer.setInput(rateKeys);
					this.writer.output(result, taskNo);
				}
				
				this.writer.close();
				
				/*
				 * TODO �t�@�C���P�ʂŌv�Z���I�������AdatList�t�@�C���Ɍv�Z���ʂ�Z�߂Ēǉ�����
				 * �t�H�[�}�b�g�F�udat�t�@�C���p�X�v,�u�S�����v�A�uNG�����v�A�u�|���ԁv
				 */
				long b = System.currentTimeMillis() - a;
				StringBuilder sb = new StringBuilder();
				sb.append("'").append(this.curInsuranceCode.substring(0, 3));
				sb.append(",").append(this.curWorkFile.getAbsolutePath());
				sb.append(",").append(this.writer.getNgCount());
				sb.append(",").append(this.writer.getLineCount());
				sb.append(",").append(SystemFunctionUtility.roundUp((double)b/1000,1));
				sb.append(",").append(SystemFunctionUtility.roundDown((double)b/this.writer.getLineCount(),3));
				datListWriter.write(sb.toString());
				datListWriter.newLine();
				datListWriter.flush();
			}
		}catch(Throwable e){
			StringBuilder sb = new StringBuilder();
			sb.append("'").append(this.curInsuranceCode.substring(0, 3));
			sb.append(",").append(this.curWorkFile.getAbsolutePath());
			sb.append(",").append("SKIP");
			sb.append(",").append(e.getMessage());
			try {
				datListWriter.write(sb.toString());
				datListWriter.newLine();
				datListWriter.flush();
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}
	}
	
	public void run(int method) {
		try {
			while(_haveNextFile()){
				calculate(method);
			}
		} catch (Throwable e) {
			// TODO �ُ���̓��O�ɁiStackTrace���e�j�o�͂��悤�ɏC���@���@�G���[���b�Z�[�W(e.getMessage)��datList�ɏo��
			e.printStackTrace();
			String data;
			try {
				StringBuilder sb = new StringBuilder();
				sb.append("'").append(curInsuranceCode.substring(0, 3));
				sb.append(",").append(curWorkFile.getAbsolutePath());
				sb.append(",").append("SKIP");
				sb.append(",").append(e.getMessage());
				datListWriter.write(sb.toString());

				datListWriter.newLine();
				datListWriter.flush();

				while ((data = datListReader.readLine()) != null) {

					datListWriter.write(data);
					datListWriter.newLine();
					datListWriter.flush();
				}
				datListWriter.close();
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		} finally {
			try {
				datListReader.close();
				datListWriter.close();
				datListFile.delete();
				datListFiletmp.renameTo(datListFile);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	private void _editCompareRateKeys(Map<String, Double> rateKeys) {
		if(rateKeys.containsKey("CompareValue")) {
			Double compareValue = rateKeys.get("CompareValue");
			if(compareValue != 0d && ret != 0d && (ret / compareValue * 10000 % 10 == 0
			        || compareValue / ret * 10000 % 10 == 0)) {
				rateKeys.put("CompareValue", ret);	
			} else {
				rateKeys.put("CompareValue", compareValue);
			}
		}
		if(rateKeys.containsKey("specialPremium")
				&& rateKeys.containsKey("PrateUnit")){
			rateKeys.put("specialPremium",
					rateKeys.get("specialPremium") * rateKeys.get("SA")
							/ rateKeys.get("PrateUnit"));
		}
		if(rateKeys.containsKey("bonusPremium")
				&& rateKeys.containsKey("PrateUnit")){
			rateKeys.put("bonusPremium",
					rateKeys.get("bonusPremium") * rateKeys.get("SA")
							/ rateKeys.get("PrateUnit"));
		}
		if(rateKeys.containsKey("specialbonusPremium")
				&& rateKeys.containsKey("PrateUnit")){
			rateKeys.put("specialbonusPremium",
					rateKeys.get("specialbonusPremium") * rateKeys.get("SA")
							/ rateKeys.get("PrateUnit"));
		}
	}
	
	public static void main(String[] args) {
		
		if (args.length < 2) {
			throw new IllegalArgumentException("RegressionTestBatch�ɂ̓f�[�^�t�H���_�̃p�X�ƌv�Z�ޕʂ��w�肭�������B");
		}

		String filePath = args[0];

		int method = Integer.valueOf(args[1]);
		if (method > 2 || method < 0) {
			throw new IllegalArgumentException("�v�Z��ʂ́u0:PVW 1:�������z�����c�� 2:���z�z�v�̂�������w�肭�������B");
		}
		RegressionTestBatch rtb = new RegressionTestBatch(filePath);
		rtb.run(method);
	}
}