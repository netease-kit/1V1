// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.beauty;

import android.Manifest;
import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.viewpager.widget.PagerAdapter;
import com.blankj.utilcode.constant.PermissionConstants;
import com.blankj.utilcode.util.PermissionUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.google.android.material.slider.Slider;
import com.gyf.immersionbar.ImmersionBar;
import com.netease.lava.api.IVideoRender;
import com.netease.lava.nertc.sdk.AbsNERtcCallbackEx;
import com.netease.lava.nertc.sdk.NERtcEx;
import com.netease.yunxin.kit.alog.ALog;
import com.netease.yunxin.kit.beauty.databinding.ActivityBeautySettingBinding;
import com.netease.yunxin.kit.beauty.module.NEEffect;
import com.netease.yunxin.kit.beauty.module.NEFilter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public class BeautySettingActivity extends BeautyBaseActivity {

  private static final String TAG = "BeautySettingActivity";

  public static final String INTENT_KEY_APP_KEY = "intent_key_app_key";

  private final Map<Integer, Float> tmpEffects = new HashMap<>();

  private final Map<Integer, Float> tmpBodyEffects = new HashMap<>();

  private final Map<Integer, Float> tmpDefaultFilters = new HashMap<>();

  private final int DEFAULT_SELECT_EFFECT = R.id.rb_effect_smooth;

  private final int DEFAULT_SELECT_BODY_EFFECT = R.id.rb_effect_thinface;

  private BeautyManager.BeautyFilter tmpSelectedFilter;

  private ActivityBeautySettingBinding binding;

  private String[] tabTags;

  private List<View> tabViews;

  private boolean isBeautySettingPanelShowing;

  private String appKey;

  @Override
  protected boolean needTransparentStatusBar() {
    return true;
  }

  @Override
  protected void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    binding = ActivityBeautySettingBinding.inflate(getLayoutInflater());
    setContentView(binding.getRoot());
    initIntent();
    initRTC();

    PermissionUtils.permission(PermissionConstants.CAMERA)
        .callback(
            new PermissionUtils.FullCallback() {

              @Override
              public void onGranted(@NonNull List<String> granted) {
                if (isFinishing() || isDestroyed()) {
                  return;
                }
                ArrayList<String> list = new ArrayList<>();
                list.add(Manifest.permission.CAMERA);
                if (new HashSet<>(granted).containsAll(list)) {
                  initBeautyData();
                  initViews();
                  BeautyManager.getInstance().startBeauty();
                  startPreview();
                }
              }

              @Override
              public void onDenied(
                  @NonNull List<String> deniedForever, @NonNull List<String> denied) {
                for (String s : denied) {
                  BeautyLogUtil.i(TAG, "onDenied:" + s);
                }
                ToastUtils.showShort(R.string.permission_request_failed_tips);
                finish();
              }
            })
        .request();
  }

  private void initIntent() {
    appKey = getIntent().getStringExtra(INTENT_KEY_APP_KEY);
  }

  private void initRTC() {

    try {
      NERtcEx.getInstance().release();
      NERtcEx.getInstance()
          .init(
              BeautySettingActivity.this,
              appKey,
              new AbsNERtcCallbackEx() {

                @Override
                public void onJoinChannel(int i, long l, long l1, long l2) {}

                @Override
                public void onLeaveChannel(int i) {}

                @Override
                public void onUserJoined(long l) {}

                @Override
                public void onUserLeave(long l, int i) {}

                @Override
                public void onUserAudioStart(long l) {}

                @Override
                public void onUserAudioStop(long l) {}

                @Override
                public void onUserVideoStart(long l, int i) {}

                @Override
                public void onUserVideoStop(long l) {}

                @Override
                public void onDisconnect(int i) {}

                @Override
                public void onError(int i) {}
              },
              null);
    } catch (Exception e) {
      ALog.e(TAG, "e:" + e);
    }
  }

  private void startPreview() {
    binding.vvLocalUser.setZOrderMediaOverlay(true);
    binding.vvLocalUser.setScalingType(IVideoRender.ScalingType.SCALE_ASPECT_FILL);
    NERtcEx.getInstance().setupLocalVideoCanvas(binding.vvLocalUser);
    NERtcEx.getInstance().startVideoPreview();
  }

  @SuppressLint("ClickableViewAccessibility")
  private void initViews() {
    ImmersionBar.with(this).statusBarView(binding.statusBarHolder).statusBarDarkFont(true).init();
    binding.imgBeautyCompared.setOnTouchListener(
        (view, motionEvent) -> {
          int action = motionEvent.getAction();
          if (action == MotionEvent.ACTION_DOWN || action == MotionEvent.ACTION_MOVE) {
            view.setAlpha(0.75f);
            NERtcEx.getInstance().enableBeauty(false);
          } else if (action == MotionEvent.ACTION_UP) {
            view.setAlpha(1.0f);
            NERtcEx.getInstance().enableBeauty(true);
          }
          return true;
        });
    tabViews = new ArrayList<>();
    tabTags = getResources().getStringArray(R.array.beauty_option_tags);
    @SuppressLint("InflateParams")
    View effectTab = getLayoutInflater().inflate(R.layout.tab_skin_effect, null);
    tabViews.add(effectTab);
    @SuppressLint("InflateParams")
    View bodyEffectTab = getLayoutInflater().inflate(R.layout.tab_body_effect, null);
    tabViews.add(bodyEffectTab);
    @SuppressLint("InflateParams")
    View filterTab = getLayoutInflater().inflate(R.layout.tab_filter, null);
    tabViews.add(filterTab);
    for (String tag : tabTags) {
      binding.tabBottom.addTab(binding.tabBottom.newTab().setText(tag));
    }
    //美肤
    AtomicInteger currentEffect = new AtomicInteger();
    Slider effectLevelSlider = effectTab.findViewById(R.id.slider_effect_level);
    NEBeautyRadioGroup effectRadioGroup = effectTab.findViewById(R.id.radio_group_effect);
    MyRadioButton recoveryRadioButton = effectTab.findViewById(R.id.rb_effect_recover);
    recoveryRadioButton.setOnClickListener(
        v -> {
          BeautyManager.getInstance().resetEffect();
          resetEffect();
          effectRadioGroup.check(DEFAULT_SELECT_EFFECT);
          if (currentEffect.get() == DEFAULT_SELECT_EFFECT) {
            effectLevelSlider.setValue(
                BeautyManager.getInstance()
                        .getDefaultEffects()
                        .get(DEFAULT_SELECT_EFFECT)
                        .getLevel()
                    * 100);
          }
        });
    effectRadioGroup.setOnCheckedChangeListener(
        (radioGroup, checkedId) -> {
          currentEffect.set(checkedId);
          float level = 0;
          Float value = tmpEffects.get(checkedId);
          if (value != null) {
            level = value;
          }
          effectLevelSlider.setValue(level * 100);
          BeautyManager.getInstance().setBeautyEffect(checkedId, level);
        });
    effectLevelSlider.addOnChangeListener(
        (slider, value, fromUser) -> {
          float level = value / 100;
          tmpEffects.put(currentEffect.get(), level);
          BeautyManager.getInstance().setBeautyEffect(currentEffect.get(), level);
        });
    effectRadioGroup.check(DEFAULT_SELECT_EFFECT);
    //美型
    AtomicInteger currentBodyEffect = new AtomicInteger();
    Slider bodyEffectLevelSlider = bodyEffectTab.findViewById(R.id.slider_body_effect_level);
    NEBeautyRadioGroup bodyEffectRadioGroup =
        bodyEffectTab.findViewById(R.id.radio_group_body_effect);
    MyRadioButton bodyRecoveryRadioButton = bodyEffectTab.findViewById(R.id.rb_effect_recover);
    bodyRecoveryRadioButton.setOnClickListener(
        v -> {
          BeautyManager.getInstance().resetBodyEffect();
          resetBodyEffect();
          bodyEffectRadioGroup.check(DEFAULT_SELECT_BODY_EFFECT);
          if (currentBodyEffect.get() == DEFAULT_SELECT_BODY_EFFECT) {
            bodyEffectLevelSlider.setValue(
                BeautyManager.getInstance()
                        .getDefaultBodyEffects()
                        .get(DEFAULT_SELECT_BODY_EFFECT)
                        .getLevel()
                    * 100);
          }
        });
    bodyEffectRadioGroup.setOnCheckedChangeListener(
        (radioGroup, checkedId) -> {
          currentBodyEffect.set(checkedId);
          float level = 0;
          Float value = tmpBodyEffects.get(checkedId);
          if (value != null) {
            level = value;
          }
          bodyEffectLevelSlider.setValue(level * 100);
          BeautyManager.getInstance().setBeautyBodyEffect(checkedId, level);
        });
    bodyEffectRadioGroup.check(DEFAULT_SELECT_BODY_EFFECT);
    bodyEffectLevelSlider.addOnChangeListener(
        (slider, value, fromUser) -> {
          float level = value / 100;
          tmpBodyEffects.put(currentBodyEffect.get(), level);
          BeautyManager.getInstance().setBeautyBodyEffect(currentBodyEffect.get(), level);
        });
    //滤镜
    AtomicInteger currentFilterStyle = new AtomicInteger();
    Slider filterLevelSlider = filterTab.findViewById(R.id.slider_filter_level);
    NEBeautyRadioGroup filterRadioGroup = filterTab.findViewById(R.id.radio_group_filter);
    MyRadioButton filterRecoveryRadioButton = filterTab.findViewById(R.id.rb_filter_origin);
    filterRecoveryRadioButton.setOnClickListener(
        v -> {
          filterLevelSlider.setValue(0);
          filterRadioGroup.reset();
          tmpSelectedFilter = null;
          BeautyManager.getInstance().resetFilter();
        });
    filterRadioGroup.setOnCheckedChangeListener(
        (radioGroup, checkedId) -> {
          currentFilterStyle.set(checkedId);
          float level = 0;
          Float value = tmpDefaultFilters.get(checkedId);
          if (value != null) {
            level = value;
          }
          if (tmpSelectedFilter != null && checkedId == tmpSelectedFilter.resId) {
            level = tmpSelectedFilter.level;
          }
          tmpSelectedFilter = new BeautyManager.BeautyFilter(checkedId, level);
          filterLevelSlider.setValue(level * 100);
          BeautyManager.getInstance().addBeautyFilter(checkedId, level);
        });
    filterLevelSlider.addOnChangeListener(
        (slider, value, fromUser) -> {
          float level = value / 100;
          if (tmpSelectedFilter != null) {
            tmpSelectedFilter.resId = currentFilterStyle.get();
            tmpSelectedFilter.level = level;
            BeautyManager.getInstance().addBeautyFilter(currentFilterStyle.get(), level);
          }
        });
    if (tmpSelectedFilter != null && tmpSelectedFilter.resId > 0) {
      filterRadioGroup.check(tmpSelectedFilter.resId);
    }
    binding.tabBottom.setupWithViewPager(binding.vpPager);
    binding.vpPager.setAdapter(
        new PagerAdapter() {

          @Override
          public int getCount() {
            return tabViews.size();
          }

          @Override
          public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
            return view == object;
          }

          @NonNull
          @Override
          public Object instantiateItem(@NonNull ViewGroup container, int position) {
            View view = tabViews.get(position);
            container.addView(view);
            return view;
          }

          @Override
          public void destroyItem(
              @NonNull ViewGroup container, int position, @NonNull Object object) {
            container.removeView((View) object);
          }

          @Override
          public CharSequence getPageTitle(int position) {
            return tabTags[position];
          }
        });
    binding.switchCamera.setOnClickListener(v -> NERtcEx.getInstance().switchCamera());
    binding.beautyOk.setOnClickListener(
        v -> {
          saveData();
          finish();
        });
    binding.openBeauty.setOnClickListener(v -> showOrDismissBeautySettingPanel());
    binding.vvLocalUser.setOnClickListener(
        v -> {
          if (isBeautySettingPanelShowing) {
            showOrDismissBeautySettingPanel();
          }
        });
  }

  private void initBeautyData() {
    HashMap<Integer, NEEffect> effects = BeautyManager.getInstance().getLocalEffects();
    for (Map.Entry<Integer, NEEffect> entry : effects.entrySet()) {
      tmpEffects.put(entry.getKey(), entry.getValue().getLevel());
    }
    HashMap<Integer, NEEffect> bodyEffects = BeautyManager.getInstance().getLocalBodyEffects();
    for (Map.Entry<Integer, NEEffect> entry : bodyEffects.entrySet()) {
      tmpBodyEffects.put(entry.getKey(), entry.getValue().getLevel());
    }
    HashMap<Integer, NEFilter> filters = BeautyManager.getInstance().getDefaultFilters();
    for (Map.Entry<Integer, NEFilter> entry : filters.entrySet()) {
      tmpDefaultFilters.put(entry.getKey(), entry.getValue().getLevel());
    }
    tmpSelectedFilter = BeautyManager.getInstance().getSelectedFilter();
  }

  private void resetEffect() {
    HashMap<Integer, NEEffect> effects = BeautyManager.getInstance().getDefaultEffects();
    for (Map.Entry<Integer, NEEffect> entry : effects.entrySet()) {
      tmpEffects.put(entry.getKey(), entry.getValue().getLevel());
    }
  }

  private void resetBodyEffect() {
    HashMap<Integer, NEEffect> bodyEffects = BeautyManager.getInstance().getDefaultBodyEffects();
    for (Map.Entry<Integer, NEEffect> entry : bodyEffects.entrySet()) {
      tmpBodyEffects.put(entry.getKey(), entry.getValue().getLevel());
    }
  }

  private void saveData() {
    for (Map.Entry<Integer, Float> entry : tmpEffects.entrySet()) {
      BeautyManager.getInstance().saveEffectData(entry.getKey(), entry.getValue());
    }
    for (Map.Entry<Integer, Float> entry : tmpBodyEffects.entrySet()) {
      BeautyManager.getInstance().saveBodyEffectData(entry.getKey(), entry.getValue());
    }
    BeautyManager.getInstance().saveFilterData(tmpSelectedFilter);
  }

  private void showOrDismissBeautySettingPanel() {
    isBeautySettingPanelShowing = !isBeautySettingPanelShowing;
    binding.beautySettingPanel.setVisibility(
        isBeautySettingPanelShowing ? View.VISIBLE : View.INVISIBLE);
    binding.tabBottom.setVisibility(isBeautySettingPanelShowing ? View.VISIBLE : View.INVISIBLE);
    binding.beautyToolsPanel.setVisibility(
        isBeautySettingPanelShowing ? View.INVISIBLE : View.VISIBLE);
  }

  @Override
  protected void onDestroy() {
    BeautyManager.getInstance().stopBeauty();
    NERtcEx.getInstance().stopVideoPreview();
    NERtcEx.getInstance().release();
    super.onDestroy();
  }
}
