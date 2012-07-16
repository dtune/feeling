package jp.co.nttdata.rate.util;

import java.io.IOException;
import java.util.Properties;

public class PropertiesUtil {
	
	private static Properties msgProp = PropertiesUtil.getExternalProperties(Const.MESSAGE_PROPERTIES);
	
	public static Properties getExternalProperties(String propFileName) {
	
		Properties prop = new Properties();

		try {
			prop.load(ResourceLoader.getExternalResourceAsStream(propFileName));
		} catch (IOException e) {
			e.printStackTrace();
			throw new RuntimeException("外部ファイルよってプロパティの生成が失敗しました。");
		}
		
		if (prop == null) {
			throw new RuntimeException("外部ファイルよってプロパティの生成が失敗しました。");
		}
		
		return prop;
	}

	/**
	 * ルートから相対パスよりプロパティを取得
	 * @param internalPropFilePath
	 * @return
	 */
	public static Properties getInternalProperties(String internalPropFilePath) {
		Properties prop = new Properties();

		try {
			prop.load(PropertiesUtil.class.getClassLoader().getResourceAsStream(internalPropFilePath));
		} catch (IOException e) {
			e.printStackTrace();
			throw new RuntimeException("内部ファイルよってプロパティの生成が失敗しました。");
		}
				
		return prop;
	}
	
	/**
	 * 指定クラスとプロパティファイル名よりプロパティを取得
	 * @param clazz
	 * @param propFileName
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static Properties getPropertiesByClass(Class clazz, String propFileName) {
		Properties prop = new Properties();

		try {
			prop.load(clazz.getResourceAsStream(propFileName));
		} catch (IOException e) {
			e.printStackTrace();
			throw new RuntimeException("内部ファイルよってプロパティの生成が失敗しました。");
		}
				
		return prop;
	}
	
	/**
	 * 入力チェックの場合、エラータイプに応じてエラーメッセージを取得する
	 * @param errType
	 * @return
	 */
	public static String getErrorTypeMessage(String errType) {
		return msgProp.getProperty(getKeyPrefix(errType, Const.ERROR));
		
	}
	
	private static String getKeyPrefix(String keyName, String prefix) {
		StringBuffer sb = new StringBuffer();
		return sb.append(prefix).append(Const.DOT).append(keyName).toString();
	}
	
}
