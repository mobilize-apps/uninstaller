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

import android.graphics.drawable.Drawable;

import java.io.File;
import java.util.Date;

public class Package {
    public String mPackageId;
    public String mLabel;
    public Date mInstallDate;
    public File apkFile;
    public long mApkSize;
    public long mDataSize;
    public Drawable mIcon;

    public Package() {
    }
}
