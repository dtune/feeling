package jp.co.nttdata.rate.model.datalayout;

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
public class DataLayoutDeclaration extends XMLBeanDeclaration {
	
	
	public static final String CATEGORY_CLASS = "jp.co.nttdata.rate.model.datalayout.InputDataCategory";
	
	public static final String DATALAYOUT_CLASS = "jp.co.nttdata.rate.model.datalayout.DataLayout";
	
	/** RateKey-definition　XMLに定義されたノード名 */
	public static final String CATEGORY_NODE = "Category";
	public static final String KEY_NODE = "key";
	
	
	/** レートキーのBeanFactory名 */
	private static final String DATALATOUT_BEANFACTORY = "DataLayoutBeanFactory";
	public static final Object LAYOUT_DATA = "key";
		
	public DataLayoutDeclaration(HierarchicalConfiguration config, String key, boolean optional) {
		super(config, key, optional);
	}
	public DataLayoutDeclaration(HierarchicalConfiguration config, String key) {
		super(config, key);

	}	
	public DataLayoutDeclaration(SubnodeConfiguration configurationAt,
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
		if (keyName.equals(CATEGORY_NODE)) {
			return CATEGORY_CLASS;	
		} else
			return DATALAYOUT_CLASS;
				
	}
	
	/**
	 * レートキーのインスタンスを作成用のFactoryName
	 */
	@Override
	public String getBeanFactoryName() {
		return DATALATOUT_BEANFACTORY;
	}

//	@SuppressWarnings("unchecked")
//	@Override
//	public Map getBeanProperties() {
//		Map props = new HashMap();
//		for (Iterator it = getNode().getAttributes().iterator(); it.hasNext();) {
//			ConfigurationNode attr = (ConfigurationNode) it.next();
//			String propName = attr.getName();
//			Object propVal = attr.getValue();
//			if (!isReservedNode(attr)) {
//
//				// 特殊値が複数指定の場合（例：RateKeyRuleのspecialValues）
//				if (propName.equals(ATTR_RULE_SPECIALVAL)) {
//					if (props.containsKey(propName)) {
//						// 初回以降の場合
//						Object specialVals = props.get(propName);
//						((List) specialVals).add(propVal);
//					} else {
//						// 初回の場合、リストをセット
//						List specialVals = new ArrayList();
//						specialVals.add(propVal);
//						props.put(propName, specialVals);
//					}
//				} else {
//					props.put(propName, interpolate(propVal));
//				}
//			}
//		}
//
//		// 特殊値のリストを配列に変換する
//		if (props.containsKey(ATTR_RULE_SPECIALVAL)) {
//			props.put(ATTR_RULE_SPECIALVAL, ((List) props
//					.get(ATTR_RULE_SPECIALVAL)).toArray());
//		}
//
//		return props;
//	}

//	/**
//	 * レートキーのインスタンスを作成する際に、型毎の属性を無視する
//	 */
//	@Override
//	protected boolean isReservedNode(ConfigurationNode nd) {
//		boolean flg = false;
//		if (nd.isAttribute()
//				&& (nd.getName() == null || nd.getName().equals(INIT_VAL))) {
//			flg = true;
//		} else if (nd.getName().equals(ITEMS_NODE)
//				|| nd.getName().equals(ITEM_NODE)) {
//			flg = true;
//		}
//
//		return flg;
//	}

	/**
	 * Creates a new BeanDeclaration for a child node of the current
	 * configuration node.
	 */
	@Override
	protected BeanDeclaration createBeanDeclaration(ConfigurationNode node) {
		return new DataLayoutDeclaration(getConfiguration().configurationAt(
				node.getName()), node);
	}

	@SuppressWarnings("unchecked")
	@Override
	public Map getNestedBeanDeclarations() {
		Map nested = new HashMap();

		List itemsDecl = new ArrayList();
		for (Iterator it = getNode().getChildren().iterator(); it.hasNext();) {
			int index = 0;
			ConfigurationNode child = (ConfigurationNode) it.next();
			if (!isReservedNode(child)) {
				String name = child.getName();
				if (name.equals(KEY_NODE)) {
					index++;
					// ラジオやコンボのアイテムを格納する
					BeanDeclaration itemDecl = new DataLayoutDeclaration(
							getConfiguration().configurationAt(
									name + "(" + index + ")"), child);
					itemsDecl.add(itemDecl);
				} else {
					nested.put(name, createBeanDeclaration(child));
				}
			}
		}

		// keyのBeanDeclarationリストをセット
		nested.put(KEY_NODE, itemsDecl);

		return nested;
	}

}
