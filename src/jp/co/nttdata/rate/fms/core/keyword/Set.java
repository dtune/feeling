package jp.co.nttdata.rate.fms.core.keyword;

import java.text.MessageFormat;
import java.util.List;

import jp.co.nttdata.rate.exception.FmsRuntimeException;
import jp.co.nttdata.rate.exception.ExpressionSyntaxException;
import jp.co.nttdata.rate.fms.core.Range;
import jp.co.nttdata.rate.fms.core.Sequence;
import jp.co.nttdata.rate.fms.core.Token;

/**
 * set{var=expression}に対して、計算結果をvarとして一時保存する
 * <br>RateCalculateContext.getComputeVariable(varの名)で計算結果を取得できる
 * <br>計算対象（主にbody）について、setキーワードは先頭から始まらなければならない
 * <br>setキーワードで作った臨時変数のスコープは当該算式に限る
 * @param tokens
 */
public class Set extends Keyword {
	
	//private static Logger logger = LogFactory.getInstance(Set.class);
	
	private static final long serialVersionUID = 1L;

	/**set臨時変数名*/
	private String var;
	
	/**カレントsetキーワードのボディtokens
	 * （setキーワードの後ろから最後の括弧までという範囲）
	 * */
	private Sequence body;
	
	public Set(Sequence seq, int pos){
		super(seq, pos);
		this.keyword = SET;
		this.type = KeywordType.SET;
		//コンパイルする
		compile();		
	}
	
	@Override
	public void compile() {
		//setの範囲を取得
		this.r = getKeywordRange(this.seq, SET, this.searchRange);
		if (this.r == null) {
			throw new ExpressionSyntaxException("setキーワードが存在していません");
		}
		
		//範囲よりボディを取得
		Range bodyRange = new Range(r.start + 1, r.end);
		this.body = this.seq.subSequence(bodyRange);
		this.body.initKeywordBlock();
		
		//setキーワードの変数名を取得
		_getVariableName();				

	}
	
	/**
	 * setの直後の{からそのあとマッチの}まで、定義の変数名と計算可のボディを取得する 
	 */
	private void _getVariableName() {
		//定義の変数名を取得
		Token t = this.body.get(1);
		if (t.isVariable()) {
			this.var = t.toVariable().getName();
		} else {
			throw new FmsRuntimeException(
					MessageFormat.format("{0}キーワードsetのボディの内に{1}は変数ではないマックを使ってしまう。", 
							this.body.toString(), t
							.toString()));
		}
	}
	
	@Override
	public Token calculate() throws Exception {						
		
		//ボディの計算結果をComputeContextに保存する
		Sequence bodyCopy = (Sequence) body.clone(); 
		this.result = bodyCopy.eval();
		this.seq.getContext().addTempVariable(var, this.result);
		
		return null;
	}
	
	/**
	 * setで作られた臨時変数の名称を返す
	 * @return
	 */
	public String getVariantName() {
		return this.var;
	}

	public List<Set> getSetBlocks() {
		return null;
	}

}
