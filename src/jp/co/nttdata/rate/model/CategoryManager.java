package jp.co.nttdata.rate.model;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.configuration.XMLConfiguration;

import jp.co.nttdata.rate.util.CommonUtil;
import jp.co.nttdata.rate.util.Const;
import jp.co.nttdata.rate.util.ResourceLoader;

/**
 * �v�Z�J�e�S���Ɋւ��ď����}�l�[�W�����g
 * @author btchoukug
 *
 */
public class CategoryManager {
	
	private static final String CALCULATION_CATE_ID = "CalculateCategory";

	private static final String CATE_PREFIX = "Categories.Category";

	private static final String CATE_NODE_NAME = CATE_PREFIX + Const.DOT + "name";
	private static final String CATE_NODE_LABEL = CATE_PREFIX + Const.DOT + "label";
	private static final String NODE_RATEKEYS = "RateKeys";
	
	private static CategoryManager INSTANCE;
	
	/** ���i��bPVW�̃J�e�S���������� */
	private XMLConfiguration config;
		
	/** ���i�R�[�h */
	private String insuranceCode;

	/** �ۏ�I������ꍇ�A�����t�����t���O��ێ� */
	private String securityFlg;
	
	private XMLConfiguration[] securityConfigs;
	
	private String[] cateNames;
	private String[] cateLabels;
	
	/**�v�Z�J�e�S�����i�[���郊�X�g*/
	private List<CalculateCategory> categories;
	
	public CategoryManager(String code) {
		categories = new ArrayList<CalculateCategory>();
		securityConfigs = new XMLConfiguration[Const.SECURITY_CATE_PREFIXS.length];
		_loadCateInfos(code);
	}
	
	public static synchronized void newInstance(String insuranceCode) {
		synchronized (CategoryManager.class) {
			INSTANCE = new CategoryManager(insuranceCode);				
		}
	}
	
	public static synchronized CategoryManager getInstance() {
		if (INSTANCE == null) {
			throw new IllegalArgumentException("CategoryManager�͏��������Ă��܂���B");
		}
		return INSTANCE;
	}
	
	/**
	 * ���i�R�[�h���v�Z�P�ʏ������[�h����
	 */
	private void _loadCateInfos(String code) {
		
		this.insuranceCode = code.substring(0, 3);
		this.securityFlg = code.substring(3);
		
		//���i�P�ʂ�PVW�̃J�e�S������ǂݍ���
		URL dtdUrl = ResourceLoader.getExternalResource(Const.CALCULATION_DEF_DTD);
		URL xmlUrl = ResourceLoader.getExternalResource(Const.CALCULATION_DEF_DIR + this.insuranceCode + Const.XML_SUFFIX);		
		this.config = CommonUtil.loadXML(xmlUrl, CALCULATION_CATE_ID, dtdUrl, false);
		
		//�ۏᕔ���̃J�e�S��prefix��ҏW���������ŕۏᕔ���Z�������[�h
		for (int i = 0; i < Const.SECURITY_CATE_PREFIXS.length; i++) {
			if (this.securityFlg.indexOf(String.valueOf(i+1)) > -1) {
				xmlUrl = ResourceLoader.getExternalResource(Const.CALCULATION_DEF_DIR + Const.SECURITY_CATE_PREFIXS[i] + Const.XML_SUFFIX);
				this.securityConfigs[i] = CommonUtil.loadXML(xmlUrl, CALCULATION_CATE_ID, dtdUrl, false);					
			}
		}
		
		//�^�u�ɏ������郌�[�g�L�[��UI�����擾
		this.cateNames = _getCateNamesFromConfig();
		this.cateLabels = _getCateLabelsFromConfig();
		
		for (int i = 0; i < cateNames.length; i++) {	
			CalculateCategory cate = new CalculateCategory(cateNames[i], cateLabels[i], _getCategoryRateKeys(i));
			this.categories.add(cate);
		}
	}
	
	/**
	 * �^�u�ɏ������[�g�L�[�����擾
	 * @param index
	 * @return
	 */
	private String[] _getCategoryRateKeys(int index) {
		
		String idx = "(" + index + ").";
		String keySearch = CATE_PREFIX + idx + NODE_RATEKEYS;
		
		// PVW�̃��[�g�L�[�����擾
		String[] keys = this.config.getStringArray(keySearch);
		
		//�@�ۏᕔ���̃��[�g�L�[�����擾
		for (int i = 0; i < Const.SECURITY_CATE_PREFIXS.length; i++) {
			if (this.securityFlg.indexOf(String.valueOf(i+1)) > -1) {
				String[] securityKeys = this.securityConfigs[i].getStringArray(keySearch);
				String[] key = new String[keys.length + securityKeys.length];
				System.arraycopy(keys, 0, key, 0, keys.length);
				System.arraycopy(securityKeys, 0, key, keys.length, securityKeys.length);
				keys = key;			
			}
		}
		
		return keys;
	}

	/**
	 * �v�Z�J�e�S���̏����擾�iUI��`XML�j
	 * @return
	 */
	public List<CalculateCategory> getCateInfos() {
		return this.categories;
	}

	public CalculateCategory getCateInfo(String cateName) {
		
		for (int i = 0; i < this.cateNames.length; i++) {
			if (this.cateNames[i].equals(cateName)) {
				return this.categories.get(i);
			}
		}
		
		throw new IllegalArgumentException("�J�e�S�����F�@" + cateName + "���Ԉ���Ă���");		
	}

	public String[] getCateNames() {
		return this.cateNames;
	}
	
	public String[] getCateLabels() {
		return this.getCateLabels();
	}

	/**
	 * ���ׂẴ^�u�����擾
	 * @return
	 */
	private String[] _getCateNamesFromConfig() {
		return this.config.getStringArray(CATE_NODE_NAME);
	}
	
	/**
	 * ���ׂẴ^�u�\���̃��x�����擾
	 * @return
	 */
	private	String[] _getCateLabelsFromConfig() {
		return this.config.getStringArray(CATE_NODE_LABEL);
	}
}
