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
 * ���q�l����f�[�^�����؃c�[���Ɏ��ʂ���郌�[�g�L�[�ɒu������
 * 
 * @author btchoukug
 */
public class Csv2RateKeyConverterImpl implements IRateKeyConverter {

	/** ����ꍇ�A�f�[�^�̒��O+��t���Ă��� */
	private static final char PLUS = '+';

	private static final int RADIX16 = 16;
	/** �������t���{����l */
	private static final Integer I80 = Integer.parseInt("80", RADIX16);

	/** BT�v�Z�ɓ�����̌Œ�l */
	public Map<String, Double> fixedValues;

	/** ��r���̍��� */
	private String[] compareObjNames;

	/** ��`�Z���ǂ���Ɍv�Z����ExpresssionFactory */
	private static ExpressionFactory expressionFactory;

	/** csv�t�@�C���ǂݍ��݂�config */
	private CompositeConfiguration config;

	/** ���I��Expression�Ń��[�g�L�[�̒l�����߂� */
	final FormulaParser parser = new FormulaParser(new DefaultCalculateContext());
	/** �Z���œ]���K�v�ȃ��[�g�L�[ */
	private List<String> convertedRatekeyNames;
	/** �v�Z�ɕK�v�ȃ��[�g�L�[ */
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
			throw new FmsDefErrorException("CSV�t�@�C����ǂނ��߁A�]����`�t�@�C�����[�h���s�ł���", e);
		}

		// �ϊ����K�v�ȃL�[���̃��X�g���擾
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

		// �v�Z�ɕK�v�ȃ��[�g�L�[���X�g���擾
		ratekeyNames = config.getList(Const.BATCH + Const.DOT + Const.RATEKEY);

		if (ratekeyNames == null) {
			throw new FmsDefErrorException("CSV�t�@�C������Ɍv�Z���邽�ߕK�v�ȃ��[�g�L�[���`���Ă�������");
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
	 * if,else�Q����ɕ�����悤�ɉ��
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
					// �K�w�����₷
					int condEnd = Parentheses.posMatchCloseParenthese(seq,
							begin);
					int expEnd = Parentheses.posMatchCloseParenthese(seq,
							condEnd + 1);
					;
					// �����̎擾
					condSeq = seq.subSequence(new Range(begin, condEnd));
					// true�̏ꍇExpression�擾
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
					// else�̏ꍇ
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
			throw new IllegalArgumentException("�ϊ���Map�͋󔒂������B");
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
				// throw new RuntimeException("���[�g�L�[�u" + ratekeyName +
				// "�v�̕ϊ���`�͂���܂���");
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

        // ���[�g�L�[���̕ϊ����Ă���Abatch.ratekey.transfer�ɏ����Ă���̏��Ԓʂ�Ƀ��[�g�L�[�̃R�[�h�l��]������
		for (String toConvertKeyName : convertedRatekeyNames) {
			Expression el = this.cachedExpressions.get(toConvertKeyName);
			Object ret = el.evaluate(convertedKeyValues);
			if (el == null){
				continue;
			}
			Double val = (Double) ConvertUtils.convert(ret, Double.class);
			convertedKeyValues.put(toConvertKeyName, val);
		}

		// UI����w��̌Œ�l���㏑������
		if (this.fixedValues != null) {
			convertedKeyValues.putAll(this.fixedValues);
		}

		// ��r���̍��ڂ����̂܂܃Z�b�g
		if (compareObjNames != null) {
			for (String compObj : compareObjNames) {
				if (!convertedKeyValues.containsKey(compObj)) {
					throw new IllegalArgumentException("�w��̔�r���̍��ڂ����݂��Ă��Ȃ�");
				}
				convertedKeyValues.put(compObj, convertedKeyValues.get(compObj));
			}
		}
		// �ϊ��������[�g�L�[��Ԃ�
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
		// �܂��A�]�v�ȃX�y�[�X���폜����
		value = CommonUtil.deleteWhitespace(value);

		if (StringUtils.isEmpty(value)) {
			return 0d;
		}

		int ret = 0;

		// +/-������擪�ɕt���Ă�ꍇ�A+���O��
		if (value.charAt(0) == PLUS) {
			value = StringUtils.remove(value, PLUS);
		}

		// ���ʐ����̏ꍇ
		if (CommonUtil.isNumeric(value)) {
			ret = Integer.parseInt(value);
		}

		// �������t���{���̏ꍇ�A8A->90
		if (layout.getName().equals("I")) {
			ret = Integer.parseInt(value, 16) - I80;
		}

		return ret;
	}

}
