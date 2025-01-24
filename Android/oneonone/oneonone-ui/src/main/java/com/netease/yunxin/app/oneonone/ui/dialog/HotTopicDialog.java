// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.app.oneonone.ui.dialog;

import android.annotation.SuppressLint;
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
import com.netease.yunxin.kit.common.ui.dialog.BaseBottomDialog;
import com.netease.yunxin.kit.common.utils.SizeUtils;
import com.netease.yunxin.kit.corekit.im2.extend.ProgressFetchCallback;

public class HotTopicDialog extends BaseBottomDialog {
  public static final String TAG = "HotTopicDialog";
  private String targetAccountId = "";

  public HotTopicDialog(String targetAccountId) {
    this.targetAccountId = targetAccountId;
  }

  @Nullable
  @Override
  protected View getRootView(
      @NonNull LayoutInflater layoutInflater, @Nullable ViewGroup viewGroup) {
    View bottomView =
        LayoutInflater.from(getContext())
            .inflate(R.layout.one_one_one_chat_dialog_hot_topic, viewGroup);
    String[] hotTopics = getContext().getResources().getStringArray(R.array.hot_topic);
    RecyclerView recyclerView = bottomView.findViewById(R.id.recyclerView);
    recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
    recyclerView.addItemDecoration(
        new RecyclerView.ItemDecoration() {
          @Override
          public void getItemOffsets(
              @NonNull Rect outRect,
              @NonNull View view,
              @NonNull RecyclerView parent,
              @NonNull RecyclerView.State state) {
            int dp16 = SizeUtils.dp2px(16f);
            int dp12 = SizeUtils.dp2px(12f);
            outRect.set(dp16, 0, dp16, dp12);
          }
        });
    recyclerView.setAdapter(
        new RecyclerView.Adapter() {
          @NonNull
          @Override
          public RecyclerView.ViewHolder onCreateViewHolder(
              @NonNull ViewGroup parent, int viewType) {
            View view =
                LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.one_on_one_chat_item_topic, parent, false);
            return new TopicViewHolder(view);
          }

          @Override
          public void onBindViewHolder(
              @NonNull RecyclerView.ViewHolder holder, @SuppressLint("RecyclerView") int position) {
            TopicViewHolder topicViewHolder = (TopicViewHolder) holder;
            String hotTopic = hotTopics[position];
            topicViewHolder.tv.setText(hotTopic);
            topicViewHolder.itemView.setOnClickListener(
                v -> {
                  ChatUtil.sendTextMessage(
                      targetAccountId,
                      hotTopic,
                      new ProgressFetchCallback<V2NIMSendMessageResult>() {
                        @Override
                        public void onError(int i, @NonNull String s) {
                          ALog.e(TAG, "sendTextMessage onFailed,code:" + i);
                        }

                        @Override
                        public void onSuccess(
                            @Nullable V2NIMSendMessageResult v2NIMSendMessageResult) {
                          ALog.i(TAG, "sendTextMessage success");
                        }

                        @Override
                        public void onProgress(int i) {}
                      });
                  dismiss();
                });
          }

          @Override
          public int getItemCount() {
            return hotTopics.length;
          }
        });
    return bottomView;
  }

  private static class TopicViewHolder extends RecyclerView.ViewHolder {
    private TextView tv;

    public TopicViewHolder(@NonNull View itemView) {
      super(itemView);
      tv = itemView.findViewById(R.id.tv);
    }
  }
}
