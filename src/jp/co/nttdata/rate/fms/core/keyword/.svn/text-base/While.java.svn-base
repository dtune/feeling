package jp.co.nttdata.rate.fms.core.keyword;

import java.math.BigDecimal;

import jp.co.nttdata.rate.exception.FmsRuntimeException;
import jp.co.nttdata.rate.fms.core.Operator;
import jp.co.nttdata.rate.fms.core.Range;
import jp.co.nttdata.rate.fms.core.Sequence;
import jp.co.nttdata.rate.fms.core.Token;
import jp.co.nttdata.rate.fms.core.Variable;

/**
 * キーワードwhileの計算を行う
 * @author btchoukug
 *
 */
public class While extends Keyword {

	//private static Logger logger = LogFactory.getInstance(While.class);
	private static final long serialVersionUID = 1L;

	/**カレントループ式の所属tokens*/
	private Sequence whileSeq;

	/**ループ計算変数名*/
	private String loopVarName;
	/**ループ計算変数値*/
	private BigDecimal loopVarValue;

	/**ループ条件*/
	private Condition condition;
	/**ループボディ*/
	private Body body;
	
	public While(Sequence seq, int pos) {
		super(seq, pos);
		this.keyword = WHILE;
		this.type = KeywordType.WHILE;
		//コンパイルする
		compile();		
	}
	
	@Override
	public void compile() {
		r = getKeywordRange(this.seq, WHILE, this.searchRange);
		if (r == null) {
			throw new FmsRuntimeException("キーワードwhileが存在していない");
		}
		whileSeq = this.seq.subSequence(r);
		
		//条件とボディを取得while(cond){body}
		condition = new Condition(whileSeq, 0);
		body = new Body(whileSeq, condition.r.end);
		
		setBlocks = body.seq.getAllSetBlocks(); 
		
		//ループの変数名を取得
		loopVarName = _getVariableName();

		
	}
	
	/**
	 * キーワードwhileの計算を行う
	 * <br>ボディの中にキーワードsetやwhileの定義が可能である
	 * @param seq
	 * @throws Exception 
	 */
	public Token calculate() throws Exception {
			
//		if (logger.isDebugEnabled()) {
//			logger.debug("☆=====ループ計算　開始=====☆");
//		}
		//計算する際に、tokensが消滅するため、ループ条件とボディのコピーより計算を行う
		Condition conditionCopy = (Condition) condition.clone();
		Body bodyCopy;
		
//		int count = 0;
		while (conditionCopy.isTrue()) {
			//ループ計算を行って、ループ条件が満たす場合、計算途中値を一時保存する
			bodyCopy = (Body) body.clone();
			loopVarValue = bodyCopy.value();

			this.seq.getContext().addTempVariable(loopVarName, loopVarValue);
			
			//FIXME ループ計算終了時、万が一変数名が重複するかもしれないため、カウントはコンテキストから外す
			conditionCopy = (Condition) condition.clone();
//			count++;
//			if (logger.isDebugEnabled()) {
//				logger.debug("☆=====ループ計算　" + count + "回目=====☆");
//			}
		}
		
		this.result = (BigDecimal) this.seq.getContext().getTempVariable(loopVarName);
		//setで定義された変数を取得してwhile計算結果として返す
		this.resultToken = new Token(this.result, 'D', r.start, 1);
				
//		if (logger.isDebugEnabled()) {
//			logger.debug("☆=====ループ計算　終了=====☆");
//		}
		
		return this.resultToken;
		
	}
	
	/**
	 * ボディから変数名を取得
	 * @param t
	 * @return
	 */
	private String _getVariableName() {
		//setキーワードのtokensを飛ばして、一番奥のwhileループ式の中に変数名を取得
		Token t = _posVariableFromBody(body.seq);
		if (t != null && t.isVariable()) {
			return ((Variable)t.token).getName();
		}		
		return null;
	}
	
	/**
	 * ボディから変数のTokenを返す
	 * @param seq
	 * @return
	 */
	private Token _posVariableFromBody(Sequence seq) {
		int i = 0;
		int end = seq.size();
		Token op;
		while (i < end) {
			op = seq.get(i);
			if (op.isKeyword()) {
				//キーワードの場合、キーワードの範囲を取得
				String keyword = (String)op.token;
				if (keyword.equals(Keyword.SET)) {
					//前回検索された部分を無視
					Range searchRange = new Range(i, end);
					//setの場合、飛ばす
					Range keywordRange =  getKeywordRange(seq, keyword, searchRange);
					//次の計算単位へ
					i = keywordRange.end;					
				} 

			} else {
				if (op.isVariable() && seq.get(i + 1).isOperator()
					&& ((Operator)seq.get(i + 1).token).operator.equals("=")) {
					//変数　かつ　直後が=の場合、当該tokenがループ変数とする
					return seq.get(i);
				}
			}
			i++;
		}
		return null;
	}

}

