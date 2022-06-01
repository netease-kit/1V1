/*
 *
 *  * Copyright (c) 2022 NetEase, Inc.  All rights reserved.
 *  * Use of this source code is governed by a MIT license that can be found in the LICENSE file.
 *
 */

package com.netease.yunxin.app.one2one.model;

import java.util.ArrayList;

public class UserInfoDetailModel {
    public String bgUrl;
    public String nickname;
    public String desc;
    public ArrayList<String> albums;
    public ArrayList<Integer> gifts;
    public String age;
    public String height;
    public String weight;
    public ArrayList<String> hobbys;

    @Override
    public String toString() {
        return "UserInfoDetailModel{" +
                "bgUrl='" + bgUrl + '\'' +
                ", nickname='" + nickname + '\'' +
                ", desc='" + desc + '\'' +
                ", albums=" + albums +
                ", gifts=" + gifts +
                ", age='" + age + '\'' +
                ", height='" + height + '\'' +
                ", weight='" + weight + '\'' +
                ", hobbys=" + hobbys +
                '}';
    }



}
