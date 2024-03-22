// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.app.oneonone.ui.fragment;

import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import com.netease.yunxin.app.oneonone.ui.R;
import com.netease.yunxin.app.oneonone.ui.activity.CallActivity;
import com.netease.yunxin.app.oneonone.ui.custommessage.GiftAttachment;
import com.netease.yunxin.app.oneonone.ui.http.HttpService;
import com.netease.yunxin.app.oneonone.ui.model.ModelResponse;
import com.netease.yunxin.app.oneonone.ui.viewmodel.CallViewModel;
import com.netease.yunxin.app.oneonone.ui.viewmodel.VirtualCallViewModel;
import com.netease.yunxin.kit.alog.ALog;
import com.netease.yunxin.kit.common.ui.utils.ToastX;
import com.netease.yunxin.kit.common.utils.ScreenUtils;
import com.netease.yunxin.kit.entertainment.common.gift.GifAnimationView;
import com.netease.yunxin.kit.entertainment.common.gift.GiftCache;
import com.netease.yunxin.kit.entertainment.common.gift.GiftDialog;
import com.netease.yunxin.kit.entertainment.common.gift.GiftRender;
import com.netease.yunxin.kit.entertainment.common.utils.ReportUtils;
import com.netease.yunxin.kit.entertainment.common.utils.UserInfoManager;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public abstract class InTheBaseCallFragment extends Fragment {
  public static final String TAG = "InTheBaseCallFragment";
  public static final String TAG_REPORT = "page_1v1_tonghua";
  protected CallViewModel viewModel;
  protected GiftRender giftRender;
  protected LifecycleOwner viewLifecycleOwner;
  protected VirtualCallViewModel virtualCallViewModel;
  private CallActivity activity;

  @Override
  public void onAttach(@NonNull Context context) {
    super.onAttach(context);
    activity = (CallActivity) context;
  }

  @Nullable
  @Override
  public View onCreateView(
      @NonNull LayoutInflater inflater,
      @Nullable ViewGroup container,
      @Nullable Bundle savedInstanceState) {
    View rootView = getRoot(inflater, container);
    viewModel = new ViewModelProvider(requireActivity()).get(CallViewModel.class);
    if (activity.isVirtualCall()) {
      virtualCallViewModel =
          new ViewModelProvider(requireActivity()).get(VirtualCallViewModel.class);
    }
    subscribeUi();
    initEvent();
    initGiftAnimation(rootView);
    if (activity.isFromFloatWindow()) {
      handleFloatWindowEvent();
    }
    return rootView;
  }

  protected abstract void handleFloatWindowEvent();

  protected abstract View getRoot(@NonNull LayoutInflater inflater, @Nullable ViewGroup container);

  protected void subscribeUi() {
    viewLifecycleOwner = getViewLifecycleOwner();
    viewModel
        .getGiftAttachment()
        .observe(
            viewLifecycleOwner,
            new Observer<GiftAttachment>() {
              @Override
              public void onChanged(GiftAttachment giftAttachment) {
                if (giftAttachment != null) {
                  giftRender.addGift(
                      GiftCache.getGift(giftAttachment.getGiftId()).getDynamicIconResId());
                  if (TextUtils.equals(
                      UserInfoManager.getSelfUserUuid(), giftAttachment.getTargetUserUuid())) {
                    ToastX.showShortToast(
                        R.string.app_receive_gift_tip,
                        giftAttachment.getGiftCount(),
                        GiftCache.getGift(giftAttachment.getGiftId()).getName());
                  }
                }
              }
            });
  }

  protected void initEvent() {}

  private void initGiftAnimation(View baseView) {
    if (baseView == null) {
      return;
    }
    GifAnimationView gifAnimationView = new GifAnimationView(getContext());
    int size = ScreenUtils.getDisplayWidth();
    ConstraintLayout.LayoutParams layoutParams = new ConstraintLayout.LayoutParams(size, size);
    layoutParams.topToTop = ConstraintLayout.LayoutParams.PARENT_ID;
    layoutParams.bottomToBottom = ConstraintLayout.LayoutParams.PARENT_ID;
    ViewGroup root = (ViewGroup) baseView.findViewById(R.id.cl_root);
    root.addView(gifAnimationView, layoutParams);
    gifAnimationView.bringToFront();
    giftRender = new GiftRender();
    giftRender.init(gifAnimationView);
  }

  protected void showGiftDialog() {
    ReportUtils.report(requireContext(), TAG_REPORT, "1v1_tonghua_gift");
    GiftDialog giftDialog = new GiftDialog(requireActivity());
    giftDialog.show(
        (giftId, giftCount) ->
            HttpService.getInstance()
                .reward(
                    giftId,
                    giftCount,
                    viewModel.getOtherInfo().getValue().accId,
                    new Callback<ModelResponse<Boolean>>() {

                      @Override
                      public void onResponse(
                          Call<ModelResponse<Boolean>> call,
                          Response<ModelResponse<Boolean>> response) {}

                      @Override
                      public void onFailure(Call<ModelResponse<Boolean>> call, Throwable t) {
                        ALog.e(TAG, "reward failed error = " + t.getMessage());
                        ToastX.showShortToast(R.string.network_error);
                      }
                    }));
  }
}
