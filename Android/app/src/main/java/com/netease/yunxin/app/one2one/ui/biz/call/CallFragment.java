// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.app.one2one.ui.biz.call;

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
import com.blankj.utilcode.util.ToastUtils;
import com.netease.nimlib.sdk.avsignalling.constant.ChannelType;
import com.netease.nimlib.sdk.avsignalling.model.ChannelFullInfo;
import com.netease.yunxin.android.lib.network.common.BaseResponse;
import com.netease.yunxin.android.lib.picture.ImageLoader;
import com.netease.yunxin.app.one2one.R;
import com.netease.yunxin.app.one2one.constant.AppConfig;
import com.netease.yunxin.app.one2one.constant.AppParams;
import com.netease.yunxin.app.one2one.constant.CallConfig;
import com.netease.yunxin.app.one2one.databinding.FragmentCallBinding;
import com.netease.yunxin.app.one2one.http.HttpService;
import com.netease.yunxin.app.one2one.model.OtherUserInfo;
import com.netease.yunxin.app.one2one.ui.biz.call.viewmodel.CallViewModel;
import com.netease.yunxin.app.one2one.ui.biz.call.viewmodel.PstnCallViewModel;
import com.netease.yunxin.app.one2one.utils.AccountAmountHelper;
import com.netease.yunxin.app.one2one.utils.AppGlobals;
import com.netease.yunxin.app.one2one.utils.CallTimeOutHelper;
import com.netease.yunxin.app.one2one.utils.DisplayUtils;
import com.netease.yunxin.app.one2one.utils.LogUtil;
import com.netease.yunxin.app.one2one.utils.NECallback;
import com.netease.yunxin.app.one2one.utils.UserInfoManager;
import com.netease.yunxin.kit.common.utils.NetworkUtils;
import com.netease.yunxin.nertc.pstn.base.PstnCallParam;
import com.netease.yunxin.nertc.pstn.base.PstnFunctionMgr;
import com.netease.yunxin.nertc.ui.base.AVChatSoundPlayer;
import com.netease.yunxin.nertc.ui.base.CallParam;
import io.reactivex.observers.ResourceSingleObserver;
import org.json.JSONException;
import org.json.JSONObject;

public class CallFragment extends Fragment {
  private static final String TAG = "CallFragment";
  private FragmentCallBinding binding;
  private CallActivity activity;
  private CallViewModel viewModel;
  private CallParam callParams;
  private PstnCallViewModel pstnCallViewModel;
  private boolean needPstnCall = false;
  private boolean callFinished = true;
  private String calledMobile;
  private String callerUserName;

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
    handleCall();
    subscribeUi();
    initEvent();
    return binding.getRoot();
  }

  private void handleCall() {
    callParams = activity.getCallParams();
    if (callParams.getCallExtraInfo() == null) {
      return;
    }
    JSONObject callParamExtraInfo;
    try {
      callParamExtraInfo = new JSONObject(callParams.getCallExtraInfo());
      if (!callParams.isCalled()) {
        callFinished = false;
        needPstnCall = callParamExtraInfo.getBoolean(AppParams.NEED_PSTN_CALL);
        calledMobile = callParamExtraInfo.getString(AppParams.CALLED_USER_MOBILE);
        callerUserName = callParamExtraInfo.getString(AppParams.CALLER_USER_NAME);
        if (callParams.getChannelType() == ChannelType.AUDIO.getValue() && needPstnCall) {
          pstnCallViewModel = new ViewModelProvider(requireActivity()).get(PstnCallViewModel.class);
          CallTimeOutHelper.configTimeOut(
              CallConfig.CALL_TOTAL_WAIT_TIMEOUT, CallConfig.CALL_PSTN_WAIT_MILLISECONDS);
          PstnCallParam pstnCallParam = new PstnCallParam(callParams, calledMobile, null);
          PstnFunctionMgr.callWithCor(pstnCallParam);
          LogUtil.i(TAG, "handleCall->pstnCall");
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
          LogUtil.i(TAG, "handleCall->rtcCall");
        }
      } else {
        playRing(AVChatSoundPlayer.RingerTypeEnum.RING);
      }
    } catch (JSONException e) {
      e.printStackTrace();
    }
  }

  private void subscribeUi() {
    viewModel.refresh(callParams);
    handleOtherInfoUi();
    handleToastEvent();
    handleRingEvent();
    handleCallFinishedEvent();
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
                if (AppConfig.isChineseEnv()) {
                  sendSms();
                }
              }
            });
  }

  private void sendSms() {
    if (AccountAmountHelper.allowSendSms(UserInfoManager.getSelfImAccid())) {
      HttpService.sendSms(calledMobile, callerUserName)
          .subscribe(
              new ResourceSingleObserver<BaseResponse>() {
                @Override
                public void onSuccess(BaseResponse baseResponse) {
                  LogUtil.i(
                      TAG,
                      "sendSms,calledMobile:"
                          + calledMobile
                          + ",callerUserName:"
                          + callerUserName
                          + ",data:"
                          + baseResponse.data);
                  if (baseResponse.isSuccessful()) {
                    AccountAmountHelper.addSmsUsedWithAccount(UserInfoManager.getSelfImAccid(), 1);
                  }
                }

                @Override
                public void onError(Throwable e) {}
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
                if (AppConfig.isChineseEnv()) {
                  sendSms();
                }
              }
            });
  }

  private void handleCallFinishedEvent() {
    viewModel
        .getCallFinished()
        .observe(
            getViewLifecycleOwner(),
            new Observer<Boolean>() {
              @Override
              public void onChanged(Boolean aBoolean) {
                finishActivity();
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

  private void handleToastEvent() {
    viewModel
        .getToastData()
        .observe(
            getViewLifecycleOwner(),
            new Observer<String>() {
              @Override
              public void onChanged(String s) {
                ToastUtils.showLong(s);
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
            if (NetworkUtils.isConnected()) {
              handleInvitedAcceptEvent();
            }
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
    if (needPstnCall && callParams.getChannelType() == ChannelType.AUDIO.getValue()) {
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
