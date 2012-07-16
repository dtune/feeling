package jp.co.nttdata.rate.batch.dataConvert;

/**
 * お客様からもらったデータのレイアウトを表現するモジュールである
 * <p>データレイアウトを基づいて、元のデータファイルを検証ツールに識別されるように転換する</p>
 * @author btchoukug
 *
 */
public class RateKeyLayout {
	
	private String name;
	private int pos;
	private int len;
	private String desc;
	private String transfer;
	
	public RateKeyLayout() {
		;
	}
	
	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer(30);
		sb.append(this.name)
		.append("@pos=").append(this.pos)
		.append(":")
		.append("len=").append(len);
		
		return sb.toString();
	}
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public int getPos() {
		return pos;
	}
	public void setPos(int pos) {
		this.pos = pos;
	}
	public int getLen() {
		return len;
	}
	public void setLen(int len) {
		this.len = len;
	}
	public String getDesc() {
		return desc;
	}
	public void setDesc(String desc) {
		this.desc = desc;
	}
	public String getTransfer() {
		return transfer;
	}
	public void setTransfer(String transfer) {
		this.transfer = transfer;
	}
}
