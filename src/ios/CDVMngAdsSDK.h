/*!
 *  @header    CDVMngAdsSDK.h
 *  @abstract  Cordova Plugin for the MngAds iOS SDK.
 *  @version   1.0.10
 */

// Cordova
#import <Cordova/CDVPlugin.h>

// MngAds SDK
#import "MNGAdsSDKFactory.h"


@interface CDVMngAdsSDK : CDVPlugin<MNGAdsAdapterInterstitialDelegate,MNGAdsAdapterBannerDelegate,MNGClickDelegate,MNGAdsSDKFactoryDelegate>

- (void)mngadssdk_initWithAppId:(CDVInvokedUrlCommand *)command;
- (void)mngadssdk_isInitialized:(CDVInvokedUrlCommand *)command;
- (void)mngadssdk_debugEnable:(CDVInvokedUrlCommand *)command;
//INTERSTITIAL
- (void)mngadssdk_createInterstitial:(CDVInvokedUrlCommand *)command;
//BANNER
- (void)mngadssdk_createBanner:(CDVInvokedUrlCommand *)command;
- (void)mngadssdk_showBanner:(CDVInvokedUrlCommand *)command;
- (void)mngadssdk_removeBanner:(CDVInvokedUrlCommand *)command;
- (void)mngadssdk_onResume:(CDVInvokedUrlCommand *)command;


@end
