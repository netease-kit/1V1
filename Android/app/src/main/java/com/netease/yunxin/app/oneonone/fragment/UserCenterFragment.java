// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.app.oneonone.fragment;

import static android.app.Activity.RESULT_OK;

import android.Manifest;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.faceunity.nama.BeautyLog;
import com.netease.lava.nertc.sdk.LastmileProbeConfig;
import com.netease.lava.nertc.sdk.LastmileProbeResult;
import com.netease.lava.nertc.sdk.NERtcConstants;
import com.netease.lava.nertc.sdk.NERtcEx;
import com.netease.yunxin.app.oneonone.R;
import com.netease.yunxin.app.oneonone.config.AppConfig;
import com.netease.yunxin.app.oneonone.databinding.FragmentUserCenterBinding;
import com.netease.yunxin.app.oneonone.utils.AppUtils;
import com.netease.yunxin.app.oneonone.utils.NavUtils;
import com.netease.yunxin.kit.alog.ALog;
import com.netease.yunxin.kit.common.ui.dialog.CommonConfirmDialog;
import com.netease.yunxin.kit.common.ui.dialog.LoadingDialog;
import com.netease.yunxin.kit.common.ui.utils.Permission;
import com.netease.yunxin.kit.common.ui.utils.ToastX;
import com.netease.yunxin.kit.entertainment.common.Constants;
import com.netease.yunxin.kit.entertainment.common.dialog.NetworkInfoDialog;
import com.netease.yunxin.kit.entertainment.common.dialog.PhoneConsultBottomDialog;
import com.netease.yunxin.kit.entertainment.common.fragment.BaseFragment;
import com.netease.yunxin.kit.entertainment.common.utils.DialogUtil;
import com.netease.yunxin.nertc.nertcvideocall.model.impl.NERtcCallbackExTemp;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

public class UserCenterFragment extends BaseFragment {
  private static final String TAG = "UserCenterFragment";
  private FragmentUserCenterBinding binding;
  private Dialog loadingDialog;
  private int count = 0;
  private int quality = -1;
  private static final int CALLBACK_TOTAL_COUNT = 2;
  private LastmileProbeResult probeResult;

  private final NERtcCallbackExTemp neRtcCallbackExTemp =
      new NERtcCallbackExTemp() {
        @Override
        public void onLastmileQuality(int quality) {
          super.onLastmileQuality(quality);
          ALog.d(TAG, "onLastmileQuality,quality:" + quality);
          count++;
          mergeInfo(quality, probeResult);
        }

        @Override
        public void onLastmileProbeResult(LastmileProbeResult result) {
          super.onLastmileProbeResult(result);
          ALog.d(TAG, "onLastmileProbeResult,result:" + result);
          count++;
          mergeInfo(quality, result);
        }
      };

  @Override
  public void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
  }

  @Nullable
  @Override
  public View onCreateView(
      @NonNull LayoutInflater inflater,
      @Nullable ViewGroup container,
      @Nullable Bundle savedInstanceState) {

    binding = FragmentUserCenterBinding.inflate(inflater, container, false);
    View rootView = binding.getRoot();
    initViews();
    initDataCenter();
    return rootView;
  }

  private void initViews() {
    initUser();
    binding.logUpload.setOnClickListener(
        v -> {
          initRTC();
          NERtcEx.getInstance().uploadSdkInfo();
          ToastX.showLongToast(R.string.please_wait_five_second_upload);
          NERtcEx.getInstance().release();
        });
    binding.beautySetting.setOnClickListener(
            v -> {
              ArrayList<String> list = new ArrayList<>();
              list.add(Manifest.permission.CAMERA);
              list.add(Manifest.permission.RECORD_AUDIO);
              Permission.requirePermissions(getActivity(), list.toArray(new String[0]))
                      .request(
                              new Permission.PermissionCallback() {

                                @Override
                                public void onGranted(@NonNull List<String> granted) {
                                  if (new HashSet<>(granted).containsAll(list)) {
                                    NavUtils.toBeautySettingPage(requireActivity());
                                  }
                                }

                                @Override
                                public void onDenial(
                                        List<String> permissionsDenial, List<String> permissionDenialForever) {
                                  for (String s : permissionsDenial) {
                                    BeautyLog.e(TAG, "permissionsDenial:" + s);
                                  }
                                  for (String s : permissionDenialForever) {
                                    BeautyLog.e(TAG, "permissionDenialForever:" + s);
                                  }
                                  if (permissionsDenial.size() > 0) {
                                    ToastX.showShortToast(R.string.permission_request_failed_tips);
                                  }
                                  if (permissionDenialForever.size() > 0) {
                                    DialogUtil.showConfirmDialog(
                                            requireActivity(),
                                            getString(R.string.app_tip),
                                            getString(R.string.app_permission_content),
                                            getString(R.string.app_cancel),
                                            getString(R.string.app_ok),
                                            new CommonConfirmDialog.Callback() {

                                              @Override
                                              public void result(@Nullable Boolean aBoolean) {
                                                if (aBoolean != null && aBoolean) {
                                                  NavUtils.goToSetting(requireActivity());
                                                }
                                              }
                                            });
                                  }
                                }

                                @Override
                                public void onException(Exception exception) {}
                              });
            });
    binding.networkDetect.setOnClickListener(
        v -> {
          initRTC();
          NERtcEx.getInstance().startLastmileProbeTest(new LastmileProbeConfig());
          toggleLoading(true);
        });
    binding.commonSetting.setOnClickListener(v -> NavUtils.toCommonSettingPage(requireActivity()));
    binding.phoneConsult.setOnClickListener(
        v -> {
          PhoneConsultBottomDialog dialog = new PhoneConsultBottomDialog(requireActivity());
          dialog.show();
        });
  }

  private void initUser() {
    binding.ivUserPortrait.loadAvatar(AppUtils.getAvatar());
    binding.tvUserName.setText(AppUtils.getUserName());
  }

  private void initDataCenter() {
    ActivityResultLauncher<Intent> launcher =
        registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
              if (result.getResultCode() == RESULT_OK) {
                if (result.getData() != null) {
                  String nick = result.getData().getStringExtra(Constants.INTENT_KEY_NICK);
                  binding.tvUserName.setText(nick);
                }
              }
            });
  }

  private void toggleLoading(boolean show) {
    if (loadingDialog == null) {
      loadingDialog = new LoadingDialog(requireActivity());
    }
    if (show && !loadingDialog.isShowing()) {
      loadingDialog.show();
    } else if (!show) {
      loadingDialog.dismiss();
      loadingDialog = null;
    }
  }

  private void initRTC() {
    try {
      NERtcEx.getInstance()
          .init(requireActivity(), AppConfig.getAppKey(), neRtcCallbackExTemp, null);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  private void mergeInfo(int quality, LastmileProbeResult probeResult) {
    this.quality = quality;
    this.probeResult = probeResult;
    if (count == CALLBACK_TOTAL_COUNT) {
      toggleLoading(false);
      NERtcEx.getInstance().stopLastmileProbeTest();
      NERtcEx.getInstance().release();
      StringBuilder stringBuilder = new StringBuilder();
      stringBuilder
          .append(getString(R.string.network_quality))
          .append(covertQuality(this.quality))
          .append("\n")
          .append(getString(R.string.quality_result))
          .append(covertState(this.probeResult.state))
          .append("\n")
          .append(getString(R.string.network_rtt))
          .append(this.probeResult.rtt + "ms")
          .append("\n")
          .append(getString(R.string.network_up_packet_loss_rate))
          .append(this.probeResult.uplinkReport.packetLossRate + "%")
          .append("\n")
          .append(getString(R.string.network_up_jitter))
          .append(this.probeResult.uplinkReport.jitter + "ms")
          .append("\n")
          .append(getString(R.string.network_up_avaliable_band_width))
          .append(this.probeResult.uplinkReport.availableBandwidth + "bps")
          .append("\n")
          .append(getString(R.string.network_down_packet_loss_rate))
          .append(this.probeResult.downlinkReport.packetLossRate + "%")
          .append("\n")
          .append(getString(R.string.network_down_jitter))
          .append(this.probeResult.downlinkReport.jitter + "ms")
          .append("\n")
          .append(getString(R.string.network_down_available_band_width))
          .append(this.probeResult.downlinkReport.availableBandwidth + "bps");
      NetworkInfoDialog dialog = new NetworkInfoDialog(requireActivity());
      dialog.setContent(stringBuilder.toString());
      dialog.setDialogCallback(Dialog::dismiss);
      dialog.show();
      count = 0;
      this.quality = -1;
      this.probeResult = null;
    }
  }

  private String covertQuality(int quality) {
    if (quality == NERtcConstants.NERtcLastmileQuality.QUALITY_UNKNOWN) {
      return getString(R.string.quality_unknown);
    } else if (quality == NERtcConstants.NERtcLastmileQuality.QUALITY_EXCELLENT) {
      return getString(R.string.quality_excellent);
    } else if (quality == NERtcConstants.NERtcLastmileQuality.QUALITY_GOOD) {
      return getString(R.string.quality_good);
    } else if (quality == NERtcConstants.NERtcLastmileQuality.QUALITY_POOR) {
      return getString(R.string.quality_poor);
    } else if (quality == NERtcConstants.NERtcLastmileQuality.QUALITY_BAD) {
      return getString(R.string.quality_bad);
    } else if (quality == NERtcConstants.NERtcLastmileQuality.QUALITY_VBAD) {
      return getString(R.string.quality_vbad);
    } else if (quality == NERtcConstants.NERtcLastmileQuality.QUALITY_DOWN) {
      return getString(R.string.quality_down);
    }
    return "";
  }

  private String covertState(short state) {
    if (state == NERtcConstants.NERtcLastmileResult.LASTMILE_PROBE_RESULT_COMPLETE) {
      return getString(R.string.state_result_complete);
    } else if (state
        == NERtcConstants.NERtcLastmileResult.LASTMILE_PROBE_RESULT_INCOMPLETE_NO_BWE) {
      return getString(R.string.state_result_incomplete_no_bwe);
    } else if (state == NERtcConstants.NERtcLastmileResult.LASTMILE_PROBE_RESULT_UNAVAILABLE) {
      return getString(R.string.state_result_unavailable);
    }
    return "";
  }

  @Override
  public void onDestroy() {
    super.onDestroy();
  }
}
