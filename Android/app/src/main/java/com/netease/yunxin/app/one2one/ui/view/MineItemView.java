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
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.netease.yunxin.app.one2one.R;
import com.netease.yunxin.app.one2one.databinding.ViewMineItemBinding;

public class MineItemView extends RelativeLayout {
    private ViewMineItemBinding binding;
    public MineItemView(Context context) {
        super(context);
        init(context, null);
    }

    public MineItemView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public MineItemView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {
        binding=ViewMineItemBinding.inflate(LayoutInflater.from(context), this, true);
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.MineItemView);
        String title = typedArray.getString(R.styleable.MineItemView_mine_item_title);
        int resId = typedArray.getResourceId(R.styleable.MineItemView_mine_item_icon,-1);
        typedArray.recycle();
        setTitle(title);
        binding.ivIcon.setImageResource(resId);
        binding.root.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                if (onItemClickListener != null) {
                    onItemClickListener.onItemClick();
                }
            }
        });
    }

    private void setTitle(String title) {
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
