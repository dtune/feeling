package jp.co.nttdata.rate.fms.core;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;

import org.apache.commons.beanutils.ConvertUtils;
import org.apache.commons.lang.ArrayUtils;

import jp.co.nttdata.rate.util.CommonUtil;
import jp.co.nttdata.rate.util.Const;
import jp.co.nttdata.rate.exception.ExpressionSyntaxException;
import jp.co.nttdata.rate.fms.calculate.ICalculateContext;

/**
 * 
 * �Z��(�e�L�X�g)����ŏ��̌v�Z�P�ʂ܂ŉ�͂���<br>
 * �v�Z�V�[�P���X<span>Sequence</span>���쐬����
 * @author btchoukug
 *
 */
public class FormulaParser {
	
	/** �I�y���[�^�i�D�揇�ő�����j */
	public final static String OPS = "{}[]()^*/%+-<>=&|!,";
	
	/** �L�[���[�h */
	public final static String[] KEYWORDS = new String[]{"if","elseIf","else","set","while","sum","mult"};

	/**�f�t�H���g�v�Z�V�[�P���X�̃T�C�Y*/
	private static final int SEQ_CAPACITY = 30;

	/** �����̃{�f�B�Ɖ�͌�v�Z�����token�P�ʂ̃}�b�s���O */
	private Map<String, Sequence> sequencesPool = new HashMap<String, Sequence>();
	
	/** �J�����g�v�Z�p�̃R���e�L�X�g*/
	private ICalculateContext ctx;	
	private FunctionFactory factory;
		
	public FormulaParser() {
		_initSysEnv();
	}

	public FormulaParser(ICalculateContext ctx) {
		this.ctx = ctx;
		this.factory = new FunctionFactory();
	}
	
	//FIXME �O��XML����V�X�e���t�@���N�V�����ƕϐ������[�h����
	private void _initSysEnv() {
		;
	}
	
	/**
	 * �v�Z���̒��g���A�v�Z�V�[�P���X���擾
	 * @param formula
	 * @return
	 */
	public Sequence parse(String formulaBody) {
				
		if (this.ctx == null) {
			throw new IllegalArgumentException("�v�Z�R���e�L�X�g�͏���������Ȃ�����");
		}

		if (sequencesPool.containsKey(formulaBody)) {
			// �v�Z�ƂƂ��ɁASequence�͕ς�邩������Ȃ����߁A���̃R�s�[��Ԃ�
			Sequence seq = (Sequence) sequencesPool.get(formulaBody).clone();
			seq.setCompiled(true);
			return seq;
		}

		// �v�Z�P��Token���i�[����Sequence��������
		//�@�����Ɍv�Z�R���e�L�X�g�ƃt�@���N�V�����H����Z�b�g
		Sequence seq = new Sequence(ctx, formulaBody);
		seq.setFunctionFactory(factory);
		seq.ensureCapacity(SEQ_CAPACITY);

		// �@�I�y���[�^�ŕ�������
		StringTokenizer st = new StringTokenizer(formulaBody, OPS, true);
		while (st.hasMoreTokens()) {
			// �ŏ��P�ʂ܂ŕ������āAVector�ɒǉ�����
			String subFormula = st.nextToken();
			_editToken(seq, subFormula);
		}

		// �L�[���[�h�u���b�N��􂢏o���A�V�[�P���X�Ɋi�[����
		sequencesPool.put(formulaBody, seq.initKeywordBlock());

		return (Sequence) seq.clone();
	}
			
	/**
	 * �����ɏ]���āA�v�Z�P��token���ꂸ�ҏW����
	 * <p>token�̌^�Ƃ��āA����D�A�ϐ�V�A�z��A�A�L�[���[�hK�A����()�A�R���}C�A�I�y���[�^P</p>
	 * @param seq 
	 * @param ctx 
	 * @param formula 
	 */	 
	private void _editToken(Sequence seq, String mark) {
		
		/*
		 * TODO DEFFUNC��VAR�AARRAY�AKeyword�A���ʂ̃}�b�`�Ȃǂ̌v�Z�̃I�u�W�F�N�g�ɂ��āA
		 * �L�����`�F�b�N�͂����ł͂Ȃ��A���O�ɍs���悤�ɉ��P����
		 */
		
		int pos = 0;
		int len = 0;

		Token t = null;
		
		//�I�y���[�^�̔���
		if (mark.length() == 1) {
			switch (mark.charAt(0)) {
			case '(':
			case '{':
				t = new Token(mark,'(',pos,len);
				break;
			case ')':
			case '}':
				t = new Token(mark,')',pos,len);
				break;
			case '[':
				t = new Token(mark,'[',pos,len);
				break;
			case ']':
				/*
				 * M[i]�̌`�Ŕz��M���A�N�Z�X����悤�ɑΉ�����
				 * i�͕ϐ���萔�����Ƃ�OK
				 * �ϐ�i�͕��ʂ̌����ł�OK�̂悤�ɑΉ�����K�v
				 * ��Fqx[50+t-1]
				 */
				
				//M,[,i,]���P��token�ɍ��ق���
				int closePos = seq.size() - 1;
				int openPos = Parentheses.posArrayOpenParentheses(seq, closePos);
				if (openPos == -1) {
					throw new ExpressionSyntaxException("�z��̏������ɂ͊Ԉ���Ă��܂��B");
				}
				
				Token arrayToken = seq.get(openPos - 1);
				Sequence indexToken = seq.subSequence(new Range(openPos + 1, closePos));
				t = new Token(new Array(arrayToken, indexToken), 'A', pos, len);
				
				//����token���O��
				seq.remove(new Range(openPos - 1, closePos));
				break;
				
			case '+':
			case '-':
			case '*':
			case '/':
			case '%':
			case '^':
			case '>':
			case '<':
			case '=':
			case '!':
			case '&':
			case '|':
								
				/*
				 * �J�����g�̒��O��token���I�y���[�^�ł���΁A�P�I�y���[�^�ɍ��ق���
				 * �����Ƃ����΁AStringTokenizer�͒�����1�̕�����ŕ������邱�ƁB
				 * ��F>=,!=,<>,&&�Ȃ�
				 */
				
				//�܂��A���O��token���擾���āA�I�y���[�^���ǂ����`�F�b�N���s��
				Token lastToken = seq.get(seq.size()-1);				
				if (lastToken.isOperator()) {
					String lastOP = ((Operator)lastToken.token).operator;
					//�P�I�y���[�^�ɍ��ق���
					mark = lastOP + mark;
					//���O�̃I�y���[�^��tokens����O��
					seq.remove(lastToken);
				}

				//�I�y���[�^�̗D�揇���擾
				int prioity = _editPriority(mark);
				t = new Token(new Operator(mark, prioity),'P',pos,len);
				break;
				
			default:
				
				/* �I�y���[�^�ȊO�̋L���̏ꍇ */
				if (CommonUtil.isNumeric(mark)) {
					//�萔�̏ꍇ
					t = new Token(ConvertUtils.convert(mark, BigDecimal.class),'D',pos,len);
				} else if (mark.equals(",")) {
					//�R���}�̏ꍇ�i�t�@�N�V������p�j
					t = new Token(mark,'C',pos,len);
				} else {
					
					//�ϐ��̏ꍇ
					t = _procSysVariable(mark);					
					if (t == null) {
						Variable var = new Variable(mark, this.ctx);
						t = new Token(var,'V',pos,len);	
					}					
				}
			}
			
		} else {

			if (CommonUtil.isNumeric(mark)) {
				//�萔�̏ꍇ
				t = new Token(ConvertUtils.convert(mark, BigDecimal.class),'D',pos,len);
			} else {
				if (_isKeyWord(mark)) {
					//�v�Z�L�[���[�h�̏ꍇ
					t = new Token(mark,'K',pos,len);					
				} else {
					//�ϐ��̏ꍇ
					Variable var = new Variable(mark, this.ctx);
					t = new Token(var,'V',pos,len);
				}
			}
		}

		seq.add(t);
		
	}

	/**
	 * FIXME P,V,H�̓V�X�e���ϐ��Ƃ��Ď戵�A�g�����̂��߁ASysFunctionMapping.properties��`�\�ɂ���
	 * �O������ݒ�ɂ���
	 * FormulaParser�������̂Ƃ��ɁAMap�Ɋi�[����
	 * @param mark
	 * @return
	 */
	private Token _procSysVariable(String mark) {
		// PVH�Ȃǂ�SYSVAR�ƒ�`����i�O���z�u�j�A�����FormulaParser�Œ��ڐ����֒u������		
		BigDecimal ret = null;
		if (mark.charAt(0) == Const.P_BASE) {
			ret = new BigDecimal(0);
		} else if (mark.charAt(0) == Const.V_BASE) {
			ret = new BigDecimal(1);
		} else if (mark.charAt(0) == Const.H_BASE) {
			ret = new BigDecimal(2);
		} else {
			return null;
		}
		
		return new Token(ret, 'D'); 
	}

	/**
	 * ���f���̃L�[���[�h���ǂ����`�F�b�N���s��
	 * @param mark
	 * @return
	 */
	private boolean _isKeyWord(String mark) {	
		return ArrayUtils.contains(KEYWORDS, mark);
	}

	/**
	 * �D�揇��ҏW����i���������͗D�揇���Ⴂ�Ƃ���j
	 * @param op
	 * @return
	 */
	private int _editPriority(String op) {
		int ret = 0;
		
		//+-*/�Ȃǂ̃I�y���[�^�̏ꍇ
		if (op.length() == 1) {
			switch (op.charAt(0)) {
			case '+':
			case '-':
				ret = 1;
				break;
			case '*':
			case '/':
			case '%':
				ret = 2;
				break;
			case '^':
				ret = 3;
				break;
			case '>':
			case '<':
			case '=':
				ret = 0;
				break;
			}
		} else if (op.equals("&&") || op.equals("||")) {
			//&&��||�̗D�揇���Œ�Ƃ���
			ret = -1;
		} else {
			//>=,<=,==,!=�Ȃǃ��W�b�N�I�y���[�^�̏ꍇ
			ret = 0;
		}

		return ret;
	}
	
	
}
