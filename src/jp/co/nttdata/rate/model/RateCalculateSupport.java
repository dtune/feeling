package jp.co.nttdata.rate.model;

import java.io.File;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import org.apache.commons.beanutils.ConvertUtils;
import org.apache.commons.lang.StringUtils;

import jp.co.nttdata.rate.exception.FmsDefErrorException;
import jp.co.nttdata.rate.exception.RateException;
import jp.co.nttdata.rate.fms.calculate.RateCalculator;
import jp.co.nttdata.rate.model.formula.Formula;
import jp.co.nttdata.rate.model.formula.FormulaManager;
import jp.co.nttdata.rate.model.rateKey.Item;
import jp.co.nttdata.rate.model.rateKey.RateKeyManager;
import jp.co.nttdata.rate.model.rateKey.RateKeyValidator;
import jp.co.nttdata.rate.rateFundation.dbConnection.DBConnection;
import jp.co.nttdata.rate.rateFundation.dbConnection.DataRow;
import jp.co.nttdata.rate.ui.view.InsuranceSelectDialog;
import jp.co.nttdata.rate.util.Const;
import jp.co.nttdata.rate.util.ResourceLoader;
/**
 * UI�}�l�[�W�����g�̓��[�g�v�Z�c�[��OL�����̃G���g���[�ł���
 * <br>�^�u��񂨂�я����v�Z�Ώۏ����擾����
 * @author btchoukug
 *
 */
public class RateCalculateSupport {
	
	private String code;
	/**���i�R�[�h*/
	private String insuranceCode;
	/**���i����*/
	private String name;
	
	/**�@������SQL��*/
	private final static String INSURANCE_GEN_SQL = "select generation "
		+ "from rate_master where insurance_code = ${code} "
		+ "group by generation";
	
	/**�v�Z���W���[��*/
	private RateCalculator rc;
	
	/*6��`�t�@�C��*/
	private File formulaDefFile;
	private File rateKeyDefFile;
	private File uiDefFile;
	
	private File fundationDefFile;
	private File benefitDefFile;
	private File lifePersionDefFile;

	private List<Item> generationList;
		
	/** 
	 * ���i�R�[�h���v�Z�P�ʂ̒�`XML��ǂݍ���ŁA
	 * ���[�g�L�[�̓���UI���邢��BT�v�Z�̏��������쐬���� 
	 * @param insuranceCode
	 * @throws FmsDefErrorException 
	 */
	public RateCalculateSupport(String code) throws FmsDefErrorException {
		_init(code);
		this.rc.getContext().setCacheEnabled(true);
	}
	
	public RateCalculateSupport(String code, boolean enableCache) throws FmsDefErrorException {
		_init(code);
		this.rc.getContext().setCacheEnabled(enableCache);
	}
	
	public void setCacheEnable(boolean enableCache) {
		this.rc.getContext().setCacheEnabled(enableCache);
	}
	
	/**
	 * XML�z�u�Ƃ�DB��Ƃ��ς��ꍇ�A�ēx���[�h����
	 * @throws FmsDefErrorException 
	 * @throws FmsDefErrorException 
	 */
	public void reload() throws FmsDefErrorException {
		_init(this.code);
	}
	
	private void _init(String code) throws FmsDefErrorException {
		this.code = code;
		
		this.insuranceCode = code.substring(0, 3);
		this.name = _getDisplayName(code);
		
		//�v�Z���W���[��������
		this.rc = new RateCalculator(code);
		
		RateKeyManager.newInstance(code);
		//�v�Z�J�e�S������ǂݍ��ށi��Ƀ^�u�\���p�j
		CategoryManager.newInstance(code);
		
		//���ׂĂ̌v�Z�͈͂̒��̌v�Z���������[�h�A���ʂ̕������܂߂�
		this.rc.setCalculateCate(CategoryManager.getInstance().getCateNames());
		
		//�f�t�H���g�ꍇ�A�ی������[�g�̌�������уL�[�̐���֌W��ݒ�
		setCalculateCategory(0);		
		
		//��������擾
		this.generationList = _getGenerationList();
		
		//�R��`�t�@�C���̏����擾
		_getDefFile();
		
	}
	
	private void _getDefFile() {

		this.formulaDefFile = ResourceLoader.getExternalFile(Const.FORMULA_DEF_DIR + this.insuranceCode + Const.XML_SUFFIX);
		this.rateKeyDefFile = ResourceLoader.getExternalFile(Const.RATEKEY_DEF_DIR + this.insuranceCode + Const.XML_SUFFIX);
		this.uiDefFile = ResourceLoader.getExternalFile(Const.CALCULATION_DEF_DIR + this.insuranceCode + Const.XML_SUFFIX);
		this.fundationDefFile = ResourceLoader.getExternalFile(Const.FUNDATION_DEF);
		this.benefitDefFile = ResourceLoader.getExternalFile(Const.FORMULA_BENEFIT_DIR);
		this.lifePersionDefFile = ResourceLoader.getExternalFile(Const.FORMULA_LIFEPENSION_DIR);
		
	}

	private String _getDisplayName(String insuranceCode) {
		return InsuranceSelectDialog.insuranceName;
		
	}
	
	public String getWindowTitle() {
		return this.name;
	}

	/**
	 * �^�u�̃C���f�b�N�X���UI�Ɏg���Ă��郌�[�g�L�[�̐���֌W��ݒ�
	 * @param tabIndex
	 */
	public void setCalculateCategory(int tabIndex) {
		String[] keys = CategoryManager.getInstance().getCateInfos().get(tabIndex).getKeys();
		RateKeyValidator.setAllConstraint(RateKeyManager.getKeyConstraints(keys));
	}

	/**
	 * XML�ɒ�`���ꂽ�������擾���Čv�Z����
	 * @param input 
	 * @param input
	 * @return 
	 * @return
	 * @throws Exception 
	 */
	public Map<String, Double> calculate(Map<String, Object> input, Formula f) throws Exception {
		
		//�R���e�L�X�g�̏������i����֌W�����؂���ۂɃR���e�L�X�g�����p�K�v�j		
		this.rc.setRateKeys(input);
				
		//OL�̏ꍇ�A�v�Z�J�e�S���ɉ����ă��[�g�L�[�ɑ΂��āA����֌W������
		RateKeyValidator.validateAllConstraint(this.rc.getContext());	

		//���̓L�[����肪�Ȃ���΁A�����v�Z���s��
		if (f == null || StringUtils.isBlank(f.getName())) {
			throw new RateException("�v�Z�Ώۂɂ͎Z��������܂���F" + f.toString());
		} 
		
		//�v�Z����
		Map<String, Double> result = new HashMap<String, Double>();
		
		//�I�񂾌v�Z�����v�Z���������A���ʂ�Ԃ�
		result.put(f.toString(), this.rc.calculate(f.getName()));
				
		//�r���l����������
		Iterator<Entry<String, Object>> it = this.rc.getContext().getIntermediateValue().entrySet().iterator();
		for (; it.hasNext(); ) {
			Entry<String, Object> entry = it.next();
			result.put(entry.getKey(), ((Double)ConvertUtils.convert(entry.getValue(), Double.class)));
		}
						
		return result;

	}
	
	/**
	 * ���ナ�X�g���擾
	 * @param code
	 * @return
	 * @throws SQLException 
	 */
	private List<Item> _getGenerationList() {
		
		DBConnection.getInstance().open();
				
		List<Item> items = new ArrayList<Item>();		
		
		try {
			List<DataRow> list = DBConnection.getInstance().query(INSURANCE_GEN_SQL, "code", this.insuranceCode);
			// ���Y���i�ɂ��ėL���Ȑ�����ꂸ�擾
			for (DataRow data : list) {
				int gen = data.getInt("generation");
				items.add(new Item(String.valueOf(gen), gen));
			}
		} finally {
			DBConnection.getInstance().close();	
		}		
		
		return items;
	}

	public File getFormulaDefFile() {
		return this.formulaDefFile;
	}

	public File getRatekeyDefFile() {
		return this.rateKeyDefFile;
	}

	public File getUIDefFile() {
		return this.uiDefFile;	
	}
	
	public File getFundationDefFile() {
		return fundationDefFile;
	}

	public File getBenefitDefFile() {
		return benefitDefFile;
	}

	public File getLifePersionDefFile() {
		return lifePersionDefFile;
	}
	
	public void cache2Disk() {
		this.rc.getContext().getCache().cache2Disk();		
	}

	public List<CalculateCategory> getCateInfos() {
		return CategoryManager.getInstance().getCateInfos();
	}

	public List<Item> getGenerationList() {
		return generationList;
	}

	public String getCode() {
		return this.code;
	}

	public FormulaManager getFormulaManager() {
		return rc.getContext().getFormulaManager();
	}

}
