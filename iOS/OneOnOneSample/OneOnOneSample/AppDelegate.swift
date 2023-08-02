// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

import UIKit
import IHProgressHUD
import NEUIKit
import NEOneOnOneUIKit
import NIMSDK
import NEOneOnOneChatUIKit
import NEMapKit
import NECoreKit
import FaceUnity
import NELoginSample

@main
class AppDelegate: UIResponder, UIApplicationDelegate, UNUserNotificationCenterDelegate {
  
  var window: UIWindow?
  
  var reachability: NPTReachability?
  
  func application(_ application: UIApplication, didFinishLaunchingWithOptions launchOptions: [UIApplication.LaunchOptionsKey: Any]?) -> Bool {
    // Override point for customization after application launch.
    
    
    window = UIWindow(frame: UIScreen.main.bounds)
    
    let tab = UITabBarController()
    tab.tabBar.backgroundColor = UIColor(red: 0.965, green: 0.973, blue: 0.98, alpha: 1)
    
    let message = UINavigationController(rootViewController: NEOneOnOneConversationsViewController())
    message.tabBarItem.title = "Message".localized
    message.tabBarItem.image = UIImage(named: "message")
    
    let homeViewController: NPTHomeViewController = NPTHomeViewController()
    homeViewController.privateLatter = { sessionId in
      // 跳转
//        tab.selectedIndex = 1
        if let tab = self.window?.rootViewController as? UITabBarController,
           let nav = tab.viewControllers?[0] as? UINavigationController{
                  let session = NIMSession(sessionId, type: .P2P)
                  Router.shared.use(
                    PushP2pChatVCRouter,
                    parameters: ["nav": nav as Any, "session": session as Any],
                    closure: nil
                  )
        }
    }
    let home = NEUIBackNavigationController(rootViewController: homeViewController)
    
    home.tabBarItem.title = "Recreation".localized
    home.tabBarItem.image = UIImage(named: "home")
    
    let person = NEUIBackNavigationController(rootViewController: NPTPersonViewController())
    person.tabBarItem.title = "Personal_Center".localized
    person.tabBarItem.image = UIImage(named: "person")
    
    tab.viewControllers = [home, message, person]
    window?.rootViewController = tab
    window?.makeKeyAndVisible()
    
    IHProgressHUD.set(defaultStyle: .light)
    IHProgressHUD.set(defaultMaskType: .black)
    IHProgressHUD.set(maximumDismissTimeInterval: 1)
    
    setupReachability("163.com")
    startNotifier()
    
    checkFirstRun()
    // 自动化测试
    //    setupHawk()
    return true
  }
    func baseInit(){
        let config = NELoginSampleConfig()
        config.appKey = Configs.AppKey
        config.appSecret = Configs.AppSecret
        var loginSampleExtras = Configs.extras
        loginSampleExtras["baseUrl"] = Configs.loginSampleBaseUrl
        config.extras = loginSampleExtras
        
        ///如果用户自己填了账号，则直接用账号密码登录
        if userUuid.count > 0 ,imToken.count > 0 ,userToken.count > 0 {
            self.loginRoom(shouldInit: true)
            return
        }
        
        NELoginSample.getInstance().initialize(config) { code, msg, obj in
            if code == 0 {
                NELoginSample.getInstance().createAccount(nil, sceneType: .oneOnOne, userUuid: nil, imToken: nil) { code, msg, account in
                    if code == 0{
                        print("\(String(describing: account))")
                        //获取账号成功
                        userUuid = account?.userUuid ?? ""
                        userToken = account?.userToken ?? ""
                        imToken = account?.imToken ?? ""
                        userName = account?.userName ?? ""
                        icon = account?.icon ?? ""

                       self.loginRoom(shouldInit: true)
                    }
                    
                }
            }
        }
    }
  
  ///远端推送
  func application(_ application: UIApplication, didRegisterForRemoteNotificationsWithDeviceToken deviceToken: Data) {
    NIMSDK.shared().updateApnsToken(deviceToken)
  }
  
  func registerAPNS(){
    if #available(iOS 10.0, *) {
      let center = UNUserNotificationCenter.current()
      center.delegate = self
      
      center.requestAuthorization(options: [.badge, .sound, .alert]) { grant, error in
      }
    } else {
      let setting = UIUserNotificationSettings(types: [.badge, .sound, .alert], categories: nil)
      UIApplication.shared.registerUserNotificationSettings(setting)
    }
    UIApplication.shared.registerForRemoteNotifications()
    UIApplication.shared.applicationIconBadgeNumber = 0
  }
  
  func application(_ application: UIApplication, didFailToRegisterForRemoteNotificationsWithError error: Error) {
    print("app delegate : \(error.localizedDescription)")
  }
  
  
  func checkFirstRun() {
    // 值没有实际含义
    if let _ = UserDefaults.standard.value(forKey: "FirstRun") as? Bool {
    } else {
      userAgreementWindow.show()
    }
  }
}

/// 子线程串行去初始化
private let initQueue: DispatchQueue = .init(label: "com.party.init")

/// 初始化
extension AppDelegate {
  
  func initAllModules(_ appKey: String, extra: [String: String] = [String: String](), callback: @escaping (Int, String?) -> Void) {
    var oneOnOneInit = false
    
    func checkCallback() {
      if oneOnOneInit {
        callback(0, nil)
      }
    }
    

      
    initOneOnOne(appKey,extras: extra) { code, msg, _ in
      if code != 0 {
        callback(code, msg)
      } else {
        initQueue.async {
          oneOnOneInit = true
          NEOneOnOneChatRegisterEngine.getInstance().resgiterEngine()
          NEOneOnOneChatRegisterEngine.getInstance().isSupportAIGC = Configs.isSupportAIGC
          //地图map初始化
          NEMapClient.shared().setupMapClient(withAppkey: Configs.AppAMapKey)
          checkCallback()
        }
      }
    }
  }
  
  func initOneOnOne(_ appKey:String ,extras: [String: String] = [String: String](), callback: @escaping (Int, String?, Any?) -> Void){
    let config = NEOneOnOneKitConfig()
    config.appKey = appKey
    var oneOnOneExtras = extras
    oneOnOneExtras["baseUrl"] = Configs.oneOnOneBaseUrl
    config.extras = oneOnOneExtras
    ///这行代码可以不写，因为如果初始化走的是Roomkit的初始化
    config.APNSCerName = Configs.pushCerName
    NEOneOnOneUIManager.sharedInstance().initialize(with: config, callback: callback)
  }
}

/// 登录
extension AppDelegate {
  
  public func loginRoom(shouldInit: Bool = false) {
    
    func login() {
        
        NEOneOnOneUIManager.sharedInstance().login(withAccount: userUuid, token: userToken, imToken: imToken, nickname: userName, avatar: icon ,resumeLogin: false) { code, msg, objc in
          if code != 0{
              DispatchQueue.main.async {
                IHProgressHUD.dismiss()
                IHProgressHUD.showError(withStatus: "Login_Failed".localized)
                  print("登录失败 code:\(code) msg:\(String(describing: msg))")
              }
            print("Error Happen")
          } else {
              //注册APNS，因为IM是在登录成功之后初始化的
              DispatchQueue.main.async {
                  self.registerAPNS()
              }
            // 启动添加监听
            NEOneOnOneUIKitEngine.sharedInstance().addObserve()
            // 是否可以拨打
            NEOneOnOneUIKitEngine.sharedInstance().canCall = {() -> String? in
              return nil
            }

              NEOneOnOneUIKitCallEngine.getInstance.interceptor = { () -> Bool in
                //收到邀请了
              NotificationCenter.default.post(name: NSNotification.Name("receiveInvite"), object: nil, userInfo:nil)
              return false
            }
          }
          DispatchQueue.main.async {
            // 刷新头像与昵称
            IHProgressHUD.dismiss()
            NotificationCenter.default.post(name: NSNotification.Name("Logined"), object: nil, userInfo: ["nickname": userName, "avatar": icon ])
            // 初始化美颜模块
            FUDemoManager.share()
          }
        }
        
      }
      
    IHProgressHUD.show(withStatus: "Logging_In".localized)
    if shouldInit {
        initAllModules(Configs.AppKey, extra: Configs.extras) { code, msg in
        if (code != 0) {
          print("初始化失败 code:\(code) msg:\(String(describing: msg))")
        } else {
          login()
        }
      }
    } else {
      login()
    }
  }
  
}

/// 网络监听
extension AppDelegate {
  func setupReachability(_ hostName: String) {
    self.reachability = try? NPTReachability(hostname: hostName)
    reachability?.whenReachable = { reachability in
        DispatchQueue.once(token: "Init") {
            DispatchQueue.main.async {
                self.baseInit()
            }
        }
    }
    reachability?.whenUnreachable = { reachability in
      
    }
  }
  
  func startNotifier() {
    print("--- start notifier")
    do {
      try reachability?.startNotifier()
    } catch {
      return
    }
  }
  
  func stopNotifier() {
    print("--- stop notifier")
    reachability?.stopNotifier()
  }
  
  func checkNetwork(showHUD: Bool = true) -> Bool {
    if reachability?.connection == .cellular || reachability?.connection == .wifi {
      return true
    }
    if showHUD {
      IHProgressHUD.showError(withStatus: "Net_Error".localized)
    }
    return false
  }
}

/// 被踢监听
extension AppDelegate:NEOneOnOneUIDelegate{
    
    func onOne(_ event: NEOneOnOneClientEvent) {
        switch event {
        case .kicOut,.forbidden:
          NEOneOnOneUIKitEngine.sharedInstance().removeObserve()
          IHProgressHUD.showError(withStatus: "Kick_Out".localized)
          DispatchQueue.main.asyncAfter(deadline: .now() + 1) {
              if let tab = UIApplication.shared.keyWindow?.rootViewController as? UITabBarController,
                 let home = tab.viewControllers?[0] as? UINavigationController,
                 let person = tab.viewControllers?[2] as? UINavigationController{
                  home.popToRootViewController(animated: false)
                  person.popToRootViewController(animated: false)
                  tab.selectedIndex = 0
                  
                  if let nav = tab.viewControllers?[1] as? UINavigationController,
                     let _ = nav.viewControllers.first as? NEOneOnOneConversationsViewController {
                    let n = NEOneOnOneConversationsViewController()
                    let message = UINavigationController(rootViewController: n)
                    message.tabBarItem.title = "Message".localized
                    message.tabBarItem.image = UIImage(named: "message")
                    tab.viewControllers = [home, message, person]
                    n.refreshNotificationTips()
                  }
              }
              print("账号退出登录,请重新启动")
          }
        case .loggedIn:
          // 登录成功默认查一把未读消息数
          if let tab = window?.rootViewController as? UITabBarController,
             let nav = tab.viewControllers?[1] as? UINavigationController,
             let message = nav.viewControllers.first as? NEOneOnOneConversationsViewController {
            message.getMsgUnreadCount()
          }
        default: break
        }
      }
    
}
