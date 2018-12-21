//package com.example.sophie.coinzapp
//
//
//import android.content.Intent
//import android.support.test.espresso.Espresso.onView
//import android.support.test.espresso.action.ViewActions.*
//import android.support.test.espresso.assertion.ViewAssertions.matches
//import android.support.test.espresso.matcher.ViewMatchers.*
//import android.support.test.filters.LargeTest
//import android.support.test.rule.ActivityTestRule
//import android.support.test.rule.GrantPermissionRule
//import android.support.test.runner.AndroidJUnit4
//import android.view.View
//import android.view.ViewGroup
//import com.google.firebase.auth.FirebaseAuth
//import com.google.firebase.firestore.FirebaseFirestore
//import org.hamcrest.Description
//import org.hamcrest.Matcher
//import org.hamcrest.Matchers.`is`
//import org.hamcrest.Matchers.allOf
//import org.hamcrest.TypeSafeMatcher
//import org.junit.Before
//import org.junit.Rule
//import org.junit.Test
//import org.junit.runner.RunWith
//
//@LargeTest
//@RunWith(AndroidJUnit4::class)
//class AcceptGiftTestCoinLimitExceeded {
//
//    /**
//     * Tests whether user's gold in bank does not increase when a gift is accepted
//     * when the user has collected more than 25 coins.
//     */
//
//    @Rule
//    @JvmField
//    var mActivityTestRule = ActivityTestRule(LoginActivity::class.java)
//
//
//    @Rule
//    @JvmField
//    var mGrantPermissionRule =
//            GrantPermissionRule.grant(
//                    "android.permission.ACCESS_FINE_LOCATION")
//
//    private val auth = FirebaseAuth.getInstance()
//    private val db = FirebaseFirestore.getInstance()
//    private var coinGoldValue = 0
//
//    @Before
//    fun createAndLoginNewUser(){
//        auth.signOut()
//        mActivityTestRule.launchActivity(Intent())
//
//        Thread.sleep(10000)
//        //set user gold in bank to 0
//        db.collection("users").document("FI03lI0G9qbTaBIg385X4BoIDw23").apply {
//            update("goldInBank", 0)
//            update("dailyCoinsCollected",25)
//        }
//
//        //add a test coin to unaccepted coins (the gift to be accepted)
//        val testCoin = HashMap<String, Any?>()
//        testCoin.put("currency", "DOLR")
//        testCoin.put("value", "1.0000")
//        db.collection("users").document("FI03lI0G9qbTaBIg385X4BoIDw23")
//                .collection("unacceptedCoins").document("testCoin").set(testCoin)
//
//    }
//
//    @Test
//    fun acceptGiftCoinLimitExceededTest() {
//        Thread.sleep(3000)
//
//        val appCompatButton = onView(
//                allOf(withId(R.id.log_in_button_dialog),
//                        isDisplayed()))
//        appCompatButton.perform(click())
//
//        Thread.sleep(2000)
//
//        val appCompatEditText = onView(
//                allOf(withId(R.id.email_form),
//                        isDisplayed()))
//        appCompatEditText.perform(replaceText("test@email.com"), closeSoftKeyboard())
//
//        val appCompatEditText2 = onView(
//                allOf(withId(R.id.password_form),
//                        isDisplayed()))
//        appCompatEditText2.perform(replaceText("password"), closeSoftKeyboard())
//
//        val appCompatButton2 = onView(
//                allOf(withId(android.R.id.button1)))
//        appCompatButton2.perform(scrollTo(), click())
//
//        Thread.sleep(3000)
//
//        val bottomNavigationItemView = onView(
//                allOf(withId(R.id.navigation_bank),
//                        isDisplayed()))
//        bottomNavigationItemView.perform(click())
//
//        Thread.sleep(3000)
//
//        //check users bank account has 0 gold
//
//        val textView = onView(
//                allOf(withId(R.id.gold_in_bank_display),
//                        isDisplayed()))
//        textView.check(matches(withText("0")))
//        Thread.sleep(2000)
//        val appCompatButton3 = onView(
//                allOf(withId(R.id.button_view_coin),
//                        isDisplayed()))
//        appCompatButton3.perform(click())
//        Thread.sleep(1000)
//
//        val appCompatButton4 = onView(
//                allOf(withId(android.R.id.button1)))
//        appCompatButton4.perform(scrollTo(), click())
//
//        val constraintLayout = onView(
//                allOf(withId(R.id.linearLayout2),
//                        isDisplayed()))
//        constraintLayout.perform(click())
//
//        Thread.sleep(3000)
//
//        val textView2 = onView(
//                allOf(withId(R.id.gold_in_bank_display),
//                        isDisplayed()))
//        textView2.check(matches(withText("0"))) // check gold in account is still equal to 0
//
//        Thread.sleep(3000)
//
//        mActivityTestRule.launchActivity(Intent())
//        auth.signOut()
//    }
//}
