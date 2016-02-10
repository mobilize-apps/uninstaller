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

/**
 * Created by Arafat on 26/11/2015.
 */

import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.text.format.Formatter;

import com.mobilize.uninstaller.ui.AppListLoader;

import java.io.File;

/**
 * This class holds the per-item data in our Loader.
 */
public class AppEntry {

    private final AppListLoader mLoader;
    private final ApplicationInfo mInfo;
    private final File mApkFile;
    private String mLabel;
    private Drawable mIcon;
    private boolean mMounted;
    private long codeSize;
    private String packageName;


    public AppEntry(AppListLoader loader, ApplicationInfo info) {
        mLoader = loader;
        mInfo = info;
        mApkFile = new File(info.sourceDir);
    }

    public String getLabel() {
        return mLabel;
    }

    public String getSize(){
        String fSize = Formatter.formatFileSize(mLoader.getContext(), codeSize);
        return fSize;
    }

    public Drawable getIcon() {
//        if (mIcon == null) {
//            if (mApkFile.exists()) {
//                mIcon = mInfo.loadIcon(mLoader.mPm);
//                return mIcon;
//            } else {
//                mMounted = false;
//            }
//        } else if (!mMounted) {
//            // If the app wasn't mounted but is now mounted, reload
//            // its icon.
//            if (mApkFile.exists()) {
//                mMounted = true;
//                mIcon = mInfo.loadIcon(mLoader.mPm);
//                return mIcon;
//            }
//        } else {
//            return mIcon;
//        }
//
//        return mLoader.getContext().getResources().getDrawable(
//                android.R.drawable.sym_def_app_icon);
        Drawable icon;
        try {
            icon = mLoader.mPm.getApplicationIcon(packageName);
        } catch (PackageManager.NameNotFoundException e) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                icon = mLoader.getContext().getResources().getDrawable(
                        android.R.drawable.sym_def_app_icon, mLoader.getContext().getTheme());
            } else {

                icon = mLoader.getContext().getResources().getDrawable(
                        android.R.drawable.sym_def_app_icon);
            }
        }

        return icon;
    }

    @Override
    public String toString() {
        return mLabel;
    }

    public File getApkFile() {
        return mApkFile;
    }

    public boolean isMounted() {
        return mMounted;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    public String getPackageName() {
        return packageName;
    }

    public long getCodeSize() {
        return codeSize;
    }

    public void setSize(Long size) {
        codeSize = size;
    }

    public void setLabel(String label) {
        mLabel = label;
    }
}
