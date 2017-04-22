package com.udacity.stockhawk.sync;

import android.app.IntentService;
import android.content.Intent;
import android.os.Handler;

import timber.log.Timber;


public class QuoteIntentService extends IntentService {
    private Handler handler;

    public QuoteIntentService() {
        super(QuoteIntentService.class.getSimpleName());
        this.handler = new Handler();
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Timber.d("Intent handled");
        QuoteSyncJob.getQuotes(getApplicationContext(), handler);
    }
}
