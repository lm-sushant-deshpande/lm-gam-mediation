package lemma.lemmavideosdk.common;

public class LMError {


    // Server communication General error

    public static final int INVALID_AD_ERROR = -1;

    public static final int UNDEFINED_ERROR = -999;

    public static final int REQUEST_ERROR = -1000;

    public static final int CONNECTION_ERROR = -1001;

    public static final int SERVER_ERROR = -1002;

    public static final int REQUEST_CANCLE = -1003;

    public static final int TIMEOUT_ERROR = -1004;

    public static final int GENERIC_IO_ERROR = -1005;

    public static final int INVALID_RESPONSE_ERROR = -1006;

    public static final int JSON_ERROR = -1007;

    public static final int REDIRECT_ERROR = -1302;

    public static final int SUCCESS_CODE = 0;

    //IAB VAST 3.0 defined error type
    // Ad response specific.
    public static final int VAST_XML_PARSING_ERROR = 100;

    public static final int VAST_SCHEMA_VALIDATION_ERROR = 101;

    public static final int VAST_RESPONSE_VERSION_NOT_SUPPRTED_ERROR = 102;
    //Video Player specific
    public static final int VAST_VIDEO_PLAYER_TRAFFICKING_ERROR = 200;

    public static final int VAST_VIDEO_PLAYER_LINESRITY_MISMATCH_ERROR = 201;

    public static final int VAST_VIDEO_PLAYER_DURATION_MISMATCH_ERROR = 202;

    public static final int VAST_VIDEO_PLAYER_SIZE_MISMATCH_ERROR = 203;
    //Wrapper response specific
    public static final int VAST_GENERAL_WRAPPER_ERROR = 300;

    public static final int VAST_WRAPPER_URI_TIMEOUT_ERROR = 301;

    public static final int VAST_WRAPPER_LIMIT_EXCEED_ERROR = 302;

    public static final int VAST_RESPONSE_UNAVAILABLE_IN_WRAPPER_ERROR = 303;
    //Linear Ad specific
    public static final int VAST_GENERAL_LINEAR_ERROR = 400;

    public static final int VAST_LINEAR_FILE_NOT_FOUND_ERROR = 401;

    public static final int VAST_MEDIA_FILE_TIMEOUT_ERROR = 402;

    public static final int VAST_SUPPORTED_MEDIAFILE_UNAVAILABLE_ERROR = 403;

    public static final int VAST_MEDIA_FILE_DISPLAY_ERROR = 405;
    //NonLinear Ad specific
    public static final int VAST_GENERAL_NONLINEAR_ERROR = 500;

    public static final int VAST_NONLINEAR_CREATIVE_DIMENSION_ALIGNMENT_ERROR = 501;

    public static final int VAST_NONLINEAR_RESOURCE_NOT_FETCHED = 502;

    public static final int VAST_SUPPORTED_NONLINEAR_RESOURCE_NOT_FOUND = 503;
    //Companion Ad specific
    public static final int VAST_GENERAL_COMPANION_ERROR = 600;

    public static final int VAST_COMPANION_CREATIVE_DIMENSION_ALIGNMENT_ERROR = 601;

    public static final int VAST_COMPANION_DISPLAY_ERROR = 602;

    public static final int VAST_COMPANION_RESOURCE_NOT_FETCHED = 603;

    public static final int VAST_SUPPORTED_COMPANION_NOT_FOUND = 604;

    public static final int VAST_UNDEFINED_ERROR = 900;

    public static final int VAST_GENERAL_VPAID_ERROR = 901;
    // End of IAB VAST error type.
}
