package lemma.lemmavideosdk.common;

import android.util.Log;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.Comparator;

public class LMLog {

    private final static String TAG_LOGGER = "LemmaLogger";
//    public static File mPath;
    private static LogLevel logLevel = LogLevel.Verbose;
    private static boolean fileBasedLogging = false;
    private static FileWriter mFileWriter;

//    static {
//        String logDir = LMUtils.getLemmaLogsDirPath();
//        mPath = new File(logDir);
//        if (mPath != null && !mPath.exists()) {
//            mPath.mkdirs();
//        }
//    }

    public static void setFileBasedLogging(boolean fileBasedLogging) {
        LMLog.fileBasedLogging = fileBasedLogging;
    }

    private static void deleteOldFilesIfRequired() {
        String path = LMUtils.getLemmaLogsDirPath();
        File directory = new File(path);
        File[] files = directory.listFiles();
        Arrays.sort(files, new Comparator<File>() {
            @Override
            public int compare(File file, File t1) {
                return file.getName().compareTo(t1.getName());
            }
        });

        if (files.length > 8) {
            for (int index = 0; index < files.length - 8; ++index) {
                File f = files[index];
                f.delete();
            }
        }
    }

    private static FileWriter sharedFileWriter() {

        String timeStamp = LMUtils.getPartDayTimeStamp().replaceAll("/", "");
        timeStamp = timeStamp.replace(" ", "");
        timeStamp = timeStamp.replace(":", "-");
        String logFilePath = LMUtils.getLemmaLogsDirPath() + "/Lemma_" + timeStamp + ".txt";
        File logfile = new File(logFilePath);
        if (!logfile.exists()) {
            // Create file
            // Create file writer
            try {
                logfile.createNewFile();
                mFileWriter = new FileWriter(logfile, true);
                deleteOldFilesIfRequired();
            } catch (Exception e) {
                Log.e(TAG_LOGGER, e.getLocalizedMessage());
            }
        } else {
            if (mFileWriter == null) {
                try {
                    mFileWriter = new FileWriter(logfile, true);
                } catch (IOException e) {
                    Log.e(TAG_LOGGER, e.getLocalizedMessage());
                }
            }
        }
        return mFileWriter;
    }

    private static void writeToFile(String tag, String message) {

        try {
            FileWriter mBufferedWriter = sharedFileWriter();
            if (mBufferedWriter != null) {
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

    public static void setLogLevel(LogLevel level) {
        logLevel = level;
    }

    private static boolean shouldLog(LogLevel level, String message) {
        if (logLevel.getLevel() <= level.getLevel()) {
            return (message != null);
        }
        return false;
    }

    public static void i(String tag, String message) {
        if (shouldLog(LogLevel.Info, message)) {
            Log.i(tag, message);
            if (fileBasedLogging) {
                writeToFile(tag, message);
            }
        }
    }

    public static void e(String tag, String message) {
        if (shouldLog(LogLevel.Error, message)) {
            Log.e(tag, message);
            if (fileBasedLogging) {
                writeToFile(tag, message);
            }
        }
    }

    public static void d(String tag, String message) {
        if (shouldLog(LogLevel.Debug, message)) {
            Log.d(tag, message);
            if (fileBasedLogging) {
                writeToFile(tag, message);
            }
        }
    }

    public static void w(String tag, String message) {
        if (shouldLog(LogLevel.Warn, message)) {
            Log.w(tag, message);
            if (fileBasedLogging) {
                writeToFile(tag, message);
            }
        }
    }

    public static void v(String tag, String message) {
        if (shouldLog(LogLevel.Verbose, message)) {
            Log.v(tag, message);
            if (fileBasedLogging) {
                writeToFile(tag, message);
            }
        }
    }

    /**
     * Log levels to filter logs
     */
    public enum LogLevel {
        /**
         * All level of logs
         */
        All(0),
        /**
         * Error, warning, info, debug and verbose logs
         */
        Verbose(1),
        /**
         * Error, warning, info and debug logs
         */
        Debug(2),
        /**
         * Error, warning and info logs
         */
        Info(3),
        /**
         * Error and warning logs
         */
        Warn(4),
        /**
         * Error logs only
         */
        Error(5),
        /**
         * No logs
         */
        Off(6);

        final int level;

        LogLevel(final int level) {
            this.level = level;
        }

        public int getLevel() {
            return level;
        }
    }
}
