/*
 *
 *  * Copyright (c) 2022 NetEase, Inc.  All rights reserved.
 *  * Use of this source code is governed by a MIT license that can be found in the LICENSE file.
 *
 */

package com.netease.yunxin.app.one2one.ui.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.RelativeLayout;

import com.netease.yunxin.app.one2one.databinding.ViewInThVideoCallBottomBarBinding;

public class InTheVideoCallBottomBar extends RelativeLayout {
    private ViewInThVideoCallBottomBarBinding binding;

    public InTheVideoCallBottomBar(Context context) {
        super(context);
        init(context);
    }

    public InTheVideoCallBottomBar(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public InTheVideoCallBottomBar(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        binding = ViewInThVideoCallBottomBarBinding.inflate(LayoutInflater.from(context), this, true);
        binding.ivMicrophone.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                if (onItemClickListener != null) {
                    onItemClickListener.onMicroPhoneButtonClick();
                }
            }
        });
        binding.ivAudio.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                if (onItemClickListener != null) {
                    onItemClickListener.onAudioButtonClick();
                }
            }
        });

        binding.ivSwitchCamera.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                if (onItemClickListener != null) {
                    onItemClickListener.onSwitchCameraButtonClick();
                }
            }
        });

        binding.ivHangup.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                if (onItemClickListener != null) {
                    onItemClickListener.onHangupButtonClick();
                }
            }
        });
    }

    public ViewInThVideoCallBottomBarBinding getViewBinding() {
        return binding;
    }

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

    private OnItemClickListener onItemClickListener;

    public interface OnItemClickListener {
        void onMicroPhoneButtonClick();

        void onAudioButtonClick();

        void onSwitchCameraButtonClick();

        void onHangupButtonClick();
    }
}
