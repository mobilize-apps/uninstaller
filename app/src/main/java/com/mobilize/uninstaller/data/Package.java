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

import java.util.Date;

public class Package {
    private final String mPackageId;
    private final String mLabel;
    private final Date mInstallDate;
    private final long mApkSize;
    private final long mDataSize;
    private final String mIconUri;

    public Package(String packageId, String label, Date installDate, long apkSize, long dataSize, String iconUri) {
        mPackageId = packageId;
        mLabel = label;
        mInstallDate = installDate;
        mApkSize = apkSize;
        mDataSize = dataSize;
        mIconUri = iconUri;
    }

    public String getPackageId() {
        return mPackageId;
    }

    public String getLabel() {
        return mLabel;
    }

    public Date getInstallDate() {
        return mInstallDate;
    }

    public long getApkSize() {
        return mApkSize;
    }

    public long getDataSize() {
        return mDataSize;
    }

    public String getIconUri() {
        return mIconUri;
    }
}
