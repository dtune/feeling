package jp.co.nttdata.rate.util;

import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jp.co.nttdata.rate.exception.FmsRuntimeException;
import jp.co.nttdata.rate.log.LogFactory;
import org.apache.commons.beanutils.ConvertUtils;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.XMLConfiguration;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

/**
 * 業務に関係ないの共通モジュール
 * @author btchoukug
 *
 */
public class CommonUtil {
	
	private static final String[] LOGIC_ABBREVIATIONS = new String[]{"lt","gt","eq","le","ge","neq","and","or"};
	
	/** ロジック計算記号 */
	private static final String[] LOGIC_OPS = new String[]{"<",">","==","<=",">=","!=","&&","||"};
	
	/**XML検証用のDTDファイルのシステムIDの先頭*/
	private static final String DTD_PUBLIC_PREFIX = "//NTTData Insurance Rate Calculation/";
		
	private static Logger logger = LogFactory.getInstance(CommonUtil.class);

	public static boolean isNumeric(String string) {
		return string.matches("^[-+]?\\d+(\\.\\d+)?$");
		//本来が2*fとなるべきですが、書きミスで2fになってしまいました。
		//だた、ApacheのNumberUtils.isNumber方法で2fがfloatタイプの2と認識されて誤判しました。
		//return NumberUtils.isNumber(string);
	}
	
	
	/**
	 * 公式定義の中身から、余計な改行やタブ、半角スペースを外す
	 * @param str
	 * @return
	 */
	public static String deleteWhitespace(String str) {
		return StringUtils.deleteWhitespace(str);
	}
	
	/**
	 * JSONフォーマットの文字列をMapに転換する
	 * @param str
	 * @return
	 * @throws IllegalArgumentException 
	 */
	@SuppressWarnings("unchecked")
	public static Map JSONString2Map(String jsonStr, Class MapValueType) throws IllegalArgumentException {
		if (StringUtils.isBlank(jsonStr))
			return null;
		if (jsonStr.indexOf(Const.SEMICOLON) < 0) {
			throw new IllegalArgumentException(jsonStr + "転換元の文字列にはコンマが含めていません（フォーマット：gen=4;sptate=0）");
		}

		Map destMap = new HashMap<String, Object>();
		String[] pairs = jsonStr.split(Const.SEMICOLON);
		for (int i = 0; i < pairs.length; i++) {

			String[] splits = pairs[i].split(Const.EQ);
			if (splits.length == 2) {
				
				destMap.put(splits[0], ConvertUtils.convert(splits[1], MapValueType));
			} else {
				throw new IllegalArgumentException(pairs[i] + "ペアの内に「=」が含めていません、或いは重複「=」があります（フォーマット：gen=4;sptate=0）");
			}

		}

		return destMap;
	}

	/**
	 * 半角から全角へ転換
	 * @param input
	 * @return
	 */
	public static String ToSBC(String input) {
		char c[] = input.toCharArray();
		for (int i = 0; i < c.length; i++) {
			if (c[i] == ' ') {
				c[i] = '\u3000';
			} else if (c[i] < '\177') {
				c[i] = (char) (c[i] + 65248);

			}
		}
		return new String(c);
	}

	/**
	 * 全角から半角へ転換
	 * @param input
	 * @return
	 */
	public static String ToDBC(String input) {

		char c[] = input.toCharArray();
		for (int i = 0; i < c.length; i++) {
			if (c[i] == '\u3000') {
				c[i] = ' ';
			} else if (c[i] > '\uFF00' && c[i] < '\uFF5F') {
				c[i] = (char) (c[i] - 65248);

			}
		}

		String returnString = new String(c);
		return returnString;
	}
	
	
	/**
	 * 指定の文字列の中に、レートキーが含むだけかどうか
	 * @param str
	 * @param keys
	 * @return
	 */
	public static boolean containsOnly(String[] str, String[] keys) {
		if (ArrayUtils.isEmpty(str) || ArrayUtils.isEmpty(keys)) {
			return false;
		}
		outer: for (int i = 0; i < str.length; i++) {
			for (int j = 0; j < keys.length; j++) {
				if (str[i].equals(keys[j])) {
					 continue outer;
				}
			}
			return false;
		}
		return true;
	}
		
	/**
	 * XMLの中身に、&lt;や&gt;などのオペレータは直接に入力できないため、英語略称を計算されるオペレータに置換する
	 * @param formula
	 * @return
	 */
	public static String convertAbbreviation2OP(String formula) {
	
		for (int i = 0; i < LOGIC_ABBREVIATIONS.length; i++) {
			if (formula.indexOf(LOGIC_ABBREVIATIONS[i]) > 0) {
				formula = formula.replaceAll(LOGIC_ABBREVIATIONS[i], LOGIC_OPS[i]);
			}			
		}
		return formula;
	}
	
	/**
	 * get a hash value for string(32bit) 
	 * @param data
	 * @return
	 */
    public static int FNVHash1(String data)   
    {   
    	if (data == null) return 0;
    	
    	final int p = 16777619;   
        int hash = (int)2166136261L;   
        for(int i=0; i < data.length(); i++)   
            hash = (hash ^ data.charAt(i)) * p;   
        hash += hash << 13;   
        hash ^= hash >> 7;   
        hash += hash << 3;   
        hash ^= hash >> 17;   
        hash += hash << 5;   
        return hash;   
    }
    
    /**
     * XMLファイルをロードする
     * @param xmlUrl
     * @param publicId
     * @param dtdUrl
     * @param delimiterParsingDisabled
     * @return
     */
    public static XMLConfiguration loadXML(URL xmlUrl,String publicId, URL dtdUrl, boolean delimiterParsingDisabled) {
			
    	XMLConfiguration config = new XMLConfiguration();
    	
		try {
			//XMLのURLを設定
			config.setURL(xmlUrl);
			
			// DTDよりxmlのvalidationを行う
			config.registerEntityId(DTD_PUBLIC_PREFIX + publicId, dtdUrl);
			config.setValidating(true);
			
			//公式定義XMLの中身に、デフォルトのコンマ分割を禁止
			config.setDelimiterParsingDisabled(delimiterParsingDisabled);
			
			config.load();
			if (logger.isDebugEnabled()) logger.debug(xmlUrl+"がロードしました。");
			
			return config;

		} catch (ConfigurationException e) {
			throw new FmsRuntimeException("定義XMLには問題があるため、ロード失敗でした。" + xmlUrl.toExternalForm(), e);
		}
    }
    
	/**
	 * mapのキーは昇順でソートしてkey:value
	 * という形で編集した文字列を返す
	 * @param input
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static String map2string(Map input) {
		
		if (input == null || input.size() == 0) return Const.EMPTY;
		
		Object[] keys = input.keySet().toArray();
		List<String> lKeys = new ArrayList<String>();
		CollectionUtils.addAll(lKeys, keys);
		//昇順でソート
		Collections.sort(lKeys);
		
		//key:valueのpairという形で編集する
		StringBuffer sb = new StringBuffer(500);
		for (String key : lKeys) {
			sb.append(key).append(Const.COLON).append(input.get(key));
		}
		
		return sb.toString();
		
	}
	
	/**
	 * key1=value1,key2=value2...の形の文字列をMapに転換する
	 * @param ratekeyTxt
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static Map str2Map(String ratekeyTxt) {
		
		String pairTxt = ratekeyTxt.substring(1, ratekeyTxt.length() - 1);
		if (StringUtils.isBlank(pairTxt) || pairTxt.indexOf(Const.COMMA) < 0 || pairTxt.indexOf(Const.EQ) < 0) {
			throw new IllegalArgumentException("レートキーの文字列が間違っていた");
		}
		
		Map rateKeys = new HashMap();
		String[] pairs = StringUtils.split(pairTxt, Const.COMMA);
		for (String pair : pairs) {
			String[] entry = StringUtils.deleteWhitespace(pair).split(Const.EQ);
			if (entry.length == 2) {
				rateKeys.put(entry[0], ConvertUtils.convert(entry[1], double.class));
			} else {
				throw new IllegalArgumentException(entry[0] + "にてキー或いは値が漏れ");
			}
		}
				
		return rateKeys;		
		
	}	
	
	  /**
	   * Sets the size of the shell to it's "packed" size, unless that makes it
	   * bigger than the display, in which case set it to 9/10 of display size.
	   */
	 public static void setShellSize(Display display, Shell shell) {
		 Rectangle bounds = display.getBounds();
		 Point size = shell.computeSize(SWT.DEFAULT, SWT.DEFAULT);
		 if (size.x > bounds.width)
			 size.x = bounds.width * 9 / 10;
		 if (size.y > bounds.height)
			 size.y = bounds.height * 9 / 10;
		 shell.setSize(size);
	 }
	 
		
	 public static void setShellLocation(Shell shell) {
		 Rectangle shellRec = shell.getBounds();
		 Rectangle displayRec = Display.getCurrent().getBounds();
		 Point p = new Point((displayRec.width - shellRec.width) / 2,
				 (displayRec.height - shellRec.height) / 2);
		 shell.setLocation(p);
	 }

	 /**
	  * 日付タイプチェックを行う
	  * <br>日付のフォーマットがyyyyMMddで扱う
	  * @return
	  */
	public static boolean isDateType(String strDate) {
		return isDateType(strDate, null);
	}
	
	/**
	 * 指定の日付フォーマットで日付タイプチェックを行う
	 * @param strDate
	 * @param pattern
	 * @return
	 */
	public static boolean isDateType(String strDate, String pattern) {
		if (StringUtils.isBlank(strDate)) return false; 
		// TODO もっといい方法がありますか
		String ptn = pattern == null ? Const.YYYYMMDD : pattern;
		if (strDate.length() != ptn.length()) return false;
		
		SimpleDateFormat sdf = new SimpleDateFormat(ptn);
		sdf.setLenient(false);
		try {
			sdf.parse(strDate);
		} catch (ParseException e) {			
//			e.printStackTrace();
			return false;
		}
		return true;
	}
	
	/** 
	 * 
	 * 日付を変更する
	 * @param date　日付(yyyyMMdd)
	 * @return String
	 * @throws ParseException 
	 */
	public static String getFormatDate(String date) {

		SimpleDateFormat sdf = new SimpleDateFormat(Const.YYYYMMDD);
		Date dDate = null;
		try {
			dDate = sdf.parse(date);
		} catch (ParseException e) {
			throw new IllegalArgumentException(date + "日付変更できません");
		}
		String sDate = sdf.format(dDate);
		return sDate;
	}
}
