package jp.co.nttdata.rate.model.rateKey;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.apache.commons.configuration.HierarchicalConfiguration;
import org.apache.commons.configuration.SubnodeConfiguration;
import org.apache.commons.configuration.beanutils.BeanDeclaration;
import org.apache.commons.configuration.beanutils.XMLBeanDeclaration;
import org.apache.commons.configuration.tree.ConfigurationNode;


/**
 * ���[�g�L�[��ނɉ������N���X���擾����
 * <br>���ɁA���W�I��R���{�̃A�C�e�����擾����BeanDeclaration���쐬����
 * @author btchoukug
 *
 */
public class RateKeyDeclaration extends XMLBeanDeclaration {
	
	//private static Logger logger = LogFactory.getInstance(RateKeyDeclaration.class);
	public static final String TEXT_KEY_CLASS = "jp.co.nttdata.rate.model.rateKey.TextKey";	
	public static final String SELECTABLE_KEY_CLASS = "jp.co.nttdata.rate.model.rateKey.SelectableKey";
	public static final String RATEKEY_RULE_CLASS = "jp.co.nttdata.rate.model.rateKey.rule.RateKeyRule";
	public static final String ITEMS_CLASS = "java.util.ArrayList";
	public static final String ITEM_CLASS = "jp.co.nttdata.rate.model.rateKey.Item";
	
	/** RateKey-definition�@XML�ɒ�`���ꂽ�m�[�h�� */
	public static final String RATEKEY_NODE = "RateKey";
	public static final String TEXT_NODE = "text";
	public static final String RADIO_NODE = "radio";
	public static final String COMBO_NODE = "combo";
	public static final String RULE_NODE = "rule";
	public static final String ITEMS_NODE = "items";
	public static final String ITEM_NODE = "item";
	private static final String ATTR_RULE_SPECIALVAL = "specialValues";	
	
	/** ���[�g�L�[��BeanFactory�� */
	private static final String RATE_KEY_BEANFACTORY = "RateKeyBeanFactory";
		
	public RateKeyDeclaration(HierarchicalConfiguration config, String key, boolean optional) {
		super(config, key, optional);
	}
	public RateKeyDeclaration(HierarchicalConfiguration config, String key) {
		super(config, key);

	}	
	public RateKeyDeclaration(SubnodeConfiguration configurationAt,
			ConfigurationNode node) {
		super(configurationAt, node);
	}
	
	/**
	 * ���[�g�L�[�y�ю�ނɉ����ăN���X��ҏW
	 */
	@Override
	public String getBeanClassName() {
		
		//���[�g�L�[�̃^�C�v���擾
		String keyName = this.getNode().getName();
		
		//���[�g�L�[��ނɉ������N���X��ҏW
		if (keyName.equals(RATEKEY_NODE)) {
			//TODO �ق���API�ő���type���擾�ł��Ȃ��́H
			String keyType = (String) ((ConfigurationNode)this.getNode().getAttributes("type").get(0)).getValue();
			if (keyType.equals(TEXT_NODE)) {
				return TEXT_KEY_CLASS;
			} else {
				return SELECTABLE_KEY_CLASS;
			}			
		} else if (keyName.equals(RULE_NODE)) {
			return RATEKEY_RULE_CLASS;
		} else if (keyName.equals(ITEMS_NODE)) {
			return ITEMS_CLASS;
		} else {
			return ITEM_CLASS;
		}
				
	}
	
	/**
	 * ���[�g�L�[�̃C���X�^���X���쐬�p��FactoryName
	 */
	@Override
	public String getBeanFactoryName() {
		return RATE_KEY_BEANFACTORY;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public Map getBeanProperties() {
        Map props = new HashMap();
        for (Iterator it = getNode().getAttributes().iterator(); it.hasNext();)
        {
            ConfigurationNode attr = (ConfigurationNode) it.next();
            String propName = attr.getName();
            Object propVal = attr.getValue();
            if (!isReservedNode(attr)) {
            	
            	//����l�������w��̏ꍇ�i��FRateKeyRule��specialValues�j
            	if (propName.equals(ATTR_RULE_SPECIALVAL)) {
            		if (props.containsKey(propName)) {
            			//����ȍ~�̏ꍇ
            			Object specialVals = props.get(propName);
            			((List)specialVals).add(propVal);
            		} else {
            			//����̏ꍇ�A���X�g���Z�b�g
            			List specialVals = new ArrayList();
            			specialVals.add(propVal);
            			props.put(propName, specialVals);
            		}
            	} else {
            		props.put(propName, interpolate(propVal));
            	}     
            }
        }
        
        //����l�̃��X�g��z��ɕϊ�����
        if (props.containsKey(ATTR_RULE_SPECIALVAL)) {
        	props.put(ATTR_RULE_SPECIALVAL, ((List)props.get(ATTR_RULE_SPECIALVAL)).toArray());
        }

        return props;
	}
	
	
	/**
	 * Creates a new BeanDeclaration for a child node of the 
	 * current configuration node. 
	 */
	@Override
	protected BeanDeclaration createBeanDeclaration(ConfigurationNode node) {
	    return new RateKeyDeclaration(getConfiguration().configurationAt(node.getName()), node);
	}
	
	@SuppressWarnings("unchecked")
	@Override
    public Map getNestedBeanDeclarations() {
        Map nested = new HashMap();
        
        List itemsDecl = new ArrayList();
        for (Iterator it = getNode().getChildren().iterator(); it.hasNext();)
        {
        	int index = 0;
            ConfigurationNode child = (ConfigurationNode) it.next();
            if (!isReservedNode(child))
            {	
            	String name = child.getName();
            	if (name.equals(ITEM_NODE)) {
            		index++;
                    //���W�I��R���{�̃A�C�e�����i�[����
            		//TODO items�̂����A�B���item������ꍇ�AconfigurationAt(name+"("+index+")")�ɂ͖�肪����
        			BeanDeclaration itemDecl = new RateKeyDeclaration(getConfiguration().configurationAt(name+"("+index+")"), child);
        			itemsDecl.add(itemDecl);
            	} else {
            		nested.put(name, createBeanDeclaration(child));
            	}  
            }
        }
        
        //item��BeanDeclaration���X�g���Z�b�g
		nested.put(ITEM_NODE, itemsDecl);

        return nested;
    }

}
