package jp.co.nttdata.rate.fms.core.keyword;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import jp.co.nttdata.rate.fms.core.Parentheses;
import jp.co.nttdata.rate.fms.core.Range;
import jp.co.nttdata.rate.fms.core.Sequence;
import jp.co.nttdata.rate.fms.core.Token;

/**
 * キーワードの有効範囲を判明して計算する
 * @author btchoukug
 *
 */
public abstract class Keyword implements Comparable<Keyword> {

	public enum KeywordType {SET, WHILE, IF, ELSEIF, ELSE, SUM, MULT}
	
	private static final long serialVersionUID = 1L;
	
	/** setキーワード */
	public static final String SET = "set";
	/** whileキーワード */
	public static final String WHILE = "while";
	/** if,elseIf,elseキーワード */
	public static final String IF = "if";
	public static final String ELSEIF = "elseIf";
	public static final String ELSE = "else";
	/** sumキーワード */
	public static final String SUM = "sum";
	/** 求積のキーワード */
	public static final String MULT = "mult";
	
	/**処理対象の計算シーケンス*/
	protected Sequence seq;
	
	protected List<Set> setBlocks = new ArrayList<Set>();
	
	protected KeywordType type;
	
	/**キーワード名*/
	protected String keyword;
	
	/**キーワードの計算結果*/
	protected BigDecimal result = BigDecimal.ZERO;
	
	protected Token resultToken;
		
	/**キーワードの範囲*/
	protected Range r;
	
	protected Range searchRange;
	
	public Keyword(Sequence seq, int pos){
		this.seq = seq;
		this.searchRange = new Range(pos, seq.size());
	}
	
	
	/**
	 * 同じ優先順で指定のキーワードより計算tokensからマッチされた1番目の範囲を返す
	 * <br>キーワードから最後のマッチされたクローズ括弧まで、条件とボディを含む
	 * <br>Range rで検索範囲が指定可能とする
	 * @param seq
	 * @param keyword
	 * @param r
	 * @return
	 */
	protected Range getKeywordRange(Sequence seq, String keyword, Range search) {
		
		int searchStart = 0;
		int searchEnd = 0;
		
		//検索範囲を編集
		if (search != null) {
			searchStart = search.start;
			searchEnd = search.end;
		} else {
			//指定していない場合、終了位置はVectorの長さとする
			searchEnd = seq.size();
		}
		
		//キーワードの開始と終了位置
		int start = 0;
		int end = 0;
		
		int i = searchStart;
		while (i < searchEnd) {
			//カレントキーワードの開始位置
			start = i;
			Token t = seq.get(i);			
			//キーワードの場合
			if (t.isKeyword()) {
				String curKeyword = (String)t.token;
				//キーワードより、範囲を算出
				if (curKeyword.equals(Keyword.SET) || curKeyword.equals(Keyword.ELSE)) {
					end = Parentheses.posMatchCloseParenthese(seq, start + 1);;
				} else {
					//if,elseIf,whileの場合、範囲はif(condition){body}のような形
					int condEnd = Parentheses.posMatchCloseParenthese(seq, start + 1);;
					end = Parentheses.posMatchCloseParenthese(seq, condEnd + 1);;
				}
				
				//指定のキーワードの場合、範囲を返す
				if (keyword.equals(curKeyword) && end != -1) {
					return new Range(start, end);
				} else {
					//なければ、上記キーワードの範囲をステップとする
					i = end;					
				}				
				
			}
			
			//次のtokenへ
			i++;
		}
		
		return null;
	}
	
	/**事前各条件・ボディなど変わらない部分を抽出すること*/
	public abstract void compile();
	
	public abstract Token calculate() throws Exception;
	
	/**キーワードの優先順を返す（低いのはうは優先とする）*/
	protected int priority() {
		switch (this.type) {
		case SET:
			return 0;
		case IF:
			return 1;
		case WHILE:
			return 2;
		case SUM:
		case MULT:
			return 3;
		default:
			return 4;
		}
		
	}
	
	/**
	 * キーワードの優先順でソートする
	 */
	@Override
	public int compareTo(Keyword k) {
		return this.priority() - k.priority();
	}
	
	public String getName() {
		return this.keyword;
	}
	
	public KeywordType getType() {
		return this.type;
	}
	
	public Range getRange() {
		return this.r;
	}

	public Token getResultToken() {
		return this.resultToken;
	}

	public List<Set> getSetBlocks() {
		return setBlocks;
	}
	
}
