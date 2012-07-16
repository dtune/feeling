package jp.co.nttdata.rate.rateFundation.dbConnection;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Map.Entry;

import jp.co.nttdata.rate.exception.FmsRuntimeException;
import jp.co.nttdata.rate.log.LogFactory;
import jp.co.nttdata.rate.util.Const;
import jp.co.nttdata.rate.util.Interpolation;
import jp.co.nttdata.rate.util.PropertiesUtil;
import org.apache.commons.lang.ClassUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

/**
 * DB接続またSQL文発行など処理を行う
 * （余裕あれば、DBの基本処理を改善）
 * @author   btchoukug
 */
public class DBConnection {
	
	private static Logger logger = LogFactory.getInstance(DBConnection.class);
	
	private static final String SINGEL_QUOTE = "'";

	private static final String DRIVER = "driver";

	private static final String DATABASE = "database";

	private static final String SERVER = "server";

	private static final String USER = "user";

	private static final String PWD = "password";

	private Properties prop;
	
	/**DBコネクタ*/
	private Connection conn = null;
	/**ステートメント*/
	private Statement stmt = null;	
	
	/**
	 * グローバルのDB接続インスタンス（Singleton）
	 */
	private static class DBConnHolder {
		private static DBConnection instance = new DBConnection();		
	}

	/**
	 * SingletonモードでDBコネクションのインスタンスを初期化する
	 * @return
	 */
	public static DBConnection getInstance(){
		return DBConnHolder.instance;
	}
	
	public DBConnection(){
		
		prop = PropertiesUtil.getExternalProperties(Const.DB_SETTING);
		String className = prop.getProperty(DRIVER);
		
		if (className == null) {
			throw new RuntimeException("データベース配置にはドライバ名称が設定されていません。");
		}
		
		try {
			ClassUtils.getClass(className).newInstance();
		} catch (InstantiationException e) {
			logger.error(e.getMessage(),e);
			throw new RuntimeException("DB接続ドライバーは初期化失敗しました。", e);
		} catch (IllegalAccessException e) {
			logger.error(e.getMessage(),e);
			throw new RuntimeException("DB接続ドライバーは初期化失敗しました。", e);
		} catch (ClassNotFoundException e) {
			logger.error(e.getMessage(),e);
			throw new RuntimeException("DB接続ドライバーは初期化失敗しました。", e);
		}
		
	}
	
	/** DBコネクタを開く */
	public void open(){
		
		try {
		
			//外部プロパティファイルからDB接続文字列を編集して接続
			String database = prop.getProperty(DATABASE, "");
			
			if (StringUtils.isEmpty(database)) {
				throw new RuntimeException("DB接続情報のプロパイティには設定が間違いました。");
			}
			
			String url = "jdbc:mysql://" + prop.getProperty(SERVER,"localhost") + "/" + database;
			String user = prop.getProperty(USER, "");
			String password = prop.getProperty(PWD, "");
			
			/*
			 * Here are the likely offenders from the Connection source: 
			 * this.netBufferLength = Integer.parseInt((String) this.serverVariables.get("net_buffer_length"));
			 * this.maxAllowedPacket = Integer.parseInt((String) this.serverVariables.get("max_allowed_packet"));
			 * The code is clearly not ready for the server to return "" for these variables.
			 * 
			 * that is why there is a NumberFormatException occurred
			 * 
			 */
			conn = DriverManager.getConnection(url, user, password);					

		} catch (SQLException e) {
			logger.error(e.getMessage(),e);
			throw new RuntimeException("DBコネクタを開くにはエラーが発生しました", e);
		}
	}

	
	/** DBコネクタをクローズ */
	public void close(){
		try {
			// ステートメントをクローズ
			stmt.close();
			//DBコネクションをクローズ
			conn.close();
		} catch (SQLException e) {
			logger.error(e.getMessage(),e);
			throw new RuntimeException("DBコネクタを開くにはエラーが発生しました", e);
		} finally {
			try {
				conn.close();
			} catch (SQLException e) {
				logger.error("DBコネクタをクローズエラー", e);
			}
		}
	}

	/** クエリーSQL文（パラメータなし）を執行する */
	public ResultSet select(String sql){
		
		ResultSet rs = null;
		
		try {
			//DBコネクタのチェックを行う
			if (conn == null || conn.isClosed()) {
				//throw new FmsRuntimeException("DB接続エラー");
				DBConnection.getInstance().open();
			}
			
			stmt = conn.createStatement();
			if (logger.isDebugEnabled()){
				logger.debug("執行SQL文:" + sql);
			}
			rs = stmt.executeQuery(sql);
		} catch (SQLException e) {			
			logger.error(e.getMessage(),e);
			throw new FmsRuntimeException("クエリーSQL文（パラメータなし）を執行するにはエラーが発生しました：" + sql, e);
		}
		
		return rs;
	}
	
	/** 更新SQL文を執行する */
	public boolean update(String sql){
		
		boolean rtn = false;
		
		try {
			//DBコネクタのチェックを行う
			if (conn == null || conn.isClosed()) {
				//throw new FmsRuntimeException("DB接続エラー");
				DBConnection.getInstance().open();
			}
			
			stmt = conn.createStatement();
			rtn = stmt.execute(sql);
		} catch (SQLException e) {
			e.printStackTrace();
			logger.error(e.getMessage());
		}
		
		return rtn;		
	}
	
	/**
	 * １つパラメータでSQLを執行する
	 * @param sql
	 * @param paraName
	 * @param paraVal
	 * @return
	 * @throws SQLException
	 */
	public List<DataRow> query(String sql,String paraName, Object paraVal) {
		
		Map<String,Object> paras = null;
		
		if (paraName == null || paraVal == null) {
			;
		} else {
			paras = new HashMap<String,Object>();
			paras.put(paraName, paraVal);
		}
				
		return query(sql,paras);
	}
	
	/**
	 * パラメータMAPとSQL文でクエリーを執行して、結果もMAPの形で返す
	 * @param sql
	 * @return
	 * @throws SQLException 
	 */
	public List<DataRow> query(String sql, Map<String, Object> paras) {
		
		if (StringUtils.isBlank(sql)) {
			throw new IllegalArgumentException("SQL文不正");
		}
		
		if (paras != null) {
			Map<String, Object> queryParas = new HashMap<String, Object>(paras);
			for (Iterator<Entry<String,Object>> it = queryParas.entrySet().iterator(); it.hasNext();) {
				Entry<String,Object> entry = it.next();
				
				/*
				 * 入れ替え時に、コラムのタイプより置換が必要
				 * 主に、Stringの場合、前後には''を追加要
				 */
				Object val = entry.getValue();
				if (val instanceof String) {
					val = SINGEL_QUOTE + val + SINGEL_QUOTE;
					//置換した文字列を再セット
					queryParas.put(entry.getKey(),val);
				} else if (val instanceof String[]) {
					StringBuffer sb = new StringBuffer();
					for (String s : (String[])val) {						
						sb.append(SINGEL_QUOTE).append(s).append(SINGEL_QUOTE).append(Const.COMMA);
					}
					queryParas.put(entry.getKey(), sb.deleteCharAt(sb.length() - 1).toString());
				} else {
					;
				}
			}			
			//SQL文にパラメータを入れ替え
			sql = Interpolation.interpolate(sql, queryParas);
		}
		
		List<DataRow> list = null;
		
		ResultSet rs = select(sql);
		
		try {
			
			list = new ArrayList<DataRow>();
			
			ResultSetMetaData rsmd = rs.getMetaData();
			int colNum = rsmd.getColumnCount();
			
			while (rs.next()) {
				DataRow data = new DataRow();
				for (int i = 1 ; i <= colNum; i++) {
					//asで別名を使うように、getColumnLabelに修正
					String colName = rsmd.getColumnLabel(i);
					data.put(colName, rs.getObject(i));
				}			
				list.add(data);
			}
			
			rs.close();
			
		} catch (SQLException e) {			
			logger.error(e.getMessage(),e);
			throw new RuntimeException("パラメータありのSQL文のクエリーにはエラーが発生しました", e);
		}
		
		return list;
	}


}
