package jp.co.nttdata.rate.rateFundation;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;

/**
 * ���鏤�i�ɑ΂��Ďg�����O���[�v�ł���
 * <p>���̉��ɁA�����̊�p�^�����܂߂Ă���</p>
 * @author zhanghy
 *
 */
public class FundationGroupDef {
	
	/**���i�R�[�h*/
	private String[] code;
	/**�TBL�̃}�b�s���O�L�[*/
	private String groupkey;
	/**��O���[�v�̐���*/
	private String desc;
	/**��O���[�v�Ɋ܂܂���p�^�����i�[���郊�X�g*/
	private List<FundationPatternDef> patternList;
	/**��O���[�v���̑S�Ă̊���̃��X�g*/
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
			throw new IllegalArgumentException("��p�^�����Ԉ���Ă����F" + ptn);
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