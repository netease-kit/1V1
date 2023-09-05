#
#  Be sure to run `pod spec lint NEChatUIKit.podspec' to ensure this is a
#  valid spec and to remove all comments including this before submitting the s.
#
#  To learn more about Podspec attributes see https://guides.cocoapods.org/syntax/pods.html
#  To see working Podspecs in the CocoaPods repo see https://github.com/CocoaPods/Specs/
#
require_relative "../../PodConfigs/config_podspec.rb"
require_relative "../../PodConfigs/config_third.rb"
require_relative "../../PodConfigs/config_local_core.rb"
require_relative "../../PodConfigs/config_local_common.rb"
require_relative "../../PodConfigs/config_local_im.rb"

Pod::Spec.new do |s|
  s.name         = 'NEChatUIKit'
  s.version      = '9.6.0'
  s.summary      = 'Chat Module of IM.'
  s.homepage         = YXConfig.homepage
  s.license          = YXConfig.license
  s.author           = YXConfig.author
  s.ios.deployment_target = YXConfig.deployment_target
  s.swift_version = YXConfig.swift_version
  
  if ENV["USE_SOURCE_FILES"] == "true"
    s.source = { :git => "https://github.com/netease-kit/" }
    s.source_files = 'NEChatUIKit/Classes/**/*'
    s.resource = 'NEChatUIKit/Assets/**/*'
    s.dependency NEChatKit.name
    s.dependency NECommonUIKit.name
    s.dependency NECommonKit.name
    s.dependency MJRefresh.name
    s.dependency 'SDWebImageWebPCoder'
    s.dependency 'SDWebImageSVGKitPlugin'
    s.dependency LottieOC.name, LottieOC.version
  else
    s.source = { :http => "https://yx-web-nosdn.netease.im/xkit/IMUIKit/9.6.0/NEChatUIKit_iOS_v9.6.0.framework.zip?download=NEChatUIKit_iOS_v9.6.0.framework.zip" }
    
    s.subspec 'NOS' do |nos|
      nos.vendored_frameworks = 'NEChatUIKit.framework'
      nos.dependency NEChatKit.NOS
      nos.dependency NECommonUIKit.name
      nos.dependency NECommonKit.name
      nos.dependency MJRefresh.name
      nos.dependency 'SDWebImageWebPCoder'
      nos.dependency 'SDWebImageSVGKitPlugin'
      nos.dependency LottieOC.name, LottieOC.version
    end
    
    s.subspec 'NOS_Special' do |nos|
      nos.vendored_frameworks = 'NEChatUIKit.framework'
      nos.dependency NEChatKit.NOS_Special
      nos.dependency NECommonUIKit.name
      nos.dependency NECommonKit.name
      nos.dependency MJRefresh.name
      nos.dependency 'SDWebImageWebPCoder'
      nos.dependency 'SDWebImageSVGKitPlugin'
      nos.dependency LottieOC.name, LottieOC.version
    end
    
    s.subspec 'FCS' do |fcs|
      fcs.vendored_frameworks = 'NEChatUIKit.framework'
      fcs.dependency NEChatKit.FCS
      fcs.dependency NECommonUIKit.name
      fcs.dependency NECommonKit.name
      fcs.dependency MJRefresh.name
      fcs.dependency 'SDWebImageWebPCoder'
      fcs.dependency 'SDWebImageSVGKitPlugin'
      fcs.dependency LottieOC.name, LottieOC.version
    end
    
    s.subspec 'FCS_Special' do |fcs|
      fcs.vendored_frameworks = 'NEChatUIKit.framework'
      fcs.dependency NEChatKit.FCS_Special
      fcs.dependency NECommonUIKit.name
      fcs.dependency NECommonKit.name
      fcs.dependency MJRefresh.name
      fcs.dependency 'SDWebImageWebPCoder'
      fcs.dependency 'SDWebImageSVGKitPlugin'
      fcs.dependency LottieOC.name, LottieOC.version
    end
    s.default_subspecs = 'NOS'
  end

  YXConfig.pod_target_xcconfig(s)
end
