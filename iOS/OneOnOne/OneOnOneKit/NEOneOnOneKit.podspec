# Copyright (c) 2022 NetEase, Inc. All rights reserved.
# Use of this source code is governed by a MIT license that can be
# found in the LICENSE file.

require_relative "../../PodConfigs/config_podspec.rb"
require_relative "../../PodConfigs/config_third.rb"
require_relative "../../PodConfigs/config_local_social.rb"
require_relative "../../PodConfigs/config_local_common.rb"
require_relative "../../PodConfigs/config_local_core.rb"

Pod::Spec.new do |s|
  s.name             = 'NEOneOnOneKit'
  s.version          = '1.0.0'
  s.summary          = 'A short description of NEOneOnOneKit.'
  s.homepage         = YXConfig.homepage
  s.license          = YXConfig.license
  s.author           = YXConfig.author
  s.ios.deployment_target = YXConfig.deployment_target
  s.swift_version = YXConfig.swift_version
  
  if ENV["USE_SOURCE_FILES"] == "true"
    s.source = { :git => "https://github.com/netease-kit/" }
    
    s.source_files = 'Classes/**/*'
    s.dependency NECoreKit.name
    s.dependency NERtcCallKit.name
    s.dependency NERtcSDK.RtcBasic
    s.dependency NECoreIMKit.name
  else
    
  end
  YXConfig.pod_target_xcconfig(s)
  
end
