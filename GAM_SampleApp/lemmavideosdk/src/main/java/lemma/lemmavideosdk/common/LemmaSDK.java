package lemma.lemmavideosdk.common;

import android.content.Context;
import android.content.pm.ApplicationInfo;

import com.liulishuo.filedownloader.FileDownloader;

public class LemmaSDK {

    private static boolean isConfigured;

    public static String getVersion() {
        return "1.2.4";
    }

    public static String getApplicationName(Context context) {
        ApplicationInfo applicationInfo = context.getApplicationInfo();
        int stringId = applicationInfo.labelRes;
        return stringId == 0 ? applicationInfo.nonLocalizedLabel.toString() : context.getString(stringId);
    }

    public static void init(Context context, Boolean useInternalStorage) {
        String rootDir = "/LemmaSDK/"; //"/"+getApplicationName(context)+"/LemmaSDK/";
        if (!useInternalStorage) {
            rootDir = "/.LemmaSDK/";
        }
        init(context, rootDir, useInternalStorage);
    }

    public static void init(Context context, String rootDirectory, Boolean useInternalStorage) {
        if (isConfigured) {
            return;
        }
        isConfigured = true;
        FileDownloader.setup(context);
        LMUtils.setUpDirectories(context, rootDirectory, useInternalStorage);
    }
}
