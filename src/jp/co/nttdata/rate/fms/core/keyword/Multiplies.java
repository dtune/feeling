package jp.co.nttdata.rate.fms.core.keyword;

import jp.co.nttdata.rate.fms.core.Sequence;

/**
 * �ݏ�̃L�[���[�hMULT <br>
 * ��������mult(begin,end){body}�Ƃ��� <br>
 * body�̒���INDEX���܂ޑO��ł���
 * @author btchoukug
 * 
 */
public class Multiplies extends Loop {

	public Multiplies(Sequence seq, int pos) {
		super(MULT, seq, pos);
		// �ݏ�̏����l��1�Ƃ���
	}

}
