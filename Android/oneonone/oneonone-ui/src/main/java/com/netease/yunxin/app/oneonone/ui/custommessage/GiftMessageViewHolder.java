// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.app.oneonone.ui.custommessage;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import com.netease.yunxin.app.oneonone.ui.R;
import com.netease.yunxin.app.oneonone.ui.databinding.OneOnOneChatGiftLeftHolderBinding;
import com.netease.yunxin.app.oneonone.ui.databinding.OneOnOneChatGiftRightHolderBinding;
import com.netease.yunxin.kit.chatkit.ui.databinding.ChatBaseMessageViewHolderBinding;
import com.netease.yunxin.kit.chatkit.ui.model.ChatMessageBean;
import com.netease.yunxin.kit.chatkit.ui.normal.view.message.viewholder.NormalChatBaseMessageViewHolder;
import com.netease.yunxin.kit.entertainment.common.gift.GiftCache;
import com.netease.yunxin.kit.entertainment.common.utils.UserInfoManager;

public class GiftMessageViewHolder extends NormalChatBaseMessageViewHolder {
  private static final String TAG = "GiftMessageViewHolder";
  private View rootView;
  private boolean isRight = false;

  public GiftMessageViewHolder(@NonNull ChatBaseMessageViewHolderBinding parent, int viewType) {
    super(parent, viewType);
  }

  @Override
  public void addViewToMessageContainer() {
    if (isRight) {
      OneOnOneChatGiftRightHolderBinding rightBinding =
          OneOnOneChatGiftRightHolderBinding.inflate(
              LayoutInflater.from(parent.getContext()), getMessageContainer(), true);
      rootView = rightBinding.getRoot();
    } else {
      OneOnOneChatGiftLeftHolderBinding leftBinding =
          OneOnOneChatGiftLeftHolderBinding.inflate(
              LayoutInflater.from(parent.getContext()), getMessageContainer(), true);
      rootView = leftBinding.getRoot();
    }
  }

  @Override
  public void bindData(ChatMessageBean message, ChatMessageBean lastMessage) {
    GiftAttachment attachment =
        (GiftAttachment) message.getMessageData().getMessage().getAttachment();
    if (attachment == null) {
      return;
    }
    if (TextUtils.equals(UserInfoManager.getSelfUserUuid(), attachment.getTargetUserUuid())) {
      isRight = false;
    } else {
      isRight = true;
    }
    super.bindData(message, lastMessage);
    ImageView giftIV = rootView.findViewById(R.id.iv_gift);
    TextView giftInfoIV = rootView.findViewById(R.id.tv_gift_info);
    TextView sendGiftIV = rootView.findViewById(R.id.tv_send_gift);
    int giftResId = R.drawable.icon_gift_plan;
    giftIV.setBackgroundResource(GiftCache.getGift(attachment.getGiftId()).getStaticIconResId());
    giftInfoIV.setText(
        GiftCache.getGift(attachment.getGiftId()).getName() + " x" + attachment.getGiftCount());
    sendGiftIV.setOnClickListener(
        v -> {
          itemClickListener.onCustomClick(v, position, message);
        });
  }
}
