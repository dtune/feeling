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
 * 算式(テキスト)から最小の計算単位まで解析して<br>
 * 計算シーケンス<span>Sequence</span>を作成する
 * @author btchoukug
 *
 */
public class FormulaParser {
	
	/** オペレータ（優先順で揃える） */
	public final static String OPS = "{}[]()^*/%+-<>=&|!,";
	
	/** キーワード */
	public final static String[] KEYWORDS = new String[]{"if","elseIf","else","set","while","sum","mult"};

	/**デフォルト計算シーケンスのサイズ*/
	private static final int SEQ_CAPACITY = 30;

	/** 公式のボディと解析後計算されるtoken単位のマッピング */
	private Map<String, Sequence> sequencesPool = new HashMap<String, Sequence>();
	
	/** カレント計算用のコンテキスト*/
	private ICalculateContext ctx;	
	private FunctionFactory factory;
		
	public FormulaParser() {
		_initSysEnv();
	}

	public FormulaParser(ICalculateContext ctx) {
		this.ctx = ctx;
		this.factory = new FunctionFactory();
	}
	
	//FIXME 外部XMLからシステムファンクションと変数をロードする
	private void _initSysEnv() {
		;
	}
	
	/**
	 * 計算式の中身より、計算シーケンスを取得
	 * @param formula
	 * @return
	 */
	public Sequence parse(String formulaBody) {
				
		if (this.ctx == null) {
			throw new IllegalArgumentException("計算コンテキストは初期化されなかった");
		}

		if (sequencesPool.containsKey(formulaBody)) {
			// 計算とともに、Sequenceは変わるかもしれないため、元のコピーを返す
			Sequence seq = (Sequence) sequencesPool.get(formulaBody).clone();
			seq.setCompiled(true);
			return seq;
		}

		// 計算単位Tokenを格納するSequenceを初期化
		//　同時に計算コンテキストとファンクション工場もセット
		Sequence seq = new Sequence(ctx, formulaBody);
		seq.setFunctionFactory(factory);
		seq.ensureCapacity(SEQ_CAPACITY);

		// 　オペレータで分割する
		StringTokenizer st = new StringTokenizer(formulaBody, OPS, true);
		while (st.hasMoreTokens()) {
			// 最小単位まで分割して、Vectorに追加する
			String subFormula = st.nextToken();
			_editToken(seq, subFormula);
		}

		// キーワードブロックを洗い出し、シーケンスに格納する
		sequencesPool.put(formulaBody, seq.initKeywordBlock());

		return (Sequence) seq.clone();
	}
			
	/**
	 * 公式に従って、計算単位tokenを一ずつ編集する
	 * <p>tokenの型として、数字D、変数V、配列A、キーワードK、括弧()、コンマC、オペレータP</p>
	 * @param seq 
	 * @param ctx 
	 * @param formula 
	 */	 
	private void _editToken(Sequence seq, String mark) {
		
		/*
		 * TODO DEFFUNCやVAR、ARRAY、Keyword、括弧のマッチなどの計算のオブジェクトについて、
		 * 有効性チェックはここではなく、事前に行うように改善する
		 */
		
		int pos = 0;
		int len = 0;

		Token t = null;
		
		//オペレータの判明
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
				 * M[i]の形で配列Mをアクセスするように対応した
				 * iは変数や定数両方ともOK
				 * 変数iは普通の公式でもOKのように対応する必要
				 * 例：qx[50+t-1]
				 */
				
				//M,[,i,]を１つtokenに合弁する
				int closePos = seq.size() - 1;
				int openPos = Parentheses.posArrayOpenParentheses(seq, closePos);
				if (openPos == -1) {
					throw new ExpressionSyntaxException("配列の書き方には間違っています。");
				}
				
				Token arrayToken = seq.get(openPos - 1);
				Sequence indexToken = seq.subSequence(new Range(openPos + 1, closePos));
				t = new Token(new Array(arrayToken, indexToken), 'A', pos, len);
				
				//元のtokenを外す
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
				 * カレントの直前のtokenがオペレータであれば、１つオペレータに合弁する
				 * 原因といえば、StringTokenizerは長さが1の文字列で分割すること。
				 * 例：>=,!=,<>,&&など
				 */
				
				//まず、直前のtokenを取得して、オペレータかどうかチェックを行う
				Token lastToken = seq.get(seq.size()-1);				
				if (lastToken.isOperator()) {
					String lastOP = ((Operator)lastToken.token).operator;
					//１つオペレータに合弁する
					mark = lastOP + mark;
					//直前のオペレータはtokensから外し
					seq.remove(lastToken);
				}

				//オペレータの優先順を取得
				int prioity = _editPriority(mark);
				t = new Token(new Operator(mark, prioity),'P',pos,len);
				break;
				
			default:
				
				/* オペレータ以外の記号の場合 */
				if (CommonUtil.isNumeric(mark)) {
					//定数の場合
					t = new Token(ConvertUtils.convert(mark, BigDecimal.class),'D',pos,len);
				} else if (mark.equals(",")) {
					//コンマの場合（ファクション専用）
					t = new Token(mark,'C',pos,len);
				} else {
					
					//変数の場合
					t = _procSysVariable(mark);					
					if (t == null) {
						Variable var = new Variable(mark, this.ctx);
						t = new Token(var,'V',pos,len);	
					}					
				}
			}
			
		} else {

			if (CommonUtil.isNumeric(mark)) {
				//定数の場合
				t = new Token(ConvertUtils.convert(mark, BigDecimal.class),'D',pos,len);
			} else {
				if (_isKeyWord(mark)) {
					//計算キーワードの場合
					t = new Token(mark,'K',pos,len);					
				} else {
					//変数の場合
					Variable var = new Variable(mark, this.ctx);
					t = new Token(var,'V',pos,len);
				}
			}
		}

		seq.add(t);
		
	}

	/**
	 * FIXME P,V,Hはシステム変数として取扱、拡張性のため、SysFunctionMapping.properties定義可能にする
	 * 外部から設定可にする
	 * FormulaParser初期化のときに、Mapに格納する
	 * @param mark
	 * @return
	 */
	private Token _procSysVariable(String mark) {
		// PVHなどはSYSVARと定義する（外部配置）、これもFormulaParserで直接数字へ置換する		
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
	 * 判断式のキーワードかどうかチェックを行う
	 * @param mark
	 * @return
	 */
	private boolean _isKeyWord(String mark) {	
		return ArrayUtils.contains(KEYWORDS, mark);
	}

	/**
	 * 優先順を編集する（小さい方は優先順が低いとする）
	 * @param op
	 * @return
	 */
	private int _editPriority(String op) {
		int ret = 0;
		
		//+-*/などのオペレータの場合
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
			//&&と||の優先順が最低とする
			ret = -1;
		} else {
			//>=,<=,==,!=などロジックオペレータの場合
			ret = 0;
		}

		return ret;
	}
	
	
}
