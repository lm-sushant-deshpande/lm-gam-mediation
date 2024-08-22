package lemma.lemmavideosdk.vast.manager;


import android.content.Context;
import android.net.Uri;


import androidx.annotation.UiThread;

import com.liulishuo.filedownloader.BaseDownloadTask;
import com.liulishuo.filedownloader.FileDownloadListener;
import com.liulishuo.filedownloader.FileDownloader;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;

import lemma.lemmavideosdk.common.LMLog;
import lemma.lemmavideosdk.common.LMUtils;
import lemma.lemmavideosdk.vast.VastBuilder.AdI;
import lemma.lemmavideosdk.vast.VastBuilder.Vast;


class DownloadManager {
    String rootDirectory;
    int downloadCount;

    @UiThread
    public DownloadManager(String rootDirectory) {
        this.rootDirectory = rootDirectory;
        FileDownloader.getImpl().setMaxNetworkThreadCount(1);
    }

    @UiThread
    public DownloadManager(String rootDirectory, Context context) {
        this.rootDirectory = rootDirectory;
        FileDownloader.getImpl().setMaxNetworkThreadCount(1);
    }

    private static String getFileNameFromURL(String url) {
        String name = url;
        int pos = name.indexOf("/");
        while (pos != -1) {
            name = name.substring(++pos);
            pos = name.indexOf("/");
        }
        return name;
    }

    private static String getFileDerivedNameFromURL(String url) {
        Uri uri = Uri.parse(url);
        String fileName = uri.getLastPathSegment();
        StringBuilder sb = new StringBuilder();

        String timeStamp = LMUtils.getCurrentPlainTimeStamp();

        sb.append(timeStamp).append("_").
                append(LMUtils.getCrc32(url)).append("_")
                .append(fileName);
        return sb.toString();
    }

    public void destroy() {
        FileDownloader.getImpl().pauseAll();
    }

    synchronized private void decrementCount() {
        downloadCount = downloadCount - 1;
    }

    private void download(final AdI ad, final String path, final DownloadCallback callback) {

        FileDownloader.getImpl().create(ad.getAdRL())
                .setPath(path)
                .setListener(new FileDownloadListener() {
                    @Override
                    protected void pending(BaseDownloadTask task, int soFarBytes, int totalBytes) {
                    }

                    @Override
                    protected void started(BaseDownloadTask task) {
                        LMLog.i("DownloadManager", "Download Started " + task.getUrl());
                    }

                    @Override
                    protected void connected(BaseDownloadTask task, String etag, boolean isContinue, int soFarBytes, int totalBytes) {
                    }

                    @Override
                    protected void progress(BaseDownloadTask task, int soFarBytes, int totalBytes) {
                    }

                    @Override
                    protected void blockComplete(BaseDownloadTask task) {
                    }

                    @Override
                    protected void retry(final BaseDownloadTask task, final Throwable ex, final int retryingTimes, final int soFarBytes) {
                        LMLog.i("DownloadManager", "Download retry " + task.getUrl());
                    }

                    @Override
                    protected void completed(BaseDownloadTask task) {
                        callback.onDownloadComplete(ad, task);
                    }

                    @Override
                    protected void paused(BaseDownloadTask task, int soFarBytes, int totalBytes) {
                    }

                    @Override
                    protected void error(BaseDownloadTask task, Throwable e) {
                        callback.onDownloadError(ad, e);
                    }

                    @Override
                    protected void warn(BaseDownloadTask task) {
                        LMLog.i("DownloadManager", "Download warn " + task);
                        callback.onDownloadComplete(ad, task);
                    }
                }).start();
    }

    private void shouldCallCompletion(CompletionCallback callback,
                                      ArrayList<AdI> downloadedAds) {
        LMLog.i("DownloadManager", "Download count - " + downloadCount);
        if (downloadCount <= 0) {
            callback.onDownloadComplete(downloadedAds);
        }
    }

    private String fileExitsInRoot(String rootPath, String fileUrl) {
        File folder = new File(rootPath);
        final String crcCode = LMUtils.getCrc32(fileUrl);
        String[] files = folder.list(new FilenameFilter() {
            @Override
            public boolean accept(File file, String s) {
                return s.contains(crcCode);
            }
        });
        if (files != null && files.length > 0) { return rootPath + files[0]; }
        return null;
    }

    public void download(final Vast vast, final CompletionCallback callback) {

        final ArrayList<AdI> ads = vast.ads;
        downloadCount = ads.size();
        final ArrayList<AdI> downloadedAds = new ArrayList<>();


        for (final AdI ad : ads) {

            final String source = ad.getAdRL();


            boolean isUrl = ad.isUrl();
            if (!isUrl) {
                downloadedAds.add(ad);
                decrementCount();
                shouldCallCompletion(callback, downloadedAds);
                continue;
            }

            String destPath = fileExitsInRoot(this.rootDirectory, source);
            if (destPath != null) {

                if (destPath.endsWith(".temp")) {
                    File dirtyFile = new File(destPath);
                    dirtyFile.delete();
                    destPath = this.rootDirectory + getFileDerivedNameFromURL(source);
                } else {
                    LMLog.i("DownloadManager", "Found file on disk, Skipping file download " + destPath);
                    ad.setAdRL(destPath);
                    downloadedAds.add(ad);
                    decrementCount();
                    shouldCallCompletion(callback, downloadedAds);
                    continue;
                }
            } else {
                destPath = this.rootDirectory + getFileDerivedNameFromURL(source);
            }

            final String finalDestPath = destPath;
            download(ad, destPath, new DownloadCallback() {
                @Override
                public void onDownloadStarted(long totalLength) {
                    LMLog.i("DownloadManager", "Download started " + ad.getAdRL());
                }

                @Override
                public void onDownloadProgress(long downloadedLength) {
                }

                @Override
                public void onDownloadComplete(AdI ad, BaseDownloadTask task) {
                    LMLog.i("DownloadManager", "Download Complete " + ad.getAdRL());
                    ad.setAdRL(finalDestPath);
                    downloadedAds.add(ad);
                    decrementCount();
                    shouldCallCompletion(callback, downloadedAds);
                }

                @Override
                public void onDownloadError(AdI ad, Throwable e) {
                    LMLog.i("DownloadManager", "Download Error " + ad.getAdRL()+" E: "+e.getLocalizedMessage());
                    decrementCount();
                    shouldCallCompletion(callback, downloadedAds);
                }
            });
        }
    }

    public interface DownloadCallback {
        void onDownloadStarted(long totalLength);

        void onDownloadProgress(long downloadedLength);

        void onDownloadComplete(AdI ad, BaseDownloadTask task);

        void onDownloadError(AdI ad, Throwable e);
    }

    public interface CompletionCallback {
        void onDownloadComplete(ArrayList<AdI> ads);
    }
}