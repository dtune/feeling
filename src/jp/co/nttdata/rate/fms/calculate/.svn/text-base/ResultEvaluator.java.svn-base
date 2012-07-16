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
 * 二元式の計算を行う
 * <br>計算類別は定数、変数２つである。変数は実はメソッドのマッピングである。
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
	// TODO　Doubleタイプの有効範囲は小数点以降17桁までのため
	private static final int DEF_DIV_SCALE = 25;

	/**
	 * オペレータに応じて二元式の計算を行う
	 * @param seq
	 * @param poz_max_op
	 * @return
	 * @throws Exception 
	 */
	public void evaluateByOperator(Sequence seq, int poz_max_op) throws Exception {

		//操作数１と２を取得
		Token operand1 = seq.get(poz_max_op - 1);
		Token operand2 = seq.get(poz_max_op + 1);
		// オペレータを取得
		Operator operator = (Operator)(seq.get(poz_max_op)).token;
		char[] ops = operator.getOps();
		
		double dOperand1Val = 0d;
		double dOperand2Val = 0d;
		BigDecimal operand1Val = null;		
		BigDecimal operand2Val = null;
		BigDecimal result = BigDecimal.ZERO;
		
		/*
		 * TODO operator optimize: 「*」、「/」、「&&」、「||」の場合、計算の改善
		 */
		char op1 = ops[0];
		char op2 = 0;
		if (ops.length > 1) {
			op2 = ops[1];
		}
		
		// 「=」の場合、左の操作数の値は求めない
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
			//除数がゼロの場合、結果もゼロとして返す
			if (op1 == '/' && dOperand2Val == 0d) {
				;
			} else {
				// 上記以外の場合、計算を行う
				result = _evaluate(operand1Val, operand2Val, dOperand1Val, dOperand2Val, op1, op2);				
			}

		}
		
		// 計算した後、元の二元式を計算結果に書き換え
		seq.remove(poz_max_op + 1);
		seq.remove(poz_max_op);
		seq.set(poz_max_op - 1, new Token(result, 'D', poz_max_op - 1, 1));
		
		//　計算した書き換えの結果の外から括弧を外す
		Parentheses.parenthesesRemoval(seq, poz_max_op - 1);
		
	}


	/**
	 * 計算対象の値を取得する
	 * <br>parasが指定していない場合、グローバルコンテキストから取得する
	 * <br>上記以外の場合、parasから取得する
	 * @param t
	 * @param paras
	 * @return
	 * @throws ErrorException
	 */
	public BigDecimal getOperandValue(Token t) throws Exception {
		BigDecimal ret = null;
			
		switch (t.mark) {
		case Token.VARIABLE:
			// TODO　予定利率、解約率、現価率は直接Contextから取得
			
			//変数の場合
			Variable v = (Variable) t.token;
			ret = v.value();			
			break;
		case Token.DECIMAL:
			// tokenを編集したときに、数字かどうかチェックを既に行いましたので、ここはBigDecimalに変換する
			ret = (BigDecimal) t.token;			
			break;
		case Token.ARRAY:
			//配列の場合
			ret = ((Array)t.token).indexValue();
			break;			
		default:
			//上記以外の型のtokenの場合
			throw new ExpressionSyntaxException(t.toString() + "は定数や変数ではありません。");
		}
		
		return ret;
	}

	/**
	 * オペレータより二元式を計算する
	 * @param operand1
	 * @param operand2
	 * @param op
	 * @return
	 */
	private BigDecimal _evaluate(BigDecimal operand1, BigDecimal operand2, double dOperand1Val, double dOperand2Val, char op1, char op2) {
		
		BigDecimal result = BigDecimal.ZERO;
		/*
		 * 1.性能向上するため、オペレータの比較はintあるいはcharに変更する
		 * 例：^、*はcharにする;==、>=はintでエンコードする
		 * 原因としては、intの比較はStringより更に速い 
	     * 2.数字は全部BigDecimalタイプで格納する
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
				throw new ExpressionSyntaxException(String.valueOf(op1) + "がサポートのオペレータの対象外のため、計算できませんでした。");
			}
		} else {
			//2桁のオペレータの場合
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
					throw new ExpressionSyntaxException(String.valueOf(new char[]{op1,op2}) + "がサポートのオペレータの対象外のため、計算できませんでした。");
				}
			} else if (op1 == '&' && op2 == '&') {
				result = (dOperand1Val * dOperand2Val > 0) ? BigDecimal.ONE : BigDecimal.ZERO;
			} else if (op1 == '|' && op2 == '|') {
				result = (dOperand1Val + dOperand2Val > 0) ? BigDecimal.ONE : BigDecimal.ZERO;
			} else {
				throw new ExpressionSyntaxException(String.valueOf(new char[]{op1,op2}) + "がサポートのオペレータの対象外のため、計算できませんでした。");
			}
		}

		
		//logger.trace("オペレータより計算途中値：" + operand1 + op + operand2 + "=" + result);
		
		return result;
	}
	
	
}
