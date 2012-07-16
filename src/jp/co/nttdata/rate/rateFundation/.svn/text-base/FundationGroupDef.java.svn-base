package jp.co.nttdata.rate.rateFundation;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;

/**
 * ある商品に対して使われる基数グループである
 * <p>その下に、複数の基数パタンを含めている</p>
 * @author zhanghy
 *
 */
public class FundationGroupDef {
	
	/**商品コード*/
	private String[] code;
	/**基数TBLのマッピングキー*/
	private String groupkey;
	/**基数グループの説明*/
	private String desc;
	/**基数グループに含まれる基数パタンを格納するリスト*/
	private List<FundationPatternDef> patternList;
	/**基数グループ下の全ての基数名のリスト*/
	private List<String> fundationNameList;
	
	public FundationGroupDef() {
		this.patternList = new ArrayList<FundationPatternDef>();
	}
	
	public String[] getCode() {
		return code;
	}
	public void setCode(String[] code) {
		this.code = code;
	}
	public String getDesc() {
		return desc;
	}
	public void setDesc(String desc) {
		this.desc = desc;
	}
	public List<FundationPatternDef> getPatternList() {
		return patternList;
	}
	public void setPatternList(List<FundationPatternDef> patternList) {
		this.patternList = patternList;
	}
	public boolean addPattern(FundationPatternDef fundPtn) {
		return this.patternList.add(fundPtn);
	}	
	public void setGroupkey(String groupkey) {
		this.groupkey = groupkey;
	}
	public String getGroupkey() {
		return groupkey;
	}

	public List<String> getFundationNameList() {
		
		if (this.fundationNameList == null) {
			this.fundationNameList = new ArrayList<String>();
			for (FundationPatternDef ptnDef : this.patternList) {
				for (FundationDef fundDef : ptnDef.getFundList()) {
					this.fundationNameList.add(fundDef.getName());
				}
			}
		}
		
		return this.fundationNameList;
	}
	
	public FundationPatternDef getFundationPatternDef(String ptn) {
		if (StringUtils.isBlank(ptn)) {
			throw new IllegalArgumentException("基数パタンが間違っていた：" + ptn);
		}
		if (this.patternList.size() == 0) return null;
		
		for (FundationPatternDef ptnDef : this.patternList) {
			if (ptnDef.getPtn().endsWith(ptn)) {
				return ptnDef;
			}
		}
		
		return null;
	}
	
}