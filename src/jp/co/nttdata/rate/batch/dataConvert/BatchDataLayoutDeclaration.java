package jp.co.nttdata.rate.batch.dataConvert;

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

public class BatchDataLayoutDeclaration extends XMLBeanDeclaration {
	
	public static final String BATCH_DATA_LAYOUT_CLASS = "jp.co.nttdata.rate.batch.dataConvert.BatchDataLayout";
	public static final String RATEKEY_LAYOUT_CLASS = "jp.co.nttdata.rate.batch.dataConvert.RateKeyLayout";
	
	/** RateKeyLayout-definition　XMLに定義されたノード名 */
	public static final String KEY_NODE = "key";
	public static final String TRANSFER_NODE = "transfer";
	public static final Object LAYOUT_DATA = "layoutData";
	
	/** レートキーレイアウトのBeanFactory名 */
	public static final String BATCH_DATA_LAYOUT_BEANFACTORY = "BatchDataLayoutBeanFactory";	
		
	public BatchDataLayoutDeclaration(HierarchicalConfiguration config, String key, boolean optional) {
		super(config, key, optional);
	}
	public BatchDataLayoutDeclaration(HierarchicalConfiguration config, String key) {
		super(config, key);

	}	
	public BatchDataLayoutDeclaration(SubnodeConfiguration configurationAt,
			ConfigurationNode node) {
		super(configurationAt, node);
	}

	@Override
	public String getBeanClassName() {
		
		String nodeName = this.getNode().getName();
		
		if (nodeName.equals(KEY_NODE)) {
			//レートキーレイアウトのクラスを返す(transferの場合も)
			return RATEKEY_LAYOUT_CLASS;
		} else {
			//計算カテゴリ単位のデータレイアウトのクラスを返す
			return BATCH_DATA_LAYOUT_CLASS;
		}
				
	}
	
	@Override
	public String getBeanFactoryName() {
		return BATCH_DATA_LAYOUT_BEANFACTORY;
	}
	
		
	/**
	 * Creates a new BeanDeclaration for a child node of the 
	 * current configuration node. 
	 */
	@Override
	protected BeanDeclaration createBeanDeclaration(ConfigurationNode node) {
	    return new BatchDataLayoutDeclaration(getConfiguration().configurationAt(node.getName()), node);
	}
		
	@SuppressWarnings("unchecked")
	@Override
    public Map getNestedBeanDeclarations() {
        Map nested = new HashMap();
        
        List keysDecl = new ArrayList();
        for (Iterator it = getNode().getChildren().iterator(); it.hasNext();) {
        	int index = 0;
            ConfigurationNode child = (ConfigurationNode) it.next();
            if (!isReservedNode(child)) {	
            	String name = child.getName();
            	if (name.equals(KEY_NODE)) {            		
            		index++;
                    //キーのレイアウト定義を格納する
        			BeanDeclaration keyDecl = new BatchDataLayoutDeclaration(
        					getConfiguration().configurationAt(name + "(" + index + ")"),
        					child);
        			keysDecl.add(keyDecl);
            	} else {
            		//TODO 置換公式transferの場合
            		//nested.put(name, createBeanDeclaration(child));
            		return null;
            	}  
            }
        }
        
        //keyのBeanDeclarationリストをセット
		nested.put(LAYOUT_DATA, keysDecl);

        return nested;
    }

}
