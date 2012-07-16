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
			throw new RuntimeException("�O���t�@�C������ăv���p�e�B�̐��������s���܂����B");
		}
		
		if (prop == null) {
			throw new RuntimeException("�O���t�@�C������ăv���p�e�B�̐��������s���܂����B");
		}
		
		return prop;
	}

	/**
	 * ���[�g���瑊�΃p�X���v���p�e�B���擾
	 * @param internalPropFilePath
	 * @return
	 */
	public static Properties getInternalProperties(String internalPropFilePath) {
		Properties prop = new Properties();

		try {
			prop.load(PropertiesUtil.class.getClassLoader().getResourceAsStream(internalPropFilePath));
		} catch (IOException e) {
			e.printStackTrace();
			throw new RuntimeException("�����t�@�C������ăv���p�e�B�̐��������s���܂����B");
		}
				
		return prop;
	}
	
	/**
	 * �w��N���X�ƃv���p�e�B�t�@�C�������v���p�e�B���擾
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
			throw new RuntimeException("�����t�@�C������ăv���p�e�B�̐��������s���܂����B");
		}
				
		return prop;
	}
	
	/**
	 * ���̓`�F�b�N�̏ꍇ�A�G���[�^�C�v�ɉ����ăG���[���b�Z�[�W���擾����
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
