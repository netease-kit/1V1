// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.app.one2one.ui.view;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import androidx.appcompat.widget.AppCompatEditText;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.drawable.DrawableCompat;
import com.netease.yunxin.app.one2one.R;

public class ClearEditText extends AppCompatEditText
    implements View.OnTouchListener, View.OnFocusChangeListener, TextWatcher {

  private Drawable mClearTextIcon;
  private OnFocusChangeListener mOnFocusChangeListener;
  private OnTouchListener mOnTouchListener;

  public ClearEditText(final Context context) {
    super(context);
    init(context);
  }

  public ClearEditText(final Context context, final AttributeSet attrs) {
    super(context, attrs);
    init(context);
  }

  public ClearEditText(final Context context, final AttributeSet attrs, final int defStyleAttr) {
    super(context, attrs, defStyleAttr);
    init(context);
  }

  private void init(final Context context) {
    final Drawable drawable = ContextCompat.getDrawable(context, R.drawable.icon_close_for_edit);
    final Drawable wrappedDrawable =
        DrawableCompat.wrap(drawable); //Wrap the drawable so that it can be tinted pre Lollipop
    DrawableCompat.setTint(wrappedDrawable, getCurrentHintTextColor());
    mClearTextIcon = wrappedDrawable;
    mClearTextIcon.setBounds(
        0, 0, mClearTextIcon.getIntrinsicHeight(), mClearTextIcon.getIntrinsicHeight());
    setClearIconVisible(false);
    super.setOnTouchListener(this);
    super.setOnFocusChangeListener(this);
    addTextChangedListener(this);
  }

  @Override
  public void setOnFocusChangeListener(OnFocusChangeListener l) {
    mOnFocusChangeListener = l;
  }

  @Override
  public void setOnTouchListener(OnTouchListener l) {
    mOnTouchListener = l;
  }

  @Override
  public void onFocusChange(View v, boolean hasFocus) {
    if (hasFocus) {
      setClearIconVisible(getText().length() > 0);
    } else {
      setClearIconVisible(false);
    }
    if (mOnFocusChangeListener != null) {
      mOnFocusChangeListener.onFocusChange(v, hasFocus);
    }
  }

  @Override
  public boolean onTouch(View view, MotionEvent motionEvent) {
    final int x = (int) motionEvent.getX();
    if (mClearTextIcon.isVisible()
        && x > getWidth() - getPaddingRight() - mClearTextIcon.getIntrinsicWidth()) {
      if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
        setError(null);
        setText("");
      }
      return true;
    }
    return mOnTouchListener != null && mOnTouchListener.onTouch(view, motionEvent);
  }

  @Override
  public final void onTextChanged(CharSequence text, int start, int lengthBefore, int lengthAfter) {
    if (isFocused()) {
      setClearIconVisible(text.length() > 0);
    }
  }

  @Override
  public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

  @Override
  public void afterTextChanged(Editable s) {}

  private void setClearIconVisible(final boolean visible) {
    mClearTextIcon.setVisible(visible, false);
    final Drawable[] compoundDrawables = getCompoundDrawables();
    setCompoundDrawables(
        compoundDrawables[0],
        compoundDrawables[1],
        visible ? mClearTextIcon : null,
        compoundDrawables[3]);
  }
}
