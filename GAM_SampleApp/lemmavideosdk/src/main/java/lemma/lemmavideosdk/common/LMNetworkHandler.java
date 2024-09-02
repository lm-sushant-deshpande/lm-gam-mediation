package lemma.lemmavideosdk.common;

import android.util.Log;

import java.io.IOException;
import java.security.cert.CertificateException;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;

import lemma.lemmavideosdk.vast.VastBuilder.VastBuilder;
import okhttp3.OkHttpClient;

public class LMNetworkHandler {

    private static String TAG = "LMNetworkHandler";

    private static OkHttpClient getOkHttpClient() {
        return new OkHttpClient();
    }

    public static interface LMNetworkHandlerListener{
        public void onSuccess(String data);
        public void onFailure(Error error);
    }

    public void fetch(String url,final LMNetworkHandlerListener listener) {

        OkHttpClient httpClient = getOkHttpClient();
        Request request = new Request.Builder()
                .url(url)
                .build();
        Call httpCall = httpClient.newCall(request);
        httpCall.enqueue(new Callback() {

            @Override
            public void onFailure(Call call, IOException e) {
                LMLog.e(TAG, e.getLocalizedMessage());
                listener.onFailure(new Error(e));
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String data = response.body().string();
                listener.onSuccess(data);
            }
        });
    }
}
