package jp.co.nttdata.rate.rateFundation;

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

public class FundationDeclaration extends XMLBeanDeclaration {

	/**計算基礎のクラス名*/
	public static final String FUNDATION_CLASS = "jp.co.nttdata.rate.rateFundation.FundationDef";	
	public static final String FUNDATION_PATTERN_CLASS = "jp.co.nttdata.rate.rateFundation.FundationPatternDef";
	
	/**計算基礎のBeanFactory名 */
	private static final String FUNDATION_BEANFACTORY = "FundationBeanFactory";
	
	/**ファンデーショングループのノード*/
	public static final String FUNDATION_PATTERN_NODE = "FundationPattern";
	/**ファンデーションのノード*/
	public static final String FUNDATION_NODE = "Fundation";
	
	public FundationDeclaration(HierarchicalConfiguration config, String key, boolean optional) {
		super(config, key, optional);
	}
	public FundationDeclaration(HierarchicalConfiguration config, String key) {
		super(config, key);

	}	
	public FundationDeclaration(SubnodeConfiguration configurationAt,
			ConfigurationNode node) {
		super(configurationAt, node);
	}
	
	public FundationDeclaration(HierarchicalConfiguration config) {
		super(config);
		// TODO Auto-generated constructor stub
	}
	
	/**
	 * 計算基礎グループ及び計算基礎に応じてクラスを編集
	 */
	@Override
	public String getBeanClassName() {
		
		//レートキーのタイプを取得
		String keyName = this.getNode().getName();
		
		//レートキー種類に応じたクラスを編集
		if (keyName.equals(FUNDATION_PATTERN_NODE)) {
			return FUNDATION_PATTERN_CLASS;
		} else {
			return FUNDATION_CLASS;
		}
				
	}
	
	/**
	 * ファンデーションのインスタンスを作成用のFactoryName
	 */
	@Override
	public String getBeanFactoryName() {
		return FUNDATION_BEANFACTORY;
	}
	
	
	/**
	 * Creates a new BeanDeclaration for a child node of the 
	 * current configuration node. 
	 */
	@Override
	protected BeanDeclaration createBeanDeclaration(ConfigurationNode node) {
	    return new FundationDeclaration(getConfiguration().configurationAt(node.getName()), node);
	}
	
	@SuppressWarnings("unchecked")
	@Override
    public Map getNestedBeanDeclarations() {
		Map nested = null;
		String nodeName = this.getNode().getName();
		if (nodeName.equals(FUNDATION_PATTERN_NODE)) {
			nested = new HashMap();
	        List fundsDecl = new ArrayList();
	        for (Iterator it = getNode().getChildren().iterator(); it.hasNext();)
	        {
	        	int index = 0;
	            ConfigurationNode child = (ConfigurationNode) it.next();
	            if (!isReservedNode(child))
	            {	
	            	String name = child.getName();
	            	if (name.equals(FUNDATION_NODE)) {
	                    //ファンデーションを格納する
	        			BeanDeclaration fundDecl = new FundationDeclaration(getConfiguration().configurationAt(name+"("+index+")"), child);
	        			fundsDecl.add(fundDecl);
	        			index++;
	            	} else {
	            		nested.put(name, createBeanDeclaration(child));
	            	}  
	            }
	        }
	        
	        //fundListのBeanDeclarationリストをセット
			nested.put(FUNDATION_NODE, fundsDecl);			
		}

        return nested;
    }

}
