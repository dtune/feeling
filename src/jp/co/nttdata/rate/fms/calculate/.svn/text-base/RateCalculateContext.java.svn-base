package jp.co.nttdata.rate.fms.calculate;

import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

import jp.co.nttdata.rate.exception.FmsDefErrorException;
import jp.co.nttdata.rate.exception.FmsRuntimeException;
import jp.co.nttdata.rate.model.formula.Formula;
import jp.co.nttdata.rate.rateFundation.RateFundationGroup;
import jp.co.nttdata.rate.rateFundation.RateFundationManager;
import jp.co.nttdata.rate.rateFundation.dbConnection.DataRow;
import jp.co.nttdata.rate.util.CommonUtil;
import jp.co.nttdata.rate.util.Const;
import org.apache.commons.beanutils.ConvertUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
/**
 * 商品定義について、それぞれ計算式の値を求めるための計算コンテキストである
 * <br>普通のコンテキストと比べ、DBから計算基数も扱える
 * @author btchoukug
 *
 */
public class RateCalculateContext extends DefaultCalculateContext {

	/** 被保者年齢 */
	public static final String RATEKEY_AGE = "x";
	/** 被保者性別 */
	public static final String RATEKEY_SEX = "sex";
	/** 世代 */
	public static final String RATEKEY_GEN = "gen";
	/** 払方（回数） */
	public static final String RATEKEY_PAYMENT = "kaisu";
	/** 配当有無 */
	public static final String RATEKEY_DIVIDEND = "dividend";
	/** 一部一時払いを判明する項目 */
	public static final String RATEKEY_PARTONETIME = "partOnetime";
	/**経過年*/
	public static final String RATEKEY_T = "t";
	/**xtime*/
	public static final String RATEKEY_XTIME = "xtime";
	
	/** 基数値を格納するマップ（基数切り替えで初期化するが、計算中で中身変更不可）*/
	private Map<String, Double[]> fundationMap = new HashMap<String, Double[]>();

	/**計算途中の公式情報の配偶者に 関係あるかどうか（実は性別）で保持すること*/
	private Stack<Integer> mateStack = new Stack<Integer>();

	/**計算途中の基数グループキーを保持すること*/
	private Stack<String> fundGroupKeyStack = new Stack<String>();

	/** 基数は解約率に関わるか */
	private boolean isQwRelated = false;
	
	/** 特体の４倍体・１倍体或は標準体を表す */
	private Stack<Integer> xtimeStack = new Stack<Integer>();
	
	private RateFundationManager fundMgr;
	
	private CacheManagerSupport cache;
	
	private ContextPlugin ctxPlugin;
	
	/**配当ありの商品*/
	private String[] dividendCodes = {"004", "008", "009", "011", "013", "017", "042",
			"301", "302", "303", "304", "307", "308", "309", "931"};
	
	/**カレント商品コード*/
	protected String insuranceCode;
	private boolean isSpecial = false;
	private String contextBaseKey;
	
	public RateCalculateContext(String code) throws FmsDefErrorException{
		super();

		//デフォルトは標準体とする
		xtimeStack.push(Const.X_TIMES_DEFAULT);
		
		//渡したコードの前3桁は商品コード、それ以降は保障のフラグとなる
		this.insuranceCode = code.substring(0, 3);
		this.isSpecial = code.substring(3).indexOf("1") > -1 ? true : false;
		
		//キャッシュ初期化
		this.cache = new CacheManagerSupport(this.insuranceCode);
		
		//DBから基数をロード
		this.fundMgr = new RateFundationManager(this.insuranceCode);
		
		//標準体基数をロード
		this.fundMgr.loadRateFundation(false);
		
		if (isSpecial) {
			//特体基数をロード
			this.fundMgr.loadRateFundation(true);	
		}
		
		if ("043".equals(this.insuranceCode)) {
			ctxPlugin = new ContextPlugin043(this);
		} else {
			//配当金計算部分利率ロード
			if (ArrayUtils.contains(dividendCodes, this.insuranceCode)) {
				ctxPlugin = new DividendContextPlugin(this);
			} else {
				ctxPlugin = new DefaultContextPlugin(this);
			}			
		}
	}
	
	@Override
	public CacheManagerSupport getCache() {
		return this.cache;
	}
	
	@Override
	public void setCacheEnabled(boolean enableCache) {
		this.cache.setCacheEnable(enableCache);
	}
	
	/**
	 * 世代、ＰＶＨより解約率を算出
	 * @param gen
	 * @param pvh
	 * @return
	 * @throws Exception 
	 */
	@SuppressWarnings("unchecked")
	private double _getQwValue(int gen, String pvh) throws Exception {
		
		for (DataRow data : this.fundMgr.getQwConditonList()) {
			int qwGen = data.getInt(RateFundationGroup.GEN);
			String qwPVH = data.getString(RateFundationGroup.PVH);
			if ((gen == qwGen || qwGen == 0) && (StringUtils.lowerCase(pvh).equals(qwPVH) || qwPVH == null)) {
				
				String condtion = data.getString("condition");
				double qw = data.getDouble(RateFundationGroup.QW);
				
				/*
				 * 課題３３： Prate計算する時に、tが入力しないはずなので、解約率をコントロールするため、
				 * 一時払いの場合、t>mとし；分割払いの場合、t=0とするように修正します。
				 * qw_masterテーブルの方は、conditionコラムにkaisuよりの条件を外す
				 */	
				int kaisu = MapUtils.getIntValue(this.tokenValueMap, RATEKEY_PAYMENT);
				//Pレートを計算する場合、UIから経過年がないため
				boolean isNeedTempT = !this.tokenValueMap.containsKey(RATEKEY_T);
				if (isNeedTempT) {
					if (kaisu == 1) {
						//t>mのため、tを最大値とする
						this.tokenValueMap.put(RATEKEY_T, 999);
					} else {
						this.tokenValueMap.put(RATEKEY_T, 0);
					}
				}
				
				//入力レートキーより条件を計算する
				if (condtion != null && StringUtils.isNotEmpty(condtion)) {
					boolean cond = this.getParser().parse(condtion).getBooleanValue();
					if (cond) {
						//カレント条件に満たす場合、応じて解約率を返す
						if (logger.isDebugEnabled()) {
							logger.debug("入力レートキーに応じて解約率：" + qw);
						}
						
						if (isNeedTempT) {
							//計算終了の場合、tをコンテキスから外す
							this.tokenValueMap.remove(RATEKEY_T);
						}						
						
						return qw;
					}
				}
			}
			
		}
		
		//最後まで一つでも満たさない場合、0を返す（解約率込んでない）
		return 0d;		
	}
		
	/**
	 * 
	 * 前提として、すべて公式を定義する時に、
	 * 計算基礎PVHや配偶者、性別制限、年齢制限を指定していること。
	 * <br>※一番上のエントリー公式には必ず基礎を指定すること。 
	 * 公式を計算する場合、定義情報を取得して、基数を読み込む
	 * <p>
	 * 計算基礎の切り替えが公式を単位となっては 公式計算開始前、
	 * その公式に使われる基礎を一旦保存して；
	 * 計算終了後、使った基礎をポップして元の基礎に戻す
	 * </P>
	 * 
	 * @param formula
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public boolean shiftFundation(Formula formula) throws Exception {

		// デフォルト場合、基数切り替えはtrueとする
		boolean shiftable = true;
		isQwRelated = false;

		// PVH基礎を取得
		String pvh = formula.getPvh();

		// グループキー編集要否の判断
		boolean toEditGroupKey = StringUtils.isNotBlank(pvh);

		// グループキーの初期化
		String fundGroupKey = null;
		if (this.fundGroupKeyStack.isEmpty()) {
			// 初回の場合、stackがemptyのため、下記処理をスキップする
			if (!toEditGroupKey) {
				throw new FmsDefErrorException("エントリーの算式にはPVH基礎を指定しなければならない:"
						+ formula.getName());
			}
		}

		// 特体算式の場合、基礎は特に指定しないと、上位算式の基礎を流用する
		if (!toEditGroupKey && formula.getXtime() > Const.X_TIMES_DEFAULT) {
			String lastGroupKey = this.fundGroupKeyStack.peek();
			// 上位算式は特体の算式（xtime=4）の場合、PVHは＋の前となる
			// 例：11P+X4
			int pos = lastGroupKey.indexOf("+X4");
			pos = pos > 1 ? pos : lastGroupKey.length();
			pvh = String.valueOf(lastGroupKey.charAt(pos - 1));
			toEditGroupKey = true;
		}

		// 基礎を指定してる場合（特体も）、基数グループキーを編集
		if (toEditGroupKey) {

			// 計算基礎はP|V|Hの中に１つを指定している場合
			if (pvh.length() == 1 && StringUtils.containsOnly(pvh, "PVH")) {
				;
			} else {
				// 計算基礎は条件として定義される場合（if(gen>2){H}else{V}のような算式）
				Double dPVH = this._parser.parse(pvh).eval().doubleValue();
				pvh = _editPVH(dPVH);
			}

			int inputGen = MapUtils
					.getIntValue(this.tokenValueMap, RATEKEY_GEN);
			int kaisu = MapUtils.getIntValue(this.tokenValueMap,
					RATEKEY_PAYMENT);
			// 一部一時払フラグ,0 : 一部一時払を指定しない、 1 : 一部一時払
			int partOnetime = MapUtils.getIntValue(this.tokenValueMap,
					RATEKEY_PARTONETIME);

			Map valuesMap = new HashMap();
			valuesMap.put(RateFundationGroup.GEN, inputGen);
			// 回数を一時払いと分割払い２つパタンに置換
			valuesMap.put(RateFundationGroup.PAYMENT, kaisu > 1
					|| partOnetime > 0 ? 2 : 1);
			valuesMap.put(RateFundationGroup.PVH, pvh);

			// 配当有無に関る場合、配当有無を含めて基数グループキーを編集
			if (this.tokenValueMap.containsKey(RATEKEY_DIVIDEND)) {
				valuesMap.put(RateFundationGroup.DIVIDEND, MapUtils
						.getIntValue(this.tokenValueMap, RATEKEY_DIVIDEND));
			}

			/**
			 * 20110915　by　zhanghy 算式定義のxtimeより４倍体、１倍体或は標準体を区別しよう改善した
			 */
			int xtime = formula.getXtime();
			if (xtime > Const.X_TIMES_DEFAULT) {
				xtimeStack.push(xtime);
				this.tokenValueMap.put(RATEKEY_XTIME, xtimeStack.peek());
			}

			/**
			 * 20110402統計側の事業費枠から要望が以下どおり：
			 * 同じ算式に、4倍体と1倍体がありますが、1倍体は普通体（qwはゼロではない）として計算したいです 対策：
			 * Formula定義(xml)に対して、１つAttribute「倍数体xtime」を追加するうえで、基数切り替えのときに
			 * 算式定義情報を判断して、033&034,237&238商品に4倍体・1倍体両方は解約率込みの基数を使う；
			 * そのたは下記のルールを従う： 特条　かつ　4倍体の場合、解約率0の特体基数をロード；
			 * 特条　かつ　1倍体の場合、解約率0の標準体基数をロード； それ以外、普通として基数をロード。
			 */
			if (this.insuranceCode.equals("031")
					|| this.insuranceCode.equals("235")) {
				isQwRelated = true;
			} else {
				if (xtimeStack.peek() == Const.X_TIMES_DEFAULT) {
					// カレント算式も上位算式も特体ではなく　
					isQwRelated = true;
				}
			}

			// グループキールールに解約率を含める場合、解約率を算出
			if (StringUtils.contains(this.fundMgr.getCurrentGroupKeyDef(),
					RateFundationGroup.QW)) {

				Double qw = 0d;

				if (isQwRelated) {
					// 特定の商品コードより解約率を求める
					String curFundCode = formula.getFundCode();
					if (StringUtils.isBlank(curFundCode)) {
						// 特に指定していない（デフォールト）場合、カレント商品コードとする
						curFundCode = this.insuranceCode;
					} else {
						if (!StringUtils.isNumeric(curFundCode)) {
							throw new FmsDefErrorException(
									MessageFormat.format(
													"算式{0}に基数コード{1}は商品コード（数字）を設定されていません",
													formula.toString(),
													curFundCode));
						}
					}
					this.setValues.put(RateFundationGroup.SYS_FUND_CODE,
							ConvertUtils.convert(curFundCode, Double.class));
					// 特条や標準体どちらでも解約率にかかわると、DBのqwMaster設定どおりに解約率を求める
					qw = _getQwValue(inputGen, pvh);
				}

				valuesMap.put(RateFundationGroup.QW, qw);
			}

			// レートキーおよびPVH、解約率を入れ替え
			fundGroupKey = this.fundMgr.editFundationGroupKey(valuesMap);

		}

		// カレント公式に適用性別を取得し、デフォルトとしては入力した性別とする
		int fundSex = MapUtils.getIntValue(this.tokenValueMap, RATEKEY_SEX);

		// 公式にて性別を特に指定している場合、入力の性別より配偶者の性別を算出
		if (formula.isMate()) {
			// 男女Switch
			fundSex = (fundSex == 0) ? 1 : 0;
		} else {
			if (!this.mateStack.isEmpty()) {
				// 指定していない場合、前回の性別をそのまま使う
				fundSex = this.mateStack.peek();
			}
		}

		/*
		 * 公式的に性別制限あり場合、レートキー（sex）と公式の配偶者特定（mate） に関わらずすべて基数は指定性別の基数とする
		 */
		if (StringUtils.isNotEmpty(formula.getLimitedSex())) {
			fundSex = formula.getLimitedSex().equals(Const.MALE) ? 0 : 1;
			if (logger.isInfoEnabled())
				logger.info("[FUND]★これから基数は全部制限性別: " + _editSex(fundSex)
						+ "で扱う★");
		}

		/*
		 * 公式的に年齢制限あり場合、レートキーxに関わらず、 すべて基数はlimitedAgeに応じて値とする
		 * 20110403:現時点では、算式側で対応済みです、ここはx=40にするだけ
		 */
		int limitedAge = formula.getLimitedAge();
		if (limitedAge > 0) {
			// Arrayのindexを算出するため、一旦臨時変数として追加する
			this.addTempVariable(RATEKEY_AGE, limitedAge);
			if (logger.isInfoEnabled())
				logger.info("[FUND]★これから基数は全部制限年齢: " + limitedAge + "歳で取り込み★");
		}

		// 切り替え要否を判断
		String lastGroupKey = null;
		int lastFundSex = -1;
		if (!this.fundGroupKeyStack.isEmpty()) {
			lastGroupKey = this.fundGroupKeyStack.peek();
			if (!toEditGroupKey) {
				// 編集必要ない場合、前回のグループキーをそのまま使う
				fundGroupKey = lastGroupKey;
			}
			lastFundSex = this.mateStack.peek();
		}

		// 4倍体の場合、特条基数を適用する（PVHが指定した場合、必ずグループキーを編集）
		if (xtimeStack.peek() == Const.X_TIMES_4
				&& (toEditGroupKey || lastGroupKey
						.indexOf(RateFundationGroup.SUFFIX_FUND_SPECIAL) < 0)) {
			fundGroupKey += RateFundationGroup.SUFFIX_FUND_SPECIAL;
		}

		// 今回の基数は前回と同じであれば、切り替えない
		if (fundGroupKey.equals(lastGroupKey) && lastFundSex == fundSex) {
			shiftable = false;
		}

		// 上記で編集した性別とグループキーより基数をロード
		if (shiftable) {
			// 基数のグループキーと性別を保存
			this.fundGroupKeyStack.push(fundGroupKey);
			this.mateStack.push(fundSex);

			RateFundationGroup rfg = this.fundMgr
					.loadFundationGroup(fundGroupKey);
			_editFundMapBySex(rfg, fundSex);

		}

		return shiftable;
	}
	
	@SuppressWarnings("unchecked")
	private void _editFundMapBySex(RateFundationGroup rfg, int fundSex) {
		if (fundSex == Const.SEX_MALE_0) {
			this.fundationMap = rfg.getMaleFundMap();
		} else {
			this.fundationMap = rfg.getFemaleFundMap();
		}
		
		//予定利率、現価率、解約率など(配列ではない)
		this.tokenValueMap.put(RateFundationGroup.RATE, rfg.getInterest());
		this.tokenValueMap.put(RateFundationGroup.V, rfg.getV());
		this.tokenValueMap.put(RateFundationGroup.QW, rfg.getQw());
		
		//最終年齢ω取得
		int omega = rfg.getOmega(fundSex);
		this.tokenValueMap.put(RateFundationGroup.OMEGA, omega);
		if (logger.isInfoEnabled()) {
			logger.info("[FUND]最終年齢ω：" + omega);	
		}
	}
	
	/**
	 * 前回の計算式に使われた基数へ戻す
	 * 
	 * @param shiftable
	 * @param formula 
	 * @throws Exception 
	 */
	@SuppressWarnings("unchecked")
	public void rollbackFundation(boolean shiftable, Formula formula) throws Exception {
		
		//制限年齢が存在する場合、臨時変数から外す
		int limitedAge = formula.getLimitedAge();
		if (limitedAge > 0) {
			this.removeTempVariable(RATEKEY_AGE);
			if (logger.isInfoEnabled()) {
				logger.info("[FUND]★これから基数は全部制限年齢を外す★");						
			}				
		}
		
		if (formula.getXtime() > Const.X_TIMES_DEFAULT) {
			xtimeStack.pop();
			this.tokenValueMap.put(RATEKEY_XTIME, xtimeStack.peek());
		}
		
		// カレント公式の計算前に、一回基数と性別を元に戻す
		if (shiftable) {
			this.fundGroupKeyStack.pop();
			this.mateStack.pop();
			
			if (!this.fundGroupKeyStack.isEmpty()) {
				String lastGroupKey = this.fundGroupKeyStack.peek();
				int lastSex = this.mateStack.peek();
				//直前の基数を回復
				RateFundationGroup rfg = this.fundMgr.loadFundationGroup(lastGroupKey);
				_editFundMapBySex(rfg, lastSex);
			}
		}
	}
	
	private String _editPVH(double dBase) {
		String pvh = null;
		if (dBase == 0d) {
			pvh = "P";
		} else if (dBase == 1d) {
			pvh = "V";
		} else {
			pvh = "H";
		}
		return pvh;
	}

	private String _editSex(int fundSex) {
		return fundSex == 0 ? "男性" : "女性";
	}
	
	/**
	 * Cachekeyを作成するため、カレント計算に応じる制限年齢を返す
	 * @return
	 */
	public int getLimitedAge() {
		Object ret = this.getTempVariable(RATEKEY_AGE);
		if (ret == null) return 0;
		return (Integer)ret;
	}

	/**
	 * カレント計算にて、使う性別を返す
	 * @return
	 */
	public int getCurrentSex() {
		if (this.mateStack.isEmpty()) return -1;
		return this.mateStack.peek();
	}
	
	/**
	 * カレント計算にて、基数のグループキーを返す
	 * @return
	 */
	public String getCurrentFundationGroupKey() {
		if (this.fundGroupKeyStack.isEmpty()) return null;
		return this.fundGroupKeyStack.peek();
	}
	
	@Override
	public void reset() {
		super.reset();
		this.mateStack.clear();
		this.fundGroupKeyStack.clear();
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public void setInput(final Map input) {
		super.setInput(input);
		
		/*
		 * 一般的に計算結果はレートキーに関るため、入力のレートキーMap
		 * のhashcodeをベースキーとする
		 */
		this.contextBaseKey = String.valueOf(CommonUtil.FNVHash1(input.toString()));
		
		//それぞれ共通以外の処理を行う(主にレートキーの転換処理)
		ctxPlugin.contextHandle();
	}
	
	/**
	 * 基数名より配列の基数を返す
	 * <br>現価率vや死亡率qxなど配列ではないの項目は対象外とする
	 * @return 
	 */
	public Double[] getFundation(String fundName) {
		
		Object fundValue = this.fundationMap.get(fundName);
		if (fundValue == null) {
			throw new FmsRuntimeException("基数" + fundName + "が取得されない。");
		}
		
		return (Double[]) fundValue;
		
	}
	
	@Override
	public StringBuilder getContextKey(boolean isRatekeyRelated) {
				
		//キャッシュ対象に対しては、商品単位で基数グループキー＋性別（配偶者と制限性別に関係ある）＋制限年齢
		StringBuilder contextKey = new StringBuilder(this.insuranceCode);
		
		if (isRatekeyRelated) {
			contextKey.append(Const.DASH).append(this.contextBaseKey);
		}

		contextKey.append(Const.DASH)
				.append(this.getCurrentFundationGroupKey()).append(Const.DASH)
				.append(this.getCurrentSex());
		
		//臨時変数も算式の値に影響ありのため、キーにも追加
		if (!this.setValues.isEmpty()) {			
			contextKey.append(Const.DASH).append(this.setValues.toString());	
		}
		
		return contextKey;
	}
}