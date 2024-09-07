package base;

import org.openqa.selenium.WebDriver;
import utilities.ExcelReader;

public class BaseTest {

	public WebDriver driver;

	//public static ExcelReader excel = new ExcelReader("./src/main/resources/excel/TestDataSheet.xlsx", 0);
	public static ExcelReader excelReader = new ExcelReader("./src/main/resources/excel/TestDataSheet.xlsx", 0);

}