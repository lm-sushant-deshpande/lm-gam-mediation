package lemma.lemmavideosdk.vast.manager;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;

import lemma.lemmavideosdk.common.LMLog;

public class LMAppInfo {

    private static final String TAG = "PMAppInfo";

    private String appName;
    private String packageName;
    private String appVersion;

    /**
     * PMAppInfo constructor to generate
     * application info and initialize the params with specific value
     *
     * @param context android context
     */
    public LMAppInfo(final Context context) {

        // Get the application name and version number
        try {
            PackageManager manager = context.getPackageManager();
            PackageInfo info;
            info = manager.getPackageInfo(context.getPackageName(),
                    0);
            appName = info.applicationInfo.loadLabel(manager).toString();
            packageName = context.getPackageName();
            appVersion = info.versionName;
        } catch (Exception e) {
            LMLog.e(TAG, e.getLocalizedMessage());
        }
    }

    public String getAppName() {
        return appName;
    }

    public String getPackageName() {
        return packageName;
    }

    public String getAppVersion() {
        return appVersion;
    }
}
