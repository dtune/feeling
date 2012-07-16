package jp.co.nttdata.rate.fms.core.keyword;

import java.math.BigDecimal;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;
import org.apache.log4j.Logger;

import jp.co.nttdata.rate.exception.FmsRuntimeException;
import jp.co.nttdata.rate.fms.calculate.ICalculateContext;
import jp.co.nttdata.rate.fms.core.Parentheses;
import jp.co.nttdata.rate.fms.core.Range;
import jp.co.nttdata.rate.fms.core.Sequence;
import jp.co.nttdata.rate.fms.core.Token;
import jp.co.nttdata.rate.log.LogFactory;

public abstract class Loop extends Keyword {

	private static final long serialVersionUID = 1L;

	private static Logger logger = LogFactory.getInstance(Loop.class);

	public static final String INDEX = "index";

	/** 連続計算のキーワード:sumまたはmult */
	protected String keyword;
	private String text;
	private ICalculateContext _ctx;
	
	private Sequence loopSeq;
	/** 合計の条件の範囲 */
	private Range condRange;

	/** 始値tokens */
	private Sequence startSeq;
	/** 終値tokens */
	private Sequence endSeq;
	
	/** ループの計算ボディ */
	private Body body;
	
	/** 合計・累乗の初期値 */
	protected BigDecimal ret = null;

	public Loop(String keyword, Sequence seq, int pos) {
		super(seq, pos);
		this.keyword = keyword;
		this._ctx = seq.getContext();
		
		if (SUM.equals(keyword)) {
			this.type = KeywordType.SUM;
		} else {
			this.type = KeywordType.MULT;
		}
		compile();
	}

	@Override
	public void compile() {
		// キーワードより範囲を取得
		r = getKeywordRange(this.seq, keyword, this.searchRange);

		if (r == null) {
			throw new FmsRuntimeException("sumやmultのようなループ処理が存在していない");
		}
		
		this.loopSeq = this.seq.subSequence(r);
		this.text = this.loopSeq.toString();

		// 上下限の範囲を取得
		int closePos = Parentheses.posMatchCloseParenthese(loopSeq, 1);
		this.condRange = new Range(1, closePos);
		_getParaInfo(this.condRange);

		// ループ計算のボディ
		body = new Body(loopSeq, closePos);
		
		setBlocks = body.seq.getAllSetBlocks();

		// 有効性チェック
		_validate();

	}

	/**
	 * 書き方の有効性チェック <br>
	 * startとmaxの間にコンマが必ず入れる、またstart<max <br>
	 * bodyの中身にindexが必ず入れる
	 * 
	 * @throws FmsRuntimeException
	 */
	private void _validate() {
		// コンマ入れのチェック
		boolean flg = false;
		for (int i = this.condRange.start + 1; i < this.condRange.end; i++) {
			if (this.loopSeq.get(i).mark == 'C') {
				flg = true;
			}
		}
		if (!flg) {
			throw new FmsRuntimeException(this.keyword + "について、始値と終値の間にコンマを入れてください。");
		}

		// ループカウントindexの存在チェック
		if (!_containIndexToken(this.body.seq)) {
			throw new FmsRuntimeException(this.keyword + "ループのボディ{}のなかにカウンターindexを入れてください。");
		}
	}

	/**
	 * ループカウントindexの存在チェック
	 * 
	 * @param seq
	 * @return
	 */
	private boolean _containIndexToken(Sequence seq) {
		boolean flg = false;
		for (Token t : seq) {
			if (t.isVariable()) {
				if (t.toVariable().getName().equals(INDEX)) {
					return true;
				}
			} else if (t.isArray()) {
				flg = _containIndexToken(t.toArray().indexToken);
			} else {
				;
			}
		}

		return flg;
	}

	/**
	 * SUMのパラメータ範囲より、パラメータ情報（始値、終値）を取得
	 * 
	 * @param r
	 */
	private void _getParaInfo(Range r) {
		int i = r.start;
		/*
		 * <formula name="sumMaxVV" paras="j,max">
		 * sum(j,max){
		 * 	set{out=index}
		 * 	max{sumVij(index,k),sumVij(index,h+index-1)}
		 * }
		 * </formula>
		 * 公式にはパターを指定した場合、そのjとmaxはパラメータの値のまま使う 特に指定しない場合、レートキーや臨時変数を見なして探す
		 */

		// 括弧ペアマッチのカウント
		int openParentheseCount = 0;

		while (i < r.end) {

			/*
			 * 特に下記のような算式の場合、コンマの位置を算出するのは、括弧ペアのマッチ は考慮しなければならない
			 * 1/D[x+t]*sum(max(t,gg),omega-x){D[x+index]*(1+theta1*index)}
			 */

			Token t = loopSeq.get(i);
			if (t.mark == '(') {
				// オープン括弧の場合
				openParentheseCount++;
			} else if (t.mark == ')') {
				// オープン括弧の場合
				openParentheseCount--;
			}

			// 一番外のオープン括弧にマッチするおよびコンマの場合、上下限の計算シーケンスを分ける
			if (t.mark == Token.COMMA && openParentheseCount == 1) {
				this.startSeq = loopSeq.subSequence(new Range(r.start + 1, i - 1));
				this.endSeq = loopSeq.subSequence(new Range(i + 1, r.end - 1));
				break;
			}
			i++;
		}
		
	}

	/**
	 * 合計の値を算出
	 * 
	 * @return
	 * @throws Exception 
	 */
	@Override
	public Token calculate() throws Exception {
		
		// コンテキストより始値・終値を算出
		Sequence indexCopy = (Sequence) startSeq.clone();
		BigDecimal bIndex = indexCopy.eval();
		int index = bIndex.intValue();//始値
				
		Sequence maxCopy = (Sequence) endSeq.clone();
		int max = maxCopy.eval().intValue();//終値
		
		// start<=maxという制限のチェック
		if (index > max) {
			//throw new FmsRuntimeException(MessageFormat.format(msg, this.text, index, max));
			if (logger.isInfoEnabled()) {
				String msg = "{0}について、始値{1}は終値{2}に超えてしまったため、ゼロを返す。";
				logger.warn(MessageFormat.format(msg, this.text, index, max));
			}
			this.resultToken = new Token(BigDecimal.ZERO, 'D', 0, 1); 
			return this.resultToken; 
		}

		if (logger.isDebugEnabled()) {
			logger.debug(this.text + "にてループの範囲:" + index + "～" + max);
		}
		
		Map<String, Object> indexMap = new HashMap<String, Object>();
		
		// 後ろの計算で利用されるため、上位のパラメータMAPをコピーしたうえindexをコンテキストのパラメータstackにpush
		Map<String, Object> lastParas = _ctx.getLastParas();		
		if (lastParas != null) {
			indexMap.putAll(lastParas);
		}
		indexMap.put(INDEX, bIndex);
		_ctx.addFunctionPara(indexMap);

		if (SUM.equals(this.keyword)) {
			this.ret = BigDecimal.ZERO;
			// 合計を行う
			while (index <= max) {
				// 計算用ボディをコピー
				Body copy = (Body) body.clone();
				ret = ret.add(copy.value());
				index++;
				// 他の計算式に利用されるため、SYS保留変数のindexを更新
				indexMap.put(INDEX, new BigDecimal(index));
			}			
		} else {
			this.ret = BigDecimal.ONE;			
			// 累乗を行う
			while (index <= max) {
				// 計算用ボディをコピー
				Body copy = (Body) body.clone();
				ret = ret.multiply(copy.value());
				index++;
				// 他の計算式に利用されるため、SYS保留変数のindexを更新
				indexMap.put(INDEX, new BigDecimal(index));
			}	
		}
		
		_ctx.clearCurrentFunctionPara();
		
		// 計算結果を全体の計算tokensにセットする
		this.resultToken = new Token(ret, 'D', 0, 1);

		return this.resultToken;
	}

}