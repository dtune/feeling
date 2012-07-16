package jp.co.nttdata.rate.rateFundation;

public class FundationDef {
	
	/**計算式に使われる基数名*/
	private String name;
	
	/**マッピングのDBコラム*/
	private String column;
	
	/**SQL文で当該基数を取得*/
	private String sql;
	
	private String desc;
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getColumn() {
		return column == null ? name : column;
	}
	public void setColumn(String column) {
		this.column = column;
	}
	public String getDesc() {
		return desc;
	}
	public void setDesc(String desc) {
		this.desc = desc;
	}
	public void setSql(String sql) {
		this.sql = sql;
	}
	public String getSql() {
		return sql;
	}	
	
	public String toString() {
		return this.desc == null ? this.name : this.desc;
	}
	
	
}
