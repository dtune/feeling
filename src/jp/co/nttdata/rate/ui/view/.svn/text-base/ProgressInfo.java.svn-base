package jp.co.nttdata.rate.ui.view;

import java.io.File;
import java.util.Date;

/**
 * バッチ計算の場合、ファイル単位の進捗情報を持つ
 * @author zhanghy
 *
 */
public class ProgressInfo {

	private String fileFullName;
	private String filePath;
	private String fileName;
	private String percentage;
	private long totalCount = -1;
	private long NGCount = -1;
	private double expendTime = -1;
	private Date completeDate;
	private int index;
	private long errorDataCount = -1;

	public ProgressInfo(final String inputDataFilePath) {
		this.fileFullName = inputDataFilePath;
		this.filePath = inputDataFilePath.substring(0, inputDataFilePath
				.lastIndexOf(File.separator) + 1);
		this.fileName = inputDataFilePath.substring(inputDataFilePath
				.lastIndexOf(File.separator) + 1, inputDataFilePath.length());
	}

	public ProgressInfo() {
	}

	public String getFileFullName() {
		return fileFullName;
	}

	public void setFileFullName(String fileFullName) {
		this.fileFullName = fileFullName;
	}

	public String getFilePath() {
		return filePath;
	}

	public void setFilePath(String filePath) {
		this.filePath = filePath;
	}

	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	public String getPercentage() {
		return percentage;
	}

	public void setPercentage(String percentage) {
		this.percentage = percentage;
	}

	public long getTotalCount() {
		return totalCount;
	}

	public void setTotalCount(long totalCount) {
		this.totalCount = totalCount;
	}

	public long getNGCount() {
		return NGCount;
	}

	public void setNGCount(long nGCount) {
		NGCount = nGCount;
	}

	public double getExpendTime() {
		return expendTime;
	}

	public void setExpendTime(double expendTime) {
		this.expendTime = expendTime;
	}

	public Date getCompleteDate() {
		return completeDate;
	}

	public void setCompleteDate(Date completeDate) {
		this.completeDate = completeDate;
	}

	public int getIndex() {
		return index;
	}

	public void setIndex(int index) {
		this.index = index;
	}

	public long getErrorDataCount() {
		return errorDataCount;
	}

	public void setErrorDataCount(long errorDataCount) {
		this.errorDataCount = errorDataCount;
	}

	public String[] getProgressInfo(int i) {
		String[] progressInfo = new String[8];
		progressInfo[0] = ((Integer) i).toString();
		progressInfo[1] = this.fileName;
		progressInfo[2] = this.filePath;
		if (this.percentage == null) {
			progressInfo[3] = "0%";
		} else {
			progressInfo[3] = this.percentage;
		}

		if (this.totalCount == -1) {
			progressInfo[4] = "-";
		} else {
			progressInfo[4] = ((Long) this.totalCount).toString();
		}

		if (this.NGCount == -1) {
			progressInfo[5] = "-";
		} else {
			progressInfo[5] = ((Long) this.NGCount).toString();
		}

		if (this.expendTime == -1) {
			progressInfo[7] = "-";
		} else {
			progressInfo[7] = ((Double) this.expendTime).toString();
		}

		if (this.errorDataCount == -1) {
			progressInfo[6] = "-";
		} else {
			progressInfo[6] = ((Long) this.errorDataCount).toString();
		}

		return progressInfo;
	}

}
