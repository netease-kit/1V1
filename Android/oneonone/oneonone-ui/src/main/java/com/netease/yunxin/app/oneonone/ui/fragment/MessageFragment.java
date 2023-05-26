// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.app.oneonone.ui.fragment;

import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import com.netease.yunxin.app.oneonone.ui.R;
import com.netease.yunxin.kit.common.ui.dialog.ListAlertDialog;
import com.netease.yunxin.kit.conversationkit.repo.ConversationRepo;
import com.netease.yunxin.kit.conversationkit.ui.ConversationKitClient;
import com.netease.yunxin.kit.conversationkit.ui.ConversationUIConfig;
import com.netease.yunxin.kit.conversationkit.ui.ItemClickListener;
import com.netease.yunxin.kit.conversationkit.ui.common.ConversationConstant;
import com.netease.yunxin.kit.conversationkit.ui.model.ConversationBean;
import com.netease.yunxin.kit.conversationkit.ui.page.ConversationFragment;
import com.netease.yunxin.kit.entertainment.common.statusbar.StatusBarConfig;
import com.netease.yunxin.kit.entertainment.common.utils.DialogUtil;

public class MessageFragment extends ConversationFragment {

  @Nullable
  @Override
  public View onCreateView(
      @NonNull LayoutInflater inflater,
      @Nullable ViewGroup container,
      @Nullable Bundle savedInstanceState) {
    initConfig();
    View roomView = super.onCreateView(inflater, container, savedInstanceState);
    StatusBarConfig.paddingStatusBarHeight(getActivity(), roomView);
    return roomView;
  }

  private void initConfig() {
    ConversationUIConfig uiConfig = new ConversationUIConfig();
    uiConfig.showTitleBarLeftIcon = false;
    uiConfig.titleBarTitle = requireContext().getString(R.string.message);
    uiConfig.showTitleBarRightIcon = false;
    uiConfig.itemStickTopBackground =
        new ColorDrawable(ContextCompat.getColor(requireContext(), R.color.color_f1f2f4));
    uiConfig.titleBarRight2Res = R.drawable.icon_clean_all_messages;
    uiConfig.titleBarRight2Click =
        v ->
            DialogUtil.showECConfirmDialog(
                getActivity(),
                getString(R.string.one_on_one_ignore_unread_tip_titile),
                getString(R.string.one_on_one_ignore_unread_tip_content),
                getString(R.string.cancel),
                getString(R.string.sure),
                aBoolean -> {
                  if (Boolean.TRUE.equals(aBoolean)) {
                    ConversationRepo.clearAllUnreadCount();
                  }
                });
    uiConfig.itemClickListener =
        new ItemClickListener() {
          @Override
          public boolean onLongClick(Context context, ConversationBean data, int position) {
            //todo 弹出弹窗

            //是否置顶判断
            //        data.infoData.isStickTop()
            //删除则调用
            //        viewModel.deleteConversation(data);
            //置顶调用
            //        viewModel.addStickTop(data);
            //移除置顶
            //        viewModel.removeStick(data);
            showAlertDialog(data);
            return true;
          }
        };

    ConversationKitClient.setConversationUIConfig(uiConfig);
  }

  private void showAlertDialog(ConversationBean data) {
    ListAlertDialog alertDialog = new ListAlertDialog();
    alertDialog.setContent(generateDialogContent(data.infoData.isStickTop()));
    alertDialog.setTitleVisibility(View.GONE);
    alertDialog.setDialogWidth(
        getResources()
            .getDimension(com.netease.yunxin.kit.conversationkit.ui.R.dimen.alert_dialog_width));
    alertDialog.setItemClickListener(
        action -> {
          if (TextUtils.equals(action, ConversationConstant.Action.ACTION_DELETE)) {

            DialogUtil.showECConfirmDialog(
                getActivity(),
                getString(R.string.one_on_one_delete_conversation_tip_title),
                getString(R.string.one_on_one_delete_conversation_tip_content),
                getString(R.string.cancel),
                getString(R.string.sure),
                aBoolean -> {
                  if (Boolean.TRUE.equals(aBoolean)) {
                    viewModel.deleteConversation(data);
                  }
                });
          } else if (TextUtils.equals(action, ConversationConstant.Action.ACTION_STICK)) {
            if (data.infoData.isStickTop()) {
              viewModel.removeStick((ConversationBean) data);
            } else {
              viewModel.addStickTop((ConversationBean) data);
            }
          }
          alertDialog.dismiss();
        });
    alertDialog.show(getParentFragmentManager());
  }
}
