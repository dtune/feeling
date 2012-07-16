package jp.co.nttdata.rate.fms.calculate;

import java.math.BigDecimal;
import jp.co.nttdata.rate.exception.ExpressionSyntaxException;
import jp.co.nttdata.rate.fms.core.Array;
import jp.co.nttdata.rate.fms.core.Operator;
import jp.co.nttdata.rate.fms.core.Parentheses;
import jp.co.nttdata.rate.fms.core.Sequence;
import jp.co.nttdata.rate.fms.core.Token;
import jp.co.nttdata.rate.fms.core.Variable;

/**
 * �񌳎��̌v�Z���s��
 * <br>�v�Z�ޕʂ͒萔�A�ϐ��Q�ł���B�ϐ��͎��̓��\�b�h�̃}�b�s���O�ł���B
 * <br>The following operators are supported:<br>
 * <ul>
 * <li>( or { open parentheses</li>
 * <li>) or } closed parentheses</li>
 * <li>+ addition (for numbers and strings)</li>
 * <li>- subtraction</li>
 * <li>* multiplication</li>
 * <li>/ division</li>
 * <li>% modulus</li>
 * <li>+ unary plus</li>
 * <li>- unary minus</li>
 * <li>= equal (for numbers and strings)</li>
 * <li>!= not equal (for numbers and strings)</li>
 * <li>< less than (for numbers and strings)</li>
 * <li><= less than or equal (for numbers and strings)</li>
 * <li>> greater than (for numbers and strings)</li>
 * <li>>= greater than or equal (for numbers and strings)</li>
 * <li>&& boolean and</li>
 * <li>|| boolean or</li>
 * <li>! boolean not</li>
 * </ul>
 * @author btchoukug
 *
 */
public class ResultEvaluator {

	//private static Logger logger = LogFactory.getInstance(ResultEvaluator.class);
	// TODO�@Double�^�C�v�̗L���͈͂͏����_�ȍ~17���܂ł̂���
	private static final int DEF_DIV_SCALE = 25;

	/**
	 * �I�y���[�^�ɉ����ē񌳎��̌v�Z���s��
	 * @param seq
	 * @param poz_max_op
	 * @return
	 * @throws Exception 
	 */
	public void evaluateByOperator(Sequence seq, int poz_max_op) throws Exception {

		//���쐔�P�ƂQ���擾
		Token operand1 = seq.get(poz_max_op - 1);
		Token operand2 = seq.get(poz_max_op + 1);
		// �I�y���[�^���擾
		Operator operator = (Operator)(seq.get(poz_max_op)).token;
		char[] ops = operator.getOps();
		
		double dOperand1Val = 0d;
		double dOperand2Val = 0d;
		BigDecimal operand1Val = null;		
		BigDecimal operand2Val = null;
		BigDecimal result = BigDecimal.ZERO;
		
		/*
		 * TODO operator optimize: �u*�v�A�u/�v�A�u&&�v�A�u||�v�̏ꍇ�A�v�Z�̉��P
		 */
		char op1 = ops[0];
		char op2 = 0;
		if (ops.length > 1) {
			op2 = ops[1];
		}
		
		// �u=�v�̏ꍇ�A���̑��쐔�̒l�͋��߂Ȃ�
		if (op1 == '=' && op2 != '=') {
			;
		} else {
			operand1Val = getOperandValue(operand1);
			dOperand1Val = operand1Val.doubleValue();
		}	

		if (dOperand1Val == 0 && ( op1 == '*' || op1 == '/' || (op1 == '&' && op2 == '&'))) {
			;
		} else if (dOperand1Val > 0 && op1 == '|' && op2 == '|') {
			result = BigDecimal.ONE;
		} else {
			operand2Val = getOperandValue(operand2);
			dOperand2Val = operand2Val.doubleValue();
			//�������[���̏ꍇ�A���ʂ��[���Ƃ��ĕԂ�
			if (op1 == '/' && dOperand2Val == 0d) {
				;
			} else {
				// ��L�ȊO�̏ꍇ�A�v�Z���s��
				result = _evaluate(operand1Val, operand2Val, dOperand1Val, dOperand2Val, op1, op2);				
			}

		}
		
		// �v�Z������A���̓񌳎����v�Z���ʂɏ�������
		seq.remove(poz_max_op + 1);
		seq.remove(poz_max_op);
		seq.set(poz_max_op - 1, new Token(result, 'D', poz_max_op - 1, 1));
		
		//�@�v�Z�������������̌��ʂ̊O���犇�ʂ��O��
		Parentheses.parenthesesRemoval(seq, poz_max_op - 1);
		
	}


	/**
	 * �v�Z�Ώۂ̒l���擾����
	 * <br>paras���w�肵�Ă��Ȃ��ꍇ�A�O���[�o���R���e�L�X�g����擾����
	 * <br>��L�ȊO�̏ꍇ�Aparas����擾����
	 * @param t
	 * @param paras
	 * @return
	 * @throws ErrorException
	 */
	public BigDecimal getOperandValue(Token t) throws Exception {
		BigDecimal ret = null;
			
		switch (t.mark) {
		case Token.VARIABLE:
			// TODO�@�\�藘���A��񗦁A�������͒���Context����擾
			
			//�ϐ��̏ꍇ
			Variable v = (Variable) t.token;
			ret = v.value();			
			break;
		case Token.DECIMAL:
			// token��ҏW�����Ƃ��ɁA�������ǂ����`�F�b�N�����ɍs���܂����̂ŁA������BigDecimal�ɕϊ�����
			ret = (BigDecimal) t.token;			
			break;
		case Token.ARRAY:
			//�z��̏ꍇ
			ret = ((Array)t.token).indexValue();
			break;			
		default:
			//��L�ȊO�̌^��token�̏ꍇ
			throw new ExpressionSyntaxException(t.toString() + "�͒萔��ϐ��ł͂���܂���B");
		}
		
		return ret;
	}

	/**
	 * �I�y���[�^���񌳎����v�Z����
	 * @param operand1
	 * @param operand2
	 * @param op
	 * @return
	 */
	private BigDecimal _evaluate(BigDecimal operand1, BigDecimal operand2, double dOperand1Val, double dOperand2Val, char op1, char op2) {
		
		BigDecimal result = BigDecimal.ZERO;
		/*
		 * 1.���\���シ�邽�߁A�I�y���[�^�̔�r��int���邢��char�ɕύX����
		 * ��F^�A*��char�ɂ���;==�A>=��int�ŃG���R�[�h����
		 * �����Ƃ��ẮAint�̔�r��String���X�ɑ��� 
	     * 2.�����͑S��BigDecimal�^�C�v�Ŋi�[����
	     */
		if (op2 == 0) {
			switch (op1) {
			case '+':
				result = operand1.add(operand2);
				break;
			case '-':
				result = operand1.subtract(operand2);
				break;
			case '*':
				result = operand1.multiply(operand2);
				break;
			case '/':
				result = operand1.divide(operand2, DEF_DIV_SCALE, BigDecimal.ROUND_HALF_UP);
				break;
			case '^':
				result = new BigDecimal(Double.toString(Math.pow(dOperand1Val, dOperand2Val)));
				break;
			case '%':
				//result = new BigDecimal(Double.toString(dOperand1Val % dOperand2Val));
				result = operand1.remainder(operand2);
				break;
			case '=':
				result = operand2;
				break;
			case '<':
				result = dOperand1Val < dOperand2Val ? BigDecimal.ONE : BigDecimal.ZERO;
				break;
			case '>':
				result = dOperand1Val > dOperand2Val ? BigDecimal.ONE : BigDecimal.ZERO;
				break;
			default:
				throw new ExpressionSyntaxException(String.valueOf(op1) + "���T�|�[�g�̃I�y���[�^�̑ΏۊO�̂��߁A�v�Z�ł��܂���ł����B");
			}
		} else {
			//2���̃I�y���[�^�̏ꍇ
			if (op2 == '=') {
				switch (op1) {
				case '=':
					result = dOperand1Val == dOperand2Val ? BigDecimal.ONE : BigDecimal.ZERO;
					break;
				case '<':
					result = dOperand1Val <= dOperand2Val ? BigDecimal.ONE : BigDecimal.ZERO;
					break;
				case '>':
					result = dOperand1Val >= dOperand2Val ? BigDecimal.ONE : BigDecimal.ZERO;
					break;
				case '!':
					result = dOperand1Val != dOperand2Val ? BigDecimal.ONE : BigDecimal.ZERO;
					break;
				default:
					throw new ExpressionSyntaxException(String.valueOf(new char[]{op1,op2}) + "���T�|�[�g�̃I�y���[�^�̑ΏۊO�̂��߁A�v�Z�ł��܂���ł����B");
				}
			} else if (op1 == '&' && op2 == '&') {
				result = (dOperand1Val * dOperand2Val > 0) ? BigDecimal.ONE : BigDecimal.ZERO;
			} else if (op1 == '|' && op2 == '|') {
				result = (dOperand1Val + dOperand2Val > 0) ? BigDecimal.ONE : BigDecimal.ZERO;
			} else {
				throw new ExpressionSyntaxException(String.valueOf(new char[]{op1,op2}) + "���T�|�[�g�̃I�y���[�^�̑ΏۊO�̂��߁A�v�Z�ł��܂���ł����B");
			}
		}

		
		//logger.trace("�I�y���[�^���v�Z�r���l�F" + operand1 + op + operand2 + "=" + result);
		
		return result;
	}
	
	
}
