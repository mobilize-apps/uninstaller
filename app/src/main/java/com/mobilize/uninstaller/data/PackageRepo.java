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

package com.mobilize.uninstaller.data;

import android.content.pm.ApplicationInfo;
import android.content.pm.IPackageStatsObserver;
import android.content.pm.PackageManager;
import android.content.pm.PackageStats;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.RemoteException;
import android.support.v4.util.SparseArrayCompat;

import java.io.File;
import java.lang.reflect.Method;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.CountDownLatch;

/**
 * Created by arafat on 12/19/15.
 */
public class PackageRepo implements PackageManagerApi {

    private PackageManager mPackageManager;

    private SparseArrayCompat<Long> mSizes;
    private SparseArrayCompat<String> mLabels;
    private CountDownLatch mLatch;

    private static Callback<List<Package>> mCallback;

    private static final int MESSAGE_POST_RESULT = 1;
    private static final int MESSAGE_POST_ERROR = 2;

    private InternalHandler mHandler;

    public PackageRepo(PackageManager packageManager) {
        mPackageManager = packageManager;
    }

    @Override
    public void getUserRemovablePackages(Callback<List<Package>> callback) {
        this.mCallback = callback;
        loadPackages();
    }

    @Override
    public void uninstallPackages(List<Package> packages) {

    }

    @Override
    public void setOnPackagesChangedListener(OnPackagesChangedListener listener) {

    }

    private void loadPackages() {
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    List<ApplicationInfo> apps = mPackageManager.getInstalledApplications(
                            PackageManager.GET_UNINSTALLED_PACKAGES |
                                    PackageManager.GET_DISABLED_COMPONENTS);
                    if (apps == null) {
                        apps = new ArrayList<>();
                    }

                    // Create corresponding array of entries and load their labels.
                    List<Package> entries = new ArrayList<>(apps.size());

                    mSizes = new SparseArrayCompat<>(apps.size());
                    mLabels = new SparseArrayCompat<>(apps.size());
                    mLatch = new CountDownLatch(apps.size());

                    for (int i = 0; i < apps.size(); i++) {
                        Package appPackage = new Package();

                        appPackage.apkFile = new File(apps.get(i).sourceDir);

                        loadAppLabel(apps.get(i), appPackage, i);
                        loadAppSize(apps.get(i).packageName, i);
                        appPackage.mPackageId = apps.get(i).packageName;
                        entries.add(appPackage);
                    }

                    try {
                        mLatch.await();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                    for (int i = 0; i < mSizes.size(); i++) {
                        entries.get(i).mApkSize = mSizes.get(i);
                        entries.get(i).mLabel = mLabels.get(i);
                    }

                    // Sort the list.
                    Collections.sort(entries, ALPHA_COMPARATOR);

                    // Done!
                    postResult(entries);
                } catch (Exception e) {
                    postError(e);
                }
            }
        });

        thread.start();
    }

    public void loadAppLabel(ApplicationInfo mInfo, Package appPackage, int index) {
        if (appPackage.mLabel == null) {
            if (!appPackage.apkFile.exists()) {
//                mMounted = false;
                mLabels.put(index, mInfo.packageName);
            } else {
//                mMounted = true;
                CharSequence label = mInfo.loadLabel(mPackageManager);
                if (label != null) {
                    mLabels.put(index, label.toString());
                } else {
                    mLabels.put(index, mInfo.packageName);
                }
            }
        }
    }

    private void loadAppSize(final String pkgName, final int index) {
        try {
            Method getPackageSizeInfo = mPackageManager.getClass().getMethod("getPackageSizeInfo",
                    String.class,
                    IPackageStatsObserver.class);

            getPackageSizeInfo.invoke(mPackageManager, pkgName,
                    new IPackageStatsObserver.Stub() {
                        // Examples in the Internet usually have this method as @Override.
                        // I got an error with @Override. Perfectly works without it.
                        public void onGetStatsCompleted(PackageStats pStats, boolean succeeded)
                                throws RemoteException {
                            long size = pStats.codeSize + pStats.dataSize;
                            mSizes.put(index, size);
                            mLatch.countDown();
                        }
                    });

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    /**
     * Perform alphabetical comparison of application entry objects.
     */
    public static final Comparator<Package> ALPHA_COMPARATOR = new Comparator<Package>() {
        private final Collator sCollator = Collator.getInstance();

        @Override
        public int compare(Package package1, Package package2) {
            return sCollator.compare(package1.mLabel, package2.mLabel);
        }
    };

    private void postResult(List<Package> entries){
        Message message = getHandler().obtainMessage(MESSAGE_POST_RESULT, entries);
        message.sendToTarget();
    }

    private void postError(Exception e){
        Message message = getHandler().obtainMessage(MESSAGE_POST_ERROR, e);
        message.sendToTarget();
    }

    private Handler getHandler(){
        synchronized (PackageRepo.class){
            if (mHandler == null){
                mHandler = new InternalHandler();
            }
            return mHandler;
        }
    }

    private static class InternalHandler extends Handler {
        public InternalHandler() {
            super(Looper.getMainLooper());
        }

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what){
                case MESSAGE_POST_RESULT:
                    List<Package> entries = (List<Package>) msg.obj;
                    mCallback.onSuccess(entries);
                    break;

                case MESSAGE_POST_ERROR:
                    Exception e = (Exception) msg.obj;
                    mCallback.onError(e);
                    break;
            }
        }
    }
}
