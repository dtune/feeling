package jp.co.nttdata.rate.batch.dataConvert;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import jp.co.nttdata.rate.model.CalculateCategory;
import jp.co.nttdata.rate.model.CategoryManager;
import jp.co.nttdata.rate.model.rateKey.RateKeyManager;
import jp.co.nttdata.rate.util.CommonUtil;
import jp.co.nttdata.rate.util.Const;
import jp.co.nttdata.rate.util.ResourceLoader;
import org.apache.commons.configuration.XMLConfiguration;
import org.apache.commons.configuration.beanutils.BeanFactory;
import org.apache.commons.configuration.beanutils.BeanHelper;
import org.apache.commons.lang.StringUtils;

public class BatchDataLayoutFactory {

	private static final String DTD_PUBLIC_ID = "InputLayoutDef";
	/**デフォルトレイアウトのノード名*/
	public static final String DEFAULT_LAYOUT = "default-layout";
	public static final String ERROR_LAYOUT = "error-layout";
		
	public static final String LAYOUT = "layout";
	public static final String CATEGORY = "Category";
	
	/**デフォルトレイアウト*/
	//private BatchDataLayout defaultDataLayout;
	private BeanFactory _factory;
		
	public BatchDataLayoutFactory() {
		_factory = new BatchDataLayoutBeanFactory();
		BeanHelper.registerBeanFactory(BatchDataLayoutDeclaration.BATCH_DATA_LAYOUT_BEANFACTORY, _factory);
	}
	
	//singleton
	private static class XMLConfigHolder {
		private static XMLConfiguration config;
		static {
			//バッチ計算用データレイアウトのXML全体を読む
			URL xmlUrl = ResourceLoader.getExternalResource(Const.BT_DATA_LAYOUT_DEF_DIR);
			URL dtdUrl = ResourceLoader.getExternalResource(Const.FORMULA_DEF_DTD);	
			config = CommonUtil.loadXML(xmlUrl, DTD_PUBLIC_ID, dtdUrl, true);
		}
	}
	
	private XMLConfiguration _getConfig() {
		return XMLConfigHolder.config;
	}
	
	/**
	 * クエリーキーよりデータレイアウトをロード
	 * @param searchKey
	 * @return
	 */
	private BatchDataLayout _loadRateKeyLayout(String searchKey) {
		//TODO transferはまだ対応してない
		BatchDataLayoutDeclaration decl = new BatchDataLayoutDeclaration(_getConfig(), searchKey);
		BatchDataLayout dataLayout = (BatchDataLayout) BeanHelper.createBean(decl);	
		
		return dataLayout; 
	}
	
	/***
	 * 指定商品のBT計算データレイアウトをロードする
	 * <br>特に定義されない場合、デフォルトレイアウトを使う
	 * @return 
	 */
	@SuppressWarnings("unchecked")
	public BatchDataLayout getDataLayout(String code, String cate, boolean isFilterByUI) {
				
		//商品特定の場合、指定コードに応じてデータレイアウトを確定
		BatchDataLayout layout = null;
		String searchKey = LAYOUT + "[@name]" ;
		List nameList = _getConfig().getList(searchKey);
		
		for (int i = 0; i < nameList.size(); i++) {
			String name = (String) nameList.get(i);
			if (StringUtils.contains(name, code.substring(0, 3))) {
				layout = _loadRateKeyLayout(LAYOUT + "(" + i + ")" + Const.DOT + cate);
				break;
			}
		}
				
		//特に定義していない場合、フォルトレイアウトを返す
		if (layout == null) {
			layout = _loadRateKeyLayout(DEFAULT_LAYOUT + Const.DOT + cate);
		}
				
		layout.setCode(code.substring(0, 3));
		layout.setCate(cate);
		
		//UI定義どおり、使われるレートキーを洗い出す
		if (isFilterByUI) {
			RateKeyManager.newInstance(code);
			CategoryManager.newInstance(code);
			layout.setLayoutData(_filterByUIDef(cate, layout));	
		}
		
		return layout;
		
	}
	
	/***
	 * エラーフラグのレイアウトをロードする
	 * @return 
	 */
	public BatchDataLayout getErrorDataLayout(String cate) {

		BatchDataLayout errorLayout = null;

		// エラーデータレイアウトを取得
		errorLayout = _loadRateKeyLayout(ERROR_LAYOUT + Const.DOT + cate);

		return errorLayout;

	}
	
	public BatchDataLayout getDataLayout(String code, String cate) {
		return getDataLayout(code, cate, false);
	}
	
	private List<RateKeyLayout> _filterByUIDef(String cate, BatchDataLayout layout) {
		List<RateKeyLayout> origLayoutList = layout.getLayoutData();
		
		CalculateCategory category = CategoryManager.getInstance().getCateInfo(cate);
		List<RateKeyLayout> filterLayoutList = new ArrayList<RateKeyLayout>(60);

		for (String key : category.getKeys()) {
			for (RateKeyLayout keyLayout : origLayoutList) {
				if (key.equals(keyLayout.getName())) {
					filterLayoutList.add(keyLayout);
					break;
				}
			}
		}
		
		return filterLayoutList;
	}

	public static void main(String[] args) {
		BatchDataLayoutFactory factory = new BatchDataLayoutFactory();		
		BatchDataLayout layout = factory.getDataLayout("030", CalculateCategory.P);
		
		for (RateKeyLayout keyLayout : layout.getLayoutData()) {
			System.out.println(keyLayout.toString());
		}
	}
	
}
