package jp.co.nttdata.rate.fms.calculate;

import java.math.BigDecimal;

import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Element;
import net.sf.ehcache.statistics.LiveCacheStatistics;
import jp.co.nttdata.rate.exception.FmsRuntimeException;
import jp.co.nttdata.rate.util.Const;
import jp.co.nttdata.rate.util.ResourceLoader;
/**
 * キャッシュマネージメントを支援するモジュールである
 * @author btchoukug
 *
 */
public class CacheManagerSupport {
	
	//private static Logger logger = LogFactory.getInstance(CacheManagerSupport.class);
	
	/** DefFuncとVariableの計算結果を格納するキャッシュ */
	public static final String VAR_CACHE = "VAR_CACHE";
	
	/**VARとDEFFUNCを一時保存するキャッシュ*/
	private Cache varCache;

	static {		
		//singleton mode
		CacheManager.create(ResourceLoader.getExternalResource(Const.CACHE_SETTING));
	}
	
	public CacheManagerSupport(String code) {
		//this.code = code;		
		this.varCache = CacheManager.getInstance().getCache(VAR_CACHE);
		if (this.varCache == null) {
			throw new FmsRuntimeException("キャッシュ配置ehcache.xmlに" + VAR_CACHE + "が定義されていない。");
		}	
	}
	
	public void setCacheEnable(boolean cacheEnable) {
		this.varCache.setDisabled(!cacheEnable);
	}	
	
	/**
	 * 計算された公式をCacheに追加する
	 * 
	 * @param name
	 * @param value
	 */
	public void addToCache(String key, Object value) {		
		this.varCache.put(new Element(key, value));
	}
	
	/**
	 * キャッシュ対象に応じてキャッシュキーを取得
	 * @param obj
	 * @param ctx 
	 * @return
	 */	
	public BigDecimal getCacheVaule(String key) {
		Element e = this.varCache.get(key);
		return (e == null ? null : (BigDecimal) e.getObjectValue());
	}
	
	/**キャッシュをシャットダウン*/
	public void shutdownCache() {
		this.varCache.flush();
		CacheManager.getInstance().shutdown();
	}

	//TODO to be used in next time, cache to disk presistence
	public void cache2Disk() {

		this.varCache.flush();
		System.out.println("今はキャッシュした計算結果はロカールに保存する。");
		
		LiveCacheStatistics sts = this.varCache.getLiveCacheStatistics();
		System.out.println("Total Hit Count=" + sts.getCacheHitCount());
		System.out.println("Total Miss Count=" + sts.getCacheMissCount());
		System.out.println("Average GetTime(ms)=" + sts.getAverageGetTimeMillis());
		System.out.println("Current Cache Size=" + sts.getSize());

	}
	
}
