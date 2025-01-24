// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.app.oneonone.ui.utils;

import android.app.Activity;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.view.WindowManager;
import com.netease.nimlib.sdk.media.record.AudioRecorder;
import com.netease.nimlib.sdk.media.record.IAudioRecordCallback;
import com.netease.nimlib.sdk.media.record.RecordType;
import com.netease.yunxin.kit.alog.ALog;
import java.io.File;
import java.util.Timer;
import java.util.TimerTask;

public final class AudioInputManager implements IAudioRecordCallback {
  private static final String TAG = "AudioInputManager";
  private Context context;
  private static final int MAX_DURATION = 60;
  private AudioRecorder mAudioRecorder;
  private boolean started = false;
  private static final int MAX_SECOND_MILLIS = MAX_DURATION * 1000;
  // 每30ms回调一次，提高动画流畅度
  private static final int PERIOD = 30;
  private int currentMilliSecond = 0;
  private Timer timer;
  private final Handler mainHandler = new Handler(Looper.getMainLooper());

  public void initAudioRecord(Context context) {
    ALog.i(TAG, "initAudioRecord,context:" + context);
    this.context = context;
    if (mAudioRecorder == null) {
      mAudioRecorder = new AudioRecorder(context, RecordType.AAC, MAX_DURATION, this);
    }
  }

  public void startAudioRecord() {
    ALog.i(TAG, "startAudioRecord");
    if (context instanceof Activity) {
      ((Activity) context)
          .getWindow()
          .setFlags(
              WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
              WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }
    if (mAudioRecorder != null) {
      mAudioRecorder.startRecord();
    }
    timer = new Timer();
    TimerTask timerTask =
        new TimerTask() {
          @Override
          public void run() {
            if (!started) {
              return;
            }
            currentMilliSecond = currentMilliSecond + PERIOD;
            if (audioInputCallback != null) {
              audioInputCallback.onAudioRecordProgress(
                  (int) (currentMilliSecond * 1.0 / MAX_SECOND_MILLIS * 100));
            }
            if (currentMilliSecond >= MAX_SECOND_MILLIS) {
              endAudioRecord(false);
            }
          }
        };
    timer.schedule(timerTask, 0, PERIOD);
  }

  public void endAudioRecord(boolean cancel) {
    ALog.d(TAG, "endAudioRecord -->> cancel:" + cancel);
    reset();
    mainHandler.post(
        () -> {
          if (context instanceof Activity) {
            ((Activity) context)
                .getWindow()
                .setFlags(0, WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
          }
          if (mAudioRecorder != null) {
            mAudioRecorder.completeRecord(cancel);
          }
        });
  }

  public void willCancelAudioRecord(boolean cancel) {
    ALog.i(TAG, "willCancelAudioRecord -->> cancel:" + cancel);
    reset();
  }

  private void startRecord() {
    ALog.i(TAG, "startRecord");
    started = true;
  }

  private void endRecord() {
    ALog.i(TAG, "endRecord");
    reset();
  }

  public void recordReachMaxTime(int maxTime) {
    ALog.i(TAG, "recordReachMaxTime,maxTime:" + maxTime);
    if (mAudioRecorder != null) {
      mAudioRecorder.handleEndRecord(true, maxTime);
    }
  }

  @Override
  public void onRecordReady() {
    ALog.i(TAG, "onRecordReady");
  }

  @Override
  public void onRecordStart(File audioFile, RecordType recordType) {
    ALog.i(TAG, "onRecordStart,audioFile:" + audioFile + ",recordType:" + recordType);
    startRecord();
  }

  @Override
  public void onRecordSuccess(File audioFile, long audioLength, RecordType recordType) {
    ALog.i(
        TAG,
        "onRecordSuccess,audioFile:"
            + audioFile
            + ",audioLength:"
            + audioLength
            + ",recordType:"
            + recordType);
    if (audioInputCallback != null) {
      audioInputCallback.onRecordSuccess(audioFile, (int) audioLength, recordType);
    }
    endRecord();
  }

  @Override
  public void onRecordFail() {
    ALog.i(TAG, "onRecordFail");
    if (audioInputCallback != null) {
      audioInputCallback.onRecordFail();
    }
    endRecord();
  }

  @Override
  public void onRecordCancel() {
    ALog.i(TAG, "onRecordCancel");
    if (audioInputCallback != null) {
      audioInputCallback.onRecordCancel();
    }
    endRecord();
  }

  @Override
  public void onRecordReachedMaxTime(int maxTime) {
    ALog.i(TAG, "onRecordReachedMaxTime,maxTime:" + maxTime);
    recordReachMaxTime(maxTime);
  }

  private void reset() {
    ALog.i(TAG, "reset");
    if (timer != null) {
      timer.cancel();
      timer = null;
    }
    started = false;
    currentMilliSecond = 0;
  }

  public void destroy() {
    reset();
    mAudioRecorder = null;
    audioInputCallback = null;
  }

  public void pause() {
    ALog.i(TAG, "pause");
    endAudioRecord(true);
  }

  public void setAudioInputCallback(AudioInputCallback audioInputCallback) {
    this.audioInputCallback = audioInputCallback;
  }

  private AudioInputCallback audioInputCallback;

  public interface AudioInputCallback {
    /** 音频录制进度 */
    void onAudioRecordProgress(int percent);

    /** 音频录制完成 */
    void onRecordSuccess(File audioFile, int audioLength, RecordType recordType);

    /** 录制失败 */
    void onRecordFail();

    /** 录制取消 */
    void onRecordCancel();
  }
}
