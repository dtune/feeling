package jp.co.nttdata.rate.batch;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;

import jp.co.nttdata.rate.model.datalayout.DataLayout;

public class DatFileWriterImpl implements ICallback {

	public List<DataLayout> dataLayout;

	/** datファイルの拡張子 */
	protected final String DAT_SUFFIX = ".dat";

	protected static final int CONTENT_SIZE = 128;

	/** 行終了符号 */
	private static final String EOR = "\r\n";

	protected File file;
	protected FileWriter fileWriter;
	
	//商品コード
	private String code;

	/** 元のファイルに上書き */
	protected boolean appendEnable = false;


	public DatFileWriterImpl(List<DataLayout> dataLayout, String inputFilePath, String code)
			throws IOException {
		this.dataLayout = dataLayout;
		this.fileWriter = _createFile(inputFilePath);
		this.code = code;

	}

	@Override
	public void execute(Map<String, Integer> keys) {
		_output(_convertToString(keys));
	}

	/**
	 * 
	 * @param keys
	 * @return
	 */
	private Map<String, String> _convertToString(Map<String, Integer> keys) {
		//System.out.println(keys);
		
		if(keys.get("gen") == null) {
			throw new IllegalArgumentException("世代（始期年月日）は生成されていません。");
		}		
		
		Map<String, String> keyConvert = new HashMap<String, String>();
		for (DataLayout dataLayout : this.dataLayout) {

			String keyName = dataLayout.getName();

			int length = dataLayout.getLen();
			
			Integer keyValue = keys.get(keyName);
			
			if(keyValue == null) {
				if (length > dataLayout.getInitValue().length()) {
					keyConvert.put(keyName, StringUtils.leftPad(dataLayout.getInitValue(), length,
							"0"));
					continue;
				}
				keyConvert.put(keyName, dataLayout.getInitValue());
			} else {
				String text = keyValue.toString();
				if (length > text.length()) {
					keyConvert.put(keyName, StringUtils.leftPad(text, length,
							"0"));
					continue;
				}

				keyConvert.put(keyName, text);
			}
			
		}
		
		int gen = keys.get("gen");
		String contractDate;
		switch(gen) {
		case 1:
			contractDate = "19990331";
			break;
		case 2:
			contractDate = "20010401";
			break;
		case 3:
			contractDate = "20070331";
			break;
		case 4:
			contractDate = "20090331";
			break;
		case 5:
			contractDate = "20090402";
			break;
		default:
			//TODO 世代６
			contractDate = "20200401";
			
		}

		keyConvert.put("contractDate", contractDate);		
		keyConvert.put("insuranceCode", code);

		return keyConvert;

	}

	private FileWriter _createFile(String fileName) throws IOException {

		//ファイル名チック
		if(!fileName.endsWith(DAT_SUFFIX)) {
			fileName += DAT_SUFFIX;
		}

		file = new File(fileName);

		if (!file.exists()) {

			File dir = new File(fileName.substring(0, fileName.lastIndexOf(File.separator)));
			if (!dir.exists() && !dir.isDirectory()) {
				dir.mkdirs();
			}
			file.createNewFile();
		}

		return new FileWriter(file, this.appendEnable);// デフォルト場合追加モードではなく
	}

	/**
	 * レートキ　フォルタの輸出
	 * 
	 * @param result
	 */
	private void _output(Map<String, String> result) {

		try {

			StringBuffer sbContent = new StringBuffer();
			for (DataLayout dataLayout : this.dataLayout) {
				sbContent.append(result.get(dataLayout.getName()));
			}
			this.fileWriter.write(sbContent.append(EOR).toString());

		} catch (IOException e) {

			e.printStackTrace();
		}

	}

	/***
	 * 対象ファイルにすべての内容を書きこんだ後、ファイルをクローズする
	 */
	public void close() {
		try {
			if (fileWriter != null)
				fileWriter.close();
		} catch (IOException e) {
			throw new RuntimeException("ファイルをクローズするときにエラーが発生しまいました。ご確認ください。", e);
		} finally {
			fileWriter = null;
		}
	}

	@Override
	public void registerReader(IRateKeyReader reader) {
		;
	}

	@Override
	public void registerWriter(IBatchWriter writer) {
		;
	}

}
