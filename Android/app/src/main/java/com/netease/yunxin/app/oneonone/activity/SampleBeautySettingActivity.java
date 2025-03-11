// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.app.oneonone.activity;

import android.os.Bundle;
import androidx.annotation.Nullable;
import com.faceunity.nama.BeautySettingActivity;
import com.netease.yunxin.kit.call.p2p.NECallEngine;
import com.netease.yunxin.kit.call.p2p.model.NECallEngineDelegate;
import com.netease.yunxin.kit.call.p2p.model.NECallEngineDelegateAbs;
import com.netease.yunxin.kit.call.p2p.model.NEInviteInfo;

public class SampleBeautySettingActivity extends BeautySettingActivity {
  private NECallEngineDelegate callEngineDelegate =
      new NECallEngineDelegateAbs() {

        @Override
        public void onReceiveInvited(NEInviteInfo info) {
          super.onReceiveInvited(info);
          // 收到来电关闭美颜
          finish();
        }
      };

  @Override
  protected void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    NECallEngine.sharedInstance().addCallDelegate(callEngineDelegate);
  }

  @Override
  protected void onDestroy() {
    NECallEngine.sharedInstance().removeCallDelegate(callEngineDelegate);
    super.onDestroy();
  }
}
