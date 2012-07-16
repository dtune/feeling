package jp.co.nttdata.rate.batch;

import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import jp.co.nttdata.rate.util.Const;

/**
 * 計算結果を比較したうえで、OK或いはNGを付けて返す
 * <br>また、比較対象よりヘッダも編集される
 * @author btchoukug
 *
 */
public class DefaultCalculateResultComparator implements
		ICalculateResultComparator {

	private Map<String,String> compareMapping;
		
	private final String compareMark = "⇔";
	/**一致のマック*/
	private final String OK_DEFAULT = "OK";
	/**一致でないのマック*/
	private final String NG_DEFAULT = "NG";
	
	/**比較結果：一致*/
	private String ok;
	
	/**比較結果：一致していない*/
	private String ng;
	
	/**ＮＧ件数*/
	private long ngCount = 0;
	
	public long getNgCount() {
		return ngCount;
	}

	@Override
	public void setCompareMapping(Map<String, String> mapping) {
		this.compareMapping = mapping;
	}

	@Override
	public StringBuffer appendCompareHeader(StringBuffer sbHeader) {
		if (this.compareMapping == null || this.compareMapping.size() == 0) {
			throw new IllegalArgumentException("ヘッダなし。");
		}

		for (Iterator<String> it = this.compareMapping.keySet().iterator(); it.hasNext();) {
			String compareOrig = it.next();
			String compareDest = this.compareMapping.get(compareOrig);
						
			StringBuffer compareColumnHeader = new StringBuffer(Const.COMMA).append(compareOrig).
			append(compareMark).append(compareDest);			
			sbHeader.append(compareColumnHeader);
		}
		
		return sbHeader;
	}

	@Override
	public boolean compare(StringBuffer sbCompareResult, Map<String, Double> rateKeys, Map<String, Double> result) {
		
		if (this.compareMapping == null || this.compareMapping.size() == 0) {
			throw new IllegalArgumentException("比較対象を指定ください。");
		}
		
		boolean ok = true;
		
		for (Iterator<Entry<String,String>> it = this.compareMapping.entrySet().iterator();
			it.hasNext();) {
			Entry<String,String> entry = it.next();
			
			double compareValue = rateKeys.get(entry.getKey());
			double dest = result.get(entry.getValue());
			//比較結果を末尾に追加			
			sbCompareResult.append(Const.COMMA);
			
			if (compareValue == dest) {
				sbCompareResult.append(_getOkMark());
			} else {
				sbCompareResult.append(_getNgMark());
				ok = false;
				this.ngCount ++;
			}
		}
		//編集した文字列を返す
		return ok;
	}

	private String _getOkMark() {
		return (ok == null) ? OK_DEFAULT : this.ok;
	}

	/**
	 * 一致のマックを設定
	 * @param ok
	 */
	public void setOkMark(String ok) {
		this.ok = ok;
	}

	private String _getNgMark() {
		return (ng == null) ? NG_DEFAULT : this.ng;
	}

	/**
	 * 一致でないのマックを設定
	 * @param ng
	 */
	public void setNgMark(String ng) {
		this.ng = ng;
	}
	
	

}
