package com.kc.net.download;

import android.graphics.PixelFormat;
import android.os.Build;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;

import com.kc.net.R;

import static android.content.Context.WINDOW_SERVICE;

/**
 * created by zhaojianwei03
 * on 2021-11-18
 * at 7:56 PM
 */
class WindowUtils {
    private static TextView percent;
    private static TextView speed;
    public static boolean isAddedView;
    private static final WindowManager windowManager = (WindowManager) App.getContext().getSystemService(WINDOW_SERVICE);
    private static final View view = View.inflate(App.getContext(), R.layout.global_window_layout, null);
    private static final WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams();


    public static void addPercentWindow() {
        // 进度条提示
        percent = view.findViewById(R.id.percent);
        speed = view.findViewById(R.id.speed);
        if (Build.VERSION.SDK_INT > 25) {
            layoutParams.type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
        } else {
            layoutParams.type = WindowManager.LayoutParams.TYPE_SYSTEM_ALERT;
        }
        // 设置flag
        int flags = WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM // Never enable input-method.
                | WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH
                | WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
                | WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
                | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
        // | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
        // 如果设置了WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE，弹出的View收不到Back键的事件
        layoutParams.flags = flags;
        layoutParams.format = PixelFormat.TRANSPARENT;
        layoutParams.width = 300;
        layoutParams.height = 300;
        layoutParams.gravity = Gravity.CENTER_VERTICAL | Gravity.END;
        layoutParams.windowAnimations = R.style.my_window_anim_style;
        if (windowManager != null) {
            windowManager.addView(view, layoutParams);
            isAddedView = true;
        }
    }

    public static void removeView() {
        if (windowManager != null) {
            windowManager.removeView(view);
            isAddedView = true;
        }
    }

    public static void setPercentNum( String percentNum){
        percent.setText(percentNum);
    }

    public static void setNetworkSpeed( String networkSpeedNum){
        speed.setText(networkSpeedNum);
    }

}
