package lemma.lemmavideosdk.vast.manager;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Looper;
import android.os.Parcel;
import android.os.RemoteException;

import java.io.IOException;
import java.util.concurrent.LinkedBlockingQueue;

public final class AdvertisingIdClient {

    private final static String LM_LIMITED_TRACKING_AD_KEY = "limited_tracking_ad_key";
    private final static String LM_AID_STORAGE = "aid_shared_preference";
    private final static String LM_AID_KEY = "aid_key";
    Context context;

    public AdvertisingIdClient(Context context) {
        this.context = context;
    }

    /**
     * Fetch the advertising info object synchronously and returns,
     * it saves the advertisement id and Opt-out state in shared preference of the application.
     * Execution of this method may take sometime as it is synchronous call.
     *
     * @param context
     * @return
     * @throws Exception
     */
    public static AdInfo getAdvertisingIdInfo(Context context) throws Exception {
        if (Looper.myLooper() == Looper.getMainLooper())
            throw new IllegalStateException("Cannot be called from the main thread");

        try {
            PackageManager pm = context.getPackageManager();
            pm.getPackageInfo("com.android.vending", 0);
        } catch (Exception e) {
            return null;
        }

        AdvertisingConnection connection = new AdvertisingConnection();
        Intent intent = new Intent("com.google.android.gms.ads.identifier.service.START");
        intent.setPackage("com.google.android.gms");
        if (context.bindService(intent, connection, Context.BIND_AUTO_CREATE)) {
            try {
                AdvertisingInterface adInterface = new AdvertisingInterface(connection.getBinder());

                //Save the Advertisement id & opt-out plag in local storage.
                saveAndroidAid(context, adInterface.getId());
                saveLimitedAdTrackingState(context, adInterface.isLimitAdTrackingEnabled(true));

                AdInfo adInfo = new AdInfo(adInterface.getId(), adInterface.isLimitAdTrackingEnabled(true));
                return adInfo;
            } catch (Exception exception) {
                throw exception;
            } finally {
                context.unbindService(connection);
            }
        }
        throw new IOException("Google Play connection failed");
    }

    /**
     * Returns the Android advertisement id if saved in local storage else returns given androidAid.
     */
    private static String getAndroidAid(Context ctx, String androidAid) {

        final Context context = ctx.getApplicationContext();

        if (context != null) {
            //Save the android_aid in local storage & use for next ad request
            SharedPreferences storage = context.getSharedPreferences(LM_AID_STORAGE, Context.MODE_PRIVATE);
            return storage.getString(LM_AID_KEY, androidAid);
        }
        return androidAid;
    }

    public static String getSavedAndroidAid(Context ctx) {

        final Context context = ctx.getApplicationContext();
        String androidAid = null;
        if (context != null) {
            //Save the android_aid in local storage & use for next ad request
            SharedPreferences storage = context.getSharedPreferences(LM_AID_STORAGE, Context.MODE_PRIVATE);
            if (!storage.contains(LM_AID_KEY)) {
                androidAid = null;
            }else{
                androidAid = storage.getString(LM_AID_KEY, "Uknown");
            }
        }
        return androidAid;
    }

    public static Boolean getSavedLimitedAdTrackingState(Context ctx) {

        final Context context = ctx.getApplicationContext();
        Boolean state = null;
        if (context != null) {
            //Save the android_aid in local storage & use for next ad request
            SharedPreferences storage = context.getSharedPreferences(LM_AID_STORAGE, Context.MODE_PRIVATE);
            if (!storage.contains(LM_LIMITED_TRACKING_AD_KEY)){
                state = null;
            }else{
                state = storage.getBoolean(LM_LIMITED_TRACKING_AD_KEY, false);
            }
        }
        return state;
    }

    /**
     * Returns the Android advertisement id if saved in local storage else returns given state.
     */
    public static boolean getLimitedAdTrackingState(Context ctx, boolean state) {

        final Context context = ctx.getApplicationContext();

        if (context != null) {
            //Save the android_aid in local storage & use for next ad request
            SharedPreferences storage = context.getSharedPreferences(LM_AID_STORAGE, Context.MODE_PRIVATE);
            return storage.getBoolean(LM_LIMITED_TRACKING_AD_KEY, state);
        }
        return state;
    }

    /**
     * Save the Android advertisement id in local storage for further use.
     */
    private static void saveAndroidAid(final Context context, String androidAid) {
        //Save the android_aid in local storage & use for next ad request
        SharedPreferences storage = context.getSharedPreferences(LM_AID_STORAGE, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = storage.edit();
        if (editor != null) {
            editor.putString(LM_AID_KEY, androidAid);
            editor.apply();
        }
    }

    /**
     * Save the Android advertisement id in local storage for further use.
     */
    private static void saveLimitedAdTrackingState(final Context context, boolean state) {
        //Save the android_aid in local storage & use for next ad request
        SharedPreferences storage = context.getSharedPreferences(LM_AID_STORAGE, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = storage.edit();
        if (editor != null) {
            editor.putBoolean(LM_LIMITED_TRACKING_AD_KEY, state);
            editor.apply();
        }
    }

    /**
     * Refresh the advertising info saved in local storage asynchronously.
     *
     * @return Returns the advertising info saved in local storage before refresh.
     */
    public AdInfo refreshAdvertisingInfo() {

        final Context context = this.context;

        new Thread(new Runnable() {
            public void run() {

                if (Looper.myLooper() == Looper.getMainLooper())
                    throw new IllegalStateException("Cannot be called from the main thread");

                AdvertisingConnection connection = new AdvertisingConnection();
                Intent intent = new Intent("com.google.android.gms.ads.identifier.service.START");
                intent.setPackage("com.google.android.gms");
                if (context.bindService(intent, connection, Context.BIND_AUTO_CREATE)) {
                    try {
                        AdvertisingInterface adInterface = new AdvertisingInterface(connection.getBinder());

                        //Save the Advertisement id & opt-out flag in local storage.
                        saveAndroidAid(context, adInterface.getId());
                        saveLimitedAdTrackingState(context, adInterface.isLimitAdTrackingEnabled(true));

                    } catch (Exception exception) {

                    } finally {
                        context.unbindService(connection);
                    }
                }
            }
        }).start();

        AdInfo adInfo = new AdInfo(AdvertisingIdClient.getAndroidAid(context, null),
                AdvertisingIdClient.getLimitedAdTrackingState(context, true));
        return adInfo;
    }

    public static final class AdInfo {
        private final String advertisingId;
        private final boolean limitAdTrackingEnabled;

        AdInfo(String advertisingId, boolean limitAdTrackingEnabled) {
            this.advertisingId = advertisingId;
            this.limitAdTrackingEnabled = limitAdTrackingEnabled;
        }

        public String getId() {
            return this.advertisingId;
        }

        public boolean isLimitAdTrackingEnabled() {
            return this.limitAdTrackingEnabled;
        }
    }

    private static final class AdvertisingConnection implements ServiceConnection {
        private final LinkedBlockingQueue<IBinder> queue = new LinkedBlockingQueue<IBinder>(1);
        boolean retrieved = false;

        public void onServiceConnected(ComponentName name, IBinder service) {
            try {
                this.queue.put(service);
            } catch (InterruptedException localInterruptedException) {
            }
        }

        public void onServiceDisconnected(ComponentName name) {
        }

        public IBinder getBinder() throws InterruptedException {
            if (this.retrieved) throw new IllegalStateException();
            this.retrieved = true;
            return this.queue.take();
        }
    }

    private static final class AdvertisingInterface implements IInterface {
        private IBinder binder;

        public AdvertisingInterface(IBinder pBinder) {
            binder = pBinder;
        }

        public IBinder asBinder() {
            return binder;
        }

        public String getId() throws RemoteException {
            Parcel data = Parcel.obtain();
            Parcel reply = Parcel.obtain();
            String id;
            try {
                data.writeInterfaceToken("com.google.android.gms.ads.identifier.internal.IAdvertisingIdService");
                binder.transact(1, data, reply, 0);
                reply.readException();
                id = reply.readString();
            } finally {
                reply.recycle();
                data.recycle();
            }
            return id;
        }

        public boolean isLimitAdTrackingEnabled(boolean paramBoolean) throws RemoteException {
            Parcel data = Parcel.obtain();
            Parcel reply = Parcel.obtain();
            boolean limitAdTracking;
            try {
                data.writeInterfaceToken("com.google.android.gms.ads.identifier.internal.IAdvertisingIdService");
                data.writeInt(paramBoolean ? 1 : 0);
                binder.transact(2, data, reply, 0);
                reply.readException();
                limitAdTracking = 0 != reply.readInt();
            } finally {
                reply.recycle();
                data.recycle();
            }
            return limitAdTracking;
        }
    }


}
