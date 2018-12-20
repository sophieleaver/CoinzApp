//package com.example.sophie.coinzapp
//
//import android.content.Intent
//import android.support.test.espresso.Espresso
//import android.support.test.espresso.action.ViewActions
//import android.support.test.espresso.assertion.ViewAssertions
//import android.support.test.espresso.matcher.ViewMatchers
//import android.support.test.filters.LargeTest
//import android.support.test.rule.ActivityTestRule
//import android.support.test.rule.GrantPermissionRule
//import android.support.test.runner.AndroidJUnit4
//import android.util.Log
//import android.view.View
//import com.example.sophie.coinzapp.R.id.withText
//import com.google.firebase.auth.FirebaseAuth
//import com.google.firebase.firestore.FirebaseFirestore
//import org.hamcrest.Matchers
//import org.hamcrest.core.IsInstanceOf
//import org.junit.Before
//import org.junit.Rule
//import org.junit.Test
//import org.junit.runner.RunWith
//import java.util.regex.Pattern.matches
//
///**
// * Tests that on attempted purchase of a map set (in this case, the dark map style) that the user's
// * gold total in bank is not decreased if there is insufficient gold in bank.
// */
//
//@LargeTest
//@RunWith(AndroidJUnit4::class)
//class CreateNewUserTest {
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
//    private val db = FirebaseFirestore.getInstance()
//    private val auth = FirebaseAuth.getInstance()
//    private val tag = "CreateNewUserTest"
//
//    @Before
//    fun createNewUser(){
//        val testCoin = HashMap<String, Any?>()
//        testCoin.put("currency", "DOLR")
//        testCoin.put("value", 1.10)
//
//        //add a coin to the test user's wallet for testing
//        db.collection("user").document("FI03lI0G9qbTaBIg385X4BoIDw23").collection("wallet")
//                .document("testCoin").set(testCoin)
//        //delete user1 from the test user's friends
//        db.collection("user").document("FI03lI0G9qbTaBIg385X4BoIDw23").collection("friends")
//                .document("user1").delete()
//        auth.signOut()
//        mActivityTestRule.launchActivity(Intent())
//    }
//
//    @Test
//    fun purchaseCoinStyleTest() {
//
//        //log test user in
//        Thread.sleep(7000)
//
//        val appCompatButton = Espresso.onView(
//                Matchers.allOf(ViewMatchers.withId(R.id.log_in_button_dialog), ViewMatchers.withText("log me in me hearty"),
//                        ViewMatchers.isDisplayed()))
//        appCompatButton.perform(ViewActions.click())
//
//        Thread.sleep(3000)
//
//        val appCompatEditText3 = Espresso.onView(
//                Matchers.allOf(ViewMatchers.withId(R.id.email_form),
//                        ViewMatchers.isDisplayed()))
//        appCompatEditText3.perform(ViewActions.replaceText("test@email.com"), ViewActions.closeSoftKeyboard())
//
//        val appCompatEditText4 = Espresso.onView(
//                Matchers.allOf(ViewMatchers.withId(R.id.password_form),
//                        ViewMatchers.isDisplayed()))
//        appCompatEditText4.perform(ViewActions.replaceText("password"), ViewActions.closeSoftKeyboard())
//
//        val appCompatButton2 = Espresso.onView(
//                Matchers.allOf(ViewMatchers.withId(android.R.id.button1), ViewMatchers.withText("Log In")))
//        appCompatButton2.perform(ViewActions.scrollTo(), ViewActions.click())
//
//        //navigate to bank
//        Thread.sleep(4000)
//
//        val bottomNavigationItemView = Espresso.onView(
//                Matchers.allOf(ViewMatchers.withId(R.id.navigation_bank)))
//        bottomNavigationItemView.perform(ViewActions.click())
//
//        val appCompatButton3 = Espresso.onView(
//                Matchers.allOf(ViewMatchers.withId(R.id.button_send_coin),
//                        ViewMatchers.isDisplayed()))
//        appCompatButton3.perform(ViewActions.click())
//
//        Thread.sleep(5000)
//
//        val textView = Espresso.onView(
//                Matchers.allOf(ViewMatchers.withId(R.id.usernameView), //withText("user1"),
//                        ViewMatchers.isDisplayed()))
//        textView.check(matches(withText("user1")))
//    }
//
//}