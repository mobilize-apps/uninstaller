/*
 * Copyright (c) 2015 Project contributors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 */

package com.mobilize.uninstaller;

import android.app.Application;
import android.os.StrictMode;

import com.crashlytics.android.Crashlytics;
import com.mobilize.uninstaller.utils.CrashlyticsTree;
import com.squareup.leakcanary.LeakCanary;

import butterknife.ButterKnife;
import io.fabric.sdk.android.Fabric;
import timber.log.Timber;

public class UninstallerApp extends Application {
    @Override
    public void onCreate() {
        super.onCreate();

        if (BuildConfig.DEBUG) {
            // Enable strict mode
            StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder()
                    .detectAll()
                    .penaltyFlashScreen()
                    .penaltyLog()
                    .build());

            // Log to logcat
            Timber.plant(new Timber.DebugTree());

            // Activity leak tracker
            LeakCanary.install(this);

            // Butterknife debugging
            ButterKnife.setDebug(true);
        } else {
            // Enable crashlytics on release builds
            Fabric.with(this, new Crashlytics());

            // Log errors and warnings to crashlytics
            Timber.plant(new CrashlyticsTree());
        }
    }
}
