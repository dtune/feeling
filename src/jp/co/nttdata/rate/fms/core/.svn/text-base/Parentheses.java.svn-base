package jp.co.nttdata.rate.fms.core;

import java.util.Stack;

/**
 * 括弧に関する処理を行う
 * @author btchoukug
 *
 */
public class Parentheses {
	
	private static boolean is_open_parenthesis(char c) {
		if (c == '(' || c == '[' || c == '{')
			return true;
		else
			return false;
	}

	private static boolean is_closed_parenthesis(char c) {
		if (c == ')' || c == ']' || c == '}')
			return true;
		else
			return false;
	}

	private static boolean parentheses_match(char open, char closed) {
		if (open == '(' && closed == ')')
			return true;
		else if (open == '[' && closed == ']')
			return true;
		else if (open == '{' && closed == '}')
			return true;
		else
			return false;
	}

	/**
	 * 括弧の完備性のチェックを行う
	 * @param exp
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static boolean parenthesesValid(String exp) {
		Stack s = new Stack();
		int i;
		char current_char;
		Character c;
		char c1;
		boolean ret = true;
		for (i = 0; i < exp.length(); i++) {
			current_char = exp.charAt(i);
			if (is_open_parenthesis(current_char)) {
				c = new Character(current_char);
				s.push(c);
			} else if (is_closed_parenthesis(current_char)) {
				if (s.isEmpty()) {
					ret = false;
					break;
				} else {
					c = (Character) s.pop();
					c1 = c.charValue();
					if (!parentheses_match(c1, current_char)) {
						ret = false;
						break;
					}
				}
			}
		}
		if (!s.isEmpty())
			ret = false;
		return ret;
	}
	
	/**
	 * 1番目のクローズ括弧の位置を取得
	 * @param tokens
	 * @return
	 */
	public static int posFirstClosedParenthesis(Sequence seq) {
		for (int i = 0; i < seq.size(); i++) {
			if (seq.get(i).mark == ')')
				return i;
		}
		return 0;
	}

	/**
	 * クローズ括弧に応じていオーペン括弧の位置を取得（一番奥の組）
	 * <br>括弧は見つかない場合、-1を返す
	 * @param tokens
	 * @param closed_parenthesis
	 * @return
	 */
	public static int posOpenParenthesis(Sequence seq, int closed_parenthesis) {
		int i = closed_parenthesis - 2;
		while (i >= 0) {
			if (seq.get(i).mark == '(') {
				return i;
			}
			i--;
		}
		return -1;
	}
	
	/**
	 * 配列のクローズ括弧よりオーペン括弧の位置を取得
	 * @param seq
	 * @param closePos
	 * @return
	 */
	public static int posArrayOpenParentheses(Sequence seq, int closePos) {
		for(int i = closePos; i>=0; i--) {
			if (seq.get(i).mark == '[') {
				return i;
			}
		}
		
		return -1;	
	}
	
	/**
	 * 指定のオーペン括弧より、指定公式からマッチされるクローズ括弧の位置を取得
	 * <br>見つかなかった場合、-1を返す
	 * @param openParenthesePos
	 * @param formula
	 * @return
	 */
	public static int posMatchCloseParenthese(Sequence seq, int openParenthesePos) {
		
		if (seq.get(openParenthesePos).mark != '(') {
			//openParenthesePosに応じてtokenはオーペン括弧ではない場合、エラーとする
			throw new IllegalArgumentException(openParenthesePos +  " 指定位置のtokenはオーペン括弧ではない：" + seq.toString());
		}		
		
		int matchCounter = 0;
		for (int i = openParenthesePos; i < seq.size(); i++) {
			switch (seq.get(i).mark){
			case '(':
				matchCounter ++;
				break;
			case ')':
				matchCounter --;
				break;
			default:
				;
			}
			
			//マッチされた括弧が見つかった、カレント位置を返す
			if (matchCounter == 0) {
				return i;
			}
		}
		//応じてクローズ括弧がないと、-1を返す
		return -1;
	}

	/**
	 * 指定範囲内のTokenの中に、最優先のオペレータの位置を取得
	 * @param seq
	 * @param offset 括弧ありの場合、2を指定；括弧ないの場合、0を指定。
	 * @param r
	 * @return
	 */
	public static int posOperator(Sequence seq, Range r, int offset) {
		int max_priority = Integer.MIN_VALUE;
		int max_pos = 0;
		int priority;
		String operator;
		Token t;
				
		//先頭のオペレータから末尾のオペレータまで
		for (int i = r.start + offset; i <= r.end - offset; i++) {
			t = (Token) seq.get(i);
			//次のオペレータを探して
			if (!t.isOperator()) {
				continue;
			}
			
			Operator op = (Operator) t.token;
			priority = op.priority;
			operator = op.operator;
			//オペレータの優先順を判断して、最優先のオペレータのレベルと位置をセット
			if (priority > max_priority || operator.equals("^")
					&& priority == max_priority) {
				max_priority = priority;
				max_pos = i;
			}
		}
		return max_pos;
	}
	
	/**
	 * 指定範囲から1番目のコンマの位置を返す
	 * @param seq
	 * @param r
	 * @return
	 */
	public static int posComma(Sequence seq, Range r) {
		int i;
		Token t;
		for (i = r.start; i <= r.end; i++) {
			t = (Token) seq.get(i);
			//コンマのmarkがCとなるため
			if (t.mark == 'C') {
				return i;
			}
		}
		return 0;		
	}
	
	/**
	 * 括弧の中に二元式を計算したら、外部の括弧を外す
	 * <br>if(exp){body}else{body}という判断式について、bodyが簡単な定数の場合、外部の括弧を外す
	 * @param seq
	 * @param pos
	 */
	public static void parenthesesRemoval(Sequence seq, int pos) {

		/*
		 * posが1の場合、該当tokenは先頭のことろである
		 * pos>1の場合、該当tokenは後ろにある。
		 * 括弧は公式ではないの場合、括弧を外す。
		 */
		if (pos > 1
				&& (seq.get(pos - 2)).mark != 'V'
				&& (seq.get(pos - 1)).mark == '('
				&& (seq.get(pos + 1)).mark == ')'
			|| pos == 1
				&& (seq.get(0)).mark == '('
				&& (seq.get(2)).mark == ')') {
			seq.remove(pos + 1);
			seq.remove(pos - 1);
		}
		
	}
	

}
