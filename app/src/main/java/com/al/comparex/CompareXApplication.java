package com.al.comparex;

import android.app.Application;

/**
 * Application class for CompareX.
 * Referenced in AndroidManifest.xml via android:name=".CompareXApplication"
 */
public class CompareXApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        // Place for any global initializations (e.g. Timber, Crashlytics, etc.)
    }
}
