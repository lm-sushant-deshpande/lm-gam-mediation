package lemma.lemmavideosdk.common;

class AppConstants {

    public static final String LEMMA_ROOT_DIR_NAME = "/LemmaApp/";
    // HTTP request body parameters
    public static final String CONTENT_TYPE = "Content-Type";

    public static final String CONTENT_LENGTH = "Content-Length";
    public static final String CONTENT_MD5 = "Content-MD5";
    public static final String ACCEPT = "Accept";
    public static final String ACCEPT_CHARSET = "Accept-Charset";
    public static final String ACCEPT_DATETIME = "Accept-Datetime";
    public static final String CACHE_CONTROL = "Cache-Control";
    public static final String CONNECTION = "Connection";
    public static final String DATE = "Date";
    public static final String CONTENT_LANGUAGE = "Content-Language";
    public static final String HOST = "Host";
    public static final String ACCEPT_LANGUAGE = "Accept-Language";
    public static final String USER_AGENT = "User-Agent";
    public static final String RLNCLIENT_IP_ADDR = "RLNClientIpAddr";
    public static final String HTTPMETHODPOST = "POST";
    public static final String HTTPMETHODGET = "GET";
    public static final String URL_ENCODING = "UTF-8";
    public static final String REQUEST_CONTENT_TYPE = "text/plain";
    public static final String REQUEST_CONTENT_LANG_EN = "en";
    public static final String AMPERSAND = "&";
    // For Shared preference key
    public static final String KEY_COMMON_CONFIG_PARAMS = "common";
    public static final String KEY_LEMMA_CONFIG_PARAMS = "lemma";
    public static final String KEY_DEFAULT_PASSBACK_THRESHOLD = "DEFAULT_PASSBACK_THRESHOLD";
    public static final String KEY_DEFAULT_WRAPPER_THRESHOLD = "DEFAULT_WRAPPER_THRESHOLD";
    public static final String KEY_DEFAULT_VID_DOMAIN = "DEFAULT_VID_DOMAIN";
    public static final String KEY_DEFAULT_ADS_PATH = "DEFAULT_ADS_PATH";
    public static final String KEY_DEFAULT_SHOWADS_DOMAIN = "DEFAULT_SHOWADS_DOMAIN";
    public static final String KEY_DEFAULT_SHOWADS_PATH = "DEFAULT_SHOWADS_PATH";
    public static final String KEY_DEFAULT_TRACKER_DOMAIN = "DEFAULT_TRACKER_DOMAIN";
    public static final String KEY_MAST_DEFAULT_PASSBACK_THRESHOLD = "MAST_DEFAULT_PASSBACK_THRESHOLD";
    public static final String KEY_MAST_DEFAULT_WRAPPER_THRESHOLD = "MAST_DEFAULT_WRAPPER_THRESHOLD";
    public static final String KEY_MAST_DEFAULT_VID_DOMAIN = "MAST_DEFAULT_VID_DOMAIN";
    public static final String KEY_MAST_DEFAULT_ADS_PATH = "MAST_DEFAULT_ADS_PATH";
    public static final String KEY_MAST_DEFAULT_TRACKER_DOMAIN = "MAST_DEFAULT_TRACKER_DOMAIN";
    public static final String KEY_LEMMA_DEFAULT_PASSBACK_THRESHOLD = "LEMMA_DEFAULT_PASSBACK_THRESHOLD";
    public static final String KEY_LEMMA_DEFAULT_WRAPPER_THRESHOLD = "LEMMA_DEFAULT_WRAPPER_THRESHOLD";
    public static final String KEY_LEMMA_DEFAULT_VID_DOMAIN = "LEMMA_DEFAULT_VID_DOMAIN";
    public static final String KEY_LEMMA_DEFAULT_ADS_PATH = "LEMMA_DEFAULT_ADS_PATH";
    public static final String KEY_LEMMA_DEFAULT_TRACKER_DOMAIN = "LEMMA_DEFAULT_TRACKER_DOMAIN";
    public static final String KEY_SUPPORTED_ERROR_CODES = "SUPPORTED_ERROR_CODES";
    public static final String HTTP_PREFIX = "http://";
    public static final String COMMON_SUPPORTED_ERROR_CODES = "[100,101,201,203,301,400,401,402,500,501,502,900]";
    public static final String LEMMA_PRODUCTION_DOMAIN = "136.243.4.77";
    public static final String LEMMA_QA_DOMAIN = "136.243.4.77";
    public static final String LEMMA_DOMAIN = AppConstants.LEMMA_PRODUCTION_DOMAIN;
    public static final String LEMMA_TRACKING_DOMAIN = "136.243.4.77";
    public static final String LEMMA_AD_SERVER_PATH = "/lemma/servad?";
    public static final String LEMMA_AD_SERVER_URL = "http://lemmatechnologies.com/lemma/servad?";//"http://136.243.4.77/lemma/servad?";
    public static final int LEMMA_MAX_VAST_WRAPPER_LEVEL = 3;
    public static final int LEMMA_MAX_AD_DEFAULTED_LEVEL = 3;
    public static final String LEMMA_RTB_DOMAIN = AppConstants.LEMMA_PRODUCTION_DOMAIN;
    public static final String LEMMA_RTB_AD_SERVER_PATH = "//lemma/servad?";
    public static final String PUB_ID = "pid";
    public static final String SITE_ID = "sid";
    public static final String AD_ID = "aid";
    public static final String OPER_ID = "oid";
    public static final String PREFS_NAME = "CONFIG";
    public static final int INVALID_INT = -999;
    public static final int AD_TAG_TYPE_VALUE = 13;
    public static final int VAD_FORMAT_VALUE = 2; //VAST version i.e. 2
    public static final int OPER_ID_VALUE = 102;
    public static final int VMINIMUM_LENGTH_VALUE = 0;
    public static final int VMAXIMUM_LENGTH_VALUE = 500;
    public static final int MAX_SOCKET_TIME = 5000;
    public static final int AD_NETWORK_243 = 243;

    public enum CONTENT_TYPE {
        JSON,
        XML,
        INVALID
    }

    public enum UDID_HASH_ALGO {
        UNKNOWN,
        RAW,
        SHA1,
        MD5
    }
}
