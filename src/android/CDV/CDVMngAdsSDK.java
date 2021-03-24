package com.mngads.cordova;

import java.io.ByteArrayOutputStream;

import android.content.res.Resources;
import android.util.Log;
import android.widget.Button;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.location.Location;
import android.util.Base64;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import org.apache.cordova.PluginResult;
import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CordovaWebView;
import org.apache.cordova.CordovaInterface;
import org.json.JSONObject;
import org.json.JSONArray;
import org.json.JSONException;

import com.mngads.util.MNGAdSize;
import com.mngads.util.MNGGender;
import com.mngads.MNGAdsFactory;
import com.mngads.listener.MNGAdsSDKFactoryListener;
import com.mngads.listener.MNGBannerListener;
import com.mngads.listener.MNGInterstitialListener;
import com.mngads.util.MNGFrame;
import com.mngads.util.MNGPreference;

import android.view.Window;


public class CDVMngAdsSDK extends CordovaPlugin implements
        MNGInterstitialListener, MNGBannerListener, MNGAdsSDKFactoryListener {

    private static final boolean CORDOVA_MIN_4 = Integer
            .valueOf(CordovaWebView.CORDOVA_VERSION.split("\\.")[0]) >= 4;

    /**
     * Cordova Actions
     */
    private final String ACTION_INITIALIZE = "mngadssdk_initWithAppId";

    private final String ACTION_CREATE_INTERSTITIAL = "mngadssdk_createInterstitial";
    private final String ACTION_SHOW_INTERSTITIAL = "mngadssdk_showInterstitial";

    private final String ACTION_CREATE_BANNER = "mngadssdk_createBanner";
    private final String ACTION_SHOW_BANNER = "mngadssdk_showBanner";
    private final String ACTION_RM_BANNER = "mngadssdk_removeBanner";

    private final String ACTION_DEBUG_ENABLE = "mngadssdk_debugEnable";

    private final String ACTION_IS_INITIALIZED = "mngadssdk_isInitialized";
    private final String ACTION_ON_RESUME = "mngadssdk_onResume";

    private Window window;


    /**
     * MNGAds Factory to create banner/interstitial Ad
     */
    private MNGAdsFactory mMNGAdsInterstitialAdsFactory;
    private MNGAdsFactory mMNGAdsBannerAdsFactory;

    /**
     * MNGAds Banner view
     */
    private View mMNGAdView;
    /**
     * MNGAds Banner visibility
     */
    private boolean mIsMNGAdVisible;
    private boolean mAutoDisplay;
    private boolean mTopAd;
    private boolean mIsDebug;

    private final String TOP = "TOP";
    /**
     * MNGpreference keys
     */
    public final String PREFERENCE_AGE = "age";
    public final String PREFERENCE_LANGUAGE = "language";
    public final String PREFERENCE_KEYWORD = "keyword";
    public final String PREFERENCE_GENDER = "gender";
    public final String PREFERENCE_GENDER_MALE = "M";
    public final String PREFERENCE_GENDER_FEMALE = "F";
    public final String PREFERENCE_GENDER_UNKONOWN = "U";
    public final String PREFERENCE_LOCATION = "location";
    public final String PREFERENCE_LAT = "lat";
    public final String PREFERENCE_LON = "lon";
    public final static String PREFERENCE_AD_CHOICE_POSITION = "adchoice_position";

    /**
     * excute create interstitial callBack
     */
    private CallbackContext mInterstitialCallBack;
    /**
     * excute create Banner callBack
     */
    private CallbackContext mBannerCallBack;

    /**
     * excute mng initialization callBack
     */
    private CallbackContext mInitializationCallBack;


    private ViewGroup mParentView;


    private final static String TAG = CDVMngAdsSDK.class.getSimpleName();

    // Initialize
    @Override
    public void initialize(CordovaInterface cordova, CordovaWebView webView) {
        super.initialize(cordova, webView);
        window = cordova.getActivity().getWindow();
        window.clearFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);

    }


    /**
     * This is the main method for the MNGAds plugin. All API calls go through
     * here. This method determines the action, and executes the appropriate
     * call.
     *
     * @param action          The action that the plugin should execute.
     * @param options         The input parameters for the action.
     * @param callbackContext The callback context.
     * @return returned if the action is recognized.
     */
    @Override
    public boolean execute(String action, JSONArray options,
                           CallbackContext callbackContext) throws JSONException {

        try {

            switch (action) {
                case ACTION_INITIALIZE:

                    MNGAdsFactory.initialize(cordova.getActivity().getApplication(),
                            options.optString(0));

                    mInitializationCallBack = callbackContext;

                    if (MNGAdsFactory.isInitialized()) {

                        mInitializationCallBack.success();

                    } else {

                        MNGAdsFactory.setMNGAdsSDKFactoryListener(this);

                    }
                    return true;

                case ACTION_CREATE_INTERSTITIAL:
                    executeCreateInterstitialView(options, callbackContext);
                    return true;
                case ACTION_SHOW_INTERSTITIAL:
                    executeShowInterstitialView(callbackContext);
                    return true;

                case ACTION_CREATE_BANNER:
                    executeCreateBannerView(options, callbackContext);
                    return true;
                case ACTION_SHOW_BANNER:
                    executeShowBanner(callbackContext);
                    return true;
                case ACTION_RM_BANNER:
                    executeRMBanner(callbackContext);
                    return true;

                case ACTION_DEBUG_ENABLE:
                    MNGAdsFactory.setDebugModeEnabled(options.optBoolean(0));
                    return true;

                case ACTION_IS_INITIALIZED:
                    callbackContext
                            .success((MNGAdsFactory.isInitialized()) ? 1 : 0);
                    return true;
                case ACTION_ON_RESUME:
                    onResume(callbackContext);
                    return true;

            }
        } catch (Exception e) {

            Log.e(TAG, "Exception in execute" + e.toString());
        }

        return false;
    }


    @Override
    public void onDestroy() {

        cordova.getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {

                if (mMNGAdsInterstitialAdsFactory != null) {
                    /** relasing mng ads factory */
                    mMNGAdsInterstitialAdsFactory.releaseMemory();
                }

                if (mMNGAdsBannerAdsFactory != null) {
                    /** relasing mng ads factory */
                    mMNGAdsBannerAdsFactory.releaseMemory();
                }

            }
        });

        super.onDestroy();
    }

    /**
     * Run to show MNGInterstitial
     *
     * @param callbackContext The callback context.
     */

    private void executeShowInterstitialView(CallbackContext callbackContext) {
// get callBack to be called on interstitial show/fail
        mInterstitialCallBack = callbackContext;

        cordova.getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (mMNGAdsInterstitialAdsFactory!=null && mMNGAdsInterstitialAdsFactory.isInterstitialReady()) {
                    mMNGAdsInterstitialAdsFactory.displayInterstitial();
                    PluginResult pluginResult = new PluginResult(
                            PluginResult.Status.OK, "Interstitial displayed");


                    mInterstitialCallBack.sendPluginResult(pluginResult);
                } else {

                    PluginResult pluginResult = new PluginResult(
                            PluginResult.Status.ERROR, "Failed to display");


                    mInterstitialCallBack.sendPluginResult(pluginResult);
                }
            }
        });
    }

    /**
     * Run to create MNGInterstitial
     *
     * @param options         The JSONArray representing input parameters.
     * @param callbackContext The callback context.
     */
    private void executeCreateInterstitialView(final JSONArray options,
                                               CallbackContext callbackContext) {

        // get callBack to be called on interstitial load/fail/disappear
        mInterstitialCallBack = callbackContext;
        // get placement id
        final String placementId = options.optString(0);
        // get user preference
        final MNGPreference preference = getPreference(options.optString(1));
        final boolean mAutoDisplay = options.optBoolean(2);
        // excute create MNG Ads interstitial on the UI Thread
        cordova.getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                // instantiate mng Ads Factory
                if (mMNGAdsInterstitialAdsFactory == null) {
                    mMNGAdsInterstitialAdsFactory = new MNGAdsFactory(cordova
                            .getActivity());
                }

                // set MNG placement Id
                mMNGAdsInterstitialAdsFactory.setPlacementId(placementId);
                // set MNG listener
                mMNGAdsInterstitialAdsFactory
                        .setInterstitialListener(CDVMngAdsSDK.this);
                // create interstitial with preference
                if (MNGAdsFactory.isInitialized() && !mMNGAdsInterstitialAdsFactory.isBusy()){
                    mMNGAdsInterstitialAdsFactory.loadInterstitial(preference, mAutoDisplay);
                } else {

                    // mMNGAdsInterstitialAdsFactory is not initialized or is
                    // busy
                    PluginResult pluginResult = new PluginResult(
                            PluginResult.Status.ERROR, "Failed to create");

                    pluginResult.setKeepCallback(true);

                    mInterstitialCallBack.sendPluginResult(pluginResult);
                }

            }
        });
    }



    /**
     * Run to create MNGBanner
     *
     * @param options         The JSONArray representing input parameters.
     * @param callbackContext The callback context.
     */
    private void executeCreateBannerView(final JSONArray options,
                                         final CallbackContext callbackContext) {

        // get callBack to be called on banner load/fail
        mBannerCallBack = callbackContext;
        // get placement id
        final String placementId = options.optString(0);
        // get device width
        final int widthPx = getScreenWidth(cordova.getActivity());
        // get requested ad height
        final int heightDp = options.optInt(1);
        // convert device width from pix to dp
        final int widthDp = (int) convertPixelsToDp(widthPx,
                cordova.getActivity());
        // get user preference
        final MNGPreference preference = getPreference(options.optString(4));
        // excute create MNG Ads banner on the UI Thread
        cordova.getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                // remove MNG Ad view from screen
                if (mMNGAdView != null) {

                    ViewGroup adParent = (ViewGroup) (mMNGAdView.getParent());
                    if (adParent != null) {
                        adParent.removeView(mMNGAdView);
                    }

                    mMNGAdView = null;
                    mIsMNGAdVisible = false;

                }

                // instantiate mng Ads Factory
                if (mMNGAdsBannerAdsFactory == null) {
                    mMNGAdsBannerAdsFactory = new MNGAdsFactory(cordova
                            .getActivity());
                }
                // set MNG placement Id
                mMNGAdsBannerAdsFactory.setPlacementId(placementId);
                // set MNG listener
                mMNGAdsBannerAdsFactory.setBannerListener(CDVMngAdsSDK.this);
                // create banner with preference


             
                if (MNGAdsFactory.isInitialized() && !mMNGAdsBannerAdsFactory.isBusy()){
                   if(heightDp==250)
                  {
                    mMNGAdsBannerAdsFactory.loadBanner(MNGAdSize.MNG_MEDIUM_RECTANGLE, preference);
                  }
                 else
                  {
                    mMNGAdsBannerAdsFactory.loadBanner(new MNGFrame(widthDp, heightDp), preference);
                  }

                    // get Ad position
                    String position = options.optString(2);
                    mTopAd = position.equals(TOP);
                    // auto display Ad or not
                    mAutoDisplay = options.optBoolean(3);
                } else{

                    // mMNGAdsBannerAdsFactory is not initialized or is busy
                    PluginResult pluginResult = new PluginResult(
                            PluginResult.Status.ERROR, "Failed to create");

                    pluginResult.setKeepCallback(true);

                    mBannerCallBack.sendPluginResult(pluginResult);
                }

            }
        });

    }

    /**
     * Run to show Banner
     *
     * @param callbackContext The callback context.
     */
    private void executeShowBanner(CallbackContext callbackContext) {

        // check that MNG Ad view not null and not visible
        if (mMNGAdView != null && !mIsMNGAdVisible) {

            // display MNG Banner on screen
            showBanner();

            // call success
            callbackContext.success();

        } else {
            // call failed to display banner on screen
            callbackContext.error("Failed to show");
        }

    }

    /**
     * Run to remove Banner
     *
     * @param callbackContext The callback context.
     */
    private void executeRMBanner(CallbackContext callbackContext) {

    ViewGroup wvParentView = (ViewGroup) getWebView().getParent();
        if (wvParentView != null && mMNGAdView != null) {

             cordova.getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
            wvParentView.removeView(mMNGAdView);
            }
        });
            // call success
            callbackContext.success();

        } else {
            // call failed to display banner on screen
            callbackContext.error("Failed to remove");
        }

    }


    /**
     * Resume  MNG Banner on screen
     */
 
    private void onResume(CallbackContext callbackContext) {
    ViewGroup wvParentView = (ViewGroup) getWebView().getParent();
        if (wvParentView != null ) {
            wvParentView.requestFocus();
            callbackContext.success();
        } else {
            callbackContext.error("Failed to resume");
        }

    }



    /**
     * Display MNG Banner on screen
     */
    private void showBanner() {
        // show MNG Ads banner
        cordova.getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {

                // get Cordova web view parent
                ViewGroup wvParentView = (ViewGroup) getWebView().getParent();
                if (wvParentView != null) {
                    wvParentView.removeView(getWebView());
                }

                // create new content view
                mParentView = new LinearLayout(webView.getContext());
                ((LinearLayout) mParentView)
                        .setOrientation(LinearLayout.VERTICAL);
                ((LinearLayout) mParentView).setGravity(Gravity.CENTER);
                mParentView.setLayoutParams(new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.MATCH_PARENT));

                // add web view to the new content view
                getWebView().setLayoutParams(
                        new LinearLayout.LayoutParams(
                                ViewGroup.LayoutParams.MATCH_PARENT,
                                ViewGroup.LayoutParams.MATCH_PARENT, 1.0F));
                mParentView.addView(getWebView());

                // add MNG Ads Banner to the screen
                if (mTopAd) {
                    mParentView.addView(mMNGAdView, 0);
                } else {
                    mParentView.addView(mMNGAdView);
                }

                // display the new content on screen
                cordova.getActivity().setContentView(mParentView);
                mParentView.requestFocus();
                mIsMNGAdVisible = true;

            }
        });
    }

    /**
     * get cordova webview
     */
    private View getWebView() {

        if (CORDOVA_MIN_4) {
            try {

                return (View) webView.getClass().getMethod("getView")
                        .invoke(webView);
            } catch (Exception e) {
                return (View) webView;
            }

        } else {

            return (View) webView;

        }
    }

    /**
     * MNG Preference from String
     */
    private MNGPreference getPreference(String preferenceString) {

        if (preferenceString.isEmpty())
            return null;

        MNGPreference preference = new MNGPreference();

        try {

            JSONObject preferenceObject = new JSONObject(preferenceString);

            // get user age
            if (preferenceObject.has(PREFERENCE_AGE)) {
                preference.setAge(preferenceObject.getInt(PREFERENCE_AGE));
            }

            // get keyword
            if (preferenceObject.has(PREFERENCE_KEYWORD)) {
                preference.setKeyword(preferenceObject
                        .getString(PREFERENCE_KEYWORD));
            }

            // get user language
            if (preferenceObject.has(PREFERENCE_LANGUAGE)) {
                preference.setLanguage(preferenceObject
                        .getString(PREFERENCE_LANGUAGE));
            }

            // get user location
            if (preferenceObject.has(PREFERENCE_LOCATION)) {
                Location location = new Location("CDV");
                JSONObject jsonLocation = preferenceObject
                        .getJSONObject(PREFERENCE_LOCATION);

                location.setLatitude(jsonLocation.getDouble(PREFERENCE_LAT));
                location.setLongitude(jsonLocation.getDouble(PREFERENCE_LON));
                preference.setLocation(location,1,cordova.getActivity());

            }

            // get user gender
            if (preferenceObject.has(PREFERENCE_GENDER)) {
                String gender = preferenceObject.getString(PREFERENCE_GENDER);
                switch (gender) {
                    case PREFERENCE_GENDER_FEMALE:
                        preference.setGender(MNGGender.MNGGenderFemale);
                        break;
                    case PREFERENCE_GENDER_MALE:
                        preference.setGender(MNGGender.MNGGenderMale);
                        break;
                    default:
                        preference.setGender(MNGGender.MNGGenderUnknown);
                        break;
                }
             }



            if (preferenceObject.has(PREFERENCE_AD_CHOICE_POSITION)){
                preference.setAdChoicePosition(preferenceObject.getInt(PREFERENCE_AD_CHOICE_POSITION));
            }

        } catch (JSONException ex) {

            Log.e(TAG, ex.toString());

            return null;

        }

        return preference;

    }



    private int getScreenWidth(Context context) {
        WindowManager wm = (WindowManager) context
                .getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        DisplayMetrics metrics = new DisplayMetrics();
        display.getMetrics(metrics);
        return metrics.widthPixels;

    }

    @Override
    public void bannerDidLoad(View adView, int preferredHeightDP) {

        mMNGAdView = adView;

        if (mAutoDisplay) {
            showBanner();
        }
        mBannerCallBack.success();

    }

    @Override
    public void bannerDidFail(Exception adsException) {
        mBannerCallBack.error(adsException.toString());

    }

    @Override
    public void bannerResize(MNGFrame frame) {
    }

    @Override
    public void interstitialDidLoad() {
        PluginResult pluginResult = new PluginResult(PluginResult.Status.OK,
                "DID_LOAD");
        pluginResult.setKeepCallback(true);
        mInterstitialCallBack.sendPluginResult(pluginResult);
    }

    @Override
    public void interstitialDidFail(Exception adsException) {
        mInterstitialCallBack.error(adsException.toString());
    }

    @Override
    public void interstitialDisappear() {

        mInterstitialCallBack.success("DID_DISAPPEAR");
    }

    @Override
    public void onMNGAdsSDKFactoryDidResetConfig() {

    }

    @Override
    public void onMNGAdsSDKFactoryDidFinishInitializing() {

        mInitializationCallBack.success();

    }

    @Override
    public void onMNGAdsSDKFactoryDidFailInitialization(Exception e) {

    }

    private static float convertPixelsToDp(float px, Context context) {
        Resources resources = context.getResources();
        DisplayMetrics metrics = resources.getDisplayMetrics();
        return px / (metrics.densityDpi / 160f);
    }



}
