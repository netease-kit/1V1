#
# Be sure to run `pod lib lint NEConversationUIKit.podspec' to ensure this is a
# valid spec before submitting.
#
# Any lines starting with a # are optional, but their use is encouraged
# To learn more about a Podspec see https://guides.cocoapods.org/syntax/podspec.html
#
require_relative "../../PodConfigs/config_podspec.rb"
require_relative "../../PodConfigs/config_third.rb"
require_relative "../../PodConfigs/config_local_core.rb"
require_relative "../../PodConfigs/config_local_common.rb"
require_relative "../../PodConfigs/config_local_im.rb"

Pod::Spec.new do |s|
  s.name             = 'NEConversationUIKit'
  s.version          = '9.6.0'
  s.summary          = 'Netease XKit'
  s.homepage         = YXConfig.homepage
  s.license          = YXConfig.license
  s.author           = YXConfig.author
  s.ios.deployment_target = YXConfig.deployment_target
  s.swift_version = YXConfig.swift_version
  
  if ENV["USE_SOURCE_FILES"] == "true"
    s.source = { :git => "https://github.com/netease-kit/" }

    s.source_files = 'NEConversationUIKit/Classes/**/*'
    s.resource = 'NEConversationUIKit/Assets/**/*'
    s.dependency NECommonUIKit.name
    s.dependency NEChatKit.name
    s.dependency MJRefresh.name

  else
    s.source = { :http => "https://yx-web-nosdn.netease.im/package/NEConversationUIKit_iOS_v9.4.0.framework.zip?download=NEConversationUIKit_iOS_v9.4.0.framework.zip" }
    
    s.subspec 'NOS' do |nos|
      nos.vendored_frameworks = 'NEConversationUIKit.framework'
      nos.dependency NEChatKit.NOS
      nos.dependency NECommonUIKit.name
      nos.dependency MJRefresh.name, MJRefresh.version

    end
    
    s.subspec 'NOS_Special' do |nos|
      nos.vendored_frameworks = 'NEConversationUIKit.framework'
      nos.dependency NEChatKit.NOS_Special
      nos.dependency NECommonUIKit.name
      nos.dependency MJRefresh.name

    end
    
    s.subspec 'FCS' do |fcs|
      fcs.vendored_frameworks = 'NEConversationUIKit.framework'
      fcs.dependency NEChatKit.FCS
      fcs.dependency NECommonUIKit.name
      fcs.dependency MJRefresh.name, MJRefresh.version

    end
    
    s.subspec 'FCS_Special' do |fcs|
      fcs.vendored_frameworks = 'NEConversationUIKit.framework'
      fcs.dependency NEChatKit.FCS_Special
      fcs.dependency NECommonUIKit.name
      fcs.dependency MJRefresh.name

    end
    s.default_subspecs = 'NOS'
  end

  YXConfig.pod_target_xcconfig(s)
  
end
