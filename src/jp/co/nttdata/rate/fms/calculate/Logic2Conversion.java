package jp.co.nttdata.rate.fms.calculate;



import jp.co.nttdata.rate.fms.core.FormulaParser;
import jp.co.nttdata.rate.fms.core.Parentheses;
import jp.co.nttdata.rate.fms.core.Range;
import jp.co.nttdata.rate.fms.core.Sequence;
import jp.co.nttdata.rate.fms.core.Token;
import jp.co.nttdata.rate.fms.core.keyword.Keyword;

public class Logic2Conversion {

	final FormulaParser parser = new FormulaParser();
	
	public String judge2Expression(String judgeText) {
		Sequence seq = parser.parse(judgeText);
		return _convert(seq);
	}
	
	/*
	 * if(periodKbn==2){n-x}elseIf(periodKbn==3){omega-n}else{n}
	 * if,else‚Q‚Â•ªŠò‚É•ª‚¯‚é‚æ‚¤‚É‰ğÍ
	 * 
	 */
	private String _convert(Sequence seq) {
		
		 StringBuffer sb = new StringBuffer();
		 
		 for (int i = 0, len = seq.size(); i < len; i++) {
			 Token t = seq.get(i);			 
			 if (t.isKeyword()) {
				 int begin = i + 1;
				 Sequence condSeq, expSeq;
				 
				 if (Keyword.IF.equals(t.token) || Keyword.ELSEIF.equals(t.token)) {
					 //ŠK‘w‚ª‘‚â‚·
					 int condEnd = Parentheses.posMatchCloseParenthese(seq, begin);
					 int expEnd = Parentheses.posMatchCloseParenthese(seq, condEnd + 1);;
					 //ğŒ‚Ìæ“¾
					 condSeq = seq.subSequence(new Range(begin, condEnd));
					 //true‚Ìê‡Expressionæ“¾
					 expSeq = seq.subSequence(new Range(condEnd + 1, expEnd));
					 
					 if (Keyword.ELSEIF.equals(t.token) || i > 0) {
						 sb.append("(");
					 }
					 
					 sb.append(_seq2Text(condSeq))
					 .append(" ? ")
					 .append(_seq2Text(expSeq))
					 .append(" : ")
					 .append(_convert(seq.subSequence(new Range(expEnd + 1, len - 1))));
					 
					 if (Keyword.ELSEIF.equals(t.token) || i > 0) {
						 sb.append(")");
					 }
					 					 
					 break;
					 
				 } else {
					 //else‚Ìê‡
					 Range r = new Range(begin, Parentheses.posMatchCloseParenthese(seq, begin));
					 sb.append(_seq2Text(seq.subSequence(r)));
					 break;
				 }
				 
			 } else {
				 sb.append(t.toString());
			 }
		}
		 
		return sb.toString();
	}
	
	private String _seq2Text(Sequence seq) {
		StringBuffer sb = new StringBuffer();
		for (int i = 1, len = seq.size(); i < len - 1; i++) {
			Token t = seq.get(i);
			sb.append(t.toString());
		}
		
		return sb.toString();
	}

	
	public static void main(String[] args) {
		
		Logic2Conversion conv = new Logic2Conversion();
		System.out.println(conv.judge2Expression("iCurrtute/100+if(paymentKbn==2){m-x}elseIf(paymentKbn==3){omega-m}elseIf(paymentKbn==3){omega-m}else{m}"));
		long n1 = System.nanoTime();
		System.out.println(conv.judge2Expression("(1/2)+if(paymentKbn==2){m-x}elseIf(paymentKbn==3){omega-m}else{m}"));
		
		System.out.println(conv.judge2Expression("t"));
		long n2 = System.nanoTime();
		System.out.println(n2-n1);
		
		
	}
}
