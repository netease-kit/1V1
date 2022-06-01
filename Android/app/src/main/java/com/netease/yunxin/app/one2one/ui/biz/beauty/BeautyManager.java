package com.netease.yunxin.app.one2one.ui.biz.beauty;

import android.content.Context;
import android.text.TextUtils;

import com.blankj.utilcode.util.SPUtils;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.netease.lava.nertc.sdk.NERtcEx;
import com.netease.yunxin.app.one2one.ui.biz.beauty.module.NEAssetsEnum;
import com.netease.yunxin.app.one2one.ui.biz.beauty.module.NEBodyEffectEnum;
import com.netease.yunxin.app.one2one.ui.biz.beauty.module.NEEffect;
import com.netease.yunxin.app.one2one.ui.biz.beauty.module.NEEffectEnum;
import com.netease.yunxin.app.one2one.ui.biz.beauty.module.NEFilter;
import com.netease.yunxin.app.one2one.ui.biz.beauty.module.NEFilterEnum;
import java.io.File;
import java.io.Serializable;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Locale;

public class BeautyManager {
    private final static String SP_EFFECTS = "effects";
    private final static String SP_BODY_EFFECTS = "body_effects";
    private final static String SP_SELECTED_FILTER = "selected_filter";
    private HashMap<Integer, NEEffect> defaultEffects;
    private HashMap<Integer, NEEffect> localEffects;
    private HashMap<Integer, NEEffect> defaultBodyEffects;
    private HashMap<Integer, NEEffect> localBodyEffects;
    private HashMap<Integer, NEFilter> defaultFilters;
    private BeautyFilter selectedFilter;
    private String extFilesDirPath;
    private static volatile BeautyManager mInstance;

    private BeautyManager(){
    }

    public static BeautyManager getInstance() {
        if(null == mInstance) {
            synchronized (BeautyManager.class) {
                if (mInstance == null) {
                    mInstance = new BeautyManager();
                }
            }
        }
        return mInstance;
    }

    public void init(Context context){
        extFilesDirPath = context.getExternalFilesDir(null).getAbsolutePath();
        defaultEffects = NEEffectEnum.getEffects();
        localEffects = NEEffectEnum.getEffects();
        defaultBodyEffects = NEBodyEffectEnum.getEffects();
        localBodyEffects = NEBodyEffectEnum.getEffects();
        defaultFilters = NEFilterEnum.getFilters();

        loadDataFromSP();
    }
    private void loadDataFromSP() {
        String effectJsonString = SPUtils.getInstance().getString(SP_EFFECTS, "");
        if(!TextUtils.isEmpty(effectJsonString)) {
            Type type = new TypeToken<HashMap<Integer, NEEffect>>() {}.getType();
            Gson gson = new Gson();
            HashMap<Integer, NEEffect> map = gson.fromJson(effectJsonString, type);
            localEffects.putAll(map);
        }

        String bodyEffectJsonString = SPUtils.getInstance().getString(SP_BODY_EFFECTS, "");
        if(!TextUtils.isEmpty(bodyEffectJsonString)) {
            Type type = new TypeToken<HashMap<Integer, NEEffect>>() {}.getType();
            Gson gson = new Gson();
            HashMap<Integer, NEEffect> map = gson.fromJson(bodyEffectJsonString, type);
            localBodyEffects.putAll(map);
        }

        String filterJsonString = SPUtils.getInstance().getString(SP_SELECTED_FILTER, "");
        if(!TextUtils.isEmpty(filterJsonString)) {
            Gson gson = new Gson();
            selectedFilter = gson.fromJson(filterJsonString, BeautyFilter.class);
        }
    }

    public void saveEffectData(int resId, float level){
        NEEffect effect = localEffects.get(resId);
        if(effect != null){
            effect.setLevel(level);
        }
        Gson gson = new Gson();
        String json = gson.toJson(localEffects);
        SPUtils.getInstance().put(SP_EFFECTS, json);
    }

    public void saveBodyEffectData(int resId, float level){
        NEEffect effect = localBodyEffects.get(resId);
        if(effect != null){
            effect.setLevel(level);
        }
        Gson gson = new Gson();
        String json = gson.toJson(localBodyEffects);
        SPUtils.getInstance().put(SP_BODY_EFFECTS, json);
    }

    public void saveFilterData(BeautyFilter beautyFilter){
        selectedFilter = beautyFilter;
        if(selectedFilter != null) {
            Gson gson = new Gson();
            String json = gson.toJson(selectedFilter);
            SPUtils.getInstance().put(SP_SELECTED_FILTER, json);
        }else{
            SPUtils.getInstance().put(SP_SELECTED_FILTER, "");
        }
    }

    public void setBeautyEffect(int resId, float level){
        NEEffect effect = defaultEffects.get(resId);
        if(effect != null) {
            NERtcEx.getInstance().setBeautyEffect(effect.getType(), level);
        }
    }

    public void setBeautyBodyEffect(int resId, float level){
        NEEffect effect = defaultBodyEffects.get(resId);
        if(effect != null) {
            NERtcEx.getInstance().setBeautyEffect(effect.getType(), level);
        }
    }

    public void addBeautyFilter(int resId, float level){
        NEFilter filter = defaultFilters.get(resId);
        if(filter != null) {
            NERtcEx.getInstance().addBeautyFilter(getBeautyAssetPath(NEAssetsEnum.FILTERS, filter.getName()));
            NERtcEx.getInstance().setBeautyFilterLevel(level);
        }
    }

    public HashMap<Integer, NEEffect> getLocalEffects(){
        return localEffects;
    }

    public HashMap<Integer, NEEffect> getDefaultEffects(){
        return defaultEffects;
    }

    public HashMap<Integer, NEEffect> getLocalBodyEffects(){
        return localBodyEffects;
    }

    public HashMap<Integer, NEEffect> getDefaultBodyEffects(){
        return defaultBodyEffects;
    }

    public HashMap<Integer, NEFilter> getDefaultFilters(){
        return defaultFilters;
    }

    public void resetEffect(){
        for (NEEffect effect : defaultEffects.values()) {
            NERtcEx.getInstance().setBeautyEffect(effect.getType(), effect.getLevel());
        }
    }

    public void resetBodyEffect(){
        for (NEEffect effect : defaultBodyEffects.values()) {
            NERtcEx.getInstance().setBeautyEffect(effect.getType(), effect.getLevel());
        }
    }

    public void resetFilter(){
        selectedFilter = null;
        NERtcEx.getInstance().removeBeautyFilter();
    }

    public void startBeauty(){
        NERtcEx.getInstance().startBeauty();
        for (NEEffect effect : localEffects.values()) {
            NERtcEx.getInstance().setBeautyEffect(effect.getType(), effect.getLevel());
        }

        for (NEEffect effect : localBodyEffects.values()) {
            NERtcEx.getInstance().setBeautyEffect(effect.getType(), effect.getLevel());
        }

        if(selectedFilter != null) {
            NERtcEx.getInstance().addBeautyFilter(getBeautyAssetPath(NEAssetsEnum.FILTERS, defaultFilters.get(selectedFilter.resId).getName()));
            NERtcEx.getInstance().setBeautyFilterLevel(selectedFilter.level);
        }
    }

    public BeautyFilter getSelectedFilter() {
        return selectedFilter;
    }
    public void stopBeauty(){
        NERtcEx.getInstance().stopBeauty();
    }

    /**
     * 生成滤镜和美妆模板资源文件的路径，资源文件在App启动后会拷贝到的App的外部存储路径
     * @param type @see NEAssetsEnum
     * @param name 滤镜或者美妆的名称，对应assets下的资源文件名
     * @return 滤镜或者美妆的App外部存储路径
     */
    private String getBeautyAssetPath(NEAssetsEnum type, String name) {
        String separator = File.separator;
        return String.format(Locale.getDefault(), "%s%s%s%s%s", extFilesDirPath, separator, type.getAssetsPath(), separator, name);
    }

    public static class BeautyFilter implements Serializable {
        public int resId;
        public float level;

        public BeautyFilter(int resId, float level) {
            this.resId = resId;
            this.level = level;
        }
    }
}
