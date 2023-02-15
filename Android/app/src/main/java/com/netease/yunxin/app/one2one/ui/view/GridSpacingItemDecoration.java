// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.app.one2one.ui.view;

import android.graphics.Rect;
import android.view.View;
import androidx.recyclerview.widget.RecyclerView;
import com.netease.yunxin.app.one2one.utils.DisplayUtils;

/**
 * @author: skd
 * @date 2019-09-06 @Desc GridSpacingItemDecoration
 */
public class GridSpacingItemDecoration extends RecyclerView.ItemDecoration {
  private int spanCount;
  private int spacing;
  private boolean includeEdge;
  private int headerNum;

  public GridSpacingItemDecoration(int spanCount, int spacing, boolean includeEdge, int headerNum) {
    this.spanCount = spanCount;
    this.spacing = spacing;
    this.includeEdge = includeEdge;
    this.headerNum = headerNum;
  }

  @Override
  public void getItemOffsets(
      Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
    int position = parent.getChildAdapterPosition(view) - headerNum; // item position

    if (position >= 0) {
      int column = position % spanCount; // item column

      if (includeEdge) {
        outRect.left =
            spacing
                - column * spacing / spanCount; // spacing - column * ((1f / spanCount) * spacing)
        outRect.right =
            (column + 1) * spacing / spanCount; // (column + 1) * ((1f / spanCount) * spacing)

        if (position < spanCount) { // top edge
          outRect.top = spacing;
        }
        outRect.bottom = spacing; // item bottom
      } else {
        outRect.left = column * spacing / spanCount; // column * ((1f / spanCount) * spacing)
        outRect.right =
            spacing
                - (column + 1)
                    * spacing
                    / spanCount; // spacing - (column + 1) * ((1f /    spanCount) * spacing)
        if (position >= spanCount) {
          outRect.top = spacing; // item top
        }
      }
      if (position <= 1) {
        outRect.top = (int) DisplayUtils.dp2px(16);
      } else if (position >= 8) {
        outRect.bottom = (int) DisplayUtils.dp2px(126);
      }
    } else {
      outRect.left = 0;
      outRect.right = 0;
      outRect.top = 0;
      outRect.bottom = 0;
    }
  }
}
