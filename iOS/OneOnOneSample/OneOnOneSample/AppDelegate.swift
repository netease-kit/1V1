// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

import UIKit
import NEUIKit
import NEOneOnOneUIKit
//import Hawk
import NIMSDK
import NEOneOnOneChatUIKit
import NEMapKit
import NECoreKit

@main
class AppDelegate: UIResponder, UIApplicationDelegate, UNUserNotificationCenterDelegate {
  
  var window: UIWindow?
  
  
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
        tab.selectedIndex = 1
        if let tab = self.window?.rootViewController as? UITabBarController,
           let nav = tab.viewControllers?[1] as? UINavigationController{
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
    
    
    tab.viewControllers = [home, message]
    window?.rootViewController = tab
    window?.makeKeyAndVisible()
    
    setupLoginSDK()
    
    return true
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
        if grant == false {
          DispatchQueue.main.async {
            UIApplication.shared.keyWindow?.makeToast(NSLocalizedString("open_push", comment: ""))
          }
        }
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
}




/// 登录
extension AppDelegate {
    
    func setupLoginSDK() {
        let config = NEOneOnOneKitConfig()
        //推送证书名称，如果需要推送功能的话，请集成网易运行IM离线推送，
        config.APNSCerName = pushCerName
        
        NEOneOnOneChatRegisterEngine.getInstance().resgiterEngine()
        //地图map初始化,如果需要使用地图，请注册appkey
        NEMapClient.shared().setupMapClient(withAppkey: AppAMapKey)
        var mutableDic:[String : String]?
        config.appKey = self.getAppkey()
        if isOverSea {
            mutableDic = ["serverUrl": "oversea"]
        }
        if mutableDic == nil{
            mutableDic = [:]
        }
        mutableDic?["baseUrl"] = kApiHost
        config.extras = mutableDic ?? [:]
        NEOneOnOneUIManager.sharedInstance().initialize(with: config) { code, msg, obj in
            guard code == 0 else { return }

            NEOneOnOneUIManager.sharedInstance().login(withAccount: accountId, token: accessToken, imToken: imToken, nickname: nickname, avatar: avatar, resumeLogin: false) { code, msg, obj in
                if code != 0 {
                    NSLog("登录失败")
                } else {
                    // 启动添加监听
                    NEOneOnOneUIKitEngine.sharedInstance().addObserve()

                    // 是否可以播放
                    NEOneOnOneUIKitEngine.sharedInstance().canCall = {
                        /// 可以拨打
                        return nil
                    }

                    NEOneOnOneUIKitEngine.sharedInstance().interceptor = {
                        return false
                    }
                }
            }
        }
    }
    
    func getAppkey() -> String {

        let isOutsea = isOverSea

        if isOutsea {

            return APP_KEY_OVERSEA

        } else {

            return APP_KEY_MAINLAND

        }

    }
}
