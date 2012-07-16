package jp.co.nttdata.rate.batch.dataConvert;

import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.beanutils.ConvertUtils;
import org.apache.commons.configuration.CompositeConfiguration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.ConfigurationFactory;
import org.apache.commons.lang.StringUtils;
import org.xidea.el.Expression;
import org.xidea.el.ExpressionFactory;
import jp.co.nttdata.rate.batch.dataConvert.RateKeyLayout;
import jp.co.nttdata.rate.exception.FmsDefErrorException;
import jp.co.nttdata.rate.fms.calculate.DefaultCalculateContext;
import jp.co.nttdata.rate.fms.core.FormulaParser;
import jp.co.nttdata.rate.fms.core.Parentheses;
import jp.co.nttdata.rate.fms.core.Range;
import jp.co.nttdata.rate.fms.core.Sequence;
import jp.co.nttdata.rate.fms.core.Token;
import jp.co.nttdata.rate.fms.core.keyword.Keyword;
import jp.co.nttdata.rate.util.CommonUtil;
import jp.co.nttdata.rate.util.Const;
import jp.co.nttdata.rate.util.ResourceLoader;

/**
 * お客様からデータを検証ツールに識別されるレートキーに置換する
 * 
 * @author btchoukug
 */
public class Csv2RateKeyConverterImpl implements IRateKeyConverter {

	/** ある場合、データの直前+を付けている */
	private static final char PLUS = '+';

	private static final int RADIX16 = 16;
	/** 満期給付金倍率基準値 */
	private static final Integer I80 = Integer.parseInt("80", RADIX16);

	/** BT計算に当たりの固定値 */
	public Map<String, Double> fixedValues;

	/** 比較元の項目 */
	private String[] compareObjNames;

	/** 定義算式どおりに計算するExpresssionFactory */
	private static ExpressionFactory expressionFactory;

	/** csvファイル読み込みのconfig */
	private CompositeConfiguration config;

	/** 動的にExpressionでレートキーの値を求める */
	final FormulaParser parser = new FormulaParser(new DefaultCalculateContext());
	/** 算式で転換必要なレートキー */
	private List<String> convertedRatekeyNames;
	/** 計算に必要なレートキー */
	private List<String> ratekeyNames;
	private Map<String, Expression> cachedExpressions = new HashMap<String, Expression>();

	@SuppressWarnings("unchecked")
	public Csv2RateKeyConverterImpl(String calcType) throws FmsDefErrorException {

		expressionFactory = ExpressionFactory.getInstance();

		try {
			ConfigurationFactory factory = new ConfigurationFactory();
			URL confUrl = ResourceLoader.getExternalResource(Const.BT_DATA_CSV_CONFIG);
			factory.setConfigurationURL(confUrl);
			config = new CompositeConfiguration(factory.getConfiguration());
		} catch (ConfigurationException e) {
			throw new FmsDefErrorException("CSVファイルを読むため、転換定義ファイルロード失敗でした", e);
		}

		// 変換が必要なキー名のリストを取得
		convertedRatekeyNames = config.getList(Const.BATCH + Const.DOT
				+ Const.RATEKEY + Const.DOT + Const.TRANSFER);

		if (convertedRatekeyNames != null) {

			for (int i = 0; i < convertedRatekeyNames.size(); i++) {
				String convertedKeyName = convertedRatekeyNames.get(i);
				String fomulaText = config.getString(Const.RATEKEY + Const.DOT
						+ convertedKeyName + Const.DOT + Const.TRANSFER,
						Const.EMPTY);

				if (StringUtils.isEmpty(fomulaText)) {
					continue;
				}
				Expression el = expressionFactory.create(CommonUtil.deleteWhitespace(_judge2Expression(fomulaText)));
				cachedExpressions.put(convertedKeyName, el);
			}

		}

		// 計算に必要なレートキーリストを取得
		ratekeyNames = config.getList(Const.BATCH + Const.DOT + Const.RATEKEY);

		if (ratekeyNames == null) {
			throw new FmsDefErrorException("CSVファイルを基に計算するため必要なレートキーを定義してください");
		}
	}

	public Csv2RateKeyConverterImpl() {

	}

	private String _judge2Expression(String judgeText) {
		Sequence seq = parser.parse(judgeText);
		return _convert(seq);
	}

	/*
	 * if(periodKbn==2){n-x}elseIf(periodKbn==3){omega-n}else{n}
	 * if,else２つ分岐に分けるように解析
	 */
	private String _convert(Sequence seq) {

		StringBuffer sb = new StringBuffer();

		for (int i = 0, len = seq.size(); i < len; i++) {
			Token t = seq.get(i);
			if (t.isKeyword()) {
				int begin = i + 1;
				Sequence condSeq, expSeq;

				if (Keyword.IF.equals(t.token)
						|| Keyword.ELSEIF.equals(t.token)) {
					// 階層が増やす
					int condEnd = Parentheses.posMatchCloseParenthese(seq,
							begin);
					int expEnd = Parentheses.posMatchCloseParenthese(seq,
							condEnd + 1);
					;
					// 条件の取得
					condSeq = seq.subSequence(new Range(begin, condEnd));
					// trueの場合Expression取得
					expSeq = seq.subSequence(new Range(condEnd + 1, expEnd));

					if (Keyword.ELSEIF.equals(t.token) || i > 0) {
						sb.append("(");
					}

					sb.append(_seq2Text(condSeq)).append(" ? ").append(
							_seq2Text(expSeq)).append(" : ").append(
							_convert(seq.subSequence(new Range(expEnd + 1,
									len - 1))));

					if (Keyword.ELSEIF.equals(t.token) || i > 0) {
						sb.append(")");
					}

					break;

				} else {
					// elseの場合
					Range r = new Range(begin, Parentheses
							.posMatchCloseParenthese(seq, begin));
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

	@Override
	public Map<String, Double> convert(Map<String, Double> oldKeyValues) {

		// String[] ratekeyNames = ratekeyDef.split(Const.COMMA);
		Map<String, Double> convertedKeyValues = new HashMap<String, Double>();

		if (oldKeyValues == null || oldKeyValues.size() == 0) {
			throw new IllegalArgumentException("変換元Mapは空白だった。");
		}

		for (int i = 0; i < ratekeyNames.size(); i++) {

			String ratekeyName = ratekeyNames.get(i);

			StringBuffer sbLabelKey = new StringBuffer(Const.RATEKEY).append(
					Const.DOT).append(ratekeyName).append(Const.DOT).append(
					Const.LABEL);

			String oldKeyName = config.getString(sbLabelKey.toString(),
					Const.EMPTY);

			if (StringUtils.isEmpty(oldKeyName)) {
				convertedKeyValues.put(ratekeyName, 0d);
				// throw new RuntimeException("レートキー「" + ratekeyName +
				// "」の変換定義はありません");
			}

			if (oldKeyValues.containsKey(oldKeyName)) {
				Double val = oldKeyValues.get(oldKeyName);
				if (val == null) {
					convertedKeyValues.put(ratekeyName, 0d);
				} else {
					convertedKeyValues.put(ratekeyName, val);
				}
			}

		}

        // レートキー名の変換してから、batch.ratekey.transferに書いてありの順番通りにレートキーのコード値を転換する
		for (String toConvertKeyName : convertedRatekeyNames) {
			Expression el = this.cachedExpressions.get(toConvertKeyName);
			Object ret = el.evaluate(convertedKeyValues);
			if (el == null){
				continue;
			}
			Double val = (Double) ConvertUtils.convert(ret, Double.class);
			convertedKeyValues.put(toConvertKeyName, val);
		}

		// UIから指定の固定値を上書きする
		if (this.fixedValues != null) {
			convertedKeyValues.putAll(this.fixedValues);
		}

		// 比較元の項目をそのままセット
		if (compareObjNames != null) {
			for (String compObj : compareObjNames) {
				if (!convertedKeyValues.containsKey(compObj)) {
					throw new IllegalArgumentException("指定の比較元の項目が存在していない");
				}
				convertedKeyValues.put(compObj, convertedKeyValues.get(compObj));
			}
		}
		// 変換したレートキーを返す
		return convertedKeyValues;
	}

	@Override
	public void setFixedValue(Map<String, Double> fixedValues) {
		this.fixedValues = fixedValues;
	}

	@Override
	public void setCompareObjectNames(String[] compareObjNames) {
		this.compareObjNames = compareObjNames;
	}

	@Override
	public double convertRateKey(RateKeyLayout layout, String value) {
		// まず、余計なスペースを削除する
		value = CommonUtil.deleteWhitespace(value);

		if (StringUtils.isEmpty(value)) {
			return 0d;
		}

		int ret = 0;

		// +/-符号を先頭に付いてる場合、+を外す
		if (value.charAt(0) == PLUS) {
			value = StringUtils.remove(value, PLUS);
		}

		// 普通数字の場合
		if (CommonUtil.isNumeric(value)) {
			ret = Integer.parseInt(value);
		}

		// 満期給付金倍率の場合、8A->90
		if (layout.getName().equals("I")) {
			ret = Integer.parseInt(value, 16) - I80;
		}

		return ret;
	}

}
