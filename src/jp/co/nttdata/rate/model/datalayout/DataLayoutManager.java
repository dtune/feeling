package jp.co.nttdata.rate.model.datalayout;

import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.configuration.XMLConfiguration;
import org.apache.commons.configuration.beanutils.BeanFactory;
import org.apache.commons.configuration.beanutils.BeanHelper;
import org.apache.log4j.Logger;
import jp.co.nttdata.rate.log.LogFactory;
import jp.co.nttdata.rate.model.rateKey.rule.RateKeyConstraint;
import jp.co.nttdata.rate.model.rateKey.rule.RateKeyRule;
import jp.co.nttdata.rate.util.CommonUtil;
import jp.co.nttdata.rate.util.Const;
import jp.co.nttdata.rate.util.ResourceLoader;

/**
 * 
 * ���[�g�L�[�Ɋւ���UI��񂨂�уL�[�̊Ԃ̐���֌W���}�l�[�W���邱��
 * @author btchoukug *
 */
public class DataLayoutManager {

	private static Logger logger = LogFactory.getInstance(DataLayoutManager.class);

	/** �B��̃C���X�^���X */
	private static DataLayoutManager INSTANCE;
		
	/** ���[�g�L�[��BeanFactory�� */
	public static final String DATALAYOUT_BEANFACTORY = "DataLayoutBeanFactory";

	private static final String NODE_KEYS = "Category";	
	
	private XMLConfiguration config;	
	
	/**�V�X�e����̂��ׂĂ�Category�̃��X�g*/
	private List<InputDataCategory> categoryList = new ArrayList<InputDataCategory>();
	
	
	/**�V�X�e����̂��ׂẴ��[�g�L�[�̐���֌W*/
	private List<RateKeyConstraint> constraintList = new ArrayList<RateKeyConstraint>();
	
	/**���i���ɐݒ肳�ꂽ���[�g�L�[���[���ł���<br>
	 * �iString:���[�g�L�[���GRateKeyRule:���[�g�L�[�̃��[���j
	 * */
	private Map<String, RateKeyRule> namedRules = new HashMap<String, RateKeyRule>();
			
	/** ���[�g�L�[��`����у��[���A����֌W���Ǘ�����  */
	public DataLayoutManager(String insuranceCode) {
		
		//TODO factory���[�h�ŃO���[�o���Ə��i���̌�����`�A���[�g�L�[��`XML�y��UI��`�A�v�Z��͂�ǂ�ŃR���o�C���ɂ���
		
		/**
		 * ���i���Ƀ��[�g�L�[�̃��[���Ɛ���֌W�͈Ⴄ���߁A��`�͏��i���ɍs���K�v
		 */
		URL xmlUrl = ResourceLoader.getExternalResource(Const.DATALAYOUT_DEF);
		URL dtdUrl = ResourceLoader.getExternalResource(Const.DATALAYOUT_DEF_DTD);
		this.config = CommonUtil.loadXML(xmlUrl, "Category", dtdUrl, false);
				
		//���[�g�L�[�̒�`����ǂݍ���
		_editRateKeyDefs(insuranceCode);


	}
	
	public static synchronized void newInstance(String insuranceCode) {
		synchronized (DataLayoutManager.class) {
				INSTANCE = new DataLayoutManager(insuranceCode);
		}
	}
	
	private static DataLayoutManager getInstance() {
		if (INSTANCE == null) {
			throw new IllegalArgumentException("RateKeyManager�͏��������Ă��܂���B");
		}
		return INSTANCE;
	}
	
	/**
	 * ���ׂă��[�g�L�[�̒�`���擾���ă��X�g�Ƃ��ĕԂ�
	 */
	private void _editRateKeyDefs(String code) {
		
		int max = this.config.getMaxIndex(NODE_KEYS);
		
		//���[�g�L�[���ꂸ�ǂݍ���
		for (int i = 0; i <= max; i++) {
			String key = "Category("+ i +")";
			DataLayoutDeclaration decl = new DataLayoutDeclaration(config, key);
			BeanFactory factory = new DataLayoutBeanFactory();
			BeanHelper.registerBeanFactory(DATALAYOUT_BEANFACTORY, factory);
			
			InputDataCategory dl = (InputDataCategory) BeanHelper.createBean(decl);
			categoryList.add(dl);
		}
		
	}
	
//	/**
//	 * �L�[�̐���֌W��ǂݍ���
//	 */
//	private void _editRateKeyConstraints() {
//
//		String[] conditions = this.config.getStringArray("Constraints.Constraint[@condition]");
//				
//		for (int i = 0; i < conditions.length; i++) {
//			String[] keys = this.config.getStringArray("Constraints.Constraint(" + i + ")[@keys]");
//			String desc = this.config.getString("Constraints.Constraint(" + i + ")[@desc]");
//			
//			
//			//����Ƃ��AXML��pEntity�L���i��F>��&gt;�j���g���܂����B
//			RateKeyConstraint rkc = new RateKeyConstraint(keys, desc, conditions[i]);
//			//����֌W�̏����̂Ȃ���OP�����������i��Fle��>=�Ɂj
//			//RateKeyConstraint rkc = new RateKeyConstraint(keys, convertAbbreviation2OP(conditions[i]));
//			constraintList.add(rkc);
//		}
//		
//	}
	
//	/**
//	 * XML����擾�������[�g�L�[�̃��[�����u���[�g�L�[���F���[���v�Ƃ����`�ŕۑ�����
//	 */
//	private void _editNamedRules() {
//		for (DataLayout dl : this.dataLayoutList) {
//			this.namedRules.put(dl.getName(), dl.getRule());
//		}
//	}
	
	/**
	 * ���ׂă��[�g�L�[�̒�`�����X�g�Ƃ��ĕԂ�
	 * @return
	 */
	public static List<InputDataCategory> getAllDataLayoutDefs() {
		return getInstance().categoryList;
	}
	
	/**
	 * �w��̃L�[���A�T�u���[�g�L�[�̒�`���X�g��Ԃ�
	 * @param keys
	 * @return
	 */
	public static List<DataLayout> getRateKeyDefs(String cateName) {
		
		for (InputDataCategory category : getInstance().categoryList) {
			if (cateName.equals(category.getName())) {
				return category.getLayoutData();
			}
		}
		
		return null;
	}

	

	/**
	 * ���[�g�L�[���ɉ����ă��[�����擾����
	 * @param keyName
	 * @return
	 */
	public static RateKeyRule getRateKeyRule(String keyName) {
		return getInstance().namedRules.get(keyName);
	}
	
	/**
	 * �w��̃��[�g�L�[���A�L�[�̊Ԃ̐���֌W��Ԃ�
	 * @return
	 */
	public static List<RateKeyConstraint> getKeyConstraints(String[] keys) {
		List<RateKeyConstraint> subKeyConstraints = new ArrayList<RateKeyConstraint>();
		for (RateKeyConstraint cons : getInstance().constraintList) {
			String[] constraintKeys = cons.getConstraintKeys();
			
			//���Y����֌W�Ɋւ���L�[�͑S���w��̃��[�g�L�[�͈̔͂ɂȂ�ƁA�L���Ȑ���֌W�Ƃ���
			if (CommonUtil.containsOnly(constraintKeys, keys)) {
				subKeyConstraints.add(cons);
			}
		}
		return subKeyConstraints;
	}
	
}
