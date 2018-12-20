//package com.example.sophie.coinzapp;
//
//
//import android.support.test.espresso.ViewInteraction;
//import android.support.test.rule.ActivityTestRule;
//import android.support.test.runner.AndroidJUnit4;
//import android.test.suitebuilder.annotation.LargeTest;
//import android.util.Log;
//import android.view.View;
//import android.view.ViewGroup;
//import android.view.ViewParent;
//
//import com.google.firebase.firestore.FirebaseFirestore;
//
//import org.hamcrest.Description;
//import org.hamcrest.Matcher;
//import org.hamcrest.TypeSafeMatcher;
//import org.junit.Before;
//import org.junit.Rule;
//import org.junit.Test;
//import org.junit.runner.RunWith;
//
//import static android.support.test.espresso.Espresso.onView;
//import static android.support.test.espresso.action.ViewActions.click;
//import static android.support.test.espresso.action.ViewActions.closeSoftKeyboard;
//import static android.support.test.espresso.action.ViewActions.replaceText;
//import static android.support.test.espresso.action.ViewActions.scrollTo;
//import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
//import static android.support.test.espresso.matcher.ViewMatchers.withClassName;
//import static android.support.test.espresso.matcher.ViewMatchers.withId;
//import static android.support.test.espresso.matcher.ViewMatchers.withText;
//import static org.hamcrest.Matchers.allOf;
//import static org.hamcrest.Matchers.is;
//
//@LargeTest
//@RunWith(AndroidJUnit4.class)
//public class AddNewFriendTest {
//
//    @Rule
//    public ActivityTestRule<LoginActivity> mActivityTestRule = new ActivityTestRule<>(LoginActivity.class);
//
//    @Before
//    fun loginTestUser(){
//        Log.d(tag, "updating test user gold balance to 24,000")
//        db.collection("users").document("FI03lI0G9qbTaBIg385X4BoIDw23").update("goldInBank", 24000).addOnCompleteListener {
//            Log.d(tag, "goldInBank value successfully updated")
//        }
//
//        Log.d("tag", "set dark map style to unpurchased at start")
//        val db = FirebaseFirestore.getInstance()
//        db.collection("users").document("FI03lI0G9qbTaBIg385X4BoIDw23").collection("purchasedCoins")
//                .document("pale").update("purchased", false).addOnSuccessListener {
//            Log.d("tag", "pale coins set as unpurchased")
//        }
//
//        auth.signOut()
//        mActivityTestRule.launchActivity(Intent())
//    }
//
//    @Test
//    public void addNewFriendTest() {
//        ViewInteraction appCompatButton = onView(
//                allOf(withId(R.id.log_in_button_dialog),
//                        isDisplayed()));
//        appCompatButton.perform(click());
//
//        // Added a sleep statement to match the app's execution delay.
//        // The recommended way to handle such scenarios is to use Espresso idling resources:
//        // https://google.github.io/android-testing-support-library/docs/espresso/idling-resource/index.html
//        try {
//            Thread.sleep(7000);
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }
//
//        ViewInteraction appCompatEditText2 = onView(
//                allOf(withId(R.id.email_form),
//                        isDisplayed()));
//        appCompatEditText2.perform(replaceText("test@email.com"), closeSoftKeyboard());
//
//        ViewInteraction appCompatEditText4 = onView(
//                allOf(withId(R.id.password_form),
//                        isDisplayed()));
//        appCompatEditText4.perform(replaceText("password"), closeSoftKeyboard());
//
//        ViewInteraction appCompatButton2 = onView(
//                allOf(withId(android.R.id.button1)));
//        appCompatButton2.perform(scrollTo(), click());
//
//        // Added a sleep statement to match the app's execution delay.
//        // The recommended way to handle such scenarios is to use Espresso idling resources:
//        // https://google.github.io/android-testing-support-library/docs/espresso/idling-resource/index.html
//        try {
//            Thread.sleep(7000);
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }
//
//        ViewInteraction bottomNavigationItemView = onView(
//                allOf(withId(R.id.navigation_bank),
//                        isDisplayed()));
//        bottomNavigationItemView.perform(click());
//
//        ViewInteraction appCompatButton3 = onView(
//                allOf(withId(R.id.button_send_coin),
//                        isDisplayed()));
//        appCompatButton3.perform(click());
//
//        // Added a sleep statement to match the app's execution delay.
//        // The recommended way to handle such scenarios is to use Espresso idling resources:
//        // https://google.github.io/android-testing-support-library/docs/espresso/idling-resource/index.html
//        try {
//            Thread.sleep(7000);
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }
//
////        ViewInteraction appCompatEditText5 = onView(
////                allOf(withId(R.id.fieldUsername),
////                        childAtPosition(
////                                childAtPosition(
////                                        withClassName(is("android.support.design.widget.CoordinatorLayout")),
////                                        1),
////                                3),
////                        isDisplayed()));
////        appCompatEditText5.perform(replaceText("user2"), closeSoftKeyboard());
////
////        ViewInteraction appCompatButton4 = onView(
////                allOf(withId(R.id.button_add_friend), withText("add new friend"),
////                        childAtPosition(
////                                childAtPosition(
////                                        withClassName(is("android.support.design.widget.CoordinatorLayout")),
////                                        1),
////                                2),
////                        isDisplayed()));
////        appCompatButton4.perform(click());
////
////        ViewInteraction appCompatEditText6 = onView(
////                allOf(withId(R.id.fieldUsername), withText("user2"),
////                        childAtPosition(
////                                childAtPosition(
////                                        withClassName(is("android.support.design.widget.CoordinatorLayout")),
////                                        1),
////                                3),
////                        isDisplayed()));
////        appCompatEditText6.perform(replaceText("user1"));
//
//        ViewInteraction appCompatEditText7 = onView(
//                allOf(withId(R.id.fieldUsername),
//                        isDisplayed()));
//        appCompatEditText7.perform(replaceText("user1"), closeSoftKeyboard());
//
//        ViewInteraction appCompatButton5 = onView(
//                allOf(withId(R.id.button_add_friend),
//                        isDisplayed()));
//        appCompatButton5.perform(click());
//
//        ViewInteraction appCompatEditText8 = onView(
//                allOf(withId(R.id.fieldUsername), withText("user1"),
//                        isDisplayed()));
//        appCompatEditText8.perform(replaceText(""));
//
//        ViewInteraction appCompatEditText9 = onView(
//                allOf(withId(R.id.fieldUsername),
//                        isDisplayed()));
//        appCompatEditText9.perform(closeSoftKeyboard());
//
//    }
//
//}
