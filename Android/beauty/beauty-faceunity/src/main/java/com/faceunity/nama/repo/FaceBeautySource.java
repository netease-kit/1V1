// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.faceunity.nama.repo;

import com.faceunity.core.controller.facebeauty.FaceBeautyParam;
import com.faceunity.core.entity.FUBundleData;
import com.faceunity.core.enumeration.FUFaceBeautyMultiModePropertyEnum;
import com.faceunity.core.enumeration.FUFaceBeautyPropertyModeEnum;
import com.faceunity.core.model.facebeauty.FaceBeauty;
import com.faceunity.core.model.facebeauty.FaceBeautyBlurTypeEnum;
import com.faceunity.core.model.facebeauty.FaceBeautyFilterEnum;
import com.faceunity.nama.FUConfig;
import com.faceunity.nama.R;
import com.faceunity.nama.entity.FaceBeautyBean;
import com.faceunity.nama.entity.FaceBeautyFilterBean;
import com.faceunity.nama.entity.ModelAttributeData;
import com.faceunity.nama.utils.FuDeviceUtils;
import com.netease.yunxin.kit.common.utils.SPUtils;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

/** DESC：美颜数据构造 Created on 2021/3/27 */
public class FaceBeautySource {

  public static String BUNDLE_FACE_BEAUTIFICATION =
      "graphics" + File.separator + "face_beautification.bundle";
  private static final String SP_KEY_CURRENT_FILTER_NAME = "currentFilterName";

  /**
   * 获取默认推荐美颜模型
   *
   * @return
   */
  public static FaceBeauty getDefaultFaceBeauty() {
    FaceBeauty recommendFaceBeauty = new FaceBeauty(new FUBundleData(BUNDLE_FACE_BEAUTIFICATION));
    recommendFaceBeauty.setFilterName(getCurrentFilterName());
    recommendFaceBeauty.setFilterIntensity(getFilterIntensity(getCurrentFilterName(), 0.4f));
    /** *** 美肤 **** */
    // 精细磨皮
    recommendFaceBeauty.setBlurType(FaceBeautyBlurTypeEnum.FineSkin);
    recommendFaceBeauty.setBlurIntensity(getParamIntensity(FaceBeautyParam.BLUR_INTENSITY, 4.2f));
    // 美白
    recommendFaceBeauty.setColorIntensity(getParamIntensity(FaceBeautyParam.COLOR_INTENSITY, 0.3f));
    // 红润
    recommendFaceBeauty.setRedIntensity(getParamIntensity(FaceBeautyParam.RED_INTENSITY, 0.3f));
    // 锐化
    recommendFaceBeauty.setSharpenIntensity(
        getParamIntensity(FaceBeautyParam.SHARPEN_INTENSITY, 0.2f));
    // 亮眼
    recommendFaceBeauty.setEyeBrightIntensity(
        getParamIntensity(FaceBeautyParam.EYE_BRIGHT_INTENSITY, 0.0f));
    // 美牙
    recommendFaceBeauty.setToothIntensity(
        getParamIntensity(FaceBeautyParam.TOOTH_WHITEN_INTENSITY, 0.0f));
    // 去黑眼圈
    recommendFaceBeauty.setRemovePouchIntensity(
        getParamIntensity(FaceBeautyParam.REMOVE_POUCH_INTENSITY, 0.0f));
    // 去法令纹
    recommendFaceBeauty.setRemoveLawPatternIntensity(
        getParamIntensity(FaceBeautyParam.REMOVE_NASOLABIAL_FOLDS_INTENSITY, 0.0f));

    /** *** 美型 **** */
    // 瘦脸
    recommendFaceBeauty.setCheekThinningIntensity(
        getParamIntensity(FaceBeautyParam.CHEEK_THINNING_INTENSITY, 0.0f));
    // V脸
    recommendFaceBeauty.setCheekVIntensity(
        getParamIntensity(FaceBeautyParam.CHEEK_V_INTENSITY, 0.5f));
    // 窄脸
    recommendFaceBeauty.setCheekNarrowIntensity(
        getParamIntensity(FaceBeautyParam.CHEEK_NARROW_INTENSITY, 0.0f));
    // 短脸
    recommendFaceBeauty.setCheekShortIntensity(
        getParamIntensity(FaceBeautyParam.CHEEK_SHORT_INTENSITY, 0.0f));
    // 小脸
    recommendFaceBeauty.setCheekSmallIntensity(
        getParamIntensity(FaceBeautyParam.CHEEK_SMALL_INTENSITY, 0.0f));
    // 瘦颧骨
    recommendFaceBeauty.setCheekBonesIntensity(
        getParamIntensity(FaceBeautyParam.INTENSITY_CHEEKBONES_INTENSITY, 0.0f));
    // 瘦下颌骨
    recommendFaceBeauty.setLowerJawIntensity(
        getParamIntensity(FaceBeautyParam.INTENSITY_LOW_JAW_INTENSITY, 0.0f));
    // 大眼
    recommendFaceBeauty.setEyeEnlargingIntensity(
        getParamIntensity(FaceBeautyParam.EYE_ENLARGING_INTENSITY, 0.4f));
    // 圆眼
    recommendFaceBeauty.setEyeCircleIntensity(
        getParamIntensity(FaceBeautyParam.EYE_CIRCLE_INTENSITY, 0.0f));
    // 下巴
    recommendFaceBeauty.setChinIntensity(getParamIntensity(FaceBeautyParam.CHIN_INTENSITY, 0.3f));
    // 额头
    recommendFaceBeauty.setForHeadIntensity(
        getParamIntensity(FaceBeautyParam.FOREHEAD_INTENSITY, 0.3f));
    // 瘦鼻
    recommendFaceBeauty.setNoseIntensity(getParamIntensity(FaceBeautyParam.NOSE_INTENSITY, 0.5f));
    // 嘴型
    recommendFaceBeauty.setMouthIntensity(getParamIntensity(FaceBeautyParam.MOUTH_INTENSITY, 0.4f));
    // 开眼角
    recommendFaceBeauty.setCanthusIntensity(
        getParamIntensity(FaceBeautyParam.CANTHUS_INTENSITY, 0.0f));
    // 眼距
    recommendFaceBeauty.setEyeSpaceIntensity(
        getParamIntensity(FaceBeautyParam.EYE_SPACE_INTENSITY, 0.5f));
    // 眼睛角度
    recommendFaceBeauty.setEyeRotateIntensity(
        getParamIntensity(FaceBeautyParam.EYE_ROTATE_INTENSITY, 0.5f));
    // 长鼻
    recommendFaceBeauty.setLongNoseIntensity(
        getParamIntensity(FaceBeautyParam.LONG_NOSE_INTENSITY, 0.5f));
    // 缩人中
    recommendFaceBeauty.setPhiltrumIntensity(
        getParamIntensity(FaceBeautyParam.PHILTRUM_INTENSITY, 0.5f));
    // 微笑嘴角
    recommendFaceBeauty.setSmileIntensity(getParamIntensity(FaceBeautyParam.SMILE_INTENSITY, 0.0f));
    // 眉毛上下
    recommendFaceBeauty.setBrowHeightIntensity(
        getParamIntensity(FaceBeautyParam.BROW_HEIGHT_INTENSITY, 0.5f));
    // 眉间距
    recommendFaceBeauty.setBrowSpaceIntensity(
        getParamIntensity(FaceBeautyParam.BROW_SPACE_INTENSITY, 0.5f));
    //性能最优策略
    if (FUConfig.DEVICE_LEVEL > FuDeviceUtils.DEVICE_LEVEL_MID) {
      setFaceBeautyPropertyMode(recommendFaceBeauty);
    }
    return recommendFaceBeauty;
  }

  /** 高端机的时候，开启4个相对吃性能的模式 1.祛黑眼圈 MODE2 2.祛法令纹 MODE2 3.大眼 MODE3 4.嘴型 MODE3 */
  private static void setFaceBeautyPropertyMode(FaceBeauty faceBeauty) {
    /*
     * 多模式属性
     * 属性名称|支持模式|默认模式|最早支持版本
     * 美白 colorIntensity|MODE1 MODE2|MODE2|MODE2 8.2.0;
     * 祛黑眼圈 removePouchIntensity|MODE1 MODE2|MODE2|MODE2 8.2.0;
     * 祛法令纹 removeLawPatternIntensity|MODE1 MODE1|MODE2|MODE2 8.2.0;
     * 窄脸程度 cheekNarrowIntensity|MODE1 MODE2|MODE2|MODE2 8.0.0;
     * 小脸程度 cheekSmallIntensity|MODE1 MODE2|MODE2|MODE2 8.0.0;
     * 大眼程度 eyeEnlargingIntensity|MODE1 MODE2 MODE3|MODE3|MODE2 8.0.0;MODE3 8.2.0;
     * 额头调整程度 forHeadIntensity|MODE1 MODE2|MODE2|MODE2 8.0.0;
     * 瘦鼻程度 noseIntensity|MODE1 MODE2|MODE2|MODE2 8.0.0;
     * 嘴巴调整程度 mouthIntensity|MODE1 MODE2 MODE3|MODE3|MODE2 8.0.0;MODE3 8.2.0;
     */
    faceBeauty.addPropertyMode(
        FUFaceBeautyMultiModePropertyEnum.REMOVE_POUCH_INTENSITY,
        FUFaceBeautyPropertyModeEnum.MODE2);
    faceBeauty.addPropertyMode(
        FUFaceBeautyMultiModePropertyEnum.REMOVE_NASOLABIAL_FOLDS_INTENSITY,
        FUFaceBeautyPropertyModeEnum.MODE2);
    faceBeauty.addPropertyMode(
        FUFaceBeautyMultiModePropertyEnum.EYE_ENLARGING_INTENSITY,
        FUFaceBeautyPropertyModeEnum.MODE3);
    faceBeauty.addPropertyMode(
        FUFaceBeautyMultiModePropertyEnum.MOUTH_INTENSITY, FUFaceBeautyPropertyModeEnum.MODE3);
  }

  /**
   * 初始化美肤参数
   *
   * @return ArrayList<FaceBeautyBean>
   */
  public static ArrayList<FaceBeautyBean> buildSkinParams() {
    ArrayList<FaceBeautyBean> params = new ArrayList<>();
    params.add(
        new FaceBeautyBean(
            FaceBeautyParam.BLUR_INTENSITY, R.string.beauty_box_heavy_blur_fine,
            R.drawable.icon_beauty_skin_buffing_close_selector,
                R.drawable.icon_beauty_skin_buffing_open_selector));
    params.add(
        new FaceBeautyBean(
            FaceBeautyParam.COLOR_INTENSITY, R.string.beauty_box_color_level,
            R.drawable.icon_beauty_skin_color_close_selector,
                R.drawable.icon_beauty_skin_color_open_selector));
    params.add(
        new FaceBeautyBean(
            FaceBeautyParam.RED_INTENSITY, R.string.beauty_box_red_level,
            R.drawable.icon_beauty_skin_red_close_selector,
                R.drawable.icon_beauty_skin_red_open_selector));
    params.add(
        new FaceBeautyBean(
            FaceBeautyParam.SHARPEN_INTENSITY, R.string.beauty_box_sharpen,
            R.drawable.icon_beauty_skin_sharpen_close_selector,
                R.drawable.icon_beauty_skin_sharpen_open_selector));
    params.add(
        new FaceBeautyBean(
            FaceBeautyParam.EYE_BRIGHT_INTENSITY, R.string.beauty_box_eye_bright,
            R.drawable.icon_beauty_skin_eyes_bright_close_selector,
                R.drawable.icon_beauty_skin_eyes_bright_open_selector));
    params.add(
        new FaceBeautyBean(
            FaceBeautyParam.TOOTH_WHITEN_INTENSITY, R.string.beauty_box_tooth_whiten,
            R.drawable.icon_beauty_skin_teeth_close_selector,
                R.drawable.icon_beauty_skin_teeth_open_selector));
    params.add(
        new FaceBeautyBean(
            FaceBeautyParam.REMOVE_POUCH_INTENSITY, R.string.beauty_micro_pouch,
            R.drawable.icon_beauty_skin_dark_circles_close_selector,
                R.drawable.icon_beauty_skin_dark_circles_open_selector));
    params.add(
        new FaceBeautyBean(
            FaceBeautyParam.REMOVE_NASOLABIAL_FOLDS_INTENSITY, R.string.beauty_micro_nasolabial,
            R.drawable.icon_beauty_skin_wrinkle_close_selector,
                R.drawable.icon_beauty_skin_wrinkle_open_selector));
    return params;
  }

  /**
   * 初始化美型参数
   *
   * @return ArrayList<FaceBeautyBean>
   */
  public static ArrayList<FaceBeautyBean> buildShapeParams() {
    ArrayList<FaceBeautyBean> params = new ArrayList<>();
    params.add(
        new FaceBeautyBean(
            FaceBeautyParam.CHEEK_THINNING_INTENSITY, R.string.beauty_box_cheek_thinning,
            R.drawable.icon_beauty_shape_face_cheekthin_close_selector,
                R.drawable.icon_beauty_shape_face_cheekthin_open_selector));
    params.add(
        new FaceBeautyBean(
            FaceBeautyParam.CHEEK_V_INTENSITY, R.string.beauty_box_cheek_v,
            R.drawable.icon_beauty_shape_face_v_close_selector,
                R.drawable.icon_beauty_shape_face_v_open_selector));
    params.add(
        new FaceBeautyBean(
            FaceBeautyParam.CHEEK_NARROW_INTENSITY, R.string.beauty_box_cheek_narrow,
            R.drawable.icon_beauty_shape_face_narrow_close_selector,
                R.drawable.icon_beauty_shape_face_narrow_open_selector));
    params.add(
        new FaceBeautyBean(
            FaceBeautyParam.CHEEK_SHORT_INTENSITY, R.string.beauty_box_cheek_short,
            R.drawable.icon_beauty_shape_face_short_close_selector,
                R.drawable.icon_beauty_shape_face_short_open_selector));
    params.add(
        new FaceBeautyBean(
            FaceBeautyParam.CHEEK_SMALL_INTENSITY, R.string.beauty_box_cheek_small,
            R.drawable.icon_beauty_shape_face_little_close_selector,
                R.drawable.icon_beauty_shape_face_little_open_selector));
    params.add(
        new FaceBeautyBean(
            FaceBeautyParam.INTENSITY_CHEEKBONES_INTENSITY, R.string.beauty_box_cheekbones,
            R.drawable.icon_beauty_shape_cheek_bones_close_selector,
                R.drawable.icon_beauty_shape_cheek_bones_open_selector));
    params.add(
        new FaceBeautyBean(
            FaceBeautyParam.INTENSITY_LOW_JAW_INTENSITY, R.string.beauty_box_lower_jaw,
            R.drawable.icon_beauty_shape_lower_jaw_close_selector,
                R.drawable.icon_beauty_shape_lower_jaw_open_selector));
    params.add(
        new FaceBeautyBean(
            FaceBeautyParam.EYE_ENLARGING_INTENSITY, R.string.beauty_box_eye_enlarge,
            R.drawable.icon_beauty_shape_enlarge_eye_close_selector,
                R.drawable.icon_beauty_shape_enlarge_eye_open_selector));
    params.add(
        new FaceBeautyBean(
            FaceBeautyParam.EYE_CIRCLE_INTENSITY, R.string.beauty_box_eye_circle,
            R.drawable.icon_beauty_shape_round_eye_close_selector,
                R.drawable.icon_beauty_shape_round_eye_open_selector));
    params.add(
        new FaceBeautyBean(
            FaceBeautyParam.CHIN_INTENSITY, R.string.beauty_box_intensity_chin,
            R.drawable.icon_beauty_shape_chin_close_selector,
                R.drawable.icon_beauty_shape_chin_open_selector));
    params.add(
        new FaceBeautyBean(
            FaceBeautyParam.FOREHEAD_INTENSITY, R.string.beauty_box_intensity_forehead,
            R.drawable.icon_beauty_shape_forehead_close_selector,
                R.drawable.icon_beauty_shape_forehead_open_selector));
    params.add(
        new FaceBeautyBean(
            FaceBeautyParam.NOSE_INTENSITY, R.string.beauty_box_intensity_nose,
            R.drawable.icon_beauty_shape_thin_nose_close_selector,
                R.drawable.icon_beauty_shape_thin_nose_open_selector));
    params.add(
        new FaceBeautyBean(
            FaceBeautyParam.MOUTH_INTENSITY, R.string.beauty_box_intensity_mouth,
            R.drawable.icon_beauty_shape_mouth_close_selector,
                R.drawable.icon_beauty_shape_mouth_open_selector));
    params.add(
        new FaceBeautyBean(
            FaceBeautyParam.CANTHUS_INTENSITY, R.string.beauty_micro_canthus,
            R.drawable.icon_beauty_shape_open_eyes_close_selector,
                R.drawable.icon_beauty_shape_open_eyes_open_selector));
    params.add(
        new FaceBeautyBean(
            FaceBeautyParam.EYE_SPACE_INTENSITY, R.string.beauty_micro_eye_space,
            R.drawable.icon_beauty_shape_distance_close_selector,
                R.drawable.icon_beauty_shape_distance_open_selector));
    params.add(
        new FaceBeautyBean(
            FaceBeautyParam.EYE_ROTATE_INTENSITY, R.string.beauty_micro_eye_rotate,
            R.drawable.icon_beauty_shape_angle_close_selector,
                R.drawable.icon_beauty_shape_angle_open_selector));
    params.add(
        new FaceBeautyBean(
            FaceBeautyParam.LONG_NOSE_INTENSITY, R.string.beauty_micro_long_nose,
            R.drawable.icon_beauty_shape_proboscis_close_selector,
                R.drawable.icon_beauty_shape_proboscis_open_selector));
    params.add(
        new FaceBeautyBean(
            FaceBeautyParam.PHILTRUM_INTENSITY, R.string.beauty_micro_philtrum,
            R.drawable.icon_beauty_shape_shrinking_close_selector,
                R.drawable.icon_beauty_shape_shrinking_open_selector));
    params.add(
        new FaceBeautyBean(
            FaceBeautyParam.SMILE_INTENSITY, R.string.beauty_micro_smile,
            R.drawable.icon_beauty_shape_smile_close_selector,
                R.drawable.icon_beauty_shape_smile_open_selector));
    params.add(
        new FaceBeautyBean(
            FaceBeautyParam.BROW_HEIGHT_INTENSITY, R.string.beauty_brow_height,
            R.drawable.icon_beauty_shape_brow_height_close_selector,
                R.drawable.icon_beauty_shape_brow_height_open_selector,
            R.string.brow_height_tips, FUConfig.DEVICE_LEVEL > FuDeviceUtils.DEVICE_LEVEL_MID));
    params.add(
        new FaceBeautyBean(
            FaceBeautyParam.BROW_SPACE_INTENSITY, R.string.beauty_brow_space,
            R.drawable.icon_beauty_shape_brow_space_close_selector,
                R.drawable.icon_beauty_shape_brow_space_open_selector,
            R.string.brow_space_tips, FUConfig.DEVICE_LEVEL > FuDeviceUtils.DEVICE_LEVEL_MID));
    return params;
  }

  /**
   * 初始化参数扩展列表
   *
   * @return HashMap<String, ModelAttributeData>
   */
  public static HashMap<String, ModelAttributeData> buildModelAttributeRange() {
    HashMap<String, ModelAttributeData> params = new HashMap<>();
    /*美肤*/
    params.put(FaceBeautyParam.COLOR_INTENSITY, new ModelAttributeData(0.3, 0.0, 0.0, 1.0));
    params.put(FaceBeautyParam.BLUR_INTENSITY, new ModelAttributeData(4.2, 0.0, 0.0, 6.0));
    params.put(FaceBeautyParam.RED_INTENSITY, new ModelAttributeData(0.3, 0.0, 0.0, 1.0));
    params.put(FaceBeautyParam.SHARPEN_INTENSITY, new ModelAttributeData(0.2, 0.0, 0.0, 1.0));
    params.put(FaceBeautyParam.EYE_BRIGHT_INTENSITY, new ModelAttributeData(0.0, 0.0, 0.0, 1.0));
    params.put(FaceBeautyParam.TOOTH_WHITEN_INTENSITY, new ModelAttributeData(0.0, 0.0, 0.0, 1.0));
    params.put(FaceBeautyParam.REMOVE_POUCH_INTENSITY, new ModelAttributeData(0.0, 0.0, 0.0, 1.0));
    params.put(
        FaceBeautyParam.REMOVE_NASOLABIAL_FOLDS_INTENSITY,
        new ModelAttributeData(0.0, 0.0, 0.0, 1.0));
    /*美型*/
    params.put(FaceBeautyParam.FACE_SHAPE_INTENSITY, new ModelAttributeData(1.0, 0.0, 0.0, 1.0));
    params.put(
        FaceBeautyParam.CHEEK_THINNING_INTENSITY, new ModelAttributeData(0.0, 0.0, 0.0, 1.0));
    params.put(FaceBeautyParam.CHEEK_LONG_INTENSITY, new ModelAttributeData(0.0, 0.0, 0.0, 1.0));
    params.put(FaceBeautyParam.CHEEK_CIRCLE_INTENSITY, new ModelAttributeData(0.0, 0.0, 0.0, 1.0));
    params.put(FaceBeautyParam.CHEEK_V_INTENSITY, new ModelAttributeData(0.5, 0.0, 0.0, 1.0));
    params.put(FaceBeautyParam.CHEEK_NARROW_INTENSITY, new ModelAttributeData(0.0, 0.0, 0.0, 1.0));
    params.put(FaceBeautyParam.CHEEK_SHORT_INTENSITY, new ModelAttributeData(0.0, 0.0, 0.0, 1.0));
    params.put(FaceBeautyParam.CHEEK_SMALL_INTENSITY, new ModelAttributeData(0.0, 0.0, 0.0, 1.0));
    params.put(
        FaceBeautyParam.INTENSITY_CHEEKBONES_INTENSITY, new ModelAttributeData(0.0, 0.0, 0.0, 1.0));
    params.put(
        FaceBeautyParam.INTENSITY_LOW_JAW_INTENSITY, new ModelAttributeData(0.0, 0.0, 0.0, 1.0));
    params.put(FaceBeautyParam.EYE_ENLARGING_INTENSITY, new ModelAttributeData(0.4, 0.0, 0.0, 1.0));
    params.put(FaceBeautyParam.EYE_CIRCLE_INTENSITY, new ModelAttributeData(0.0, 0.0, 0.0, 1.0));
    params.put(FaceBeautyParam.CHIN_INTENSITY, new ModelAttributeData(0.3, 0.5, 0.0, 1.0));
    params.put(FaceBeautyParam.FOREHEAD_INTENSITY, new ModelAttributeData(0.3, 0.5, 0.0, 1.0));
    params.put(FaceBeautyParam.NOSE_INTENSITY, new ModelAttributeData(0.5, 0.0, 0.0, 1.0));
    params.put(FaceBeautyParam.MOUTH_INTENSITY, new ModelAttributeData(0.4, 0.5, 0.0, 1.0));
    params.put(FaceBeautyParam.CANTHUS_INTENSITY, new ModelAttributeData(0.0, 0.0, 0.0, 1.0));
    params.put(FaceBeautyParam.EYE_SPACE_INTENSITY, new ModelAttributeData(0.5, 0.5, 0.0, 1.0));
    params.put(FaceBeautyParam.EYE_ROTATE_INTENSITY, new ModelAttributeData(0.5, 0.5, 0.0, 1.0));
    params.put(FaceBeautyParam.LONG_NOSE_INTENSITY, new ModelAttributeData(0.5, 0.5, 0.0, 1.0));
    params.put(FaceBeautyParam.PHILTRUM_INTENSITY, new ModelAttributeData(0.5, 0.5, 0.0, 1.0));
    params.put(FaceBeautyParam.SMILE_INTENSITY, new ModelAttributeData(0.0, 0.0, 0.0, 1.0));
    params.put(FaceBeautyParam.BROW_HEIGHT_INTENSITY, new ModelAttributeData(0.5, 0.5, 0.0, 1.0));
    params.put(FaceBeautyParam.BROW_SPACE_INTENSITY, new ModelAttributeData(0.5, 0.5, 0.0, 1.0));
    return params;
  }

  /**
   * 初始化滤镜参数
   *
   * @return ArrayList<FaceBeautyFilterBean>
   */
  public static ArrayList<FaceBeautyFilterBean> buildFilters() {
    ArrayList<FaceBeautyFilterBean> filters = new ArrayList<>();
    filters.add(
        new FaceBeautyFilterBean(
            FaceBeautyFilterEnum.ORIGIN, R.mipmap.icon_beauty_filter_cancel, R.string.origin, 0.0));
    filters.add(
        new FaceBeautyFilterBean(
            FaceBeautyFilterEnum.ZIRAN_1,
            R.mipmap.icon_beauty_filter_natural_1,
            R.string.ziran_1,
            getFilterIntensity(FaceBeautyFilterEnum.ZIRAN_1, 0.4f)));
    filters.add(
        new FaceBeautyFilterBean(
            FaceBeautyFilterEnum.ZIRAN_2,
            R.mipmap.icon_beauty_filter_natural_2,
            R.string.ziran_2,
            getFilterIntensity(FaceBeautyFilterEnum.ZIRAN_2, 0.4f)));
    filters.add(
        new FaceBeautyFilterBean(
            FaceBeautyFilterEnum.ZHIGANHUI_1,
            R.mipmap.icon_beauty_filter_texture_gray_1,
            R.string.zhiganhui_1,
            getFilterIntensity(FaceBeautyFilterEnum.ZHIGANHUI_1, 0.4f)));
    filters.add(
        new FaceBeautyFilterBean(
            FaceBeautyFilterEnum.ZHIGANHUI_2,
            R.mipmap.icon_beauty_filter_texture_gray_2,
            R.string.zhiganhui_2,
            getFilterIntensity(FaceBeautyFilterEnum.ZHIGANHUI_2, 0.4f)));
    filters.add(
        new FaceBeautyFilterBean(
            FaceBeautyFilterEnum.BAILIANG_1,
            R.mipmap.icon_beauty_filter_bailiang_1,
            R.string.bailiang_1,
            getFilterIntensity(FaceBeautyFilterEnum.BAILIANG_1, 0.4f)));
    filters.add(
        new FaceBeautyFilterBean(
            FaceBeautyFilterEnum.BAILIANG_2,
            R.mipmap.icon_beauty_filter_bailiang_2,
            R.string.bailiang_2,
            getFilterIntensity(FaceBeautyFilterEnum.BAILIANG_2, 0.4f)));
    filters.add(
        new FaceBeautyFilterBean(
            FaceBeautyFilterEnum.FENNEN_1,
            R.mipmap.icon_beauty_filter_fennen_1,
            R.string.fennen_1,
            getFilterIntensity(FaceBeautyFilterEnum.FENNEN_1, 0.4f)));
    filters.add(
        new FaceBeautyFilterBean(
            FaceBeautyFilterEnum.FENNEN_2,
            R.mipmap.icon_beauty_filter_fennen_2,
            R.string.fennen_2,
            getFilterIntensity(FaceBeautyFilterEnum.FENNEN_2, 0.4f)));
    filters.add(
        new FaceBeautyFilterBean(
            FaceBeautyFilterEnum.LENGSEDIAO_1,
            R.mipmap.icon_beauty_filter_lengsediao_1,
            R.string.lengsediao_1,
            getFilterIntensity(FaceBeautyFilterEnum.LENGSEDIAO_1, 0.4f)));
    filters.add(
        new FaceBeautyFilterBean(
            FaceBeautyFilterEnum.LENGSEDIAO_2,
            R.mipmap.icon_beauty_filter_lengsediao_2,
            R.string.lengsediao_2,
            getFilterIntensity(FaceBeautyFilterEnum.LENGSEDIAO_2, 0.4f)));
    return filters;
  }

  public static void saveParamIntensity(String key, float value) {
    SPUtils.getInstance().put(key, value);
  }

  public static float getParamIntensity(String key, float defaultValue) {
    return SPUtils.getInstance().getFloat(key, (float) defaultValue);
  }

  public static void saveFilterIntensity(String name, float intensity) {
    SPUtils.getInstance().put(name, intensity);
  }

  public static float getFilterIntensity(String name, float defaultValue) {
    return SPUtils.getInstance().getFloat(name, defaultValue);
  }

  public static void saveCurrentFilterName(String name) {
    SPUtils.getInstance().put(SP_KEY_CURRENT_FILTER_NAME, name);
  }

  public static String getCurrentFilterName() {
    return SPUtils.getInstance()
        .getString(SP_KEY_CURRENT_FILTER_NAME, FaceBeautyFilterEnum.ZIRAN_1);
  }
}
