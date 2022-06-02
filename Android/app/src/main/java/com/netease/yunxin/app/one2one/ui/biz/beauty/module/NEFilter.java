/*
 *
 *  * Copyright (c) 2022 NetEase, Inc.  All rights reserved.
 *  * Use of this source code is governed by a MIT license that can be found in the LICENSE file.
 *
 */

package com.netease.yunxin.app.one2one.ui.biz.beauty.module;

public class NEFilter {
    private int resId;
    private String name;
    private float level;

    public NEFilter(int resId, String name,float level) {
        this.resId = resId;
        this.name = name;
        this.level = level;
    }

    public int getResId() {
        return resId;
    }

    public void setResId(int resId) {
        this.resId = resId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public float getLevel() {
        return level;
    }

    public void setLevel(float level) {
        this.level = level;
    }

    @Override
    public String toString() {
        return "NEFilter{" +
                "resId=" + resId +
                ", name='" + name + '\'' +
                ", level=" + level +
                '}';
    }
}
