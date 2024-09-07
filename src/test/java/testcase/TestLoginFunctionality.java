package testcase;

import java.awt.AWTException;
import java.io.IOException;
import org.apache.poi.EncryptedDocumentException;
import org.testng.Assert;
import org.testng.annotations.Test;

import Pages.LoginPage;
import Pages.SelectCountryPage;
import base.BasePage;
import utilities.DataUtilities;

public class TestLoginFunctionality {

	BasePage base = new BasePage();
	SelectCountryPage selcountry = new SelectCountryPage();
	LoginPage login = new LoginPage();
	@Test(dataProviderClass = DataUtilities.class, dataProvider = "dataprocess")
	public void testLoginFunctionality(String username, String password, String country)
			throws InterruptedException, AWTException, EncryptedDocumentException, IOException {
		// try {
		base.setUp(); // Launch browser
		login.doLogin(username, password); // perform login
		//Assert.assertEquals(base.getText("welcomeMessage_XPATH"), "Welcome");

		// Below are the steps to select country
		//selcountry.SelectCountry(country); // select the country
		// Assert.assertEquals(base.getText("masterConfig_CSS"), "Master
		// Configuration");

			}
}