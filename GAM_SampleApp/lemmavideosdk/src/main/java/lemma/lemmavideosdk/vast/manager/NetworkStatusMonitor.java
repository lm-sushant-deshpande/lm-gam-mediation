package lemma.lemmavideosdk.vast.manager;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.v4.content.ContextCompat;

public class NetworkStatusMonitor {

    Context context;

    public NetworkStatusMonitor(Context context) {
        this.context = context;
    }

    public boolean isNetworkConnected() {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        return cm.getActiveNetworkInfo() != null;
    }

    public NETWORK_TYPE getCurrentNetworkType() {

        ConnectivityManager cm = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);

        if (cm != null && ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_NETWORK_STATE) == PackageManager.PERMISSION_GRANTED) {
            NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
            if (null != activeNetwork) {

                switch (activeNetwork.getType()) {

                    case ConnectivityManager.TYPE_WIFI:
                        return NETWORK_TYPE.WIFI;
                    case ConnectivityManager.TYPE_MOBILE:
                        return NETWORK_TYPE.CELLULAR;
                    default:
                        return NETWORK_TYPE.UNKNOWN;
                }
            }
        }
        return NETWORK_TYPE.UNKNOWN;
    }

    public enum NETWORK_TYPE {
        CELLULAR("cellular"), WIFI("wifi"), UNKNOWN(null);

        private final String value;

        NETWORK_TYPE(String val) {
            this.value = val;
        }

        public String getValue() {
            return value;
        }
    }
}
