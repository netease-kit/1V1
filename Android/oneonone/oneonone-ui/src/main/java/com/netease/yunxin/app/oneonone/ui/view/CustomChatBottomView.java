// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.app.oneonone.ui.view;

import android.content.Context;
import android.graphics.Rect;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.netease.nimlib.sdk.v2.message.result.V2NIMSendMessageResult;
import com.netease.yunxin.app.oneonone.ui.R;
import com.netease.yunxin.app.oneonone.ui.utils.ChatUtil;
import com.netease.yunxin.kit.alog.ALog;
import com.netease.yunxin.kit.common.utils.SizeUtils;
import com.netease.yunxin.kit.corekit.im2.extend.ProgressFetchCallback;

public class CustomChatBottomView extends RecyclerView {
  private static final String TAG = "CustomChatBottomView";
  private String sessionId;

  public CustomChatBottomView(@NonNull Context context) {
    super(context);
    setPadding(0, 0, 0, SizeUtils.dp2px(10));
    setLayoutManager(new LinearLayoutManager(context, HORIZONTAL, false));
    addItemDecoration(
        new ItemDecoration() {
          @Override
          public void getItemOffsets(
              @NonNull Rect outRect,
              @NonNull View view,
              @NonNull RecyclerView parent,
              @NonNull State state) {
            int dp10 = SizeUtils.dp2px(12f);
            outRect.set(dp10, 0, 0, 0);
          }
        });
    String[] topics = getContext().getResources().getStringArray(R.array.recommend_topic);
    setAdapter(
        new Adapter() {
          @NonNull
          @Override
          public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view =
                LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.one_on_one_chat_item_recommend_topic, parent, false);
            return new TopicViewHolder(view);
          }

          @Override
          public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            TopicViewHolder topicViewHolder = (TopicViewHolder) holder;
            String topic = topics[position];
            topicViewHolder.tv.setText(topic);
            topicViewHolder.itemView.setOnClickListener(
                v -> {
                  ChatUtil.sendTextMessage(
                      sessionId,
                      topic,
                      new ProgressFetchCallback<V2NIMSendMessageResult>() {
                        @Override
                        public void onError(int i, @NonNull String s) {
                          ALog.e(TAG, "sendTextMessage onFailed,code:" + i);
                        }

                        @Override
                        public void onSuccess(
                            @Nullable V2NIMSendMessageResult v2NIMSendMessageResult) {
                          ALog.e(TAG, "sendTextMessage success");
                        }

                        @Override
                        public void onProgress(int i) {}
                      });
                });
          }

          @Override
          public int getItemCount() {
            if (ChatUtil.isSystemAccount(sessionId)) {
              return 0;
            }
            return topics.length;
          }
        });
  }

  public void setSessionId(String sessionId) {
    this.sessionId = sessionId;
  }

  private static class TopicViewHolder extends RecyclerView.ViewHolder {
    private TextView tv;

    public TopicViewHolder(@NonNull View itemView) {
      super(itemView);
      tv = itemView.findViewById(R.id.tv);
    }
  }
}
