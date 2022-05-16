package com.dsoumis.dbpediavirtualsparqlqueryconstructor.listeners;

import android.os.Handler;
import android.os.SystemClock;
import android.view.View;

public abstract class DoubleClickListener implements View.OnClickListener {
    private static final long DEFAULT_QUALIFICATION_SPAN = 200;
    private boolean isSingleEvent;
    private final long doubleClickQualificationSpanInMillis;
    private long timestampLastClick;
    private final Handler handler;
    private final Runnable runnable;

    public DoubleClickListener() {
        doubleClickQualificationSpanInMillis = DEFAULT_QUALIFICATION_SPAN;
        timestampLastClick = 0;
        handler = new Handler();
        runnable = new Runnable() {
            @Override
            public void run() {
                if (isSingleEvent) {
                    onSingleClick();
                }
            }
        };
    }

    @Override
    public void onClick(View v) {
        if((SystemClock.elapsedRealtime() - timestampLastClick) < doubleClickQualificationSpanInMillis) {
            isSingleEvent = false;
            handler.removeCallbacks(runnable);
            onDoubleClick();
            return;
        }

        isSingleEvent = true;
        handler.postDelayed(runnable, DEFAULT_QUALIFICATION_SPAN);
        timestampLastClick = SystemClock.elapsedRealtime();
    }

    public abstract void onDoubleClick();
    public abstract void onSingleClick();
}
