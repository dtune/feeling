package jp.co.nttdata.rate.util;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;

/**
 * プロジェクトのパッケージ或いは外部リソースを読み込んで、URLあるいはファイルを返す
 * @author btchoukug
 *
 */
public class ResourceLoader {
		
	private static final String USER_DIR = "user.dir";
	private static final String FILE_PROTOCOL_PREFIX = "file:///";

	/**
	 * settingsの下のセッティングファイルはjarに入れ込む場合、配置ファイルも読めるように改善
	 * @param relativePath
	 * @return
	 */
	public static URL getUrl(String relativePath) {
		URL url = ClassLoader.getSystemClassLoader().getResource(relativePath);
		if (url == null) {
			url = Thread.currentThread().getContextClassLoader().getResource(relativePath);
		}
		
		return url;
	}

	public static URL getBaseDir() {		
		return getUrl("");
	}
	
	/**
	 * カレントプロジェクトのルートを返す
	 * @return URL
	 */	
	public static String getProjectDir() {		
		return System.getProperty(USER_DIR);
	}
	
	@SuppressWarnings("unchecked")
	public static URL getResourceByClass(Class clz, String relativePath) {
		return clz.getResource(relativePath);
	}
		
	/**
	 * カレントプロジェクトのルートから相対パスよりURLを返す
	 * <br>パッケージに含まれない場合、プロジェクトの直下に探す
	 * @param urlName
	 * @return
	 */
	public static URL getExternalResource(String relativePath) {
		
		URL url = getUrl(relativePath);
		
		//パッケージに含まれない場合、プロジェクトの直下に探す
		if (url == null) {
			try {
				url = new URL(FILE_PROTOCOL_PREFIX + getProjectDir() + File.separator + relativePath);
			} catch (MalformedURLException e) {
				throw new RuntimeException("不正なパス： " + relativePath, e);
			}
		}			
				
		return url;		
	}
	
	/**
	 * カレントプロジェクトのルートから相対パスよりファイルを返す
	 * @param urlName
	 * @return
	 */
	public static File getExternalFile(String relativePath){

		URL resUrl = getExternalResource(relativePath);
		
		File file = null;
		try {
			file = new File(resUrl.toURI());
		} catch (URISyntaxException e) {
			throw new RuntimeException("右のパスよりファイルの読込は失敗でした： " + relativePath, e);
		}
		
		return file;
	}
	
	/**
	 * カレントプロジェクトのルートから相対パスに該当するリソースをストリームとして返す
	 * @param urlName
	 * @return InputStream
	 */
	public static InputStream getExternalResourceAsStream(String relativePath) {

		URL resUrl = getExternalResource(relativePath);
		InputStream is = null;		
		try {
			is = resUrl.openStream();
		} catch (IOException e) {
			throw new RuntimeException("右のパスより読込失敗でした： " + relativePath, e);
		}
		
		return is;
	}
	
}
