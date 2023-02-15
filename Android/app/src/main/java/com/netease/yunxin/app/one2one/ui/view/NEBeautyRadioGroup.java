// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.app.one2one.ui.view;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import com.netease.yunxin.app.one2one.R;

public class NEBeautyRadioGroup extends RadioGroup {
  private static final float DEFAULT_ALPHA = 1.0f;
  private static final float SELECTED_ALPHA = 0.8f;
  private int mLastCheckedId = -1;
  private OnCheckedChangeListener mOnCheckedChangeListener;
  private boolean isReset;

  public NEBeautyRadioGroup(Context context) {
    super(context);
    init();
  }

  public NEBeautyRadioGroup(Context context, AttributeSet attrs) {
    super(context, attrs);
    init();
  }

  private void init() {
    super.setOnCheckedChangeListener(new CheckStateListener());
  }

  @Override
  public void setOnCheckedChangeListener(OnCheckedChangeListener listener) {
    mOnCheckedChangeListener = listener;
  }

  private class CheckStateListener implements OnCheckedChangeListener {

    @Override
    public void onCheckedChanged(RadioGroup radioGroup, int checkedId) {
      if (isReset) {
        checkButtonState(checkedId, false);
        isReset = false;
      } else {
        if (mLastCheckedId != -1) {
          RadioButton lastCheckBtn = radioGroup.findViewById(mLastCheckedId);
          lastCheckBtn.setTextColor(getResources().getColor(R.color.color_80ffffff));
        }
        checkButtonState(mLastCheckedId, false);
        checkButtonState(checkedId, true);
        mLastCheckedId = checkedId;
        RadioButton currentBtn = radioGroup.findViewById(radioGroup.getCheckedRadioButtonId());
        if (currentBtn != null) {
          currentBtn.setTextColor(getResources().getColor(R.color.white));
        }
        if (mOnCheckedChangeListener != null) {
          mOnCheckedChangeListener.onCheckedChanged(radioGroup, checkedId);
        }
      }
    }
  }

  @SuppressLint("UseCompatLoadingForDrawables")
  public void checkButtonState(int id, boolean isChecked) {
    View view = findViewById(id);
    if (view instanceof MyRadioButton) {
      MyRadioButton radioButton = (MyRadioButton) view;
      if (id == R.id.rb_effect_smooth) {
        if (isChecked) {
          radioButton.setCompoundDrawablesWithIntrinsicBounds(
              getContext().getDrawable(R.drawable.mopi2), null, null, null);
        } else {
          radioButton.setCompoundDrawablesWithIntrinsicBounds(
              getContext().getDrawable(R.drawable.mopi), null, null, null);
        }
      } else if (id == R.id.rb_effect_whiten) {
        if (isChecked) {
          radioButton.setCompoundDrawablesWithIntrinsicBounds(
              getContext().getDrawable(R.drawable.meibai2), null, null, null);
        } else {
          radioButton.setCompoundDrawablesWithIntrinsicBounds(
              getContext().getDrawable(R.drawable.meibai), null, null, null);
        }
      } else if (id == R.id.rb_effect_faceruddy) {
        if (isChecked) {
          radioButton.setCompoundDrawablesWithIntrinsicBounds(
              getContext().getDrawable(R.drawable.hongrun2), null, null, null);
        } else {
          radioButton.setCompoundDrawablesWithIntrinsicBounds(
              getContext().getDrawable(R.drawable.hongrun), null, null, null);
        }
      } else if (id == R.id.rb_effect_facesharpen) {
        if (isChecked) {
          radioButton.setCompoundDrawablesWithIntrinsicBounds(
              getContext().getDrawable(R.drawable.ruihua2), null, null, null);
        } else {
          radioButton.setCompoundDrawablesWithIntrinsicBounds(
              getContext().getDrawable(R.drawable.ruihua), null, null, null);
        }
      } else if (id == R.id.rb_effect_lighteye) {
        if (isChecked) {
          radioButton.setCompoundDrawablesWithIntrinsicBounds(
              getContext().getDrawable(R.drawable.liangyan2), null, null, null);
        } else {
          radioButton.setCompoundDrawablesWithIntrinsicBounds(
              getContext().getDrawable(R.drawable.liangyan), null, null, null);
        }
      } else if (id == R.id.rb_effect_whiteteeth) {
        if (isChecked) {
          radioButton.setCompoundDrawablesWithIntrinsicBounds(
              getContext().getDrawable(R.drawable.meiya2), null, null, null);
        } else {
          radioButton.setCompoundDrawablesWithIntrinsicBounds(
              getContext().getDrawable(R.drawable.meiya), null, null, null);
        }
      } else if (id == R.id.rb_effect_thinface) {
        if (isChecked) {
          radioButton.setCompoundDrawablesWithIntrinsicBounds(
              getContext().getDrawable(R.drawable.shoulian2), null, null, null);
        } else {
          radioButton.setCompoundDrawablesWithIntrinsicBounds(
              getContext().getDrawable(R.drawable.shoulian), null, null, null);
        }
      } else if (id == R.id.rb_effect_vface) {
        if (isChecked) {
          radioButton.setCompoundDrawablesWithIntrinsicBounds(
              getContext().getDrawable(R.drawable.vlian2), null, null, null);
        } else {
          radioButton.setCompoundDrawablesWithIntrinsicBounds(
              getContext().getDrawable(R.drawable.vlian), null, null, null);
        }
      } else if (id == R.id.rb_effect_narrowface) {
        if (isChecked) {
          radioButton.setCompoundDrawablesWithIntrinsicBounds(
              getContext().getDrawable(R.drawable.zhailian2), null, null, null);
        } else {
          radioButton.setCompoundDrawablesWithIntrinsicBounds(
              getContext().getDrawable(R.drawable.zhailian), null, null, null);
        }
      } else if (id == R.id.rb_effect_smallface) {
        if (isChecked) {
          radioButton.setCompoundDrawablesWithIntrinsicBounds(
              getContext().getDrawable(R.drawable.xiaolian2), null, null, null);
        } else {
          radioButton.setCompoundDrawablesWithIntrinsicBounds(
              getContext().getDrawable(R.drawable.xiaolian), null, null, null);
        }
      } else if (id == R.id.rb_effect_underjaw) {
        if (isChecked) {
          radioButton.setCompoundDrawablesWithIntrinsicBounds(
              getContext().getDrawable(R.drawable.xiahe2), null, null, null);
        } else {
          radioButton.setCompoundDrawablesWithIntrinsicBounds(
              getContext().getDrawable(R.drawable.xiahe), null, null, null);
        }
      } else if (id == R.id.rb_effect_cheekbone) {
        if (isChecked) {
          radioButton.setCompoundDrawablesWithIntrinsicBounds(
              getContext().getDrawable(R.drawable.lianjia2), null, null, null);
        } else {
          radioButton.setCompoundDrawablesWithIntrinsicBounds(
              getContext().getDrawable(R.drawable.lianjia), null, null, null);
        }
      } else if (id == R.id.rb_effect_jaw) {
        if (isChecked) {
          radioButton.setCompoundDrawablesWithIntrinsicBounds(
              getContext().getDrawable(R.drawable.xiaba2), null, null, null);
        } else {
          radioButton.setCompoundDrawablesWithIntrinsicBounds(
              getContext().getDrawable(R.drawable.xiaba), null, null, null);
        }
      } else if (id == R.id.rb_effect_bigeye) {
        if (isChecked) {
          radioButton.setCompoundDrawablesWithIntrinsicBounds(
              getContext().getDrawable(R.drawable.dayan2), null, null, null);
        } else {
          radioButton.setCompoundDrawablesWithIntrinsicBounds(
              getContext().getDrawable(R.drawable.dayan), null, null, null);
        }
      } else if (id == R.id.rb_effect_eyedis) {
        if (isChecked) {
          radioButton.setCompoundDrawablesWithIntrinsicBounds(
              getContext().getDrawable(R.drawable.yanju2), null, null, null);
        } else {
          radioButton.setCompoundDrawablesWithIntrinsicBounds(
              getContext().getDrawable(R.drawable.yanju), null, null, null);
        }
      } else if (id == R.id.rb_effect_eyecorner) {
        if (isChecked) {
          radioButton.setCompoundDrawablesWithIntrinsicBounds(
              getContext().getDrawable(R.drawable.yanjiao2), null, null, null);
        } else {
          radioButton.setCompoundDrawablesWithIntrinsicBounds(
              getContext().getDrawable(R.drawable.yanjiao), null, null, null);
        }
      } else if (id == R.id.rb_effect_roundeye) {
        if (isChecked) {
          radioButton.setCompoundDrawablesWithIntrinsicBounds(
              getContext().getDrawable(R.drawable.yuanyan2), null, null, null);
        } else {
          radioButton.setCompoundDrawablesWithIntrinsicBounds(
              getContext().getDrawable(R.drawable.yuanyan), null, null, null);
        }
      } else if (id == R.id.rb_effect_mouth) {
        if (isChecked) {
          radioButton.setCompoundDrawablesWithIntrinsicBounds(
              getContext().getDrawable(R.drawable.zuiba2), null, null, null);
        } else {
          radioButton.setCompoundDrawablesWithIntrinsicBounds(
              getContext().getDrawable(R.drawable.zuiba), null, null, null);
        }
      } else if (id == R.id.rb_effect_mouthangle) {
        if (isChecked) {
          radioButton.setCompoundDrawablesWithIntrinsicBounds(
              getContext().getDrawable(R.drawable.zuijiao2), null, null, null);
        } else {
          radioButton.setCompoundDrawablesWithIntrinsicBounds(
              getContext().getDrawable(R.drawable.zuijiao), null, null, null);
        }
      } else if (id == R.id.rb_effect_smallnose) {
        if (isChecked) {
          radioButton.setCompoundDrawablesWithIntrinsicBounds(
              getContext().getDrawable(R.drawable.xiaobi2), null, null, null);
        } else {
          radioButton.setCompoundDrawablesWithIntrinsicBounds(
              getContext().getDrawable(R.drawable.xiaobi), null, null, null);
        }
      } else if (id == R.id.rb_effect_longnose) {
        if (isChecked) {
          radioButton.setCompoundDrawablesWithIntrinsicBounds(
              getContext().getDrawable(R.drawable.changbi2), null, null, null);
        } else {
          radioButton.setCompoundDrawablesWithIntrinsicBounds(
              getContext().getDrawable(R.drawable.changbi), null, null, null);
        }
      } else if (id == R.id.rb_effect_philtrum) {
        if (isChecked) {
          radioButton.setCompoundDrawablesWithIntrinsicBounds(
              getContext().getDrawable(R.drawable.renzhong2), null, null, null);
        } else {
          radioButton.setCompoundDrawablesWithIntrinsicBounds(
              getContext().getDrawable(R.drawable.renzhong), null, null, null);
        }
      }
    }
  }

  public void reset() {
    isReset = true;
    clearCheck();
  }
}
