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

package com.mobilize.uninstaller.ui;

/**
 * Created by Arafat on 26/11/2015.
 */

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.IPackageStatsObserver;
import android.content.pm.PackageManager;
import android.content.pm.PackageStats;
import android.os.RemoteException;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.util.SparseArrayCompat;

import com.mobilize.uninstaller.data.AppEntry;

import java.lang.reflect.Method;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.CountDownLatch;

/**
 * A custom Loader that loads all of the installed applications.
 */
public class AppListLoader extends AsyncTaskLoader<List<AppEntry>> {
    final InterestingConfigChanges mLastConfig = new InterestingConfigChanges();
    public final PackageManager mPm;

    List<AppEntry> mApps;
    PackageIntentReceiver mPackageObserver;

    private SparseArrayCompat<Long> mSizes;
    private SparseArrayCompat<String> mLabels;
    private CountDownLatch mLatch;

    public AppListLoader(Context context) {
        super(context);

        // Retrieve the package manager for later use; note we don't
        // use 'context' directly but instead the save global application
        // context returned by getContext().
        mPm = getContext().getPackageManager();
    }

    /**
     * This is where the bulk of our work is done.  This function is
     * called in a background thread and should generate a new set of
     * data to be published by the loader.
     */
    @Override
    public List<AppEntry> loadInBackground() {
        // Retrieve all known applications.
        List<ApplicationInfo> apps = mPm.getInstalledApplications(
                PackageManager.GET_UNINSTALLED_PACKAGES |
                        PackageManager.GET_DISABLED_COMPONENTS);
        if (apps == null) {
            apps = new ArrayList<ApplicationInfo>();
        }

        final Context context = getContext();

        // Create corresponding array of entries and load their labels.
        List<AppEntry> entries = new ArrayList<AppEntry>(apps.size());

        mSizes = new SparseArrayCompat<Long>(apps.size());
        mLabels = new SparseArrayCompat<String>(apps.size());
        mLatch = new CountDownLatch(apps.size());

        for (int i = 0; i < apps.size(); i++) {
            AppEntry entry = new AppEntry(this, apps.get(i));
            loadAppLabel(apps.get(i), entry, i);
            loadAppSize(apps.get(i).packageName, i);
            entry.setPackageName(apps.get(i).packageName);
            entries.add(entry);
        }

        try {
            mLatch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        for (int i = 0; i < mSizes.size(); i++) {
            entries.get(i).setSize(mSizes.get(i));
            entries.get(i).setLabel(mLabels.get(i));
        }

        // Sort the list.
        Collections.sort(entries, ALPHA_COMPARATOR);

        // Done!
        return entries;
    }

    private void loadAppSize(final String pkgName, final int index) {
        try {
            Method getPackageSizeInfo = mPm.getClass().getMethod("getPackageSizeInfo",
                    String.class,
                    IPackageStatsObserver.class);

            getPackageSizeInfo.invoke(mPm, pkgName,
                    new IPackageStatsObserver.Stub() {
                        // Examples in the Internet usually have this method as @Override.
                        // I got an error with @Override. Perfectly works without it.
                        public void onGetStatsCompleted(PackageStats pStats, boolean succeeded)
                                throws RemoteException {
                            long size = pStats.codeSize + pStats.dataSize;
//                            Log.d("PKGSIZE", pkgName + " " + size);
                            mSizes.put(index, size);
                            mLatch.countDown();
                        }
                    });

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public void loadAppLabel(ApplicationInfo mInfo, AppEntry entry, int index) {
        if (entry.getLabel() == null || !entry.isMounted()) {
            if (!entry.getApkFile().exists()) {
//                mMounted = false;
                mLabels.put(index, mInfo.packageName);
            } else {
//                mMounted = true;
                CharSequence label = mInfo.loadLabel(mPm);
                if (label != null) {
                    mLabels.put(index, label.toString());
                } else {
                    mLabels.put(index, mInfo.packageName);
                }
            }
        }
    }

    /**
     * Called when there is new data to deliver to the client.  The
     * super class will take care of delivering it; the implementation
     * here just adds a little more logic.
     */
    @Override
    public void deliverResult(List<AppEntry> apps) {
        if (isReset()) {
            // An async query came in while the loader is stopped.  We
            // don't need the result.
            if (apps != null) {
                onReleaseResources(apps);
            }
        }
        List<AppEntry> oldApps = mApps;
        mApps = apps;

//        SystemClock.sleep(2000);

        if (isStarted()) {
            // If the Loader is currently started, we can immediately
            // deliver its results.
            super.deliverResult(apps);
        }

        // At this point we can release the resources associated with
        // 'oldApps' if needed; now that the new result is delivered we
        // know that it is no longer in use.
        if (oldApps != null) {
            onReleaseResources(oldApps);
        }
    }

    /**
     * Handles a request to start the Loader.
     */
    @Override
    protected void onStartLoading() {
        if (mApps != null) {
            // If we currently have a result available, deliver it
            // immediately.
            deliverResult(mApps);
        }

        // Start watching for changes in the app data.
        if (mPackageObserver == null) {
            mPackageObserver = new PackageIntentReceiver(this);
        }

        // Has something interesting in the configuration changed since we
        // last built the app list?
        boolean configChange = mLastConfig.applyNewConfig(getContext().getResources());

        if (takeContentChanged() || mApps == null || configChange) {
            // If the data has changed since the last time it was loaded
            // or is not currently available, start a load.
            forceLoad();
        }
    }

    /**
     * Handles a request to stop the Loader.
     */
    @Override
    protected void onStopLoading() {
        // Attempt to cancel the current load task if possible.
        cancelLoad();
    }

    /**
     * Handles a request to cancel a load.
     */
    @Override
    public void onCanceled(List<AppEntry> apps) {
        super.onCanceled(apps);

        // At this point we can release the resources associated with 'apps'
        // if needed.
        onReleaseResources(apps);
    }

    /**
     * Handles a request to completely reset the Loader.
     */
    @Override
    protected void onReset() {
        super.onReset();

        // Ensure the loader is stopped
        onStopLoading();

        // At this point we can release the resources associated with 'apps'
        // if needed.
        if (mApps != null) {
            onReleaseResources(mApps);
            mApps = null;
        }

        // Stop monitoring for changes.
        if (mPackageObserver != null) {
            getContext().unregisterReceiver(mPackageObserver);
            mPackageObserver = null;
        }
    }

    /**
     * Helper function to take care of releasing resources associated
     * with an actively loaded data set.
     */
    protected void onReleaseResources(List<AppEntry> apps) {
        // For a simple List<> there is nothing to do.  For something
        // like a Cursor, we would close it here.
    }

    /**
     * Perform alphabetical comparison of application entry objects.
     */
    public static final Comparator<AppEntry> ALPHA_COMPARATOR = new Comparator<AppEntry>() {
        private final Collator sCollator = Collator.getInstance();

        @Override
        public int compare(AppEntry object1, AppEntry object2) {
            return sCollator.compare(object1.getLabel(), object2.getLabel());
        }
    };
}
