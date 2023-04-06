// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

#import <Foundation/Foundation.h>
#import <SystemConfiguration/SystemConfiguration.h>

/**
 * Create NS_ENUM macro if it does not exist on the targeted version of iOS or
 *OS X.
 *
 * @see http://nshipster.com/ns_enum-ns_options/
 **/
#ifndef NS_ENUM
#define NS_ENUM(_type, _name) \
  enum _name : _type _name;   \
  enum _name : _type
#endif

extern NSString *const kOneOnOneReachabilityChangedNotification;

typedef NS_ENUM(NSInteger, NetworkStatus) {
  // Apple NetworkStatus Compatible Names.
  NotReachable = 0,
  ReachableViaWiFi = 2,
  ReachableViaWWAN = 1
};

@class NEOneOnOneReachability;

typedef void (^NetworkReachable)(NEOneOnOneReachability *reachability);
typedef void (^NetworkUnreachable)(NEOneOnOneReachability *reachability);

@interface NEOneOnOneReachability : NSObject

@property(nonatomic, copy) NetworkReachable reachableBlock;
@property(nonatomic, copy) NetworkUnreachable unreachableBlock;

@property(nonatomic, assign) BOOL reachableOnWWAN;

+ (NEOneOnOneReachability *)reachabilityWithHostname:(NSString *)hostname;
// This is identical to the function above, but is here to maintain
// compatibility with Apples original code. (see .m)
+ (NEOneOnOneReachability *)reachabilityWithHostName:(NSString *)hostname;
+ (NEOneOnOneReachability *)reachabilityForInternetConnection;
+ (NEOneOnOneReachability *)reachabilityWithAddress:(void *)hostAddress;
+ (NEOneOnOneReachability *)reachabilityForLocalWiFi;

- (NEOneOnOneReachability *)initWithReachabilityRef:(SCNetworkReachabilityRef)ref;

- (BOOL)startNotifier;
- (void)stopNotifier;

- (BOOL)isReachable;
- (BOOL)isReachableViaWWAN;
- (BOOL)isReachableViaWiFi;

// WWAN may be available, but not active until a connection has been
// established. WiFi may require a connection for VPN on Demand.
- (BOOL)isConnectionRequired;  // Identical DDG variant.
- (BOOL)connectionRequired;    // Apple's routine.
// Dynamic, on demand connection?
- (BOOL)isConnectionOnDemand;
// Is user intervention required?
- (BOOL)isInterventionRequired;

- (NetworkStatus)currentReachabilityStatus;
- (SCNetworkReachabilityFlags)reachabilityFlags;
- (NSString *)currentReachabilityString;
- (NSString *)currentReachabilityFlags;

@end
