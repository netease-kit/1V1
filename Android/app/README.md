网易云信为您提供 1 对 1 娱乐社交场景的开源示例项目，您可以参考本文档快速跑通示例项目，在本地运行示例 Demo，体验 1 对 1 娱乐社交场景的最佳效果。

## 开发环境要求
在开始运行示例项目之前，请确保开发环境满足以下要求：

| 环境要求         | 说明                                                         |
| ---------------- | ------------------------------------------------------------ |
| JDK 版本         | 1.8.0 及以上版本                                             |
| Android API 版本 | API 21、Android5.0 及以上版本                               |
| CPU架构          | ARM 64、ARMV7                                                 |
| IDE              | Android Studio                                               |
| 其他             | 依赖 Androidx，不支持 support 库。 |

## <span id="前提条件">前提条件</span>
请确认您已完成以下操作：

- 在云信控制台中创建应用并获取App Key。
- 在云信控制台中开通音视频通话2.0、信令服务以及相关的抄送与权限、PSTN功能。

## <span id="快速跑通 Sample Code">运行示例源码</span>

::: note note
1 对 1 娱乐社交的示例源码仅供开发者接入参考，实际应用开发场景中，请结合具体业务需求修改使用。

若您计划将源码用于生产环境，请确保应用正式上线前已经过全面测试，以免因兼容性等问题造成损失。
:::



1. 克隆本仓库至您本地工程。
2. 开启 Android 设备的开发者选项，通过 USB 连接线将 Android 设备接入电脑。

3. 通过 Android Studio 打开项目。在 `AppConstants` 文件中配置应用的 App Key。

    ```
        public static final String APP_KEY = "your app key";
    ```
4. 在 Android Studio 中，单击 **Sync Project with Gradle Files** 按钮，同步工程依赖。
5. 选中设备直接运行，即可体验 Demo。

## 示例项目结构
目录 | 描述
---|---
base | 基础类
constant | 常量及配置项
http | 网络请求
model | 数据模型
ui | 页面
utils | 工具类