package jp.co.nttdata.rate.model.rateKey;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import jp.co.nttdata.rate.batch.ICallback;
import jp.co.nttdata.rate.exception.RateException;
import jp.co.nttdata.rate.model.rateKey.rule.RateKeyRule;

/**
 * バッチ計算の場合、ルールよりレートキーを生成して計算を行うジェネレータである
 * 
 * @author btchoukug
 */
public class RateKeyGenerator {

	/** 生成レートキーの上限 */
	private long maxNum = 99999999;
	/** 生成されたキーの数量 */
	private long generatedKeyNum = 0l;

	/** BT計算の場合、自動的に生成したキーの値を格納する */
	private Map<String, Integer> dataLayoutValues;

	/** レートキー定義のリスト */
	private List<RateKey> dataLayoutDefs;

	private int rateKeyNum = 0;

	private boolean isStop;

	private IRateKeyRelationship rateKeyRelation;

	/***
	 * 入力のRateInputのクラスおよびレートキーのバリデーションでレートキーのジェネレーターを初期化する
	 * 
	 * @param c
	 * @param insurance
	 * @param validator
	 * @throws RateException
	 */
	public RateKeyGenerator() {
		
		this.dataLayoutValues = new HashMap<String, Integer>();
		// TODO もしレートキーの間は何か制御関係が変わったら、下記のクラスを書き直せば
		this.rateKeyRelation = new DefaultRateKeyRelationship();
		
	}

	/***
	 * 繰り返しでレートキーを作成する
	 * 
	 * @param callback
	 */
	public void generateKeyValue(ICallback callback) {
		int i;
		int keyNum = this.dataLayoutDefs.size();

		Map<String, Integer> values = new HashMap<String, Integer>();

		// すべてのキーを最小値で初期化する
		for (i = 0; i < keyNum; i++) {
			// カレントレートキー名とルールを取得
			RateKey key = this.dataLayoutDefs.get(i);
			String keyName = key.getName();
//			RateKeyRule rule = RateKeyManager.getRateKeyRule(keyName);
			RateKeyRule rule = key.getRule();

			// デフォルト場合、0とする
			int value = 0;

			// レートキーの初期化
			if (rule != null) {
				// ルール指定している場合、最小値とする
				// value = rule.getMin();
				value = this.rateKeyRelation.getMinValue(keyName, rule, values);
			}
			values.put(keyName, value);
		}

		while (this.generatedKeyNum < this.maxNum && this.maxNum > 0) {

			// 生成中止かどうか判定
			if (this.isStop) return;

			//有効なレートキーの組み合わせであれば、datファイルに書き込み
			if (this.rateKeyRelation.validate(values)) {
				//System.out.println(values);
				callback.execute(values);
				generatedKeyNum++;				
			}

			// 上記に従って、最後のキーから逆に繰り返してレートキーの値を設定する
			for (i = keyNum - 1; i >= 0; i--) {
				// 最大値を編集
				RateKey key = this.dataLayoutDefs.get(i);
				String keyName = key.getName();
//				RateKeyRule rule = RateKeyManager.getRateKeyRule(keyName);
				RateKeyRule rule = key.getRule();
				
				int max = this.rateKeyRelation.getMaxValue(keyName, rule, values);
				int step = rule.getStep();
				
				//カレントキーにステップをプラス
				int value = values.get(keyName) + step;
				
				// カレントキーは最大値になると、最小値に戻す
				if (value > max) {
					values.put(keyName, this.rateKeyRelation.getMinValue(
							keyName, rule, values));
				} else {
					//特殊値の場合、ステッププラスで特殊値をスキップします
					while (RateKeyValidator.isSpecialValue(value, rule.getSpecialValues())) {
						value += step;
					}
					
					values.put(keyName, value);
					break;
				}
			}

			// 一番目のレートになると、whileループを中止する
			if (i < 0) break;

		}
	}

	/**
	 * 再帰でレートキーを生成する <br>
	 * 無駄なキーの組み合わせを生成しないように最終的に一括検証するではなく、 <br>
	 * 生成の間にキー制御チェックを行う
	 * 
	 * @param index
	 * @param callback
	 */
	public void generateKeyValue(int index, ICallback callback) {

		if (this.generatedKeyNum >= this.maxNum && this.maxNum > 0) {
			// 再帰でレートキーを生成を終了にする
			return;
		}

		// カレントレートキー名とルールを取得
		RateKey key = this.dataLayoutDefs.get(index);
		String keyName = key.getName();
		RateKeyRule rule = key.getRule();

		// デフォルト場合、0とする
		int value = 0;

		// レートキーの初期化
		if (rule != null) {
			// ルール指定している場合、最小値とする
			// value = rule.getMin();
			value = this.rateKeyRelation.getMinValue(keyName, rule,
					this.dataLayoutValues);
		}

		// 関連のレートキーの値より始値を編集
		int max = this.rateKeyRelation.getMaxValue(keyName, rule,
				this.dataLayoutValues);

		// 初期値を一旦Mapに保存する
		this.dataLayoutValues.put(keyName, value);

		// 最大値まで繰り返してレートキーの値を生成する
		if (rule != null) {
			while (value <= max) {
				// 特殊値チェック
				if (!RateKeyValidator.isSpecialValue(value, rule
						.getSpecialValues())) {
					// 無効値ではない場合、再帰で次のレートキーの値を生成する
					if (index < this.rateKeyNum - 1) {
						generateKeyValue(index + 1, callback);
					} else {
						// 最後の項目となると、業務上のバリデーションで検証して
						// OKなら、コールバックのメソッドを呼び出し
						callback.execute(dataLayoutValues);
						this.generatedKeyNum++;
					}
				}

				// ステップ値をプラスして、レートキーMapに保存する
				value = value + rule.getStep();
				this.dataLayoutValues.put(keyName, value);
			}
		} else {
			if (index < this.rateKeyNum - 1) {
				generateKeyValue(index + 1, callback);
			} else {
				// 最後の項目となると、業務上のバリデーションで検証して
				// OKなら、コールバックメソッドを呼び出し
				callback.execute(dataLayoutValues);
				this.generatedKeyNum++;
			}
		}

	}

	/**
	 * 参照元のレートキーの定義を設定する
	 * 
	 * @param rateKeyDefs
	 */
	public void setRateKeys(List<RateKey> rateKeyDefs) {
		this.dataLayoutDefs = rateKeyDefs;
		// ローマ字の昇順でソートしたRateKeyDefどおりにキーの値を生成する
		Collections.sort(this.dataLayoutDefs);
		this.rateKeyNum = rateKeyDefs.size();
	}

	/**
	 * 自動的に生成されたキーの数の上限を設定
	 * 
	 * @param maxNum
	 */
	public void setMaxNum(long maxNum) {
		this.maxNum = maxNum;
	}

	/** 自動的に生成されたキーの数を取得 */
	public long getGeneratedKeyNum() {
		return this.generatedKeyNum;
	}

	public void stop() {
		this.isStop = true;
	}
	
	public void start() {
		this.isStop = false;
	}

}
