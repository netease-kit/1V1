/*
 *
 *  * Copyright (c) 2022 NetEase, Inc.  All rights reserved.
 *  * Use of this source code is governed by a MIT license that can be found in the LICENSE file.
 *
 */

package com.netease.yunxin.app.one2one.ui.biz.userinfo;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;

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
        HomeItemModel homeItemModel = MockDatas.MOCK_USERS.get(index);

        UserInfoDetailModel userInfoDetailModel = new UserInfoDetailModel();
        userInfoDetailModel.bgUrl = homeItemModel.imageUrl;
        userInfoDetailModel.nickname = homeItemModel.nickName;
        userInfoDetailModel.desc = "秋天的落叶是如此美丽";
        userInfoDetailModel.age = homeItemModel.age+"";
        userInfoDetailModel.height = "170cm";
        userInfoDetailModel.weight = "56kg";
        ArrayList<String> hobbys = new ArrayList<>();
        hobbys.add("唱歌");
        hobbys.add("跳舞");
        hobbys.add("旅行");
        userInfoDetailModel.hobbys = hobbys;
        userInfoDetailModel.albums=new ArrayList<>(MockDatas.ALBUMS);
        detail.postValue(userInfoDetailModel);
    }
}
