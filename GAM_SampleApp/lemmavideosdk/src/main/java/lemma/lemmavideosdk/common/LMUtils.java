package lemma.lemmavideosdk.common;

import android.app.Application;
import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;
import java.util.zip.CRC32;

import lemma.lemmavideosdk.vast.VastBuilder.AdI;
import lemma.lemmavideosdk.vast.VastBuilder.LinearAd;
import lemma.lemmavideosdk.vast.VastBuilder.MediaFile;
import lemma.lemmavideosdk.vast.VastBuilder.Vast;


public class LMUtils {

    private static final SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
    static String STORAGE_PATH;
    static String LEMMA_ROOT_PATH;
    //Note : Always point to production server, while testing point to staging
    static String PRODUCTION_SERVER_URL = "http://lemmadigital.com";
    public static String SERVER_URL = PRODUCTION_SERVER_URL;
    private static String TAG = "LMUtils";
    private static SimpleDateFormat dateFormatter = null;
    private static SimpleDateFormat partDayFormatter = null;
    private static List<SimpleDateFormat> knownPatterns = new ArrayList<SimpleDateFormat>();

    public static final String ORTB_VERSION = "2.3";

    public static final String RESPONSE_HEADER_CONTENT_TYPE_JSON = "application/json";

    static final String KEY_DEBUG = "DEBUG";
    static final String KEY_ID = "id";
    static final String KEY_AT = "at";
    static final String KEY_CURRENCY = "cur";
    static final String KEY_IMPRESSION = "imp";
    static final String KEY_APP = "app";
    static final String KEY_DEVICE = "device";
    static final String KEY_USER = "user";
    static final String KEY_REGS = "regs";
    static final String KEY_EXTENSION = "ext";
    static final String KEY_DISPLAY_MANAGER = "displaymanager";
    static final String KEY_DISPLAY_MANAGER_VERSION = "displaymanagerver";
    static final String KEY_POSITION = "pos";
    static final String KEY_W = "w";
    static final String KEY_H = "h";
    static final String KEY_FORMAT = "format";
    static final String KEY_API = "api";
    static final String KEY_BANNER = "banner";
    static final String KEY_VIDEO = "video";
    static final String KEY_INTERSTITIAL = "instl";
    static final String KEY_NAME = "name";
    static final String KEY_BUNDLE = "bundle";
    static final String KEY_DOMAIN = "domain";
    static final String KEY_STORE_URL = "storeurl";
    static final String KEY_CATEGORY = "cat";
    static final String KEY_VERSION = "ver";
    static final String KEY_PAID = "paid";
    static final String KEY_PUBLISHER = "publisher";
    static final String KEY_GEO = "geo";
    static final String KEY_GEO_TYPE = "type";
    static final String KEY_LMT = "lmt";
    static final String KEY_IFA = "ifa";
    static final String KEY_WIFI = "wifi";
    static final String KEY_CELLULAR = "cellular";
    static final String KEY_CONNECTION_TYPE = "connectiontype";
    static final String KEY_CARRIER = "carrier";
    static final String KEY_JS = "js";
    static final String KEY_USER_AGENT = "ua";
    static final String KEY_MAKE = "make";
    static final String KEY_MODEL = "model";
    static final String KEY_OS = "os";
    static final String KEY_OS_VERSION = "osv";
    static final String KEY_PXRATIO = "pxratio";

    static final String KEY_LANGUAGE = "language";
    static final String KEY_DEVICE_TYPE = "devicetype";
    static final String KEY_LATITUDE = "lat";
    static final String KEY_LONGITUDE = "lon";
    static final String KEY_KEYWORDS = "keywords";
    static final String KEY_COPPA = "coppa";

    // Response parsing key
    static final String KEY_BID = "bid";
    static final String KEY_PRICE = "price";
    static final String KEY_ADM = "adm";
    static final String KEY_CREATIVE_ID = "crid";
    static final String KEY_DEAL_ID = "dealid";
    static final String KEY_NURL = "nurl";
    static final String KEY_SUMMARY = "summary";
    static final String KEY_SECURE = "secure";
    static final String KEY_WIDTH = "w";
    static final String KEY_HEIGHT = "h";
    static final String VALUE_PLATFORM = "inapp";

    static final String VALUE_DISPLAY_MANAGER = "Lemma_Ads_SDK";


    public static final String KEY_GDPR = "gdpr";
    public static final String KEY_GDPR_CONSENT = "consent";


    public static final String KEY_VIDEO_MIN_DURATION = "minduration";
    public static final String KEY_VIDEO_MAX_DURATION = "maxduration";
    public static final String KEY_VIDEO_PROTOCOLS  = "protocols";
    public static final String KEY_LINEARITY  = "linearity";
   // public static final String KEY_VIDEO_MIMES  = "linearity";
    public static final String KEY_VIDEO_MIMES  = "mimes";

    static final String KEY_TAGID = "tagid";


    public static final String ORTB_VERSION_PARAM = "x-openrtb-version";

    public static final String CONTENT_TYPE = "Content-Type";
    public static final String ACCEPT_LANGUAGE = "Accept-Language";
    public static final String USER_AGENT = "User-Agent";
    public static final String URL_ENCODING = "UTF-8";


    static {
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
        dateFormatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US);
        partDayFormatter = new SimpleDateFormat("yyyy-MM-dd-a", Locale.US);
        knownPatterns.add(new SimpleDateFormat("HH:mm:ss"));
        knownPatterns.add(new SimpleDateFormat("ss"));
        knownPatterns.add(new SimpleDateFormat("HH:mm:ss.SSS"));
    }

    public static int convertDpToPixel(int value) {
        return (int) (value * Resources.getSystem().getDisplayMetrics().density);
    }

    public static boolean isTablet(Context context) {
        return (context.getResources().getConfiguration().screenLayout
                & Configuration.SCREENLAYOUT_SIZE_MASK)
                >= Configuration.SCREENLAYOUT_SIZE_LARGE;
    }

    public static String writeStringToFile(String string, String filePath) {
        File file = new File(filePath);
        FileOutputStream fOut;
        try {
            fOut = new FileOutputStream(file);
            byte[] b = string.getBytes();//converting string into byte array
            fOut.write(b);
            fOut.close();
        } catch (Exception e) {
            Log.e(TAG, e.getLocalizedMessage());
        }
        return null;
    }

    public static String getLemmaRootDir() {
        return LEMMA_ROOT_PATH;
    }

    public static void setUpDirectories(Context context, String rootDirectory) {
        STORAGE_PATH = Environment.getExternalStorageDirectory().getPath();
        LEMMA_ROOT_PATH = STORAGE_PATH + rootDirectory;
        LMUtils.createPubDirectory();
        LMUtils.createRTBParamXML();
    }

    public static void setUpDirectories(Context context, String rootDirectory, boolean internalStorage) {
        if (internalStorage) {
            STORAGE_PATH = context.getFilesDir().getAbsolutePath();
        } else {
            STORAGE_PATH = Environment.getExternalStorageDirectory().getPath();
        }
        LEMMA_ROOT_PATH = STORAGE_PATH + rootDirectory;
        LMUtils.createPubDirectory();
        LMUtils.createRTBParamXML();

        String logDir = LMUtils.getLemmaLogsDirPath();
        File mPath = new File(logDir);
        if (mPath != null && !mPath.exists()) {
            mPath.mkdirs();
        }

    }

    public static void createRTBParamXML() {

        String rtbParamFilePath = getLemmaRootDir() + "/RTBParameter.xml";
        File file = new File(rtbParamFilePath);

        if (file != null && !file.exists()) {
            try {
                file.createNewFile();
                writeStringToFile("<RTBParameterList>\n" +
                        "<apurl>https://play.google.com/store/apps/details?id=com.lemma.digital</apurl>\n" +
                        "<iploc>18.5246036,73.7929268</iploc>\n" +
                        "<apbndl>com.lemma.digital</apbndl>\n" +
                        "</RTBParameterList>", rtbParamFilePath);
            } catch (IOException e) {
                Log.d(TAG, e.getLocalizedMessage());
            }
        }
    }

    public static void createPubDirectory() {
        String directoryName = getLemmaRootDir() + "Pub/";
        File folder = new File(directoryName);
        if (!folder.exists()) {
            folder.mkdirs();
        }
    }

    public static String getFileNameFromURL(String url) {

        String name = url;
        int pos = name.indexOf("/");
        while (pos != -1) {
            name = name.substring(++pos);
            pos = name.indexOf("/");
        }
        return name;
    }

    public static boolean isNetworkConnected(Context context) {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    public static String readFile(String filaPath) {

        File file = new File(filaPath);

        //write the bytes in file
        if (file.exists()) {
            FileInputStream fIn;
            try {
                fIn = new FileInputStream(file);
                InputStreamReader inputStreamReader = new InputStreamReader(fIn);

                int size = fIn.available();
                char[] buffer = new char[size];

                inputStreamReader.read(buffer);

                fIn.close();
                return new String(buffer);
            } catch (Exception e) {
                Log.d(TAG, e.getLocalizedMessage());
            }
            return null;

        }
        return null;
    }

    /**
     * Returns the long value in milli second for given time string. It accepts
     * input in HH:mm:ss.SSS as well as HH:mm:ss else return -1.
     *
     * @param time
     * @return
     */
    public static long convertTimeToMillis(String time) {
        for (SimpleDateFormat pattern : knownPatterns) {
            try {
                Date date = pattern.parse(time);
                return date.getTime();
            } catch (Exception e) {
                Log.e(TAG, e.getLocalizedMessage());
            }
        }
        return -1;
    }

    public static boolean isImageType(String value) {
        if (value == null) {
            return false;
        }
        return value.endsWith("png") || value.endsWith("jpeg") || value.endsWith("jpg");
    }

    public static ArrayList<MediaFile> filteredListWithMimeType(ArrayList<MediaFile> mediaFiles, String mimeType) {

        ArrayList<MediaFile> filteredMediaFiles = new ArrayList<>();
        for (MediaFile mf : mediaFiles) {

            if (mimeType.equalsIgnoreCase(mf.getType())) {
                filteredMediaFiles.add(mf);
            }
        }
        return filteredMediaFiles;
    }

    public static ArrayList<MediaFile> filteredListForBestMatchingMedia(ArrayList<MediaFile> mediaFiles) {

        final String[] supportedMediaTypes = {"video/mp4", "video/3gpp", "video/webm",
                "image/jpeg", "image/jpg", "image/png"};

        for (String mimeType : supportedMediaTypes) {

            ArrayList<MediaFile> fMediaFiles = filteredListWithMimeType(mediaFiles, mimeType);
            if (fMediaFiles.size() > 0) {

                ArrayList<MediaFile> sMediaFiles = sortedListByWidth(fMediaFiles);
                MediaFile finalMediaFile = sMediaFiles.get(0);

                ArrayList<MediaFile> finalMediaFileArray = new ArrayList<>();
                finalMediaFileArray.add(finalMediaFile);
                return finalMediaFileArray;
            }
        }
        return mediaFiles;
    }

    public static void filterUnsupportedAds(Vast vast) {
        for (AdI ad : vast.ads) {
            if (ad instanceof LinearAd) {
                ((LinearAd) ad).filter();
            }
        }
    }

    public static ArrayList<MediaFile> sortedListByWidth(ArrayList<MediaFile> mediaFiles) {
        Collections.sort(mediaFiles, new Comparator<MediaFile>() {
            @Override
            public int compare(MediaFile mediaFile, MediaFile t1) {
                Integer width1 = Integer.parseInt(mediaFile.getWidth());
                Integer width2 = Integer.parseInt(t1.getHeight());
                return width2 - width1;
            }
        });
        return mediaFiles;
    }

    public static String getCurrentTimeStamp() {
        return dateFormatter.format(new Date());
    }

    public static String getPartDayTimeStamp() {
        return partDayFormatter.format(new Date());
    }

    public static String getCurrentPlainTimeStamp() {
        return sdf.format(new Date());
    }

    public static Context getApplicationContext() {
        Application application = null;
        try {
            application = (Application) Class.forName("android.app.ActivityThread")
                    .getMethod("currentApplication").invoke(null, (Object[]) null);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return application.getApplicationContext();
    }

    public static String getCrc32(String input) {
        CRC32 crc = new CRC32();
        crc.update(input.getBytes());
        String enc = String.format("%08X", crc.getValue());
        return enc;
    }

    public static String getLemmaLogsDirPath() {
        return LMUtils.getLemmaRootDir() + "/Logs";
    }

    public static void delete(ArrayList<File> files) {
        for (File f : files) {
            delete(f);
        }
    }

    private static void delete(File f) {
        if (f.isDirectory()) {
            for (File c : f.listFiles()) {
                delete(c);
            }
        } else {
            if (!f.delete()) {
                LMLog.i(TAG, "Failed to delete file: " + f);
            }
        }
    }

    public static ArrayList<File> listFiles(File f, ArrayList<String> excludeFiles, int counts, int limit) {

        ArrayList<File> files = new ArrayList<>();
        if (counts >= limit) {
            return files;
        }

        if (f.isDirectory()) {
            for (File file : f.listFiles()) {
                ArrayList<File> intermediateFiles = listFiles(file, excludeFiles, counts + files.size(), limit);
                files.addAll(intermediateFiles);
            }

        } else {
            String path = f.getAbsolutePath();
            if (!excludeFiles.contains(path)) {
                LMLog.i(TAG, "Deleting  " + f.getAbsolutePath());
                files.add(f);
            } else {
                LMLog.i(TAG, "Excluding file from deletion: " + f);
            }
        }
        return files;
    }

    public static long getAvailableExternalMemorySize() {
        long freeBytesExternal = new File(STORAGE_PATH).getFreeSpace();
        return freeBytesExternal;
    }

    public static boolean isDeviceMemoryAvailable(float ratio) {
        long totalExtMemoryInMb = getTotalExternalMemorySize();
        long availableExtMemoryInMb = getAvailableExternalMemorySize();
        double available = (double) availableExtMemoryInMb / (double) totalExtMemoryInMb;
        return availableExtMemoryInMb > 0 && ratio < available;
    }

    public static long getTotalExternalMemorySize() {
        long ttlBytesExternal = new File(STORAGE_PATH).getTotalSpace();
        return ttlBytesExternal;
    }

}
