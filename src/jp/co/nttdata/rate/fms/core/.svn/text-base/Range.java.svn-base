package jp.co.nttdata.rate.fms.core;

import java.text.MessageFormat;

/**
 * 公式を解析して計算する際に計算範囲である
 * <br>０ベースからカウント
 * @author btchoukug
 *
 */
public class Range {
	
	public int start;
	public int end;
	
	public Range(int start, int end) {
		
		if (start > end) {
			throw new IllegalArgumentException(MessageFormat.format("範囲の開始位置：{0}～終了位置：{1}が不正になった。", start, end));
		}
		
		this.start = start;
		this.end = end;
	}
	
	@Override
	public String toString() {
		return this.start + "~" + this.end;
	}
	
	public int getLength() {
		return end - start + 1;
	}
}
