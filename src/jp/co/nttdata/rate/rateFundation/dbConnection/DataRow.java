package jp.co.nttdata.rate.rateFundation.dbConnection;
import java.util.HashMap;
import org.apache.commons.beanutils.ConvertUtils;

public class DataRow extends HashMap<String, Object> {

	private static final long serialVersionUID = 7794399613700909153L;
	
	public DataRow() {
		super();
	}
	
	public DataRow(DataRow dataRow) {
		super(dataRow);
	}
	
	public String getString(String name) {
		Object val = get(name);
		if (val == null) return null;
		return (String) ConvertUtils.convert(val,String.class);
	}
	
	public int getInt(String name) {
		Object val = get(name);
		if (val == null) return 0;
		return (Integer) ConvertUtils.convert(get(name),Integer.class);
	}
	
	public double getDouble(String name) {
		
		Object val = get(name);
		if (val == null) return 0d;
		//return (Double) (val);
		return (Double) ConvertUtils.convert(get(name),Double.class);
	}
	
	public String[] getColumnNames() {
		return super.keySet().toArray(new String[]{});
	}
	
	public Object[] getColumnValues() {
		return super.values().toArray();
	}

}
