/*
 *
 *  * Copyright (c) 2022 NetEase, Inc.  All rights reserved.
 *  * Use of this source code is governed by a MIT license that can be found in the LICENSE file.
 *
 */

package com.netease.yunxin.app.one2one.ui.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.netease.yunxin.app.one2one.R;
import com.netease.yunxin.app.one2one.databinding.ViewSettingItemBinding;

public class SettingItemView extends RelativeLayout {
    private ViewSettingItemBinding binding;
    public SettingItemView(Context context) {
        super(context);
        init(context, null);
    }

    public SettingItemView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public SettingItemView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {
        binding=ViewSettingItemBinding.inflate(LayoutInflater.from(context), this, true);
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.SettingItemView);
        String title = typedArray.getString(R.styleable.SettingItemView_setting_item_title);
        typedArray.recycle();
        setTitle(title);
        binding.root.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                if (onItemClickListener != null) {
                    onItemClickListener.onItemClick();
                }
            }
        });
    }

    public void setTitle(String title) {
        if (TextUtils.isEmpty(title)) {
            return;
        }
        binding.tv.setText(title);
    }


    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

    private OnItemClickListener onItemClickListener;

    public interface OnItemClickListener {
        void onItemClick();
    }
}
