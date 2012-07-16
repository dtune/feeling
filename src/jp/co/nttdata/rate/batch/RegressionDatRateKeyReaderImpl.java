package jp.co.nttdata.rate.batch;

import java.io.File;
import java.util.List;

import jp.co.nttdata.rate.batch.dataConvert.BatchDataLayoutFactory;
import jp.co.nttdata.rate.batch.dataConvert.RateKeyLayout;
import jp.co.nttdata.rate.exception.RateException;
import jp.co.nttdata.rate.model.CalculateCategory;

public class RegressionDatRateKeyReaderImpl extends DatRateKeyReaderImpl {

	public RegressionDatRateKeyReaderImpl(File file) throws RateException {
		super(file);
	}

	public RegressionDatRateKeyReaderImpl(String filePath) throws RateException {
		super(filePath);
		
	}

	public RegressionDatRateKeyReaderImpl(File curDataFile,
			BatchDataLayoutFactory factory) throws RateException {
		super(curDataFile, factory);
	}
	
	@Override
	public void loadDataLayout(String code, String cate) {
		super.loadDataLayout(code, cate);
		List<RateKeyLayout> list = this.dataLayout.getLayoutData();
		if (CalculateCategory.P.equals(cate)) {
			list.add(setDataLayout("insuranceCode",22,3,"商品コード"));
			list.add(setDataLayout("bonusCN",83,1,"bonusCN"));
			list.add(setDataLayout("specialCN",47,3,"specialCN"));
		}
		if (CalculateCategory.V.equals(cate)) {
			list.add(setDataLayout("dividend",151,1,"配当有無"));
			list.add(setDataLayout("insuranceCode",146,3,"商品コード"));
			list.add(setDataLayout("bonusCN",55,1,"bonusCN"));
			list.add(setDataLayout("specialCN",209,3,"specialCN"));
			list.add(setDataLayout("vrateCN",107,1,"vrateCN"));
		}
		if (CalculateCategory.W.equals(cate)) {
			list.add(setDataLayout("dividend",32,1,"配当有無"));
			list.add(setDataLayout("insuranceCode",27,3,"商品コード"));
			list.add(setDataLayout("bonusCN",94,1,"bonusCN"));
		}
		if (CalculateCategory.H.equals(cate)) {
			RateKeyLayout insuranceCode = setDataLayout("insuranceCode",275,3,"商品コード");
			list.add(insuranceCode);
		}
		if (CalculateCategory.E.equals(cate)) {
			RateKeyLayout insuranceCode = setDataLayout("insuranceCode",117,3,"商品コード");
			list.add(insuranceCode);
		}
		if (CalculateCategory.A.equals(cate)) {
			// 特約の場合、従契約から解析。
			RateKeyLayout insuranceCode;
			if (Integer.valueOf(code) > 200) {
				insuranceCode = setDataLayout("insuranceCode",265,3,"商品コード");
			} else {
				insuranceCode = setDataLayout("insuranceCode",153,3,"商品コード");
			}			
			list.add(insuranceCode);
		}
		this.dataLayout.setLayoutData(list);
	}

	public RateKeyLayout setDataLayout(String Name, int Pos, int Len, String Desc) {
		
		RateKeyLayout rateKeyLayout = new RateKeyLayout();
		rateKeyLayout.setName(Name);
		rateKeyLayout.setPos(Pos);
		rateKeyLayout.setLen(Len);
		rateKeyLayout.setDesc(Desc);
		return rateKeyLayout;
	}
}
