package jp.co.nttdata.rate.batch;

import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import jp.co.nttdata.rate.util.Const;

/**
 * ŒvZŒ‹‰Ê‚ğ”äŠr‚µ‚½‚¤‚¦‚ÅAOKˆ½‚¢‚ÍNG‚ğ•t‚¯‚Ä•Ô‚·
 * <br>‚Ü‚½A”äŠr‘ÎÛ‚æ‚èƒwƒbƒ_‚à•ÒW‚³‚ê‚é
 * @author btchoukug
 *
 */
public class DefaultCalculateResultComparator implements
		ICalculateResultComparator {

	private Map<String,String> compareMapping;
		
	private final String compareMark = "Ì";
	/**ˆê’v‚Ìƒ}ƒbƒN*/
	private final String OK_DEFAULT = "OK";
	/**ˆê’v‚Å‚È‚¢‚Ìƒ}ƒbƒN*/
	private final String NG_DEFAULT = "NG";
	
	/**”äŠrŒ‹‰ÊFˆê’v*/
	private String ok;
	
	/**”äŠrŒ‹‰ÊFˆê’v‚µ‚Ä‚¢‚È‚¢*/
	private String ng;
	
	/**‚m‚fŒ”*/
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
			throw new IllegalArgumentException("ƒwƒbƒ_‚È‚µB");
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
			throw new IllegalArgumentException("”äŠr‘ÎÛ‚ğw’è‚­‚¾‚³‚¢B");
		}
		
		boolean ok = true;
		
		for (Iterator<Entry<String,String>> it = this.compareMapping.entrySet().iterator();
			it.hasNext();) {
			Entry<String,String> entry = it.next();
			
			double compareValue = rateKeys.get(entry.getKey());
			double dest = result.get(entry.getValue());
			//”äŠrŒ‹‰Ê‚ğ––”ö‚É’Ç‰Á			
			sbCompareResult.append(Const.COMMA);
			
			if (compareValue == dest) {
				sbCompareResult.append(_getOkMark());
			} else {
				sbCompareResult.append(_getNgMark());
				ok = false;
				this.ngCount ++;
			}
		}
		//•ÒW‚µ‚½•¶š—ñ‚ğ•Ô‚·
		return ok;
	}

	private String _getOkMark() {
		return (ok == null) ? OK_DEFAULT : this.ok;
	}

	/**
	 * ˆê’v‚Ìƒ}ƒbƒN‚ğİ’è
	 * @param ok
	 */
	public void setOkMark(String ok) {
		this.ok = ok;
	}

	private String _getNgMark() {
		return (ng == null) ? NG_DEFAULT : this.ng;
	}

	/**
	 * ˆê’v‚Å‚È‚¢‚Ìƒ}ƒbƒN‚ğİ’è
	 * @param ng
	 */
	public void setNgMark(String ng) {
		this.ng = ng;
	}
	
	

}
