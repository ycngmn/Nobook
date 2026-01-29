package com.ycngmn.nobook

import com.microsoft.playwright.BrowserType
import com.microsoft.playwright.Playwright
import com.microsoft.playwright.options.AriaRole
import com.ycngmn.nobook.utils.FACEBOOK_PASSWORD
import com.ycngmn.nobook.utils.FACEBOOK_USERNAME
import com.ycngmn.nobook.utils.nbTestContext
import org.junit.Test
import java.io.File

/*
* Strategy:
*  for each language, scroll until at least 3 ads.
*  count total posts.
*  apply adblock script.
*  assert ads are not visible.
*  total post count = initial counts - ads counts
* */
class AdblockTest {

    @Test
    fun generateAuth() {
        Playwright.create().use { playwright ->
            playwright.chromium().launch(BrowserType.LaunchOptions().setTimeout(10000.0)).use { browser ->
                val context = browser.newContext()
                val page = context.newPage()
                page.navigate("https://www.facebook.com/")

                val cookiePopUp = page.locator("._pig")
                cookiePopUp.waitFor()
                cookiePopUp.getByRole(AriaRole.BUTTON).last().click()

                page.getByTestId("royal-email").click()
                page.getByTestId("royal-email").fill(FACEBOOK_USERNAME)
                page.getByTestId("royal-pass").click()
                page.getByTestId("royal-pass").fill(FACEBOOK_PASSWORD)
                page.getByTestId("royal-login-button").click()
                page.waitForLoadState()
                File("src/test/resources/auth.json")
                    .writeText(context.storageState())
            }
        }
    }

    @Test
    fun testAdblock() {
        Playwright.create().use { playwright ->
            val options = BrowserType.LaunchOptions().setHeadless(false).setSlowMo(100.0)
            playwright.chromium().launch(options).use { browser ->
                val context = browser.nbTestContext()
                val page = context.newPage()
                page.navigate("https://facebook.com/")
                page.evaluate(File("src/main/res/raw/scripts.js").readText())
                page.evaluate(File("src/main/res/raw/adblock.js").readText())
                page.waitForTimeout(5000.0)
            }
        }
    }
}