/*
 *
 *  * Copyright (c) 2022 NetEase, Inc.  All rights reserved.
 *  * Use of this source code is governed by a MIT license that can be found in the LICENSE file.
 *
 */

package com.netease.yunxin.app.one2one;

import android.graphics.Color;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.TextPaint;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.text.style.UnderlineSpan;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.netease.yunxin.app.one2one.constant.AppConstants;
import com.netease.yunxin.app.one2one.databinding.ActivityLoginHomeBinding;
import com.netease.yunxin.app.one2one.databinding.ActivitySplashBinding;
import com.netease.yunxin.app.one2one.utils.NavUtils;
import com.netease.yunxin.kit.alog.ALog;
import com.netease.yunxin.kit.login.AuthorManager;
import com.netease.yunxin.kit.login.model.LoginCallback;
import com.netease.yunxin.kit.login.model.UserInfo;

public class LoginHomeActivity extends AppCompatActivity {
    private ActivityLoginHomeBinding _binding;
    private static final String TAG="SplashActivity";
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        _binding = ActivityLoginHomeBinding.inflate(getLayoutInflater());
        setContentView(_binding.getRoot());
        initView();
    }

    private void initView() {
        SpannableStringBuilder spannableString = new SpannableStringBuilder();
        spannableString.append("登录即视为您已同意 隐私政策 和 用户服务协议");
        UnderlineSpan underlineSpan1 = new UnderlineSpan();
        spannableString.setSpan(underlineSpan1, 10, 14, Spannable.SPAN_EXCLUSIVE_INCLUSIVE);

        ClickableSpan clickableSpan1 = new ClickableSpan() {
            @Override
            public void onClick(View view) {
               NavUtils.toBrowsePage(LoginHomeActivity.this,"隐私政策",AppConstants.PRIVACY_POLICY);
            }

            @Override
            public void updateDrawState(@NonNull TextPaint ds) {
                super.updateDrawState(ds);
                ds.setColor(Color.WHITE);
            }
        };
        spannableString.setSpan(clickableSpan1, 10, 14, Spannable.SPAN_EXCLUSIVE_INCLUSIVE);

        UnderlineSpan underlineSpan2 = new UnderlineSpan();
        spannableString.setSpan(underlineSpan2, 17, 23, Spannable.SPAN_EXCLUSIVE_INCLUSIVE);
        ClickableSpan clickableSpan2 = new ClickableSpan() {
            @Override
            public void onClick(View view) {
                NavUtils.toBrowsePage(LoginHomeActivity.this,"隐私政策",AppConstants.USER_AGREEMENT);
            }
            @Override
            public void updateDrawState(@NonNull TextPaint ds) {
                super.updateDrawState(ds);
                ds.setColor(Color.WHITE);
            }
        };
        spannableString.setSpan(clickableSpan2, 17, 23, Spannable.SPAN_EXCLUSIVE_INCLUSIVE);
        _binding.tv.setText(spannableString);
        _binding.tv.setMovementMethod(LinkMovementMethod.getInstance());
        _binding.tv.setHighlightColor(Color.TRANSPARENT);
        _binding.verificationCode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                NavUtils.toLoginPage(LoginHomeActivity.this);
                finish();
            }
        });
    }
}
