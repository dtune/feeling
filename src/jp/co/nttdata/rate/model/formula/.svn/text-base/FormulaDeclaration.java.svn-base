package jp.co.nttdata.rate.model.formula;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import jp.co.nttdata.rate.util.CommonUtil;

import org.apache.commons.configuration.HierarchicalConfiguration;
import org.apache.commons.configuration.SubnodeConfiguration;
import org.apache.commons.configuration.XMLConfiguration;
import org.apache.commons.configuration.beanutils.BeanDeclaration;
import org.apache.commons.configuration.beanutils.XMLBeanDeclaration;
import org.apache.commons.configuration.tree.ConfigurationNode;

public class FormulaDeclaration extends XMLBeanDeclaration {

	//private static Logger logger = LogFactory.getInstance(FormulaDeclaration.class);
	public static final String FORMULA_CLASS = "jp.co.nttdata.rate.model.formula.Formula";
	private static final String FORMULA_BEANFACTORY = "FormulaBeanFactory";
	public static final String ATTR_PARAS = "paras";
	private static final String BODY = "body";
	public static final String NODE_FORMULA = "formula";
	
	public FormulaDeclaration(XMLConfiguration config, String key) {
		super(config, key);
	}
	
	public FormulaDeclaration(HierarchicalConfiguration config, String key) {
		super(config, key);
	}

	public FormulaDeclaration(SubnodeConfiguration configurationAt,
			ConfigurationNode node) {
		super(configurationAt, node);
	}

	@Override
	public String getBeanClassName() {
		return FORMULA_CLASS;
	}

	@Override
	public String getBeanFactoryName() {
		return FORMULA_BEANFACTORY;
	}

	@SuppressWarnings("unchecked")
	@Override
	public Map getBeanProperties() {
        Map props = new HashMap();
        
        //公式のボディをセット
        String body = (String) getNode().getValue();
        if (body != null) {
        	props.put(BODY, CommonUtil.deleteWhitespace(body));
        	//props.put(BODY, body);
        }        
        
        //Nodeの他の属性を取得
        for (Iterator it = getNode().getAttributes().iterator(); it.hasNext();)
        {
            ConfigurationNode attr = (ConfigurationNode) it.next();
            String propName = attr.getName();
            Object propVal = attr.getValue();
            if (!isReservedNode(attr)) {
	        	props.put(propName, interpolate(propVal));         	     
            }
        }
        
        return props;
	}
	

	@SuppressWarnings("unchecked")
	@Override
	public Map getNestedBeanDeclarations() {
        Map nested = new HashMap();
        List subFormulaDecls = null;
        int index = 0;
        for (Iterator it = getNode().getChildren().iterator(); it.hasNext();) {
        	if (subFormulaDecls == null) {
        		subFormulaDecls = new ArrayList();
        	}
        	
            ConfigurationNode child = (ConfigurationNode) it.next();
            if (!isReservedNode(child))
            {	
            	String name = child.getName();           	
                //サブ公式を格納する
    			BeanDeclaration itemDecl = new FormulaDeclaration(getConfiguration().configurationAt(name+"("+index+")"), child);
    			subFormulaDecls.add(itemDecl);
    			index++;            	  
            }
        }
        
        // formulaのBeanDeclarationリストをセット
        if (subFormulaDecls != null) {
        	nested.put(NODE_FORMULA, subFormulaDecls);
        }

        return nested;
	}
	
	/**
	 * Creates a new BeanDeclaration for a child node of the 
	 * current configuration node. 
	 */
	@Override
	protected BeanDeclaration createBeanDeclaration(ConfigurationNode node) {
	    return new FormulaDeclaration(getConfiguration().configurationAt(node.getName()), node);
	}

}
