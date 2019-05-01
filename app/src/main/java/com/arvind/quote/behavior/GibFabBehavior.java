package com.arvind.quote.behavior;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import androidx.recyclerview.widget.RecyclerView;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;

import com.arvind.quote.R;

/**
 * Behavior Class of the FAB (In GibQuoteFragment)
 * Describes what the FAB (the LinearLayout in which it is present, actually) should do
 * in response to changes in the parent CoordinatorLayout
 *
 * Behavior is linked in XML (app:layout_behavior)
 */
public class GibFabBehavior extends CoordinatorLayout.Behavior<LinearLayout> {

    private String TAG = "GibFabBehavior";
    private FloatingActionButton gibQuoteFab;

    // Required Constructor, called on inflating CoordinatorLayout
    public GibFabBehavior(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public boolean layoutDependsOn(CoordinatorLayout parent, LinearLayout child, View dependency) {
        gibQuoteFab = child.findViewById(R.id.gib_quote_fab);
        Log.d(TAG, "dependency : " + dependency.getTag());
        return dependency instanceof Snackbar.SnackbarLayout || dependency instanceof RecyclerView;
    }

    @Override
    public boolean onStartNestedScroll(@NonNull CoordinatorLayout coordinatorLayout, @NonNull LinearLayout child, @NonNull View directTargetChild, @NonNull View target, int axes, int type) {
        // Accept Scrolling behaviors in Descendant View
        // The acceptable descendant in our case being the RecyclerView in which quotes are displayed
        return target instanceof RecyclerView;
    }

    @Override
    public void onNestedScroll(@NonNull CoordinatorLayout coordinatorLayout, @NonNull LinearLayout child, @NonNull View target, int dxConsumed, int dyConsumed, int dxUnconsumed, int dyUnconsumed, int type) {
        super.onNestedScroll(coordinatorLayout, child, target, dxConsumed, dyConsumed, dxUnconsumed, dyUnconsumed, type);
        Log.d(TAG, "Nested scroll on : " + target.getTag());
        if(dxConsumed == 0 | dxUnconsumed == 0)
            gibQuoteFab.hide();
    }

    @Override
    public void onStopNestedScroll(@NonNull CoordinatorLayout coordinatorLayout, @NonNull LinearLayout child, @NonNull View target, int type) {
        super.onStopNestedScroll(coordinatorLayout, child, target, type);
        gibQuoteFab.show();
    }

    @Override
    public boolean onDependentViewChanged(CoordinatorLayout parent, LinearLayout child, View dependency) {
        // getTranslationY -> Gets final Y value to translate to
        // getHeight -> Height of the view
        //
        // Behavior:
        // > Snackbar going down (Final -ve value)** ----v
        // -> getTranslationY = 0 && getHeight = +ve     |
        // --> Difference is -ve                       equal
        // ---> Minimum of 0 & -ve => -ve value          |
        // ----> Fab goes down too! (Final -ve value)** -^
        if(dependency instanceof Snackbar.SnackbarLayout) {
            child.setTranslationY(Math.min(0, dependency.getTranslationY() - dependency.getHeight()));
            // We've modified the one of the child's parameters, so return true
            return true;
        }
        // Done nothing
        return false;
    }

}
