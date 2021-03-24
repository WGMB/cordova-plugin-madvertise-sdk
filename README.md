# BlueStack SDK - Cordova plugin

- Our single BlueStack SDK Cordova plugin can be used for Ios and Android Apps

- You can see explanation for native [BlueStack Ios](https://bitbucket.org/mngcorp/mngads-demo-ios/wiki/setup) or [BlueStack Android](https://bitbucket.org/mngcorp/mngads-demo-android/wiki/setup) SDKs. 

[TOC]


## Pre-Requisites

**For Android : Init cordova-plugin-androidx**

Make sure you have this plugin included in your project.

```xml
cordova plugin add cordova-plugin-androidx

```

## I.Integration Plugin


### Step 1: Download Plugin

Just download or clone [BlueStack SDK Cordova plugin](https://bitbucket.org/mngcorp/cordova-madvertive-mediation/src) repository.

### Step 2 - For Android : Adding Ad Networks 

Before adding the plugin to your Cordova application, you have to add the libraries that you wish to use by following the steps described below :
 
> build.gradle file path : **mngads-private-cordova-plugin/mng-ads-sdk-cordova-plugin/src/android/gradle/build.gradle**

build.gradle file must contain dependencies define with Madvertise publisher team. See [Android SDK Integration](https://bitbucket.org/mngcorp/mngads-demo-android/wiki/setup#markdown-header-step-1-installation-using-gradle)



### Step 2 - For IOS : Initial setup for iOS
  **Adding Ad Networks**

Before adding the plugin to your Cordova application, you have to add the libraries that you wish to use by following the steps described below :
 
NB: this need to be set before adding the plugin to your app.
First you will need to have cocoapods installed (you can follow the instructions here)
Open the plugin.xml, under <platform name="ios"> , you will notice several frameworks with type podspec being linked, here you can chose which adnetworks you will be working with (delete those you have no need for), you can also edit the desired version of mngads you which to work with.

```xml
<podspec>
   <pods use-frameworks="true">
                    <pod name="BlueStack-SDK/MAdvertiseLocation" spec=">=3.2.0" />
                    <pod name="BlueStack-SDK/Google-Mobile-Ads-SDK" spec=">=3.2.0" />
                    <pod name="BlueStack-SDK/FlurryAds" spec=">=3.2.0" />
                    <pod name="BlueStack-SDK/mopub-ios-sdk" spec=">=3.2.0" />
                    <pod name="BlueStack-SDK/AppLovinSDK" spec=">=3.2.0" />
                    <pod name="BlueStack-SDK/OguryAds" spec=">=3.2.0" />
                    <pod name="BlueStack-SDK/Smart-Display-SDK" spec=">=3.2.0" />
                    <pod name="BlueStack-SDK/AmazonPublisherServicesSDK" spec=">=3.2.0" />
                    <pod name="BlueStack-SDK/FBAudienceNetwork" spec=">=3.2.0" />
                </pods>
           </podspec>
           
```

once everything is set to your preferences, you ll only need to add the plugin to your app, no further setup required (adding the plugin using cordova plugin add PATH/TO/MNGADS/PLUGIN will download and set up everything you will need for ios) just make sure to use xcworkspace and not xcodeproj once the installation is completed.

if you use  DFP Ad Network : 
```xml
 <pod name="BlueStack-SDK/Google-Mobile-Ads-SDK" spec=">=3.2.0" />
```
**Important :You should note that it is mandatory that you add to your plugin.xml file:**

```xml
 <config-file target="*-Info.plist" parent="GADIsAdManagerApp">
            <true/>
        </config-file>
```

### Step 3: Adding Plugin

1. Open your command line tool and navigate to your existing cordova project's directory.

2. Add the MAdvertise CMP cordova plugin with the following command :

```bash
$ cordova plugin add PATH_TO_MNGADS_PLUGIN_ON_YOUR_COMPUTER/mng-ads-sdk-cordova-plugin
```

## II.Implementation SDK

You find an example of implementation on [www] directory


### Initializing the SDK

- You must set the App Id by platform provided by Bluestack team.

```javascript
mngadsAppId: function() {
return (device.platform == "iOS")?'YOUR_IOS_APPID':'YOUR_ANDROID_APPID';
}
```

- You have to init SDK :

```javascript
initMngAds: function() {
this.mngAds = new MngAdsSDK();
if (!this.mngAds ) { alert( 'mngAds plugin not ready' ); return; }
this.mngAds.initWithAppId(this.mngadsAppId());
}
```

### Enabling debug mode

To enbale debug mode you need to set debug mode to true :

```javascript
this.mngAds.setDebugMode(true);
```

### Verify Your Integration

To verify if the SDK is fully initialized you have to call isInitialized():

```javascript
 this.mngAds.isInitialized(this.isInitialized);
```
*Here example :*

```javascript 
...
this.mngAds.isInitialized(this.isInitialized);
...
```

```javascript       
    isInitialized: function (ok) {
        if (ok == true) {
            console.log("MNGAds is initialized");
        } else {
            console.log("MNGAds is not initialized");
        }
    },
```

### Focus Reset

Override the onResume() method to reset the actual flow of current focus :

*Here example :*

```javascript 
...
function onDeviceReady() {
        document.addEventListener("resume", onResume, false);
}
    
function onResume() {
this.mngAds.onResume(this.adSuccess, this.adError);
}
...
```

## III.Select an ad format

### Banner / Medium Rectangle

##### Create Banner / Medium Rectangle

- You must set the placementId provided by mngAds team.
- You must set (size = 50) for Banner and (size = 250) for Medium Rectangle.

```
/**
* Create banner.
*  @param: placementId
*  @param: height: requested height (dp for android and pt for iOS)
*  @param: position: TOP or BOTTOM
*  @param: autoDisplay: if autoDisplay == false, use MngAdsSDK.prototype.showBanner to show it
*  @param: preferences: (location, keyword...)
*  @param: successCallback: this callback is called when banner did load
*  @param: failureCallback: this callback is called when Factory failed to create banner (isBusy,worong placmentID,No ad,Timeout ...)
*/
createBanner: function(id,size,position) {
var preferences = "{\
\"age\": \"25\",\
\"language\": \"fr\",\
\"keyword\": \"brand=myBrand;category=sport\",\
\"location\": {\
\"lat\": \"48.876\",\
\"lon\": \"10.453\"\
}\
}";
document.getElementById("status-ads").innerHTML="waiting "+device.platform;
// Set your placementId.
var placementId = '/'+this.mngadsAppId()+'/homebanner';
this.mngAds.createBanner(placementId,size,position,true,preferences,this.adSuccess,this.adError)
},
```
##### Remove Banner / Medium Rectangle

If you like to remove Banner / Medium Rectangle, you have to call removeBanner method:

```
this.mngAds.removeBanner(this.adSuccess, this.adError);
```

### Interstitial

##### Make a request

You must set the placementId provided by mngAds team.

```
#
/**
* Create interstitial.
*  @param: placementId
*  @param: preferences: (location, keyword...)
*  @param: successCallback: this callback is called when interstitial did load or did disappear
*  @param: failureCallback: this callback is called when Factory failed to create interstitial (isBusy,worong placmentID,No ad,Timeout ...)
*  @param: autoDisplay: to chose if the interstitial will be displayed automatically
*/
createInterstitial: function(autoDisplay) {
var preferences = "{\
\"age\": \"25\",\
\"language\": \"fr\",\
\"keyword\": \"brand=myBrand;category=sport\",\
\"location\": {\
\"lat\": \"48.876\",\
\"lon\": \"10.453\"\
}\
}";
document.getElementById("status-ads").innerHTML="waiting "+device.platform;
// Set your placementId.
var placementId = '/'+this.mngadsAppId()+'/interstitial';
this.mngAds.createInterstitial(placementId,preferences,this.adSuccess,this.adError,autoDisplay);

},
```

##### Show interstitial

If you create an interstitial with autoDisplay = false, you have to call showInterstitial

```
#!javascript
/**
* Show interstitial.
*  @param: successCallback: this callback is called when interstitial showen
*  @param: failureCallback: this callback is called when Factory failed to show interstitial
*/

showInterstitial: function() {
document.getElementById("status-ads").innerHTML="waiting Interstitial " ;
this.mngAds.showInterstitial(function(message)
{
document.getElementById("status-ads").innerHTML="Interstitial displayed" ;
},
function(message) {
document.getElementById("status-ads").innerHTML="Fail to display " ;
});

},

```




[www]:https://bitbucket.org/mngcorp/mngads-private-cordova-plugin/src/HEAD/www/?at=master
[plugin.xml]:https://bitbucket.org/mngcorp/mngads-cordova-plugin/src/HEAD/mng-ads-sdk-cordova-plugin/plugin.xml?at=master&fileviewer=file-view-default
