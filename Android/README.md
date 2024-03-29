<!-- keywords: Sample Code, 示例项目源码, 1V1, 1对1娱乐社交-->
网易云信为您提供 1 对 1 娱乐社交场景的开源示例项目，您可以参考本文档快速跑通示例项目，体验 1 对 1 娱乐社交场景的最佳效果。

## 开发环境要求

在开始运行示例项目之前，请确保开发环境满足以下要求：

| 环境要求         | 说明                                                         |
| :---------------- | :------------------------------------------------------------ |
| Android Studio 版本 | Android Studio 5.0 及以上版本  <note type="note">Android Studio 版本编号系统的变更请参考 [Android Studio 版本说明](https://developer.android.google.cn/studio/releases/index.html)。</note>                              |
| Android API 版本 | Level 为 21 及以上版本。                              |
| Android SDK 版本     | Android SDK 31、Android SDK Platform-Tools 31.x.x 及以上版本。                   |
| Gradle 及所需的依赖库| 在 [Gradle Services](https://services.gradle.org/distributions/) 页面下载对应版本的 Gradle 及所需的依赖库。<ul> <li>Gradle 版本：7.4.1<li>Android Gradle 插件版本: 7.1.3<br>关于 Android Gradle 插件、Gradle、SDK Tool 之间的版本依赖关系，请参见 [Android Gradle 插件版本说明](https://developer.android.com/studio/releases/gradle-plugin)。|
| CPU架构          | ARM 64、ARMV7                                                 |
| IDE              | Android Studio                                               |
| 其他             | 依赖 Androidx，不支持 support 库。<br>Android 系统 5.0  及以上版本的真机。<note type="note">由于模拟器缺少摄像头及麦克风能力，因此工程需要在真机运行。</note> |


## <span id="前提条件">前提条件</span>
请确认您已完成以下操作：

- [已创建应用并获取 App Key](https://doc.yunxin.163.com/docs/jcyOTA0ODM/jE3OTc5NTY?platformId=50002)
- [已开通IM 即时通讯、信令和音视频通话2.0](https://doc.yunxin.163.com/docs/DgyMDc0NTA/DA3NzUzNzY)
 ![开通IM和RTC.png](https://yx-web-nosdn.netease.im/common/3191fccdcab4cb7f39f647c261f4fa96/开通IM和RTC.png)
![开通信令.png](https://yx-web-nosdn.netease.im/common/3f3de9246c62c15432fb5b2cb6f3b65b/开通信令.png)
- [已配置RTC 和 IM 的消息抄送地址（抄送地址请填写`http://yiyong.netease.im/nemo/socialChat/nim/notify`）](https://doc.yunxin.163.com/nertc/docs/DExNjg2MDc?platform=server)
    ![消息抄送.png](https://yx-web-nosdn.netease.im/common/f972950448a9aab5e4707ab363082b5b/消息抄送.png)

## <span id="快速跑通 Sample Code">运行示例项目</span>

> 注意：
>- 1 对 1 娱乐社交的示例源码仅供开发者接入参考，实际应用开发场景中，请结合具体业务需求修改使用。
>- 若您计划将源码用于生产环境，请确保应用正式上线前已经过全面测试，以免因兼容性等问题造成损失。
>- 以下源码跑通无须部署服务端即可体验，请按照以下步骤设置客户端源码配置。
 



1. 克隆 [示例项目源码](https://github.com/netease-kit/1V1) 仓库至您本地工程。

2. 通过 Android Studio 打开项目。

   在菜单栏中选择 **File** > **Open**，选择示例项目源码仓库（`1V1/Android`）所在目录。

3. 在 `/app/java/com.netease.yunxin.app.oneonone/config/AppConfig.java` 文件中配置应用的 AppKey 和 AppSecret。


    ```  
    private static final String APP_KEY = "your AppKey"; // 请填写应用对应的AppKey，可在云信控制台的”AppKey管理“页面获取
    public static final String APP_SECRET = "your AppSecret"; // 请填写应用对应的AppSecret，可在云信控制台的”AppKey管理“页面获取
    public static final boolean IS_OVERSEA = false; // 如果您的AppKey为海外，填ture；如果您的AppKey为中国国内，填false
    /**
     * 默认的BASE_URL地址仅用于跑通体验Demo，请勿用于正式产品上线。在产品上线前，请换为您自己实际的服务端地址
    */
    public static final String BASE_URL = "https://yiyong.netease.im/";   //云信派对服务端国内的体验地址
    public static final String BASE_URL_OVERSEA = "https://yiyong-sg.netease.im/";   //云信派对服务端海外的体验地址
    ```

    > 注意：
    > - 获取 AppKey 和 AppSecret 的方法请参见<a href="https://doc.yunxin.163.com/console/docs/TIzMDE4NTA?platform=console#获取-appkey" target="_blank">创建应用并获取 AppKey</a>。
    > - BASE_URL 地址 `http://yiyong.netease.im`为云信派对服务端体验地址，该地址仅用于体验 Demo，请勿用于生产环境。您可以使用云信派对 Demo 体验 1 小时音视频通话。
    > - 如果您的 AppKey 为海外，IS_OVERSEA 请设置为 true。
    
      ![配置AppKey.png](https://yx-web-nosdn.netease.im/common/4fa23d7115b8cde79cc6204d24f7a7e1/配置AppKey.png)

4. 在 Android Studio 中，单击 **Sync Project with Gradle Files** 按钮![SyncProjectwithGradleFiles.png](https://yx-web-nosdn.netease.im/common/c1108a218316297bb16d5d4080fcbf57/SyncProjectwithGradleFiles.png)，同步工程依赖。

5. 运行工程。
   1. 开启 Android 设备的**开发者模式**和**USB 调试**功能。将 Android 设备连接到开发电脑，在弹出的授予调试权限对话框中，**授予调试权限**，具体步骤请参见[在硬件设备上运行应用](https://developer.android.com/studio/run/device?hl=zh-cn)。

      Android Studio 菜单栏中的 **Running Devices** 下拉列表选项，由 **No Devices**变为对应的设备名称。


        ![noDevice.png](https://yx-web-nosdn.netease.im/common/7463e241a107e29ff164a5c3de9fae19/noDevice.png)
        
          
        ![RunningDevices.png](https://yx-web-nosdn.netease.im/common/9ac93e1fad905eff173f27b3cdd3b00a/RunningDevices.png)

       
        此时表示设备已成功连接到 Android Studio。

 

    2. 单击 **Run** 按钮![run按钮.png](https://yx-web-nosdn.netease.im/common/7908cfd95ba729bf8bc4d91f1c949794/run按钮.png)，编译并运行示例源码。

     

    > 注意：
    > - 建议在真机上运行，不支持模拟器调试。
    > - 请使用两个手机运行工程，Demo 上才能显示用户列表，体验 1 对 1 聊天等功能。

    

## 示例项目结构
目录 | 描述
:------|:-----
app | App模块
beauty | 美颜
oneonone | 1对1业务
entertainment | 通用工具类

```
oneonone核心目录结构
├── activity    
│   ├── BaseActivity.java  基础Activity
│   ├── CallActivity.java  呼叫&&通话页
│   ├── CustomChatP2PActivity.java   自定义1对1聊天页
│   ├── OneOnOneHomeActivity.java   1对1首页
├── constant
│   ├── AppParams.java       App参数
│   ├── AppRtcConfig.java    Rtc参数
│   ├── CallConfig.java      呼叫参数
│   ├── Constants.java       常量
├── custommessage     
│   ├── AccostMessageAttachment.java   IM自定义消息-搭讪
│   ├── AccostMessageViewHolder.java   IM自定义UI-搭讪 
│   ├── AssistantAttachment.java       IM自定义消息-小秘书
│   ├── AssistantMessageViewHolder.java  IM自定义UI-小秘书
│   ├── CommonRiskAttachment.java         IM自定义消息-风险提醒
│   ├── CommonRiskMessageViewHolder.java  IM自定义UI-风险提醒
│   ├── GiftAttachment.java            IM自定义消息-礼物
│   ├── GiftMessageViewHolder.java     IM自定义UI-礼物
│   ├── OneOnOneChatCustomMessageType.java  IM自定义消息类型常量
│   ├── PrivacyRiskAttachment.java          IM自定义消息-隐私泄漏提醒 
│   ├── PrivacyRiskMessageViewHolder.java   IM自定义UI-隐私泄漏提醒 
│   ├── TryAudioCallMessageAttachment.java  IM自定义消息-尝试音频通话 
│   ├── TryAudioCallMessageViewHolder.java  IM自定义UI-尝试音频通话 
│   ├── TryVideoCallMessageAttachment.java  IM自定义消息-尝试视频通话
│   └── TryVideoCallMessageViewHolder.java  IM自定义UI-尝试视频通话
├── dialog
│   ├── AudioInputDialog.java    语音输入弹窗
│   ├── ContactUserDialog.java   联系对方弹窗
├── fragment
│   ├── CallFragment.java              呼叫页
│   ├── HomeFragment.java              首页 
│   ├── InTheAudioCallFragment.java    音频通话中
│   ├── InTheBaseCallFragment.java     音视频通话中Base 
│   ├── InTheVideoCallFragment.java    视频通话中
│   ├── MessageFragment.java           消息列表
├── http
│   ├── HttpService.java               网络服务 
│   └── ServerApi.java                 网络Api
├── utils

│   ├── AudioInputManager.java         语音输入
│   ├── ChatUIConfigManager.java       IM UIKit自定义配置
│   ├── IMUIKitUtil.java               IM UIKIT初始化工具类
│   ├── SecurityAuditManager.java      音视频通话安全通管理类
└── viewmodel
    ├── CallViewModel.java             音视频通话ViewModel
    ├── CustomP2PViewModel.java        1对1消息ViewModel

```

