/*
 *
 *  * Copyright (c) 2022 NetEase, Inc.  All rights reserved.
 *  * Use of this source code is governed by a MIT license that can be found in the LICENSE file.
 *
 */

package com.netease.yunxin.app.one2one.constant;

import com.netease.yunxin.app.one2one.model.HomeItemModel;

import java.util.Arrays;
import java.util.List;

public class MockDatas {
    public static final List<HomeItemModel> MOCK_USERS = Arrays.asList(
            new HomeItemModel("https://yx-web-nosdn.netease.im/quickhtml%2Fassets%2Fyunxin%2Fdefault%2Favatars%2Favatar1.png", "小花花", 22),
            new HomeItemModel("https://yx-web-nosdn.netease.im/quickhtml%2Fassets%2Fyunxin%2Fdefault%2Favatars%2Favatar2.png", "茉莉花", 22),
            new HomeItemModel("https://yx-web-nosdn.netease.im/quickhtml%2Fassets%2Fyunxin%2Fdefault%2Favatars%2Favatar3.png", "向日葵", 22),
            new HomeItemModel("https://yx-web-nosdn.netease.im/quickhtml%2Fassets%2Fyunxin%2Fdefault%2Favatars%2Favatar4.png", "小辣椒", 22),
            new HomeItemModel("https://yx-web-nosdn.netease.im/quickhtml%2Fassets%2Fyunxin%2Fdefault%2Favatars%2Favatar5.png", "小脑虎", 22),
            new HomeItemModel("https://yx-web-nosdn.netease.im/quickhtml%2Fassets%2Fyunxin%2Fdefault%2Favatars%2Favatar6.png", "蜡笔小新", 22),
            new HomeItemModel("https://yx-web-nosdn.netease.im/quickhtml%2Fassets%2Fyunxin%2Fdefault%2Favatars%2Favatar7.png", "甜妹", 22),
            new HomeItemModel("https://yx-web-nosdn.netease.im/quickhtml%2Fassets%2Fyunxin%2Fdefault%2Favatars%2Favatar8.png", "午后咖啡馆", 25),
            new HomeItemModel("https://yx-web-nosdn.netease.im/quickhtml%2Fassets%2Fyunxin%2Fdefault%2Favatars%2Favatar9.png", "温柔港湾", 23),
            new HomeItemModel("https://yx-web-nosdn.netease.im/quickhtml%2Fassets%2Fyunxin%2Fdefault%2Favatars%2Favatar10.png", "拾柒Iris", 20)
    );

    public static final List<String> ALBUMS = Arrays.asList(
            "https://yx-web-nosdn.netease.im/quickhtml%2Fassets%2Fyunxin%2Fdefault%2Fphotos%2Fphoto1.png",
            "https://yx-web-nosdn.netease.im/quickhtml%2Fassets%2Fyunxin%2Fdefault%2Fphotos%2Fphoto2.png",
            "https://yx-web-nosdn.netease.im/quickhtml%2Fassets%2Fyunxin%2Fdefault%2Fphotos%2Fphoto3.png",
            "https://yx-web-nosdn.netease.im/quickhtml%2Fassets%2Fyunxin%2Fdefault%2Fphotos%2Fphoto4.png"
    );
}
