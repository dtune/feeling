package jp.co.nttdata.rate.fms.core.keyword;

import java.util.ArrayList;
import java.util.List;
import org.apache.log4j.Logger;

import jp.co.nttdata.rate.exception.FmsRuntimeException;
import jp.co.nttdata.rate.fms.core.Range;
import jp.co.nttdata.rate.fms.core.Sequence;
import jp.co.nttdata.rate.fms.core.Token;
import jp.co.nttdata.rate.log.LogFactory;

/**
 * 判断式の条件を判断して、ボディを計算するモジュール
 * <br>判断式は必ずifから、elseIfは繰り返し可、elseまでという前提である
 * @author btchoukug
 *
 */
public class Judge extends Keyword {

	private static final long serialVersionUID = 1L;

	private static Logger logger = LogFactory.getInstance(Judge.class);
	
	/**一つ判断式の所属tokens*/
	private Sequence judgeSeq;
	/**表す文字*/
	private String judgeText;
	
	private int pos = 0;
	
	private Range ifBlockRange;
	private Range elseBlockRange;

	/**カレントif,elseIf,elseキーワードの条件*/
	private List<Condition> conditionList = new ArrayList<Condition>();
	/**カレントif,elseIf,elseキーワードのボディ*/
	private List<Body> bodyList = new ArrayList<Body>();
	
	public Judge(Sequence seq, int pos) {
		super(seq, pos);
		this.keyword = IF;
		this.type = KeywordType.IF;
		//コンパイルする
		compile();
	}
	
	/**
	 * 初期化時に、計算に向けの構造を立ち上げ
	 */
	@Override
	public void compile() {
		
		_getCurrentJudgeRange();
		
		//if,elseIf,elseブロック一ずつを抽出する
		while (pos < elseBlockRange.end) {
		
			//ifまたはelseIfまたはelseサブ判断単位を取得
			Sequence subSeq = _nextBlock();

			Condition condition;
			Body body;
			if (this.type == KeywordType.IF || this.type == KeywordType.ELSEIF) {
				condition = new Condition(subSeq, 0);
				body = new Body(subSeq, condition.r.end);
			} else {
				condition = new Condition(String.valueOf(this.type), true);
				body = new Body(subSeq, 0);					
			}
			pos ++;
			conditionList.add(condition);
			bodyList.add(body);
			
			//if...else判断の場合、ボディごとにsetを抽出
			setBlocks.addAll(body.seq.getAllSetBlocks());
		}

	}
	
	/**
	 * 初期化の公式tokensの中に判断式が存在しているかどうかを判明する
	 * @return
	 */
	public void _getCurrentJudgeRange() {
		//判断式は必ずelseまで終わるため、elseよりelseIf（繰り返しも）の範囲を算出
		elseBlockRange = getKeywordRange(this.seq, ELSE, this.searchRange);
		if (elseBlockRange == null) {
			throw new FmsRuntimeException("ifからelseまでという形に書いてください");
		}
	
		//ifの範囲を取得
		ifBlockRange = getKeywordRange(this.seq, IF, null);
		//判断式全体の範囲を算出
		r = new Range(ifBlockRange.start, elseBlockRange.end);
		judgeSeq = this.seq.subSequence(r);
		judgeText = judgeSeq.toString();
	
	}
		
	/**
	 * カレント判断式から1番目のキーワード（条件）｛計算式｝３つ部分のtokensを返す
	 * <br>まずif、次はelseIf（optional）、最終はelse
	 * @return
	 */
	private Sequence _nextBlock() {
		Range subRange = null;
		if (pos == 0) {
			subRange = ifBlockRange;
		} else if (pos < elseBlockRange.start) {
			this.type = KeywordType.ELSEIF;
			subRange = getKeywordRange(this.seq, ELSEIF, new Range(pos, elseBlockRange.end));
		} else {
			this.type = KeywordType.ELSE;
			subRange = elseBlockRange;
		}
		
		pos = subRange.end;
		return this.seq.subSequence(subRange);
	}
		
	/**
	 * 判断式は必ずifから、elseIfは繰り返し可、elseまでというユニットを計算する
	 * @param seq
	 * @throws Exception 
	 */
	@Override
	public Token calculate() throws Exception {
		
		//条件if,elseIf,else一ずつで計算を行う		
		if (logger.isDebugEnabled()) {
			logger.debug("判断開始:" + this.judgeText);							
		}
		
		for (int i = 0; i < conditionList.size(); i++) {
			Condition cond = (Condition) conditionList.get(i).clone();
			//最後のelseのconditionがtrueになった
			if (cond.isTrue()) {
				this.result = ((Body)bodyList.get(i).clone()).value();
				break;
			}
		}

		//最終の計算結果を書き込み
		this.resultToken = new Token(this.result, 'D', r.start, 1);
		if (logger.isDebugEnabled()) {
			logger.debug("判断終了:" + this.result);							
		}
		
		return this.resultToken;
	
	}
	
	public String toString() {
		return this.judgeText;
	}
	
}
