var argscheck = require('cordova/argscheck'),
    utils = require('cordova/utils'),
    exec = require('cordova/exec');

var MngAdsSDK = function() {
    this.serviceName = "MngAdsSDK";
};


// MngAdsSDK

/**
 * Initialize the MngAdsSDK.
 *  @param: appID from dashboard
 */
MngAdsSDK.prototype.initWithAppId = function(appID,successCallback) {
    if (typeof(appID) != 'string') appID = '';
    exec(successCallback, null, this.serviceName, 'mngadssdk_initWithAppId', [appID]);
};

/**
 * Create interstitial.
 *  @param: placementId
 *  @param: preferences: (gender, location, keyword...)
 *  @param: successCallback: this callback is called when interstitial did load or did disappear
 *  @param: failureCallback: this callback is called when Factory failed to create interstitial (isBusy,worong placmentID,No ad,Timeout ...)
 */
MngAdsSDK.prototype.createInterstitial = function(placementId,preferences,successCallback, failureCallback, autoDisplay) {
    if (typeof(placementId) != 'string') placementId = '';
    if (typeof(preferences) != 'object') preferences = '';
    exec(successCallback, failureCallback, this.serviceName, 'mngadssdk_createInterstitial', [placementId,preferences,autoDisplay]);
};

/**
 * Show interstitial.
 *  @param: successCallback: this callback is called when interstitial showen
 *  @param: failureCallback: this callback is called when Factory failed to show interstitial
 */
MngAdsSDK.prototype.showInterstitial = function(successCallback, failureCallback) {
    exec(successCallback, failureCallback, this.serviceName, 'mngadssdk_showInterstitial', []);
};

/**
 * Create banner.
 *  @param: placementId
 *  @param: height: requested height (dp for android and pt for iOS)
 *  @param: position: TOP or BOTTOM
 *  @param: autoDisplay: if autoDisplay == false, use MngAdsSDK.prototype.showBanner to show it
 *  @param: preferences: (gender, location, keyword...)
 *  @param: successCallback: this callback is called when banner did load
 *  @param: failureCallback: this callback is called when Factory failed to create banner (isBusy,worong placmentID,No ad,Timeout ...)
 */
MngAdsSDK.prototype.createBanner = function(placementId,height,position,autoDisplay,preferences,successCallback, failureCallback) {
    if (typeof(placementId) != 'string') placementId = '';
    if (typeof(preferences) != 'object') preferences = '';
    exec(successCallback, failureCallback, this.serviceName, 'mngadssdk_createBanner', [placementId,height,position,autoDisplay,preferences]);
};

MngAdsSDK.prototype.createInfeed = function(placementId,preferences,successCallback, failureCallback) {
    if (typeof(placementId) != 'string') placementId = '';
    if (typeof(preferences) != 'object') preferences = '';
    exec(successCallback, failureCallback, this.serviceName, 'mngadssdk_createInfeed', [placementId,preferences]);
};

/**
 * Show banner.
 *  @param: successCallback: this callback is called when banner did be showen
 *  @param: failureCallback: this callback is called when Factory failed to show banner (no banner already loaded, banner already showen ...)
 */
MngAdsSDK.prototype.showBanner = function(successCallback, failureCallback) {
    exec(successCallback, failureCallback, this.serviceName, 'mngadssdk_showBanner', []);
};

MngAdsSDK.prototype.removeBanner = function(successCallback, failureCallback) {
    exec(successCallback, failureCallback, this.serviceName, 'mngadssdk_removeBanner', []);
};

MngAdsSDK.prototype.onResume = function(successCallback, failureCallback) {
    exec(successCallback, failureCallback, this.serviceName, 'mngadssdk_onResume', []);
};

MngAdsSDK.prototype.setDebugMode = function(enabled) {
    exec(null, null, this.serviceName, 'mngadssdk_debugEnable', [enabled]);
};

MngAdsSDK.prototype.isInitialized = function(successCallback) {
    exec(successCallback, null, this.serviceName, 'mngadssdk_isInitialized', []);
};


module.exports = MngAdsSDK;
