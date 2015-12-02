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

package com.mobilize.uninstaller.mvp;

import com.mobilize.uninstaller.data.Package;
import com.mobilize.uninstaller.data.PackageManagerApi;

import java.util.List;

public class PackageManagerPresenter implements PackageManagerActionListener, PackageManagerApi.OnPackagesChangedListener {
    private final PackageManagerApi mApi;
    private final PackageManagerView mView;

    public PackageManagerPresenter(PackageManagerApi api, PackageManagerView view) {
        mApi = api;
        mView = view;
    }

    @Override
    public void onStarted() {
        mApi.setOnPackagesChangedListener(this);
        mView.showLoadingIndicator(true);

        mApi.getUserRemovablePackages(new PackageManagerApi.Callback<List<Package>>() {
            @Override
            public void onSuccess(List<Package> result) {
                mView.showLoadingIndicator(false);
                mView.showPackages(result);
            }

            @Override
            public void onError(Exception e) {
                mView.showLoadingIndicator(false);
                mView.showLoadingError(e);
            }
        });
    }

    @Override
    public void onStopped() {
        mApi.setOnPackagesChangedListener(null);
    }

    @Override
    public void onUninstallPackages(List<Package> packages) {
        mApi.uninstallPackages(packages);
    }

    @Override
    public void onPackagesChanged(List<Package> newPackages) {
        mView.showPackages(newPackages);
    }
}
