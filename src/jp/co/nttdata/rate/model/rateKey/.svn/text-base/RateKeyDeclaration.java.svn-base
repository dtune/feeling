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
 * レートキー種類に応じたクラスを取得する
 * <br>特に、ラジオやコンボのアイテムを取得してBeanDeclarationを作成する
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
	
	/** RateKey-definition　XMLに定義されたノード名 */
	public static final String RATEKEY_NODE = "RateKey";
	public static final String TEXT_NODE = "text";
	public static final String RADIO_NODE = "radio";
	public static final String COMBO_NODE = "combo";
	public static final String RULE_NODE = "rule";
	public static final String ITEMS_NODE = "items";
	public static final String ITEM_NODE = "item";
	private static final String ATTR_RULE_SPECIALVAL = "specialValues";	
	
	/** レートキーのBeanFactory名 */
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
	 * レートキー及び種類に応じてクラスを編集
	 */
	@Override
	public String getBeanClassName() {
		
		//レートキーのタイプを取得
		String keyName = this.getNode().getName();
		
		//レートキー種類に応じたクラスを編集
		if (keyName.equals(RATEKEY_NODE)) {
			//TODO ほかのAPIで属性typeを取得できないの？
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
	 * レートキーのインスタンスを作成用のFactoryName
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
            	
            	//特殊値が複数指定の場合（例：RateKeyRuleのspecialValues）
            	if (propName.equals(ATTR_RULE_SPECIALVAL)) {
            		if (props.containsKey(propName)) {
            			//初回以降の場合
            			Object specialVals = props.get(propName);
            			((List)specialVals).add(propVal);
            		} else {
            			//初回の場合、リストをセット
            			List specialVals = new ArrayList();
            			specialVals.add(propVal);
            			props.put(propName, specialVals);
            		}
            	} else {
            		props.put(propName, interpolate(propVal));
            	}     
            }
        }
        
        //特殊値のリストを配列に変換する
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
                    //ラジオやコンボのアイテムを格納する
            		//TODO itemsのした、唯一のitemがある場合、configurationAt(name+"("+index+")")には問題がある
        			BeanDeclaration itemDecl = new RateKeyDeclaration(getConfiguration().configurationAt(name+"("+index+")"), child);
        			itemsDecl.add(itemDecl);
            	} else {
            		nested.put(name, createBeanDeclaration(child));
            	}  
            }
        }
        
        //itemのBeanDeclarationリストをセット
		nested.put(ITEM_NODE, itemsDecl);

        return nested;
    }

}
