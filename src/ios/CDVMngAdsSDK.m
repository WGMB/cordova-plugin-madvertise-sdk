/*!
 *  @header    CDVMngAdsSDK.m
 *  @abstract  Cordova Plugin for the Mng Ads iOS SDK.
 *  @version   1.0.10
 */

#import "CDVMngAdsSDK.h"
#import <Cordova/CDVViewController.h>
#import "MNGPreference.h"

@implementation CDVMngAdsSDK{
    MNGAdsSDKFactory *interstitialAdsFactory;
    MNGAdsSDKFactory *bannerAdsFactory;
    MNGAdsSDKFactory *nativeAdsFactory;
    NSString *interstitialCallbackId;
    NSString *bannerCallbackId;
    NSString *nativeCallbackId;
    NSString *sdkCallbackId;
    UIView *banner;
    UIView *webView;
    UIView *nativeView;
    BOOL bannerShowen;
    int height;
    NSString *position;
    BOOL autoDisplay;
}

- (void)pluginInitialize {
    [super pluginInitialize];
    bannerShowen = NO;
    webView = [(CDVViewController*)[self viewController] webView];
    webView.scrollView.backgroundColor = [UIColor whiteColor];
}

#pragma mark - MNG Ads SDK

-(MNGPreference *)preferencesWithString:( NSObject *)pref{
    if (!pref || [pref isEqual:@""]) {
        return nil;
    }
    NSDictionary *dict;
    if ([pref isKindOfClass:[NSString class]]) {
        NSString *jsonString = (NSString *)pref;
        NSData *data = [jsonString dataUsingEncoding:NSUTF8StringEncoding];
        dict = [NSJSONSerialization JSONObjectWithData:data
                                               options:0
                                                 error:NULL];
    }else if([pref isKindOfClass:[NSDictionary class]]){
        dict = (NSDictionary *)pref;
    }else{
        return nil;
    }
    
    if (!dict) {
        return nil;
    }
    MNGPreference *preferences = [[MNGPreference alloc]init];
    if (dict[@"age"]) {
        preferences.age = [dict[@"age"]integerValue];
    }
    
    if (dict[@"keyword"]) {
        preferences.keyWord = dict[@"keyword"];
    }
    if (dict[@"gender"]) {
        if ([dict[@"gender"] isEqualToString:@"M"]) {
            preferences.gender = MNGGenderMale;
        }else if ([dict[@"gender"] isEqualToString:@"F"]) {
            preferences.gender = MNGGenderFemale;
        }else{
            preferences.gender = MNGGenderUnknown;
        }
    }
    
    if (dict[@"location"]) {
        if (dict[@"location"][@"lat"] && dict[@"location"][@"lon"]) {
            preferences.location = [[CLLocation alloc]initWithLatitude:[dict[@"location"][@"lat"]floatValue]
                                                             longitude:[dict[@"location"][@"lon"]floatValue]];
        }
    }
    
    return preferences;
}

- (void)mngadssdk_initWithAppId:(CDVInvokedUrlCommand *)command{
    NSArray *arguments = command.arguments;
    NSString *appId = [arguments objectAtIndex:0];
    sdkCallbackId = command.callbackId;
    dispatch_async(dispatch_get_main_queue(), ^{
        [MNGAdsSDKFactory initWithAppId:appId];
        if ([MNGAdsSDKFactory isInitialized]) {
            CDVPluginResult *pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK];
            [self.commandDelegate sendPluginResult:pluginResult callbackId:self->sdkCallbackId];
        }else{
            [MNGAdsSDKFactory setDelegate:self];
        }
    });
}



-(void)MNGAdsSDKFactoryDidFinishInitializing{
    CDVPluginResult *pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK];
    [self.commandDelegate sendPluginResult:pluginResult callbackId:sdkCallbackId];
}


- (void)mngadssdk_isInitialized:(CDVInvokedUrlCommand *)command{
    CDVPluginResult *pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsBool:[MNGAdsSDKFactory isInitialized]];
    [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
}


- (void)mngadssdk_createInterstitial:(CDVInvokedUrlCommand *)command{
    NSArray *arguments = command.arguments;
    interstitialCallbackId = command.callbackId;
    NSString *placementId = [arguments objectAtIndex:0];
    NSString *pref = [arguments objectAtIndex:1];
    BOOL _autoDisplay = [[arguments objectAtIndex:2]boolValue];
    if(interstitialAdsFactory == nil)interstitialAdsFactory = [[MNGAdsSDKFactory alloc]init];
    interstitialAdsFactory.interstitialDelegate = self;
    interstitialAdsFactory.viewController = [self viewController];
    interstitialAdsFactory.placementId = placementId;
    interstitialAdsFactory.clickDelegate = self;
    dispatch_async(dispatch_get_main_queue(), ^{
        bool ok = [self->interstitialAdsFactory createInterstitialWithPreferences:[self preferencesWithString:pref]autoDisplayed:_autoDisplay];
        if (!ok) {
            CDVPluginResult *pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR messageAsString:@"Fail to create"];
            [pluginResult setKeepCallbackAsBool:YES];
            [self.commandDelegate sendPluginResult:pluginResult callbackId:self->interstitialCallbackId];
        }
    });
}

- (void)mngadssdk_showInterstitial:(CDVInvokedUrlCommand *)command{
    if ([interstitialAdsFactory isInterstitialReady]) {
        [interstitialAdsFactory displayInterstitial];
        
        
        CDVPluginResult *pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsString:@"DID_BE_SHOWEN"];
        [pluginResult setKeepCallbackAsBool:YES];
        [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
    }else{
        CDVPluginResult *pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR messageAsString:@"Fail to display interstitial"];
        [pluginResult setKeepCallbackAsBool:YES];
        [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
    }
}


- (void)mngadssdk_debugEnable:(CDVInvokedUrlCommand *)command{
    NSArray *arguments = command.arguments;
    BOOL enabled = [[arguments objectAtIndex:0]boolValue];
    [MNGAdsSDKFactory setDebugModeEnabled:enabled];
}


- (void)mngadssdk_createBanner:(CDVInvokedUrlCommand *)command{
    NSArray *arguments = command.arguments;
    bannerCallbackId = command.callbackId;
    //[placementId,height,position,autoDisplay]
    NSString *placementId = [arguments objectAtIndex:0];
    int mheight = [[arguments objectAtIndex:1]intValue];
    NSString *pref = [arguments objectAtIndex:4];
    if(bannerAdsFactory == nil)bannerAdsFactory = [[MNGAdsSDKFactory alloc]init];
    bannerAdsFactory.bannerDelegate = self;
    bannerAdsFactory.viewController = [self viewController];
    bannerAdsFactory.placementId = placementId;
    CGRect frame;
    CGFloat screenWidth = [self screenWidth];
    if (height == 250) {
        frame = kMNGAdSizeMediumRectangle;
    }else{
        frame = CGRectMake(0, 0, screenWidth, height);
    }
    dispatch_async(dispatch_get_main_queue(), ^{
        if (self->bannerShowen && self->banner != nil) {
            [self->banner removeFromSuperview];
            self->banner = nil;
            self->bannerShowen = NO;
        }
        bool ok = [self->bannerAdsFactory createBannerInFrame:frame withPreferences:[self preferencesWithString:pref]];
        if (!ok) {
            CDVPluginResult *pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR messageAsString:@"Fail to create"];
            [pluginResult setKeepCallbackAsBool:YES];
            [self.commandDelegate sendPluginResult:pluginResult callbackId:self->bannerCallbackId];
        }else{
            self->height = mheight;
            self->position = [arguments objectAtIndex:2];
            self->autoDisplay = [[arguments objectAtIndex:3]boolValue];
        }
    });
    
}

- (void)mngadssdk_showBanner:(CDVInvokedUrlCommand *)command{
    if (banner&&!bannerShowen) {
        [self showBanner];
        CDVPluginResult *pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK];
        [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
    }else{
        CDVPluginResult *pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR];
        [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
    }
}
- (void)mngadssdk_removeBanner:(CDVInvokedUrlCommand *)command{
    dispatch_async(dispatch_get_main_queue(), ^{
        if (self->bannerShowen && self->banner != nil) {
            [self->banner removeFromSuperview];
            self->banner = nil;
            self->bannerShowen = NO;
        }
    });
    CDVPluginResult *pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK];
    [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
    
}


-(void)showBanner{
    if (bannerShowen) {
        return;
    }
    if ([position isEqualToString:@"TOP"]) {
        [self showBannerFixTop];
    }else{
        [self showBannerFixBottom];
    }
    bannerShowen = YES;
}

-(void)showBannerFixTop{
    CGRect frame;
    CGFloat screenWidth = [self screenWidth];
    CGFloat topPadding = 20;
    
    if (@available(iOS 11.0, *)) {
        UIWindow *window = UIApplication.sharedApplication.windows.firstObject;
        topPadding = window.safeAreaInsets.top;
    }
    if (height ==250) {
        frame = CGRectMake((screenWidth - 300)/2, topPadding, 300, height);
    }else{
        frame = CGRectMake(0, topPadding, screenWidth, height);
    }
    banner.frame = frame;
    [self.webView addSubview:banner];
    
}

-(void)showBannerFixBottom{
    CGRect frame;
    CGFloat screenWidth = [self screenWidth];
    CGFloat y = [self screenHeight] - height;
    int width = [self screenWidth];
    CGFloat bottomPadding = 0;
    
    if (@available(iOS 11.0, *)) {
        UIWindow *window = UIApplication.sharedApplication.windows.firstObject;
        bottomPadding = window.safeAreaInsets.bottom;
    }
    if (height ==250) {
        width = 300;
        frame = CGRectMake((screenWidth - width)/2, y - bottomPadding, width, height);
    }else{
        frame = CGRectMake(0, y - bottomPadding, screenWidth, height);
    }
    banner.frame = frame;
    [self.webView addSubview:banner];
    
}

-(void)showBannerScrollTop{
    //
}

-(void)showBannerScrollBottom{
    //
}


#pragma mark - MNGAdsAdapterBannerDelegate

-(void)adsAdapter:(MNGAdsAdapter *)adsAdapter bannerDidLoad:(UIView *)adView preferredHeight:(CGFloat)preferredHeight{
    if (preferredHeight > 0) {
        height = preferredHeight;
    }
    
    banner = adView;
    if (autoDisplay) {
        [self showBanner];
    }
    CDVPluginResult *pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK];
    [self.commandDelegate sendPluginResult:pluginResult callbackId:bannerCallbackId];
}

-(void)adsAdapter:(MNGAdsAdapter *)adsAdapter bannerDidFailWithError:(NSError *)error{
    CDVPluginResult *pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR messageAsString:error.description];
    [self.commandDelegate sendPluginResult:pluginResult callbackId:bannerCallbackId];
}
-(void)adsAdapter:(MNGAdsAdapter *)adsAdapter bannerDidChangeFrame:(CGRect)frame{
    CGFloat width = frame.size.width;
    CGFloat height = frame.size.height;
    banner.frame = CGRectMake(([self screenWidth ]-width)/2, [self screenHeight] - height, width, height);
    [self.webView updateFocusIfNeeded];
    CDVPluginResult *pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK];
    [self.commandDelegate sendPluginResult:pluginResult callbackId:bannerCallbackId];
}
#pragma mark - MNGAdsAdapterInterstitialDelegate

-(void)adsAdapterInterstitialDidLoad:(MNGAdsAdapter *)adsAdapter{
    [[UIApplication sharedApplication]setStatusBarHidden:YES];
    CDVPluginResult *pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsString:@"DID_LOAD"];
    [pluginResult setKeepCallbackAsBool:YES];
    [self.commandDelegate sendPluginResult:pluginResult callbackId:interstitialCallbackId];
}

-(void)adsAdapterInterstitialDisappear:(MNGAdsAdapter *)adsAdapter{
    [[UIApplication sharedApplication]setStatusBarHidden:NO];
    
    CDVPluginResult *pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsString:@"DID_DISAPPEAR"];
    [self.commandDelegate sendPluginResult:pluginResult callbackId:interstitialCallbackId];
}

-(void)adsAdapter:(MNGAdsAdapter *)adsAdapter interstitialDidFailWithError:(NSError *)error{
    CDVPluginResult *pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR messageAsString:error.description];
    [self.commandDelegate sendPluginResult:pluginResult callbackId:interstitialCallbackId];
}

#pragma mark - Click delegate

-(void)adsAdapterAdWasClicked:(MNGAdsAdapter *)adsAdapter {
    
    for (UIView *subv in UIApplication.sharedApplication.keyWindow.subviews) {
        if ([subv isKindOfClass:NSClassFromString(@"MNGSASInterstitialView")]) {
            [subv removeFromSuperview];
        }
    }
}


#pragma mark - LifeCycle


-(void)onAppTerminate{
    NSLog(@"onAppTerminate");
    [interstitialAdsFactory releaseMemory];
    interstitialAdsFactory = nil;
    [bannerAdsFactory releaseMemory];
    bannerAdsFactory = nil;
    [super onAppTerminate];
}
- (void)mngadssdk_onResume:(CDVInvokedUrlCommand *)command{
    [self.webView updateFocusIfNeeded];
    CDVPluginResult *pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK];
    [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
}

-(CGFloat)screenWidth{
    return webView.frame.size.width;
}

-(CGFloat)screenHeight{
    return webView.frame.size.height;
    
}

@end
