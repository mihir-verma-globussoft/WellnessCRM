package com.crm.enhance_wellness

import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class FullAppImageHandlingDeviceTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun loginAndVerifyApiImageSurfaces() {
        val args = InstrumentationRegistry.getArguments()
        val email = requireNotNull(args.getString("email")) { "Missing instrumentation arg: email" }
        val password = requireNotNull(args.getString("password")) { "Missing instrumentation arg: password" }

        loginIfNeeded(email, password)
        waitForAny("Home", "Dashboard", "Overview")

        clickFirstClickableText("Catalog")
        waitForAny("Services", "Categories")
        clickFirstClickableText("Categories")
        waitForAny("Acne Treatment", "Consultation")
        composeRule.waitUntil(timeoutMillis = 15_000) {
            composeRule.onAllNodes(hasContentDescription("Acne Treatment") or hasContentDescription("Consultation"))
                .fetchSemanticsNodes()
                .isNotEmpty()
        }

        clickFirstClickableText("Bookings")
        waitForAny("Upcoming", "Pending", "Completed", "Cancelled")
        composeRule.onNodeWithContentDescription("Book appointment").performClick()
        waitForAny("Choose a service", "No services available")
        composeRule.onNodeWithText("Choose a service").assertExists()

        clickFirstClickableText("Finance")
        waitForAny("Gift", "Wallet", "Payments")

        clickFirstClickableText("Profile")
        waitForAny("MOHIT THAKUR", "CUSTOMER")
        composeRule.waitUntil(timeoutMillis = 15_000) {
            composeRule.onAllNodes(hasContentDescription("Profile photo"), useUnmergedTree = true)
                .fetchSemanticsNodes()
                .isNotEmpty()
        }
    }

    private fun loginIfNeeded(email: String, password: String) {
        composeRule.waitForIdle()
        val isOnLogin = composeRule.onAllNodesWithText("Email").fetchSemanticsNodes().isNotEmpty()
        if (!isOnLogin) return

        composeRule.onNodeWithText("Email").performTextInput(email)
        composeRule.onNodeWithText("Password").performTextInput(password)
        composeRule.onNodeWithText("Sign In").performClick()
        waitForAny("Home", "Overview")
    }

    private fun waitForAny(vararg texts: String) {
        composeRule.waitUntil(timeoutMillis = 30_000) {
            texts.any { text ->
                composeRule.onAllNodesWithText(text, substring = true)
                    .fetchSemanticsNodes()
                    .isNotEmpty()
            }
        }
    }

    private fun clickFirstClickableText(text: String) {
        composeRule.onAllNodes(hasText(text, substring = true) and hasClickAction())
            .onFirst()
            .performClick()
    }
}
