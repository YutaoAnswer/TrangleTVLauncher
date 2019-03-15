package com.triangle.trangletvlauncher.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.widget.HorizontalScrollView;

import com.triangle.trangletvlauncher.R;

public class SmoothHorizontalScrollView extends HorizontalScrollView {

    private final static String TAG = SmoothHorizontalScrollView.class.getSimpleName();

    private int mFadingEdge;

    public SmoothHorizontalScrollView(Context context) {
        super(context);
    }

    public SmoothHorizontalScrollView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public SmoothHorizontalScrollView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.SmoothHorizontalScrollView);
        mFadingEdge = typedArray.getInt(R.styleable.SmoothHorizontalScrollView_fadingEdge, 100);
        typedArray.recycle();
    }

    @Override
    protected int computeScrollDeltaToGetChildRectOnScreen(Rect rect) {
        if (getChildCount() == 0) return 0;
        int width = getWidth();
        int screenLeft = getScrollX();
        int screenRight = screenLeft + width;

        // leave room for left fading edge as long as rect isn't at very left
        if (rect.left > 0) {
            screenLeft += mFadingEdge;
        }

        // leave room for right fading edge as long as rect isn't at very right
        if (rect.right < getChildAt(0).getWidth()) {
            screenRight -= mFadingEdge;
        }

        int scrollXDelta = 0;

        if (rect.right > screenRight && rect.left > screenLeft) {
            // need to move right to get it in view: move right just enough so
            // that the entire rectangle is in view (or at least the first
            // screen size chunk).

            if (rect.width() > width) {
                // just enough to get screen size chunk on
                scrollXDelta += (rect.left - screenLeft);
            } else {
                // get entire rect at right of screen
                scrollXDelta += (rect.right - screenRight);
            }

            // make sure we aren't scrolling beyond the end of our content
            int right = getChildAt(0).getRight();
            int distanceToRight = right - screenRight;
            scrollXDelta = Math.min(scrollXDelta, distanceToRight);

        } else if (rect.left < screenLeft && rect.right < screenRight) {
            // need to move right to get it in view: move right just enough so
            // that
            // entire rectangle is in view (or at least the first screen
            // size chunk of it).

            if (rect.width() > width) {
                // screen size chunk
                scrollXDelta -= (screenRight - rect.right);
            } else {
                // entire rect at left
                scrollXDelta -= (screenLeft - rect.left);
            }

            // make sure we aren't scrolling any further than the left our
            // content
            scrollXDelta = Math.max(scrollXDelta, -getScrollX());
        }
        return scrollXDelta;
    }
}
