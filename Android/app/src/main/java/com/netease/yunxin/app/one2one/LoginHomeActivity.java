// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

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
import com.netease.yunxin.app.one2one.constant.AppConfig;
import com.netease.yunxin.app.one2one.databinding.ActivityLoginHomeBinding;
import com.netease.yunxin.app.one2one.utils.NavUtils;
import java.util.Locale;

public class LoginHomeActivity extends AppCompatActivity {
  private ActivityLoginHomeBinding _binding;
  private static final String TAG = "LoginHomeActivity";

  @Override
  protected void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    _binding = ActivityLoginHomeBinding.inflate(getLayoutInflater());
    setContentView(_binding.getRoot());
    initView();
  }

  private void initView() {
    SpannableStringBuilder spannableString = new SpannableStringBuilder();
    spannableString.append(getString(R.string.login_tips));
    UnderlineSpan underlineSpan1 = new UnderlineSpan();
    boolean isChineseLanguage = Locale.getDefault().getLanguage().contains("zh");
    if (isChineseLanguage) {
      spannableString.setSpan(underlineSpan1, 10, 14, Spannable.SPAN_EXCLUSIVE_INCLUSIVE);
    } else {
      spannableString.setSpan(underlineSpan1, 38, 52, Spannable.SPAN_EXCLUSIVE_INCLUSIVE);
    }

    ClickableSpan clickableSpan1 =
        new ClickableSpan() {
          @Override
          public void onClick(View view) {
            NavUtils.toBrowsePage(
                LoginHomeActivity.this,
                getString(R.string.privacy_policy),
                AppConfig.getPrivacyPolicyUrl());
          }

          @Override
          public void updateDrawState(@NonNull TextPaint ds) {
            super.updateDrawState(ds);
            ds.setColor(Color.WHITE);
          }
        };
    if (isChineseLanguage) {
      spannableString.setSpan(clickableSpan1, 10, 14, Spannable.SPAN_EXCLUSIVE_INCLUSIVE);
    } else {
      spannableString.setSpan(clickableSpan1, 38, 52, Spannable.SPAN_EXCLUSIVE_INCLUSIVE);
    }

    UnderlineSpan underlineSpan2 = new UnderlineSpan();
    if (isChineseLanguage) {
      spannableString.setSpan(underlineSpan2, 17, 23, Spannable.SPAN_EXCLUSIVE_INCLUSIVE);
    } else {
      spannableString.setSpan(underlineSpan2, 57, 73, Spannable.SPAN_EXCLUSIVE_INCLUSIVE);
    }
    ClickableSpan clickableSpan2 =
        new ClickableSpan() {
          @Override
          public void onClick(View view) {
            NavUtils.toBrowsePage(
                LoginHomeActivity.this,
                getString(R.string.terms_of_service),
                AppConfig.getUserAgreementUrl());
          }

          @Override
          public void updateDrawState(@NonNull TextPaint ds) {
            super.updateDrawState(ds);
            ds.setColor(Color.WHITE);
          }
        };
    if (isChineseLanguage) {
      spannableString.setSpan(clickableSpan2, 17, 23, Spannable.SPAN_EXCLUSIVE_INCLUSIVE);
    } else {
      spannableString.setSpan(clickableSpan2, 57, 73, Spannable.SPAN_EXCLUSIVE_INCLUSIVE);
    }
    _binding.tv.setText(spannableString);
    _binding.tv.setMovementMethod(LinkMovementMethod.getInstance());
    _binding.tv.setHighlightColor(Color.TRANSPARENT);
    _binding.verificationCode.setOnClickListener(
        new View.OnClickListener() {
          @Override
          public void onClick(View view) {
            NavUtils.toLoginPage(LoginHomeActivity.this);
            finish();
          }
        });
  }
}
