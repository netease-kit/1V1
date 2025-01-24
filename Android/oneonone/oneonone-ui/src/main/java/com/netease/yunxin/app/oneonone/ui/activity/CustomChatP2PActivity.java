// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.app.oneonone.ui.activity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.inputmethod.InputMethodManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.RequiresApi;
import androidx.core.widget.PopupWindowCompat;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import com.netease.yunxin.app.oneonone.ui.OneOnOneUI;
import com.netease.yunxin.app.oneonone.ui.R;
import com.netease.yunxin.app.oneonone.ui.constant.Constants;
import com.netease.yunxin.app.oneonone.ui.custommessage.GiftAttachment;
import com.netease.yunxin.app.oneonone.ui.databinding.ActivityMyChatP2pBinding;
import com.netease.yunxin.app.oneonone.ui.dialog.AudioInputDialog;
import com.netease.yunxin.app.oneonone.ui.http.HttpService;
import com.netease.yunxin.app.oneonone.ui.model.ModelResponse;
import com.netease.yunxin.app.oneonone.ui.model.UserModel;
import com.netease.yunxin.app.oneonone.ui.utils.AudioInputManager;
import com.netease.yunxin.app.oneonone.ui.utils.CallKitUtil;
import com.netease.yunxin.app.oneonone.ui.utils.ChatUIConfigManager;
import com.netease.yunxin.app.oneonone.ui.utils.ChatUtil;
import com.netease.yunxin.app.oneonone.ui.utils.OneOnOneUtils;
import com.netease.yunxin.app.oneonone.ui.view.SettingPopupWindows;
import com.netease.yunxin.app.oneonone.ui.viewmodel.CustomP2PViewModel;
import com.netease.yunxin.kit.alog.ALog;
import com.netease.yunxin.kit.call.p2p.NECallEngine;
import com.netease.yunxin.kit.call.p2p.model.NECallEndInfo;
import com.netease.yunxin.kit.call.p2p.model.NECallEngineDelegate;
import com.netease.yunxin.kit.call.p2p.model.NECallEngineDelegateAbs;
import com.netease.yunxin.kit.call.p2p.model.NEHangupReasonCode;
import com.netease.yunxin.kit.chatkit.ui.normal.builder.P2PChatFragmentBuilder;
import com.netease.yunxin.kit.chatkit.ui.normal.page.fragment.ChatP2PFragment;
import com.netease.yunxin.kit.chatkit.ui.normal.view.MessageBottomLayout;
import com.netease.yunxin.kit.chatkit.ui.view.emoji.EmojiPickerView;
import com.netease.yunxin.kit.common.ui.dialog.TopPopupWindow;
import com.netease.yunxin.kit.common.ui.utils.ToastX;
import com.netease.yunxin.kit.common.utils.PermissionUtils;
import com.netease.yunxin.kit.common.utils.ScreenUtils;
import com.netease.yunxin.kit.common.utils.SizeUtils;
import com.netease.yunxin.kit.corekit.im2.utils.RouterConstant;
import com.netease.yunxin.kit.entertainment.common.activity.BasePartyActivity;
import com.netease.yunxin.kit.entertainment.common.gift.GifAnimationView;
import com.netease.yunxin.kit.entertainment.common.gift.GiftCache;
import com.netease.yunxin.kit.entertainment.common.gift.GiftDialog;
import com.netease.yunxin.kit.entertainment.common.gift.GiftRender;
import com.netease.yunxin.kit.entertainment.common.utils.DialogUtil;
import com.netease.yunxin.kit.entertainment.common.utils.ReportUtils;
import com.netease.yunxin.kit.entertainment.common.utils.UserInfoManager;
import java.util.Map;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

@RequiresApi(api = Build.VERSION_CODES.M)
public class CustomChatP2PActivity extends BasePartyActivity {
  private static final String TAG = "CustomChatP2PActivity";
  public static final String TAG_REPORT = "page_xiaoxi_huihua";
  private ActivityMyChatP2pBinding viewBinding;
  private ChatP2PFragment chatP2PFragment;
  private UserModel userInfo;
  private String accId;
  private ImageView mAudioIv;
  private ImageView mEmojiIv;
  private ImageView mGiftIv;
  private TextView mAudioTv;
  private MessageBottomLayout messageInputLayout;
  private AudioInputDialog dialog;
  private boolean isInsideView;
  private CustomP2PViewModel viewModel;
  private final Handler handler = new Handler();
  private static final int TYPE_DELAY_TIME = 3000;
  private final Runnable stopTypingRunnable = () -> setTypeState(false);
  private final ChatUIConfigManager chatUIConfigManager = new ChatUIConfigManager();
  private final AudioInputManager audioInputManager = new AudioInputManager();
  private GiftRender giftRender;
  private int audioIvNormalViewTop;
  private TopPopupWindow permissionPop;
  private final NECallEngineDelegate callEngineDelegate =
      new NECallEngineDelegateAbs() {

        @Override
        public void onCallEnd(NECallEndInfo neCallEndInfo) {
          if (neCallEndInfo.reasonCode == NEHangupReasonCode.BUSY
              || (neCallEndInfo.reasonCode == NEHangupReasonCode.CALLER_REJECTED
                  && !TextUtils.isEmpty(neCallEndInfo.extraString))) {
            DialogUtil.showAlertDialog(
                CustomChatP2PActivity.this, getString(R.string.one_on_one_other_is_busy));
          }
        }
      };
  private final ActivityResultLauncher<String[]> permissionLauncher =
      registerForActivityResult(
          new ActivityResultContracts.RequestMultiplePermissions(),
          result -> {
            if (result != null) {
              if (permissionPop != null) {
                permissionPop.dismiss();
              }
              for (Map.Entry<String, Boolean> entry : result.entrySet()) {
                String permission = entry.getKey();
                boolean grant = entry.getValue();
                if (!grant) {
                  if (shouldShowRequestPermissionRationale(permission)) {
                    ToastX.showShortToast(
                        getResources()
                            .getString(
                                com.netease.yunxin.kit.chatkit.ui.R.string.permission_deny_tips));
                  } else {
                    ToastX.showShortToast(getPermissionText(permission));
                  }
                }
              }
            }
          });

  @Override
  protected void init() {
    paddingStatusBarHeight(viewBinding.getRoot());
    userInfo = (UserModel) getIntent().getSerializableExtra(RouterConstant.CHAT_KRY);
    accId = getIntent().getStringExtra(RouterConstant.CHAT_ID_KRY);
    if (userInfo == null && TextUtils.isEmpty(accId)) {
      ALog.e(TAG, "user info is null && sessionID is null:");
      finish();
      return;
    }
    viewModel = new ViewModelProvider(this).get(CustomP2PViewModel.class);
    viewModel.initialize(accId, userInfo, chatUIConfigManager);
    configP2PChatCustomUI();
    loadP2PFragment();
    initGiftAnimation();
    handleEvent();
    initObserver();
    NECallEngine.sharedInstance().addCallDelegate(callEngineDelegate);
    ReportUtils.report(CustomChatP2PActivity.this, TAG_REPORT, "xiaoxi_enter");
  }

  private void configP2PChatCustomUI() {
    chatUIConfigManager.initChatUIConfig(CustomChatP2PActivity.this, accId);
  }

  private void initObserver() {
    chatUIConfigManager.reEditRevokeMessageLiveData.observe(
        this,
        chatMessageBean -> {
          if (mAudioTv.getVisibility() == View.VISIBLE) {
            mAudioTv.setVisibility(View.GONE);
          }
        });
    viewModel
        .getOnlineStatusData()
        .observe(
            this,
            aBoolean -> {
              if (aBoolean) {
                viewBinding.llOnline.setVisibility(View.VISIBLE);
              } else {
                viewBinding.llOnline.setVisibility(View.GONE);
              }
            });
    viewModel
        .getTypeStateLiveData()
        .observe(
            this,
            isTyping -> {
              ALog.i(TAG, "isTyping:" + isTyping);
              handler.removeCallbacks(stopTypingRunnable);
              setTypeState(isTyping);
              if (isTyping) {
                handler.postDelayed(stopTypingRunnable, TYPE_DELAY_TIME);
              }
            });
    viewModel
        .getUserInfoLiveData()
        .observe(
            this,
            userInfo -> {
              if (ChatUtil.isSystemAccount(accId)) {
                return;
              }
              viewBinding.tvTitle.setText(userInfo.nickname);
            });
    viewModel
        .getGiftMessageLiveData()
        .observe(
            this,
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
    viewModel.getBusyLiveData().observe(this, aBoolean -> showBusyDialog(aBoolean));
  }

  private void showBusyDialog(Boolean isShow) {
    if (isShow) {
      handler.post(
          () ->
              DialogUtil.showAlertDialog(
                  CustomChatP2PActivity.this, getString(R.string.one_on_one_other_is_busy)));
    }
  }

  private void setTypeState(boolean isTyping) {
    if (isTyping) {
      viewBinding.tvOnline.setText(getString(R.string.one_on_one_chat_inputing));
      viewBinding.ivOnline.setVisibility(View.GONE);
    } else {
      viewBinding.tvOnline.setText(getString(R.string.one_on_one_chat_online));
      viewBinding.ivOnline.setVisibility(View.VISIBLE);
    }
  }

  private void loadP2PFragment() {
    //创建ChatP2PFragment
    P2PChatFragmentBuilder fragmentBuilder = new P2PChatFragmentBuilder();
    chatP2PFragment = fragmentBuilder.build();
    Bundle bundle = new Bundle();
    bundle.putSerializable(RouterConstant.CHAT_ID_KRY, accId);
    bundle.putSerializable(RouterConstant.CHAT_KRY, userInfo);
    chatP2PFragment.setArguments(bundle);
    //将ChatP2PFragment加载到Activity中
    FragmentManager fragmentManager = getSupportFragmentManager();
    fragmentManager
        .beginTransaction()
        .add(R.id.chat_container, chatP2PFragment)
        .commitAllowingStateLoss();
  }

  private void handleEvent() {
    viewBinding.ivBack.setOnClickListener(v -> finish());
    viewBinding.ivSetting.setOnClickListener(
        v -> {
          SettingPopupWindows popupWindows = new SettingPopupWindows(CustomChatP2PActivity.this);
          PopupWindowCompat.showAsDropDown(
              popupWindows, viewBinding.ivSetting, -1 * SizeUtils.dp2px(20), 0, Gravity.NO_GRAVITY);
        });
  }

  @Override
  protected void onStart() {
    super.onStart();
    if (mAudioTv == null) {
      initView(chatP2PFragment.getView());
    }
  }

  @Override
  protected View getRootView() {
    viewBinding = ActivityMyChatP2pBinding.inflate(getLayoutInflater());
    return viewBinding.getRoot();
  }

  @SuppressLint("ClickableViewAccessibility")
  private void initView(View view) {
    mAudioIv = view.findViewById(R.id.chat_message_input_audio_iv);
    mEmojiIv = view.findViewById(R.id.chat_message_input_emoji_iv);
    mGiftIv = view.findViewById(R.id.chat_message_input_gift_iv);
    mAudioTv = view.findViewById(R.id.chat_message_input_aduio_tv);
    EmojiPickerView emojiPickerView = view.findViewById(R.id.chat_message_emoji_view);
    emojiPickerView
        .getViewTreeObserver()
        .addOnGlobalLayoutListener(
            () -> {
              if (emojiPickerView.getVisibility() == View.VISIBLE) {
                mEmojiIv.setImageResource(R.drawable.one_on_one_chat_emoji_red_icon);
              } else {
                mEmojiIv.setImageResource(R.drawable.one_on_one_chat_emoji_icon);
              }
            });
    messageInputLayout = view.findViewById(R.id.chatBottomInputLayout);
    if (ChatUtil.isSystemAccount(accId)) {
      messageInputLayout.setVisibility(View.GONE);
      viewBinding.ivSetting.setVisibility(View.GONE);
      if (Constants.ASSIST_ACCOUNT.equals(accId)) {
        viewBinding.tvTitle.setText(getString(R.string.one_one_one_littie_secre));
      }
    }
    viewBinding.llVirtualTips.setVisibility(
        (ChatUtil.isVirtualManSession(accId) && !OneOnOneUI.getInstance().isOversea())
            ? View.VISIBLE
            : View.GONE);
    mAudioTv.setOnTouchListener(
        (v, event) -> {
          if (event.getAction() == MotionEvent.ACTION_DOWN) {
            if (OneOnOneUtils.isInVoiceRoom()) {
              ToastX.showShortToast(R.string.one_on_one_other_you_are_in_the_chatroom);
            } else if (CallKitUtil.isInTheCall()) {
              ToastX.showShortToast(R.string.ec_in_the_call_tips);
            } else {
              showAudioInputDialog();
            }
          } else if (event.getAction() == MotionEvent.ACTION_MOVE) {
            if (dialog != null) {
              int x = (int) event.getRawX();
              int y = (int) event.getRawY();
              isInsideView = dialog.getFlViewRect().contains(x, y);
              dialog.showCancelAudioSendUI(isInsideView);
            }
          } else if (event.getAction() == MotionEvent.ACTION_UP) {
            if (dialog != null) {
              audioInputManager.endAudioRecord(isInsideView);
              dismissAudioInputDialog();
            }
          }
          return true;
        });

    mAudioTv
        .getViewTreeObserver()
        .addOnGlobalLayoutListener(
            () -> {
              if (mAudioTv.getVisibility() == View.VISIBLE) {
                int[] location = new int[2];
                mAudioTv.getLocationOnScreen(location);
                int viewLeft = location[0];
                int viewTop = location[1];
                int viewRight = viewLeft + mAudioTv.getWidth();
                int viewBottom = viewTop + mAudioTv.getHeight();
                if (dialog != null && dialog.isShowing()) {
                  dialog.refreshAudioInputLayout(viewLeft, viewTop, viewRight, viewBottom);
                }
              }
            });

    mAudioIv.setOnClickListener(
        v -> {
          InputMethodManager imm =
              (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
          if (imm.isActive()) {
            // 隐藏软键盘
            imm.hideSoftInputFromWindow(v.getApplicationWindowToken(), 0);
          }
          if (mAudioTv.getVisibility() == View.VISIBLE) {
            mAudioTv.setVisibility(View.GONE);
          } else {
            mAudioTv.setVisibility(View.VISIBLE);
          }
        });
    mAudioIv
        .getViewTreeObserver()
        .addOnGlobalLayoutListener(
            new ViewTreeObserver.OnGlobalLayoutListener() {
              @Override
              public void onGlobalLayout() {
                int[] location = new int[2];
                mAudioIv.getLocationOnScreen(location);
                if (audioIvNormalViewTop == 0) {
                  audioIvNormalViewTop = location[1];
                }
                if (location[1] < audioIvNormalViewTop) {
                  // 点击了更多选项，则隐藏按住说话按钮，改为默认的输入框
                  if (mAudioTv.getVisibility() == View.VISIBLE) {
                    mAudioTv.setVisibility(View.GONE);
                  }
                }
              }
            });
    mEmojiIv.setOnClickListener(v -> messageInputLayout.switchEmoji());

    mGiftIv.setOnClickListener(v -> showGiftDialog());
  }

  private void showGiftDialog() {
    GiftDialog giftDialog = new GiftDialog(CustomChatP2PActivity.this);
    giftDialog.show(
        (giftId, giftCount) ->
            HttpService.getInstance()
                .reward(
                    giftId,
                    giftCount,
                    accId,
                    new Callback<ModelResponse<Boolean>>() {

                      @Override
                      public void onResponse(
                          Call<ModelResponse<Boolean>> call,
                          Response<ModelResponse<Boolean>> response) {}

                      @Override
                      public void onFailure(Call<ModelResponse<Boolean>> call, Throwable t) {}
                    }));
  }

  private void dismissAudioInputDialog() {
    if (dialog != null && dialog.isShowing()) {
      dialog.dismiss();
    }
  }

  private void showAudioInputDialog() {
    if (!PermissionUtils.hasPermissions(this, Manifest.permission.RECORD_AUDIO)) {
      permissionPop =
          new TopPopupWindow(
              CustomChatP2PActivity.this,
              R.string.app_permission_tip_microphone_title,
              R.string.app_permission_tip_microphone_content);
      viewBinding
          .getRoot()
          .post(
              () -> {
                permissionPop.showAtLocation(viewBinding.getRoot(), Gravity.TOP, 0, 100);
              });
      permissionLauncher.launch(new String[] {Manifest.permission.RECORD_AUDIO});
      return;
    }
    audioInputManager.initAudioRecord(this);
    audioInputManager.startAudioRecord();
    if (dialog == null) {
      dialog = new AudioInputDialog(CustomChatP2PActivity.this, accId, audioInputManager);
    }
    if (!dialog.isShowing()) {
      dialog.show();
      dialog.showCancelAudioSendUI(false);
    }
  }

  public String getPermissionText(String permission) {
    String text = getString(com.netease.yunxin.kit.chatkit.ui.R.string.permission_default);
    if (TextUtils.equals(permission, Manifest.permission.CAMERA)) {
      text = getString(com.netease.yunxin.kit.chatkit.ui.R.string.permission_camera);
    } else if (TextUtils.equals(permission, Manifest.permission.READ_EXTERNAL_STORAGE)) {
      text = getString(com.netease.yunxin.kit.chatkit.ui.R.string.permission_storage);
    } else if (TextUtils.equals(permission, Manifest.permission.RECORD_AUDIO)) {
      text = getString(com.netease.yunxin.kit.chatkit.ui.R.string.permission_audio);
    }
    return text;
  }

  @Override
  protected void onDestroy() {
    viewBinding = null;
    audioInputManager.destroy();
    chatUIConfigManager.destroy();
    if (giftRender != null) {
      giftRender.release();
    }
    NECallEngine.sharedInstance().removeCallDelegate(callEngineDelegate);
    handler.removeCallbacksAndMessages(null);
    super.onDestroy();
  }

  @Override
  protected void onPause() {
    super.onPause();
    audioInputManager.pause();
    dismissAudioInputDialog();
    viewModel.registerReceiveMessageObserve(false);
  }

  @Override
  protected void onNewIntent(Intent intent) {
    super.onNewIntent(intent);
    ALog.i(TAG, "onNewIntent,intent:" + intent);
    chatP2PFragment.onNewIntent(intent);
  }

  private void initGiftAnimation() {
    GifAnimationView gifAnimationView = new GifAnimationView(this);
    int size = ScreenUtils.getDisplayWidth();
    FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(size, size);
    layoutParams.gravity = Gravity.CENTER;
    FrameLayout contentParent = getWindow().getDecorView().findViewById(android.R.id.content);
    contentParent.addView(gifAnimationView, layoutParams);
    gifAnimationView.bringToFront();
    giftRender = new GiftRender();
    giftRender.init(gifAnimationView);
  }

  @Override
  protected void onResume() {
    super.onResume();
    viewModel.registerReceiveMessageObserve(true);
  }
}
