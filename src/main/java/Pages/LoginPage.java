package Pages;

import base.BasePage;

public class LoginPage extends BasePage {

	public SelectCountryPage doLogin(String username, String password) throws InterruptedException {
		enterTextIntoInputBoxForLogin("username_ID", username);
		click("nextButton_XPATH");
		enterTextIntoInputBoxForLogin("password_ID", password);
		//click("loginButton_XPATH");
		return new SelectCountryPage();
	}
}