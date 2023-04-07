// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.app.oneonone.ui.viewmodel;

import android.os.Build;
import android.util.Pair;
import androidx.annotation.RequiresApi;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.netease.yunxin.app.oneonone.ui.http.HttpService;
import com.netease.yunxin.app.oneonone.ui.model.HomeItemModel;
import com.netease.yunxin.app.oneonone.ui.model.ModelResponse;
import java.util.ArrayList;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HomeViewModel extends ViewModel {
  private final MutableLiveData<Pair<List<HomeItemModel>, Boolean>> userListData =
      new MutableLiveData<>();
  private List<HomeItemModel> userList = new ArrayList<>();
  private boolean haveMore = false;

  public void getUserList(int pageNum, int pageSize) {
    HttpService.getInstance()
        .getUserList(
            pageNum,
            pageSize,
            new Callback<ModelResponse<List<HomeItemModel>>>() {
              @RequiresApi(api = Build.VERSION_CODES.ECLAIR)
              @Override
              public void onResponse(
                  Call<ModelResponse<List<HomeItemModel>>> call,
                  Response<ModelResponse<List<HomeItemModel>>> response) {
                if (pageNum == 0) {
                  userList.clear();
                }
                if (response.body() != null
                    && response.body().code == 200
                    && response.body().data != null) {
                  userList.addAll(response.body().data);
                  haveMore = response.body().data.size() == pageSize;
                }
                userListData.setValue(new Pair<>(userList, haveMore));
              }

              @Override
              public void onFailure(Call<ModelResponse<List<HomeItemModel>>> call, Throwable t) {
                userListData.setValue(new Pair<>(userList, haveMore));
              }
            });
  }

  public MutableLiveData<Pair<List<HomeItemModel>, Boolean>> getUserListData() {
    return userListData;
  }
}
