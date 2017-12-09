package com.example.chenn.scan.android;

import android.app.Activity;
import android.content.DialogInterface;

/**
 * Created by chenn on 2017/9/29.
 *
 * Simple listener used to exit the app in a few cases.
 * 用于在少数情况下退出App的监听
 */

public final class FinishListener implements DialogInterface.OnClickListener, DialogInterface.OnCancelListener {

    private final Activity activityToFinish;

    public FinishListener(Activity activityToFinish) {
        this.activityToFinish = activityToFinish;
    }

    @Override
    public void onCancel(DialogInterface dialogInterface) {
        run();
    }

    @Override
    public void onClick(DialogInterface dialogInterface, int i) {
        run();
    }

    private void run() {
        activityToFinish.finish();
    }
}
