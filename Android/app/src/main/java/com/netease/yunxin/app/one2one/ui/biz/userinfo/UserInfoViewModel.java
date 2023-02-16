// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.app.one2one.ui.biz.userinfo;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;
import com.netease.yunxin.app.one2one.R;
import com.netease.yunxin.app.one2one.constant.MockDatas;
import com.netease.yunxin.app.one2one.model.HomeItemModel;
import com.netease.yunxin.app.one2one.model.UserInfoDetailModel;
import java.util.ArrayList;

public class UserInfoViewModel extends AndroidViewModel {
  private MutableLiveData<UserInfoDetailModel> detail = new MutableLiveData<>();

  public MutableLiveData<UserInfoDetailModel> getDetail() {
    return detail;
  }

  public UserInfoViewModel(@NonNull Application application) {
    super(application);
  }

  public void fetchData(int index) {
    HomeItemModel homeItemModel = MockDatas.getMockUsers().get(index);

    UserInfoDetailModel userInfoDetailModel = new UserInfoDetailModel();
    userInfoDetailModel.bgUrl = homeItemModel.imageUrl;
    userInfoDetailModel.nickname = homeItemModel.nickName;
    userInfoDetailModel.desc = getApplication().getString(R.string.user_sign);
    userInfoDetailModel.age = homeItemModel.age + "";
    userInfoDetailModel.height = "170cm";
    userInfoDetailModel.weight = "56kg";
    ArrayList<String> hobbys = new ArrayList<>();
    hobbys.add(getApplication().getString(R.string.user_sing));
    hobbys.add(getApplication().getString(R.string.user_dance));
    hobbys.add(getApplication().getString(R.string.user_travel));
    userInfoDetailModel.hobbys = hobbys;
    userInfoDetailModel.albums = new ArrayList<>(MockDatas.getAlbums());
    detail.postValue(userInfoDetailModel);
  }
}
