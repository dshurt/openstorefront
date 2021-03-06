/*
 * Copyright 2017 Space Dynamics Laboratory - Utah State University Research Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package edu.usu.sdl.openstorefront.ui.test.user;

import edu.usu.sdl.openstorefront.selenium.provider.AuthenticationProvider;
import edu.usu.sdl.openstorefront.selenium.provider.ClientApiProvider;
import edu.usu.sdl.openstorefront.selenium.provider.NotificationEventProvider;
import edu.usu.sdl.openstorefront.ui.test.BrowserTestBase;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

/**
 *
 * @author dshurtleff
 */
public class UserProfileIT
		extends BrowserTestBase
{

	private static final Logger LOG = Logger.getLogger(BrowserTestBase.class.getName());
	private AuthenticationProvider authProvider;
	private NotificationEventProvider notificationProvider;
	private ClientApiProvider provider;

	@Before
	public void setup() throws InterruptedException
	{
		authProvider = new AuthenticationProvider(properties, webDriverUtil);
		authProvider.login();
		provider = new ClientApiProvider();
		notificationProvider = new NotificationEventProvider(provider.getAPIClient());
	}

	@Test
	public void userProfileTest()
	{
		for (WebDriver driver : webDriverUtil.getDrivers()) {

			webDriverUtil.getPage(driver, "UserTool.action");
			editProfile(driver, properties.getProperty("test.newuseremail"));
			sendTestMessage(driver);
		}
	}

	public void editProfile(WebDriver driver, String email)
	{
		WebDriverWait wait = new WebDriverWait(driver, 10);

		wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("#userHeaderProfileBtn"))).click();

		wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("[name='phone']"))).clear();

		wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("[name='phone']"))).sendKeys("000-000-0000");

		wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("[name*='email']"))).clear();

		wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("[name*='email']"))).sendKeys(email);

		wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("[data-test='saveProfileFormBtn']"))).click();

		try {
			wait.until(ExpectedConditions.textToBePresentInElementValue(By.cssSelector(".x-autocontainer-innerCt"), "Updated User Profile"));
		} catch (Exception e) {
			LOG.log(Level.INFO, e.toString());
		}

		try {
			wait.until(ExpectedConditions.invisibilityOfElementWithText(By.cssSelector(".x-mask-msg-text"), "Updated User Profile"));
		} catch (Exception e) {
			LOG.log(Level.INFO, e.toString());
		}
	}

	public void sendTestMessage(WebDriver driver)
	{
		WebDriverWait wait = new WebDriverWait(driver, 8);

		wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("[data-test='emailSendTestBtn']"))).click();

		try {
			wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".x-window.x-toast")));
		} catch (Exception e) {
			LOG.log(Level.INFO, e.toString());
		}

		try {
			wait.until(ExpectedConditions.invisibilityOfElementLocated(By.cssSelector(".x-window.x-toast")));
		} catch (Exception e) {
			LOG.log(Level.INFO, e.toString());
		}

		wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("#dashboardUserHomeButton"))).click();

		wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("#dashPanel_header-title-textEl")));

	}

	@After
	public void cleanupTest()
	{
		notificationProvider.cleanup();
	}
}
