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
import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import org.hamcrest.Matchers.allOf
import org.junit.*
import org.junit.runner.RunWith

/**
 * Tests that on purchase of a coin set (in this case, the pale coin set) that the user's
 * gold total in bank decreases by 10,000.
 */

@LargeTest
@RunWith(AndroidJUnit4::class)
class PurchaseCoinStyleTest {

    @Rule
    @JvmField
    var mActivityTestRule = ActivityTestRule(LoginActivity::class.java)

    @Rule
    @JvmField
    var mGrantPermissionRule =
            GrantPermissionRule.grant(
                    "android.permission.ACCESS_FINE_LOCATION")

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val tag = "PurchaseCoinStyleTest"

    @Before
    fun loginTestUser(){
        //set test users account to contain 10,000 gold
        Log.d(tag, "updating test user gold balance to 10,000")
        db.collection("users").document("FI03lI0G9qbTaBIg385X4BoIDw23").update("goldInBank", 10000).addOnCompleteListener {
            Log.d(tag, "goldInBank value successfully updated")
        }
        //set test users account to have not already purchased the pale coin set
        Log.d(tag, "set pale coin style to unpurchased started")
        val db = FirebaseFirestore.getInstance()
        db.collection("users").document("FI03lI0G9qbTaBIg385X4BoIDw23").collection("purchasedCoins")
                .document("pale").update("purchased", false).addOnSuccessListener {
                    Log.d("tag", "pale coins set as unpurchased")
                }

        auth.signOut()
        mActivityTestRule.launchActivity(Intent())
    }

    @Test
    fun purchaseCoinStyleTest() {

            // Added a sleep statement to match the app's execution delay.
            // The recommended way to handle such scenarios is to use Espresso idling resources:
            // https://google.github.io/android-testing-support-library/docs/espresso/idling-resource/index.html
            Thread.sleep(7000)

            val appCompatButton = onView(
                    allOf(withId(R.id.log_in_button_dialog), withText("log me in me hearty"),
                            isDisplayed()))
            appCompatButton.perform(click())

            // Added a sleep statement to match the app's execution delay.
            // The recommended way to handle such scenarios is to use Espresso idling resources:
            // https://google.github.io/android-testing-support-library/docs/espresso/idling-resource/index.html
            Thread.sleep(3000)

            val appCompatEditText3 = onView(
                    allOf(withId(R.id.email_form),
                            isDisplayed()))
            appCompatEditText3.perform(replaceText("test@email.com"), closeSoftKeyboard())

            val appCompatEditText4 = onView(
                    allOf(withId(R.id.password_form),
                            isDisplayed()))
            appCompatEditText4.perform(replaceText("password"), closeSoftKeyboard())

            val appCompatButton2 = onView(
                    allOf(withId(android.R.id.button1), withText("Log In")))
            appCompatButton2.perform(scrollTo(), click())

            // Added a sleep statement to match the app's execution delay.
            // The recommended way to handle such scenarios is to use Espresso idling resources:
            // https://google.github.io/android-testing-support-library/docs/espresso/idling-resource/index.html
            Thread.sleep(7000)

            val bottomNavigationItemView = onView(
                    allOf(withId(R.id.navigation_bank)))
            bottomNavigationItemView.perform(click())

            Thread.sleep(7000)
            val textView = onView(
                    allOf(withId(R.id.gold_in_bank_display),// withText("975000"),
                            isDisplayed()))
            textView.check(matches(withText("10000")))

            val bottomNavigationItemView2 = onView(
                    allOf(withId(R.id.navigation_shop),
                            isDisplayed()))
            bottomNavigationItemView2.perform(click())

            val appCompatButton3 = onView(
                    allOf(withId(R.id.button_view_paleCoins), withText("Pale"),
                            isDisplayed()))
            appCompatButton3.perform(click())

            val appCompatButton4 = onView(
                    allOf(withId(android.R.id.button1)))
            appCompatButton4.perform(scrollTo(), click())

            val bottomNavigationItemView3 = onView(
                    allOf(withId(R.id.navigation_bank),
                            isDisplayed()))
            bottomNavigationItemView3.perform(click())
            Thread.sleep(7000)
            val textView3 = onView(
                    allOf(withId(R.id.gold_in_bank_display)))
            textView3.check(matches(withText("0")))

    }

//    @After
//    fun setPaleCoinsAsUnpurchased(){
//        Log.d("tag", "set pale coin style to unpurchased started")
//        val db = FirebaseFirestore.getInstance()
//        db.collection("users").document("FI03lI0G9qbTaBIg385X4BoIDw23").collection("purchasedCoins")
//                .document("pale").update("purchased", false).addOnSuccessListener {
//                    Log.d("tag", "pale coins set as unpurchased")
//                }
//        auth.signOut()
//    }

}