package lemma.lemmavideosdk.common;

import android.util.Log;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class AppLog {

    private final static String TAG_LOGGER = "LemmaLogger";
    public static File mPath;
    public static boolean mVerboseEnabled = false;
    public static boolean mInfoEnabled = false;
    public static boolean mErrorEnabled = false;
    public static boolean mDebugEnabled = false;
    public static boolean mWarningEnabled = false;

    public static boolean canLogToFile = false;
    ;
    private static File mLogfile;
    private static FileWriter mFileWriter;

    static {

        mPath = new File(LMUtils.getLemmaLogsDirPath());
        if (mPath != null && !mPath.exists()) {
            mPath.mkdirs();
        }
    }



    private static FileWriter sharedFileWriter() {

        String timeStamp = LMUtils.getCurrentTimeStamp().replaceAll("/", "");
        timeStamp = timeStamp.replace(" ", "");
        timeStamp = timeStamp.replace(":", "-");
        String logFilePath = LMUtils.getLemmaLogsDirPath()+"/Lemma_" + timeStamp + ".txt";
        if (mLogfile == null) {
            mLogfile = new File(logFilePath);
            if (!mLogfile.exists()){
                try {
                    mLogfile.createNewFile();
                } catch (IOException e) {
                    mLogfile = null;
                    Log.e(TAG_LOGGER,e.getLocalizedMessage());
                }
            }
        }
        mLogfile.setWritable(true);

        if (mLogfile != null){
            try {
                if (mFileWriter == null) {
                    mFileWriter = new FileWriter(mLogfile, true);
                }
//                mBufferedWritter = new BufferedWriter(mFileWriter);
            } catch (IOException e) {
                Log.e(TAG_LOGGER, Log.getStackTraceString(e));
            }
        }
        return mFileWriter;

    }

    private static void writeToFile(String tag, String message) {

        if (!canLogToFile) {
            return;
        }

        try {
            FileWriter mBufferedWriter = sharedFileWriter();
            if (mBufferedWriter != null){
                String text = LMUtils.getCurrentTimeStamp() + " " + tag + " " + message
                        + System.getProperty("line.separator");

                mBufferedWriter.append(text);
                mBufferedWriter.flush();
            }

        } catch (Exception e) {
            Log.e(TAG_LOGGER,
                    "Problem in log file writing " + e.getMessage());
        }
    }

    public static void setLogType(boolean verboseEnabled, boolean infoEnabled,
                                  boolean errorEnabled, boolean debugEnabled, boolean warningEnabled) {
        mVerboseEnabled = verboseEnabled;
        mInfoEnabled = infoEnabled;
        mErrorEnabled = errorEnabled;
        mDebugEnabled = debugEnabled;
        mWarningEnabled = warningEnabled;
    }

    public static void i(String tag, String message) {
        if (message == null) {
            message = "";
        }
        Log.i(tag, message);
        if (mInfoEnabled) {
            writeToFile(tag, message);
        }
    }

    public static void e(String tag, String message) {
        if (message == null) {
            message = "";
        }
        Log.e(tag, message);
        if (mErrorEnabled) {
            writeToFile(tag, message);
        }
    }

    public static void d(String tag, String message) {
        if (message == null) {
            message = "";
        }
        Log.d(tag, message);
        if (mDebugEnabled) {
            writeToFile(tag, message);
        }
    }

    public static void w(String tag, String message) {
        if (message == null) {
            message = "";
        }
        Log.w(tag, message);
        if (mWarningEnabled) {
            writeToFile(tag, message);
        }
    }

    public static void v(String tag, String message) {
        if (message == null) {
            message = "";
        }
        Log.v(tag, message);
        if (mVerboseEnabled) {
            writeToFile(tag, message);
        }
    }

//    public static void e(String tag, Exception e) {
//        Writer writer = new StringWriter();
//        PrintWriter printWriter = new PrintWriter(writer);
//        e.printStackTrace(printWriter);
//        String message = writer.toString();
//        e(tag, message);
//    }
}
