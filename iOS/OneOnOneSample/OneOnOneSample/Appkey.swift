// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

import Foundation

// MARK: 请填写您的AppKey和AppSecret
let APP_KEY: String = "your appKey" // 填入您的AppKey,可在云信控制台AppKey管理处获取
let APP_SECRET: String = "your appSecret" // 填入您的AppSecret,可在云信控制台AppKey管理处获取

// MARK: 海外用户填ture，国内用户填false
// 如果为 true ，同步修改 BASE_URL 为 "https://yiyong-sg.netease.im"
// 如果为 false ，同步修改 BASE_URL 为 "https://yiyong.netease.im"
let IS_OVERSEA = false

// MARK: BASE_URL为服务端地址,请在跑通Server Demo(https://github.com/netease-kit/nemo)后，替换为您自己实际的服务端地址 "http://yiyong.netease.im/"与"http://yiyong-sg.netease.im"仅用于跑通体验Demo,请勿用于正式产品上线
let BASE_URL: String = "https://yiyong.netease.im"

let kPushCerName: String = "push notification name" //推送证书名称

let kAppAMapKey: String = "AMap key" //高德地图Key

// 获取userUuid和对应的userToken，请参考https://doc.yunxin.163.com/neroom/docs/TY1NzM5MjQ?platform=server

// MARK: 云信IM账号（userUuid），用户Token（userToken）和 云信IM账号token（imToken） 默认为空，如果未填写或者只填写了个别数据， 则自动生成一个账号。如果填写完整则会使用填写的账号。

// 云信IM账号，说明：账号信息为空，则默认自动生成一个账号
var userUuid: String = ""
// 用户Token，说明：账号信息为空，则默认自动生成一个账号
var userToken: String = ""
// 云信IM账号 token，说明：账号信息为空，则默认自动生成一个账号
var imToken: String = ""

// MARK: 以下内容选填

// 用户名
var userName: String = ""
// 头像
var icon: String = ""
