/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2014-2021 Tuya Inc.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NO
 */

package com.tuya.appsdk.sample.device.config.ap;

import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.material.progressindicator.CircularProgressIndicator;
import com.tuya.appsdk.sample.device.config.R;
import com.tuya.appsdk.sample.resource.HomeModel;
import com.thingclips.smart.home.sdk.ThingHomeSdk;
import com.thingclips.smart.home.sdk.builder.ActivatorBuilder;
import com.thingclips.smart.sdk.api.IThingActivator;
import com.thingclips.smart.sdk.api.IThingActivatorGetToken;
import com.thingclips.smart.sdk.api.IThingSmartActivatorListener;
import com.thingclips.smart.sdk.bean.DeviceBean;
import com.thingclips.smart.sdk.enums.ActivatorModelEnum;

/**
 * Device Configuration AP Mode Sample
 *
 * @author chuanfeng <a href="mailto:developer@tuya.com"/>
 * @since 2021/2/18 9:50 AM
 */
public class DeviceConfigAPActivity extends AppCompatActivity implements View.OnClickListener {

    public static final String TAG = "DeviceConfigAPActivity";

    public CircularProgressIndicator cpiLoading;
    public Button btnSearch;
    private TextView mContentTv;
    private String strSsid;
    private EditText etPassword;
    private String strPassword;
    private String mToken;
    private IThingActivator mTuyaActivator;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.device_config_activity);

        Toolbar toolbar = findViewById(R.id.topAppBar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        toolbar.setTitle(getString(R.string.device_config_ap_title));

        cpiLoading = findViewById(R.id.cpiLoading);
        btnSearch = findViewById(R.id.btnSearch);
        btnSearch.setOnClickListener(this);
        mContentTv = findViewById(R.id.content_tv);
        mContentTv.setText(getString(R.string.device_config_ap_description));
    }

    @Override
    public void onClick(View v) {
        EditText etSsid = findViewById(R.id.etSsid);
        strSsid = etSsid.getText().toString();
        etPassword = findViewById(R.id.etPassword);
        strPassword = etPassword.getText().toString();

        if (v.getId() == R.id.btnSearch) {
            long homeId = HomeModel.getCurrentHome(this);

            // Get Network Configuration Token
            ThingHomeSdk.getActivatorInstance().getActivatorToken(homeId,
                    new IThingActivatorGetToken() {
                        @Override
                        public void onSuccess(String token) {
                            mToken = token;
                            onClickSetting();

                        }


                        @Override
                        public void onFailure(String errorCode, String errorMsg) {

                        }
                    });


        }

    }

    @Override
    protected void onRestart() {
        super.onRestart();
        //Show loading progress, disable btnSearch clickable
        btnSearch.setClickable(false);

        cpiLoading.setVisibility(View.VISIBLE);
        cpiLoading.setIndeterminate(true);

        ActivatorBuilder builder = new ActivatorBuilder()
                .setSsid(strSsid)
                .setContext(DeviceConfigAPActivity.this)
                .setPassword(strPassword)
                .setActivatorModel(ActivatorModelEnum.THING_AP)
                .setTimeOut(100)
                .setToken(mToken)
                .setListener(new IThingSmartActivatorListener() {
                    @Override
                    public void onError(String errorCode, String errorMsg) {
                        cpiLoading.setVisibility(View.GONE);
                        btnSearch.setClickable(true);
                        Toast.makeText(DeviceConfigAPActivity.this,
                                "Activate error-->" + errorMsg,
                                Toast.LENGTH_LONG
                        ).show();

                    }

                    @Override
                    public void onActiveSuccess(DeviceBean devResp) {
                        cpiLoading.setVisibility(View.GONE);

                        Log.i(TAG, "Activate success");
                        Toast.makeText(DeviceConfigAPActivity.this,
                                "Activate success",
                                Toast.LENGTH_LONG
                        ).show();

                        finish();
                    }

                    @Override
                    public void onStep(String step, Object data) {
                        Log.i(TAG, step + " --> " + data);
                    }
                });

        mTuyaActivator = ThingHomeSdk.getActivatorInstance().newActivator(builder);

        //Start configuration
        mTuyaActivator.start();


    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //Exit the page to destroy some cache data and monitoring data.
        if (null != mTuyaActivator) {
            mTuyaActivator.onDestroy();
        }
    }

    /**
     *
     * wifi setting
     */
    private void onClickSetting() {
        Intent wifiSettingsIntent = new Intent("android.settings.WIFI_SETTINGS");
        if (null != wifiSettingsIntent.resolveActivity(getPackageManager())) {
            startActivity(wifiSettingsIntent);
        } else {
            wifiSettingsIntent = new Intent(Settings.ACTION_WIFI_SETTINGS);
            if (null != wifiSettingsIntent.resolveActivity(getPackageManager())) {
                startActivity(wifiSettingsIntent);
            }
        }
    }
}