package jp.co.nttdata.rate.rateFundation;

import java.util.List;

public class FundationPatternDef {
	
	private String ptn;
	private boolean isStdXtimesFixed = false;
	private String desc;
	private List<FundationDef> fundList;

	public String getDesc() {
		return desc;
	}
	public void setDesc(String desc) {
		this.desc = desc;
	}
	public List<FundationDef> getFundList() {
		return fundList;
	}
	public void setFundList(List<FundationDef> fundList) {
		this.fundList = fundList;
	}
	public void setPtn(String ptn) {
		this.ptn = ptn;
	}
	public String getPtn() {
		return ptn;
	}	
	public void setStdXtimesFixed(boolean isStdXtimesFixed) {
		this.isStdXtimesFixed = isStdXtimesFixed;
	}
	public boolean isStdXtimesFixed() {
		return isStdXtimesFixed;
	}
	public String toString() {
		return this.desc == null ? this.ptn : this.desc;
	}

}