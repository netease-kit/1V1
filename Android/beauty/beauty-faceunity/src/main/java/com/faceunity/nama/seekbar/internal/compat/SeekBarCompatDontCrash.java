// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.faceunity.nama.seekbar.internal.compat;

import android.annotation.TargetApi;
import android.content.res.ColorStateList;
import android.graphics.Outline;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.RippleDrawable;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewOutlineProvider;
import android.view.ViewParent;
import android.widget.TextView;
import com.faceunity.nama.seekbar.internal.drawable.MarkerDrawable;

/**
 * Wrapper compatibility class to call some API-Specific methods And offer alternate procedures when
 * possible
 *
 * @hide
 */
@TargetApi(21)
class SeekBarCompatDontCrash {
  public static void setOutlineProvider(View marker, final MarkerDrawable markerDrawable) {
    marker.setOutlineProvider(
        new ViewOutlineProvider() {
          @Override
          public void getOutline(View view, Outline outline) {
            outline.setConvexPath(markerDrawable.getPath());
          }
        });
  }

  public static Drawable getRipple(ColorStateList colorStateList) {
    return new RippleDrawable(colorStateList, null, null);
  }

  public static void setBackground(View view, Drawable background) {
    view.setBackground(background);
  }

  public static void setTextDirection(TextView number, int textDirection) {
    number.setTextDirection(textDirection);
  }

  public static boolean isInScrollingContainer(ViewParent p) {
    while (p != null && p instanceof ViewGroup) {
      if (((ViewGroup) p).shouldDelayChildPressedState()) {
        return true;
      }
      p = p.getParent();
    }
    return false;
  }

  public static boolean isHardwareAccelerated(View view) {
    return view.isHardwareAccelerated();
  }
}
