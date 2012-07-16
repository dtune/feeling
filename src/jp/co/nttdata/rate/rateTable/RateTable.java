package jp.co.nttdata.rate.rateTable;

import java.util.HashMap;
import java.util.Map;

import jp.co.nttdata.rate.fms.calculate.SimpleCalculator;
import jp.co.nttdata.rate.log.LogFactory;
import jp.co.nttdata.rate.util.CommonUtil;
import jp.co.nttdata.rate.util.Interpolation;
import jp.co.nttdata.rate.util.ResourceLoader;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.SubnodeConfiguration;
import org.apache.commons.configuration.XMLConfiguration;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

/**
 * 固定値を格納するXML
 * @author zhanghy
 *
 */
public class RateTable {

	public static final String RATE_TABLE_DEF_DIR = "settings/rateTable/";
	
	private static Logger logger = LogFactory.getInstance(RateTable.class);

	private final String RATETABLE_ATTR_NAME = "RateTable[@name]";
	private final String QUERY_KEY_NODE = "keys.key";
	private final String SET_NODE = "set";
	private final String VALUE_NODE = "value";

	private XMLConfiguration config;
	private String[] tableNames;
	
	/**検索されるキーの配列*/
	private String[] keys;
	private String keyTemplate;
	
	private Map<String, Double> rateTableMap;
	
	public RateTable(String filePath) throws ConfigurationException {
		//指定のテーブル名に応じてRateTableのXMLファイルをロードする
		config = new XMLConfiguration();
		config.setURL(ResourceLoader.getExternalResource(filePath));
		config.setDelimiterParsingDisabled(false);//コンマで分割されるようにする
		config.load();
		
		tableNames = config.getStringArray(RATETABLE_ATTR_NAME);
		rateTableMap = new HashMap<String, Double>();		
	}
	
	/**
	 * 指定のテーブル名通りにRateTableをロードする
	 * @param tableName
	 * @return 存在してない場合、falseを返す
	 */
	public boolean load(String tableName) {
		
		if (logger.isInfoEnabled()) {
			logger.info("RateTableをロード：" + tableName);	
		}
		
		//指定のレートテーブル名より適当的な階層XMLノードを取得		
		for (int i = 0; i < tableNames.length; i++) {
			
			if (tableName.equals(tableNames[i])) {
				SubnodeConfiguration subnodeConf = config.configurationAt("RateTable(" + i + ")");				
				//条件になるキーの配列を取得
				this.keys = subnodeConf.getStringArray(QUERY_KEY_NODE);
				
				//レートテーブルに格納するキーのテンプレートを作る				
				StringBuilder sb = new StringBuilder();
				for (String keyName : this.keys) {
					sb.append("${").append(keyName).append("}-");
				}
				sb.deleteCharAt(sb.length() - 1);//最後の-を削除
				this.keyTemplate = sb.toString(); 
				
				int setMaxIndex = subnodeConf.getMaxIndex(SET_NODE);
				int len = this.keys.length;				
				for (int j = 0; j <= setMaxIndex; j++) {
					String[][] keyValues = new String[len][];
					
					//セットノードのなかからキーの設定値を取得(デフォルトが０とする)
					String srhKeyPrefix = new StringBuilder(SET_NODE).append("(").append(j).append(").").toString();
					int pos = 0;
					for (String keyName : this.keys) {
						String[] keyValue = subnodeConf.getStringArray(srhKeyPrefix + keyName);
						//設定されなかった場合、ゼロとする
						keyValues[pos] = keyValue.length == 0 ? new String[]{"0"} : keyValue;
						pos++;
					}
					//上記のキーに応じて結果を取得してマップに格納する
					double ret = subnodeConf.getDouble(srhKeyPrefix + VALUE_NODE, 0d);
					Map<String, String> valuesMap = new HashMap<String, String>();
					_editValuesMap(0, keyValues, ret, valuesMap);				

				}
				
				break;
			}
				
		}
		
		//指定のテーブル名は探さなかった場合、falseを返す
		return false;
	}
	
	/**
	 * 再帰でキーの値を横展開にする
	 * @param index
	 * @param valuesMap
	 */
	private void _editValuesMap( int index, String[][] keyValues, double ret, Map<String, String> valuesMap) {
		
		for (String val : keyValues[index]) {

			valuesMap.put(this.keys[index], val);
			if (index < (keyValues.length - 1)) {
				//残りのキーを行きます
				_editValuesMap(++index, keyValues, ret, valuesMap);
			} else {
				//最後の場合、キーを生成する
				String generatedKey = Interpolation.interpolate(this.keyTemplate, valuesMap);
				rateTableMap.put(generatedKey, ret);
			}
			
		}
	}
	
	public Double getDoubleValue(Map<String,String> keys, double defaultValue) {
		Double ret = getDoubleValue(keys);
		if (ret == null) return defaultValue;
		return ret;
	}

	public Double getDoubleValue(Map<String,String> paras) {
		Map<String, String> valueMap = new HashMap<String, String>();		
		for (String keyName : this.keys) {
			if (paras.containsKey(keyName)) {
				valueMap.put(keyName, paras.get(keyName));
			} else {
				//定義されたキーにて値が指定されてない場合、ゼロとする
				valueMap.put(keyName, "0");
			}
		}
		String queryKey = Interpolation.interpolate(this.keyTemplate, valueMap);
		return this.rateTableMap.get(queryKey);
	}

	public static void main(String[] args) {
		
		try {
			
			final int TEST_COUNT = 3;
			long t1 = System.currentTimeMillis();			
			RateTable table = new RateTable(RATE_TABLE_DEF_DIR + "common.xml");	

//			table.load("PayFactor");
			long t2 = System.currentTimeMillis();
			System.out.println((t2-t1)+"ms@ratetable initilize");
//			
//			Map<String,String> cond = new HashMap<String,String>();
//			cond.put("kaisu", "2");
//			System.out.println(table.getDoubleValue(cond, 100d));
//			
//			cond.put("kaisu", "1");
//			System.out.println(table.getDoubleValue(cond, 100d));
//			
//
//			cond.put("kaisu", "4");
//			cond.put("keiro", "1");
//			System.out.println(table.getDoubleValue(cond, 1000d));	
//			
//			cond.put("keiro", "2");
//			System.out.println(table.getDoubleValue(cond, 2000d));
			
			long t11 = System.currentTimeMillis();
			table.load("bonus_lambda");
			long t22 = System.currentTimeMillis();
			System.out.println((t22-t11)+"ms@load ratetable bonus_lambda");
			Map<String,String> paras = new HashMap<String,String>();
			paras.put("gen", "2");
			paras.put("bonus_k", "0");
			paras.put("bonus_l", "5");
			System.out.println(table.getDoubleValue(paras, 200d));			
			long t3 = System.nanoTime();
			int count = TEST_COUNT;
			while (count-->0) {
				table.getDoubleValue(paras, 200d);
			}
			long t4 = System.nanoTime();
			System.out.println((t4-t3)+"ns get from ratetable for 10k times!");
			
//			paras.put("bonus_l", "6");
//			System.out.println(table.getDoubleValue(paras, 200d));
//			
//			paras.put("bonus_l", "7");
//			System.out.println(table.getDoubleValue(paras, 200d));
//
//			paras.put("bonus_k", "1");
//			System.out.println(table.getDoubleValue(paras, 200d));
			//bonus_lambda算式定義
			String formulaBody = CommonUtil.deleteWhitespace(
					 		  "if(bonus_k==0){"
							 +"	if(bonus_l==5){"
							 +"		0.999"
							 +"	}elseIf(bonus_l==6){"
							 +"		1.000"
							 +"	}elseIf(bonus_l==7){"
							 +"		1.001"
							 +"	}else{0}"
							 +"}elseIf(bonus_k==1){"
							 +"	if(bonus_l==6){"
							 +"		1.001"
							 +"	}elseIf(bonus_l==7){"
							 +"		1.002"
							 +"	}elseIf(bonus_l==8){"
							 +"		1.003"
							 +"	}else{0}"
							 +"}elseIf(bonus_k==2){"
							 +"	if(bonus_l==7){"
							 +"		1.003"
							 +"	}elseIf(bonus_l==8){"
							 +"		1.004"
							 +"	}elseIf(bonus_l==9){"
							 +"		1.005"
							 +"	}else{0}"
							 +"}elseIf(bonus_k==3){"
							 +"	if(bonus_l==8){"
							 +"		1.005"
							 +"	}elseIf(bonus_l==9){"
							 +"		1.006"
							 +"	}elseIf(bonus_l==10){"
							 +"		1.007"
							 +"	}else{0}"
							 +"}elseIf(bonus_k==4){"
							 +"	if(bonus_l==9){"
							 +"		1.007"
							 +"	}elseIf(bonus_l==10){"
							 +"		1.008"
							 +"	}elseIf(bonus_l==11){"
							 +"		1.009"
							 +"	}else{0}"
							 +"}elseIf(bonus_k==5){"
							 +"	if(bonus_l==10){"
							 +"		1.009"
							 +"	}elseIf(bonus_l==11){"
							 +"		1.010"
							 +"	}else{0}"
							 +"}elseIf(bonus_k==6){"
							 +"	if(bonus_l==11){"
							 +"		1.011"
							 +"	}else{0}"
							 +"}else{0}");
			

			try {
				long tt3 = System.currentTimeMillis();
				SimpleCalculator sc = new SimpleCalculator();
				long tt4 = System.currentTimeMillis();
				System.out.println((tt4-tt3) + "ms@SimpleCalculator initialize");
				sc.setRateKeys(paras);
				System.out.println(sc.calculate(formulaBody));
				LogFactory.setLoggerLevel(Level.OFF);
				long t33 = System.nanoTime();
				int count1 = TEST_COUNT;
				while (count1-->0) {
					sc.calculate(formulaBody);		
				}
				long t44 = System.nanoTime();
				System.out.println((t44-t33)+"ns executed by formulaEngine for 10k times!");
				
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		} catch (ConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
}

