package lemma.lemmavideosdk.vast.manager;

import android.content.Context;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.telephony.TelephonyManager;
import android.util.DisplayMetrics;
import android.view.WindowManager;
import android.webkit.WebSettings;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import lemma.lemmavideosdk.common.LMLog;

public class LMDeviceInfo {

    final private String TAG = "PMDeviceInfo";

    private String androidId = null;
    private String advertisingID = null;
    private Boolean lmtEnabled = null;

    private String countryCode = null;
    private String carrierName = null;
    private String acceptLanguage = null;


    private String make = null;
    private String model = null;
    private String osName = null;
    private String osVersion = null;

    private String screenResolution = null;

    private String userAgent = null;
    private String currentTimeZone = null;

    private Context context;

    /**
     * Constructor.
     * It initializes all the device info attributes as a member variables. These can be directly
     * accessed using getter methods.
     *
     * @param context context
     */
    public LMDeviceInfo(@NonNull Context context) {

        this.context = context;
        init(context);
    }

    /**
     * Method is used to generate the Device information from context and
     * assign it to specific reference variable of the class
     *
     * @param context android context
     */
    private void init(Context context) {

        updateAdvertisingIdInfo();

        androidId = getUDIDFromContext(context);

        // Get the carrier name
        TelephonyManager telephonyManager = ((TelephonyManager) context
                .getSystemService(Context.TELEPHONY_SERVICE));
        if (telephonyManager != null) {
            if (telephonyManager.getPhoneType() != TelephonyManager.PHONE_TYPE_CDMA) {
                String str = telephonyManager.getNetworkCountryIso();
                if (str != null && str.length() <= 0) {
                    str = telephonyManager.getSimCountryIso();
                }

                if (str != null && str.length() > 0) {
                    Locale locale = new Locale(Locale.getDefault().getLanguage(), str);
                    countryCode = locale.getISO3Country();
                }
            }

            // Get the network carrier name
            carrierName = telephonyManager.getNetworkOperatorName();
        }

        // Get the device country code using local and accept language
        acceptLanguage = Locale.getDefault().toString();

        // Get the device ODIN number
        make = Build.MANUFACTURER;
        model = Build.MODEL;
        osName = "Android";
        osVersion = Build.VERSION.RELEASE;

        // Fetch screen width-height
        DisplayMetrics displaymetrics = new DisplayMetrics();
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);

        if (null != wm) {
            wm.getDefaultDisplay().getMetrics(displaymetrics);
            screenResolution = displaymetrics.widthPixels + "x" + displaymetrics.heightPixels;
        }

        // Fetch user agent
        asyncFetchUserAgentFromWebView(context);

        // Get the system time and time zone
        Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("GMT"),
                Locale.getDefault());
        Date currentLocalTime = calendar.getTime();

        DateFormat date = new SimpleDateFormat("ZZZZZ", Locale.getDefault());
        currentTimeZone = date.format(currentLocalTime);
    }

    public DEVICE_ID_TYPE getAndroidIdType(boolean aidEnabled) {
        return aidEnabled ? DEVICE_ID_TYPE.ADVERTISING_ID : DEVICE_ID_TYPE.ANDROID_ID;
    }

    public String getAndroidId() {
        return androidId;
    }

    public String getAdvertisingID() {
        if (advertisingID == null) {
            advertisingID = AdvertisingIdClient.getSavedAndroidAid(context);
        }
        return advertisingID;
    }

    public String getCountryCode() {
        return countryCode;
    }

    public String getCarrierName() {
        return carrierName;
    }

    public String getAcceptLanguage() {
        return acceptLanguage;
    }

    public String getMake() {
        return make;
    }

    public String getModel() {
        return model;
    }

    public String getOsName() {
        return osName;
    }

    public String getOsVersion() {
        return osVersion;
    }

    public String getScreenResolution() {
        return screenResolution;
    }


    /**
     * Returns user agent if this class already fetched it else return using
     * System.getProperty("http.agent").
     * Also see getUserAgent(<Context>)
     *
     * @return user agent
     */
    public String getUserAgent() {
        if (userAgent == null) {
            // Return the user agent from system properties only for first time
            String ua;
            try {
                ua = System.getProperty("http.agent");
            } catch (Exception e) {
                ua = "";
            }
            return ua;
        } else {
            return userAgent;
        }
    }

    /**
     * Returns user agent from WebView if already fetched else return using
     * System.getProperty("http.agent"). And in parallel starts fetching user agent from WebView in
     * worker thread. It will be saved in member variable and will be used later.
     *
     * @param context Activity context
     */
    private void asyncFetchUserAgentFromWebView(final Context context) {
        if (userAgent == null) {

            // Delegate the UA in main thread from worker thread.
            // It will not block the UI thread on app launch.
            new Thread(new Runnable() {
                @Override
                public void run() {

                    new Handler(Looper.getMainLooper()).post(new Runnable() {
                        @Override
                        public void run() {
                            // this will run in the main thread
                            try {
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                                    userAgent = WebSettings.getDefaultUserAgent(context);
                                }
                            } catch (Exception e) {
                                LMLog.e(TAG, e.getLocalizedMessage());
                            }

                        }
                    });

                }
            }).start();
        }
    }

    /**
     * Method is used to get current datetime in formated String
     * yyyy-MM-dd HH:mm:ss
     *
     * @return return date of type String
     */
    public String getCurrentTime() {
        Calendar c = Calendar.getInstance();

        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US);
        return df.format(c.getTime());
    }

    public String getCurrentTimeZone() {
        return currentTimeZone;
    }

    public Boolean getLmtEnabled() {
        if (lmtEnabled == null) {
            lmtEnabled = AdvertisingIdClient.getSavedLimitedAdTrackingState(context);
        }
        return lmtEnabled;
    }

    public int getOrientation() {
        return context.getResources().getConfiguration().orientation;
    }


    private String getUDIDFromContext(Context context) {
        String deviceId = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
        if (deviceId == null) {
            deviceId = "";
        }
        return deviceId;
    }

    /**
     * Method is used to update advertising Id from AdvertisingIdClient class
     */
    public void updateAdvertisingIdInfo() {

        new Thread() {
            @Override
            public void run() {
                AdvertisingIdClient.AdInfo idInfo;
                try {
                    idInfo = AdvertisingIdClient.getAdvertisingIdInfo(context);
                    if (idInfo != null) {
                        advertisingID = idInfo.getId();
                        lmtEnabled = idInfo.isLimitAdTrackingEnabled();
                    }
                } catch (Exception e) {
                    LMLog.e(TAG, e.getLocalizedMessage());
                }
            }
        }.start();
    }

    /**
     * Enum to denote the device type.
     */
    public enum DEVICE_ID_TYPE {
        ANDROID_ID("3"), ADVERTISING_ID("9");


        private final String value;

        DEVICE_ID_TYPE(String val) {
            this.value = val;
        }

        public String getValue() {
            return value;
        }
    }
}
