<!-- keywords: Sample Code, 示例项目源码, 1V1, 1对1娱乐社交-->
网易云信为您提供 1 对 1 娱乐社交场景的开源示例项目，您可以参考本文档快速跑通示例项目，体验 1 对 1 娱乐社交场景的最佳效果。

## 运行环境要求

在开始运行示例项目之前，请确保运行环境满足以下要求：



| 环境要求  | 说明                                      |
| --------- | ------------------------------------------- |
| iOS 版本  | 11.0 及以上的 iPhone 或者 iPad 真机         |
| CPU 架构 | ARM64、ARMV7                               |
| IDE       | XCode                                       |
| 其他    | 安装 CocoaPods。  |


## <span id="前提条件">前提条件</span>
请确认您已完成以下操作：

- [已创建应用并获取 App Key](https://doc.yunxin.163.com/docs/jcyOTA0ODM/jE3OTc5NTY?platformId=50002)
- [已开通相关能力](https://doc.yunxin.163.com/1v1-social/docs/zk3NTk1NzU?platform=iOS)
- [已配置RTC 和 IM 的消息抄送地址（http://yiyong.netease.im/nemo/socialChat/nim/notify）](https://doc.yunxin.163.com/nertc/docs/DExNjg2MDc?platform=server)

## 注意事项

示例项目需要在 **RTC 调试模式**下使用，此时无需传入 Token。修改鉴权方式的方法请参见 <a href="https://doc.yunxin.163.com/nertc/docs/TQ0MTI2ODQ?platform=android" target="_blank">Token 鉴权</a> 。

您可以在集成开发阶段使用调试模式进行应用开发与测试。但是出于安全考虑，应用正式上线前，请在控制台中将指定应用的鉴权方式改回安全模式。


## <span id="快速跑通 Sample Code">运行示例项目</span>

> 注意：
> - 1 对 1 娱乐社交的示例源码仅供开发者接入参考，实际应用开发场景中，请结合具体业务需求修改使用。
> - 若您计划将源码用于生产环境，请确保应用正式上线前已经过全面测试，以免因兼容性等问题造成损失。
> - 以下源码跑通无须部署服务端即可体验，请按照以下步骤设置客户端源码配置。


1. 克隆<a href="https://github.com/netease-kit/1V1" target="_blank">示例项目源码</a>仓库至您本地工程。


2. 打开终端，在 `Podfile` 所在文件夹中执行如下命令进行安装：
    ```
    pod install
    ```
3. 完成安装后，通过 Xcode 打开 `xxx.xcworkspace` 工程。

4. 在 `OneOnOneSample/OneOnOneSample/AppKey.swift ` 中，替换您自己的 App Key 和 App Secret 。获取 App Key 和 App Secret 的方法请参见获取 App Key <a href="https://doc.yunxin.163.com/docs/jcyOTA0ODM/jcwMDQ2MTg?platformId=50192#获取 App Key" target="_blank">获取 App Key</a>。并填写云信 IM 账号相关信息。

    ```
    // MARK: 请填写您的AppKey和AppSecret
    let APP_KEY: String = "your appKey" // 请填写应用对应的AppKey，可在云信控制台的”AppKey管理“页面获取
    let APP_SECRET: String = "your appSecret" // 请填写应用对应的AppSecret，可在云信控制台的”AppKey管理“页面获取

    // MARK: 如果您的AppKey为海外，填ture；如果您的AppKey为中国国内，填false
    let IS_OVERSEA = false

    // MARK: 默认的BASE_URL地址仅用于跑通体验Demo，请勿用于正式产品上线。在产品上线前，请换为您自己实际的服务端地址
    let BASE_URL: String = "https://yiyong.netease.im"  //云信派对服务端国内的体验地址
    let BASE_URL_OVERSEA: String = "http://yiyong-sg.netease.im"   //云信派对服务端海外的体验地址

    ```
    > 注意：
    > - 获取 AppKey 和 AppSecret 的方法请参见<a href="https://doc.yunxin.163.com/docs/jcyOTA0ODM/jcwMDQ2MTg?platformId=50192#获取 App Key" target="_blank">获取 App Key</a>。
    > - 配置文件中的 kApiHost 地址 `http://yiyong.netease.im`为云信派对服务端体验地址，该地址仅用于体验 Demo，请勿用于生产环境。 您可以使用云信派对 Demo 体验 1 小时音视频通话。
    > - 如果您应用的 AppKey 为海外，`IS_OVERSEA` 的值请设置为 `ture`。
  
5. 运行工程。

    > 注意：
    > - 请使用两个手机运行工程，Demo 上才能显示用户列表，体验 1 对 1 聊天等功能。
    > - 示例项目源码不包含虚拟主播，云信派对 Demo 中的虚拟主播是为了方便您快速体验相关功能而设计，不作为实际项目交付。


## 示例项目结构
```
OneOnOne                # 1V1 文件夹
   ├── OneOnOneSample  # 1V1 示例工程文件夹
   │   └── OneOnOneSample            # 主工程文件夹
   │       ├── Category            # 主工程入口配置
   │       └──AppKey            # 主工程项目配置
   │   
   ├── OneOnOneKit # 1V1 基于NERTCCallkit的封装
   │   ├── Engine # 引擎文件夹
   │   │   ├── NEOneOnOne # 单例对象
   │   │   ├── NEOneOnOne+Auth # 单例对象拓展登录接口相关
   │   │   ├── NEOneOnOneKit+Rtc # 单例对象拓展RTC相关接口
   │   │   ├── NEOneOnOneKit+Room # 单例对象拓展房间相关接口
   │   │   └── NEOneOnOneKit+Message # 单例对象拓展消息相关接口
   │   ├── Common # 内部通用工具文件夹
   │   │   ├── NEOneOnOneKitConfig # 配置对象
   │   │   ├── NEOneOnOneErrorCode # 错误码
   │   │   ├── NEOneOnOneCommon # 枚举定义
   │   │   └── NEOneOnOneCallback # 回调定义
   │   ├── Public  # 公开文件夹
   │   │   ├── NEOneOnOneListener # 监听对象
   │   │   └── NEOneOnOneAuthListener # 登录监听对象
   │   ├── Log  # 日志文件夹
   │   │   └── NEOneOnOneLog # 日志处理
   │   └──Private #私有文件夹
   │       ├── NEOneOnOnePrivateModels # 模型定义
   │       ├── Judge # 通用前置处理
   │       ├── NSPointerArray+Extension # 数组分类
   │       ├── NEOneOnOneRoomService # 房间服务
   │       ├── NEOneOnOneMessageService # 消息服务
   │       ├── NEOneOnOneDecoder # 解码文件
   │       ├── NEOneOnOne+Codable # 编码文件
   │       └── NEOneOnOneNetwork # 网络处理
   ├── OneOnOneUIKit # 1V1 UI层处理
   │        ├── ViewModel # 模型定义
   │        │    └── NEOneOnOneRoomListViewModel # 列表数据viewmodel
   │        ├── NEOneOnOneUIManager # UI层管理入口
   │        ├── Macro # 模型定义
   │        │    └── NEOneOnOneUIKitMacro # 宏定义
   │        ├── Engine # 模型定义
   │        │    └── NEOneOnOneUIKitEngine # UI层引擎
   │        ├── Utils # 工具类
   │        ├── ViewController # 模型定义
   │        NEOneOnOneCallViewController
   │        │    ├── NEOneOnOneCallViewController # 1V1 主视图控制
   │        │    └── NEOneOnOneRoomListViewController # 列表视图控制器
   │        └── View # 视图层
   │             ├── NEOneOnOneBottomPresentView # 底部弹出视图
   │             ├── NEOneOnOneUserBusyView # 用户忙碌视图
   │             ├── NEOneOnOneVideoConnectedView # 视频通话连接后视图
   │             ├── NEOneOnOneVideoButtomView # 视频通话底部控制视图
   │             ├── NEOneOnOneUILiveListCell # 列表Cell
   │             ├── NEOneOnOneEmptyListView # 列表空数据视图
   │             ├── NEOneOnOneConnectingView # 呼叫中视图/音频通话中视图
   │             ├── NEOneOnOneAudioButtomView # 音频通话底部控制视图
   │             └── NEOneOnOneCustomButtonView # 自定义按钮视图
   │             └── OneOnOneUIKit # 1V1 UI层处理
   │
   └── OneOnOneChatUIKit # 1V1 消息系统
           ├── Engine # 引擎定义
           │    ├── NEOneOnOneChatRegisterEngine # 1V1 消息系统 单例对象以及路由跳转控制器
           │    └── NEOneOnOneCustomAttachment # 1V1 消息系统 自定义对象解析器
           ├── View # 视图定义
           │    ├── NEOneOnOneCustomMsgView # 1V1 消息系统 自定义消息页面View
           │    ├── NEOneOnOneNavigationItemView # 1V1 消息系统 自定义导航中心视图UI
           │    ├── NEOneOnOneAudioInputingView # 1V1 消息系统 自定义音频输入页面
           │    ├── NEOneOnOnePushSettingView # 1V1 消息系统 自定义消息通知UI
           │    ├── NEOneOnOneChatHotTopicsView # 1V1 消息系统 热聊话题UI
           │    ├── NEOneOnOneCustomChatCell # 1V1 消息系统 自定义消息Cell ：对于自定义UIView NEOneOnOneCustomMsgView 封装一层
           │    ├── NEOneOnOneConversionCell # 1V1 消息系统 自定义消息列表Cell
           │    ├── NEOneOnOneBottomCustomView # 1V1 消息系统 自定义底部视图
           │    ├── NEOneOnOneReportView # 1V1 消息系统 自定义举报视图
           │    └── NEOneOnOneHeaderView # 1V1 消息系统 自定义导航栏
           ├── Controller # 控制器定义
           │    ├── NEOneOnOneAlertViewController # 1V1 消息系统 警告框
           │    ├── NEOneOnOneChatP2PViewController # 1V1 消息系统 聊天详情页
           │    └── NEOneOnOneConversationsViewController # 1V1 消息系统 聊天列表页
           └── Utils # 工具定义
                ├── NEOneOnOneChatUtils # 1V1 消息系统 文本，图片加载方法定义
                ├── NEOneOnOneChatMacro # 1V1 消息系统 宏定义
                └── NEOneOnOneMessageUtil # 1V1 消息系统 消息内容处理方法
```






