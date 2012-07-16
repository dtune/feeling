package jp.co.nttdata.rate.batch;

import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import jp.co.nttdata.rate.util.Const;

/**
 * �v�Z���ʂ��r���������ŁAOK������NG��t���ĕԂ�
 * <br>�܂��A��r�Ώۂ��w�b�_���ҏW�����
 * @author btchoukug
 *
 */
public class DefaultCalculateResultComparator implements
		ICalculateResultComparator {

	private Map<String,String> compareMapping;
		
	private final String compareMark = "��";
	/**��v�̃}�b�N*/
	private final String OK_DEFAULT = "OK";
	/**��v�łȂ��̃}�b�N*/
	private final String NG_DEFAULT = "NG";
	
	/**��r���ʁF��v*/
	private String ok;
	
	/**��r���ʁF��v���Ă��Ȃ�*/
	private String ng;
	
	/**�m�f����*/
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
			throw new IllegalArgumentException("�w�b�_�Ȃ��B");
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
			throw new IllegalArgumentException("��r�Ώۂ��w�肭�������B");
		}
		
		boolean ok = true;
		
		for (Iterator<Entry<String,String>> it = this.compareMapping.entrySet().iterator();
			it.hasNext();) {
			Entry<String,String> entry = it.next();
			
			double compareValue = rateKeys.get(entry.getKey());
			double dest = result.get(entry.getValue());
			//��r���ʂ𖖔��ɒǉ�			
			sbCompareResult.append(Const.COMMA);
			
			if (compareValue == dest) {
				sbCompareResult.append(_getOkMark());
			} else {
				sbCompareResult.append(_getNgMark());
				ok = false;
				this.ngCount ++;
			}
		}
		//�ҏW�����������Ԃ�
		return ok;
	}

	private String _getOkMark() {
		return (ok == null) ? OK_DEFAULT : this.ok;
	}

	/**
	 * ��v�̃}�b�N��ݒ�
	 * @param ok
	 */
	public void setOkMark(String ok) {
		this.ok = ok;
	}

	private String _getNgMark() {
		return (ng == null) ? NG_DEFAULT : this.ng;
	}

	/**
	 * ��v�łȂ��̃}�b�N��ݒ�
	 * @param ng
	 */
	public void setNgMark(String ng) {
		this.ng = ng;
	}
	
	

}
