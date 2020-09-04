package com.zhy.base;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Point;
import android.os.Bundle;
import android.view.Display;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import androidx.annotation.NonNull;

public class BaseDialog extends Dialog {

    public BaseDialog(@NonNull Context context) {
        super(context);
    }

    public BaseDialog(@NonNull Context context, int themeResId) {
        super(context, themeResId);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setCancelable(true);
        setCanceledOnTouchOutside(true);
    }

    protected final void setAttributesWrap() {
        Window window = getWindow();
        if (window != null) {
            WindowManager.LayoutParams params = window.getAttributes();
            params.width = WindowManager.LayoutParams.WRAP_CONTENT;
            params.height = WindowManager.LayoutParams.WRAP_CONTENT;
            window.setAttributes(params);
        }
    }

    protected final void setAttributesWH(int width, int height) {
        Window window = getWindow();
        if (window != null) {
            WindowManager.LayoutParams params = window.getAttributes();
            params.width = width;
            params.height = height;
            window.setAttributes(params);
        }
    }

    private View mAttachView;
    private int mMargin;
    private int mType;//0 1 2

    public final void attachViewLeftBottom(View view, int margin) {
        this.mType = 1;
        this.mAttachView = view;
        this.mMargin = margin;
    }

    public final void attachViewCenterBottom(View view, int margin) {
        this.mType = 2;
        this.mAttachView = view;
        this.mMargin = margin;
    }

    private void runAttachViewCenterBottom() {
        Window window = getWindow();
        if (window != null) {
            int[] location = new int[2];
            mAttachView.getLocationOnScreen(location);
            int x = location[0];
            int y = location[1];
            setGravity(getGravity(x, y, mAttachView, window));
            WindowManager.LayoutParams params = window.getAttributes();
            int width = window.getDecorView().getWidth();
            params.x = x - width / 2 + mAttachView.getWidth() / 2;
            params.y = y + mAttachView.getHeight() + mMargin;
            onWindowAttributesChanged(params);
        }
    }

    private void runAttachViewLeftBottom() {
        Window window = getWindow();
        if (window != null) {
            int[] location = new int[2];
            mAttachView.getLocationOnScreen(location);
            int x = location[0];
            int y = location[1];
            setGravity(getGravity(x, y, mAttachView, window));
            WindowManager.LayoutParams params = window.getAttributes();
            params.x = x;
            params.y = y + mAttachView.getHeight() + mMargin;
            onWindowAttributesChanged(params);
        }
    }

    protected final int getGravity(int x, int y, View view, Window window) {
        Point point = new Point();
        Display display = window.getWindowManager().getDefaultDisplay();
        display.getSize(point);

        int screen_width = point.x;
        int screen_height = point.y;

        if ((x + view.getWidth() / 2) <= (screen_width / 2) && (y + view.getHeight() / 2) < screen_height / 2) {
            return Gravity.START | Gravity.TOP;
        } else if ((x + view.getWidth() / 2) < (screen_width / 2) && (y + view.getHeight() / 2) > screen_height / 2) {
            return Gravity.START | Gravity.BOTTOM;
        } else if ((x + view.getWidth() / 2) > (screen_width / 2) && (y + view.getHeight() / 2) < screen_height / 2) {
            return Gravity.END | Gravity.TOP;
        } else {
            return Gravity.END | Gravity.BOTTOM;
        }
    }

    public void setGravity(int gravity) {
        Window window = getWindow();
        if (window != null) {
            window.setGravity(gravity);
        }
    }

    private boolean mIsFocusChangedFinish;

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (!mIsFocusChangedFinish) {
            mIsFocusChangedFinish = true;
            if (mType == 1) {
                runAttachViewLeftBottom();
            } else if (mType == 2) {
                runAttachViewCenterBottom();
            }
        }
    }
}
