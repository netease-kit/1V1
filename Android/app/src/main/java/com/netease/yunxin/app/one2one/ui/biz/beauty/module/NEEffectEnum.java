/*
 *
 *  * Copyright (c) 2022 NetEase, Inc.  All rights reserved.
 *  * Use of this source code is governed by a MIT license that can be found in the LICENSE file.
 *
 */

package com.netease.yunxin.app.one2one.ui.biz.beauty.module;

import com.netease.lava.nertc.sdk.video.NERtcBeautyEffectType;
import com.netease.yunxin.app.one2one.R;

import java.util.HashMap;

public enum NEEffectEnum {
    WHITETEETH(R.id.rb_effect_whiteteeth, NERtcBeautyEffectType.kNERtcBeautyWhiteTeeth, 0.6f),
    LIGHTEYE(R.id.rb_effect_lighteye, NERtcBeautyEffectType.kNERtcBeautyLightEye, 0.6f),
    WHITEN(R.id.rb_effect_whiten, NERtcBeautyEffectType.kNERtcBeautyWhiten, 0.6f),
    SMOOTH(R.id.rb_effect_smooth, NERtcBeautyEffectType.kNERtcBeautySmooth, 0.8f),
    FACERUDDY(R.id.rb_effect_faceruddy, NERtcBeautyEffectType.kNERtcBeautyFaceRuddy, 0.0f),
    FACESHARPEN(R.id.rb_effect_facesharpen, NERtcBeautyEffectType.kNERtcBeautyFaceSharpen, 0.0f);

    private int resId;
    private NERtcBeautyEffectType type;
    private float level;

    NEEffectEnum(int resId, NERtcBeautyEffectType type, float level) {
        this.resId = resId;
        this.type = type;
        this.level = level;
    }

    public static HashMap<Integer, NEEffect> getEffects() {
        NEEffectEnum[] neEffectEnums = NEEffectEnum.values();
        HashMap<Integer, NEEffect> effects = new HashMap<>();
        for (NEEffectEnum beauty : neEffectEnums) {
            effects.put(beauty.resId, new NEEffect(beauty.resId, beauty.type, beauty.level));
        }

        return effects;
    }
}
