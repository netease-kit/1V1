# Copyright (c) 2022 NetEase, Inc. All rights reserved.
# Use of this source code is governed by a MIT license that can be
# found in the LICENSE file.

Pod::Spec.new do |s|
  s.name             = 'NEOneOnOneUIKit'
  s.version          = '1.0.0'
  s.summary          = 'A short description of NEOneOnOneUIKit.'
  
  # This description is used to generate tags and improve search results.
  #   * Think: What does it do? Why did you write it? What is the focus?
  #   * Try to keep it short, snappy and to the point.
  #   * Write the description between the DESC delimiters below.
  #   * Finally, don't worry about the indent, CocoaPods strips it!
  
  s.description      = <<-DESC
  TODO: Add long description of the pod here.
  DESC
  
  s.homepage         = 'https://github.com/mayajie@corp.netease.com/NEOneOnOneUIKit'
  # s.screenshots     = 'www.example.com/screenshots_1', 'www.example.com/screenshots_2'
  s.license          = { :type => 'MIT', :file => 'LICENSE' }
  s.author           = { 'mayajie@corp.netease.com' => 'mayajie@corp.netease.com' }
  s.source           = { :git => 'https://github.com/mayajie@corp.netease.com/NEOneOnOneUIKit.git', :tag => s.version.to_s }
  # s.social_media_url = 'https://twitter.com/<TWITTER_USERNAME>'
  
  s.ios.deployment_target = '10.0'
  
  s.source_files = 'Classes/**/*'
  s.dependency 'NEOneOnOneKit'
  s.dependency 'SDWebImage'
  s.dependency 'NERtcCallUIKit'
  s.dependency 'NEUIKit'
  s.dependency 'MJRefresh'
  s.dependency 'lottie-ios', '~> 2.5.3'
  s.dependency 'Toast'
  s.dependency 'NERtcCallKit'
  s.dependency 'NECommonUIKit'
  s.dependency 'SnapKit'
  s.dependency 'NESocialUIKit'
  s.resource = 'Assets/**/*'
  
  s.pod_target_xcconfig = {
    'EXCLUDED_ARCHS[sdk=iphonesimulator*]' => 'arm64',
    'BUILD_LIBRARY_FOR_DISTRIBUTION' => 'YES'
  }
  
  s.user_target_xcconfig = {
    'EXCLUDED_ARCHS[sdk=iphonesimulator*]' => 'arm64',
    'BUILD_LIBRARY_FOR_DISTRIBUTION' => 'YES'
  }
  
end
