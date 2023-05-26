// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.app.oneonone.ui.fragment;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import com.blankj.utilcode.util.PermissionUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.netease.nimlib.sdk.avsignalling.constant.ChannelType;
import com.netease.nimlib.sdk.avsignalling.model.ChannelFullInfo;
import com.netease.yunxin.app.oneonone.ui.OneOnOneUI;
import com.netease.yunxin.app.oneonone.ui.R;
import com.netease.yunxin.app.oneonone.ui.activity.CallActivity;
import com.netease.yunxin.app.oneonone.ui.constant.AppParams;
import com.netease.yunxin.app.oneonone.ui.constant.CallConfig;
import com.netease.yunxin.app.oneonone.ui.databinding.FragmentCallBinding;
import com.netease.yunxin.app.oneonone.ui.http.HttpService;
import com.netease.yunxin.app.oneonone.ui.model.ModelResponse;
import com.netease.yunxin.app.oneonone.ui.model.OtherUserInfo;
import com.netease.yunxin.app.oneonone.ui.utils.AccountAmountHelper;
import com.netease.yunxin.app.oneonone.ui.utils.AppGlobals;
import com.netease.yunxin.app.oneonone.ui.utils.CallTimeOutHelper;
import com.netease.yunxin.app.oneonone.ui.utils.DisplayUtils;
import com.netease.yunxin.app.oneonone.ui.utils.LogUtil;
import com.netease.yunxin.app.oneonone.ui.utils.NECallback;
import com.netease.yunxin.app.oneonone.ui.utils.NERTCCallStateManager;
import com.netease.yunxin.app.oneonone.ui.viewmodel.CallViewModel;
import com.netease.yunxin.app.oneonone.ui.viewmodel.PstnCallViewModel;
import com.netease.yunxin.kit.alog.ALog;
import com.netease.yunxin.kit.common.image.ImageLoader;
import com.netease.yunxin.kit.common.network.Response;
import com.netease.yunxin.kit.common.utils.NetworkUtils;
import com.netease.yunxin.kit.entertainment.common.utils.UserInfoManager;
import com.netease.yunxin.nertc.pstn.base.PstnCallParam;
import com.netease.yunxin.nertc.pstn.base.PstnFunctionMgr;
import com.netease.yunxin.nertc.ui.base.AVChatSoundPlayer;
import com.netease.yunxin.nertc.ui.base.CallParam;
import org.json.JSONException;
import org.json.JSONObject;
import retrofit2.Call;
import retrofit2.Callback;

public class CallFragment extends Fragment {
  private static final String TAG = "CallFragment";
  private FragmentCallBinding binding;
  private CallActivity activity;
  private CallViewModel viewModel;
  private CallParam callParams;
  private PstnCallViewModel pstnCallViewModel;
  private boolean callFinished = true;
  private String calledMobile;
  private String callerUserName;
  /** 音频呼叫场景下，RTC呼叫转PSTN呼叫的等待时长 */
  private long callPstnWaitMilliseconds;

  @Override
  public void onAttach(@NonNull Context context) {
    super.onAttach(context);
    activity = (CallActivity) context;
    OnBackPressedCallback callback =
        new OnBackPressedCallback(true) {
          @Override
          public void handleOnBackPressed() {
            new AlertDialog.Builder(activity)
                .setTitle(activity.getString(R.string.end_call))
                .setMessage(activity.getString(R.string.sure_end_call))
                .setPositiveButton(
                    activity.getString(R.string.yes),
                    new DialogInterface.OnClickListener() {
                      @Override
                      public void onClick(DialogInterface dialogInterface, int i) {
                        if (!callFinished) {
                          ToastUtils.showShort(R.string.call_out_failed);
                          finishActivity();
                          return;
                        }
                        handleHangupEvent();
                      }
                    })
                .setNegativeButton(
                    activity.getString(R.string.no),
                    new DialogInterface.OnClickListener() {
                      @Override
                      public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                      }
                    })
                .show();
          }
        };
    activity
        .getOnBackPressedDispatcher()
        .addCallback(
            this, // LifecycleOwner
            callback);
  }

  @Nullable
  @Override
  public View onCreateView(
      @NonNull LayoutInflater inflater,
      @Nullable ViewGroup container,
      @Nullable Bundle savedInstanceState) {
    binding = FragmentCallBinding.inflate(inflater, container, false);
    viewModel = new ViewModelProvider(requireActivity()).get(CallViewModel.class);
    callParams = activity.getCallParams();
    Bundle bundle = getArguments();
    if (bundle != null && bundle.getBoolean(AppParams.AUTO_CALL, false) && !callParams.isCalled()) {
      handleCall();
    }
    if (callParams.isCalled()) {
      playRing(AVChatSoundPlayer.RingerTypeEnum.RING);
    }
    subscribeUi();
    initEvent();
    return binding.getRoot();
  }

  public void handleCall() {

    if (callParams.getCallExtraInfo() == null) {
      return;
    }
    JSONObject callParamExtraInfo;
    try {
      callParamExtraInfo = new JSONObject(callParams.getCallExtraInfo());
      if (!callParams.isCalled()) {
        callFinished = false;
        callPstnWaitMilliseconds =
            callParamExtraInfo.getLong(AppParams.CALL_PSTN_WAIT_MILLISECONDS);
        calledMobile = callParamExtraInfo.getString(AppParams.CALLED_USER_MOBILE);
        callerUserName = callParamExtraInfo.getString(AppParams.CALLER_USER_NAME);
        if (callParams.getChannelType() == ChannelType.AUDIO.getValue()
            && activity.needPstnCall()) {
          pstnCallViewModel = new ViewModelProvider(requireActivity()).get(PstnCallViewModel.class);
          CallTimeOutHelper.configTimeOut(
              CallConfig.CALL_TOTAL_WAIT_TIMEOUT, callPstnWaitMilliseconds);
          PstnCallParam pstnCallParam = new PstnCallParam(callParams, calledMobile, null);
          PstnFunctionMgr.callWithCor(pstnCallParam);
          LogUtil.i(TAG, "handleCall->pstnCall");
        } else {
          if (activity.isVirtualCall()) {
            NERTCCallStateManager.setCallOutState();
          } else {
            CallTimeOutHelper.configTimeOut(
                CallConfig.CALL_TOTAL_WAIT_TIMEOUT, CallConfig.CALL_TOTAL_WAIT_TIMEOUT);
            activity.rtcCall(
                new NECallback<ChannelFullInfo>() {
                  @Override
                  public void onSuccess(ChannelFullInfo channelFullInfo) {
                    callFinished = true;
                  }

                  @Override
                  public void onError(int code, String errorMsg) {
                    ToastUtils.showShort(R.string.call_failed);
                  }
                });
          }
          LogUtil.i(TAG, "handleCall->rtcCall");
        }
      }
    } catch (JSONException e) {
      e.printStackTrace();
    }
  }

  private void subscribeUi() {
    viewModel.refresh(callParams);
    handleOtherInfoUi();
    handleRingEvent();
    handlePstnEvent();
    handleSmsEvent();
  }

  private void handleSmsEvent() {
    viewModel
        .getSendSmsData()
        .observe(
            getViewLifecycleOwner(),
            new Observer<Boolean>() {
              @Override
              public void onChanged(Boolean aBoolean) {
                if (OneOnOneUI.getInstance().isChineseEnv()) {
                  // 移除短信提醒功能
                  //                  sendSms();
                }
              }
            });
  }

  private void sendSms() {
    if (AccountAmountHelper.allowSendSms(UserInfoManager.getSelfImAccid())) {
      HttpService.getInstance()
          .sendSms(
              calledMobile,
              callerUserName,
              new Callback<ModelResponse<Response>>() {

                @Override
                public void onResponse(
                    @NonNull Call<ModelResponse<Response>> call,
                    @NonNull retrofit2.Response<ModelResponse<Response>> response) {
                  LogUtil.i(
                      TAG,
                      "sendSms,calledMobile:"
                          + calledMobile
                          + ",callerUserName:"
                          + callerUserName
                          + ",data:"
                          + response.body());
                  if (response.isSuccessful() && response.body().code == 200) {
                    AccountAmountHelper.addSmsUsedWithAccount(UserInfoManager.getSelfImAccid(), 1);
                  }
                }

                @Override
                public void onFailure(
                    @NonNull Call<ModelResponse<Response>> call, @NonNull Throwable t) {
                  LogUtil.e(TAG, "sendSms failed,e:" + t.getMessage());
                }
              });
    }
  }

  private void handlePstnEvent() {
    if (pstnCallViewModel == null) {
      return;
    }
    LifecycleOwner viewLifecycleOwner = getViewLifecycleOwner();
    pstnCallViewModel
        .getSwitchToInTheCall()
        .observe(
            viewLifecycleOwner,
            new Observer<Integer>() {
              @Override
              public void onChanged(Integer integer) {
                activity.switchToInTheCallFragment();
              }
            });

    pstnCallViewModel
        .getPstnToastData()
        .observe(
            viewLifecycleOwner,
            new Observer<String>() {
              @Override
              public void onChanged(String s) {
                ToastUtils.showShort(s);
              }
            });
    pstnCallViewModel
        .getReleaseAndFinish()
        .observe(
            viewLifecycleOwner,
            new Observer<Boolean>() {
              @Override
              public void onChanged(Boolean aBoolean) {
                releaseAndFinish(aBoolean);
              }
            });
    pstnCallViewModel
        .getRtcCallResult()
        .observe(
            viewLifecycleOwner,
            new Observer<Boolean>() {
              @Override
              public void onChanged(Boolean aBoolean) {
                callFinished = aBoolean;
              }
            });
    pstnCallViewModel
        .getSendSmsData()
        .observe(
            viewLifecycleOwner,
            new Observer<Boolean>() {
              @Override
              public void onChanged(Boolean aBoolean) {
                if (OneOnOneUI.getInstance().isChineseEnv()) {
                  // 移除短信提醒功能
                  //                  sendSms();
                }
              }
            });
  }

  private void handleRingEvent() {
    viewModel
        .getPlayRing()
        .observe(
            getViewLifecycleOwner(),
            new Observer<AVChatSoundPlayer.RingerTypeEnum>() {
              @Override
              public void onChanged(AVChatSoundPlayer.RingerTypeEnum ringerTypeEnum) {
                playRing(ringerTypeEnum);
              }
            });
  }

  private void handleOtherInfoUi() {
    viewModel
        .getOtherInfo()
        .observe(
            getViewLifecycleOwner(),
            new Observer<OtherUserInfo>() {
              @Override
              public void onChanged(OtherUserInfo otherUserInfo) {
                if (otherUserInfo == null) {
                  return;
                }
                if (otherUserInfo.callType == ChannelType.VIDEO.getValue()) {
                  binding.clRoot.setBackgroundResource(R.drawable.bg_video_call_page);
                  binding.ivInvitedAccept.setBackgroundResource(
                      R.drawable.selector_img_video_accept);
                } else {
                  binding.clRoot.setBackgroundResource(R.drawable.bg_audio_call_page);
                  binding.ivInvitedAccept.setBackgroundResource(
                      R.drawable.selector_img_audio_accept);
                }
                binding.tvNick.setText(otherUserInfo.nickname);
                ImageLoader.with(AppGlobals.getApplication())
                    .circleLoad(otherUserInfo.avatar, binding.ivAvatar);
                binding.tvTitle.setText(otherUserInfo.title);
                binding.tvSubtitle.setText(otherUserInfo.subtitle);
                if (otherUserInfo.isCalled) {
                  binding.llInvite.setVisibility(View.GONE);
                  binding.rlInvited.setVisibility(View.VISIBLE);
                  binding.tvSubtitle.setTextColor(Color.parseColor("#8CFFFFFF"));
                  binding.tvSubtitle.setPadding(0, 0, 0, 0);
                } else {
                  binding.llInvite.setVisibility(View.VISIBLE);
                  binding.rlInvited.setVisibility(View.GONE);
                  binding.tvSubtitle.setTextColor(Color.WHITE);
                  binding.tvSubtitle.setPadding(0, (int) DisplayUtils.dp2px(24), 0, 0);
                }
              }
            });
  }

  private void initEvent() {

    binding.ivInviteCancel.setOnClickListener(
        new View.OnClickListener() {
          @Override
          public void onClick(View view) {
            handleHangupEvent();
          }
        });

    binding.ivInvitedAccept.setOnClickListener(
        new View.OnClickListener() {
          @Override
          public void onClick(View view) {
            handleInvitedAcceptEvent();
          }
        });

    binding.ivInvitedReject.setOnClickListener(
        new View.OnClickListener() {
          @Override
          public void onClick(View view) {
            handleHangupEvent();
          }
        });
  }

  private void handleHangupEvent() {
    if (!NetworkUtils.isConnected()) {
      ToastUtils.showShort(getString(R.string.one_on_one_network_error));
      return;
    }
    if (activity.needPstnCall()) {
      PstnFunctionMgr.hangup();
      finishActivity();
    } else {
      activity.rtcHangup(
          new NECallback<Integer>() {
            @Override
            public void onSuccess(Integer integer) {
              finishActivity();
            }

            @Override
            public void onError(int code, String errorMsg) {
              finishActivity();
            }
          });
    }
    if (callParams.isCalled()) {
      binding.ivInvitedAccept.setEnabled(false);
      binding.ivInvitedReject.setEnabled(false);
    } else {
      binding.ivInviteCancel.setEnabled(false);
    }
  }

  private void handleInvitedAcceptEvent() {
    if (!NetworkUtils.isConnected()) {
      ToastUtils.showShort(getString(R.string.one_on_one_network_error));
      return;
    }
    boolean microPhoneGranted = PermissionUtils.isGranted(Manifest.permission.RECORD_AUDIO);
    boolean cameraGranted = PermissionUtils.isGranted(Manifest.permission.CAMERA);
    if (callParams.getChannelType() == ChannelType.AUDIO.getValue() && !microPhoneGranted) {
      ToastUtils.showShort(getString(R.string.permission_microphone_missing_tips));
      ALog.e(TAG, "Unable to access the microphone. Enable microphone access and try again");
      return;
    } else if (callParams.getChannelType() == ChannelType.VIDEO.getValue()) {
      if (!microPhoneGranted && !cameraGranted) {
        ToastUtils.showShort(getString(R.string.permission_microphone_and_camera_missing_tips));
        ALog.e(
            TAG,
            "Unable to access the microphone and camera. Enable microphone and camera access and try again");
        return;
      } else if (!microPhoneGranted) {
        ToastUtils.showShort(getString(R.string.permission_microphone_missing_tips));
        ALog.e(TAG, "Unable to access the microphone. Enable microphone access and try again");
        return;
      } else if (!cameraGranted) {
        ToastUtils.showShort(getString(R.string.permission_camera_missing_tips));
        ALog.e(TAG, "Unable to access the camera. Enable camera access and try again");
        return;
      }
    }
    activity.rtcAccept();
    binding.ivInvitedAccept.setEnabled(false);
    binding.ivInvitedReject.setEnabled(false);
  }

  private void releaseAndFinish(boolean finishCall) {
    LogUtil.i(
        TAG, "releaseAndFinish,finishCall:" + finishCall + ",isCalled:" + callParams.isCalled());
    AVChatSoundPlayer.Companion.instance().stop(AVChatSoundPlayer.RingerTypeEnum.RING, activity);
    AVChatSoundPlayer.Companion.instance()
        .stop(AVChatSoundPlayer.RingerTypeEnum.CONNECTING, activity);
    if (finishCall) {
      if (callParams.isCalled()) {
        activity.rtcHangup(
            new NECallback<Integer>() {
              @Override
              public void onSuccess(Integer integer) {
                finishActivity();
              }

              @Override
              public void onError(int code, String errorMsg) {
                finishActivity();
              }
            });
      } else {
        pstnHangup();
      }
    }
  }

  private void playRing(AVChatSoundPlayer.RingerTypeEnum ringerTypeEnum) {
    AVChatSoundPlayer.Companion.instance().play(activity, ringerTypeEnum);
  }

  @Override
  public void onDestroyView() {
    super.onDestroyView();
    binding = null;
  }

  private void pstnHangup() {
    pstnCallViewModel.hangup();
    PstnFunctionMgr.hangup();
    finishActivity();
  }

  private void finishActivity() {
    if (!activity.isFinishing()) {
      activity.finish();
    }
  }
}
