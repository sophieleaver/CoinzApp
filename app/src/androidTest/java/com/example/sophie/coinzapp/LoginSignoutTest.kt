package com.example.sophie.coinzapp


import android.content.Intent
import android.support.test.espresso.Espresso.onView
import android.support.test.espresso.action.ViewActions.*
import android.support.test.espresso.assertion.ViewAssertions.matches
import android.support.test.espresso.matcher.ViewMatchers.*
import android.support.test.filters.LargeTest
import android.support.test.rule.ActivityTestRule
import android.support.test.rule.GrantPermissionRule
import android.support.test.runner.AndroidJUnit4
import android.view.View
import android.view.ViewGroup
import com.google.firebase.auth.FirebaseAuth
import org.hamcrest.Description
import org.hamcrest.Matcher
import org.hamcrest.Matchers.allOf
import org.hamcrest.TypeSafeMatcher
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@LargeTest
@RunWith(AndroidJUnit4::class)
class LoginSignoutTest {

    @Rule
    @JvmField
    var mActivityTestRule = ActivityTestRule(LoginActivity::class.java)

    @Rule
    @JvmField
    var mGrantPermissionRule =
            GrantPermissionRule.grant(
                    "android.permission.ACCESS_FINE_LOCATION")
    private val auth = FirebaseAuth.getInstance()
    @Before
    fun createAndLoginNewUser(){
        auth.signOut()
        mActivityTestRule.launchActivity(Intent())
    }

    @Test
    fun loginSignoutTest() {
        // Added a sleep statement to match the app's execution delay.
        // The recommended way to handle such scenarios is to use Espresso idling resources:
        // https://google.github.io/android-testing-support-library/docs/espresso/idling-resource/index.html
        Thread.sleep(7000)

        val appCompatButton = onView(
                allOf(withId(R.id.log_in_button_dialog), withText("log me in me hearty"),
                        isDisplayed()))
        appCompatButton.perform(click())

        val appCompatEditText4 = onView(
                allOf(withId(R.id.email_form),
                        isDisplayed()))
        appCompatEditText4.perform(replaceText("test@email.com"),closeSoftKeyboard())

        val appCompatEditText5 = onView(
                allOf(withId(R.id.password_form),
                        isDisplayed()))
        appCompatEditText5.perform(replaceText("password"), closeSoftKeyboard())

        val appCompatButton2 = onView(
                allOf(withId(android.R.id.button1), withText("Log In") ))
        appCompatButton2.perform(scrollTo(), click())

        // Added a sleep statement to match the app's execution delay.
        // The recommended way to handle such scenarios is to use Espresso idling resources:
        // https://google.github.io/android-testing-support-library/docs/espresso/idling-resource/index.html
        Thread.sleep(7000)

        val viewGroup = onView(
                allOf(withId(R.id.appBarLayout),
                        isDisplayed()))
        viewGroup.check(matches(isDisplayed()))

        val bottomNavigationItemView = onView(
                allOf(withId(R.id.navigation_settings),
                        isDisplayed()))
        bottomNavigationItemView.perform(click())

        val button = onView(
                allOf(withId(R.id.button_log_out),
                        isDisplayed()))
        button.check(matches(isDisplayed()))

        val appCompatButton3 = onView(
                allOf(withId(R.id.button_log_out), withText("log out"),
                        isDisplayed()))
        appCompatButton3.perform(click())

        // Added a sleep statement to match the app's execution delay.
        // The recommended way to handle such scenarios is to use Espresso idling resources:
        // https://google.github.io/android-testing-support-library/docs/espresso/idling-resource/index.html
        Thread.sleep(7000)

        val button2 = onView(
                allOf(withId(R.id.register_button_dialog),
                        isDisplayed()))
        button2.check(matches(isDisplayed()))
    }
}
