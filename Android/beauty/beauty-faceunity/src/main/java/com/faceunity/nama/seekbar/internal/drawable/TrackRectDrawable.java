// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.faceunity.nama.seekbar.internal.drawable;

import android.content.res.ColorStateList;
import android.graphics.Canvas;
import android.graphics.Paint;
import androidx.annotation.NonNull;

/**
 * Simple {@link StateDrawable} implementation to draw rectangles
 *
 * @hide
 */
public class TrackRectDrawable extends StateDrawable {
  public TrackRectDrawable(@NonNull ColorStateList tintStateList) {
    super(tintStateList);
  }

  @Override
  void doDraw(Canvas canvas, Paint paint) {
    canvas.drawRect(getBounds(), paint);
  }
}
