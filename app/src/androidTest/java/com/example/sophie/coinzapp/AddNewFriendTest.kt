package com.example.sophie.coinzapp


import android.content.Intent
import android.support.test.espresso.ViewInteraction
import android.support.test.rule.ActivityTestRule
import android.support.test.runner.AndroidJUnit4
import android.support.test.filters.LargeTest
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.view.ViewParent

import com.google.firebase.firestore.FirebaseFirestore

import org.hamcrest.Description
import org.hamcrest.Matcher
import org.hamcrest.TypeSafeMatcher
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

import android.support.test.espresso.Espresso.onView
import android.support.test.espresso.action.ViewActions.click
import android.support.test.espresso.action.ViewActions.closeSoftKeyboard
import android.support.test.espresso.action.ViewActions.replaceText
import android.support.test.espresso.action.ViewActions.scrollTo
import android.support.test.espresso.matcher.ViewMatchers.isDisplayed
import android.support.test.espresso.matcher.ViewMatchers.withClassName
import android.support.test.espresso.matcher.ViewMatchers.withId
import android.support.test.espresso.matcher.ViewMatchers.withText
import com.google.firebase.auth.FirebaseAuth
import org.hamcrest.Matchers.allOf
import org.hamcrest.Matchers.`is`

@LargeTest
@RunWith(AndroidJUnit4::class)
class AddNewFriendTest {

    @Rule
    @JvmField
    var mActivityTestRule = ActivityTestRule(LoginActivity::class.java)

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val tag = "AddNewFriendTest"

    @Before
    fun logoutCurrentUser(){
        auth.signOut()
        mActivityTestRule.launchActivity(Intent())

        Log.d(tag, "ensure user1 is not a friend of test user")
        db.collection("users").document("FI03lI0G9qbTaBIg385X4BoIDw23").collection("friends").document("user1").delete()
    }


    @Test
    fun addNewFriendTest() {
        val appCompatButton = onView(
                allOf(withId(R.id.log_in_button_dialog),
                        isDisplayed()))
        appCompatButton.perform(click())

        // Added a sleep statement to match the app's execution delay.
        // The recommended way to handle such scenarios is to use Espresso idling resources:
        // https://google.github.io/android-testing-support-library/docs/espresso/idling-resource/index.html
        try {
            Thread.sleep(7000)
        } catch (e: InterruptedException) {
            e.printStackTrace()
        }

        val appCompatEditText2 = onView(
                allOf(withId(R.id.email_form),
                        isDisplayed()))
        appCompatEditText2.perform(replaceText("test@email.com"), closeSoftKeyboard())

        val appCompatEditText4 = onView(
                allOf(withId(R.id.password_form),
                        isDisplayed()))
        appCompatEditText4.perform(replaceText("password"), closeSoftKeyboard())

        val appCompatButton2 = onView(
                allOf(withId(android.R.id.button1)))
        appCompatButton2.perform(scrollTo(), click())

        // Added a sleep statement to match the app's execution delay.
        // The recommended way to handle such scenarios is to use Espresso idling resources:
        // https://google.github.io/android-testing-support-library/docs/espresso/idling-resource/index.html
        try {
            Thread.sleep(7000)
        } catch (e: InterruptedException) {
            e.printStackTrace()
        }

        val bottomNavigationItemView = onView(
                allOf(withId(R.id.navigation_bank),
                        isDisplayed()))
        bottomNavigationItemView.perform(click())

        val appCompatButton3 = onView(
                allOf(withId(R.id.button_send_coin),
                        isDisplayed()))
        appCompatButton3.perform(click())

        // Added a sleep statement to match the app's execution delay.
        // The recommended way to handle such scenarios is to use Espresso idling resources:
        // https://google.github.io/android-testing-support-library/docs/espresso/idling-resource/index.html
        try {
            Thread.sleep(7000)
        } catch (e: InterruptedException) {
            e.printStackTrace()
        }

        //        ViewInteraction appCompatEditText5 = onView(
        //                allOf(withId(R.id.fieldUsername),
        //                        childAtPosition(
        //                                childAtPosition(
        //                                        withClassName(is("android.support.design.widget.CoordinatorLayout")),
        //                                        1),
        //                                3),
        //                        isDisplayed()));
        //        appCompatEditText5.perform(replaceText("user2"), closeSoftKeyboard());
        //
        //        ViewInteraction appCompatButton4 = onView(
        //                allOf(withId(R.id.button_add_friend), withText("add new friend"),
        //                        childAtPosition(
        //                                childAtPosition(
        //                                        withClassName(is("android.support.design.widget.CoordinatorLayout")),
        //                                        1),
        //                                2),
        //                        isDisplayed()));
        //        appCompatButton4.perform(click());
        //
        //        ViewInteraction appCompatEditText6 = onView(
        //                allOf(withId(R.id.fieldUsername), withText("user2"),
        //                        childAtPosition(
        //                                childAtPosition(
        //                                        withClassName(is("android.support.design.widget.CoordinatorLayout")),
        //                                        1),
        //                                3),
        //                        isDisplayed()));
        //        appCompatEditText6.perform(replaceText("user1"));

        val appCompatEditText7 = onView(
                allOf(withId(R.id.fieldUsername),
                        isDisplayed()))
        appCompatEditText7.perform(replaceText("user1"), closeSoftKeyboard())

        val appCompatButton5 = onView(
                allOf(withId(R.id.button_add_friend),
                        isDisplayed()))
        appCompatButton5.perform(click())

        val appCompatEditText8 = onView(
                allOf(withId(R.id.fieldUsername), withText("user1"),
                        isDisplayed()))
        appCompatEditText8.perform(replaceText(""))

        val appCompatEditText9 = onView(
                allOf(withId(R.id.fieldUsername),
                        isDisplayed()))
        appCompatEditText9.perform(closeSoftKeyboard())

    }

}
