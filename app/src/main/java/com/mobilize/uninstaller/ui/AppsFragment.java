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

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.mobilize.uninstaller.R;
import com.mobilize.uninstaller.data.Package;
import com.mobilize.uninstaller.data.PackageRepo;
import com.mobilize.uninstaller.mvp.PackageManagerPresenter;
import com.mobilize.uninstaller.mvp.PackageManagerView;

import java.util.List;

/**
 * A placeholder fragment containing a simple view.
 */
public class AppsFragment extends Fragment implements PackageManagerView{

    private PackageManagerPresenter mPresenter;

    public AppsFragment() {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_main, container, false);
    }

    @Override
    public void onStart() {
        super.onStart();
        PackageRepo packageRepo = new PackageRepo(getActivity().getPackageManager());
        mPresenter = new PackageManagerPresenter(packageRepo, this);
        mPresenter.onStarted();
    }

    @Override
    public void showLoadingIndicator(boolean show) {

    }

    @Override
    public void showLoadingError(Exception e) {

    }

    @Override
    public void showPackages(final List<Package> packages) {
        Toast.makeText(getContext(), String.valueOf(packages.size()), Toast.LENGTH_SHORT).show();
    }

    @Override
    public void removePackage(Package pkg) {

    }
}
