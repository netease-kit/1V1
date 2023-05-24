// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.app.oneonone.ui.custommessage;

import android.view.LayoutInflater;
import android.view.View;
import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import com.netease.yunxin.app.oneonone.ui.R;
import com.netease.yunxin.kit.chatkit.ui.databinding.ChatBaseMessageViewHolderBinding;
import com.netease.yunxin.kit.chatkit.ui.model.ChatMessageBean;
import com.netease.yunxin.kit.chatkit.ui.view.message.viewholder.ChatBaseMessageViewHolder;

public class CommonRiskMessageViewHolder extends ChatBaseMessageViewHolder {
  private static final String TAG = "CommonRiskMessageViewHolder";

  public CommonRiskMessageViewHolder(
      @NonNull ChatBaseMessageViewHolderBinding parent, int viewType) {
    super(parent, viewType);
  }

  @Override
  public void bindData(ChatMessageBean message, ChatMessageBean lastMessage) {
    super.bindData(message, lastMessage);
    baseViewBinding.baseRoot.removeAllViews();
    View view =
        LayoutInflater.from(parent.getContext())
            .inflate(R.layout.one_on_one_chat_common_risk_holder, null);
    baseViewBinding.baseRoot.addView(view);
    ConstraintLayout.LayoutParams layoutParams =
        (ConstraintLayout.LayoutParams) view.getLayoutParams();
    layoutParams.leftToLeft = R.id.baseRoot;
    layoutParams.rightToRight = R.id.baseRoot;
    view.setLayoutParams(layoutParams);
  }
}
