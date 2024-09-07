package Pages;

import org.openqa.selenium.By;
import org.openqa.selenium.support.ui.ExpectedConditions;

import com.aventstack.extentreports.Status;

import base.BasePage;
import ExtentListeners.ExtentListeners;

public class SelectCountryPage extends BasePage {

	// To select the country on country page
	public void SelectCountry(String country) throws InterruptedException {
		wait.until(ExpectedConditions
				.elementToBeClickable(By.xpath("//button[contains(text()," + "'" + country + "'" + ")]"))).click();
		ExtentListeners.test.log(Status.INFO, country + " :country is selected successfully.");
		
	}
}