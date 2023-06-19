// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.app.oneonone.ui.viewmodel;

import android.app.Application;
import android.os.CountDownTimer;
import android.util.Pair;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;
import com.netease.lava.nertc.sdk.NERtcEx;
import com.netease.neliveplayer.sdk.NELivePlayer;
import com.netease.neliveplayer.sdk.model.NESDKConfig;
import com.netease.nimlib.sdk.avsignalling.constant.ChannelType;
import com.netease.yunxin.app.oneonone.ui.constant.AppParams;
import com.netease.yunxin.app.oneonone.ui.utils.LogUtil;
import com.netease.yunxin.app.oneonone.ui.utils.NERTCCallStateManager;
import com.netease.yunxin.kit.alog.ALog;
import com.netease.yunxin.nertc.ui.base.CallParam;
import com.netease.yunxin.nertc.ui.utils.SecondsTimer;
import java.io.IOException;
import kotlin.Unit;
import kotlin.jvm.functions.Function1;
import org.json.JSONException;
import org.json.JSONObject;

public class VirtualCallViewModel extends AndroidViewModel {
  private static final String TAG = "VirtualCallViewModel";
  private int channelType = ChannelType.AUDIO.getValue();
  private final MutableLiveData<Boolean> switchToInTheCall = new MutableLiveData<>();
  private final MutableLiveData<Boolean> releaseAndFinish = new MutableLiveData<>();
  private final MutableLiveData<Boolean> playError = new MutableLiveData<>();
  private final MutableLiveData<Pair<Integer, Integer>> videoSizeChanged = new MutableLiveData<>();
  private NELivePlayer player;
  private final MutableLiveData<Long> inTheCallDuration = new MutableLiveData<>();
  private SecondsTimer inTheCallSecondTimer;
  private final NELivePlayer.OnCompletionListener mCompletionListener =
      neLivePlayer -> {
        ALog.i(TAG, "onCompletion");
        cancelInTheCallTimer();
        releaseAndFinish.postValue(true);
      };
  private final NELivePlayer.OnVideoSizeChangedListener mSizeChangedListener =
      (neLivePlayer, width, height, sar_num, sar_den) -> {
        if (isVideoType()) {
          videoSizeChanged.postValue(new Pair<>(width, height));
        }
      };

  private final NELivePlayer.OnErrorListener mErrorListener =
      (neLivePlayer, what, extra) -> {
        ALog.e(TAG, "what:" + what + ",extra:" + extra);
        playError.postValue(true);
        return false;
      };

  private final CountDownTimer countDownTimer =
      new CountDownTimer(2000, 1000) {
        @Override
        public void onTick(long millisUntilFinished) {
          ALog.i(TAG, "onTick:" + millisUntilFinished);
        }

        @Override
        public void onFinish() {
          switchToInTheCall.postValue(true);
          startInTheCallTimer();
        }
      };

  private void initPlayer(String audioUrl, String videoUrl) {
    NESDKConfig config = new NESDKConfig();
    NELivePlayer.init(getApplication(), config);
    player = NELivePlayer.create();
    player.setOnVideoSizeChangedListener(mSizeChangedListener);
    player.setOnCompletionListener(mCompletionListener);
    player.setOnErrorListener(mErrorListener);
    try {
      if (isVideoType()) {
        player.setDataSource(videoUrl);
      } else {
        player.setDataSource(audioUrl);
      }
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  public VirtualCallViewModel(@NonNull Application application) {
    super(application);
  }

  public void setCallParam(CallParam callParam) {
    channelType = callParam.getChannelType();
    try {
      JSONObject jsonObject = new JSONObject(callParam.getCallExtraInfo());
      String audioUrl = jsonObject.optString(AppParams.CALLED_AUDIO_URL);
      String videoUrl = jsonObject.optString(AppParams.CALLED_VIDEO_URL);
      initPlayer(audioUrl, videoUrl);
    } catch (JSONException e) {
      e.printStackTrace();
    }
  }

  public void release() {
    if (player != null) {
      player.setSurface(null);
      player.release();
    }
    NERtcEx.getInstance().release();
  }

  @Override
  protected void onCleared() {
    super.onCleared();
    NERTCCallStateManager.setIdleState();
    release();
    cancelInTheCallTimer();
  }

  public MutableLiveData<Boolean> getSwitchToInTheCall() {
    return switchToInTheCall;
  }

  public MutableLiveData<Boolean> getReleaseAndFinish() {
    return releaseAndFinish;
  }

  public MutableLiveData<Pair<Integer, Integer>> getVideoSizeChanged() {
    return videoSizeChanged;
  }

  public MutableLiveData<Long> getInTheCallDuration() {
    return inTheCallDuration;
  }

  public MutableLiveData<Boolean> getPlayError() {
    return playError;
  }

  public NELivePlayer getPlayer() {
    return player;
  }

  public boolean isVideoType() {
    return channelType == ChannelType.VIDEO.getValue();
  }

  public void startInTheCallTimer() {
    if (inTheCallSecondTimer == null) {
      inTheCallSecondTimer = new SecondsTimer(0, 1000);
    }
    inTheCallSecondTimer.start(
        new Function1<Long, Unit>() {
          @Override
          public Unit invoke(Long aLong) {
            LogUtil.i(TAG, "startInTheCallTimer aLong:" + aLong);
            inTheCallDuration.postValue(aLong);
            return null;
          }
        });
  }

  public void cancelInTheCallTimer() {
    if (inTheCallSecondTimer != null) {
      inTheCallSecondTimer.cancel();
      inTheCallSecondTimer = null;
    }
  }

  public void muteAudio(boolean mute) {
    if (mute) {
      player.setVolume(0);
    } else {
      player.setVolume(100);
    }
  }

  public void startCountDown() {
    countDownTimer.start();
  }
}
