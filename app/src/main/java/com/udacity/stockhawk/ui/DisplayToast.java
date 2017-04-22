package com.udacity.stockhawk.ui;

import android.content.Context;
import android.widget.Toast;

public class DisplayToast implements Runnable {
    private final Context context;
    private String message;
    private int duration;

    public DisplayToast(Context context, String message, int duration) {
        this.context = context;
        this.message = message;
        this.duration = duration;
    }

    @Override
    public void run() {
        Toast.makeText(context, message, duration).show();
    }

}
