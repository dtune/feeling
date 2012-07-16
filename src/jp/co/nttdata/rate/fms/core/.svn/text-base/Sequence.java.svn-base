package jp.co.nttdata.rate.fms.core;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Stack;

import org.apache.log4j.Logger;

import jp.co.nttdata.rate.exception.FmsRuntimeException;
import jp.co.nttdata.rate.fms.calculate.ICalculateContext;
import jp.co.nttdata.rate.fms.calculate.ResultEvaluator;
import jp.co.nttdata.rate.fms.core.keyword.Judge;
import jp.co.nttdata.rate.fms.core.keyword.Keyword;
import jp.co.nttdata.rate.fms.core.keyword.Multiplies;
import jp.co.nttdata.rate.fms.core.keyword.Set;
import jp.co.nttdata.rate.fms.core.keyword.Sum;
import jp.co.nttdata.rate.fms.core.keyword.While;
import jp.co.nttdata.rate.fms.core.keyword.Keyword.KeywordType;
import jp.co.nttdata.rate.log.LogFactory;

/**
 * 計算tokenのシーケンスである
 * 普通公式や分岐式（if,elseIf,else）含む、およびwhileキーワードというループ、sum合計などの計算を行う
 * <br>また、setキーワードで一時変数の新規や変更（設定後他の公式には利用可能）
 * <p>現時点では、効率的にSequenceArrayListを使います</p>
 * @author btchoukug
 *
 */
public class Sequence extends ArrayList<Token> {

	private static final long serialVersionUID = 4431997813949899425L;
	
	private static Logger logger = LogFactory.getInstance(Sequence.class);
		
	/**当該計算シーケンスに当たる計算式のテキスト内容*/
	private String formulaBody;
	
	/**計算シーケンスには使える計算コンテキスト*/
	protected ICalculateContext _ctx;
	
	protected FunctionFactory _factory;
	
	protected ResultEvaluator _evaluator;
	
	/** 順番どおりに当たるキーワードを格納するリスト */
	private List<Keyword> keywordBlocks = new ArrayList<Keyword>();
	
	/** 階層に関わらず、下のすべてのSetのブロックを格納するリスト */
	private List<Set> setBlocks = new ArrayList<Set>();
	
	/**計算する際に、括弧とオペレータの位置を保持する配列（スキャン順番）*/
	private Stack<Integer> firstOpenPosStack = new Stack<Integer>();
	private Stack<Integer> firstClosePosStack = new Stack<Integer>();
	private Stack<Integer> opPosStack = new Stack<Integer>();
	
	private boolean isCompiled = false;
	
	public Sequence(ICalculateContext ctx, String formulaBody) {
		super();
		this.formulaBody = formulaBody;
		this._ctx = ctx;
		this._evaluator = ctx.getEvaluator();

	}

	public Sequence(List<Token> subList) {
		super(subList);
	}
	
	public void setFunctionFactory(FunctionFactory factory) {
		this._factory = factory;
	}
	
	public Sequence initKeywordBlock() {
		//0から順番でキーワードをスキャンする
		int i = 0;
		while(i < this.size()) {
			//キーワードの場合
			Token t = this.get(i);
			if (t.isKeyword()) {
				String keyword = (String) t.token;
				Keyword keywordProc = _getKeywordProcessor(keyword, i);
				//キーワードハンドラーの優先順どおりで計算を行う
				keywordBlocks.add(keywordProc);
				//次のスキャンの位置をキーワードの範囲の末尾にする
				i = keywordProc.getRange().end;
				
				//Setのブロックを編集
				if (keywordProc.getType() == KeywordType.SET) {
					//set自体の場合
					this.setBlocks.add((Set) keywordProc);
				} else {
					//judge判断、whileやsumループなどのボディのなかにsetを抽出
					this.setBlocks.addAll(keywordProc.getSetBlocks());
				}
			}				
			i++;
		}
		
		if (logger.isDebugEnabled()) {
			if (this.keywordBlocks.size() > 0) {
				logger.debug(this.toString() + ":" + this.keywordBlocks.toString());
			}				
		}
		
		
		return this;
	}

	/**
	 * キーワードハンドラーは指定どおりでロードする
	 * <br>処理順番あり
	 */
	private Keyword _getKeywordProcessor(String keyword, int pos) {
		
		/*
		 * FIXME キーワード処理のため、当該シーケンスを全部コピーする必要ないと思う
		 * new キーワードで当該シーケンスをそのままコピー操作を行ってしまう
		 */
		Keyword keywordProc = null;
		if (Keyword.SET.equals(keyword)) {
			keywordProc  = new Set(this, pos);
		} else if (Keyword.IF.equals(keyword)) {
			keywordProc = new Judge(this, pos);
		} else if (Keyword.WHILE.equals(keyword)) {
			keywordProc = new While(this, pos);
		} else if (Keyword.SUM.equals(keyword)) {
			keywordProc = new Sum(this, pos);
		} else if (Keyword.MULT.equals(keyword)) {
			keywordProc = new Multiplies(this, pos);
		} else {
			throw new FmsRuntimeException("サポートされないキーワードを使ってしまった：" + keyword + "@pos:" + pos);
		}
				
		return keywordProc;			
	}
	

	/**
	 * 指定の範囲でサブ計算Tokensを返す
	 * @param tokens
	 * @param r
	 * @return
	 */
	public Sequence subSequence(Range r) {
		Sequence sub = new Sequence(this.subList(r.start, r.end + 1));
		
		sub._ctx = this._ctx;
		sub.formulaBody = sub.toString();
		sub._evaluator = this._evaluator;
		sub._factory = this._factory;
		
		r = null;
		
		return sub;
	}	
	
	/**
	 * 指定範囲のtokensを外す
	 * @param tokens
	 * @param r
	 */
	public Sequence remove(Range r) {
		this.removeRange(r.start, r.end + 1);
		r = null;
		return this;
	}
	
	@Override
	public String toString() {
		if (this.formulaBody == null) {
			StringBuilder sb = new StringBuilder(500);
			for (Iterator<Token> it = this.iterator();it.hasNext();) {
				Token t = it.next();
				sb.append(t.toString());
			}
			this.formulaBody = sb.toString();			
		}		
		
		return this.formulaBody;
	}
	
	public BigDecimal eval() {

		BigDecimal ret = null;
		try {
			ret = _evaluator.getOperandValue(this._calculate());
		} catch (Exception e) {
			logger.error(this.toString() + "にはエラーが起こった：", e);
			//TODO 異常ではなく、代わりにEventの形でUIへエラー情報を引き渡す
			throw new FmsRuntimeException(e);
		}
		return ret;
	}
	
	public boolean getBooleanValue() {
		if (eval().intValue() > 0) return true;
		return false;
	}
	
	/**
	 *計算シーケンスから１つ計算単位まで計算を行う
	 * @throws Exception 
	 */
	private Token _calculate() throws Exception {
		
		//TODO 性能向上するため、無駄なループを減らす必要
				
		//１つtokenの場合、直接自身を返す
		if (this.size() == 1) return this.get(0);
					
		/*
		 * スキャンしたキーワード順番どおりに一ずつ計算を行う
		 * ただ、元のtokenを外す際に、各キーワードの範囲Rangeを影響しないように、最後から最初まで逆に行う
		 */
		for (Keyword keywordProc : keywordBlocks) {
			keywordProc.calculate();
		}
		for (int i = keywordBlocks.size() - 1; i >= 0; i--) {
			Keyword keywordProc = keywordBlocks.get(i);
			Range r = keywordProc.getRange();
			if (keywordProc.getType() == KeywordType.SET) {
				//setキーワードは最後にTokenが残さないため、次に進まない
				this.remove(r);
			} else {
				this.set(r.start, keywordProc.getResultToken());
				//元のif,elseIfやelseを公式tokensから外す
				this.remove(new Range(r.start + 1, r.end));				
			}
		}
			
		//すべて括弧の中のサブ公式を計算する
		_processWithParenthesis(this);
		
		//最後のtokenが計算結果となる
		if (this.size() == 1) {
			return this.get(0);
		} else {
			if (logger.isInfoEnabled()) {
				logger.warn("カレント計算には2つtokenで終わりました。最後の計算結果を返す：" + this.toString());
			}
			return this.get(this.size()-1);
		}
		
	}

	/**
	 * すべて括弧の中の計算単位tokenを+-*\/およびロジック計算を行うる
	 * 括弧優先の方針で計算シーケンスをスキャンに行く
	 * @param tokens
	 * @throws Exception 
	 */
	private void _processWithParenthesis(Sequence seq) throws Exception {
				
		//TODO 括弧がマッチされているかどうかはチェックしていない
		int index = 0; 
		int first_closed_pos = -1;
		int first_open_pos = -1;
		int poz_max_op = -1;
		
		while (true) {
			
			if (seq.size() == 1) return;
						
			// まず、公式の中身に一番優先の括弧を探して、計算範囲を取得

			if (!isCompiled) {
				first_closed_pos = Parentheses.posFirstClosedParenthesis(seq);
				first_open_pos = Parentheses.posOpenParenthesis(seq, first_closed_pos);
				this.firstOpenPosStack.push(first_open_pos);
				this.firstClosePosStack.push(first_closed_pos);
			} else {
				first_closed_pos = this.firstClosePosStack.get(index); 
				first_open_pos = this.firstOpenPosStack.get(index);;
			}	
			
			// 有効の計算が存在する場合、
			if (first_open_pos >= 0) {				

				Range r = new Range(first_open_pos, first_closed_pos);
				
				// 括弧とオペレータの位置（常に同じ）を一旦保存したうえ、二度以降の計算には直接に行う
				if (!isCompiled) {		
					//そして、この計算範囲に最優先のオペレータの位置を取得
					poz_max_op = Parentheses.posOperator(seq, r, 2);
					this.opPosStack.push(poz_max_op);	
				} else {					
					poz_max_op = this.opPosStack.get(index);
				}
								
				/*
				 * オペレータは１つでも存在していない場合
				 * if判断式の条件は全部計算したら、ボディが計算されない場合に該当する
				 * 例：if(cond1){exp1}elseIf(cond2){exp2}else{exp3}
				 * 括弧に対して計算はすべて括弧を外して、上記の条件とボディは１つtokenになるあめ、
				 * 外部の括弧は必要ないとなる
				 */
				
				if (poz_max_op == 0) {

					// オーペン括弧の直前のtokenが公式かどうかのチェック
					if (r.start >= 1) {
						Token t = seq.get(r.start - 1);
						if (t.isVariable()) {
							
							String funcName = t.toVariable().getName();							
							// SYSFUNC或いはDEFFUNCの場合
							_processFunctions(funcName, r, seq);				
						} else {
							// 先頭と末尾の括弧を外す
							seq.remove(r.end);
							seq.remove(r.start);
							//logger.debug("余計な括弧@" + r.start + "と" + r.end + "を外す。");						
						}
					} else {
						seq.remove(r.end);
						seq.remove(r.start);
						//logger.debug("余計な括弧@" + r.start + "と" + r.end + "を外す。");
					}
					
				} else {
					// オペレータに応じて、二元式を計算する
					_evaluator.evaluateByOperator(seq, poz_max_op);
				}
								
			} else {
				// 括弧は全部外した場合、単純な四則計算へ
				break;
			}
			
			index++;			
		}		
		
		//括弧を外したうえ、残りのtokenを+-*/計算を行う
		if (seq.size() == 1) return;
		_processNoParenthesis(seq, index);

	}
	
	/**
	 * ファンクション計算を行う
	 * @param funcName
	 * @param r
	 * @param seq
	 * @throws Exception 
	 */
	private void _processFunctions(String funcName, Range r, Sequence seq) throws Exception {
		
		// TODO パラメータ定義と一致しているかどうかチェック
		//BigDecimal ret = 0d;		
		
		Function func = this._factory.getInstance(funcName, seq, r);
		BigDecimal ret = func.result();			

		//計算完了したら、自身を計算tokensから外す
		seq.remove(r);
		seq.set(r.start - 1, new Token(ret, 'D', 0, 1));	
	}
	
	
	/**
	 * 括弧は全部外して計算完了の場合、「+-*\/」計算を行う
	 * @param seq
	 * @throws Exception 
	 */
	private void _processNoParenthesis(Sequence seq, int index) throws Exception {
		
		int poz_max_op = 0;
		int opIndex = index;
		
		while (true) {
			
			// 括弧とオペレータの位置（常に同じ）を一旦保存したうえ、二度以降の計算には直接に行う
			if (!isCompiled) {
				Range r = new Range(0, seq.size() - 1);
				poz_max_op = Parentheses.posOperator(seq, r, 0);
				this.opPosStack.push(poz_max_op);	
			} else {
				poz_max_op = this.opPosStack.get(opIndex);
			}
			
			if (poz_max_op > 0) {
				_evaluator.evaluateByOperator(seq, poz_max_op);				
			} else {
				break;
			}
			
			opIndex ++;
		}
		
		isCompiled = true;
	}

	
	@Override
	public Object clone() {
		Sequence seq = (Sequence) super.clone();
		if (this.formulaBody != null) {
			seq.formulaBody = new String(this.formulaBody);
		}
		seq._ctx = this._ctx;
		seq._evaluator = this._evaluator;
		seq._factory = this._factory;
		if (this.firstClosePosStack.size() > 0) {
			seq.isCompiled = true;	
		}		
		
		return seq;
	}
	
	public ICalculateContext getContext() {
		return this._ctx;
	}

	public void setCompiled(boolean isCompiled) {
		this.isCompiled = isCompiled;
	}

	public List<Set> getAllSetBlocks() {
		return this.setBlocks;
	}
	
}
	
