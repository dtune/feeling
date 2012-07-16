/**
 * 
 */
package jp.co.nttdata.rate.batch;

import java.io.File;
import java.io.FileFilter;

import org.apache.commons.lang.StringUtils;

/**
 * 検証ツールにサポートされるデータファイルを洗い出す
 * <p>拡張子：.dat,.csv,.txt（コンマ区切り）、デフォルト場合、.datとする</p>
 * @author btchoukug
 *
 */
public class BatchInputFileFilter implements FileFilter {

	private static final String DEFAULT_SUFFIX = ".dat";
	private String[] suffix;
	private String[] upperCaseSuffix;
	
	/**
	 * デフォルトとして、.datをフィルターする
	 */
	public BatchInputFileFilter() {
		this.suffix = new String[]{DEFAULT_SUFFIX};
		this.upperCaseSuffix = new String[]{StringUtils.upperCase(DEFAULT_SUFFIX)};
	}
	
	/**
	 * 指定の拡張子より、ファイルフィルターを作成
	 * @param suffix
	 */
	public BatchInputFileFilter(String[] suffix) {
		setFileSuffix(suffix);
	}
	
	public void setFileSuffix(String[] suffix) {
		if (suffix == null || suffix.length == 0) {
			throw new IllegalArgumentException("拡張子を指定してください");
		}
		
		this.suffix = suffix;
		int len = suffix.length;
		
		this.upperCaseSuffix = new String[len];
		for (int i = 0; i < len; i++) {
			this.upperCaseSuffix[i] = StringUtils.upperCase(suffix[i]);	
		}		 
	}
	
	public String[] getFileSuffix() {
		return this.suffix;
	}
	
	/* (non-Javadoc)
	 * @see java.io.FileFilter#accept(java.io.File)
	 */
	@Override
	public boolean accept(File arg0) {
		
		if (arg0.isFile()) {
			
			String fileName = arg0.getName();
			int i = 0;
			for (String suf : this.suffix) {
				//指定の拡張子のファイルの場合、trueを返す
				if (fileName.endsWith(suf) || fileName.endsWith(this.upperCaseSuffix[i])) {
					return true;
				}
				i++;
			}
			
			return false;
			
		} else if (arg0.isDirectory()) {
			//フォルダーの場合、したのファイルを取得するため、trueを返す
			return true;
		} else {
			return false;	
		}
		
	}

}
