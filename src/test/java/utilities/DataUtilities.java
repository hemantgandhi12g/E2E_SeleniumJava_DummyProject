package utilities;

import java.lang.reflect.Method;
import org.testng.annotations.DataProvider;
import base.BaseTest;

public class DataUtilities extends BaseTest {

	/*
	 * @DataProvider(name = "datapro") public Object[][] getData(Method m) { String
	 * sheetName = m.getName(); int rowNum = excel.getRowCount(sheetName); int
	 * colNum = excel.getColumnCount(sheetName); Object[][] data = new Object[rowNum
	 * - 1][colNum];
	 * 
	 * for (int rows = 2; rows <= rowNum; rows++) { for (int cols = 0; cols <
	 * colNum; cols++) { data[rows - 2][cols] = excel.getCellData(sheetName, cols,
	 * rows); } } return data; }
	 */
	
	@DataProvider(name = "dataprocess")
	public Object[][] getDataFromExcel(Method m) {
		String sheetName = m.getName();
		int rowNum = excelReader.getRowCount(sheetName);
		int colNum = excelReader.getColumnCount(sheetName);
		Object[][] data = new Object[rowNum - 1][colNum];

		for (int rows = 2; rows <= rowNum; rows++) {
			for (int cols = 0; cols < colNum; cols++) {
				data[rows - 2][cols] = excelReader.getCellData(sheetName, cols, rows);
			}
		}
		return data;
	}
}