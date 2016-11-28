/*
 * Copyright (C) 2016 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package android.support.design.widget;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.swipeUp;
import static android.support.test.espresso.matcher.ViewMatchers.withId;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.same;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import android.app.Instrumentation;
import android.graphics.Rect;
import android.support.design.test.R;
import android.support.design.testutils.CoordinatorLayoutUtils;
import android.support.design.widget.CoordinatorLayout.Behavior;
import android.support.test.InstrumentationRegistry;
import android.support.test.filters.MediumTest;
import android.support.test.filters.SdkSuppress;
import android.support.v4.view.ViewCompat;
import android.support.v4.view.WindowInsetsCompat;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.MeasureSpec;
import android.widget.ImageView;

import org.junit.Before;
import org.junit.Test;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

@MediumTest
public class CoordinatorLayoutTest extends BaseInstrumentationTestCase<CoordinatorLayoutActivity> {

    private Instrumentation mInstrumentation;

    public CoordinatorLayoutTest() {
        super(CoordinatorLayoutActivity.class);
    }

    @Before
    public void setup() {
        mInstrumentation = InstrumentationRegistry.getInstrumentation();
    }

    @Test
    @SdkSuppress(minSdkVersion = 21)
    public void testSetFitSystemWindows() throws Throwable {
        final Instrumentation instrumentation = InstrumentationRegistry.getInstrumentation();
        final CoordinatorLayout col = mActivityTestRule.getActivity().mCoordinatorLayout;
        final View view = new View(col.getContext());

        // Create a mock which calls the default impl of onApplyWindowInsets()
        final CoordinatorLayout.Behavior<View> mockBehavior =
                mock(CoordinatorLayout.Behavior.class);
        doCallRealMethod().when(mockBehavior)
                .onApplyWindowInsets(same(col), same(view), any(WindowInsetsCompat.class));

        // Assert that the CoL is currently not set to fitSystemWindows
        assertFalse(col.getFitsSystemWindows());

        // Now add a view with our mocked behavior to the CoordinatorLayout
        view.setFitsSystemWindows(true);
        mActivityTestRule.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                final CoordinatorLayout.LayoutParams lp = col.generateDefaultLayoutParams();
                lp.setBehavior(mockBehavior);
                col.addView(view, lp);
            }
        });
        instrumentation.waitForIdleSync();

        // Now request some insets and wait for the pass to happen
        mActivityTestRule.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                col.requestApplyInsets();
            }
        });
        instrumentation.waitForIdleSync();

        // Verify that onApplyWindowInsets() has not been called
        verify(mockBehavior, never())
                .onApplyWindowInsets(same(col), same(view), any(WindowInsetsCompat.class));

        // Now enable fits system windows and wait for a pass to happen
        mActivityTestRule.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                col.setFitsSystemWindows(true);
            }
        });
        instrumentation.waitForIdleSync();

        // Verify that onApplyWindowInsets() has been called with some insets
        verify(mockBehavior, atLeastOnce())
                .onApplyWindowInsets(same(col), same(view), any(WindowInsetsCompat.class));
    }

    @Test
    public void testInsetDependency() {
        final CoordinatorLayout col = mActivityTestRule.getActivity().mCoordinatorLayout;

        final CoordinatorLayout.LayoutParams lpInsetLeft = col.generateDefaultLayoutParams();
        lpInsetLeft.insetEdge = Gravity.LEFT;

        final CoordinatorLayout.LayoutParams lpInsetRight = col.generateDefaultLayoutParams();
        lpInsetRight.insetEdge = Gravity.RIGHT;

        final CoordinatorLayout.LayoutParams lpInsetTop = col.generateDefaultLayoutParams();
        lpInsetTop.insetEdge = Gravity.TOP;

        final CoordinatorLayout.LayoutParams lpInsetBottom = col.generateDefaultLayoutParams();
        lpInsetBottom.insetEdge = Gravity.BOTTOM;

        final CoordinatorLayout.LayoutParams lpDodgeLeft = col.generateDefaultLayoutParams();
        lpDodgeLeft.dodgeInsetEdges = Gravity.LEFT;

        final CoordinatorLayout.LayoutParams lpDodgeLeftAndTop = col.generateDefaultLayoutParams();
        lpDodgeLeftAndTop.dodgeInsetEdges = Gravity.LEFT | Gravity.TOP;

        final CoordinatorLayout.LayoutParams lpDodgeAll = col.generateDefaultLayoutParams();
        lpDodgeAll.dodgeInsetEdges = Gravity.FILL;

        final View a = new View(col.getContext());
        final View b = new View(col.getContext());

        assertThat(dependsOn(lpDodgeLeft, lpInsetLeft, col, a, b), is(true));
        assertThat(dependsOn(lpDodgeLeft, lpInsetRight, col, a, b), is(false));
        assertThat(dependsOn(lpDodgeLeft, lpInsetTop, col, a, b), is(false));
        assertThat(dependsOn(lpDodgeLeft, lpInsetBottom, col, a, b), is(false));

        assertThat(dependsOn(lpDodgeLeftAndTop, lpInsetLeft, col, a, b), is(true));
        assertThat(dependsOn(lpDodgeLeftAndTop, lpInsetRight, col, a, b), is(false));
        assertThat(dependsOn(lpDodgeLeftAndTop, lpInsetTop, col, a, b), is(true));
        assertThat(dependsOn(lpDodgeLeftAndTop, lpInsetBottom, col, a, b), is(false));

        assertThat(dependsOn(lpDodgeAll, lpInsetLeft, col, a, b), is(true));
        assertThat(dependsOn(lpDodgeAll, lpInsetRight, col, a, b), is(true));
        assertThat(dependsOn(lpDodgeAll, lpInsetTop, col, a, b), is(true));
        assertThat(dependsOn(lpDodgeAll, lpInsetBottom, col, a, b), is(true));

        assertThat(dependsOn(lpInsetLeft, lpDodgeLeft, col, a, b), is(false));
    }

    private static boolean dependsOn(CoordinatorLayout.LayoutParams lpChild,
            CoordinatorLayout.LayoutParams lpDependency, CoordinatorLayout col,
            View child, View dependency) {
        child.setLayoutParams(lpChild);
        dependency.setLayoutParams(lpDependency);
        return lpChild.dependsOn(col, child, dependency);
    }

    @Test
    public void testInsetEdge() throws Throwable {
        final Instrumentation instrumentation = InstrumentationRegistry.getInstrumentation();
        final CoordinatorLayout col = mActivityTestRule.getActivity().mCoordinatorLayout;

        final View insetView = new View(col.getContext());
        final View dodgeInsetView = new View(col.getContext());
        final AtomicInteger originalTop = new AtomicInteger();

        mActivityTestRule.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                CoordinatorLayout.LayoutParams lpInsetView = col.generateDefaultLayoutParams();
                lpInsetView.width = CoordinatorLayout.LayoutParams.MATCH_PARENT;
                lpInsetView.height = 100;
                lpInsetView.gravity = Gravity.TOP | Gravity.LEFT;
                lpInsetView.insetEdge = Gravity.TOP;
                col.addView(insetView, lpInsetView);
                insetView.setBackgroundColor(0xFF0000FF);

                CoordinatorLayout.LayoutParams lpDodgeInsetView = col.generateDefaultLayoutParams();
                lpDodgeInsetView.width = 100;
                lpDodgeInsetView.height = 100;
                lpDodgeInsetView.gravity = Gravity.TOP | Gravity.LEFT;
                lpDodgeInsetView.dodgeInsetEdges = Gravity.TOP;
                col.addView(dodgeInsetView, lpDodgeInsetView);
                dodgeInsetView.setBackgroundColor(0xFFFF0000);
            }
        });
        instrumentation.waitForIdleSync();
        mActivityTestRule.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                List<View> dependencies = col.getDependencies(dodgeInsetView);
                assertThat(dependencies.size(), is(1));
                assertThat(dependencies.get(0), is(insetView));

                // Move the insetting view
                originalTop.set(dodgeInsetView.getTop());
                assertThat(originalTop.get(), is(insetView.getBottom()));
                ViewCompat.offsetTopAndBottom(insetView, 123);
            }
        });
        instrumentation.waitForIdleSync();
        mActivityTestRule.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                // Confirm that the dodging view was moved by the same size
                assertThat(dodgeInsetView.getTop() - originalTop.get(), is(123));
            }
        });
    }

    @Test
    public void testDependentViewChanged() throws Throwable {
        final Instrumentation instrumentation = InstrumentationRegistry.getInstrumentation();
        final CoordinatorLayout col = mActivityTestRule.getActivity().mCoordinatorLayout;

        // Add two views, A & B, where B depends on A
        final View viewA = new View(col.getContext());
        final CoordinatorLayout.LayoutParams lpA = col.generateDefaultLayoutParams();
        lpA.width = 100;
        lpA.height = 100;

        final View viewB = new View(col.getContext());
        final CoordinatorLayout.LayoutParams lpB = col.generateDefaultLayoutParams();
        lpB.width = 100;
        lpB.height = 100;
        final CoordinatorLayout.Behavior behavior =
                spy(new CoordinatorLayoutUtils.DependentBehavior(viewA));
        lpB.setBehavior(behavior);

        mActivityTestRule.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                col.addView(viewA, lpA);
                col.addView(viewB, lpB);
            }
        });
        instrumentation.waitForIdleSync();

        // Reset the Behavior since onDependentViewChanged may have already been called as part of
        // any layout/draw passes already
        reset(behavior);

        // Now offset view A
        mActivityTestRule.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                ViewCompat.offsetLeftAndRight(viewA, 20);
                ViewCompat.offsetTopAndBottom(viewA, 20);
            }
        });
        instrumentation.waitForIdleSync();

        // And assert that view B's Behavior was called appropriately
        verify(behavior, times(1)).onDependentViewChanged(col, viewB, viewA);
    }

    @Test
    public void testDependentViewRemoved() throws Throwable {
        final Instrumentation instrumentation = InstrumentationRegistry.getInstrumentation();
        final CoordinatorLayout col = mActivityTestRule.getActivity().mCoordinatorLayout;

        // Add two views, A & B, where B depends on A
        final View viewA = new View(col.getContext());
        final View viewB = new View(col.getContext());
        final CoordinatorLayout.LayoutParams lpB = col.generateDefaultLayoutParams();
        final CoordinatorLayout.Behavior behavior =
                spy(new CoordinatorLayoutUtils.DependentBehavior(viewA));
        lpB.setBehavior(behavior);

        mActivityTestRule.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                col.addView(viewA);
                col.addView(viewB, lpB);
            }
        });
        instrumentation.waitForIdleSync();

        // Now remove view A
        mActivityTestRule.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                col.removeView(viewA);
            }
        });

        // And assert that View B's Behavior was called appropriately
        verify(behavior, times(1)).onDependentViewRemoved(col, viewB, viewA);
    }

    @Test
    public void testGetDependenciesAfterDependentViewRemoved() throws Throwable {
        final Instrumentation instrumentation = InstrumentationRegistry.getInstrumentation();
        final CoordinatorLayout col = mActivityTestRule.getActivity().mCoordinatorLayout;

        // Add two views, A & B, where B depends on A
        final View viewA = new View(col.getContext());
        final View viewB = new View(col.getContext());
        final CoordinatorLayout.LayoutParams lpB = col.generateDefaultLayoutParams();
        final CoordinatorLayout.Behavior behavior
                = new CoordinatorLayoutUtils.DependentBehavior(viewA) {
            @Override
            public void onDependentViewRemoved(CoordinatorLayout parent, View child,
                    View dependency) {
                parent.getDependencies(child);
            }
        };
        lpB.setBehavior(behavior);

        // Now add views
        mActivityTestRule.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                col.addView(viewA);
                col.addView(viewB, lpB);
            }
        });

        // Wait for a layout
        instrumentation.waitForIdleSync();

        // Now remove view A, which will trigger onDependentViewRemoved() on view B's behavior
        mActivityTestRule.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                col.removeView(viewA);
            }
        });
    }

    @Test
    public void testDodgeInsetBeforeLayout() throws Throwable {
        final CoordinatorLayout col = mActivityTestRule.getActivity().mCoordinatorLayout;

        // Add a dummy view, which will be used to trigger a hierarchy change.
        final View dummy = new View(col.getContext());

        mActivityTestRule.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                col.addView(dummy);
            }
        });

        // Wait for a layout.
        mInstrumentation.waitForIdleSync();

        final View dodge = new View(col.getContext());
        final CoordinatorLayout.LayoutParams lpDodge = col.generateDefaultLayoutParams();
        lpDodge.dodgeInsetEdges = Gravity.BOTTOM;
        lpDodge.setBehavior(new Behavior() {
            @Override
            public boolean getInsetDodgeRect(CoordinatorLayout parent, View child, Rect rect) {
                // Any non-empty rect is fine here.
                rect.set(0, 0, 10, 10);
                return true;
            }
        });

        mActivityTestRule.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                col.addView(dodge, lpDodge);

                // Ensure the new view is in the list of children.
                int heightSpec = MeasureSpec.makeMeasureSpec(col.getHeight(), MeasureSpec.EXACTLY);
                int widthSpec = MeasureSpec.makeMeasureSpec(col.getWidth(), MeasureSpec.EXACTLY);
                col.measure(widthSpec, heightSpec);

                // Force a hierarchy change.
                col.removeView(dummy);
            }
        });

        // Wait for a layout.
        mInstrumentation.waitForIdleSync();
    }

    @Test
    public void testGoneViewsNotMeasuredLaidOut() throws Throwable {
        final CoordinatorLayoutActivity activity = mActivityTestRule.getActivity();
        final CoordinatorLayout col = activity.mCoordinatorLayout;

        // Now create a GONE view and add it to the CoordinatorLayout
        final View imageView = new View(activity);
        imageView.setVisibility(View.GONE);
        mActivityTestRule.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                col.addView(imageView, 200, 200);
            }
        });
        // Wait for a layout and measure pass
        mInstrumentation.waitForIdleSync();

        // And assert that it has not been laid out
        assertFalse(imageView.getMeasuredWidth() > 0);
        assertFalse(imageView.getMeasuredHeight() > 0);
        assertFalse(ViewCompat.isLaidOut(imageView));

        // Now set the view to INVISIBLE
        mActivityTestRule.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                imageView.setVisibility(View.INVISIBLE);
            }
        });
        // Wait for a layout and measure pass
        mInstrumentation.waitForIdleSync();

        // And assert that it has been laid out
        assertTrue(imageView.getMeasuredWidth() > 0);
        assertTrue(imageView.getMeasuredHeight() > 0);
        assertTrue(ViewCompat.isLaidOut(imageView));
    }

    @Test
    public void testNestedScrollingDispatchesToBehavior() throws Throwable {
        final CoordinatorLayoutActivity activity = mActivityTestRule.getActivity();
        final CoordinatorLayout col = activity.mCoordinatorLayout;

        // Now create a view and add it to the CoordinatorLayout with the spy behavior,
        // along with a NestedScrollView
        final ImageView imageView = new ImageView(activity);
        final CoordinatorLayout.Behavior behavior = spy(new NestedScrollingBehavior());
        mActivityTestRule.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                LayoutInflater.from(activity).inflate(R.layout.include_nestedscrollview, col, true);

                CoordinatorLayout.LayoutParams clp = new CoordinatorLayout.LayoutParams(200, 200);
                clp.setBehavior(behavior);
                col.addView(imageView, clp);
            }
        });

        // Now vertically swipe up on the NSV, causing nested scrolling to occur
        onView(withId(R.id.nested_scrollview)).perform(swipeUp());

        // Verify that the Behavior's onStartNestedScroll was called once
        verify(behavior, times(1)).onStartNestedScroll(
                eq(col), // parent
                eq(imageView), // child
                any(View.class), // target
                any(View.class), // direct child target
                any(int.class)); // axes

        // Verify that the Behavior's onNestedScrollAccepted was called once
        verify(behavior, times(1)).onNestedScrollAccepted(
                eq(col), // parent
                eq(imageView), // child
                any(View.class), // target
                any(View.class), // direct child target
                any(int.class)); // axes

        // Verify that the Behavior's onNestedPreScroll was called at least once
        verify(behavior, atLeastOnce()).onNestedPreScroll(
                eq(col), // parent
                eq(imageView), // child
                any(View.class), // target
                any(int.class), // dx
                any(int.class), // dy
                any(int[].class)); // consumed

        // Verify that the Behavior's onNestedScroll was called at least once
        verify(behavior, atLeastOnce()).onNestedScroll(
                eq(col), // parent
                eq(imageView), // child
                any(View.class), // target
                any(int.class), // dx consumed
                any(int.class), // dy consumed
                any(int.class), // dx unconsumed
                any(int.class)); // dy unconsumed

        // Verify that the Behavior's onStopNestedScroll was called once
        verify(behavior, times(1)).onStopNestedScroll(
                eq(col), // parent
                eq(imageView), // child
                any(View.class)); // target
    }

    @Test
    public void testNestedScrollingDispatchingToBehaviorWithGoneView() throws Throwable {
        final CoordinatorLayoutActivity activity = mActivityTestRule.getActivity();
        final CoordinatorLayout col = activity.mCoordinatorLayout;

        // Now create a GONE view and add it to the CoordinatorLayout with the spy behavior,
        // along with a NestedScrollView
        final ImageView imageView = new ImageView(activity);
        imageView.setVisibility(View.GONE);
        final CoordinatorLayout.Behavior behavior = spy(new NestedScrollingBehavior());
        mActivityTestRule.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                LayoutInflater.from(activity).inflate(R.layout.include_nestedscrollview, col, true);

                CoordinatorLayout.LayoutParams clp = new CoordinatorLayout.LayoutParams(200, 200);
                clp.setBehavior(behavior);
                col.addView(imageView, clp);
            }
        });

        // Now vertically swipe up on the NSV, causing nested scrolling to occur
        onView(withId(R.id.nested_scrollview)).perform(swipeUp());

        // Verify that the Behavior's onStartNestedScroll was not called
        verify(behavior, never()).onStartNestedScroll(
                eq(col), // parent
                eq(imageView), // child
                any(View.class), // target
                any(View.class), // direct child target
                any(int.class)); // axes

        // Verify that the Behavior's onNestedScrollAccepted was not called
        verify(behavior, never()).onNestedScrollAccepted(
                eq(col), // parent
                eq(imageView), // child
                any(View.class), // target
                any(View.class), // direct child target
                any(int.class)); // axes

        // Verify that the Behavior's onNestedPreScroll was not called
        verify(behavior, never()).onNestedPreScroll(
                eq(col), // parent
                eq(imageView), // child
                any(View.class), // target
                any(int.class), // dx
                any(int.class), // dy
                any(int[].class)); // consumed

        // Verify that the Behavior's onNestedScroll was not called
        verify(behavior, never()).onNestedScroll(
                eq(col), // parent
                eq(imageView), // child
                any(View.class), // target
                any(int.class), // dx consumed
                any(int.class), // dy consumed
                any(int.class), // dx unconsumed
                any(int.class)); // dy unconsumed

        // Verify that the Behavior's onStopNestedScroll was not called
        verify(behavior, never()).onStopNestedScroll(
                eq(col), // parent
                eq(imageView), // child
                any(View.class)); // target
    }

    @Test
    public void testDispatchingTouchEventsToBehaviorWithGoneView() throws Throwable {
        final CoordinatorLayoutActivity activity = mActivityTestRule.getActivity();
        final CoordinatorLayout col = activity.mCoordinatorLayout;

        // Now create a GONE view and add it to the CoordinatorLayout with the spy behavior
        final ImageView imageView = new ImageView(activity);
        imageView.setVisibility(View.GONE);
        final CoordinatorLayout.Behavior behavior = spy(new NestedScrollingBehavior());
        mActivityTestRule.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                CoordinatorLayout.LayoutParams clp = new CoordinatorLayout.LayoutParams(200, 200);
                clp.setBehavior(behavior);
                col.addView(imageView, clp);
            }
        });

        // Now click on the coordinator layout
        onView(withId(R.id.coordinator)).perform(click());

        // And verify that the Behavior for the hidden view did not receive any touch events
        verify(behavior, never())
                .onInterceptTouchEvent(same(col), same(imageView), any(MotionEvent.class));
        verify(behavior, never())
                .onTouchEvent(same(col), same(imageView), any(MotionEvent.class));
    }

    public static class NestedScrollingBehavior extends CoordinatorLayout.Behavior<ImageView> {
        @Override
        public boolean onStartNestedScroll(CoordinatorLayout coordinatorLayout, ImageView child,
                View directTargetChild, View target, int nestedScrollAxes) {
            // Return true so that we always accept nested scroll events
            return true;
        }
    }
}
