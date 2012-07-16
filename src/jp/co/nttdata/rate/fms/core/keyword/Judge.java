package jp.co.nttdata.rate.fms.core.keyword;

import java.util.ArrayList;
import java.util.List;
import org.apache.log4j.Logger;

import jp.co.nttdata.rate.exception.FmsRuntimeException;
import jp.co.nttdata.rate.fms.core.Range;
import jp.co.nttdata.rate.fms.core.Sequence;
import jp.co.nttdata.rate.fms.core.Token;
import jp.co.nttdata.rate.log.LogFactory;

/**
 * ���f���̏����𔻒f���āA�{�f�B���v�Z���郂�W���[��
 * <br>���f���͕K��if����AelseIf�͌J��Ԃ��Aelse�܂łƂ����O��ł���
 * @author btchoukug
 *
 */
public class Judge extends Keyword {

	private static final long serialVersionUID = 1L;

	private static Logger logger = LogFactory.getInstance(Judge.class);
	
	/**����f���̏���tokens*/
	private Sequence judgeSeq;
	/**�\������*/
	private String judgeText;
	
	private int pos = 0;
	
	private Range ifBlockRange;
	private Range elseBlockRange;

	/**�J�����gif,elseIf,else�L�[���[�h�̏���*/
	private List<Condition> conditionList = new ArrayList<Condition>();
	/**�J�����gif,elseIf,else�L�[���[�h�̃{�f�B*/
	private List<Body> bodyList = new ArrayList<Body>();
	
	public Judge(Sequence seq, int pos) {
		super(seq, pos);
		this.keyword = IF;
		this.type = KeywordType.IF;
		//�R���p�C������
		compile();
	}
	
	/**
	 * ���������ɁA�v�Z�Ɍ����̍\���𗧂��グ
	 */
	@Override
	public void compile() {
		
		_getCurrentJudgeRange();
		
		//if,elseIf,else�u���b�N�ꂸ�𒊏o����
		while (pos < elseBlockRange.end) {
		
			//if�܂���elseIf�܂���else�T�u���f�P�ʂ��擾
			Sequence subSeq = _nextBlock();

			Condition condition;
			Body body;
			if (this.type == KeywordType.IF || this.type == KeywordType.ELSEIF) {
				condition = new Condition(subSeq, 0);
				body = new Body(subSeq, condition.r.end);
			} else {
				condition = new Condition(String.valueOf(this.type), true);
				body = new Body(subSeq, 0);					
			}
			pos ++;
			conditionList.add(condition);
			bodyList.add(body);
			
			//if...else���f�̏ꍇ�A�{�f�B���Ƃ�set�𒊏o
			setBlocks.addAll(body.seq.getAllSetBlocks());
		}

	}
	
	/**
	 * �������̌���tokens�̒��ɔ��f�������݂��Ă��邩�ǂ����𔻖�����
	 * @return
	 */
	public void _getCurrentJudgeRange() {
		//���f���͕K��else�܂ŏI��邽�߁Aelse���elseIf�i�J��Ԃ����j�͈̔͂��Z�o
		elseBlockRange = getKeywordRange(this.seq, ELSE, this.searchRange);
		if (elseBlockRange == null) {
			throw new FmsRuntimeException("if����else�܂łƂ����`�ɏ����Ă�������");
		}
	
		//if�͈̔͂��擾
		ifBlockRange = getKeywordRange(this.seq, IF, null);
		//���f���S�͈̂̔͂��Z�o
		r = new Range(ifBlockRange.start, elseBlockRange.end);
		judgeSeq = this.seq.subSequence(r);
		judgeText = judgeSeq.toString();
	
	}
		
	/**
	 * �J�����g���f������1�Ԗڂ̃L�[���[�h�i�����j�o�v�Z���p�R������tokens��Ԃ�
	 * <br>�܂�if�A����elseIf�ioptional�j�A�ŏI��else
	 * @return
	 */
	private Sequence _nextBlock() {
		Range subRange = null;
		if (pos == 0) {
			subRange = ifBlockRange;
		} else if (pos < elseBlockRange.start) {
			this.type = KeywordType.ELSEIF;
			subRange = getKeywordRange(this.seq, ELSEIF, new Range(pos, elseBlockRange.end));
		} else {
			this.type = KeywordType.ELSE;
			subRange = elseBlockRange;
		}
		
		pos = subRange.end;
		return this.seq.subSequence(subRange);
	}
		
	/**
	 * ���f���͕K��if����AelseIf�͌J��Ԃ��Aelse�܂łƂ������j�b�g���v�Z����
	 * @param seq
	 * @throws Exception 
	 */
	@Override
	public Token calculate() throws Exception {
		
		//����if,elseIf,else�ꂸ�Ōv�Z���s��		
		if (logger.isDebugEnabled()) {
			logger.debug("���f�J�n:" + this.judgeText);							
		}
		
		for (int i = 0; i < conditionList.size(); i++) {
			Condition cond = (Condition) conditionList.get(i).clone();
			//�Ō��else��condition��true�ɂȂ���
			if (cond.isTrue()) {
				this.result = ((Body)bodyList.get(i).clone()).value();
				break;
			}
		}

		//�ŏI�̌v�Z���ʂ���������
		this.resultToken = new Token(this.result, 'D', r.start, 1);
		if (logger.isDebugEnabled()) {
			logger.debug("���f�I��:" + this.result);							
		}
		
		return this.resultToken;
	
	}
	
	public String toString() {
		return this.judgeText;
	}
	
}
