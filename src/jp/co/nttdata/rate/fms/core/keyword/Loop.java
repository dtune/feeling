package jp.co.nttdata.rate.fms.core.keyword;

import java.math.BigDecimal;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;
import org.apache.log4j.Logger;

import jp.co.nttdata.rate.exception.FmsRuntimeException;
import jp.co.nttdata.rate.fms.calculate.ICalculateContext;
import jp.co.nttdata.rate.fms.core.Parentheses;
import jp.co.nttdata.rate.fms.core.Range;
import jp.co.nttdata.rate.fms.core.Sequence;
import jp.co.nttdata.rate.fms.core.Token;
import jp.co.nttdata.rate.log.LogFactory;

public abstract class Loop extends Keyword {

	private static final long serialVersionUID = 1L;

	private static Logger logger = LogFactory.getInstance(Loop.class);

	public static final String INDEX = "index";

	/** �A���v�Z�̃L�[���[�h:sum�܂���mult */
	protected String keyword;
	private String text;
	private ICalculateContext _ctx;
	
	private Sequence loopSeq;
	/** ���v�̏����͈̔� */
	private Range condRange;

	/** �n�ltokens */
	private Sequence startSeq;
	/** �I�ltokens */
	private Sequence endSeq;
	
	/** ���[�v�̌v�Z�{�f�B */
	private Body body;
	
	/** ���v�E�ݏ�̏����l */
	protected BigDecimal ret = null;

	public Loop(String keyword, Sequence seq, int pos) {
		super(seq, pos);
		this.keyword = keyword;
		this._ctx = seq.getContext();
		
		if (SUM.equals(keyword)) {
			this.type = KeywordType.SUM;
		} else {
			this.type = KeywordType.MULT;
		}
		compile();
	}

	@Override
	public void compile() {
		// �L�[���[�h���͈͂��擾
		r = getKeywordRange(this.seq, keyword, this.searchRange);

		if (r == null) {
			throw new FmsRuntimeException("sum��mult�̂悤�ȃ��[�v���������݂��Ă��Ȃ�");
		}
		
		this.loopSeq = this.seq.subSequence(r);
		this.text = this.loopSeq.toString();

		// �㉺���͈̔͂��擾
		int closePos = Parentheses.posMatchCloseParenthese(loopSeq, 1);
		this.condRange = new Range(1, closePos);
		_getParaInfo(this.condRange);

		// ���[�v�v�Z�̃{�f�B
		body = new Body(loopSeq, closePos);
		
		setBlocks = body.seq.getAllSetBlocks();

		// �L�����`�F�b�N
		_validate();

	}

	/**
	 * �������̗L�����`�F�b�N <br>
	 * start��max�̊ԂɃR���}���K�������A�܂�start<max <br>
	 * body�̒��g��index���K�������
	 * 
	 * @throws FmsRuntimeException
	 */
	private void _validate() {
		// �R���}����̃`�F�b�N
		boolean flg = false;
		for (int i = this.condRange.start + 1; i < this.condRange.end; i++) {
			if (this.loopSeq.get(i).mark == 'C') {
				flg = true;
			}
		}
		if (!flg) {
			throw new FmsRuntimeException(this.keyword + "�ɂ��āA�n�l�ƏI�l�̊ԂɃR���}�����Ă��������B");
		}

		// ���[�v�J�E���gindex�̑��݃`�F�b�N
		if (!_containIndexToken(this.body.seq)) {
			throw new FmsRuntimeException(this.keyword + "���[�v�̃{�f�B{}�̂Ȃ��ɃJ�E���^�[index�����Ă��������B");
		}
	}

	/**
	 * ���[�v�J�E���gindex�̑��݃`�F�b�N
	 * 
	 * @param seq
	 * @return
	 */
	private boolean _containIndexToken(Sequence seq) {
		boolean flg = false;
		for (Token t : seq) {
			if (t.isVariable()) {
				if (t.toVariable().getName().equals(INDEX)) {
					return true;
				}
			} else if (t.isArray()) {
				flg = _containIndexToken(t.toArray().indexToken);
			} else {
				;
			}
		}

		return flg;
	}

	/**
	 * SUM�̃p�����[�^�͈͂��A�p�����[�^���i�n�l�A�I�l�j���擾
	 * 
	 * @param r
	 */
	private void _getParaInfo(Range r) {
		int i = r.start;
		/*
		 * <formula name="sumMaxVV" paras="j,max">
		 * sum(j,max){
		 * 	set{out=index}
		 * 	max{sumVij(index,k),sumVij(index,h+index-1)}
		 * }
		 * </formula>
		 * �����ɂ̓p�^�[���w�肵���ꍇ�A����j��max�̓p�����[�^�̒l�̂܂܎g�� ���Ɏw�肵�Ȃ��ꍇ�A���[�g�L�[��Վ��ϐ������Ȃ��ĒT��
		 */

		// ���ʃy�A�}�b�`�̃J�E���g
		int openParentheseCount = 0;

		while (i < r.end) {

			/*
			 * ���ɉ��L�̂悤�ȎZ���̏ꍇ�A�R���}�̈ʒu���Z�o����̂́A���ʃy�A�̃}�b�` �͍l�����Ȃ���΂Ȃ�Ȃ�
			 * 1/D[x+t]*sum(max(t,gg),omega-x){D[x+index]*(1+theta1*index)}
			 */

			Token t = loopSeq.get(i);
			if (t.mark == '(') {
				// �I�[�v�����ʂ̏ꍇ
				openParentheseCount++;
			} else if (t.mark == ')') {
				// �I�[�v�����ʂ̏ꍇ
				openParentheseCount--;
			}

			// ��ԊO�̃I�[�v�����ʂɃ}�b�`���邨��уR���}�̏ꍇ�A�㉺���̌v�Z�V�[�P���X�𕪂���
			if (t.mark == Token.COMMA && openParentheseCount == 1) {
				this.startSeq = loopSeq.subSequence(new Range(r.start + 1, i - 1));
				this.endSeq = loopSeq.subSequence(new Range(i + 1, r.end - 1));
				break;
			}
			i++;
		}
		
	}

	/**
	 * ���v�̒l���Z�o
	 * 
	 * @return
	 * @throws Exception 
	 */
	@Override
	public Token calculate() throws Exception {
		
		// �R���e�L�X�g���n�l�E�I�l���Z�o
		Sequence indexCopy = (Sequence) startSeq.clone();
		BigDecimal bIndex = indexCopy.eval();
		int index = bIndex.intValue();//�n�l
				
		Sequence maxCopy = (Sequence) endSeq.clone();
		int max = maxCopy.eval().intValue();//�I�l
		
		// start<=max�Ƃ��������̃`�F�b�N
		if (index > max) {
			//throw new FmsRuntimeException(MessageFormat.format(msg, this.text, index, max));
			if (logger.isInfoEnabled()) {
				String msg = "{0}�ɂ��āA�n�l{1}�͏I�l{2}�ɒ����Ă��܂������߁A�[����Ԃ��B";
				logger.warn(MessageFormat.format(msg, this.text, index, max));
			}
			this.resultToken = new Token(BigDecimal.ZERO, 'D', 0, 1); 
			return this.resultToken; 
		}

		if (logger.isDebugEnabled()) {
			logger.debug(this.text + "�ɂă��[�v�͈̔�:" + index + "�`" + max);
		}
		
		Map<String, Object> indexMap = new HashMap<String, Object>();
		
		// ���̌v�Z�ŗ��p����邽�߁A��ʂ̃p�����[�^MAP���R�s�[��������index���R���e�L�X�g�̃p�����[�^stack��push
		Map<String, Object> lastParas = _ctx.getLastParas();		
		if (lastParas != null) {
			indexMap.putAll(lastParas);
		}
		indexMap.put(INDEX, bIndex);
		_ctx.addFunctionPara(indexMap);

		if (SUM.equals(this.keyword)) {
			this.ret = BigDecimal.ZERO;
			// ���v���s��
			while (index <= max) {
				// �v�Z�p�{�f�B���R�s�[
				Body copy = (Body) body.clone();
				ret = ret.add(copy.value());
				index++;
				// ���̌v�Z���ɗ��p����邽�߁ASYS�ۗ��ϐ���index���X�V
				indexMap.put(INDEX, new BigDecimal(index));
			}			
		} else {
			this.ret = BigDecimal.ONE;			
			// �ݏ���s��
			while (index <= max) {
				// �v�Z�p�{�f�B���R�s�[
				Body copy = (Body) body.clone();
				ret = ret.multiply(copy.value());
				index++;
				// ���̌v�Z���ɗ��p����邽�߁ASYS�ۗ��ϐ���index���X�V
				indexMap.put(INDEX, new BigDecimal(index));
			}	
		}
		
		_ctx.clearCurrentFunctionPara();
		
		// �v�Z���ʂ�S�̂̌v�Ztokens�ɃZ�b�g����
		this.resultToken = new Token(ret, 'D', 0, 1);

		return this.resultToken;
	}

}