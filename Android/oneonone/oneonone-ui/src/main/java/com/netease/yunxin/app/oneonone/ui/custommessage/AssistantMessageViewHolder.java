// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.app.oneonone.ui.custommessage;

import android.app.Activity;
import android.net.Uri;
import android.text.Html;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.text.style.URLSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import com.netease.yunxin.app.oneonone.ui.R;
import com.netease.yunxin.app.oneonone.ui.databinding.OneOnOneChatAssistantLeftHolderBinding;
import com.netease.yunxin.kit.alog.ALog;
import com.netease.yunxin.kit.chatkit.ui.databinding.ChatBaseMessageViewHolderBinding;
import com.netease.yunxin.kit.chatkit.ui.model.ChatMessageBean;
import com.netease.yunxin.kit.chatkit.ui.normal.view.message.viewholder.NormalChatBaseMessageViewHolder;
import com.netease.yunxin.kit.corekit.im2.utils.RouterConstant;
import com.netease.yunxin.kit.corekit.route.XKitRouter;

public class AssistantMessageViewHolder extends NormalChatBaseMessageViewHolder {
  private static final String TAG = "AssistantMessageViewHolder";
  private View rootView;

  public AssistantMessageViewHolder(
      @NonNull ChatBaseMessageViewHolderBinding parent, int viewType) {
    super(parent, viewType);
  }

  @Override
  public void addViewToMessageContainer() {
    OneOnOneChatAssistantLeftHolderBinding leftBinding =
        OneOnOneChatAssistantLeftHolderBinding.inflate(
            LayoutInflater.from(parent.getContext()), getMessageContainer(), true);
    rootView = leftBinding.getRoot();
  }

  @Override
  public void bindData(ChatMessageBean message, ChatMessageBean lastMessage) {
    super.bindData(message, lastMessage);

    AssistantAttachment attachment = (AssistantAttachment) message.getMessageData().getAttachment();
    if (attachment == null) {
      return;
    }

    TextView contentTV = rootView.findViewById(R.id.tv_content);
    contentTV.setText(getClickableHtml(attachment.getMsg()));
    contentTV.setMovementMethod(LinkMovementMethod.getInstance());
  }

  private void setLinkClickable(
      final SpannableStringBuilder clickableHtmlBuilder, final URLSpan urlSpan) {
    int start = clickableHtmlBuilder.getSpanStart(urlSpan);
    int end = clickableHtmlBuilder.getSpanEnd(urlSpan);
    int flags = clickableHtmlBuilder.getSpanFlags(urlSpan);
    ClickableSpan clickableSpan =
        new ClickableSpan() {

          public void onClick(View view) {
            String url = urlSpan.getURL();
            if (url.startsWith("party://chat/p2pChat")) {
              String user = Uri.parse(url).getQueryParameter("user");
              if (!TextUtils.isEmpty(user)) {
                if (view.getContext() instanceof Activity) {
                  ((Activity) view.getContext()).finish();
                }
                XKitRouter.withKey(RouterConstant.PATH_CHAT_P2P_PAGE)
                    .withParam(RouterConstant.CHAT_ID_KRY, user)
                    .withContext(view.getContext())
                    .navigate();
              } else {
                ALog.e(TAG, "onClick but user is empty");
              }
            }
          }

          public void updateDrawState(TextPaint ds) {
            //设置颜色
            ds.setColor(ContextCompat.getColor(rootView.getContext(), R.color.color_2a6bf2));
            //设置是否要下划线
            ds.setUnderlineText(false);
          }
        };
    clickableHtmlBuilder.setSpan(clickableSpan, start, end, flags);
  }

  private CharSequence getClickableHtml(String html) {
    Spanned spannedHtml = Html.fromHtml(html);
    SpannableStringBuilder clickableHtmlBuilder = new SpannableStringBuilder(spannedHtml);
    URLSpan[] urls = clickableHtmlBuilder.getSpans(0, spannedHtml.length(), URLSpan.class);
    for (final URLSpan span : urls) {
      setLinkClickable(clickableHtmlBuilder, span);
    }
    return clickableHtmlBuilder;
  }
}
