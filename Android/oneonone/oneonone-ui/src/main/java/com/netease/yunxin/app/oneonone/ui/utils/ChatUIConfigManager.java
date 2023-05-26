// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.app.oneonone.ui.utils;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.text.TextUtils;
import android.view.View;
import android.widget.FrameLayout;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.netease.nimlib.sdk.avsignalling.constant.ChannelType;
import com.netease.nimlib.sdk.msg.attachment.NetCallAttachment;
import com.netease.nimlib.sdk.msg.constant.MsgTypeEnum;
import com.netease.nimlib.sdk.msg.model.IMMessage;
import com.netease.yunxin.app.oneonone.ui.R;
import com.netease.yunxin.app.oneonone.ui.activity.CustomChatP2PActivity;
import com.netease.yunxin.app.oneonone.ui.constant.CallConfig;
import com.netease.yunxin.app.oneonone.ui.dialog.HotTopicDialog;
import com.netease.yunxin.app.oneonone.ui.http.HttpService;
import com.netease.yunxin.app.oneonone.ui.model.ModelResponse;
import com.netease.yunxin.app.oneonone.ui.model.User;
import com.netease.yunxin.app.oneonone.ui.view.CustomChatBottomView;
import com.netease.yunxin.kit.alog.ALog;
import com.netease.yunxin.kit.chatkit.ui.ChatKitClient;
import com.netease.yunxin.kit.chatkit.ui.ChatUIConfig;
import com.netease.yunxin.kit.chatkit.ui.IChatInputMenu;
import com.netease.yunxin.kit.chatkit.ui.model.ChatMessageBean;
import com.netease.yunxin.kit.chatkit.ui.view.input.ActionConstants;
import com.netease.yunxin.kit.chatkit.ui.view.input.InputProperties;
import com.netease.yunxin.kit.chatkit.ui.view.interfaces.IMessageItemClickListener;
import com.netease.yunxin.kit.chatkit.ui.view.message.MessageProperties;
import com.netease.yunxin.kit.chatkit.ui.view.popmenu.ChatPopMenuAction;
import com.netease.yunxin.kit.chatkit.ui.view.popmenu.IChatPopMenu;
import com.netease.yunxin.kit.common.ui.action.ActionItem;
import com.netease.yunxin.kit.common.ui.dialog.CommonAlertDialog;
import com.netease.yunxin.kit.common.ui.utils.ToastX;
import com.netease.yunxin.kit.corekit.im.model.UserInfo;
import com.netease.yunxin.kit.corekit.service.XKitServiceManager;
import com.netease.yunxin.kit.entertainment.common.gift.GiftDialog;
import com.netease.yunxin.kit.entertainment.common.utils.ClickUtils;
import com.netease.yunxin.kit.entertainment.common.utils.DialogUtil;
import com.netease.yunxin.kit.entertainment.common.utils.ReportUtils;
import com.netease.yunxin.kit.entertainment.common.utils.UserInfoManager;
import java.util.ArrayList;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ChatUIConfigManager {
  private static final String VOICE_ROOM_SERVICE_NAME = "VoiceRoomKit";
  private static final String TAG = "ChatUIConfigUtil";
  private static final String ACTION_TYPE_TOPIC = "TOPIC";
  private static final String ONLINE = "online";
  private UserInfo userInfo;
  private String sessionId;

  public void initChatUIConfig(Context context, String sessionID) {
    this.sessionId = sessionID;
    ChatUIConfig chatUIConfig = new ChatUIConfig();
    // 会话页面自定义背景
    initChatBgPage(context, chatUIConfig);
    initChatPopMenu(chatUIConfig);
    // 输入框底部更多菜单栏
    initChatInputMenu(chatUIConfig);
    initChatMessageItemConfig(context, chatUIConfig);
    initChatBottomView(context, chatUIConfig);
    ChatKitClient.setChatUIConfig(chatUIConfig);
  }

  private void initChatBottomView(Context context, ChatUIConfig chatUIConfig) {
    chatUIConfig.inputProperties = new InputProperties();
    chatUIConfig.inputProperties.inputBarBg =
        new ColorDrawable(context.getResources().getColor(R.color.white));
    chatUIConfig.inputProperties.inputEditBg =
        new ColorDrawable(context.getResources().getColor(R.color.color_f1f2f4));
    chatUIConfig.inputProperties.inputEditHintTextColor =
        context.getResources().getColor(R.color.color_grey_999999);
    chatUIConfig.chatViewCustom =
        layout -> {
          // 从ChatView中获取底部消息类型布局自定义
          FrameLayout frameLayout = layout.getChatBodyBottomLayout();
          frameLayout.setBackgroundColor(Color.TRANSPARENT);
          CustomChatBottomView customChatBottomView = new CustomChatBottomView(context);
          customChatBottomView.setBackgroundColor(Color.TRANSPARENT);
          customChatBottomView.setSessionId(sessionId);
          frameLayout.addView(customChatBottomView);
        };
  }

  private void initChatPopMenu(ChatUIConfig chatUIConfig) {
    chatUIConfig.chatPopMenu =
        new IChatPopMenu() {
          @NonNull
          @Override
          public List<ChatPopMenuAction> customizePopMenu(
              List<ChatPopMenuAction> menuList, ChatMessageBean messageBean) {
            return generateCustomPopMenuAction(menuList, messageBean);
          }

          @Override
          public boolean showDefaultPopMenu() {
            return IChatPopMenu.super.showDefaultPopMenu();
          }
        };
  }

  private List<ChatPopMenuAction> generateCustomPopMenuAction(
      List<ChatPopMenuAction> menuList, ChatMessageBean messageBean) {
    List<ChatPopMenuAction> menuNewList = new ArrayList<>(3);
    UserInfo fromUser = messageBean.getMessageData().getFromUser();
    boolean isSelf =
        fromUser != null
            && TextUtils.equals(fromUser.getAccount(), UserInfoManager.getSelfImAccid());
    if (MsgTypeEnum.text.getValue() == messageBean.getViewType()) {
      int size = 2;
      if (isSelf) {
        size = 3;
      }
      for (int i = 0; i < size; i++) {
        menuNewList.add(
            new ChatPopMenuAction(
                "", "", com.netease.yunxin.kit.chatkit.ui.R.drawable.ic_message_copy));
      }
      for (ChatPopMenuAction chatPopMenuAction : menuList) {
        if (TextUtils.equals(ActionConstants.POP_ACTION_COPY, chatPopMenuAction.getAction())) {
          menuNewList.set(0, chatPopMenuAction);
        } else if (TextUtils.equals(
            ActionConstants.POP_ACTION_DELETE, chatPopMenuAction.getAction())) {
          menuNewList.set(1, chatPopMenuAction);
        } else if (isSelf
            && TextUtils.equals(ActionConstants.POP_ACTION_RECALL, chatPopMenuAction.getAction())) {
          menuNewList.set(2, chatPopMenuAction);
        }
      }
    } else {
      int size = 1;
      if (isSelf && !ChatUtil.isGiftMessageType(messageBean.getMessageData())) {
        size = 2;
      }
      for (int i = 0; i < size; i++) {
        menuNewList.add(
            new ChatPopMenuAction(
                "", "", com.netease.yunxin.kit.chatkit.ui.R.drawable.ic_message_copy));
      }
      for (ChatPopMenuAction chatPopMenuAction : menuList) {
        if (TextUtils.equals(ActionConstants.POP_ACTION_DELETE, chatPopMenuAction.getAction())) {
          menuNewList.set(0, chatPopMenuAction);
        } else if (!ChatUtil.isGiftMessageType(messageBean.getMessageData())
            && isSelf
            && TextUtils.equals(ActionConstants.POP_ACTION_RECALL, chatPopMenuAction.getAction())) {
          menuNewList.set(1, chatPopMenuAction);
        }
      }
    }
    return menuNewList;
  }

  private void initChatMessageItemConfig(Context context, ChatUIConfig chatUIConfig) {
    chatUIConfig.messageItemClickListener =
        new IMessageItemClickListener() {
          @Override
          public boolean onCustomClick(View view, int position, ChatMessageBean messageInfo) {
            if (ChatUtil.isGiftMessageType(messageInfo.getMessageData())) {
              showGiftDialog(context);
            }
            return true;
          }

          @Override
          public boolean onMessageClick(View view, int position, ChatMessageBean messageInfo) {
            if (messageInfo.getViewType() == MsgTypeEnum.nrtc_netcall.getValue()) {
              IMMessage message = messageInfo.getMessageData().getMessage();
              if (message.getAttachment() instanceof NetCallAttachment) {
                NetCallAttachment attachment = (NetCallAttachment) message.getAttachment();
                int type = attachment.getType();
                if (type == ChannelType.AUDIO.getValue()) {
                  handleAudioCallAction(context);
                } else {
                  handleVideoCallAction(context);
                }
              }
              return true;
            }
            return IMessageItemClickListener.super.onMessageClick(view, position, messageInfo);
          }
        };
  }

  private void initChatBgPage(Context context, ChatUIConfig chatUIConfig) {
    chatUIConfig.messageProperties = new MessageProperties();
    chatUIConfig.messageProperties.showTitleBar = false;
    chatUIConfig.messageProperties.chatViewBackground =
        context.getResources().getDrawable(R.drawable.one_on_one_chat_p2p_bg);
    chatUIConfig.messageProperties.selfMessageRes = R.drawable.one_on_one_chat_message_self_bg;
    chatUIConfig.messageProperties.receiveMessageRes = R.drawable.one_on_one_chat_message_other_bg;
  }

  private void initChatInputMenu(ChatUIConfig chatUIConfig) {
    chatUIConfig.chatInputMenu =
        new IChatInputMenu() {
          @Override
          public List<ActionItem> customizeInputBar(List<ActionItem> actionItemList) {
            return generateCustomInputBar(actionItemList);
          }

          @Override
          public List<ActionItem> customizeInputMore(List<ActionItem> actionItemList) {
            return generateCustomInputMore(actionItemList);
          }

          @Override
          public boolean onCustomInputClick(Context context, View view, String action) {
            if (ActionConstants.ACTION_TYPE_AUDIO_CALL_ACTION.equals(action)) {
              ReportUtils.report(context, CustomChatP2PActivity.TAG_REPORT, "xiaoxi_voicecall");
              handleAudioCallAction(context);
              return true;
            } else if (ActionConstants.ACTION_TYPE_VIDEO_CALL_ACTION.equals(action)) {
              ReportUtils.report(context, CustomChatP2PActivity.TAG_REPORT, "xiaoxi_videocall");
              handleVideoCallAction(context);
              return true;
            } else if (ACTION_TYPE_TOPIC.equals(action)) {
              handleTopicAction(context);
              return true;
            }
            return false;
          }

          @Override
          public boolean onInputClick(Context context, View view, String action) {
            if (ActionConstants.ACTION_TYPE_CAMERA.equals(action)) {
              if (OneOnOneUtils.isInVoiceRoom()) {
                ToastX.showShortToast(R.string.one_on_one_other_you_are_in_the_chatroom);
                return true;
              } else {
                return false;
              }
            }

            return IChatInputMenu.super.onInputClick(context, view, action);
          }
        };
  }

  private void handleTopicAction(Context context) {

    if (!ClickUtils.isFastClick()) {
      AppCompatActivity topActivity = (AppCompatActivity) context;
      new HotTopicDialog(sessionId)
          .show(topActivity.getSupportFragmentManager(), HotTopicDialog.TAG);
    }
  }

  private void handleVideoCallAction(Context context) {
    if (!ClickUtils.isFastClick()) {
      Object result =
          XKitServiceManager.Companion.getInstance()
              .callService(VOICE_ROOM_SERVICE_NAME, "getCurrentRoomInfo", null);
      if (result instanceof Boolean && (boolean) result) {
        showTipsDialog(
            (AppCompatActivity) context,
            context.getString(R.string.one_on_one_other_you_are_in_the_chatroom));
      } else {
        if (userInfo == null) {
          HttpService.getInstance()
              .getUserInfo(
                  sessionId,
                  new Callback<ModelResponse<User>>() {
                    @Override
                    public void onResponse(
                        Call<ModelResponse<User>> call, Response<ModelResponse<User>> response) {
                      if (response.body() == null) {
                        return;
                      }
                      User data = response.body().data;
                      if (data != null) {
                        userInfo = UserInfoUtil.generateUserInfo(data);
                        queryOnlineStateThenVideoCall(context, userInfo);
                      }
                    }

                    @Override
                    public void onFailure(Call<ModelResponse<User>> call, Throwable t) {
                      ALog.e(TAG, "ChatUIConfigUtil getUserInfo failed,t:" + t);
                    }
                  });
        } else {
          queryOnlineStateThenVideoCall(context, userInfo);
        }
      }
    }
  }

  private void queryOnlineStateThenVideoCall(Context context, UserInfo userInfo) {
    HttpService.getInstance()
        .getUserState(
            userInfo.getMobile(),
            new Callback<ModelResponse<String>>() {
              @Override
              public void onResponse(
                  Call<ModelResponse<String>> call, Response<ModelResponse<String>> response) {
                if (response.body() != null && response.body().code == 200) {
                  String onlineState = response.body().data;
                  if (ONLINE.equals(onlineState)) {
                    NavUtils.toCallVideoPage(context, userInfo);
                  } else {
                    DialogUtil.showAlertDialog(
                        (AppCompatActivity) context,
                        context.getString(
                            R
                                .string
                                .one_on_one_other_is_not_online));
                  }
                }
              }

              @Override
              public void onFailure(Call<ModelResponse<String>> call, Throwable t) {
                ALog.e(TAG, "getUserState t:" + t);
              }
            });
  }

  private void handleAudioCallAction(Context context) {
    if (!ClickUtils.isFastClick()) {
      Object result =
          XKitServiceManager.Companion.getInstance()
              .callService(VOICE_ROOM_SERVICE_NAME, "getCurrentRoomInfo", null);
      if (result instanceof Boolean && (boolean) result) {
        showTipsDialog(
            (AppCompatActivity) context,
            context.getString(R.string.one_on_one_other_you_are_in_the_chatroom));
      } else {
        if (userInfo == null) {
          HttpService.getInstance()
              .getUserInfo(
                  sessionId,
                  new Callback<ModelResponse<User>>() {
                    @Override
                    public void onResponse(
                        Call<ModelResponse<User>> call, Response<ModelResponse<User>> response) {
                      if (response.body() == null) {
                        return;
                      }
                      User data = response.body().data;
                      if (data != null) {
                        userInfo = UserInfoUtil.generateUserInfo(data);
                        queryOnlineStateThenAudioCall(context, userInfo);
                      }
                    }

                    @Override
                    public void onFailure(Call<ModelResponse<User>> call, Throwable t) {
                      ALog.e(TAG, "ChatUIConfigUtil getUserInfo failed,t:" + t);
                    }
                  });
        } else {
          queryOnlineStateThenAudioCall(context, userInfo);
        }
      }
    }
  }

  private void queryOnlineStateThenAudioCall(Context context, UserInfo userInfo) {
    HttpService.getInstance()
        .getUserState(
            userInfo.getMobile(),
            new Callback<ModelResponse<String>>() {
              @Override
              public void onResponse(
                  Call<ModelResponse<String>> call, Response<ModelResponse<String>> response) {
                if (response.body() != null && response.body().code == 200) {
                  String onlineState = response.body().data;
                  if (ONLINE.equals(onlineState)) {
                    NavUtils.toCallAudioPage(
                        context, userInfo, CallConfig.CALL_PSTN_WAIT_MILLISECONDS);
                  } else {
                    NavUtils.toCallAudioPage(context, userInfo, 0);
                  }
                }
              }

              @Override
              public void onFailure(Call<ModelResponse<String>> call, Throwable t) {
                ALog.e(TAG, "getUserState t:" + t);
              }
            });
  }

  private static List<ActionItem> generateCustomInputMore(List<ActionItem> actionItemList) {
    //个性化配置更多面板中按钮，actionItemList传递默认配置的四个按钮：拍摄、文件、位置和音视频通话。
    List<ActionItem> actionItemNewList = new ArrayList<>();
    ActionItem cameraItem = null;
    ActionItem locationItem = null;
    for (ActionItem actionItem : actionItemList) {
      if (TextUtils.equals(actionItem.getAction(), ActionConstants.ACTION_TYPE_CAMERA)) {
        cameraItem = actionItem;
        cameraItem.setIconResId(R.drawable.one_on_one_chat_camera);
      } else if (TextUtils.equals(actionItem.getAction(), ActionConstants.ACTION_TYPE_LOCATION)) {
        locationItem = actionItem;
        locationItem.setIconResId(R.drawable.one_on_one_chat_location);
      }
    }
    if (cameraItem != null) {
      actionItemNewList.add(cameraItem);
    }
    if (locationItem != null) {
      actionItemNewList.add(locationItem);
    }
    return actionItemNewList;
  }

  private List<ActionItem> generateCustomInputBar(List<ActionItem> actionItemList) {
    //个性化配置输入框下方按钮，actionItemList传递默认配置的四个按钮：语音、表情、图片和更多。
    //您可以根据自己的需求修改actionItemList的数量和顺序，ChatKit-UI会按照您的返回结果展示。
    List<ActionItem> actionItemNewList = new ArrayList<>();
    ActionItem albumItem = null;
    ActionItem moreItem = null;
    for (ActionItem actionItem : actionItemList) {
      if (TextUtils.equals(actionItem.getAction(), ActionConstants.ACTION_TYPE_ALBUM)) {
        albumItem = actionItem;
      } else if (TextUtils.equals(actionItem.getAction(), ActionConstants.ACTION_TYPE_MORE)) {
        moreItem = actionItem;
      }
    }
    if (albumItem != null) {
      albumItem.setIconResId(R.drawable.one_on_one_chat_album);
      actionItemNewList.add(albumItem);
    }
    actionItemNewList.add(
        new ActionItem(
            ActionConstants.ACTION_TYPE_AUDIO_CALL_ACTION, R.drawable.one_on_one_chat_audio_call));
    actionItemNewList.add(
        new ActionItem(
            ActionConstants.ACTION_TYPE_VIDEO_CALL_ACTION, R.drawable.one_on_one_chat_video_call));
    actionItemNewList.add(new ActionItem(ACTION_TYPE_TOPIC, R.drawable.one_on_one_chat_topic));
    if (moreItem != null) {
      moreItem.setIconResId(R.drawable.one_on_one_chat_more_selector);
      actionItemNewList.add(moreItem);
    }
    return actionItemNewList;
  }

  private static void showTipsDialog(AppCompatActivity activity, String content) {
    CommonAlertDialog commonDialog = new CommonAlertDialog();
    commonDialog
        .setTitleStr(content)
        .setPositiveStr(activity.getString(R.string.one_on_one_confirm))
        .setConfirmListener(() -> {})
        .show(activity.getSupportFragmentManager());
  }

  public void setUserInfo(UserInfo userInfo) {
    this.userInfo = userInfo;
  }

  private void showGiftDialog(Context context) {
    GiftDialog giftDialog = new GiftDialog((Activity) context);
    giftDialog.show(
        (giftId, giftCount, userUuids) ->
            HttpService.getInstance()
                .reward(
                    giftId,
                    giftCount,
                    sessionId,
                    new Callback<ModelResponse<Boolean>>() {

                      @Override
                      public void onResponse(
                          Call<ModelResponse<Boolean>> call,
                          Response<ModelResponse<Boolean>> response) {}

                      @Override
                      public void onFailure(Call<ModelResponse<Boolean>> call, Throwable t) {}
                    }));
  }

  public void destroy() {
    userInfo = null;
    sessionId = null;
  }
}
