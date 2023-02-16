// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.app.one2one.constant;

import android.app.Application;
import com.blankj.utilcode.util.Utils;
import com.netease.yunxin.app.one2one.R;
import com.netease.yunxin.app.one2one.model.HomeItemModel;
import java.util.ArrayList;
import java.util.List;

public class MockDatas {
  private static final int TOTAL_COUNT = 10;

  public static List<HomeItemModel> getMockUsers() {
    Application app = Utils.getApp();
    ArrayList<HomeItemModel> list = new ArrayList<>();
    String[] nicknames = app.getResources().getStringArray(R.array.mock_nicknames);
    String[] avatars = app.getResources().getStringArray(R.array.mock_avatars);
    int[] ages = app.getResources().getIntArray(R.array.mock_ages);
    if (nicknames.length == TOTAL_COUNT
        && avatars.length == TOTAL_COUNT
        && ages.length == TOTAL_COUNT) {
      for (int i = 0; i < TOTAL_COUNT; i++) {
        HomeItemModel homeItemModel = new HomeItemModel();
        list.add(homeItemModel);
      }
      for (int i = 0; i < nicknames.length; i++) {
        list.get(i).nickName = nicknames[i];
      }
      for (int i = 0; i < avatars.length; i++) {
        list.get(i).imageUrl = avatars[i];
      }
      for (int i = 0; i < ages.length; i++) {
        list.get(i).age = ages[i];
      }
    }
    return list;
  }

  public static List<String> getAlbums() {
    List<String> list = new ArrayList<>();
    String[] albums = Utils.getApp().getResources().getStringArray(R.array.mock_albums);
    for (String album : albums) {
      list.add(album);
    }
    return list;
  }
}
