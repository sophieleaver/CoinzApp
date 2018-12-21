package com.example.sophie.coinzapp


import android.content.Intent
import android.support.test.espresso.Espresso.onView
import android.support.test.espresso.assertion.ViewAssertions.matches
import android.support.test.espresso.matcher.ViewMatchers.isDisplayed
import android.support.test.espresso.matcher.ViewMatchers.withId
import android.support.test.filters.LargeTest
import android.support.test.rule.ActivityTestRule
import android.support.test.runner.AndroidJUnit4
import com.google.firebase.auth.FirebaseAuth
import org.hamcrest.Matchers.allOf
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@LargeTest
@RunWith(AndroidJUnit4::class)
class LoginActivityCheck {

    /**
     * Checks that the LoginActivity is the first activity on opening the app
     */
    @Rule
    @JvmField
    var mActivityTestRule = ActivityTestRule(LoginActivity::class.java)
    private val auth = FirebaseAuth.getInstance()

    @Before
    fun createAndLoginNewUser(){
        auth.signOut()
        mActivityTestRule.launchActivity(Intent())
    }
    @Test
    fun loginActivityCheck() {
        Thread.sleep(7000)

        val button = onView(
                allOf(withId(R.id.register_button_dialog),
                        isDisplayed()))
        button.check(matches(isDisplayed()))
    }
}
