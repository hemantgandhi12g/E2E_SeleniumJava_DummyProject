package base;

import java.awt.AWTException;
import java.awt.Robot;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;
import java.time.Duration;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.apache.poi.EncryptedDocumentException;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.openqa.selenium.Alert;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.Keys;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.edge.EdgeDriver;
import org.openqa.selenium.edge.EdgeOptions;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.FluentWait;
import org.openqa.selenium.support.ui.WebDriverWait;

import com.aventstack.extentreports.Status;

import ExtentListeners.ExtentListeners;

public class BasePage {

	public static WebDriver driver;
	public static Properties OR = new Properties();
	public static Properties config = new Properties();
	private static FileInputStream fis;
	protected static Logger log = Logger.getLogger("BaseTest.class");
	public static WebDriverWait wait;

	static DataFormatter formatter = new DataFormatter();

	public void ExcelWorkBookComparision(String previousReleaseFile, String currentReleaseFile)
			throws EncryptedDocumentException, IOException {
		String userDir = new File(System.getProperty("user.dir")).getAbsolutePath();
		try {
			fis = new FileInputStream("./src/main/resources/properties/config.properties");
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		try {
			config.load(fis);
			log.info("Config properties file loaded");
		} catch (IOException e) {
			e.printStackTrace();
		}
		String comparisionExcelFilePath = userDir + config.getProperty("excelCompareReport");
		System.out.println(comparisionExcelFilePath);
		PrintStream myConsole = new PrintStream(comparisionExcelFilePath);
		System.setOut(myConsole);

		FileInputStream file1 = new FileInputStream(
				userDir + config.getProperty("previousReleaseFileLocation") + previousReleaseFile);
		System.out.println(userDir + config.getProperty("previousReleaseFileLocation") + previousReleaseFile);
		FileInputStream file2 = new FileInputStream(
				userDir + config.getProperty("currentReleaseFileLocation") + currentReleaseFile);
		System.out.println(userDir + config.getProperty("currentReleaseFileLocation") + currentReleaseFile);
		try (XSSFWorkbook workbook1 = new XSSFWorkbook(file1); XSSFWorkbook workbook2 = new XSSFWorkbook(file2)) {
			if (workbook1.getNumberOfSheets() != workbook2.getNumberOfSheets()) {
				myConsole.println("Workbooks have different sheet count!");
				return;
			}

			// Iterate through sheets
			for (int i = 0; i < workbook1.getNumberOfSheets(); i++) {
				XSSFSheet sheet1 = workbook1.getSheetAt(i);
				XSSFSheet sheet2 = workbook2.getSheetAt(i);

				// Compare sheet names
				if (!sheet1.getSheetName().equals(sheet2.getSheetName())) {
					myConsole.println("Sheet names differ at index: " + i);
				}

				// Example using a loop to iterate through rows and columns
				for (int rowNum = 0; rowNum <= sheet1.getLastRowNum(); rowNum++) {
					XSSFRow row1 = sheet1.getRow(rowNum);
					XSSFRow row2 = sheet2.getRow(rowNum);

					// Check if rows exist in both sheets
					if (row1 == null && row2 != null || row1 != null && row2 == null) {
						myConsole.println("Rows differ at sheet: " + sheet1.getSheetName() + ", row: " + rowNum);
						continue;
					}

					if (row1 != null) {
						for (int colNum = 0; colNum < row1.getLastCellNum(); colNum++) {
							Cell cell1 = row1.getCell(colNum);
							Cell cell2 = row2.getCell(colNum);

							// Check if cells exist in both rows
							if (cell1 == null && cell2 != null || cell1 != null && cell2 == null) {
								myConsole.println("Cells differ at sheet: " + sheet1.getSheetName() + ", row: " + rowNum
										+ ", column: " + colNum);
								continue;
							}

							Object value1 = formatter.formatCellValue(cell1);
							Object value2 = formatter.formatCellValue(cell2);

							if (!value1.equals(value2)) {
								myConsole.println("Cell values differ at sheet: " + sheet1.getSheetName() + ", row: "
										+ rowNum + ", column: " + colNum + "  " + value1 + " VS " + value2);
							}

							// if(!((String) value1).isEmpty() && !((String) value2).isEmpty() &&
							// value1.equals(value2))
							// {
							// myConsole.println("Cell values match at sheet: " + sheet1.getSheetName() + ",
							// row: " + rowNum + ", column: " + colNum + " "+ value1 + " VS " + value2);
							// }
						}
					}
				}
			}
		}
	}

	// Method to perform pre-requisite steps
	public void setUp() {
		PropertyConfigurator.configure("./src/main/resources/properties/log4j.properties");
		log.info("Test Execution Starts !!");
		ExtentListeners.test.info("Test Execution Starts !!");

		try {
			fis = new FileInputStream("./src/main/resources/properties/config.properties");
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		try {
			config.load(fis);
			log.info("Config properties file loaded");
		} catch (IOException e) {
			e.printStackTrace();
		}
		try {
			fis = new FileInputStream("./src/main/resources/properties/or.properties");
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		try {
			OR.load(fis);
			log.info("OR properties file loaded");
		} catch (IOException e) {

			e.printStackTrace();
		}

		if (config.getProperty("browser").equals("chrome")) {
			ChromeOptions options = new ChromeOptions();
			options.addArguments("start-maximized");
			options.setAcceptInsecureCerts(true);
			String userDir = new File(System.getProperty("user.dir")).getAbsolutePath();
			String completeDownloadExcelPath = userDir + "\\src\\main\\resources\\downloadExcel";
			Map<String, Object> chromePrefs = new HashMap<String, Object>();
			chromePrefs.put("download.default_directory", completeDownloadExcelPath);
			options.setExperimentalOption("prefs", chromePrefs);
			options.setExperimentalOption("useAutomationExtension", false);
			driver = new ChromeDriver(options);
			log.info("Chrome browser launched");
			ExtentListeners.test.info("Chrome browser launched");
		} else if (config.getProperty("browser").equals("edge")) {
			EdgeOptions options = new EdgeOptions();
			options.addArguments("start-maximized");
			options.setAcceptInsecureCerts(true);
			// options.addArguments("--headless");
			options.addArguments("force-device-scale-factor=0.90");
			options.addArguments("high-dpi-support=0.90");
			String userDir = new File(System.getProperty("user.dir")).getAbsolutePath();
			String completeDownloadExcelPath = userDir + "\\src\\main\\resources\\downloadExcel";
			Map<String, Object> edgePrefs = new HashMap<String, Object>();
			edgePrefs.put("download.default_directory", completeDownloadExcelPath);
			options.setExperimentalOption("prefs", edgePrefs);
			options.setExperimentalOption("useAutomationExtension", false);
			driver = new EdgeDriver(options);
			log.info("Edge browser launched");
			ExtentListeners.test.info("Edge browser launched");
		} else if (config.getProperty("browser").equals("firefox")) {
			driver = new FirefoxDriver();
			log.info("Firefox browser launched");
			ExtentListeners.test.info("Firefox browser launched");
		}
		driver.get(config.getProperty("testsiteurl"));
		log.info("Navigate to : " + config.getProperty("testsiteurl"));
		ExtentListeners.test.info("Navigate to : " + config.getProperty("testsiteurl"));
		driver.manage().window().maximize();
		driver.manage().timeouts()
				.implicitlyWait(Duration.ofSeconds(Integer.parseInt(config.getProperty("implicit.wait"))));
		wait = new WebDriverWait(driver, Duration.ofSeconds(Integer.parseInt(config.getProperty("explicit.wait"))));
	}

	public boolean isFileExistsAtLocation(String fileName) {
		String userDir = new File(System.getProperty("user.dir")).getAbsolutePath();
		String downloadedFileDirectory = userDir + "\\src\\main\\resources\\downloadExcel\\";
		File tmpDir = new File(downloadedFileDirectory + fileName + ".xlsx");
		FluentWait<WebDriver> wait = new FluentWait<WebDriver>(driver).withTimeout(Duration.ofMinutes(3))
				.pollingEvery(Duration.ofSeconds(10));
		wait.until(x -> tmpDir.exists());
		boolean fileExists = tmpDir.exists();
		if (fileExists) {
			log.info(fileName + "File exists at a location : " + fileExists);
			ExtentListeners.test.info(fileName + "File exists at a location : " + fileExists);
		} else {
			log.info(fileName + "File exists at a location : " + fileExists);
			ExtentListeners.test.info(fileName + "File exists at a location : " + fileExists);
		}
		return fileExists;
	}

	// Generic method to enter text into an input box
	public void enterTextIntoInputBox(String locatorKey, String value) {
		try {
			if (locatorKey.endsWith("_ID")) {
				wait.until(ExpectedConditions.presenceOfElementLocated(By.id(OR.getProperty(locatorKey))))
						.sendKeys(value);
			} else if (locatorKey.endsWith("_XPATH")) {
				wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath(OR.getProperty(locatorKey))))
						.sendKeys(value);
			} else if (locatorKey.endsWith("_CSS")) {
				wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector(OR.getProperty(locatorKey))))
						.sendKeys(value);
			}
			log.info("Typing in " + locatorKey + " element and entered the value as " + value);
			ExtentListeners.test.log(Status.PASS,
					"Typing in " + locatorKey + " element and entered the value as " + value);
		} catch (NoSuchElementException e) {
			log.error("Element not found: " + locatorKey);
			ExtentListeners.test.log(Status.FAIL, "Element not found: " + locatorKey);
			e.printStackTrace();
		}
	}

	// Generic method to enter text into an input box
	public void enterTextIntoInputBoxForLogin(String locatorKey, String value) {
		try {
			if (locatorKey.endsWith("_ID")) {
				wait.until(ExpectedConditions.presenceOfElementLocated(By.id(OR.getProperty(locatorKey))))
						.sendKeys(value);
			} else if (locatorKey.endsWith("_XPATH")) {
				wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath(OR.getProperty(locatorKey))))
						.sendKeys(value);
			} else if (locatorKey.endsWith("_CSS")) {
				wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector(OR.getProperty(locatorKey))))
						.sendKeys(value);
			}
			log.info("Typing in " + locatorKey + " element and entered the value as " + value);
			ExtentListeners.test.log(Status.PASS, "Typing in " + locatorKey + " element and entered the value");
		} catch (NoSuchElementException e) {
			log.error("Element not found: " + locatorKey);
			ExtentListeners.test.log(Status.FAIL, "Element not found: " + locatorKey);
			e.printStackTrace();
		}
	}

	// Generic method to upload excel file
	public void uploadExcel(String locatorKey, String value) {
		try {
			if (locatorKey.endsWith("_ID")) {
				wait.until(ExpectedConditions.presenceOfElementLocated(By.id(OR.getProperty(locatorKey))))
						.sendKeys(value);
			} else if (locatorKey.endsWith("_XPATH")) {
				wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath(OR.getProperty(locatorKey))))
						.sendKeys(value);
			} else if (locatorKey.endsWith("_CSS")) {
				wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector(OR.getProperty(locatorKey))))
						.sendKeys(value);
			}
			log.info("File is uploaded from the location as " + value);
			ExtentListeners.test.log(Status.PASS, "File is uploaded from the location as " + value);
		} catch (NoSuchElementException e) {
			log.error("File location not found: " + locatorKey);
			ExtentListeners.test.log(Status.FAIL, "File location not found: " + locatorKey);
			e.printStackTrace();
		}
	}

	// Method to click on show results button
	public void clickOnShowResultsButton() {
		try {
			click("showResultsButton_XPATH");
			ExtentListeners.test.log(Status.PASS, "Clicked on show result button");
		} catch (NoSuchElementException e) {
			log.error("Show result button is not clicked successfully");
			ExtentListeners.test.log(Status.FAIL, "Show result button is not clicked successfully");
		}
	}

	// Generic method to click on any webElement
	public static void click(String locatorKey) {
		try {
			if (locatorKey.endsWith("_ID")) {
				wait.until(ExpectedConditions.elementToBeClickable(By.id(OR.getProperty(locatorKey)))).click();
			} else if (locatorKey.endsWith("_XPATH")) {
				wait.until(ExpectedConditions.elementToBeClickable(By.xpath(OR.getProperty(locatorKey)))).click();
			} else if (locatorKey.endsWith("_CSS")) {
				wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector(OR.getProperty(locatorKey)))).click();
			}
			log.info("Clicking on " + locatorKey);
			ExtentListeners.test.log(Status.PASS, "Clicking on " + locatorKey);

		} catch (NoSuchElementException e) {
			log.error("The element " + locatorKey + " is not available to click");
			ExtentListeners.test.log(Status.FAIL, "The element " + locatorKey + " is not available to click");
		}
	}

	// Generic method to click on upload icon across page
	public void clickUploadIconOnDataUploadPage(String inputTypes) throws InterruptedException {
		try {
			WebElement uploadIcon = wait
					.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//span[contains(text(),'"
							+ inputTypes + "')]/ancestor::a/ancestor::md-input-container/following-sibling::button")));
			uploadIcon.click();
			log.info("Clicking on upload icon of " + inputTypes);
			ExtentListeners.test.log(Status.PASS, "Clicking on upload icon of " + inputTypes);
		} catch (NoSuchElementException e) {
			log.error("The upload icon of " + inputTypes + " is not available to click");
			ExtentListeners.test.log(Status.FAIL, "The upload icon of " + inputTypes + " is not available to click");
		}
	}

	public void UploadFile(String inputDataFileName) {
		System.out.println(config.getProperty("uploadFileLocation"));
	}

	// Method to set explicit waits
	public void setExplicitWaitInMinutes(String locatorKey, int minWait) {
		try {
			if (locatorKey.endsWith("_ID")) {
				new WebDriverWait(driver, Duration.ofMinutes(minWait))
						.until(ExpectedConditions.presenceOfElementLocated(By.id(OR.getProperty(locatorKey))));
			} else if (locatorKey.endsWith("_XPATH")) {
				new WebDriverWait(driver, Duration.ofMinutes(minWait))
						.until(ExpectedConditions.presenceOfElementLocated(By.xpath(OR.getProperty(locatorKey))));
			} else if (locatorKey.endsWith("_CSS")) {
				new WebDriverWait(driver, Duration.ofMinutes(minWait))
						.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector(OR.getProperty(locatorKey))));
			}

			log.info("Element " + locatorKey + " is visible on page");
			ExtentListeners.test.info("Element " + locatorKey + " is visible on page");
		} catch (Throwable t) {
			log.info("Error while waiting for visibility of element " + locatorKey);
			ExtentListeners.test.info("Error while waiting for visibility of element " + locatorKey);
		}
	}

	// Method to check if the element is present
	public boolean isElementPresent(String locatorKey) {
		try {
			if (locatorKey.endsWith("_ID")) {
				driver.findElement(By.id(OR.getProperty(locatorKey)));
			} else if (locatorKey.endsWith("_XPATH")) {
				driver.findElement(By.xpath(OR.getProperty(locatorKey)));
			} else if (locatorKey.endsWith("_CSS")) {
				driver.findElement(By.cssSelector(OR.getProperty(locatorKey)));
			}
			log.info("Element " + locatorKey + " is present on webpage");
			ExtentListeners.test.info("Element " + locatorKey + " is present on webpage");
			return true;
		} catch (Throwable t) {
			log.info("Error while finding presence of element " + locatorKey);
			ExtentListeners.test.info("Error while finding presence of element " + locatorKey);
			return false;
		}
	}

	// Method to perform page scroll down
	public void pageScrollDown() {
		JavascriptExecutor jse = (JavascriptExecutor) driver;
		jse.executeScript("window.scrollTo(0, 500);");
	}

	public void pageScrollRight1() {
		JavascriptExecutor jse = (JavascriptExecutor) driver;
		jse.executeScript("window.scrollTo(500, 0);");
	}

	// Method to perform page scroll up
	public void pageScrollUp() {
		JavascriptExecutor jse = (JavascriptExecutor) driver;
		jse.executeScript("window.scrollTo(0, -500);");
	}

	// Method to get upload file location}
	public String getUploadFileLocation() {
		return config.getProperty("uploadFileLocation");
	}

	// Method to get upload file location
	public String getDownloadedFileLocation() {
		return config.getProperty("downloadFileLocation");
	}

	// Method to get text
	public String getText(String locatorKey) {
		String text = null;
		try {
			if (locatorKey.endsWith("_ID")) {
				text = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id(OR.getProperty(locatorKey))))
						.getText();
			} else if (locatorKey.endsWith("_XPATH")) {
				text = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath(OR.getProperty(locatorKey))))
						.getText();
			} else if (locatorKey.endsWith("_CSS")) {
				text = wait.until(
						ExpectedConditions.visibilityOfElementLocated(By.cssSelector(OR.getProperty(locatorKey))))
						.getText();
			}
			log.info("Element available as text : " + text);
			ExtentListeners.test.log(Status.PASS, "Element available as text : " + text);
			return text;
		} catch (Throwable t) {
			log.info("The element " + locatorKey + " is not available on page");
			ExtentListeners.test.log(Status.FAIL, "The element " + locatorKey + " is not available on page");
			return text;
		}
	}

	// Method to get text from calendar
	public String getTextFromCalendar() {
		String Text = driver.findElement(By.xpath("/html[1]/body[1]/div[6]/div[1]/table[1]/thead[1]/tr[1]/th[2]"))
				.getText();
		return Text;

	}

	// Method to put explicit waits in seconds
	public void setExplicitWaitInSeconds(String locatorKey, long secWait) {
		try {
			if (locatorKey.endsWith("_ID")) {
				new WebDriverWait(driver, Duration.ofSeconds(secWait))
						.until(ExpectedConditions.visibilityOfElementLocated(By.id(OR.getProperty(locatorKey))));
			} else if (locatorKey.endsWith("_XPATH")) {
				new WebDriverWait(driver, Duration.ofSeconds(secWait))
						.until(ExpectedConditions.visibilityOfElementLocated(By.xpath(OR.getProperty(locatorKey))));
			} else if (locatorKey.endsWith("_CSS")) {
				new WebDriverWait(driver, Duration.ofSeconds(secWait)).until(
						ExpectedConditions.visibilityOfElementLocated(By.cssSelector(OR.getProperty(locatorKey))));
			}

			log.info("Element " + locatorKey + " is visible on page");
			ExtentListeners.test.log(Status.PASS, "Element " + locatorKey + " is visible on page");

		} catch (Throwable t) {
			log.info("Error while waiting for visibility of element " + locatorKey);
			ExtentListeners.test.log(Status.FAIL, "Error while waiting for visibility of element " + locatorKey);
		}
	}

	// Method to click on escape button
	public void clickEscape() throws AWTException, InterruptedException {
		Thread.sleep(2000);
		Robot r = new Robot();
		r.keyPress(KeyEvent.VK_ESCAPE);
		r.keyRelease(KeyEvent.VK_ESCAPE);
	}

	// Method to right click
	public void mouseClickRight(int mask) throws AWTException, InterruptedException {
		Robot rob = new Robot();
		Thread.sleep(1000);
		rob.mousePress(mask);
		rob.mouseRelease(mask);
	}

	// Method to delete old files from the location
	public void deleteOldFilesFromLocation(String AgreementType, String month, String year, String billType) {
		String userDir = new File(System.getProperty("user.dir")).getAbsolutePath();
		File downloadDirectory = new File(userDir + "\\src\\main\\resources\\downloadExcel\\");
		File[] files = downloadDirectory.listFiles((dir, name) -> name
				.equals(AgreementType + "_" + month + "-" + year + "_" + billType + "_External.xlsx"));
		Arrays.asList(files).stream().forEach(File::delete);
	}

	public void deleteOldFilesFromLocation(String deleteFileName) {

		String userDir = new File(System.getProperty("user.dir")).getAbsolutePath();

		File downloadDirectory = new File(userDir + "\\src\\main\\resources\\downloadExcel\\");

		File[] files = downloadDirectory.listFiles((dir, name) -> name.startsWith(deleteFileName));

		Arrays.asList(files).stream().forEach(File::delete);

	}

	// Method to get today's date
	public String getTodaysDate() {
		DateTimeFormatter dtf = DateTimeFormatter.ofPattern("d-MMM-yyyy");
		LocalDate localDate = LocalDate.now();
		return dtf.format(localDate);
	}

	// Method to update the file name
	public void updateFileName(String beforeFileName, String afterFileName) {
		// File downloadDirectory = new File(config.getProperty("excelDirectoryPath"));
		File file = new File(config.getProperty("fileLocation") + beforeFileName);
		File renameFile = new File(config.getProperty("fileLocation") + afterFileName);
		boolean flag = file.renameTo(renameFile);
		if (flag == true) {
			log.info("File Successfully Renamed from :" + beforeFileName + " to: " + afterFileName);
			ExtentListeners.test.log(Status.PASS,
					"File Successfully Renamed from :" + beforeFileName + " to: " + afterFileName);
		} else {
			log.info("File Renaming Operation failed from :" + beforeFileName + " to: " + afterFileName);
			ExtentListeners.test.log(Status.FAIL,
					"File Successfully Renamed from :" + beforeFileName + " to: " + afterFileName);
		}
	}

	// Method to get alert text
	public String getAlertText() {
		Alert alert = driver.switchTo().alert();
		String alertText = alert.getText();
		log.info("Handling the alert and getting the alert text as " + alertText);
		ExtentListeners.test.info("Handling the alert and getting the alert text as " + alertText);
		return alertText;
	}

	// Method to compare alert data
	public void alertDataComparision(String expectedAlertText, String actualAlertText) {
		if (expectedAlertText.equals(actualAlertText)) {
			log.info("File is uploaded correctly for the requested month-year");
			ExtentListeners.test.log(Status.PASS, "File is uploaded correctly for the requested month-year");
		} else {
			log.info("File is not uploaded correctly for the requested month-year");
			ExtentListeners.test.log(Status.FAIL, "File is not uploaded correctly for the requested month-year");
		}
	}

	// Method to select agreement type
	public void selectDropdownOption(String agreementType) {
		WebElement element = driver.findElement(By.xpath("//*[@ng-reflect-value=" + "'" + agreementType + "'" + "]"));
		element.click();
		log.info("Agreement Type selected as: " + agreementType);
		ExtentListeners.test.info("Agreement Type selected as: " + agreementType);
	}

	// Method to click on load asset data button
	public void clickLoadAssetData() {
		try {
			Thread.sleep(3000);
			click("loadAssetDataButton_XPATH");
			log.info("Clicking on load asset data button successful");
			ExtentListeners.test.log(Status.PASS, "Clicking on load asset data button successful");
		} catch (Exception e) {
			log.info("Clicking on load asset data button unsuccessful");
			ExtentListeners.test.log(Status.FAIL, "Clicking on load asset data button unsuccessful");
		}
	}

	// Method to dismiss the alert
	public void dismissTheAlert() {
		driver.switchTo().alert().dismiss();
		log.info("Dismissing the alert");
		ExtentListeners.test.info("Dismissing the alert");
	}

	// Method to accept the alert
	public void acceptTheAlert() {
		driver.switchTo().alert().accept();
		log.info("Accepting the alert");
		ExtentListeners.test.info("Accepting the alert");
	}

	// Method to enter date and month in calendar for Bills // Used in loaders
	public void enterDateAndMonthInCalendar(String year, String month) throws InterruptedException {
		driver.findElement(By.id("startDate")).click();
		while (true) {
			String Text = driver.findElement(By.cssSelector(".ng-binding[ng-bind='view.title']")).getText();

			if (Text.equalsIgnoreCase(year)) {
				break;
			} else {
				driver.findElement(By.cssSelector(".ng-binding[ng-class='{disabled: !view.next.selectable}']")).click();
			}
		}
		driver.findElement(By.xpath("//td[normalize-space()='" + month + "']")).click();
		ExtentListeners.test.info("Entering date and month in calendar");
	}

	// Method to click on preview asset data on details pop up
	public void clickPreviewAssetDataOnDetailsPopup() {
		try {
			click("previewAssetDataOnDetailsPopup_XPATH");
			log.info("Clicking on preview asset data button on details successful");
			ExtentListeners.test.log(Status.PASS, "Clicking on preview asset data button on details successful");
		} catch (Exception e) {
			log.info("Clicking on preview asset data button on details unsuccessful");
			ExtentListeners.test.log(Status.FAIL, "Clicking on preview asset data button on details unsuccessful");
		}
	}

	// Method to enter date and month in calendar for Bills // Used in energy
	// billable records
	public static void enterDateAndMonthInCalendarForBill(String year, String month) {
		while (true) {
			String Text = driver.findElement(By.cssSelector(".ng-binding[ng-bind='view.title']")).getText();

			if (Text.equalsIgnoreCase(year)) {
				break;
			} else {
				driver.findElement(By.cssSelector(".ng-binding[ng-class='{disabled: !view.next.selectable}']")).click();
			}
		}
		driver.findElement(By.xpath("//td[normalize-space()='" + month + "']")).click();
		ExtentListeners.test.info("Entering date and month in calendar for bills");
	}

	public void HandleProcessingDataBlocker() {
		try {
			WebElement elementBlock = (driver.findElement(By.xpath("//div[@class='blockUI blockOverlay']")));
			// blockUI blockMsg blockPage
			WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(6000));
			wait.until(ExpectedConditions.invisibilityOf(elementBlock));
			log.info("Wait for elements to load");
			ExtentListeners.test.info("Wait for elements to load");
		} finally {
			log.info("Element is already loaded");
			ExtentListeners.test.info("Element is already loaded");
		}
	}

	// Method to double click on an element
	public void doubleClick(String locatorKey) {
		Actions actions = new Actions(driver);
		try {
			if (locatorKey.endsWith("_ID")) {
				WebElement element = wait
						.until(ExpectedConditions.elementToBeClickable(By.id(OR.getProperty(locatorKey))));
				actions.contextClick(element).perform();
			} else if (locatorKey.endsWith("_XPATH")) {
				WebElement element = wait
						.until(ExpectedConditions.elementToBeClickable(By.xpath(OR.getProperty(locatorKey))));
				actions.contextClick(element).perform();
			} else if (locatorKey.endsWith("_CSS")) {
				WebElement element = wait
						.until(ExpectedConditions.elementToBeClickable(By.cssSelector(OR.getProperty(locatorKey))));
				actions.contextClick(element).perform();
			}
			log.info("Double clicking on " + locatorKey);
			ExtentListeners.test.info("Double clicking on " + locatorKey);
		} catch (NoSuchElementException e) {
			log.error("The element " + locatorKey + " is not available to double click");
			ExtentListeners.test.info("The element " + locatorKey + " is not available to double click");
		}
	}

	// Click on blank area
	public void clickOnBlankArea() throws InterruptedException {
		Thread.sleep(5000);
		driver.findElement(By.xpath("//body")).click();
	}

	// To close all the session of browser
	public static void quit() {
		driver.quit();
		log.info("Browser is closed and test execution complete ");
		ExtentListeners.test.info("Browser is closed and test execution complete ");
	}

	public void clickOnBlocker() {
		click("screenBlocker_XPATH");
	}

	public void pageSlidingRight() throws InterruptedException {
		WebElement e = driver.findElement(
				By.xpath("//app-common-grid/block-ui/div[1]/div/ag-grid-angular/div/div[2]/div[2]/div[5]/div[2]"));
		Actions move = new Actions(driver);
		move.moveToElement(e).clickAndHold().moveByOffset(500, 0).release().perform();
		Thread.sleep(5000);

	}

	public void refreshPage() {
		driver.navigate().refresh();

	}

	public void selectQuarterFromDropdownOption2(String quarterType) {
		WebElement element = driver.findElement(
				By.xpath("//span[@class='mat-option-text'][normalize-space()=" + "'" + quarterType + "'" + "]"));
		element.click();
		log.info("Quarter Type selected as: " + quarterType);
		ExtentListeners.test.info("Quarter Type selected as: " + quarterType);

	}

	public void QuarterFromDropdownOption(String quarterType) throws InterruptedException {
		click("quarterFromDropdownOptioncheckbox_XPATH");
		WebElement element = driver.findElement(By.xpath("//span[@class='mat-option-text'][normalize-space()=" + "'"
				+ quarterType + "'" + "]/preceding-sibling::mat-pseudo-checkbox"));
		element.click();

		log.info("Quarter Type selected as: " + quarterType);
		ExtentListeners.test.info("Quarter Type selected as: " + quarterType);
		Thread.sleep(3000);

	}

	public void clickOutside() throws InterruptedException {
		Actions act = new Actions(driver);
		act.sendKeys(Keys.TAB).perform();
		Thread.sleep(3000);
	}

	public void selectCurrencyFromDropdown() {
		WebElement element = driver.findElement(By.xpath("//span[normalize-space()='USD']/parent::mat-option"));
		element.click();
		log.info("Quarter Type selected as: USD");
		ExtentListeners.test.info("Quarter Type selected as: USD");
	}

	public void selectCurrencyToDropdown() {
		WebElement element = driver.findElement(By.xpath("/html/body/div[2]/div[4]/div/div/div/mat-option[2]/span"));
		element.click();
		log.info("Quarter Type selected as: NGN");
		ExtentListeners.test.info("Quarter Type selected as: NGN");
	}
	public void enterDateAndMonthInCalendarMTNAsset(String assetyear, String assetmonth) {
		driver.findElement(By.id("startDate")).click();
		while (true) {
			String Text = driver.findElement(By.cssSelector(".ng-binding[ng-bind='view.title']")).getText();

			if (Text.equalsIgnoreCase(assetyear)) {
				break;
			} else {
				driver.findElement(By.cssSelector(".ng-binding[ng-class='{disabled: !view.next.selectable}']")).click();
			}
		}
		driver.findElement(By.xpath("//td[normalize-space()='" + assetmonth + "']")).click();
		ExtentListeners.test.info("Entering date and month in calendar");
		
		
	}
public void enterDateAndMonthInCalendarMTN(String assetyear, String assetmonth) {
		
		while (true) {
			String Text = driver.findElement(By.cssSelector(".ng-binding[ng-bind='view.title']")).getText();

			if (Text.equalsIgnoreCase(assetyear)) {
				break;
			} else {
				driver.findElement(By.cssSelector(".ng-binding[ng-class='{disabled: !view.next.selectable}']")).click();
			}
		}
		driver.findElement(By.xpath("//td[normalize-space()='" + assetmonth + "']")).click();
		ExtentListeners.test.info("Entering date and month in calendar");
		
	}

	public void enterDateAndMonthInCalendarLagos(String year) throws InterruptedException {
		driver.findElement(By.id("startDate")).click();
		Thread.sleep(5000);
		int year1 = Integer.parseInt(year);
		outloop: while (true) {
			if (year1 >= 2030) {
				Thread.sleep(2000);
				driver.findElement(By.xpath("//th[contains(text(),'→')]")).click();
				Thread.sleep(2000);
			}
			String yearRange = driver.findElement(By.cssSelector(".ng-binding[ng-bind='view.title']")).getText();
			String[] parts = yearRange.split(" - ");
			String startYear1 = parts[0]; // "startYear"
			String endYear1 = parts[1]; // "endYear"
			int startYear = Integer.parseInt(startYear1);
			int endYear = Integer.parseInt(endYear1);
			for (int i = startYear; i <= endYear; i++) {
				if (year1 == i) {
					driver.findElement(By.xpath("//td[normalize-space()='" + i + "']")).click();
					
					break outloop;

				}
			}
		}
	}

	public void enterDateAndMonthInCalendar2(String year, String month) throws InterruptedException {
		Thread.sleep(5000);
		//driver.findElement(By.id("startDate")).click();
		while (true) {
			String Text = driver.findElement(By.cssSelector("button[aria-label='Choose date']")).getText();
 
			if (Text.equalsIgnoreCase(year)) {
				break;
			} else {
				driver.findElement(By.xpath("//button[@aria-label='Next year']")).click();
			}
		}
		//driver.findElement(By.xpath("//div[normalize-space()='"+month+"']")).click();
		//ExtentListeners.test.info("Entering date and month in calendar");
		List<WebElement> months=driver.findElements(By.cssSelector("div[class='mat-calendar-body-cell-content mat-focus-indicator']"));
		for(WebElement month1 : months)
		{
			if(month1.getText().equalsIgnoreCase(month))
			{
				month1.click();
				break;
			}
		}
		wait.until(ExpectedConditions.elementToBeClickable(By.xpath(
				"//div[normalize-space()='1'][@class=\"mat-calendar-body-cell-content mat-focus-indicator\"]"))).click();
 
	}

	public String CustomerSiteTypeDealNmaeValue(String CustomerTyepDealName) {
		String  value = driver.findElement(By.xpath("//span[contains(text(),'"+CustomerTyepDealName+"')]")).getText();
		//span[contains(text(),'BTF')]
		System.out.println(value);
		return value;
 
	}
	
	public void skipLagosifDataPresent1() throws InterruptedException
	{
	String Lockedsite = "0";

	try {
	   // WebDriverWait wait = new WebDriverWait(driver, 10);
	    WebElement lockedSitesElement = driver.findElement(By.xpath("//*[@id='center']/div/div[4]/div[3]/div/div/div[4]/div[2]/span/b"));
	    String lockedsites = lockedSitesElement.getText();
	    System.out.println("lockedsites value: " + lockedsites);

	    if (lockedsites.equals(Lockedsite.toLowerCase())) { // Case-insensitive comparison
	        System.out.println("Condition is true!");
	        pageScrollDown();
	        clickLoadAssetData();
	        dismissTheAlert(); // Check if this is correct
	        pageScrollDown();
	        clickLoadAssetData();
	        acceptTheAlert(); // Check if this is correct
	        setExplicitWaitInMinutes("completeStatusMessage_XPATH", 3);
	    } else {
	        System.out.println("Condition is false.");
	    }
	} catch (Exception e) {
	    System.out.println("Error: " + e.getMessage()); // Handle exceptions
	}
	
	}
	
	
	public void refreshPage1() throws InterruptedException {
		driver.navigate().refresh();
		Thread.sleep(5000);

	}
	
	public void refreshPage3() throws InterruptedException {
		Thread.sleep(10000);
		driver.navigate().refresh();
		Thread.sleep(5000);
		

	}

	
}
