网易云信基于 1 对 1 社交业务场景，提供了一体式、可扩展、功能业务融合的解决方案，满足用户痛点需求，促进业务增长。


## 方案简介

针对 1 对 1 娱乐社交场景，网易云信提供一站式的 1 对 1  UIKit 组件库，帮助开发者更快地搭建 1 对 1 社交平台，轻松实现音视频呼叫、音视频通话、单聊消息、美颜和礼物功能。此外，各组件之间的兼容性和稳定性，让用户可以享受到高质量的社交服务体验。

您可以根据业务需要，集成对应的组件：

- **音视频呼叫组件（NERtcCallKit）**：

    音视频呼叫组件（NERtcCallKit）可以帮助用户实现高质量的语音和视频通话，保证用户之间的沟通畅通无阻。

    您可以集成呼叫组件实现音频呼叫、视频呼叫、音频通话、视频通话。
    
- **消息组件（chatKit）**：

    消息组件保证用户之间信息传递的功能，让用户能够及时收到对方的消息，保持良好的沟通。

    您可以集成消息组件实现会话列表、聊天消息、消息置顶、消息操作（复制、删除、撤回）、小秘书消息提醒等。
    
- **美颜组件（beautyKit）**：

    美颜组件是一种增强用户体验的功能，它可以帮助用户在通话过程中有更好的外观表现，提高用户的自信心。

    您可以集成美颜组件实现美肤、美型、滤镜。
    
- **礼物组件（giftKit）**：  

    礼物组件增加互动性的功能，它可以让用户之间更加亲密，提高用户留存率。

    您可以集成礼物组件实现礼物发送、礼物特效。



## 方案架构

1 对 1 娱乐社交的业务流程如下图所示。


![1V1业务流程.png](https://yx-web-nosdn.netease.im/common/528d83f47a2c5ce238bb9bc3e619f98b/1V1业务流程.png)



方案架构说明如下：
- 1 对 1 娱乐社交 App 集成了呼叫组件、消息组件、礼物组件和美颜组件。呼叫组件内部封装了 RTC SDK，您可以通过呼叫组件直接调用 RTC 的接口。消息组件内部集成了 IM UIKit。
- 客户端 A 通过呼叫组件呼叫客户端 B，客户端 B 接听后，加入RTC 房间。若客户端 B 未接听，则通过 PSTN 运营商网络，呼叫 B 的手机号。
- 融合易盾的安全通方案对内容进行安全检测，应用服务器调用 RTC 服务端接口发起内容审核后，媒体服务器拉取音视频内容，发给审核服务器进行内容安全检测。

1 对 1  UIKit 组件的架构如下表所示。

![1对1UIKit架构.png](https://yx-web-nosdn.netease.im/common/651a9f7e8546a92a02766602185a88b8/1对1UIKit架构.png)


## 功能列表

### 音视频通话和呼叫

- 集成呼叫组件，实现呼叫界面交互，接入即可用，也可自由扩展呼叫样式和交互。稳定性更好，更易对接。

- 1 对 1 音视频通话，用户可以自由沟通。具体功能如下表所示。

| <div style="width: 200px">功能</div> | 描述 |
|---|---|
| 音视频呼叫 | 发起音频呼叫或视频呼叫。 |
| 音视频通话 | 接通后可依照呼叫类型进行实时通话。 |
| 音视频控制 | 通话过程中可以控制本端音频或视频的开关，以及摄像头方向等。 |
| 话单 | 每次通话结束后都会收到对应的话单消息，标记本次通话是否接通以及通话时间、类型等数据。 |

![音视频通话.png](https://yx-web-nosdn.netease.im/common/ea4226b5048a89973ddfeaf5c5199eeb/音视频通话.png)


### 聊天消息

1 对 1 聊天消息系统支持的功能如下表所示。

<table>     
  <tr>         
    <th>功能分类</th>
    <th>功能</th>
    <th>描述</th>
  </tr>
  <tr>
    <td rowspan="9">消息类型</td>
    <td>文本消息</td>
    <td>消息内容为普通文本。</td> 
  </tr>     
  <tr>        
    <td>表情消息</td>
    <td>在输入框编辑发送emoji表情。</td>  
  </tr>   
  <tr>     
    <td>图片消息</td>
    <td>消息内容为图片 URL 地址、尺寸、图片大小等信息。
可实时拍照。</td> 
  </tr> 
  <tr>     
    <td>语音消息</td>
    <td>消息内容为语音文件的 URL 地址、时长、大小、格式等信息。</td>  
  </tr>
  <tr>     
    <td>视频消息</td>
    <td>消息内容为视频文件的 URL 地址、时长、大小、格式等信息。
可实时拍摄。</td>  
  </tr>
  <tr>     
    <td>地理位置消息</td>
    <td>消息内容为地理位置标题、经度、纬度信息。</td>  
  </tr>
  <tr>     
    <td>通知消息</td>
    <td>主要用于发送事件的通知。</td>  
  </tr>
  <tr>     
    <td>提示消息</td>
    <td>通知消息的简化，没有推送和通知栏提醒。</td>  
  </tr>
  <tr>     
    <td>自定义消息</td>
    <td>通过自定义的消息类型，实现发送和接收礼物消息。</td>  
  </tr>
  <tr>
    <td rowspan="12">消息功能</td>
    <td>删除消息</td>
    <td>删除本地聊天窗口中的聊天记录，但对端仍可见。</td> 
  </tr>    
  <tr>     
    <td>撤回消息</td>
    <td>撤回投递成功的消息，默认撤回 2 分钟内的消息，可撤回的时长可在用户控制台进行配置。
撤回操作仅支持文本、emoji表情、图片、语音、视频地理位置消息。</td>  
  </tr>
  <tr>     
    <td>重新编辑</td>
    <td>撤回后的消息，支持重新编辑。
仅文本、emoji表情消息在撤回后可重新编辑。</td>  
  </tr>
  <tr>     
    <td>复制消息</td>
    <td>仅文本、emoji表情消息支持复制。</td>  
  </tr>
  <tr>     
    <td>重发消息</td>
    <td>在弱网、断网的情况下，消息发送失败时，支持重新发送。</td>  
  </tr>
  <tr>     
    <td>离线消息</td>
    <td>用户不在线时，其他人发来消息。在下次登录时，会自动将离线期间暂存的离线消息自动下发到 客户端SDK。
下发最近 30 天内的最新的 5000 条离线消息，且每个会话最多 100 条最新的离线消息。</td>  
  </tr>
  <tr>     
    <td>漫游消息</td>
    <td>在新设备登录时，将服务器记录的漫游消息同步下来。
SDK 自动漫游 7 天内最近 100 个会话，每个会话最近的 100 条消息。</td>  
  </tr>
  <tr>     
    <td>多端同步</td>
    <td>多客户端同时在线时，消息实时下发到多端。</td>  
  </tr>
  <tr>     
    <td>历史消息</td>
    <td>支持本地历史消息和云端历史消息。
标准版IM可获取到最近1年的云端历史消息，最多支持扩展至3年(增值功能)。</td>  
  </tr>
  <tr>     
    <td>已读回执</td>
    <td>查看点对点会话中对方的已读未读状态。</td>  
  </tr>
  <tr>     
    <td>正在输入</td>
    <td>通过自定义系统通知实现，可参见：<a href="https://doc.yunxin.163.com/messaging/docs/zI2ODg0MjA?platform=android">自定义系统通知收发</a>
。</td>  
  </tr>
  <tr>     
    <td>消息推送</td>
    <td>支持苹果 APNS、谷歌FCM、小米推送、华为推送、OPPO推送、VIVO推送等厂商推送。</td>  
  </tr>
  <tr>
    <td rowspan="2">会话列表</td>
    <td>置顶会话</td>
    <td>删置顶会话功能可以将重要的聊天记录置顶，方便快速查找。</td> 
  </tr> 
  <tr>     
    <td>删除会话</td>
    <td>支持将某个会话从会话列表中删除，但不删除聊天记录，可用于隐藏某些私密聊天等场景。</td>  
  </tr>
</table>




![消息系统Demo.png](https://yx-web-nosdn.netease.im/common/2f91cdb81c894a92fde95adafb00c6a7/消息系统Demo.png)


### 礼物

送礼在 1 对 1 娱乐社交场景中是非常重要的功能，它可以让用户之间更加亲密，提高用户留存率。

礼物组件默认提供了几种礼物类型, 用于展示礼物消息的发送和接收功能，开发者根据自己的业务需求定制礼物特效。



### 美颜
提供高质量美颜设置功能，UI 交互和美颜维度都已设置好，接入即用。用户也可以自行调整所有美颜效果参数。支持如下美颜效果：
- 滤镜；持通过美颜资源或模型打造多种个性化的滤镜。
- 美肤：包括磨皮、美白、红润和锐化。
- 美型：包括大眼、瘦脸、下巴调整、亮眼、美牙等。


![美颜.png](https://yx-web-nosdn.netease.im/common/63985acccd4ae180f8af5d75e588bf2c/美颜.png)

### 安全通
针对内容安全，1 对 1 娱乐社交场景方案支持融合安全通，在保障产品运营过程中的基础能力稳定及安全合规的同时，兼具高效、安全、易用、省钱。

安全通能对聊天消息和音视频流中的广告、暴力、涉政、色情、欺诈等内容进行实时检测，同时支持开发者自定义关键词和图片内容库，以及检测内容查询和数据统计，根据业务情况动态调整音视频检测频率。

低成本接入，安全通和音视频功能深度融合，无需开发和花成本对接其它内容安全检测商。另外，通信服务管理和内容安全检测服务混合部署，也能一定程度上降低成本。

利用通信服务和内容安全检测服务的强绑定，在通信服务端可先对视频进行降噪、优化的前处理，进一步提升检测率，保证了安全通产品优秀的检测质量。

![安全通.png](https://yx-web-nosdn.netease.im/common/1b9b1a2f646bbbadc33867f936cf82bf/安全通.png)


## 方案优势

### 一站式 UI 组件库，快速、简单、高效

| 优势 | 说明 |  |
|---|---|:---:|
| UI 组件解耦 | 1对1 UIKit 不同组件可相互独立运行使用。您可按需选择组件，将其快速集成到您的应用，实现相应的 UI 功能，减少无用依赖。 |  
| UI 能力简洁易用 | 1对1 UIKit 的业务逻辑层与 UI 层相互独立。在 UI 层，仅需关注视图展示和事件处理，清晰的数据流转处理，让 UI 层代码更简洁易懂。 |  
| 强大的自定义能力 | 1对1 UIKit 支持在各 UI 组件的初始化过程中配置自定义 UI。同时提供 Fragment 和 View 的能力封装，快速将 UI 功能添加到您的应用中。 |  
| 完善的业务逻辑处理 | 1对1 UIKit 业务逻辑层提供完善的业务逻辑处理能力。无需关心 SDK 层不同接口间的复杂处理逻辑，业务逻辑层一个接口帮您搞定所有。 |  

### 超低时延
- 通过网易云信自研实时传输网络，网络节点覆盖全球。
- 采用最优寻址算法辅以全局实时调度能力，保证端到端平均时延 < 200 ms。

### 超低卡顿
- 网易云信拥有领先行业的精准带宽侦测、智能拥塞控制、前向纠错、编码优化等技术，实测抗丢包率可达80%。
- 适应复杂多变的网络环境，在丢包严重的弱网环境仍可正常通话，确保通话体验流畅稳定。

### 超高品质
- 支持 1080P 高清画质，80% 丢包率下仍可正常视频。
- 音频方面支持 48 kHz 采样率，128 kbps 码率，80% 丢包率可正常语音。
- 拥有行业一流的音频 3A 算法处理，即回声消除 AEC、自动噪声抑制 ANS、自动增益控制 AGC，帮助用户消除通话中的回声和啸叫，为用户提供最纯净的通话体验。



## 效果展示
您可以扫描二维码下载网易云信派对，体验 1 对 1 娱乐社交、语聊房、一起听三个业务场景。

| Android|iOS |  
:---- | :----------|  
|<img   style="width:200px" src="https://yx-web-nosdn.netease.im/common/53a6ebcf6bbbf6b605acfb108fba0d15/云信派对Demo-Android.png" alt="云信派对Demo-Android" > | <img   style="width:200px" src="https://yx-web-nosdn.netease.im/common/6744142358308041f128ba68e9c54b8e/云信派对Demo-iOS.png" alt="云信派对Demo-iOS" >| 




1 对 1 娱乐社交 Demo 的界面效果如下：



![Demo界面效果.png](https://yx-web-nosdn.netease.im/common/8f1fe6045a97486f26486e3a93c1d971/Demo界面效果.png)


![消息系统Demo.png](https://yx-web-nosdn.netease.im/common/2f91cdb81c894a92fde95adafb00c6a7/消息系统Demo.png)

单击**个人中心** > **美颜设置**，体验各种美颜效果。

## 联系我们

- 如果想要了解该场景的更多信息，请参见[1 对 1 娱乐社交场景方案文档](https://doc.yunxin.163.com/1v1-social/docs/jk2OTI0NTM?platform=android)
- 如果您遇到问题，可以先查阅[知识库](https://faq.yunxin.163.com/kb/main/#/)
- 如果需要售后技术支持，请[提交工单](https://app.yunxin.163.com/index#/issue/submit)  

## 更多场景方案
网易云信针对1 对 1 娱乐社交、语聊房、PK连麦、在线教育等业务场景，推出了一体式、可扩展、功能业务融合的全链路解决方案，帮助客户快速接入、业务及时上线，提高营收增长。
- [云信娱乐社交服务端 Nemo](https://github.com/netease-kit/nemo)
- [语聊房](https://github.com/netease-kit/NEChatroom)
- [一起听](https://github.com/netease-kit/NEListenTogether)
- [在线K歌](https://github.com/netease-kit/NEKaraoke)
- [PK连麦](https://github.com/netease-kit/OnlinePK)
- [在线教育](https://github.com/netease-kit/WisdomEducation)
- [多人视频通话](https://github.com/netease-kit/NEGroupCall)
