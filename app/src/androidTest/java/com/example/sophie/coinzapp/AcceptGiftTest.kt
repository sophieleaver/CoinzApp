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
//import org.json.JSONObject
//import org.junit.Before
//import org.junit.Rule
//import org.junit.Test
//import org.junit.runner.RunWith
//import java.text.SimpleDateFormat
//import java.util.*
//
//@LargeTest
//@RunWith(AndroidJUnit4::class)
//class AcceptGiftTest {
//
//    /**
//     * Tests whether user's gold in bank increases by amount according to daily exchange rate (this test uses dollar
//     * exchange rate) when the user has collected less than 25 coins on acceptance of a gift.
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
//    private val todayDate = SimpleDateFormat("YYYY/MM/dd").format(Calendar.getInstance().time)
//    val testGeoJsonUrl = "http://homepages.inf.ed.ac.uk/stg/coinz/$todayDate/coinzmap.geojson"
//    val geoJsonDataString = DownloadFileTask(caller = DownloadCompleteRunner).execute(testGeoJsonUrl).get()
//    val todaysRates = JSONObject(geoJsonDataString).getJSONObject("rates")
//    val dolrRate = todaysRates.getDouble("DOLR")
//
//    @Before
//    fun createAndLoginNewUser(){
//        auth.signOut()
//        mActivityTestRule.launchActivity(Intent())
//
//
//        //set user gold in bank to 0
//        db.collection("users").document("FI03lI0G9qbTaBIg385X4BoIDw23")
//                .update("goldInBank", 0)
//        //set user collected coins to 0
//        db.collection("users").document("FI03lI0G9qbTaBIg385X4BoIDw23")
//                .update("dailyCoinsCollected",0)
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
//    fun acceptGiftTest() {
//        Thread.sleep(8000)
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
//        Thread.sleep(4000)
//
//        val bottomNavigationItemView = onView(
//                allOf(withId(R.id.navigation_bank),
//                        isDisplayed()))
//        bottomNavigationItemView.perform(click())
//
//        //check users bank account has 0 gold
//        Thread.sleep(7000)
//
//        val textView = onView(
//                allOf(withId(R.id.gold_in_bank_display),
//                        isDisplayed()))
//        textView.check(matches(withText("0"))) // check gold in bank is equal to 0 before method
//
//        val appCompatButton3 = onView(
//                allOf(withId(R.id.button_view_coin),
//                        isDisplayed()))
//        appCompatButton3.perform(click())
//        Thread.sleep(2000)
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
//        textView2.check(matches(withText("${dolrRate.toInt()}"))) // check new gold in account is equal to 1 * dollar exchange rate
//        Thread.sleep(1000)
//    }
//}
