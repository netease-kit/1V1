/*
 *
 *  * Copyright (c) 2022 NetEase, Inc.  All rights reserved.
 *  * Use of this source code is governed by a MIT license that can be found in the LICENSE file.
 *
 */

package com.netease.yunxin.app.one2one.ui.view;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.netease.yunxin.android.lib.picture.ImageLoader;
import com.netease.yunxin.app.one2one.databinding.RvItemAlbumBinding;
import com.netease.yunxin.app.one2one.utils.AppGlobals;
import com.netease.yunxin.app.one2one.utils.DisplayUtils;

import java.util.ArrayList;

public class AlbumsRecycleView extends RecyclerView {

    private AlbumAdapter albumAdapter;

    public AlbumsRecycleView(@NonNull Context context) {
        super(context);
        init(context);
    }

    public AlbumsRecycleView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public AlbumsRecycleView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        albumAdapter = new AlbumAdapter();
        setAdapter(albumAdapter);
        setLayoutManager(new LinearLayoutManager(context, RecyclerView.HORIZONTAL, false));
        addItemDecoration(new ItemDecoration() {
            @Override
            public void getItemOffsets(@NonNull Rect outRect, @NonNull View view, @NonNull RecyclerView parent, @NonNull State state) {
                super.getItemOffsets(outRect, view, parent, state);
                if (parent.getChildAdapterPosition(view) == 0) {
                    outRect.left = (int) DisplayUtils.dp2px(20);
                } else {
                    outRect.left = (int) DisplayUtils.dp2px(5);
                }
            }
        });
    }

    @SuppressLint("NotifyDataSetChanged")
    public void bindData(ArrayList<String> list) {
        albumAdapter.bindData(list);
    }

    static final class AlbumAdapter extends RecyclerView.Adapter {
        ArrayList<String> list;

        public void bindData(ArrayList<String> list) {
            this.list = list;
            notifyDataSetChanged();
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            RvItemAlbumBinding binding = RvItemAlbumBinding.inflate(LayoutInflater.from(AppGlobals.getApplication()), parent, false);
            return new AlbumHolder(binding);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            AlbumHolder albumHolder = (AlbumHolder) holder;
            albumHolder.bindData(list.get(position));
        }

        @Override
        public int getItemCount() {
            if (list==null){
                return 0;
            }
            return list.size();
        }
    }

    public static class AlbumHolder extends RecyclerView.ViewHolder {
        private RvItemAlbumBinding binding;

        public AlbumHolder(@NonNull RvItemAlbumBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        @SuppressLint("SetTextI18n")
        public void bindData(String url) {
            Context context = AppGlobals.getApplication();
            ImageLoader.with(context).roundedCorner(url, (int) DisplayUtils.dp2px(4), binding.iv);
        }
    }

}
